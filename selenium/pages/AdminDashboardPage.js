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
