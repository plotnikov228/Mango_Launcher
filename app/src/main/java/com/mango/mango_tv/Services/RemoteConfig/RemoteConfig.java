package com.mango.mango_tv.Services.RemoteConfig;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.mango.mango_tv.BuildConfig;
import com.mango.mango_tv.R;
import com.mango.mango_tv.Services.Downloader.Downloader;
import com.mango.mango_tv.Utils.PermissionUtils;

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
            /*if (_package != null ) {
                System.out.println(_package);
                intent.putExtra("package", _package);
            }*/
            intent.putExtra("package", activity.getPackageName());
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

            checkAppVersion(activity);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void checkAppVersion(Activity activity) {
        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        String app_version = "appVersion";
        if (!BuildConfig.VERSION_NAME.equals(appVersion) && appVersion != null) {
            currentAppVersionStr = appVersion;
            if (!PermissionUtils.hasPermissions(context)) {
                PermissionUtils.requestPermissions(activity, 101);
                PermissionUtils.getPermissionsForInstallingFromUnknownSource(context, activity);
            }


            Downloader downloader = new Downloader(currentAppVersionStr, context, activity);
            downloader.createMessage(intent);
        } else {
            activity.startActivity(intent);
            activity.finish();
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

    private void getNotification() {
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

    private void showIptvCoreNotFoundDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_core_not_installed_title);
        builder.setMessage(R.string.dialog_core_not_installed_message);
        builder.setPositiveButton(R.string.dialog_button_install,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        try {
                            // try to open Google Play app first
                            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + _IPTV_CORE_PACKAGE_NAME)));
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
    }
}
