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
