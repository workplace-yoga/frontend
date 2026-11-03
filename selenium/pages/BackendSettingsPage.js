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
