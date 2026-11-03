package com.adaptivetrust.qa.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class AdminDashboardPage extends BasePage {
    private By totalUsersMetric = By.id("com.adaptivetrust.mobile:id/metric_total_users");
    private By activeDevicesMetric = By.id("com.adaptivetrust.mobile:id/metric_active_users");
    private By riskAlertsMetric = By.id("com.adaptivetrust.mobile:id/metric_risk_alerts");
    private By filterRoleDropdown = By.id("com.adaptivetrust.mobile:id/filter_role");
    private By sortByDropdown = By.id("com.adaptivetrust.mobile:id/sort_by");
    private By liveFeedTerminal = By.id("com.adaptivetrust.mobile:id/live_stream_feed");
    private By clearFeedButton = By.id("com.adaptivetrust.mobile:id/btn_clear_feed");
    private By logoutButton = By.id("com.adaptivetrust.mobile:id/btn_logout");

    public AdminDashboardPage(AndroidDriver driver) {
        super(driver);
    }

    public String getTotalUsersCount() {
        return getText(totalUsersMetric);
    }

    public String getActiveDevicesCount() {
        return getText(activeDevicesMetric);
    }

    public String getRiskAlertsCount() {
        return getText(riskAlertsMetric);
    }

    public void clickLogout() {
        click(logoutButton);
    }

    public void clearFeed() {
        click(clearFeedButton);
    }
}
