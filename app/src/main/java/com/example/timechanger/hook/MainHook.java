package com.example.timechanger.hook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    
    private static final String TAG = "TimeChanger";
    
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedBridge.log(TAG + ": Loaded for package: " + lpparam.packageName);
        
        // Initialize our time changer hook
        TimeChangerHook timeChangerHook = new TimeChangerHook();
        timeChangerHook.handleLoadPackage(lpparam);
    }
} 