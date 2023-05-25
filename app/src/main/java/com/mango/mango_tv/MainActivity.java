package com.mango.mango_tv;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mango.mango_tv.Services.Analytics.MyFirebaseAnalytics;
import com.mango.mango_tv.Services.Notifications.Local.LocalNotification;
import com.mango.mango_tv.Services.Notifications.Remote.MyFirebaseMessagingService;
import com.mango.mango_tv.Services.RemoteConfig.RemoteConfig;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main);
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("all");
            deviceModelCheck(this, this);
            // If "package" extra is set, IPTV Core will be able to show your app name as a title

        } catch (Exception _) {
            Toast.makeText(this, _.toString(),
                    Toast.LENGTH_LONG).show();
        }


    }


    private void deviceModelCheck(Context context, Activity activity) {
        String model = Build.MODEL;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Models").document(model).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        start(context);
                    } else {
                        showDeviceErrorDialog(activity);
                    }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity, e.toString(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    private void start(Context appContext) {
        MyFirebaseAnalytics myFirebaseAnalytics = new MyFirebaseAnalytics(appContext);
        myFirebaseAnalytics.firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, new Bundle());

        Intent intent = new Intent(appContext, MyFirebaseMessagingService.class);
        startService(intent);
        final SharedPreferences myPreferences = PreferenceManager
                .getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor myEditor = myPreferences.edit();

        NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        LocalNotification localNotification = new LocalNotification(notificationManager, appContext);

        boolean first = myPreferences.getBoolean("firstStart?", true);

        RemoteConfig remoteConfig = new RemoteConfig(this,this);
        if (first == true) {
            AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
            builder.setTitle(getString(R.string.hi));
            builder.setMessage(getString(R.string.first_start));
            builder.setPositiveButton(getString(R.string.apply),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int id) {
                            dialogInterface.dismiss();
                            remoteConfig.fetch();
                        }
                    });
            builder.create().show();
            //localNotification.createNotificationChannel("1");
            //localNotification.sendNotification(new Config("It's first app start!", "Hi"));
            myEditor.putBoolean("firstStart?", false);
            myEditor.apply();
        } else {
            remoteConfig.fetch();
        }
    }

    private void showDeviceErrorDialog(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.device_verification_error));
        builder.setPositiveButton(getString(R.string.dialog_button_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        activity.finish();
                    }
                });
        builder.setCancelable(false);
        builder.create().show();
    }

    private static final String _IPTV_CORE_PACKAGE_NAME = "ru.iptvremote.android.iptv.core";
    private static final String _IPTV_CORE_CLASS_NAME = _IPTV_CORE_PACKAGE_NAME + ".ChannelsActivity";
}