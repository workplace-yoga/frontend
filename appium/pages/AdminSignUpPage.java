package com.adaptivetrust.qa.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class AdminSignUpPage extends BasePage {
    private By fullNameInput = By.id("com.adaptivetrust.mobile:id/admin_name");
    private By emailInput = By.id("com.adaptivetrust.mobile:id/admin_email");
    private By passwordInput = By.id("com.adaptivetrust.mobile:id/admin_password");
    private By companyInput = By.id("com.adaptivetrust.mobile:id/admin_company");
    private By createWorkspaceButton = By.id("com.adaptivetrust.mobile:id/btn_create_workspace");
    private By successBanner = By.id("com.adaptivetrust.mobile:id/tv_success_banner");

    public AdminSignUpPage(AndroidDriver driver) {
        super(driver);
    }

    public void registerAdmin(String name, String email, String password, String company) {
        sendKeys(fullNameInput, name);
        sendKeys(emailInput, email);
        sendKeys(passwordInput, password);
        sendKeys(companyInput, company);
        click(createWorkspaceButton);
    }

    public boolean isSuccessBannerDisplayed() {
        return isDisplayed(successBanner);
    }
}
