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
