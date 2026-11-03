package com.adaptivetrust.qa.drivers;

import com.adaptivetrust.qa.config.AppiumConfig;
import com.adaptivetrust.qa.config.CapabilityManager;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import java.net.URL;
import java.time.Duration;

public class AppiumDriverFactory {
    public static AndroidDriver createDriver() {
        try {
            URL url = new URL(AppiumConfig.getAppiumServerUrl());
            UiAutomator2Options options = CapabilityManager.getAndroidOptions();
            AndroidDriver driver = new AndroidDriver(url, options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Appium AndroidDriver: " + e.getMessage(), e);
        }
    }
}
