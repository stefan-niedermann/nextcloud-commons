package it.niedermann.android.markdown;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import it.niedermann.android.markdown.controller.Command;
import it.niedermann.android.markdown.controller.EditorState;
import it.niedermann.android.markdown.controller.EditorStateListener;
import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor;

/**
 * <ol>
 *     <li>
 *         Call {@link #inflateMenu(int)} or add the corresponding attribute in your XML layout.<br>
 *         Usually you want to use the shipped  {@link Menu} {@link R.menu#toolbar}
 *     </li>
 *     <li>
 *         Use {@link #setCommandMap(Map)} to map your custom {@link MenuItem}s to editor {@link Command}s<br>
 *         By default a mapping for all {@link MenuItem}s of {@link R.menu#toolbar} is provided.<br>
 *         Only needed if the used {@link Menu} is <em>not</em> {@link R.menu#toolbar}
 *     </li>
 * </ol>
 */
public class MarkdownToolbarController extends Toolbar implements MarkdownController, Toolbar.OnMenuItemClickListener {

    private static final String TAG = MarkdownToolbarController.class.getSimpleName();
    @Nullable
    private MarkwonMarkdownEditor editor;
    @NonNull
    private final Map<Integer, Command> commandMap = new HashMap<>();
    @Nullable
    private EditorState state;

    public MarkdownToolbarController(@NonNull Context context) {
        this(context, null);
    }

    public MarkdownToolbarController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarkdownToolbarController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnMenuItemClickListener(this);
        setCommandMap(Map.of(
                R.id.bold, Command.TOGGLE_BOLD,
                R.id.italic, Command.TOGGLE_ITALIC,
                R.id.checkbox, Command.TOGGLE_CHECKBOX_LIST,
                R.id.strikethrough, Command.TOGGLE_STRIKE_THROUGH,
                R.id.unordered_list, Command.TOGGLE_UNORDERED_LIST,
                R.id.ordered_list, Command.TOGGLE_ORDERED_LIST,
                R.id.blockquote, Command.TOGGLE_BLOCK_QUOTE,
                R.id.codeblock, Command.TOGGLE_CODE_BLOCK,
                R.id.link, Command.INSERT_LINK
        ));
    }

    @Override
    public void setEditor(@Nullable MarkwonMarkdownEditor editor) {
        this.editor = editor;
    }

    /**
     * @param commandMap {@link MenuItem#getItemId()} to {@link Command}
     */
    public void setCommandMap(@NonNull Map<Integer, Command> commandMap) {
        this.commandMap.clear();
        this.commandMap.putAll(commandMap);
    }

    @Override
    public void onEditorStateChanged(@NonNull EditorState state) {
        this.state = state;
        final var utils = ThemeUtils.Companion.of(state.color());
        for (int i = 0; i < getMenu().size(); i++) {
            final var item = getMenu().getItem(i);
            final var command = Optional.ofNullable(commandMap.get(item.getItemId()));

            if (command.isEmpty()) {
                continue;
            }

            final var icon = Optional.ofNullable(item.getIcon());
            icon.ifPresent(ic -> ic.setTintList(ColorStateList.valueOf(utils.getOnSurfaceVariant(getContext()))));

            final var commandState = Optional.ofNullable(state.commands().get(command.get()));

            if (commandState.isEmpty() || !commandState.get().enabled()) {
                item.setEnabled(false);
                icon.ifPresent(ic -> ic.setAlpha(128));
            } else {
                item.setEnabled(true);
                icon.ifPresent(ic -> ic.setAlpha(255));
            }
        }
        invalidateMenu();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (editor == null) {
            Log.v(TAG, MarkwonMarkdownEditor.class.getSimpleName() + " must be set before clicking on menu items is possible");
            return false;
        }

        if (state == null) {
            Log.v(TAG, MarkdownController.class.getSimpleName() + " must be registered as " + EditorStateListener.class.getSimpleName() + " on " + MarkdownEditorImpl.class.getSimpleName() + " before clicking on menu items is possible");
            return false;
        }

        final var command = Optional.ofNullable(commandMap.get(item.getItemId()));

        if (command.isEmpty()) {
            Log.v(TAG, "Couldn't find " + Command.class.getSimpleName() + " for " + MenuItem.class.getSimpleName() + " ID " + item.getTitle());
            return false;
        }

        final var commandState = Optional.ofNullable(state.commands().get(command.get()));

        if (commandState.isEmpty()) {
            Log.v(TAG, Command.class.getSimpleName() + " for " + MenuItem.class.getSimpleName() + " is not included in the current " + EditorState.class.getSimpleName());
            return false;
        }

        if (!commandState.get().enabled()) {
            Log.v(TAG, Command.class.getSimpleName() + " for " + MenuItem.class.getSimpleName() + " is not enabled in the current " + EditorState.class.getSimpleName());
            return false;
        }

        try {
            editor.executeCommand(command.get());
            return true;
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        }

        return false;
    }
}
