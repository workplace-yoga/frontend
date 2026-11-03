const { Builder } = require('selenium-webdriver');
const chrome = require('selenium-webdriver/chrome');
const config = require('./config');

async function getDriver() {
  const options = new chrome.Options();
  if (config.HEADLESS) {
    options.addArguments('--headless=new');
  }
  options.addArguments(
    '--no-sandbox',
    '--disable-dev-shm-usage',
    '--disable-gpu',
    '--window-size=1920,1080',
    '--ignore-certificate-errors'
  );

  const driver = await new Builder()
    .forBrowser(config.BROWSER)
    .setChromeOptions(options)
    .build();

  await driver.manage().setTimeouts({ implicit: 10000, pageLoad: 30000 });
  return driver;
}

module.exports = { getDriver };
