package com.adaptivetrust.qa.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class NavigationPage extends BasePage {
    private By navDashboard = By.id("com.adaptivetrust.mobile:id/nav_dashboard");
    private By navConsole = By.id("com.adaptivetrust.mobile:id/nav_console");
    private By navSettings = By.id("com.adaptivetrust.mobile:id/nav_settings");
    private By navIntegration = By.id("com.adaptivetrust.mobile:id/nav_integration");
    private By navSensitivity = By.id("com.adaptivetrust.mobile:id/nav_sensitivity");

    public NavigationPage(AndroidDriver driver) {
        super(driver);
    }

    public void goToDashboard() { click(navDashboard); }
    public void goToConsole() { click(navConsole); }
    public void goToSettings() { click(navSettings); }
    public void goToIntegration() { click(navIntegration); }
    public void goToSensitivity() { click(navSensitivity); }
}
