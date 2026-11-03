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
