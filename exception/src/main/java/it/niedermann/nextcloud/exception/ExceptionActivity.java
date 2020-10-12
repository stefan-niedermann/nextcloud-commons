package it.niedermann.nextcloud.exception;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import it.niedermann.nextcloud.exception.databinding.ActivityExceptionBinding;
import it.niedermann.nextcloud.exception.tips.TipsAdapter;

import static it.niedermann.android.util.ClipboardUtil.copyToClipboard;

public class ExceptionActivity extends AppCompatActivity {

    private static final String TAG = ExceptionActivity.class.getSimpleName();

    private static final String KEY_THROWABLE = "throwable";
    private static final String KEY_APPLICATION_ID = "applicationId";
    private static final String KEY_BUG_REPORT_URL = "bugReportUrl";
    private static final String KEY_FLAVOR = "flavor";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        final ActivityExceptionBinding binding = ActivityExceptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        super.onCreate(savedInstanceState);

        Throwable throwable = ((Throwable) getIntent().getSerializableExtra(KEY_THROWABLE));

        if (throwable == null) {
            throwable = new Exception("Could not get exception");
        }

        Log.e(TAG, throwable.getMessage(), throwable);

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitle(R.string.simple_exception);
        binding.message.setText(throwable.getMessage());

        final String debugInfo = "Full Crash:\n\n" + ExceptionUtil.getDebugInfos(this, throwable);

        binding.stacktrace.setText(debugInfo);

        final TipsAdapter adapter = new TipsAdapter(getIntent().getStringExtra(KEY_APPLICATION_ID), getIntent().getStringExtra(KEY_BUG_REPORT_URL), this::startActivity);
        binding.tips.setAdapter(adapter);
        binding.tips.setNestedScrollingEnabled(false);
        adapter.setThrowable(this, null, throwable);

        binding.copy.setOnClickListener((v) -> copyToClipboard(this, getString(R.string.simple_exception), "```\n" + debugInfo + "\n```"));
        binding.close.setOnClickListener((v) -> finish());
    }

    @NonNull
    public static Intent createIntent(@NonNull Context context, @NonNull Throwable throwable, @NonNull String applicationId, @NonNull String flavor, @Nullable String bugReportUrl) {
        return new Intent(context, ExceptionActivity.class)
                .putExtra(KEY_THROWABLE, throwable)
                .putExtra(KEY_APPLICATION_ID, applicationId)
                .putExtra(KEY_FLAVOR, flavor)
                .putExtra(KEY_BUG_REPORT_URL, bugReportUrl)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }
}
