import os
import json

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

def ensure_dir(path):
    os.makedirs(path, exist_ok=True)

# 1. Directories
dirs = [
    os.path.join(BASE_DIR, "config"),
    os.path.join(BASE_DIR, "drivers"),
    os.path.join(BASE_DIR, "pages"),
    os.path.join(BASE_DIR, "tests", "auth"),
    os.path.join(BASE_DIR, "tests", "dashboard"),
    os.path.join(BASE_DIR, "tests", "navigation"),
    os.path.join(BASE_DIR, "tests", "forms"),
    os.path.join(BASE_DIR, "tests", "crud"),
    os.path.join(BASE_DIR, "tests", "security"),
    os.path.join(BASE_DIR, "tests", "performance"),
    os.path.join(BASE_DIR, "tests", "accessibility"),
    os.path.join(BASE_DIR, "tests", "regression"),
    os.path.join(BASE_DIR, "tests", "superadmin"),
    os.path.join(BASE_DIR, "data"),
    os.path.join(BASE_DIR, "utils"),
    os.path.join(BASE_DIR, "listeners"),
    os.path.join(BASE_DIR, "runners"),
    os.path.join(BASE_DIR, "resources"),
    os.path.join(BASE_DIR, "reports"),
    os.path.join(BASE_DIR, "screenshots"),
    os.path.join(BASE_DIR, "logs"),
    os.path.join(BASE_DIR, ".github", "workflows"),
]

for d in dirs:
    ensure_dir(d)

# 2. pom.xml
pom_xml = """<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.adaptivetrust.qa</groupId>
    <artifactId>adaptivetrust-appium-e2e</artifactId>
    <version>1.0.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <appium.java.client.version>8.5.1</appium.java.client.version>
        <selenium.version>4.13.0</selenium.version>
        <testng.version>7.8.0</testng.version>
        <poi.version>5.2.3</poi.version>
        <log4j.version>2.20.0</log4j.version>
        <jackson.version>2.15.2</jackson.version>
        <freemarker.version>2.3.32</freemarker.version>
    </properties>

    <dependencies>
        <!-- Appium Java Client -->
        <dependency>
            <groupId>io.appium</groupId>
            <artifactId>java-client</artifactId>
            <version>${appium.java.client.version}</version>
        </dependency>

        <!-- Selenium Java -->
        <dependency>
            <groupId>org.seleniumhq.selenium</groupId>
            <artifactId>selenium-java</artifactId>
            <version>${selenium.version}</version>
        </dependency>

        <!-- TestNG -->
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>${testng.version}</version>
        </dependency>

        <!-- Apache POI for Excel reporting -->
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi</artifactId>
            <version>${poi.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi-ooxml</artifactId>
            <version>${poi.version}</version>
        </dependency>

        <!-- Jackson for JSON parsing -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>${jackson.version}</version>
        </dependency>

        <!-- Freemarker for HTML reports -->
        <dependency>
            <groupId>org.freemarker</groupId>
            <artifactId>freemarker</artifactId>
            <version>${freemarker.version}</version>
        </dependency>

        <!-- Logging -->
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-api</artifactId>
            <version>${log4j.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-core</artifactId>
            <version>${log4j.version}</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.1.2</version>
                <configuration>
                    <suiteXmlFiles>
                        <suiteXmlFile>testng.xml</suiteXmlFile>
                    </suiteXmlFiles>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
"""
with open(os.path.join(BASE_DIR, "pom.xml"), "w", encoding="utf-8") as f:
    f.write(pom_xml)

# 3. appium.config.json
appium_config = {
    "server": {
        "host": "127.0.0.1",
        "port": 4723,
        "basePath": "/"
    },
    "capabilities": {
        "platformName": "Android",
        "automationName": "UiAutomator2",
        "deviceName": "Android Emulator",
        "platformVersion": "13.0",
        "app": "C:/Users/yogan/.gemini/antigravity-ide/scratch/adaptivetrust-multitenant/AdaptiveTrust-Mobile-Release.apk",
        "appPackage": "com.adaptivetrust.mobile",
        "appActivity": "com.adaptivetrust.mobile.ui.MainActivity",
        "noReset": False,
        "fullReset": False,
        "autoGrantPermissions": True,
        "newCommandTimeout": 300
    },
    "timeouts": {
        "implicit": 10,
        "explicit": 20,
        "pageLoad": 30
    },
    "reporting": {
        "screenshotOnFailure": True,
        "generateExcel": True,
        "generateHtml": True,
        "excelReportPath": "reports/Automation_Test_Report.xlsx",
        "htmlReportPath": "reports/execution-report.html"
    }
}
with open(os.path.join(BASE_DIR, "appium.config.json"), "w", encoding="utf-8") as f:
    json.dump(appium_config, f, indent=2)

# 4. testng.xml
testng_xml = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="AdaptiveTrust Mobile E2E Automation Suite" verbose="2">
    <listeners>
        <listener class-name="com.adaptivetrust.qa.listeners.TestListener" />
    </listeners>

    <test name="Authentication Tests">
        <classes>
            <class name="com.adaptivetrust.qa.tests.auth.LoginTests" />
            <class name="com.adaptivetrust.qa.tests.auth.AdminSignUpTests" />
            <class name="com.adaptivetrust.qa.tests.auth.EmployeeSignUpTests" />
            <class name="com.adaptivetrust.qa.tests.auth.OTPVerifyTests" />
        </classes>
    </test>

    <test name="Dashboard and Gateway Tests">
        <classes>
            <class name="com.adaptivetrust.qa.tests.dashboard.AdminDashboardTests" />
            <class name="com.adaptivetrust.qa.tests.dashboard.EmployeeDashboardTests" />
            <class name="com.adaptivetrust.qa.tests.superadmin.SuperAdminTests" />
        </classes>
    </test>

    <test name="Navigation and Forms Tests">
        <classes>
            <class name="com.adaptivetrust.qa.tests.navigation.NavigationTests" />
            <class name="com.adaptivetrust.qa.tests.forms.FormValidationTests" />
            <class name="com.adaptivetrust.qa.tests.crud.CrudOperationTests" />
        </classes>
    </test>

    <test name="Security, Quality and Regression Tests">
        <classes>
            <class name="com.adaptivetrust.qa.tests.security.SessionManagementTests" />
            <class name="com.adaptivetrust.qa.tests.performance.PerformanceSmokeTests" />
            <class name="com.adaptivetrust.qa.tests.accessibility.AccessibilityTests" />
            <class name="com.adaptivetrust.qa.tests.regression.RegressionSuiteTests" />
        </classes>
    </test>
</suite>
"""
with open(os.path.join(BASE_DIR, "testng.xml"), "w", encoding="utf-8") as f:
    f.write(testng_xml)

# 5. testng-parallel.xml
testng_parallel_xml = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="AdaptiveTrust Mobile Parallel E2E Suite" parallel="tests" thread-count="4" verbose="2">
    <listeners>
        <listener class-name="com.adaptivetrust.qa.listeners.TestListener" />
    </listeners>

    <test name="Parallel-Auth">
        <classes>
            <class name="com.adaptivetrust.qa.tests.auth.LoginTests" />
            <class name="com.adaptivetrust.qa.tests.auth.AdminSignUpTests" />
        </classes>
    </test>
    <test name="Parallel-Dashboard">
        <classes>
            <class name="com.adaptivetrust.qa.tests.dashboard.AdminDashboardTests" />
            <class name="com.adaptivetrust.qa.tests.dashboard.EmployeeDashboardTests" />
        </classes>
    </test>
    <test name="Parallel-Forms-CRUD">
        <classes>
            <class name="com.adaptivetrust.qa.tests.forms.FormValidationTests" />
            <class name="com.adaptivetrust.qa.tests.crud.CrudOperationTests" />
        </classes>
    </test>
    <test name="Parallel-Regression">
        <classes>
            <class name="com.adaptivetrust.qa.tests.regression.RegressionSuiteTests" />
        </classes>
    </test>
</suite>
"""
with open(os.path.join(BASE_DIR, "testng-parallel.xml"), "w", encoding="utf-8") as f:
    f.write(testng_parallel_xml)

# 6. log4j2.xml
log4j2_xml = """<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
        <RollingFile name="FileAppender" fileName="logs/appium-test.log"
                     filePattern="logs/appium-test-%d{yyyy-MM-dd}-%i.log">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss} [%t] %-5level %logger{36} - %msg%n"/>
            <Policies>
                <SizeBasedTriggeringPolicy size="10MB"/>
            </Policies>
        </RollingFile>
    </Appenders>
    <Loggers>
        <Root level="INFO">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="FileAppender"/>
        </Root>
    </Loggers>
</Configuration>
"""
with open(os.path.join(BASE_DIR, "resources", "log4j2.xml"), "w", encoding="utf-8") as f:
    f.write(log4j2_xml)

print("Root configuration files generated successfully.")

