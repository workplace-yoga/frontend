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
