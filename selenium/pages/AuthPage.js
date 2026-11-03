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
