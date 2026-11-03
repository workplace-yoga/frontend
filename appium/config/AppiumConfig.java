package com.adaptivetrust.qa.config;

import java.io.InputStream;
import java.util.Properties;

public class AppiumConfig {
    private static Properties properties = new Properties();

    static {
        try {
            InputStream is = AppiumConfig.class.getClassLoader().getResourceAsStream("app.properties");
            if (is != null) {
                properties.load(is);
            }
        } catch (Exception e) {
            System.err.println("Failed to load app.properties: " + e.getMessage());
        }
    }

    public static String getProperty(String key, String defaultValue) {
        return System.getProperty(key, properties.getProperty(key, defaultValue));
    }

    public static String getAppiumServerUrl() {
        return getProperty("appium.server.url", "http://127.0.0.1:4723");
    }

    public static String getApkPath() {
        return getProperty("app.apk.path", "C:/Users/yogan/.gemini/antigravity-ide/scratch/adaptivetrust-multitenant/AdaptiveTrust-Mobile-Release.apk");
    }

    public static String getDeviceName() {
        return getProperty("device.name", "Android Emulator");
    }

    public static String getPlatformVersion() {
        return getProperty("platform.version", "13.0");
    }

    public static int getExplicitWait() {
        return Integer.parseInt(getProperty("timeout.explicit", "20"));
    }
}
