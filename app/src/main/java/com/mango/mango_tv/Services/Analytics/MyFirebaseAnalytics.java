package com.mango.mango_tv.Services.Analytics;

import android.content.Context;

import com.google.firebase.analytics.FirebaseAnalytics;


public class MyFirebaseAnalytics {
    public FirebaseAnalytics firebaseAnalytics;

    public MyFirebaseAnalytics(Context context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }
}
