package it.niedermann.android.markdown;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.QuoteSpan;
import android.util.Log;
import android.util.Pair;
import android.widget.RemoteViews.RemoteView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import com.nextcloud.android.common.ui.theme.utils.AndroidViewThemeUtils;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.noties.markwon.Markwon;
import it.niedermann.android.markdown.model.EListType;
import it.niedermann.android.markdown.model.SearchSpan;
import it.niedermann.android.markdown.remoteviews.RemoteViewElement;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class MarkdownUtil {

    private static final String TAG = MarkdownUtil.class.getSimpleName();

    private static final Parser PARSER = Parser.builder().build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().softbreak("<br>").build();

    private static final Pattern PATTERN_CODE_FENCE = Pattern.compile("^(`{3,})");
    private static final Pattern PATTERN_ORDERED_LIST_ITEM = Pattern.compile("^(\\d+)\\.\\s.+$");
    private static final Pattern PATTERN_ORDERED_LIST_ITEM_EMPTY = Pattern.compile("^(\\d+)\\.\\s$");
    private static final Pattern PATTERN_MARKDOWN_LINK = Pattern.compile("\\[(.+)?]\\(([^ ]+?)?( \"(.+)\")?\\)");

    private static final String PATTERN_QUOTE_BOLD_PUNCTUATION = Pattern.quote("**");

    private static final Optional<String> CHECKBOX_CHECKED_EMOJI = getCheckboxEmoji(true);
    private static final Optional<String> CHECKBOX_UNCHECKED_EMOJI = getCheckboxEmoji(false);

    private MarkdownUtil() {
        // Util class
    }


    /**
     * {@link RemoteView}s have a limited subset of supported classes to maintain compatibility with many different launchers.
     * <p>
     * This function splits markdown into elements that can be displayed by a widget natively.
     * Currently supported are: Markdown text, and Checkbox
     *
     * @return {@link ArrayList<RemoteViewElement>} of elements from the {@param content}.
     * @noinspection unused
     */
    public static ArrayList<RemoteViewElement> getRenderedElementsForRemoteView(@NonNull Context context, @NonNull String content) {

        ArrayList<RemoteViewElement> remoteViews = new ArrayList<>();
        StringBuilder currentLineBlock = new StringBuilder();
        int startLine = 0;

        final String[] lines = content.split("\n");
        boolean isInFencedCodeBlock = false;
        int fencedCodeBlockSigns = 0;
        for (int i = 0; i < lines.length; i++) {
            final var matcher = PATTERN_CODE_FENCE.matcher(lines[i]);
            if (matcher.find()) {
                final String fence = matcher.group(1);
                if (fence != null) {
                    int currentFencedCodeBlockSigns = fence.length();
                    if (isInFencedCodeBlock) {
                        if (currentFencedCodeBlockSigns == fencedCodeBlockSigns) {
                            isInFencedCodeBlock = false;
                            fencedCodeBlockSigns = 0;
                        }
                    } else {
                        isInFencedCodeBlock = true;
                        fencedCodeBlockSigns = currentFencedCodeBlockSigns;
                    }
                }
            }
            if (!isInFencedCodeBlock) {
                if (isCheckboxLine(lines[i])) {
                    // if the first line is a checkbox, this will be an empty markdown block. It will also end in line -1.
                    var endline = i - 1;
                    if (endline < 0) {
                        endline = 0;
                    }
                    remoteViews.add(new RemoteViewElement(RemoteViewElement.Type.TEXT, currentLineBlock.toString(), startLine, endline));
                    currentLineBlock = new StringBuilder();
                    startLine = i + 1;

                    boolean isChecked = false;
                    for (EListType listType : EListType.values()) {
                        if (lineStartsWithCheckedCheckbox(lines[i], listType)) {
                            isChecked = true;
                        }
                    }
                    if (isChecked) {
                        remoteViews.add(new RemoteViewElement(RemoteViewElement.Type.CHECKBOX_CHECKED, lines[i], i, i));
                    } else {
                        remoteViews.add(new RemoteViewElement(RemoteViewElement.Type.CHECKBOX_UNCHECKED, lines[i], i, i));
                    }
                    continue;
                }
            }
            currentLineBlock.append(lines[i]).append("\n");
        }
        return remoteViews;
    }

    /**
     * {@link RemoteView}s have a limited subset of supported classes to maintain compatibility with many different launchers.
     * <p>
     * Since {@link Markwon} makes heavy use of custom spans, this won't look nice e. g. at app widgets, because they simply won't be rendered.
     * Therefore we currently use {@link HtmlCompat} to filter supported spans from the output of {@link HtmlRenderer} as an intermediate step.
     *
     * @noinspection unused
     */
    public static CharSequence renderForRemoteView(@NonNull Context context, @NonNull String content) {
        // Create HTML string from Markup
        final String html = RENDERER.render(PARSER.parse(replaceCheckboxesWithEmojis(content)));

        // Create Spanned from HTML, with special handling for ordered list items
        final Spanned spanned = HtmlCompat.fromHtml(ListTagHandler.prepareTagHandling(html), 0, null, new ListTagHandler());

        // Enhance colors and margins of the Spanned
        return customizeQuoteSpanAppearance(context, spanned, 5, 30);
    }

    @SuppressWarnings("SameParameterValue")
    private static Spanned customizeQuoteSpanAppearance(@NonNull Context context, @NonNull Spanned input, int stripeWidth, int gapWidth) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return input;
        }
        final var ssb = new SpannableStringBuilder(input);
        final var originalQuoteSpans = ssb.getSpans(0, ssb.length(), QuoteSpan.class);
        @ColorInt final int colorBlockQuote = ContextCompat.getColor(context, R.color.block_quote);
        for (final var originalQuoteSpan : originalQuoteSpans) {
            final int start = ssb.getSpanStart(originalQuoteSpan);
            final int end = ssb.getSpanEnd(originalQuoteSpan);
            ssb.removeSpan(originalQuoteSpan);
            ssb.setSpan(new QuoteSpan(colorBlockQuote, stripeWidth, gapWidth), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ssb;
    }

    @NonNull
    public static String replaceCheckboxesWithEmojis(@NonNull String content) {
        return runForEachCheckbox(content, (line) -> {
            for (final var listType : EListType.values()) {
                if (CHECKBOX_CHECKED_EMOJI.isPresent()) {
                    line = line.replace(listType.checkboxChecked, CHECKBOX_CHECKED_EMOJI.get());
                    line = line.replace(listType.checkboxCheckedUpperCase, CHECKBOX_CHECKED_EMOJI.get());
                }
                if (CHECKBOX_UNCHECKED_EMOJI.isPresent()) {
                    line = line.replace(listType.checkboxUnchecked, CHECKBOX_UNCHECKED_EMOJI.get());
                }
            }
            return line;
        });
    }

    @NonNull
    private static Optional<String> getCheckboxEmoji(boolean checked) {
        final String[] emojis;
        // Seriously what the fuck, Samsung?
        // https://emojipedia.org/ballot-box-with-x#designs
        if (Build.MANUFACTURER != null && Build.MANUFACTURER.toLowerCase(Locale.getDefault()).contains("samsung")) {
            emojis = checked
                    ? new String[]{"✅", "☑️", "✔️"}
                    : new String[]{"❌", "\uD83D\uDD32️", "☐️"};
        } else {
            emojis = checked
                    ? new String[]{"☒", "✅", "☑️", "✔️"}
                    : new String[]{"☐", "❌", "\uD83D\uDD32️", "☐️"};
        }
        final var paint = new Paint();
        for (String emoji : emojis) {
            if (paint.hasGlyph(emoji)) {
                return Optional.of(emoji);
            }
        }
        return Optional.empty();
    }

    /**
     * Performs the given {@param map} function for each line which contains a checkbox
     */
    @NonNull
    private static String runForEachCheckbox(@NonNull String markdownString, @NonNull Function<String, String> map) {
        final String[] lines = markdownString.split("\n");
        boolean isInFencedCodeBlock = false;
        int fencedCodeBlockSigns = 0;
        for (int i = 0; i < lines.length; i++) {
            final var matcher = PATTERN_CODE_FENCE.matcher(lines[i]);
            if (matcher.find()) {
                final String fence = matcher.group(1);
                if (fence != null) {
                    int currentFencedCodeBlockSigns = fence.length();
                    if (isInFencedCodeBlock) {
                        if (currentFencedCodeBlockSigns == fencedCodeBlockSigns) {
                            isInFencedCodeBlock = false;
                            fencedCodeBlockSigns = 0;
                        }
                    } else {
                        isInFencedCodeBlock = true;
                        fencedCodeBlockSigns = currentFencedCodeBlockSigns;
                    }
                }
            }
            if (!isInFencedCodeBlock) {
                if (isCheckboxLine(lines[i])) {
                    lines[i] = map.apply(lines[i]);
                }
            }
        }
        return TextUtils.join("\n", lines);
    }

    public static boolean isCheckboxLine(String line) {
        return lineStartsWithCheckbox(line) && line.trim().length() > EListType.DASH.checkboxChecked.length();
    }

    public static String getLine(@NonNull CharSequence s, int cursorPosition) {
        final int startOfLine = getStartOfLine(s, cursorPosition);
        final int endOfLine = getEndOfLine(s, startOfLine);
        return s.subSequence(startOfLine, endOfLine).toString();
    }

    public static boolean isMultilineSelection(@NonNull Spannable content, int selectionStart, int selectionEnd) {
        if (selectionStart > content.length()) {
            throw new IndexOutOfBoundsException("selection start was " + selectionStart + " but content length was only " + content.length());
        }

        if (selectionEnd > content.length()) {
            throw new IndexOutOfBoundsException("selection end was " + selectionEnd + " but content length was only " + content.length());
        }

        if (selectionStart > selectionEnd) {
            throw new IllegalArgumentException("selection end must be greater or equal to selection start");
        }
        return content.subSequence(selectionStart, selectionEnd).toString().contains("\n");
    }

    public static boolean isSinglelineSelection(@NonNull Spannable content, int selectionStart, int selectionEnd) {
        return !isMultilineSelection(content, selectionStart, selectionEnd);
    }

    public static int getStartOfLine(@NonNull CharSequence s, int cursorPosition) {
        int startOfLine = cursorPosition;
        while (startOfLine > 0 && s.charAt(startOfLine - 1) != '\n') {
            startOfLine--;
        }
        return startOfLine;
    }

    public static int getEndOfLine(@NonNull CharSequence s, int cursorPosition) {
        int nextLinebreak = s.toString().indexOf('\n', cursorPosition);
        if (nextLinebreak > -1) {
            return nextLinebreak;
        }
        return s.length();
    }

    public static Optional<String> getListItemIfIsEmpty(@NonNull String line) {
        final String trimmedLine = line.trim();
        final int indention = line.indexOf(trimmedLine);
        final var builder = new StringBuilder(" ".repeat(indention));
        for (final var listType : EListType.values()) {
            if (trimmedLine.equals(listType.checkboxUnchecked)) {
                return Optional.of(builder.append(listType.checkboxUncheckedWithTrailingSpace).toString());
            } else if (trimmedLine.equals(listType.listSymbol)) {
                return Optional.of(builder.append(listType.listSymbolWithTrailingSpace).toString());
            }
        }
        final var matcher = PATTERN_ORDERED_LIST_ITEM_EMPTY.matcher(line.substring(indention));
        if (matcher.find()) {
            return Optional.of(builder.append(matcher.group()).toString());
        }
        return Optional.empty();
    }

    public static CharSequence setCheckboxStatus(@NonNull String markdownString, int targetCheckboxIndex, boolean newCheckedState) {
        final String[] lines = markdownString.split("\n");
        int checkboxIndex = 0;
        boolean isInFencedCodeBlock = false;
        int fencedCodeBlockSigns = 0;
        for (int i = 0; i < lines.length; i++) {
            final var matcher = PATTERN_CODE_FENCE.matcher(lines[i]);
            if (matcher.find()) {
                final String fence = matcher.group(1);
                if (fence != null) {
                    int currentFencedCodeBlockSigns = fence.length();
                    if (isInFencedCodeBlock) {
                        if (currentFencedCodeBlockSigns == fencedCodeBlockSigns) {
                            isInFencedCodeBlock = false;
                            fencedCodeBlockSigns = 0;
                        }
                    } else {
                        isInFencedCodeBlock = true;
                        fencedCodeBlockSigns = currentFencedCodeBlockSigns;
                    }
                }
            }
            if (!isInFencedCodeBlock) {
                if (lineStartsWithCheckbox(lines[i]) && lines[i].trim().length() > EListType.DASH.checkboxChecked.length()) {
                    if (checkboxIndex == targetCheckboxIndex) {
                        final int indexOfStartingBracket = lines[i].indexOf("[");
                        final String toggledLine = lines[i].substring(0, indexOfStartingBracket + 1) +
                                (newCheckedState ? 'x' : ' ') +
                                lines[i].substring(indexOfStartingBracket + 2);
                        lines[i] = toggledLine;
                        break;
                    }
                    checkboxIndex++;
                }
            }
        }
        return TextUtils.join("\n", lines);
    }

    public static Optional<EListType> lineStartsWithList(@NonNull String line) {
        for (EListType listType : EListType.values()) {
            if (lineStartsWithList(line, listType)) {
                return Optional.of(listType);
            }
        }
        return Optional.empty();
    }

    public static boolean lineStartsWithList(@NonNull String line, @NonNull EListType listType) {
        final String trimmedLine = line.trim();
        return (trimmedLine.startsWith(listType.listSymbolWithTrailingSpace));
    }

    public static boolean lineStartsWithCheckbox(@NonNull String line) {
        for (EListType listType : EListType.values()) {
            if (lineStartsWithCheckbox(line, listType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean lineStartsWithCheckbox(@NonNull String line, @NonNull EListType listType) {
        return lineStartsWithCheckedCheckbox(line, listType) | lineStartsWithUncheckedCheckbox(line, listType);
    }

    public static boolean lineStartsWithCheckedCheckbox(@NonNull String line, @NonNull EListType listType) {
        final String trimmedLine = line.trim();
        return (trimmedLine.startsWith(listType.checkboxChecked) || trimmedLine.startsWith(listType.checkboxCheckedUpperCase));
    }

    public static boolean lineStartsWithUncheckedCheckbox(@NonNull String line, @NonNull EListType listType) {
        final String trimmedLine = line.trim();
        return (trimmedLine.startsWith(listType.checkboxUnchecked));
    }


    /**
     * @return the number of the ordered list item if the line is an ordered list, otherwise -1.
     */
    public static Optional<Integer> getOrderedListNumber(@NonNull String line) {
        final var matcher = PATTERN_ORDERED_LIST_ITEM.matcher(line);
        if (matcher.find()) {
            final String groupNumber = matcher.group(1);
            if (groupNumber != null) {
                try {
                    return Optional.of(Integer.parseInt(groupNumber));
                } catch (NumberFormatException e) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Modifies the {@param editable} and adds the given {@param punctuation} from
     * {@param selectionStart} to {@param selectionEnd} or removes the {@param punctuation} in case
     * it already is around the selected part.
     *
     * @return the new cursor position
     */
    public static int togglePunctuation(@NonNull Editable editable, int selectionStart, int selectionEnd, @NonNull String punctuation) {
        final String initialString = editable.toString();
        if (selectionStart < 0 || selectionStart > initialString.length() || selectionEnd < 0 || selectionEnd > initialString.length()) {
            return 0;
        }

        // handle special case: italic (that damn thing will match like ANYTHING (regarding bold / bold+italic)....)
        final boolean isItalic = punctuation.length() == 1 && punctuation.charAt(0) == '*';
        if (isItalic) {
            final var result = handleItalicEdgeCase(editable, initialString, selectionStart, selectionEnd);
            // The result is only present if this actually was an edge case
            if (result.isPresent()) {
                return result.get();
            }
        }

        // handle the simple cases
        final String wildcardRex = "([^" + punctuation.charAt(0) + "])+";
        final String punctuationRex = Pattern.quote(punctuation);
        final String pattern = isItalic
                // in this case let's make optional asterisks around it, so it wont match anything between two (bold+italic)s
                ? "\\*?\\*?" + punctuationRex + wildcardRex + punctuationRex + "\\*?\\*?"
                : punctuationRex + wildcardRex + punctuationRex;
        final var searchPattern = Pattern.compile(pattern);
        int relevantStart = selectionStart - 2;
        relevantStart = Math.max(relevantStart, 0);
        int relevantEnd = selectionEnd + 2;
        relevantEnd = Math.min(relevantEnd, initialString.length());
        final Matcher matcher = searchPattern.matcher(initialString).region(relevantStart, relevantEnd);

        // if the matcher matches, it's a remove
        if (matcher.find()) {
            // this resets the matcher, while keeping the required region
            matcher.region(relevantStart, relevantEnd);
            final int punctuationLength = punctuation.length();
            final var startEnd = new LinkedList<Pair<Integer, Integer>>();
            int removedCount = 0;
            while (matcher.find()) {
                startEnd.add(new Pair<>(matcher.start(), matcher.end()));
                removedCount += punctuationLength;
            }
            // start from the end
            Collections.reverse(startEnd);
            for (final var item : startEnd) {
                deletePunctuation(editable, punctuationLength, item.first, item.second);
            }
            int offsetAtEnd = 0;
            // depending on if the user has selected the markdown chars, we might need to add an offset to the resulting cursor position
            if (initialString.substring(Math.max(selectionEnd - punctuationLength + 1, 0), Math.min(selectionEnd + 1, initialString.length())).equals(punctuation) ||
                    initialString.substring(selectionEnd, Math.min(selectionEnd + punctuationLength, initialString.length())).equals(punctuation)) {
                offsetAtEnd = punctuationLength;
            }
            return selectionEnd - removedCount * 2 + offsetAtEnd;
            //                                 ^
            //         start+end, need to double
        }

        // do nothing when punctuation is contained only once
        if (Pattern.compile(punctuationRex).matcher(initialString).region(selectionStart, selectionEnd).find()) {
            return selectionEnd;
        }

        // nothing returned so far, so it has to be an insertion
        return insertPunctuation(editable, selectionStart, selectionEnd, punctuation);
    }

    private static void deletePunctuation(Editable editable, int punctuationLength, int start, int end) {
        editable.delete(end - punctuationLength, end);
        editable.delete(start, start + punctuationLength);
    }

    /**
     * @return an {@link Optional<Integer>} of the new cursor position.
     * The return value is only {@link Optional#isPresent()}, if this is an italic edge case.
     */
    @NonNull
    private static Optional<Integer> handleItalicEdgeCase(Editable editable, String editableAsString, int selectionStart, int selectionEnd) {
        // look if selection is bold, this is the only edge case afaik
        final var searchPattern = Pattern.compile("(^|[^*])" + PATTERN_QUOTE_BOLD_PUNCTUATION + "([^*])*" + PATTERN_QUOTE_BOLD_PUNCTUATION + "([^*]|$)");
        // look the selection expansion by 1 is intended, so the NOT '*' has a chance to match. we don't want to match ***blah***
        final var matcher = searchPattern.matcher(editableAsString)
                .region(Math.max(selectionStart - 1, 0), Math.min(selectionEnd + 1, editableAsString.length()));
        if (matcher.find()) {
            return Optional.of(insertPunctuation(editable, selectionStart, selectionEnd, "*"));
        }
        // look around (3 chars) (NOT '*' + "**"). User might have selected the text only
        if (matcher.region(Math.max(selectionStart - 3, 0), Math.min(selectionEnd + 3, editableAsString.length())).find()) {
            return Optional.of(insertPunctuation(editable, selectionStart, selectionEnd, "*"));
        }
        return Optional.empty();
    }

    private static int insertPunctuation(Editable editable, int firstPosition, int secondPosition, String punctuation) {
        editable.insert(secondPosition, punctuation);
        editable.insert(firstPosition, punctuation);
        return secondPosition + punctuation.length();
    }

    /**
     * Inserts a link into the given {@param editable} from {@param selectionStart} to {@param selectionEnd} and uses the {@param clipboardUrl} if available.
     *
     * @return the new cursor position
     */
    public static int insertLink(@NonNull Editable editable, int selectionStart, int selectionEnd, @Nullable String clipboardUrl) {
        if (selectionStart == selectionEnd) {
            if (selectionStart > 0 && selectionEnd < editable.length()) {
                char start = editable.charAt(selectionStart - 1);
                char end = editable.charAt(selectionEnd);
                if (start == ' ' || end == ' ') {
                    if (start != ' ') {
                        editable.insert(selectionStart, " ");
                        selectionStart += 1;
                    }
                    if (end != ' ') {
                        editable.insert(selectionEnd, " ");
                    }
                    editable.insert(selectionStart, "[](" + (clipboardUrl == null ? "" : clipboardUrl) + ")");
                    return selectionStart + 1;

                } else {
                    while (start != ' ') {
                        selectionStart--;
                        start = editable.charAt(selectionStart);
                    }
                    selectionStart++;
                    while (end != ' ') {
                        selectionEnd++;
                        end = editable.charAt(selectionEnd);
                    }
                    selectionEnd++;
                    editable.insert(selectionStart, "[");
                    editable.insert(selectionEnd, "](" + (clipboardUrl == null ? "" : clipboardUrl) + ")");
                    if (clipboardUrl != null) {
                        selectionEnd += clipboardUrl.length();
                    }
                    return selectionEnd + 2;
                }
            } else {
                editable.insert(selectionStart, "[](" + (clipboardUrl == null ? "" : clipboardUrl) + ")");
                return selectionStart + 1;
            }

        } else {
            final boolean textToFormatIsLink = TextUtils.indexOf(editable.subSequence(selectionStart, selectionEnd), "http") == 0;
            if (textToFormatIsLink) {
                if (clipboardUrl == null) {
                    editable.insert(selectionEnd, ")");
                    editable.insert(selectionStart, "[](");
                } else {
                    editable.insert(selectionEnd, "](" + clipboardUrl + ")");
                    editable.insert(selectionStart, "[");
                    selectionEnd += clipboardUrl.length();
                }
            } else {
                if (clipboardUrl == null) {
                    editable.insert(selectionEnd, "]()");
                } else {
                    editable.insert(selectionEnd, "](" + clipboardUrl + ")");
                    selectionEnd += clipboardUrl.length();
                }
                editable.insert(selectionStart, "[");
            }
            return textToFormatIsLink && clipboardUrl == null
                    ? selectionStart + 1
                    : selectionEnd + 3;
        }
    }


    public static boolean selectionIsInLink(@NonNull CharSequence text, int start, int end) {
        final var matcher = PATTERN_MARKDOWN_LINK.matcher(text);
        while (matcher.find()) {
            if ((start >= matcher.start() && start < matcher.end()) || (end > matcher.start() && end <= matcher.end())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @deprecated use {@link AndroidViewThemeUtils#highlightText(TextView, String, String)}
     */
    @Deprecated(forRemoval = true)
    public static void searchAndColor(@NonNull Spannable editable, @Nullable CharSequence searchText, @Nullable Integer current, @ColorInt int color, @ColorInt int ignoredColor, boolean ignoredDarkTheme) {
        try {
            @SuppressLint("PrivateApi") final var context = (Context) Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication")
                    .invoke(null, (Object[]) null);

            searchAndColor(Objects.requireNonNull(context), editable, searchText, color, current);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void searchAndColor(@NonNull Context context, @NonNull Spannable editable, @Nullable CharSequence searchText, @ColorInt int color, @Nullable Integer current) {
        final var util = ThemeUtils.Companion.of(color);
        if (searchText != null) {
            final var m = Pattern
                    .compile(searchText.toString(), Pattern.CASE_INSENSITIVE | Pattern.LITERAL)
                    .matcher(editable);

            int i = 1;
            while (m.find()) {
                int start = m.start();
                int end = m.end();
                final var span = current == null || i == current
                        ? new SearchSpan(util.getPrimary(context), util.getOnPrimary(context))
                        : new SearchSpan(util.getSecondary(context), util.getOnSecondary(context));
                editable.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                i++;
            }
        }
    }

    /**
     * Removes all spans of {@param spanType} from {@param spannable}.
     */
    public static <T> void removeSpans(@NonNull Spannable spannable, @SuppressWarnings("SameParameterValue") Class<T> spanType) {
        for (final var span : spannable.getSpans(0, spannable.length(), spanType)) {
            spannable.removeSpan(span);
        }
    }

    /**
     * @return When the content of the {@param textView} is already of type {@link Spannable}, it will cast and return it directly.
     * Otherwise it will create a new {@link SpannableString} from the content, set this as new content of the {@param textView} and return it.
     */
    public static Spannable getContentAsSpannable(@NonNull TextView textView) {
        final var content = textView.getText();
        if (content.getClass() == SpannableString.class || content instanceof Spannable) {
            return (Spannable) content;
        } else {
            Log.w(TAG, "Expected " + TextView.class.getSimpleName() + " content to be of type " + Spannable.class.getSimpleName() + ", but was of type " + content.getClass() + ". Search highlighting will be not performant.");
            final var spannableContent = new SpannableString(content);
            textView.setText(spannableContent, TextView.BufferType.SPANNABLE);
            return spannableContent;
        }
    }

    public static String getMarkdownLink(@NonNull String content, @NonNull String url) {
        return "[" + content + "](" + url + ")";
    }

    public static String getMarkdownEmbedded(@NonNull String content, @NonNull String url) {
        return "!" + getMarkdownLink(content, url);
    }

    /**
     * Strips all Markdown from {@param s}
     *
     * @param s Markdown string
     * @return Plain text string
     */
    @NonNull
    public static String removeMarkdown(@Nullable String s) {
        if (TextUtils.isEmpty(s)) {
            return "";
        }
        final String html = RENDERER.render(PARSER.parse(replaceCheckboxesWithEmojis(s)));
        return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT).toString().trim();
    }
}
