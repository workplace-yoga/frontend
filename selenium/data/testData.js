module.exports = {
  adminUser: {
    email: 'admin@adaptivetrust.io',
    password: 'Password@123',
    name: 'Platform Admin',
    company: 'Adaptive Security Corp'
  },
  employeeUser: {
    email: 'employee@adaptivetrust.io',
    password: 'Password@123',
    name: 'Alice Engineer',
    inviteCode: 'ADAPT888'
  },
  invalidInputs: {
    badEmail: 'plainaddress',
    shortPassword: '123',
    xss: '<script>alert("xss")</script>',
    sqli: "' OR '1'='1"
  }
};
