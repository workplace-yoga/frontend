package com.adaptivetrust.qa.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class OTPVerifyPage extends BasePage {
    private By otpInput = By.id("com.adaptivetrust.mobile:id/verify_otp");
    private By verifyButton = By.id("com.adaptivetrust.mobile:id/btn_verify_activate");
    private By resendButton = By.id("com.adaptivetrust.mobile:id/btn_resend_code");
    private By backToLoginButton = By.id("com.adaptivetrust.mobile:id/btn_back_to_login");
    private By displayEmail = By.id("com.adaptivetrust.mobile:id/verify_display_email");

    public OTPVerifyPage(AndroidDriver driver) {
        super(driver);
    }

    public void enterOtp(String otp) {
        sendKeys(otpInput, otp);
    }

    public void clickVerify() {
        click(verifyButton);
    }

    public void clickResend() {
        click(resendButton);
    }

    public void clickBackToLogin() {
        click(backToLoginButton);
    }

    public String getDisplayEmail() {
        return getText(displayEmail);
    }
}
