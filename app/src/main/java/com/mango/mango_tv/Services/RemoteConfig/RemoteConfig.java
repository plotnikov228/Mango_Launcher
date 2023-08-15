package com.mango.mango_tv.Services.RemoteConfig;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mango.mango_tv.BuildConfig;
import com.mango.mango_tv.R;
import com.mango.mango_tv.Services.Browser.Browser;
import com.mango.mango_tv.Services.Downloader.Downloader;
import com.mango.mango_tv.Utils.PermissionUtils;

import java.util.concurrent.Callable;

public class RemoteConfig {

    private Context context;
    private Activity activity;

    public Intent intent = new Intent();

    private static final String _IPTV_CORE_PACKAGE_NAME = "ru.iptvremote.android.iptv.core";
    private static final String _IPTV_CORE_CLASS_NAME = _IPTV_CORE_PACKAGE_NAME + ".ChannelsActivity";


    private final String playlist_url = "playlist_url";
    private final String tvg_url = "tvg_url";
    private final String http_connect_timeout = "http_connect_timeout";
    private final String http_read_timeout = "http_read_timeout";
    private final String http_user_agent = "http_user_agent";
    private final String preferred_player_package = "preferred_player_package";
    private final String hide_all_channels_tab = "hide_all_channels_tab";
    private final String app_version = "app_version";
    private final String notification_title = "notification_title";
    private final String notification_body = "notification_body";
    private final String notification_title_in_first_open = "notification_title_in_first_open";
    private final String notification_body_in_first_open = "notification_body_in_first_open";

    private String playlistUrl;
    private String tvgUrl;
    private Integer httpConnectTimeout;
    private Integer httpReadTimeout;
    private String httpUserAgent;
    private String preferredPlayerPackage;
    private Boolean hideAllChannelsTab;
    private String appVersion;
    private String _package;
    public String currentAppVersionStr;
    public String notificationTitle;
    public String notificationBody;
    public String notificationTitleInFirstOpen;
    public String notificationBodyInFirstOpen;


    FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0).build();

    public RemoteConfig(Context context, Activity activity) {
        this.activity = activity;
        this.context = context;
    }

    public void fetch() {
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings).addOnCompleteListener(task -> mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                playlistUrl = getPlaylistUrl();
                tvgUrl = getTvgUrl();
                httpConnectTimeout = getHttpConnectTimeout();
                httpReadTimeout = getHttpReadTimeout();
                httpUserAgent = getHttpUserAgent();
                preferredPlayerPackage = getPreferredPlayerPackage();
                hideAllChannelsTab = getHideAllChannelsTab();
                appVersion = getAppVersion();
                _package = getPackage();
                getNotification();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println(e);
            }
        }));
    }

    private void init() {
        try {
            intent.setClassName(_IPTV_CORE_PACKAGE_NAME, _IPTV_CORE_CLASS_NAME);
        } catch (ActivityNotFoundException e) {
            // IPTV core app is not installed, let's ask the user to install it.
            showIptvCoreNotFoundDialog();
        }
        try {
            intent.putExtra("package", _package);
            if (playlistUrl != null) {
                intent.setData(Uri.parse(playlistUrl));
            }
            if (tvgUrl != null) {
                intent.putExtra("url-tvg", tvgUrl);
            }
            if (httpConnectTimeout != null) {
                intent.putExtra(http_connect_timeout, httpConnectTimeout * 1000);
            }
            if (httpReadTimeout != null) {
                intent.putExtra(http_read_timeout, httpReadTimeout * 1000);
            }
            if (httpUserAgent != null) {
                intent.putExtra(http_user_agent, httpUserAgent);
            }
            if (preferredPlayerPackage != null) {
                intent.putExtra(preferred_player_package, preferredPlayerPackage);
            }
            if (hideAllChannelsTab != null) {
                intent.putExtra(hide_all_channels_tab, hideAllChannelsTab);
            }

            checkAppVersion();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    Downloader downloader = new Downloader(context, activity);
    private void checkAppVersion() {

        if (!BuildConfig.VERSION_NAME.equals(appVersion) && appVersion != null) {
            currentAppVersionStr = appVersion;
            if (!PermissionUtils.hasPermissions(context)) {
                PermissionUtils.requestPermissions(activity, 101);
                PermissionUtils.getPermissionsForInstallingFromUnknownSource(context, activity);
            }



            downloader.createMessage(intent, showIptvCoreNotFoundDialog(), currentAppVersionStr);
        } else {
            try {
                activity.startActivity(intent);
                activity.finish();
            }
            catch (ActivityNotFoundException e) {
                // IPTV core app is not installed, let's ask the user to install it.
                showIptvCoreNotFoundDialog();
            }

        }
    }

    private String getPlaylistUrl() {
        return mFirebaseRemoteConfig.getString(playlist_url);
    }

    private String getTvgUrl() {
        return mFirebaseRemoteConfig.getString(tvg_url);
    }

    private Integer getHttpConnectTimeout() {
        return (int) mFirebaseRemoteConfig.getDouble(http_connect_timeout);
    }

    private Integer getHttpReadTimeout() {
        return (int) mFirebaseRemoteConfig.getDouble(http_read_timeout);
    }

    private String getHttpUserAgent() {
        return mFirebaseRemoteConfig.getString(http_user_agent);
    }

    private String getPreferredPlayerPackage() {
        return mFirebaseRemoteConfig.getString(preferred_player_package);
    }


    private Boolean getHideAllChannelsTab() {
        return mFirebaseRemoteConfig.getBoolean(hide_all_channels_tab);
    }

    private String getPackage() {
        return mFirebaseRemoteConfig.getString("package");
    }

    private void getNotification () {
        final SharedPreferences myPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor myEditor = myPreferences.edit();
        boolean first = myPreferences.getBoolean("firstStart?", true);


        notificationTitleInFirstOpen = mFirebaseRemoteConfig.getString(notification_title_in_first_open);
        notificationBodyInFirstOpen = mFirebaseRemoteConfig.getString(notification_body_in_first_open);

        if (first == true) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(notificationTitleInFirstOpen);
            builder.setMessage(notificationBodyInFirstOpen);
            builder.setPositiveButton(context.getString(R.string.apply),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int id) {
                            dialogInterface.dismiss();
                            myEditor.putBoolean("firstStart?", false);
                            myEditor.apply();
                            getAnotherNotifications();
                        }
                    });
            builder.setCancelable(false);
            builder.create().show();

        } else {
            getAnotherNotifications();
        }
    }

    private void getAnotherNotifications() {
        notificationTitle = mFirebaseRemoteConfig.getString(notification_title);
        notificationBody = mFirebaseRemoteConfig.getString(notification_body);
        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        String title = sharedPreferences.getString(notification_title, "");
        String body = sharedPreferences.getString(notification_body, "");
        System.out.println(title);
        System.out.println(body);
        if (!notificationTitle.equals(title) || !notificationBody.equals(body)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(notificationTitle);
            builder.setMessage(notificationBody);
            builder.setPositiveButton(context.getString(R.string.apply),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int id) {

                            dialogInterface.dismiss();

                            SharedPreferences.Editor myEditor = sharedPreferences.edit();
                            myEditor.putString(notification_title, notificationTitle);
                            myEditor.putString(notification_body, notificationBody);
                            myEditor.apply();
                            init();

                        }
                    });


            builder.setCancelable(false);
            builder.create().show();
        } else {
            init();
        }

    }


    private String getAppVersion() {
        return mFirebaseRemoteConfig.getString(app_version);
    }

    private Callable<Void> showIptvCoreNotFoundDialog () {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_core_not_installed_title);
        builder.setMessage(R.string.dialog_core_not_installed_message);
        builder.setPositiveButton(R.string.dialog_button_install,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        try {
                            StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl("gs://mangotv-app-1ff36.appspot.com/APKs/iptv-core.apk");
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    new Browser().openBrowser(uri ,activity);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(activity, context.getString(R.string.internet_connection_error),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                            // downloader.downloadFile("iptv.apk");
                        } catch (ActivityNotFoundException e) {
                            // if Google Play is not found for some reason, let's open browser
                            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + _IPTV_CORE_PACKAGE_NAME)));
                        }
                    }
                });
        builder.setNegativeButton(R.string.dialog_button_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        // if cancelled - just close the app
                        activity.finish();
                    }
                });
        builder.setCancelable(false);
        builder.create().show();
        return null;
    }
}
