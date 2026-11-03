const { until } = require('selenium-webdriver');

class WaitUtil {
  static async sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
}

module.exports = WaitUtil;
