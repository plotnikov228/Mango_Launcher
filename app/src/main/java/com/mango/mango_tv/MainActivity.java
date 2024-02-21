package com.mango.mango_tv;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mango.mango_tv.Services.Analytics.MyFirebaseAnalytics;
import com.mango.mango_tv.Services.Downloader.Downloader;
import com.mango.mango_tv.Services.Notifications.Remote.MyFirebaseMessagingService;
import com.mango.mango_tv.Services.RemoteConfig.RemoteConfig;
import com.mango.mango_tv.Utils.NetUtil;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            Downloader downloader = new Downloader();
            if (new File(downloader.getFilePath("iptv.apk", "")).exists()) {
                this.startActivity(config.intent);
            }
        }
    }

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
            System.out.println(_);
        }


    }


    private void _start(boolean value, Context context, Activity activity) {
        if (value) {
            start(context);
        } else {
            showDeviceErrorDialog(activity);
        }
    }

    private void deviceModelCheck(Context context, Activity activity) {
        String model = Build.MODEL;

        FirebaseDatabase database = FirebaseDatabase.getInstance();

            DatabaseReference myRef = database.getReference(model);
            System.out.println(NetUtil.isOnline(context));
            if(NetUtil.isOnline(context)) {
                myRef.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        _start(dataSnapshot.exists(), context, activity);
                        System.out.println("onSuccess");

                    }
                }).addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        System.out.println("onCanceled");

                        _start(model.contains("SMUX BOX"), context, activity);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("onFailure " + e.toString());

                        _start(model.contains("SMUX BOX"), context, activity);

                    }
                });
            }
            else _start(model.contains("SMUX BOX"), context, activity);

        /*FirebaseFirestore db = FirebaseFirestore.getInstance();
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
                if(model.contains("SMUX BOX")) {
                    start(context);
                } else {
                    showDeviceErrorDialog(activity);
                }
            }
        });*/
    }

    RemoteConfig config = new RemoteConfig(this, this);

    private void start(Context appContext) {
        MyFirebaseAnalytics myFirebaseAnalytics = new MyFirebaseAnalytics(appContext);
        myFirebaseAnalytics.firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, new Bundle());
        Intent intent = new Intent(appContext, MyFirebaseMessagingService.class);
        startService(intent);
        config.fetch();
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