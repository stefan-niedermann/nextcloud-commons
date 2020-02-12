package it.niedermann.nextcloud.exception;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.Serializable;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = ExceptionHandler.class.getCanonicalName();
    private Context context;
    public static final String KEY_THROWABLE = "T";

    public ExceptionHandler(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Log.e(TAG, e.getMessage(), e);
        if (context instanceof Activity) {
            Intent intent = new Intent(context.getApplicationContext(), context.getClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Bundle extras = new Bundle();
            intent.putExtra(KEY_THROWABLE, e);
            extras.putSerializable(KEY_THROWABLE, e);
            intent.putExtras(extras);
            context.getApplicationContext().startActivity(intent);
            ((Activity) context).finish();
        }
        Runtime.getRuntime().exit(0);
    }
}
