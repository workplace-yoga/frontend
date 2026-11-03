package com.adaptivetrust.qa.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class SuperAdminPage extends BasePage {
    private By totalWorkspaces = By.id("com.adaptivetrust.mobile:id/super_metric_companies");
    private By totalPlatformUsers = By.id("com.adaptivetrust.mobile:id/super_metric_users");
    private By refreshButton = By.id("com.adaptivetrust.mobile:id/btn_refresh_system");
    private By companiesTable = By.id("com.adaptivetrust.mobile:id/super_companies_list");

    public SuperAdminPage(AndroidDriver driver) {
        super(driver);
    }

    public String getTotalWorkspaces() {
        return getText(totalWorkspaces);
    }

    public void refreshData() {
        click(refreshButton);
    }
}
