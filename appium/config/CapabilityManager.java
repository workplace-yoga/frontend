package com.adaptivetrust.qa.config;

import io.appium.java_client.android.options.UiAutomator2Options;
import java.io.File;
import java.time.Duration;

public class CapabilityManager {
    public static UiAutomator2Options getAndroidOptions() {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");
        options.setDeviceName(AppiumConfig.getDeviceName());
        options.setPlatformVersion(AppiumConfig.getPlatformVersion());

        String apkPath = AppiumConfig.getApkPath();
        File appFile = new File(apkPath);
        if (appFile.exists()) {
            options.setApp(appFile.getAbsolutePath());
        }

        options.setAppPackage("com.adaptivetrust.mobile");
        options.setAppActivity("com.adaptivetrust.mobile.ui.MainActivity");
        options.setNoReset(false);
        options.setFullReset(false);
        options.setAutoGrantPermissions(true);
        options.setNewCommandTimeout(Duration.ofSeconds(300));
        return options;
    }
}
