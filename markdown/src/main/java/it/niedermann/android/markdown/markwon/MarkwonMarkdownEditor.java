package it.niedermann.android.markdown.markwon;

import android.content.Context;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.EditText;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

import io.noties.markwon.Markwon;
import io.noties.markwon.editor.MarkwonEditor;
import io.noties.markwon.editor.handler.EmphasisEditHandler;
import io.noties.markwon.editor.handler.StrongEmphasisEditHandler;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.image.ImagesPlugin;
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin;
import io.noties.markwon.simple.ext.SimpleExtPlugin;
import it.niedermann.android.markdown.MarkdownEditor;
import it.niedermann.android.markdown.MarkdownUtil;
import it.niedermann.android.markdown.controller.Command;
import it.niedermann.android.markdown.controller.CommandReceiver;
import it.niedermann.android.markdown.controller.EditorStateListener;
import it.niedermann.android.markdown.controller.EditorStateNotifier;
import it.niedermann.android.markdown.controller.MarkdownController;
import it.niedermann.android.markdown.markwon.format.ContextBasedFormattingCallback;
import it.niedermann.android.markdown.markwon.handler.BlockQuoteEditHandler;
import it.niedermann.android.markdown.markwon.handler.CodeBlockEditHandler;
import it.niedermann.android.markdown.markwon.handler.CodeEditHandler;
import it.niedermann.android.markdown.markwon.handler.HeadingEditHandler;
import it.niedermann.android.markdown.markwon.handler.StrikethroughEditHandler;
import it.niedermann.android.markdown.markwon.plugins.SearchHighlightPlugin;
import it.niedermann.android.markdown.markwon.plugins.ThemePlugin;
import it.niedermann.android.markdown.markwon.textwatcher.CombinedTextWatcher;
import it.niedermann.android.markdown.markwon.textwatcher.SearchHighlightTextWatcher;

public class MarkwonMarkdownEditor extends AppCompatEditText implements MarkdownEditor, CommandReceiver {

    private static final String TAG = MarkwonMarkdownEditor.class.getSimpleName();

    @Nullable
    private Consumer<CharSequence> listener;
    @Nullable
    private final Set<MarkdownController> controllers = ConcurrentHashMap.newKeySet(2);
    private final EditorStateNotifier editorStateNotifier;
    private final MutableLiveData<CharSequence> unrenderedText$ = new MutableLiveData<>();
    private final CombinedTextWatcher combinedWatcher;
    @ColorInt
    private int color;

    public MarkwonMarkdownEditor(@NonNull Context context) {
        this(context, null);
    }

    public MarkwonMarkdownEditor(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public MarkwonMarkdownEditor(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, EditorStateNotifier::new);
    }

    @VisibleForTesting
    protected MarkwonMarkdownEditor(@NonNull Context context,
                                    @Nullable AttributeSet attrs,
                                    int defStyleAttr,
                                    @NonNull Function<Collection<? extends EditorStateListener>, EditorStateNotifier> editStateNotifierFactory) {
        super(context, attrs, defStyleAttr);

        final var typedValue = new TypedValue();
        final var theme = context.getTheme();
        theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);
        this.color = typedValue.data;

        this.editorStateNotifier = editStateNotifierFactory.apply(controllers);
        final var markwon = createMarkwonBuilder(context, color).build();
        final var editor = createMarkwonEditorBuilder(markwon).build();

        combinedWatcher = new CombinedTextWatcher(editor, this);
        addTextChangedListener(combinedWatcher);

        final var actionModeCallback = new ContextBasedFormattingCallback();
        setCustomSelectionActionModeCallback(actionModeCallback);
        setCustomInsertionActionModeCallback(actionModeCallback);
        registerController(actionModeCallback);
    }

    private static Markwon.Builder createMarkwonBuilder(@NonNull Context context, @ColorInt int color) {
        return Markwon.builder(context)
                .usePlugin(ThemePlugin.create(context))
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(SimpleExtPlugin.create())
                .usePlugin(ImagesPlugin.create())
                .usePlugin(MarkwonInlineParserPlugin.create())
                .usePlugin(SearchHighlightPlugin.create(color));
    }

    private static MarkwonEditor.Builder createMarkwonEditorBuilder(@NonNull Markwon markwon) {
        return MarkwonEditor.builder(markwon)
                .useEditHandler(new EmphasisEditHandler())
                .useEditHandler(new StrongEmphasisEditHandler())
                .useEditHandler(new StrikethroughEditHandler())
                .useEditHandler(new CodeEditHandler())
                .useEditHandler(new CodeBlockEditHandler())
                .useEditHandler(new BlockQuoteEditHandler())
                .useEditHandler(new HeadingEditHandler());
    }

    /**
     * @param color which will be used for highlighting. See {@link #setSearchText(CharSequence)}
     * @deprecated Use {@link MarkdownEditor#setCurrentSingleSignOnAccount(SingleSignOnAccount, int)}
     */
    @Override
    @Deprecated(forRemoval = true)
    public void setSearchColor(int color) {
        try {
            final var ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(getContext());
            setCurrentSingleSignOnAccount(ssoAccount, color);
        } catch (NoCurrentAccountSelectedException | NextcloudFilesAppAccountNotFoundException e) {
            setCurrentSingleSignOnAccount(null, color);
        }
    }

    @Override
    public void setCurrentSingleSignOnAccount(@Nullable SingleSignOnAccount account, @ColorInt int color) {
        this.color = color;
        notifyControllers();
        final var searchHighlightTextWatcher = combinedWatcher.get(SearchHighlightTextWatcher.class);
        searchHighlightTextWatcher.setSearchColor(color);
    }

    @Override
    public void setSearchText(@Nullable CharSequence searchText, @Nullable Integer current) {
        final var searchHighlightTextWatcher = combinedWatcher.get(SearchHighlightTextWatcher.class);
        searchHighlightTextWatcher.setSearchText(searchText, current);
    }

    @Override
    public void setMarkdownString(CharSequence text) {
        setText(text);
        setMarkdownStringModel(text);
        notifyControllers();
    }

    @Override
    public void setMarkdownString(CharSequence text, Runnable afterRender) {
        throw new UnsupportedOperationException("This is not available in " + MarkwonMarkdownEditor.class.getSimpleName() + " because the text is getting rendered all the time.");
    }

    /**
     * Updates the current model which matches the rendered state of the editor *without* triggering
     * anything of the native {@link EditText}
     *
     * @deprecated will be library private in the next release. Use {@link #setMarkdownString(CharSequence)}
     */
    @Deprecated
    public void setMarkdownStringModel(CharSequence text) {
        unrenderedText$.setValue(text == null ? "" : text.toString());
        if (listener != null) {
            listener.accept(unrenderedText$.getValue());
        }
    }

    /**
     * @deprecated use {@link #setMarkdownStringChangedListener(Consumer)}
     */
    @Override
    @Deprecated
    public LiveData<CharSequence> getMarkdownString() {
        return unrenderedText$;
    }

    @Override
    public void setMarkdownStringChangedListener(@Nullable Consumer<CharSequence> listener) {
        this.listener = listener;
    }

    /**
     * ⚠ This is a <strong>BETA</strong> feature. Please be careful. API changes can happen anytime and won't be announced!
     */
    @NonNull
    @Override
    public Future<Void> registerController(@NonNull MarkdownController controller) {
        Log.w(TAG, "⚠ This is a BETA feature. Please be careful. API changes can happen anytime and won't be announced!");

        if (controllers == null) {
            final var npe = new NullPointerException();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                return CompletableFuture.failedFuture(npe);
            } else {
                final var future = new CompletableFuture<Void>();
                future.completeExceptionally(npe);
                return future;
            }
        }

        controllers.add(controller);
        controller.setCommandReceiver(this);
        return editorStateNotifier.forceNotify(getContext(),
                controller,
                isEnabled(),
                this.color,
                MarkdownUtil.getContentAsSpannable(this),
                getSelectionStart(),
                getSelectionEnd());
    }

    /**
     * ⚠ This is a <strong>BETA</strong> feature. Please be careful. API changes can happen anytime and won't be announced!
     */
    @Override
    public void unregisterController(@NonNull MarkdownController controller) {
        Log.w(TAG, "⚠ This is a BETA feature. Please be careful. API changes can happen anytime and won't be announced!");

        if (controllers == null) {
            return;
        }

        controller.setCommandReceiver(null);
        controllers.remove(controller);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (controllers != null) {
            controllers.forEach(this::unregisterController);
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        notifyControllers();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        notifyControllers();
    }

    private void notifyControllers() {
        if (editorStateNotifier == null) {
            CompletableFuture.completedFuture(null);
            return; // Called during constructor
        }

        editorStateNotifier.notify(
                getContext(),
                isEnabled(),
                this.color,
                MarkdownUtil.getContentAsSpannable(this),
                getSelectionStart(),
                getSelectionEnd());
    }

    /**
     * ⚠ This is a <strong>BETA</strong> feature. Please be careful. API changes can happen anytime and won't be announced!
     */
    public void executeCommand(@NonNull Command command) throws UnsupportedOperationException {
        Log.w(TAG, "⚠ This is a BETA feature. Please be careful. API changes can happen anytime and won't be announced!");

        final var spannable = new SpannableStringBuilder(MarkdownUtil.getContentAsSpannable(this));
        final var start = getSelectionStart();
        final var end = getSelectionEnd();

        if (!command.isEnabled(getContext(), spannable, start, end)) {
            throw new UnsupportedOperationException();
        }

        final var result = command.applyCommand(getContext(), spannable, start, end);

        if (result.isEmpty()) {
            throw new UnsupportedOperationException();
        }

        setMarkdownString(result.get().content());
        setSelection(result.get().selection());
    }
}
