package com.andy;

import android.app.Application;
import android.os.SystemClock;

public class myApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SystemClock.sleep(3000);
    }
}
