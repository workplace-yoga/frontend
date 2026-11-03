const { By } = require('selenium-webdriver');
const BasePage = require('./BasePage');

class TelemetrySimulatorPage extends BasePage {
  constructor(driver) {
    super(driver);
    this.latInput = By.id('sim-lat');
    this.lonInput = By.id('sim-lon');
    this.submitBtn = By.css('#form-telemetry-sim button[type="submit"]');
  }

  async submitTelemetry(lat, lon) {
    await this.type(this.latInput, lat);
    await this.type(this.lonInput, lon);
    await this.click(this.submitBtn);
  }
}

module.exports = TelemetrySimulatorPage;
