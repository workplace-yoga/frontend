const { getDriver } = require('../config/webdriver.config');

class DriverFactory {
  static async createDriver() {
    return await getDriver();
  }
}

module.exports = DriverFactory;
