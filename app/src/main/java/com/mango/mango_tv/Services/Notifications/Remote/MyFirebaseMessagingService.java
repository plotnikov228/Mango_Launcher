package com.mango.mango_tv.Services.Notifications.Remote;

import android.content.DialogInterface;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mango.mango_tv.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingServ";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Task<String> refreshedToken = FirebaseMessaging.getInstance().getToken();

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(s);
        Log.e("NEW_TOKEN",s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        sendNotification(remoteMessage);
    }

    private void sendNotification(RemoteMessage remoteMessage){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(remoteMessage.getNotification().getTitle());
        builder.setMessage(remoteMessage.getNotification().getBody());
        builder.setPositiveButton(getString(R.string.apply),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        dialogInterface.dismiss();
                    }
                });
        builder.create().show();
        //localNotification.sendNotification(new Config(remoteMessage.getNotification().getBody(), remoteMessage.getNotification().getTitle()));
    }
}
