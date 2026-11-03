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
