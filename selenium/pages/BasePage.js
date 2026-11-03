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
