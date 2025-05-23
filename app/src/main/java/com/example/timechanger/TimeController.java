package com.example.timechanger;

import android.content.Context;
import android.content.SharedPreferences;

public class TimeController {
    private static final String PREFS_NAME = "time_changer_prefs";
    private static final String KEY_TIME_ENABLED = "time_modification_enabled";
    
    /**
     * Enable time modification to 2025-05-16
     * @param context Application context
     */
    public static void enableTimeModification(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_TIME_ENABLED, true);
        editor.apply();
    }
    
    /**
     * Disable time modification and restore original time
     * @param context Application context
     */
    public static void disableTimeModification(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_TIME_ENABLED, false);
        editor.apply();
    }
    
    /**
     * Check if time modification is currently enabled
     * @param context Application context
     * @return true if modification is enabled
     */
    public static boolean isTimeModificationEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_READABLE);
        return prefs.getBoolean(KEY_TIME_ENABLED, false);
    }
} 