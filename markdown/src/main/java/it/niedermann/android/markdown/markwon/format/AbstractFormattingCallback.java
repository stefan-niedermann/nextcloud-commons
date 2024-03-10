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

import it.niedermann.android.markdown.controller.Command;
import it.niedermann.android.markdown.controller.CommandReceiver;
import it.niedermann.android.markdown.controller.CommandState;
import it.niedermann.android.markdown.controller.EditorState;
import it.niedermann.android.markdown.controller.MarkdownController;

public abstract class AbstractFormattingCallback implements ActionMode.Callback, MarkdownController {

    @NonNull
    protected final Map<Integer, Command> commandMap;
    @Nullable
    protected CommandReceiver commandReceiver;
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
    public void setCommandReceiver(@Nullable CommandReceiver commandReceiver) {
        this.commandReceiver = commandReceiver;
        this.state = null;
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

    @NonNull
    protected Optional<CommandState> getCommandState(@NonNull Command command) {
        if (state == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(state.commands().get(command));
    }

    @CallSuper
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (commandReceiver == null) {
            return false;
        }

        final int itemId = item.getItemId();
        final var command = Optional.ofNullable(commandMap.get(itemId));

        if (command.isPresent()) {
            commandReceiver.executeCommand(command.get());
            return true;
        }

        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        // Nothing to do here…
    }
}
