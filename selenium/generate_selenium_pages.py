import os
import json

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

def write(rel_path, content):
    full_path = os.path.join(BASE_DIR, rel_path)
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    with open(full_path, "w", encoding="utf-8") as f:
        f.write(content.strip() + "\n")

# ==========================================================
# 1. package.json, jest.config.js, .env.example
# ==========================================================
package_json = {
  "name": "adaptivetrust-selenium-e2e",
  "version": "1.0.0",
  "description": "Enterprise-Grade Selenium E2E Automation for AdaptiveTrust Web Portal",
  "main": "index.js",
  "scripts": {
    "test": "jest --runInBand --testTimeout=60000",
    "test:parallel": "jest --maxWorkers=4 --testTimeout=60000",
    "test:auth": "jest tests/auth",
    "test:ui": "jest tests/ui",
    "generate:reports": "node utils/reportRunner.js"
  },
  "dependencies": {
    "selenium-webdriver": "^4.16.0",
    "chromedriver": "^120.0.0",
    "exceljs": "^4.4.0",
    "winston": "^3.11.0",
    "dotenv": "^16.3.1",
    "axios": "^1.6.2"
  },
  "devDependencies": {
    "jest": "^29.7.0"
  }
}
write("package.json", json.dumps(package_json, indent=2))

write("jest.config.js", """
module.exports = {
  testEnvironment: 'node',
  testMatch: ['**/tests/**/*.test.js'],
  setupFilesAfterEnv: ['./jest.setup.js'],
  verbose: true,
  testTimeout: 60000,
  reporters: ['default']
};
""")

write("jest.setup.js", """
require('dotenv').config();
jest.setTimeout(60000);
""")

write(".env.example", """
BASE_URL=https://workplace-yoga.github.io/frontend/
API_BASE=https://adaptivetrust-backend.onrender.com/api/v1
HEADLESS=true
BROWSER=chrome
VALID_ADMIN_EMAIL=admin@adaptivetrust.io
VALID_ADMIN_PASSWORD=Password@123
""")

# ==========================================================
# 2. CONFIG
# ==========================================================
write("config/config.js", """
require('dotenv').config();

module.exports = {
  BASE_URL: process.env.BASE_URL || 'https://workplace-yoga.github.io/frontend/',
  API_BASE: process.env.API_BASE || 'https://adaptivetrust-backend.onrender.com/api/v1',
  TIMEOUT: parseInt(process.env.TIMEOUT || '30000', 10),
  BROWSER: process.env.BROWSER || 'chrome',
  HEADLESS: process.env.HEADLESS !== 'false',
  VALID_ADMIN_EMAIL: process.env.VALID_ADMIN_EMAIL || 'admin@adaptivetrust.io',
  VALID_ADMIN_PASSWORD: process.env.VALID_ADMIN_PASSWORD || 'Password@123',
  VALID_EMP_EMAIL: process.env.VALID_EMP_EMAIL || 'employee@adaptivetrust.io',
  VALID_EMP_PASSWORD: process.env.VALID_EMP_PASSWORD || 'Password@123',
  VALID_INVITE_CODE: process.env.VALID_INVITE_CODE || 'ADAPT888'
};
""")

write("config/webdriver.config.js", """
const { Builder } = require('selenium-webdriver');
const chrome = require('selenium-webdriver/chrome');
const config = require('./config');

async function getDriver() {
  const options = new chrome.Options();
  if (config.HEADLESS) {
    options.addArguments('--headless=new');
  }
  options.addArguments(
    '--no-sandbox',
    '--disable-dev-shm-usage',
    '--disable-gpu',
    '--window-size=1920,1080',
    '--ignore-certificate-errors'
  );

  const driver = await new Builder()
    .forBrowser(config.BROWSER)
    .setChromeOptions(options)
    .build();

  await driver.manage().setTimeouts({ implicit: 10000, pageLoad: 30000 });
  return driver;
}

module.exports = { getDriver };
""")

write("drivers/driverFactory.js", """
const { getDriver } = require('../config/webdriver.config');

class DriverFactory {
  static async createDriver() {
    return await getDriver();
  }
}

module.exports = DriverFactory;
""")

# ==========================================================
# 3. PAGES (Page Object Model)
# ==========================================================
write("pages/BasePage.js", """
const { By, until } = require('selenium-webdriver');
const config = require('../config/config');

class BasePage {
  constructor(driver) {
    this.driver = driver;
    this.timeout = config.TIMEOUT;
  }

  async navigate(path = '') {
    const fullUrl = path.startsWith('http') ? path : `${config.BASE_URL}${path}`;
    await this.driver.get(fullUrl);
  }

  async waitForElement(locator, timeout = this.timeout) {
    return await this.driver.wait(until.elementLocated(locator), timeout);
  }

  async click(locator) {
    const el = await this.waitForElement(locator);
    await this.driver.wait(until.elementIsVisible(el), this.timeout);
    await el.click();
  }

  async type(locator, text) {
    const el = await this.waitForElement(locator);
    await el.clear();
    await el.sendKeys(text);
  }

  async getText(locator) {
    const el = await this.waitForElement(locator);
    return await el.getText();
  }

  async isVisible(locator) {
    try {
      const el = await this.waitForElement(locator, 5000);
      return await el.isDisplayed();
    } catch (e) {
      return false;
    }
  }

  async getTitle() {
    return await this.driver.getTitle();
  }
}

module.exports = BasePage;
""")

write("pages/AuthPage.js", """
const { By } = require('selenium-webdriver');
const BasePage = require('./BasePage');

class AuthPage extends BasePage {
  constructor(driver) {
    super(driver);
    this.tabLogin = By.id('tab-login');
    this.tabAdminSignUp = By.id('tab-admin-signup');
    this.tabEmpSignUp = By.id('tab-emp-signup');
    this.tabVerify = By.id('tab-verify');
    this.toastMessage = By.id('toast-message');
    this.settingsGear = By.css('.btn-settings-gear');
  }

  async openLoginTab() { await this.click(this.tabLogin); }
  async openAdminSignUpTab() { await this.click(this.tabAdminSignUp); }
  async openEmployeeSignUpTab() { await this.click(this.tabEmpSignUp); }
  async openVerifyTab() { await this.click(this.tabVerify); }
  async openSettings() { await this.click(this.settingsGear); }

  async getToastText() {
    return await this.getText(this.toastMessage);
  }
}

module.exports = AuthPage;
""")

write("pages/LoginPage.js", """
const { By } = require('selenium-webdriver');
const BasePage = require('./BasePage');

class LoginPage extends BasePage {
  constructor(driver) {
    super(driver);
    this.emailInput = By.id('login-email');
    this.passwordInput = By.id('login-password');
    this.submitBtn = By.css('#form-login button[type="submit"]');
  }

  async login(email, password) {
    await this.type(this.emailInput, email);
    await this.type(this.passwordInput, password);
    await this.click(this.submitBtn);
  }
}

module.exports = LoginPage;
""")

write("pages/AdminSignUpPage.js", """
const { By } = require('selenium-webdriver');
const BasePage = require('./BasePage');

class AdminSignUpPage extends BasePage {
  constructor(driver) {
    super(driver);
    this.nameInput = By.id('admin-name');
    this.emailInput = By.id('admin-email');
    this.passwordInput = By.id('admin-password');
    this.companyInput = By.id('admin-company');
    this.submitBtn = By.css('#form-admin-signup button[type="submit"]');
  }

  async registerAdmin(name, email, password, company) {
    await this.type(this.nameInput, name);
    await this.type(this.emailInput, email);
    await this.type(this.passwordInput, password);
    await this.type(this.companyInput, company);
    await this.click(this.submitBtn);
  }
}

module.exports = AdminSignUpPage;
""")

write("pages/EmployeeSignUpPage.js", """
const { By } = require('selenium-webdriver');
const BasePage = require('./BasePage');

class EmployeeSignUpPage extends BasePage {
  constructor(driver) {
    super(driver);
    this.nameInput = By.id('emp-name');
    this.emailInput = By.id('emp-email');
    this.passwordInput = By.id('emp-password');
    this.codeInput = By.id('emp-code');
    this.submitBtn = By.css('#form-emp-signup button[type="submit"]');
  }

  async registerEmployee(name, email, password, code) {
    await this.type(this.nameInput, name);
    await this.type(this.emailInput, email);
    await this.type(this.passwordInput, password);
    await this.type(this.codeInput, code);
    await this.click(this.submitBtn);
  }
}

module.exports = EmployeeSignUpPage;
""")

write("pages/OTPVerifyPage.js", """
const { By } = require('selenium-webdriver');
const BasePage = require('./BasePage');

class OTPVerifyPage extends BasePage {
  constructor(driver) {
    super(driver);
    this.otpInput = By.id('verify-otp');
    this.submitBtn = By.css('#form-verify button[type="submit"]');
    this.resendBtn = By.xpath("//button[contains(text(), 'Resend Code')]");
    this.displayEmail = By.id('verify-display-email');
  }

  async verify(code) {
    await this.type(this.otpInput, code);
    await this.click(this.submitBtn);
  }

  async resendCode() {
    await this.click(this.resendBtn);
  }
}

module.exports = OTPVerifyPage;
""")

write("pages/AdminDashboardPage.js", """
const { By } = require('selenium-webdriver');
const BasePage = require('./BasePage');

class AdminDashboardPage extends BasePage {
  constructor(driver) {
    super(driver);
    this.totalUsersMetric = By.id('metric-total-users');
    this.activeUsersMetric = By.id('metric-active-users');
    this.riskAlertsMetric = By.id('metric-risk-alerts');
    this.filterRole = By.id('admin-filter-role');
    this.sortBy = By.id('admin-sort-by');
    this.directoryTable = By.id('directory-table');
    this.liveStreamFeed = By.id('live-stream-feed');
    this.boostBtn = By.xpath("//button[contains(text(), 'BOOST TRUST')]");
  }

  async getTotalUsers() { return await this.getText(this.totalUsersMetric); }
  async getActiveUsers() { return await this.getText(this.activeUsersMetric); }
}

module.exports = AdminDashboardPage;
""")

write("pages/EmployeeDashboardPage.js", """
const { By } = require('selenium-webdriver');
const BasePage = require('./BasePage');

class EmployeeDashboardPage extends BasePage {
  constructor(driver) {
    super(driver);
    this.gaugeScore = By.id('gauge-score-value');
    this.gaugeStatus = By.id('gauge-status-value');
    this.timelineList = By.id('timeline-list');
  }

  async getScore() { return await this.getText(this.gaugeScore); }
  async getStatus() { return await this.getText(this.gaugeStatus); }
}

module.exports = EmployeeDashboardPage;
""")

write("pages/SuperAdminPage.js", """
const { By } = require('selenium-webdriver');
const BasePage = require('./BasePage');

class SuperAdminPage extends BasePage {
  constructor(driver) {
    super(driver);
    this.companiesMetric = By.id('super-metric-companies');
    this.usersMetric = By.id('super-metric-users');
    this.companiesTbody = By.id('super-companies-tbody');
  }

  async getCompaniesCount() { return await this.getText(this.companiesMetric); }
}

module.exports = SuperAdminPage;
""")

write("pages/TelemetrySimulatorPage.js", """
const { By } = require('selenium-webdriver');
const BasePage = require('./BasePage');

class TelemetrySimulatorPage extends BasePage {
  constructor(driver) {
    super(driver);
    this.latInput = By.id('sim-lat');
    this.lonInput = By.id('sim-lon');
    this.submitBtn = By.css('#form-telemetry-sim button[type="submit"]');
  }

  async submitTelemetry(lat, lon) {
    await this.type(this.latInput, lat);
    await this.type(this.lonInput, lon);
    await this.click(this.submitBtn);
  }
}

module.exports = TelemetrySimulatorPage;
""")

write("pages/BackendSettingsPage.js", """
const { By } = require('selenium-webdriver');
const BasePage = require('./BasePage');

class BackendSettingsPage extends BasePage {
  constructor(driver) {
    super(driver);
    this.apiUrlInput = By.id('settings-api-url');
    this.saveBtn = By.xpath("//button[contains(text(), 'Save & Reload')]");
    this.resetBtn = By.xpath("//button[contains(text(), 'Reset to Default')]");
    this.closeBtn = By.css('#backend-settings-modal .modal-close');
  }

  async setUrl(url) {
    await this.type(this.apiUrlInput, url);
    await this.click(this.saveBtn);
  }
}

module.exports = BackendSettingsPage;
""")

print("Selenium pages created.")

