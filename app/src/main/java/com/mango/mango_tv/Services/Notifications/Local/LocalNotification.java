package com.mango.mango_tv.Services.Notifications.Local;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.mango.mango_tv.Config.Config;
import com.mango.mango_tv.MainActivity;
import com.mango.mango_tv.R;

public class LocalNotification {
    private NotificationManager notificationManager;
    private Context context;

    public LocalNotification(NotificationManager notificationManager, Context context) {
        this.notificationManager = notificationManager;
        this.context = context;
    }
    private String channelId = "1";
    public void createNotificationChannel(String NOTIFICATION_CHANNEL_ID) {
        channelId = NOTIFICATION_CHANNEL_ID;
        final String CHANNEL_NAME = "Notification Channel";
        final int NOTIFICATION_ID = 101;
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, CHANNEL_NAME, importance);

            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(notificationChannel);
        }

    }

    public void sendNotification (Config config) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(config.title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(config.content)
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_MAX);

        builder.setContentIntent(contentIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setGroup("channel_id");
        }
        Notification notification = builder.build();
        notificationManager.notify(0, notification);
    }

    public void showToast(Config config) {
        Toast toast3 = Toast.makeText(context,
                config.title + "\n" + config.content, Toast.LENGTH_LONG);

        toast3.setGravity(Gravity.BOTTOM, 0, 0);
        LinearLayout toastContainer = (LinearLayout) toast3.getView();
        toast3.show();
    }
}

