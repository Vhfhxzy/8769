package com.example.timechanger.hook;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.os.SystemClock;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.TimeZone;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class TimeChangerHook implements IXposedHookLoadPackage {
    
    private static final String TAG = "TimeChangerHook";
    
    // Target date: 2025-05-16
    private static final int TARGET_YEAR = 2025;
    private static final int TARGET_MONTH = 4; // May (0-based index)
    private static final int TARGET_DAY = 16;
    
    private static boolean timeModified = false;
    private static long originalTime = 0;

    // Frontend package name - replace with your actual app package
    private static final String FRONTEND_PACKAGE = "com.example.yourapp";
    
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("android")) {
            hookSystemTime(lpparam);
            hookAutoTimeSync(lpparam);
        }
    }

    private void hookSystemTime(XC_LoadPackage.LoadPackageParam lpparam) {
        // Hook System.currentTimeMillis()
        XposedHelpers.findAndHookMethod("java.lang.System", lpparam.classLoader,
            "currentTimeMillis", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (timeModified) {
                        // Return the target time
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(TARGET_YEAR, TARGET_MONTH, TARGET_DAY, 12, 0, 0);
                        param.setResult(calendar.getTimeInMillis());
                    }
                }
            });

        // Hook SystemClock.currentTimeMillis() if it exists
        try {
            XposedHelpers.findAndHookMethod("android.os.SystemClock", lpparam.classLoader,
                "currentTimeMillis", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (timeModified) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(TARGET_YEAR, TARGET_MONTH, TARGET_DAY, 12, 0, 0);
                            param.setResult(calendar.getTimeInMillis());
                        }
                    }
                });
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Could not hook SystemClock.currentTimeMillis(): " + e.getMessage());
        }

        // Hook Calendar and Date to return modified time
        hookCalendarAndDate(lpparam);
        
        // Register for package state changes to detect when the frontend app exits
        XposedHelpers.findAndHookMethod("android.app.ContextImpl", lpparam.classLoader, "registerReceiver",
            BroadcastReceiver.class, IntentFilter.class, String.class, android.os.Handler.class, int.class,
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    BroadcastReceiver receiver = (BroadcastReceiver) param.args[0];
                    IntentFilter filter = (IntentFilter) param.args[1];
                    
                    if (filter != null && receiver != null) {
                        try {
                            IntentFilter packageFilter = new IntentFilter();
                            packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
                            packageFilter.addAction(Intent.ACTION_PACKAGE_RESTARTED);
                            packageFilter.addDataScheme("package");
                            
                            Context context = (Context) XposedHelpers.callMethod(param.thisObject, "getBaseContext");
                            context.registerReceiver(new BroadcastReceiver() {
                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    String packageName = intent.getData().getSchemeSpecificPart();
                                    if (FRONTEND_PACKAGE.equals(packageName)) {
                                        // Frontend app was closed, restore original time
                                        timeModified = false;
                                        XposedBridge.log(TAG + ": Frontend app closed, restoring original time");
                                    }
                                }
                            }, packageFilter);
                        } catch (Exception e) {
                            XposedBridge.log(TAG + ": Error registering package receiver: " + e.getMessage());
                        }
                    }
                }
            });
    }

    private void hookCalendarAndDate(XC_LoadPackage.LoadPackageParam lpparam) {
        // Hook Calendar.getInstance()
        XposedHelpers.findAndHookMethod("java.util.Calendar", lpparam.classLoader,
            "getInstance", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (timeModified) {
                        Calendar calendar = (Calendar) param.getResult();
                        calendar.set(TARGET_YEAR, TARGET_MONTH, TARGET_DAY, 12, 0, 0);
                        param.setResult(calendar);
                    }
                }
            });
            
        // Hook Date constructor
        XposedHelpers.findAndHookConstructor("java.util.Date", lpparam.classLoader, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (timeModified) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(TARGET_YEAR, TARGET_MONTH, TARGET_DAY, 12, 0, 0);
                    XposedHelpers.callMethod(param.thisObject, "setTime", calendar.getTimeInMillis());
                }
            }
        });
    }

    private void hookAutoTimeSync(XC_LoadPackage.LoadPackageParam lpparam) {
        // Disable automatic time sync by intercepting the setting check
        XposedHelpers.findAndHookMethod("android.provider.Settings$Global", lpparam.classLoader,
            "getInt", android.content.ContentResolver.class, String.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String name = (String) param.args[1];
                    
                    // When checking if auto time is enabled, return 0 (disabled)
                    if ("auto_time".equals(name) && timeModified) {
                        param.setResult(0);
                    }
                    
                    // When checking if auto timezone is enabled, return 0 (disabled)
                    if ("auto_timezone".equals(name) && timeModified) {
                        param.setResult(0);
                    }
                }
            });
            
        // Hook into the system to enable our time modification
        XposedHelpers.findAndHookMethod("com.android.server.timedetector.TimeDetectorService", 
            lpparam.classLoader, "onBootPhase", int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    // Save the original time when our module loads
                    if (!timeModified && originalTime == 0) {
                        originalTime = System.currentTimeMillis();
                        timeModified = true;
                        XposedBridge.log(TAG + ": Time modification enabled, original time saved: " + originalTime);
                    }
                }
            });
    }
} 