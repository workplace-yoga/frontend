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
