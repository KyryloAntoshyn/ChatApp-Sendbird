package com.example.akiri.chatapp;

import android.app.Application;

import com.example.akiri.chatapp.utils.PreferenceUtils;
import com.sendbird.android.SendBird;

/**
 * Created by akiri on 23.04.2018.
 */

public class App extends Application {
    private static final String APP_ID = "ABD38050-779D-4ED1-87F2-64A02516D497";

    @Override
    public void onCreate(){
        super.onCreate();

        PreferenceUtils.init(getApplicationContext());

        SendBird.init(APP_ID, getApplicationContext());
    }
}
