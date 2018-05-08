package com.example.akiri.chatapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by akiri on 23.04.2018.
 */

public class PreferenceUtils {
    private static final String PREFERENCE_KEY_USER_ID = "userId";
    private static final String PREFERENCE_KEY_NICKNAME = "nickname";
    private static final String PREFERENCE_KEY_TOKEN = "token";
    private static final String PREFERENCE_KEY_CONNECTED = "connected";

    private static Context mAppContext;

    // Prevent instantiation
    private PreferenceUtils() {
    }

    public static void init(Context appContext) {
        mAppContext = appContext;
    }

    private static SharedPreferences getSharedPreferences() {
        return mAppContext.getSharedPreferences("sendbird", Context.MODE_PRIVATE);
    }

    public static void setUserId(String userId) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_USER_ID, userId).apply();
    }

    public static String getUserId() {
        return getSharedPreferences().getString(PREFERENCE_KEY_USER_ID, "");
    }

    public static void setNickname(String nickname) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_NICKNAME, nickname).apply();
    }

    public static String getNickname() {
        return getSharedPreferences().getString(PREFERENCE_KEY_NICKNAME, "");
    }

    public static void setToken(String token) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_TOKEN, token).apply();
    }

    public static String getToken() {
        return getSharedPreferences().getString(PREFERENCE_KEY_TOKEN, "");
    }

    public static void setConnected(boolean tf) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(PREFERENCE_KEY_CONNECTED, tf).apply();
    }

    public static boolean getConnected() {
        return getSharedPreferences().getBoolean(PREFERENCE_KEY_CONNECTED, false);
    }

    public static void clearAll() {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.clear().apply();
    }
}
