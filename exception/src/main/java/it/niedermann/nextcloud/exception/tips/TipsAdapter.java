package it.niedermann.nextcloud.exception.tips;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import com.nextcloud.android.sso.exceptions.NextcloudApiNotRespondingException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotSupportedException;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;
import com.nextcloud.android.sso.exceptions.TokenMismatchException;

import org.json.JSONException;

import java.io.Serializable;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;

import it.niedermann.nextcloud.exception.R;

import static android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
import static it.niedermann.nextcloud.exception.ExceptionDialogFragment.INTENT_EXTRA_BUTTON_TEXT;

public class TipsAdapter extends RecyclerView.Adapter<TipsViewHolder> implements Serializable {

    @Nullable
    private final Intent intentAppInfo;
    @Nullable
    private final String bugReportUrl;
    @NonNull
    private Consumer<Intent> actionButtonClickedListener;
    @NonNull
    private List<TipsModel> tips = new LinkedList<>();

    public TipsAdapter(@Nullable String applicationId, @Nullable String bugReportUrl, @NonNull Consumer<Intent> actionButtonClickedListener) {
        this.intentAppInfo = TextUtils.isEmpty(applicationId) ? null : new Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:" + applicationId))
                .putExtra(INTENT_EXTRA_BUTTON_TEXT, R.string.error_action_open_deck_info);
        this.bugReportUrl = bugReportUrl;
        this.actionButtonClickedListener = actionButtonClickedListener;
    }

    @NonNull
    @Override
    public TipsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TipsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tip, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TipsViewHolder holder, int position) {
        holder.bind(tips.get(position), actionButtonClickedListener);
    }

    @Override
    public int getItemCount() {
        return tips.size();
    }

    @CallSuper
    public void setThrowable(@NonNull Context context, @Nullable String accountUrl, @NonNull Throwable throwable) {
        if (throwable instanceof TokenMismatchException) {
            add(R.string.error_dialog_tip_token_mismatch_retry);
            add(R.string.error_dialog_tip_clear_storage_might_help);
            if (intentAppInfo == null) {
                add(R.string.error_dialog_tip_clear_storage);
            } else {
                add(R.string.error_dialog_tip_clear_storage, intentAppInfo);
            }
        } else if (throwable instanceof NextcloudFilesAppNotSupportedException) {
            add(R.string.error_dialog_tip_files_outdated);
        } else if (throwable instanceof NextcloudApiNotRespondingException) {
            if (VERSION.SDK_INT >= VERSION_CODES.M) {
                add(R.string.error_dialog_tip_disable_battery_optimizations, new Intent().setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).putExtra(INTENT_EXTRA_BUTTON_TEXT, R.string.error_action_open_battery_settings));
            } else {
                add(R.string.error_dialog_tip_disable_battery_optimizations);
            }
            add(R.string.error_dialog_tip_files_force_stop);
            add(R.string.error_dialog_tip_files_delete_storage);
        } else if (throwable instanceof SocketTimeoutException || throwable instanceof ConnectException) {
            add(R.string.error_dialog_timeout_instance);
            add(R.string.error_dialog_timeout_toggle, new Intent(Settings.ACTION_WIFI_SETTINGS).putExtra(INTENT_EXTRA_BUTTON_TEXT, R.string.error_action_open_network));
        } else if (throwable instanceof JSONException || throwable instanceof NullPointerException) {
            add(R.string.error_dialog_check_server);
        } else if (throwable instanceof NextcloudHttpRequestFailedException) {
            int statusCode = ((NextcloudHttpRequestFailedException) throwable).getStatusCode();
            switch (statusCode) {
                case 302:
                    add(R.string.error_dialog_redirect);
                    break;
                case 500:
                    if (accountUrl != null) {
                        add(R.string.error_dialog_check_server_logs, new Intent(Intent.ACTION_VIEW)
                                .putExtra(INTENT_EXTRA_BUTTON_TEXT, R.string.error_action_server_logs)
                                .setData(Uri.parse(accountUrl + context.getString(R.string.url_fragment_server_logs))));
                    } else {
                        add(R.string.error_dialog_check_server_logs);
                    }
                    break;
                case 503:
                    add(R.string.error_dialog_check_maintenance);
                    break;
                case 507:
                    add(R.string.error_dialog_insufficient_storage);
                    break;
            }
        } else if (throwable instanceof RuntimeException) {
            if (throwable.getMessage() != null && throwable.getMessage().contains("database")) {
                if(bugReportUrl == null) {
                    add(R.string.error_dialog_tip_database_upgrade_failed);
                } else {
                    Intent reportIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(bugReportUrl))
                            .putExtra(INTENT_EXTRA_BUTTON_TEXT, R.string.error_action_report_issue);
                    add(R.string.error_dialog_tip_database_upgrade_failed, reportIntent);
                }
                if (intentAppInfo == null) {
                    add(R.string.error_dialog_tip_clear_storage);
                } else {
                    add(R.string.error_dialog_tip_clear_storage, intentAppInfo);
                }
            }
        }
    }

    public void add(@StringRes int text) {
        add(text, null);
    }

    public void add(@StringRes int text, @Nullable Intent primaryAction) {
        tips.add(new TipsModel(text, primaryAction));
        notifyItemInserted(tips.size());
    }
}