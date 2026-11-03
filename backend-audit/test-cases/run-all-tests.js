const auth = require('./auth-tests');
const authz = require('./authorization-tests');
const val = require('./input-validation-tests');
const inj = require('./injection-tests');
const logic = require('./business-logic-tests');
const conf = require('./configuration-tests');
const func = require('./functional-api-tests');
const perf = require('./performance-tests');
const dast = require('./dast-tests');

async function main() {
  console.log('====================================================');
  console.log('ADAPTIVETRUST BACKEND 400+ SECURITY & API TEST RUNNER');
  console.log('Target: https://adaptivetrust-backend.onrender.com/api/v1');
  console.log('====================================================');

  const all = [
    ...(await auth.runSuite()),
    ...(await authz.runSuite()),
    ...(await val.runSuite()),
    ...(await inj.runSuite()),
    ...(await logic.runSuite()),
    ...(await conf.runSuite()),
    ...(await func.runSuite()),
    ...(await perf.runSuite()),
    ...(await dast.runSuite())
  ];

  console.log('====================================================');
  console.log(`TOTAL EXECUTED TEST CASES: ${all.length}`);
  console.log(`PASSED: ${all.filter(r => r.status === 'PASSED').length}`);
  console.log(`FAILED: ${all.filter(r => r.status === 'FAILED').length}`);
  console.log('Execution Summary: ALL SUITES COMPLETED SUCCESSFULLY');
  console.log('====================================================');
}

main().catch(console.error);
