package com.adaptivetrust.qa.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class EmployeeSignUpPage extends BasePage {
    private By fullNameInput = By.id("com.adaptivetrust.mobile:id/emp_name");
    private By emailInput = By.id("com.adaptivetrust.mobile:id/emp_email");
    private By passwordInput = By.id("com.adaptivetrust.mobile:id/emp_password");
    private By inviteCodeInput = By.id("com.adaptivetrust.mobile:id/emp_code");
    private By registerDeviceButton = By.id("com.adaptivetrust.mobile:id/btn_register_device");

    public EmployeeSignUpPage(AndroidDriver driver) {
        super(driver);
    }

    public void registerEmployee(String name, String email, String password, String inviteCode) {
        sendKeys(fullNameInput, name);
        sendKeys(emailInput, email);
        sendKeys(passwordInput, password);
        sendKeys(inviteCodeInput, inviteCode);
        click(registerDeviceButton);
    }
}
