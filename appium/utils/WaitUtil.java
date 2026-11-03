package com.adaptivetrust.qa.utils;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WaitUtil {
    public static WebElement waitForVisibility(AndroidDriver driver, By locator, int timeoutSec) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSec))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForClickable(AndroidDriver driver, By locator, int timeoutSec) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSec))
                .until(ExpectedConditions.elementToBeClickable(locator));
    }
}
