package com.adaptivetrust.qa.drivers;

import io.appium.java_client.android.AndroidDriver;

public class DriverManager {
    private static final ThreadLocal<AndroidDriver> driverThreadLocal = new ThreadLocal<>();

    public static AndroidDriver getDriver() {
        return driverThreadLocal.get();
    }

    public static void setDriver(AndroidDriver driver) {
        driverThreadLocal.set(driver);
    }

    public static void quitDriver() {
        AndroidDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.err.println("Error quitting driver: " + e.getMessage());
            } finally {
                driverThreadLocal.remove();
            }
        }
    }
}
