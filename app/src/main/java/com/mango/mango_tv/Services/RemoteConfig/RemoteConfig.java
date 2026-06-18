package com.mango.mango_tv.Services.RemoteConfig;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

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
import com.mango.mango_tv.Utils.NetUtil;
import com.mango.mango_tv.Utils.PermissionUtils;
import com.mango.mango_tv.Utils.VersionComparator;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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
    private final String iptv_core_version = "iptv_core_version";
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
    public String iptvCoreVersion;


    private boolean test = true;
    private ParseQuery<ParseObject> parseQuery;
    private ParseObject parseObject;

    FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0).build();

    public RemoteConfig(Context context, Activity activity) {
        this.activity = activity;
        this.context = context;
    }


    public void getClient() {
        Parse.initialize(new Parse.Configuration.Builder(context)
                .applicationId("MDMKHd6immrSO6SWnl8toIgGfuWZY8x3txT6qoQ0")
                .clientKey("gBsw9m0QQ87eekHaK5gzjdzWxvDWSyBgTqaAvmJg")
                .server("https://parseapi.back4app.com")
                .build());
        parseQuery = ParseQuery.getQuery("datas");
        try {
            parseObject = parseQuery.get("tVSJAy3Gsm");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void fetch() {

        try {
            if(test) {
                back4appInit();
            } else {
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
                        iptvCoreVersion = getIptvCoreVersion();
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
        } catch (Exception error) {
            back4appInit();
        }
    }

    String back4appString (String key) {
        String value = parseObject.getString(key);
        if(value != null) return  value.trim();
        return  "";
    }

    Integer back4appInteger (String key) {
        Integer value = parseObject.getInt(key);
        return  value;

    }

    boolean back4appBoolean (String key) {
        boolean value = parseObject.getBoolean(key);
        return  value;

    }

    void back4appInit () {
        getClient();



        playlistUrl = getPlaylistUrl();
        tvgUrl = getTvgUrl();
        httpConnectTimeout = getHttpConnectTimeout();
        httpReadTimeout = getHttpReadTimeout();
        httpUserAgent = getHttpUserAgent();
        preferredPlayerPackage = getPreferredPlayerPackage();
        hideAllChannelsTab = getHideAllChannelsTab();
        appVersion = getAppVersion();
        iptvCoreVersion = getIptvCoreVersion();
        _package = getPackage();
        getNotification();
    }
    private void init() {
        try {
            intent.setClassName(_IPTV_CORE_PACKAGE_NAME, _IPTV_CORE_CLASS_NAME);
            /*if (_package != null ) {
                System.out.println(_package);
                intent.putExtra("package", _package);
            } else*/
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
            checkAppVersion();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    Downloader downloader = new Downloader();

    private void checkAppVersion() {

        if (appVersion != null && VersionComparator.compareVersions(BuildConfig.VERSION_NAME, appVersion)) {
            currentAppVersionStr = appVersion;
            if (!PermissionUtils.hasPermissions(context)) {
                PermissionUtils.requestPermissions(activity, 101);
                PermissionUtils.getPermissionsForInstallingFromUnknownSource(context, activity);
            }

            downloader.createMessage(intent, showIptvCoreNotFoundDialog(false), currentAppVersionStr, context, activity);
        } else {
            final String iptvLocalVersion = checkIptvCoreVersion();
            if (iptvLocalVersion.isEmpty()) {
                showIptvCoreNotFoundDialog(false);

            } else {
                if (iptvCoreVersion != null && VersionComparator.compareVersions(iptvLocalVersion, iptvCoreVersion) && !iptvLocalVersion.equals(iptvCoreVersion)) {
                    showIptvCoreNotFoundDialog(true);
                } else {
                    try {
                        activity.startActivity(intent);
                        activity.finish();
                    } catch (Exception e) {
                        showIptvCoreNotFoundDialog(false);
                    }
                }
            }
        }
    }

    private String getPlaylistUrl() {
        try {
            if(test) {
                return back4appString(playlist_url);

            }
            return mFirebaseRemoteConfig.getString(playlist_url);
        } catch (Exception e) {
                return back4appString(playlist_url).trim();


        }
    }

    private String getTvgUrl() {
        try {
            if(test) {
                String value = back4appString(tvg_url);
                if(value != null) return value;
                return "";
            }
            return mFirebaseRemoteConfig.getString(tvg_url);
        } catch (Exception e) {
                return back4appString(tvg_url).trim();


        }
    }

    private Integer getHttpConnectTimeout() {
        try {
            if(test) {
                return back4appInteger(http_connect_timeout);

            }
            return (int) mFirebaseRemoteConfig.getDouble(http_connect_timeout);
        } catch (Exception e) {
                return back4appInteger(http_connect_timeout);


        }
    }

    private Integer getHttpReadTimeout() {
        try {
            if(test) {
                return back4appInteger(http_read_timeout);

            }
            return (int) mFirebaseRemoteConfig.getDouble(http_read_timeout);
        } catch (Exception e) {
                return back4appInteger(http_read_timeout);

        }
    }

    private String getHttpUserAgent() {
        try {
            if(test) {
                return back4appString(http_user_agent).trim();

            }
            return mFirebaseRemoteConfig.getString(http_user_agent);
        } catch (Exception e) {
                return back4appString(http_user_agent).trim();


        }
    }

    private String getPreferredPlayerPackage() {
        try {
            if(test) {
                return back4appString(preferred_player_package).trim();

            }
            return mFirebaseRemoteConfig.getString(preferred_player_package);
        } catch (Exception e) {
                return back4appString(preferred_player_package).trim();

        }
    }


    private Boolean getHideAllChannelsTab() {
        try {
            if(test) {
                return back4appBoolean(hide_all_channels_tab);

            }
        return mFirebaseRemoteConfig.getBoolean(hide_all_channels_tab);
    } catch (Exception e) {
                return back4appBoolean(hide_all_channels_tab);


        }
    }

    private String getPackage() {
        try {
            if(test) {
                return back4appString("package").trim();

            }
        return mFirebaseRemoteConfig.getString("package");
    } catch (Exception e) {
                return back4appString("package").trim();

        }
    }

    private String getIptvCoreVersion() {
        try {
            if(test) {
                return back4appString(iptv_core_version).trim();
            }
        return mFirebaseRemoteConfig.getString(iptv_core_version);
    } catch (Exception e) {
                return back4appString(iptv_core_version).trim();


        }
    }

    private void getNotification() {
        final SharedPreferences myPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor myEditor = myPreferences.edit();
        boolean first = myPreferences.getBoolean("firstStart?", true);


        if(test) {
                notificationTitleInFirstOpen = back4appString(notification_title_in_first_open);
                notificationBodyInFirstOpen = back4appString(notification_body_in_first_open);

        } else {
            try {
                notificationTitleInFirstOpen = mFirebaseRemoteConfig.getString(notification_title_in_first_open);
                notificationBodyInFirstOpen = mFirebaseRemoteConfig.getString(notification_body_in_first_open);

            } catch (Exception e) {
                try {
                    notificationTitleInFirstOpen = back4appString(notification_title_in_first_open);
                    notificationBodyInFirstOpen = back4appString(notification_body_in_first_open);

                } catch (Exception ex) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (first && NetUtil.isOnline(context)) {
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

        if(test) {
                notificationTitle = back4appString(notification_title).trim();
                notificationBody = back4appString(notificationBody).trim();


        } else {
            try {
                notificationTitle = mFirebaseRemoteConfig.getString(notification_title).trim();
                notificationBody = mFirebaseRemoteConfig.getString(notification_body).trim();

            } catch (Exception e) {
                try {
                    notificationTitle = back4appString(notification_title).trim();
                    notificationBody = back4appString(notificationBody).trim();
                } catch (Exception ex) {
                    throw new RuntimeException(e);
                }
            }
        }

        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        String title = sharedPreferences.getString(notification_title, "");
        String body = sharedPreferences.getString(notification_body, "");
        System.out.println(title);
        System.out.println(body);
        if ((!notificationTitle.equals(title) || !notificationBody.equals(body) )&& !notificationTitle.isEmpty() || !notificationBody.isEmpty()) {
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
        try {
            if(test) return  back4appString(app_version);
        return mFirebaseRemoteConfig.getString(app_version);
    } catch (Exception e) {
          return   back4appString(app_version);
    }
    }


    private Callable<Void> showIptvCoreNotFoundDialog(boolean needUpdate) {
        if (!PermissionUtils.hasPermissions(context)) {
            PermissionUtils.requestPermissions(activity, 101);
            PermissionUtils.getPermissionsForInstallingFromUnknownSource(context, activity);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(needUpdate ? context.getString(R.string.a_new_update_is_available) : context.getString(R.string.dialog_core_not_installed_title));
        builder.setMessage(needUpdate ? context.getString(R.string.dialog_core_not_updated_title) : context.getString(R.string.dialog_core_not_installed_message));
        builder.setPositiveButton(context.getString(R.string.dialog_button_install),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        try {
                            downloader.downloadFile("iptv.apk", context, activity);
                        } catch (ActivityNotFoundException e) {
                            // if Google Play is not found for some reason, let's open browser
                            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + _IPTV_CORE_PACKAGE_NAME)));
                        }
                    }
                });
        builder.setNegativeButton(context.getString(R.string.dialog_button_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        // if cancelled - just close the app
                        if (needUpdate) {
                            activity.startActivity(intent);
                        }
                        activity.finish();
                    }
                });
        builder.setCancelable(false);
        builder.create().show();
        return null;
    }

    String checkIptvCoreVersion() {
        PackageInfo pinfo = null;
        try {
            pinfo = activity.getPackageManager().getPackageInfo(_IPTV_CORE_PACKAGE_NAME, 0);
            return pinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}
