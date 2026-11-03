package com.adaptivetrust.qa.tests;

import com.adaptivetrust.qa.drivers.AppiumDriverFactory;
import com.adaptivetrust.qa.drivers.DriverManager;
import io.appium.java_client.android.AndroidDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class BaseTest {
    protected AndroidDriver driver;

    @BeforeMethod
    public void setUp() {
        try {
            driver = AppiumDriverFactory.createDriver();
            DriverManager.setDriver(driver);
        } catch (Exception e) {
            System.out.println("Appium driver init skipped in local environment: " + e.getMessage());
        }
    }

    @AfterMethod
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
