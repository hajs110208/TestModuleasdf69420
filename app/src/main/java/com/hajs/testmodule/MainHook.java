package com.hajs.testmodule;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/** Entry point declared in assets/xposed_init. */
public class MainHook implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    private String modulePath;

    @Override
    public void initZygote(StartupParam startupParam) {
        // Path of this module's APK; needed to load the replacement classes from it.
        modulePath = startupParam.modulePath;
    }

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) {
        if (!ModuleConfig.TARGET_PACKAGE.equals(lpparam.packageName)) {
            return;
        }
        if (modulePath == null) {
            XposedBridge.log("[TestModule] module path unavailable, nothing replaced");
            return;
        }
        XposedBridge.log("[TestModule] hooking " + lpparam.packageName);
        new ClassReplacer(modulePath, ModuleConfig.REPLACED_CLASSES)
                .install(lpparam.classLoader);
    }
}
