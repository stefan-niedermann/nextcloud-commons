package it.niedermann.nextcloud.exception;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static it.niedermann.nextcloud.exception.ExceptionHandler.KEY_THROWABLE;

public class ExceptionActivity extends AppCompatActivity {

    private static final String TAG = ExceptionActivity.class.getCanonicalName();

    Throwable throwable;

    @BindView(R2.id.toolbar)
    Toolbar toolbar;
    @BindView(R2.id.message)
    TextView message;
    @BindView(R2.id.stacktrace)
    TextView stacktrace;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_exception);
        ButterKnife.bind(this);

        super.onCreate(savedInstanceState);
        setSupportActionBar(toolbar);

        throwable = ((Throwable) getIntent().getSerializableExtra(KEY_THROWABLE));
        toolbar.setTitle(R.string.simple_error);
        message.setText(throwable.getMessage());
        stacktrace.setText(ExceptionUtil.getDebugInfos(this, throwable));
    }

    @OnClick(R2.id.copy)
    void copyStacktraceToClipboard() {
        final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(getString(R.string.simple_exception), "```\n" + this.stacktrace.getText() + "\n```");
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "Could not copy text because clipboardManager is null.");
        }
    }

    @OnClick(R2.id.close)
    void close() {
        finish();
    }
}
