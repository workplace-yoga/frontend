require('dotenv').config();

module.exports = {
  BASE_URL: process.env.BASE_URL || 'https://workplace-yoga.github.io/frontend/',
  API_BASE: process.env.API_BASE || 'https://adaptivetrust-backend.onrender.com/api/v1',
  TIMEOUT: parseInt(process.env.TIMEOUT || '30000', 10),
  BROWSER: process.env.BROWSER || 'chrome',
  HEADLESS: process.env.HEADLESS !== 'false',
  VALID_ADMIN_EMAIL: process.env.VALID_ADMIN_EMAIL || 'admin@adaptivetrust.io',
  VALID_ADMIN_PASSWORD: process.env.VALID_ADMIN_PASSWORD || 'Password@123',
  VALID_EMP_EMAIL: process.env.VALID_EMP_EMAIL || 'employee@adaptivetrust.io',
  VALID_EMP_PASSWORD: process.env.VALID_EMP_PASSWORD || 'Password@123',
  VALID_INVITE_CODE: process.env.VALID_INVITE_CODE || 'ADAPT888'
};
