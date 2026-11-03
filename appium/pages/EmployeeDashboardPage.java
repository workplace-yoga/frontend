package com.adaptivetrust.qa.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class EmployeeDashboardPage extends BasePage {
    private By scoreValue = By.id("com.adaptivetrust.mobile:id/gauge_score_value");
    private By statusBadge = By.id("com.adaptivetrust.mobile:id/gauge_status_value");
    private By timelineList = By.id("com.adaptivetrust.mobile:id/timeline_list");
    private By logoutButton = By.id("com.adaptivetrust.mobile:id/btn_employee_logout");

    public EmployeeDashboardPage(AndroidDriver driver) {
        super(driver);
    }

    public String getTrustScore() {
        return getText(scoreValue);
    }

    public String getTrustStatus() {
        return getText(statusBadge);
    }

    public boolean hasSecurityEvents() {
        return isDisplayed(timelineList);
    }

    public void logout() {
        click(logoutButton);
    }
}
