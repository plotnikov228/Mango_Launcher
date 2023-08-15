package com.mango.mango_tv.Services.Browser;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class Browser {

    public void openBrowser(Uri url, Activity activity) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, url);
        activity.startActivity(browserIntent);
    }
}