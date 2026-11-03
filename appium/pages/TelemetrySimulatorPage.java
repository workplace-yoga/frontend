package com.adaptivetrust.qa.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class TelemetrySimulatorPage extends BasePage {
    private By latInput = By.id("com.adaptivetrust.mobile:id/sim_lat");
    private By lonInput = By.id("com.adaptivetrust.mobile:id/sim_lon");
    private By presetSF = By.id("com.adaptivetrust.mobile:id/btn_preset_sf");
    private By presetLondon = By.id("com.adaptivetrust.mobile:id/btn_preset_london");
    private By submitButton = By.id("com.adaptivetrust.mobile:id/btn_submit_telemetry");

    public TelemetrySimulatorPage(AndroidDriver driver) {
        super(driver);
    }

    public void setCoordinates(String lat, String lon) {
        sendKeys(latInput, lat);
        sendKeys(lonInput, lon);
    }

    public void clickSanFranciscoPreset() {
        click(presetSF);
    }

    public void clickLondonPreset() {
        click(presetLondon);
    }

    public void submit() {
        click(submitButton);
    }
}
