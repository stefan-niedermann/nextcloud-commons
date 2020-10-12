package it.niedermann.nextcloud.exception;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;

import it.niedermann.nextcloud.exception.databinding.DialogExceptionBinding;
import it.niedermann.nextcloud.exception.tips.TipsAdapter;

import static it.niedermann.android.util.ClipboardUtil.copyToClipboard;

public class ExceptionDialogFragment extends AppCompatDialogFragment {

    private static final String TAG = ExceptionDialogFragment.class.getSimpleName();

    private static final String KEY_THROWABLE = "throwable";
    private static final String KEY_APPLICATION_ID = "applicationId";
    private static final String KEY_FLAVOR = "flavor";
    private static final String KEY_BUG_REPORT_URL = "bugReportUrl";
    private static final String KEY_ACCOUNT_URL = "accountUrl";
    private static final String KEY_SERVER_APP_VERSION = "additionalInformation";
    public static final String INTENT_EXTRA_BUTTON_TEXT = "button_text";

    private Throwable throwable;
    @Nullable
    private String applicationId;
    @Nullable
    private String accountUrl;
    @Nullable
    private String bugReportUrl;
    @Nullable
    private String serverAppVersion;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        final Bundle args = getArguments();
        if (args != null) {
            this.throwable = (Throwable) args.getSerializable(KEY_THROWABLE);
            if (this.throwable == null) {
                throwable = new IllegalArgumentException("Did not receive any exception in " + ExceptionDialogFragment.class.getSimpleName());
            }
            this.applicationId = args.getString(KEY_APPLICATION_ID);
            this.accountUrl = args.getString(KEY_ACCOUNT_URL);
            this.bugReportUrl = args.getString(KEY_BUG_REPORT_URL);
            this.serverAppVersion = args.getString(KEY_SERVER_APP_VERSION);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View view = View.inflate(getContext(), R.layout.dialog_exception, null);
        final DialogExceptionBinding binding = DialogExceptionBinding.bind(view);

        final TipsAdapter adapter = new TipsAdapter(applicationId, bugReportUrl, (actionIntent) -> requireActivity().startActivity(actionIntent));

        final String debugInfos = ExceptionUtil.getDebugInfos(requireContext(), throwable, this.serverAppVersion);

        binding.tips.setAdapter(adapter);
        binding.stacktrace.setText(debugInfos);

        Log.e(TAG, throwable.getMessage(), throwable);

        adapter.setThrowable(requireContext(), accountUrl, throwable);

        return new AlertDialog.Builder(requireActivity())
                .setView(binding.getRoot())
                .setTitle(R.string.error_dialog_title)
                .setPositiveButton(android.R.string.copy, (a, b) -> {
                    copyToClipboard(requireContext(), getString(R.string.simple_exception), "```\n" + debugInfos + "\n```");
                    a.dismiss();
                })
                .setNegativeButton(R.string.simple_close, null)
                .create();
    }

    public static DialogFragment newInstance(@Nullable Throwable throwable, @Nullable String applicationId, @Nullable String flavor, @Nullable String accountUrl, @Nullable String bugReportUrl, @Nullable String serverAppVersion) {
        final Bundle args = new Bundle();
        args.putSerializable(KEY_THROWABLE, throwable);
        args.putString(KEY_APPLICATION_ID, applicationId);
        args.putString(KEY_FLAVOR, flavor);
        args.putString(KEY_ACCOUNT_URL, accountUrl);
        args.putString(KEY_BUG_REPORT_URL, bugReportUrl);
        args.putSerializable(KEY_SERVER_APP_VERSION, serverAppVersion);
        final DialogFragment fragment = new ExceptionDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
