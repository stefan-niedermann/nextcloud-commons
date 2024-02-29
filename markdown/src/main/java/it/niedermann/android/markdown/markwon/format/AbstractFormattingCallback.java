package it.niedermann.android.markdown.markwon.format;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import it.niedermann.android.markdown.MarkdownController;
import it.niedermann.android.markdown.controller.Command;
import it.niedermann.android.markdown.controller.CommandState;
import it.niedermann.android.markdown.controller.EditorState;
import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor;

public abstract class AbstractFormattingCallback implements ActionMode.Callback, MarkdownController {

    @NonNull
    protected final Map<Integer, Command> commandMap;
    @Nullable
    protected MarkwonMarkdownEditor editor;
    @Nullable
    private EditorState state;
    @MenuRes
    private final int menuId;

    public AbstractFormattingCallback(@MenuRes int menuId,
                                      @NonNull Map<Integer, Command> commandMap) {
        this.menuId = menuId;
        this.commandMap = new HashMap<>(commandMap);
    }

    @Override
    public void setEditor(@Nullable MarkwonMarkdownEditor editor) {
        if (this.editor == null && editor != null) {
            this.editor = editor;
            this.editor.registerController(this);
        } else if (this.editor != editor) {
            this.editor.unregisterController(this);
            this.editor = editor;
            if (this.editor != null) {
                this.editor.registerController(this);
            }
        }
    }

    @Override
    public void onEditorStateChanged(@NonNull EditorState state) {
        this.state = state;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(menuId, menu);
        return true;
    }

    @CallSuper
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        commandMap.keySet().forEach(itemId -> setMenuItemVisibility(menu, itemId));
        return false;
    }

    protected void setMenuItemVisibility(@NonNull Menu menu, @IdRes int itemId) {
        final var command = Objects.requireNonNull(commandMap.get(itemId));
        getCommandState(command).ifPresentOrElse(
                state -> menu.findItem(itemId).setVisible(state.enabled()),
                () -> menu.findItem(itemId).setVisible(false));
    }

    protected Optional<CommandState> getCommandState(@NonNull Command command) {
        if (state == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(state.commands().get(command));
    }

    @CallSuper
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (editor == null) {
            return false;
        }

        final int itemId = item.getItemId();
        final var command = Optional.ofNullable(commandMap.get(itemId));

        if (command.isPresent()) {
            editor.executeCommand(command.get());
            return true;
        }

        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        // Nothing to do here...
    }
}
