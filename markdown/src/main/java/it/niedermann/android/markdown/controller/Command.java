package it.niedermann.android.markdown.controller;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Optional;

import it.niedermann.android.markdown.controller.applier.CommandApplier;
import it.niedermann.android.markdown.controller.applier.LinkCa;
import it.niedermann.android.markdown.controller.applier.ToggleBlockQuoteCa;
import it.niedermann.android.markdown.controller.applier.ToggleCheckboxCa;
import it.niedermann.android.markdown.controller.applier.ToggleInlinePunctuationCa;
import it.niedermann.android.markdown.controller.applier.ToggleOrderedListCa;
import it.niedermann.android.markdown.controller.applier.ToggleUnorderedListCa;
import it.niedermann.android.markdown.controller.stateresolver.CommandStateResolver;
import it.niedermann.android.markdown.controller.stateresolver.LinkCsr;
import it.niedermann.android.markdown.controller.stateresolver.ToggleBlockquoteCsr;
import it.niedermann.android.markdown.controller.stateresolver.ToggleCheckboxCsr;
import it.niedermann.android.markdown.controller.stateresolver.ToggleInlinePunctuationCsr;
import it.niedermann.android.markdown.controller.stateresolver.ToggleOrderedListCsr;
import it.niedermann.android.markdown.controller.stateresolver.ToggleUnorderedListCsr;

public enum Command {
    TOGGLE_BOLD(new ToggleInlinePunctuationCsr("**"), new ToggleInlinePunctuationCa("**")),
    TOGGLE_ITALIC(new ToggleInlinePunctuationCsr("*"), new ToggleInlinePunctuationCa("*")),
    TOGGLE_STRIKE_THROUGH(new ToggleInlinePunctuationCsr("~~"), new ToggleInlinePunctuationCa("~~")),
    INSERT_LINK(new LinkCsr(), new LinkCa()),
    TOGGLE_CHECKBOX_LIST(new ToggleCheckboxCsr(), new ToggleCheckboxCa()),
    TOGGLE_UNORDERED_LIST(new ToggleUnorderedListCsr(), new ToggleUnorderedListCa()),
    TOGGLE_ORDERED_LIST(new ToggleOrderedListCsr(), new ToggleOrderedListCa()),
    TOGGLE_BLOCK_QUOTE(new ToggleBlockquoteCsr(), new ToggleBlockQuoteCa()),
    TOGGLE_CODE_BLOCK(new ToggleBlockquoteCsr(), new ToggleBlockQuoteCa()),
    ;

    @Nullable
    private final CommandStateResolver csr;

    @Nullable
    private final CommandApplier ca;

    Command(@Nullable CommandStateResolver csr,
            @Nullable CommandApplier ca) {
        this.csr = csr;
        this.ca = ca;
    }

    public boolean isEnabled(@NonNull Context context,
                             @NonNull Spannable content,
                             int selectionStart,
                             int selectionEnd) {
        return ca != null && csr != null && csr.isEnabled(context, content, selectionStart, selectionEnd);
    }

    public boolean isActive(@NonNull Context context,
                            @NonNull Spannable content,
                            int selectionStart,
                            int selectionEnd) {
        return csr != null && csr.isActive(context, content, selectionStart, selectionEnd);
    }

    @NonNull
    public Optional<CommandApplier.CommandApplierResult> applyCommand(@NonNull Context context,
                                                                      @NonNull Editable content,
                                                                      int selectionStart,
                                                                      int selectionEnd) {
        return Optional.ofNullable(ca)
                .map(ca -> ca.applyCommand(context, content, selectionStart, selectionEnd));
    }
}
