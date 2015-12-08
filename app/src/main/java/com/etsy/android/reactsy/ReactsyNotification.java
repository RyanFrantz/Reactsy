package com.etsy.android.reactsy;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

/**
 * Used by ReactsyConfigureActivity and (TODO) the service that listens for requests from the server
 * Created by mhorowitz on 8/22/14.
 */
public class ReactsyNotification {
    public static final int NOTIFICATION_ID = 0;

    public static Bundle buildIntentArguments(ReactsyTestParameters parms) {
        Bundle parameterBundle = new Bundle();
        parameterBundle.putInt(Reactsy.PARAM_TEST_DURATION, parms.testDurationSec);
        parameterBundle.putInt(Reactsy.PARAM_MIN_DELAY, parms.minDelayMs);
        parameterBundle.putInt(Reactsy.PARAM_MAX_DELAY, parms.maxDelayMs);
        parameterBundle.putInt(Reactsy.PARAM_TOO_SOON, parms.tooSoonMs);
        parameterBundle.putInt(Reactsy.PARAM_TOO_LATE, parms.tooLateMs);
        parameterBundle.putString(Reactsy.PARAM_TEST_MECHANISM, parms.testMechanism);
        parameterBundle.putInt(Reactsy.PARAM_RECOVERY_DELAY, parms.recoveryDelayMs);
        parameterBundle.putDouble(Reactsy.PARAM_MIN_VALID_TRIALS, parms.minValidTrialsPct);
        parameterBundle.putString(Reactsy.PARAM_LDAP, parms.ldap);

        return parameterBundle;
    }

    public static Intent buildIntentWithArguments(Context context, ReactsyTestParameters parms) {
        Intent reactsyIntent = new Intent(context, Reactsy.class);
        reactsyIntent.putExtras(buildIntentArguments(parms));
        return reactsyIntent;
    }

    public static void addReactsyNotification(Context context, ReactsyTestParameters parms) {
        NotificationCompat.Builder builder =
            new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(context.getString(R.string.time_for_test_title))
                .setContentText(context.getString(R.string.time_for_test));

        // The stack builder object will contain an artificial back stack for the started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(Reactsy.class);

        // Creates and adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(buildIntentWithArguments(context, parms));
        PendingIntent resultPendingIntent =
            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // NOTIFICATION_ID allows one to update the notification later on.
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static void cancelNotification(Context context) {
        NotificationManager notificationManager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
