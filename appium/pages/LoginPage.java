package com.adaptivetrust.qa.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class LoginPage extends BasePage {
    private By emailInput = By.id("com.adaptivetrust.mobile:id/login_email");
    private By passwordInput = By.id("com.adaptivetrust.mobile:id/login_password");
    private By signInButton = By.id("com.adaptivetrust.mobile:id/btn_sign_in");
    private By adminSignUpTab = By.id("com.adaptivetrust.mobile:id/tab_admin_signup");
    private By employeeSignUpTab = By.id("com.adaptivetrust.mobile:id/tab_emp_signup");
    private By errorMessage = By.id("com.adaptivetrust.mobile:id/tv_error_message");
    private By settingsGear = By.id("com.adaptivetrust.mobile:id/btn_settings_gear");

    public LoginPage(AndroidDriver driver) {
        super(driver);
    }

    public void enterEmail(String email) {
        sendKeys(emailInput, email);
    }

    public void enterPassword(String password) {
        sendKeys(passwordInput, password);
    }

    public void clickSignIn() {
        click(signInButton);
    }

    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickSignIn();
    }

    public void clickAdminSignUpTab() {
        click(adminSignUpTab);
    }

    public void clickEmployeeSignUpTab() {
        click(employeeSignUpTab);
    }

    public boolean isErrorMessageDisplayed() {
        return isDisplayed(errorMessage);
    }

    public String getErrorMessage() {
        return getText(errorMessage);
    }

    public boolean isSignInButtonEnabled() {
        return driver.findElement(signInButton).isEnabled();
    }

    public void openSettings() {
        click(settingsGear);
    }
}
