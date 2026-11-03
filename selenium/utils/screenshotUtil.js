const fs = require('fs');
const path = require('path');

class ScreenshotUtil {
  static async takeScreenshot(driver, testName) {
    try {
      const image = await driver.takeScreenshot();
      const dir = path.join(__dirname, '../screenshots');
      if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
      const filename = `${testName}_${Date.now()}.png`;
      fs.writeFileSync(path.join(dir, filename), image, 'base64');
      return filename;
    } catch (e) {
      console.error('Failed to take screenshot:', e.message);
      return null;
    }
  }
}

module.exports = ScreenshotUtil;
