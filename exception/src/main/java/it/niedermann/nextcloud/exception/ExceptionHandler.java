package it.niedermann.nextcloud.exception;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    @NonNull
    private Activity activity;
    @NonNull
    private final String applicationId;
    @NonNull
    private final String flavor;
    @Nullable
    private final String bugReportUrl;

    public ExceptionHandler(@NonNull Activity activity, @NonNull String applicationId, @NonNull String flavor, @Nullable String bugReportUrl) {
        super();
        this.activity = activity;
        this.applicationId = applicationId;
        this.flavor = flavor;
        this.bugReportUrl = bugReportUrl;
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        activity.getApplicationContext().startActivity(ExceptionActivity.createIntent(activity, e, applicationId, flavor, bugReportUrl));
        activity.finish();
        Runtime.getRuntime().exit(0);
    }
}
