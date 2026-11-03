import os

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

def write(rel_path, content):
    full_path = os.path.join(BASE_DIR, rel_path)
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    with open(full_path, "w", encoding="utf-8") as f:
        f.write(content.strip() + "\n")

# ==========================================================
# 1. CONFIG
# ==========================================================
write("config/AppiumConfig.java", """
package com.adaptivetrust.qa.config;

import java.io.InputStream;
import java.util.Properties;

public class AppiumConfig {
    private static Properties properties = new Properties();

    static {
        try {
            InputStream is = AppiumConfig.class.getClassLoader().getResourceAsStream("app.properties");
            if (is != null) {
                properties.load(is);
            }
        } catch (Exception e) {
            System.err.println("Failed to load app.properties: " + e.getMessage());
        }
    }

    public static String getProperty(String key, String defaultValue) {
        return System.getProperty(key, properties.getProperty(key, defaultValue));
    }

    public static String getAppiumServerUrl() {
        return getProperty("appium.server.url", "http://127.0.0.1:4723");
    }

    public static String getApkPath() {
        return getProperty("app.apk.path", "C:/Users/yogan/.gemini/antigravity-ide/scratch/adaptivetrust-multitenant/AdaptiveTrust-Mobile-Release.apk");
    }

    public static String getDeviceName() {
        return getProperty("device.name", "Android Emulator");
    }

    public static String getPlatformVersion() {
        return getProperty("platform.version", "13.0");
    }

    public static int getExplicitWait() {
        return Integer.parseInt(getProperty("timeout.explicit", "20"));
    }
}
""")

write("config/CapabilityManager.java", """
package com.adaptivetrust.qa.config;

import io.appium.java_client.android.options.UiAutomator2Options;
import java.io.File;
import java.time.Duration;

public class CapabilityManager {
    public static UiAutomator2Options getAndroidOptions() {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");
        options.setDeviceName(AppiumConfig.getDeviceName());
        options.setPlatformVersion(AppiumConfig.getPlatformVersion());

        String apkPath = AppiumConfig.getApkPath();
        File appFile = new File(apkPath);
        if (appFile.exists()) {
            options.setApp(appFile.getAbsolutePath());
        }

        options.setAppPackage("com.adaptivetrust.mobile");
        options.setAppActivity("com.adaptivetrust.mobile.ui.MainActivity");
        options.setNoReset(false);
        options.setFullReset(false);
        options.setAutoGrantPermissions(true);
        options.setNewCommandTimeout(Duration.ofSeconds(300));
        return options;
    }
}
""")

write("config/TestConfig.java", """
package com.adaptivetrust.qa.config;

public class TestConfig {
    public static final String DEFAULT_ADMIN_EMAIL = "admin@adaptivetrust.io";
    public static final String DEFAULT_ADMIN_PASSWORD = "Password@123";
    public static final String DEFAULT_EMP_EMAIL = "employee@adaptivetrust.io";
    public static final String DEFAULT_EMP_PASSWORD = "Password@123";
    public static final String DEFAULT_INVITE_CODE = "ADAPT888";
    public static final String BASE_API_URL = "https://adaptivetrust-backend.onrender.com/api/v1";
}
""")

# ==========================================================
# 2. DRIVERS
# ==========================================================
write("drivers/DriverManager.java", """
package com.adaptivetrust.qa.drivers;

import io.appium.java_client.android.AndroidDriver;

public class DriverManager {
    private static final ThreadLocal<AndroidDriver> driverThreadLocal = new ThreadLocal<>();

    public static AndroidDriver getDriver() {
        return driverThreadLocal.get();
    }

    public static void setDriver(AndroidDriver driver) {
        driverThreadLocal.set(driver);
    }

    public static void quitDriver() {
        AndroidDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.err.println("Error quitting driver: " + e.getMessage());
            } finally {
                driverThreadLocal.remove();
            }
        }
    }
}
""")

write("drivers/AppiumDriverFactory.java", """
package com.adaptivetrust.qa.drivers;

import com.adaptivetrust.qa.config.AppiumConfig;
import com.adaptivetrust.qa.config.CapabilityManager;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import java.net.URL;
import java.time.Duration;

public class AppiumDriverFactory {
    public static AndroidDriver createDriver() {
        try {
            URL url = new URL(AppiumConfig.getAppiumServerUrl());
            UiAutomator2Options options = CapabilityManager.getAndroidOptions();
            AndroidDriver driver = new AndroidDriver(url, options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Appium AndroidDriver: " + e.getMessage(), e);
        }
    }
}
""")

# ==========================================================
# 3. PAGES (Page Object Model)
# ==========================================================
write("pages/BasePage.java", """
package com.adaptivetrust.qa.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class BasePage {
    protected AndroidDriver driver;
    protected WebDriverWait wait;

    public BasePage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        PageFactory.initElements(driver, this);
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void click(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
    }

    protected void sendKeys(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.clear();
        el.sendKeys(text);
    }

    protected void sendKeys(WebElement element, String text) {
        wait.until(ExpectedConditions.visibilityOf(element));
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
    }

    protected String getText(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element)).getText();
    }

    protected boolean isDisplayed(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isDisplayed(WebElement element) {
        try {
            return wait.until(ExpectedConditions.visibilityOf(element)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
""")

write("pages/LoginPage.java", """
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
""")

write("pages/AdminSignUpPage.java", """
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
""")

write("pages/EmployeeSignUpPage.java", """
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
""")

write("pages/OTPVerifyPage.java", """
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
""")

write("pages/AdminDashboardPage.java", """
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
""")

write("pages/EmployeeDashboardPage.java", """
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
""")

write("pages/SuperAdminPage.java", """
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
""")

write("pages/TelemetrySimulatorPage.java", """
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
""")

write("pages/NavigationPage.java", """
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
""")

print("Pages created.")

