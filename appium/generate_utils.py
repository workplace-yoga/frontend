import os

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

def write(rel_path, content):
    full_path = os.path.join(BASE_DIR, rel_path)
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    with open(full_path, "w", encoding="utf-8") as f:
        f.write(content.strip() + "\n")

# ==========================================================
# 1. UTILS
# ==========================================================
write("utils/ScreenshotUtil.java", """
package com.adaptivetrust.qa.utils;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenshotUtil {
    public static String captureScreenshot(AndroidDriver driver, String testName) {
        try {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = testName + "_" + timestamp + ".png";
            String destDir = "screenshots/";
            Files.createDirectories(Paths.get(destDir));
            String destPath = destDir + fileName;
            Files.copy(srcFile.toPath(), Paths.get(destPath));
            return destPath;
        } catch (Exception e) {
            System.err.println("Failed to capture screenshot: " + e.getMessage());
            return null;
        }
    }
}
""")

write("utils/LogUtil.java", """
package com.adaptivetrust.qa.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogUtil {
    private static final Logger logger = LogManager.getLogger(LogUtil.class);

    public static void info(String message) { logger.info(message); }
    public static void warn(String message) { logger.warn(message); }
    public static void error(String message) { logger.error(message); }
    public static void debug(String message) { logger.debug(message); }
}
""")

write("utils/WaitUtil.java", """
package com.adaptivetrust.qa.utils;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WaitUtil {
    public static WebElement waitForVisibility(AndroidDriver driver, By locator, int timeoutSec) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSec))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForClickable(AndroidDriver driver, By locator, int timeoutSec) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSec))
                .until(ExpectedConditions.elementToBeClickable(locator));
    }
}
""")

write("utils/ExcelReportGenerator.java", """
package com.adaptivetrust.qa.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

public class ExcelReportGenerator {
    public static void generateReport(String filePath, List<Map<String, String>> results) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Sheet 1: Executed Test Cases
            Sheet sheet1 = workbook.createSheet("Executed Test Cases");
            createTestSheet(sheet1, results, null);

            // Sheet 2: Passed Tests
            Sheet sheet2 = workbook.createSheet("Passed Tests");
            createTestSheet(sheet2, results, "PASSED");

            // Sheet 3: Failed Tests
            Sheet sheet3 = workbook.createSheet("Failed Tests");
            createTestSheet(sheet3, results, "FAILED");

            // Sheet 4: Skipped Tests
            Sheet sheet4 = workbook.createSheet("Skipped Tests");
            createTestSheet(sheet4, results, "SKIPPED");

            // Sheet 5: Execution Metrics
            Sheet sheet5 = workbook.createSheet("Execution Metrics");
            createMetricsSheet(sheet5, results);

            // Sheet 6: Defect Summary
            Sheet sheet6 = workbook.createSheet("Defect Summary");
            createDefectSheet(sheet6, results);

            // Sheet 7: Pass Rate Summary
            Sheet sheet7 = workbook.createSheet("Pass Rate Summary");
            createPassRateSheet(sheet7, results);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        } catch (Exception e) {
            System.err.println("Error generating Excel report: " + e.getMessage());
        }
    }

    private static void createTestSheet(Sheet sheet, List<Map<String, String>> results, String filterStatus) {
        Row header = sheet.createRow(0);
        String[] columns = {"Test ID", "Module", "Test Name", "Priority", "Status", "Execution Time (ms)", "Error Reason"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
        }
        int rowIdx = 1;
        for (Map<String, String> r : results) {
            if (filterStatus == null || filterStatus.equalsIgnoreCase(r.get("status"))) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getOrDefault("id", ""));
                row.createCell(1).setCellValue(r.getOrDefault("module", ""));
                row.createCell(2).setCellValue(r.getOrDefault("name", ""));
                row.createCell(3).setCellValue(r.getOrDefault("priority", "Medium"));
                row.createCell(4).setCellValue(r.getOrDefault("status", "PASSED"));
                row.createCell(5).setCellValue(r.getOrDefault("time", "0"));
                row.createCell(6).setCellValue(r.getOrDefault("error", ""));
            }
        }
    }

    private static void createMetricsSheet(Sheet sheet, List<Map<String, String>> results) {
        int total = results.size();
        long passed = results.stream().filter(r -> "PASSED".equalsIgnoreCase(r.get("status"))).count();
        long failed = results.stream().filter(r -> "FAILED".equalsIgnoreCase(r.get("status"))).count();
        long skipped = total - passed - failed;

        Row r0 = sheet.createRow(0);
        r0.createCell(0).setCellValue("Metric");
        r0.createCell(1).setCellValue("Value");

        String[][] data = {
            {"Total Tests", String.valueOf(total)},
            {"Passed", String.valueOf(passed)},
            {"Failed", String.valueOf(failed)},
            {"Skipped", String.valueOf(skipped)},
            {"Pass Percentage", String.format("%.2f%%", total > 0 ? (passed * 100.0 / total) : 0)}
        };
        for (int i = 0; i < data.length; i++) {
            Row r = sheet.createRow(i + 1);
            r.createCell(0).setCellValue(data[i][0]);
            r.createCell(1).setCellValue(data[i][1]);
        }
    }

    private static void createDefectSheet(Sheet sheet, List<Map<String, String>> results) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Defect ID");
        header.createCell(1).setCellValue("Associated Test");
        header.createCell(2).setCellValue("Severity");
        header.createCell(3).setCellValue("Failure Description");

        int idx = 1;
        for (Map<String, String> r : results) {
            if ("FAILED".equalsIgnoreCase(r.get("status"))) {
                Row row = sheet.createRow(idx);
                row.createCell(0).setCellValue("DEF-" + String.format("%03d", idx));
                row.createCell(1).setCellValue(r.getOrDefault("id", ""));
                row.createCell(2).setCellValue(r.getOrDefault("priority", "High"));
                row.createCell(3).setCellValue(r.getOrDefault("error", "Assertion Failure"));
                idx++;
            }
        }
    }

    private static void createPassRateSheet(Sheet sheet, List<Map<String, String>> results) {
        Row h = sheet.createRow(0);
        h.createCell(0).setCellValue("Module");
        h.createCell(1).setCellValue("Total");
        h.createCell(2).setCellValue("Passed");
        h.createCell(3).setCellValue("Pass Rate");
        // Module summary rows can be populated here
    }
}
""")

write("utils/HTMLReportGenerator.java", """
package com.adaptivetrust.qa.utils;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class HTMLReportGenerator {
    public static void generateReport(String outputPath, List<Map<String, String>> results) {
        int total = results.size();
        long passed = results.stream().filter(r -> "PASSED".equalsIgnoreCase(r.get("status"))).count();
        long failed = results.stream().filter(r -> "FAILED".equalsIgnoreCase(r.get("status"))).count();
        long skipped = total - passed - failed;
        double passPct = total > 0 ? (passed * 100.0 / total) : 0;

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>AdaptiveTrust Appium E2E Report</title>");
        sb.append("<style>");
        sb.append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #0f172a; color: #f8fafc; margin: 0; padding: 20px; }");
        sb.append(".container { max-width: 1200px; margin: auto; }");
        sb.append(".header { background: #1e293b; padding: 20px; border-radius: 12px; margin-bottom: 20px; border: 1px solid #334155; }");
        sb.append(".grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }");
        sb.append(".card { background: #1e293b; padding: 18px; border-radius: 10px; border: 1px solid #334155; text-align: center; }");
        sb.append(".card h3 { margin: 0; font-size: 2rem; }");
        sb.append(".card p { margin: 4px 0 0; color: #94a3b8; }");
        sb.append(".passed { color: #10b981; } .failed { color: #ef4444; } .skipped { color: #f59e0b; } .total { color: #38bdf8; }");
        sb.append("table { width: 100%; border-collapse: collapse; background: #1e293b; border-radius: 10px; overflow: hidden; }");
        sb.append("th, td { padding: 12px 14px; text-align: left; border-bottom: 1px solid #334155; font-size: 0.9rem; }");
        sb.append("th { background: #0f172a; color: #94a3b8; }");
        sb.append(".badge { padding: 4px 8px; border-radius: 6px; font-weight: bold; font-size: 0.75rem; }");
        sb.append(".badge-pass { background: rgba(16, 185, 129, 0.2); color: #10b981; }");
        sb.append(".badge-fail { background: rgba(239, 68, 68, 0.2); color: #ef4444; }");
        sb.append(".badge-skip { background: rgba(245, 158, 11, 0.2); color: #f59e0b; }");
        sb.append("</style></head><body><div class='container'>");

        sb.append("<div class='header'>");
        sb.append("<h1>AdaptiveTrust Mobile Automation Report</h1>");
        sb.append("<p>Execution Date: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("</p>");
        sb.append("<p>Target APK: AdaptiveTrust-Mobile-Release.apk | Platform: Android 13</p>");
        sb.append("</div>");

        sb.append("<div class='grid'>");
        sb.append("<div class='card'><h3 class='total'>").append(total).append("</h3><p>Total Tests</p></div>");
        sb.append("<div class='card'><h3 class='passed'>").append(passed).append("</h3><p>Passed (").append(String.format("%.1f", passPct)).append("%)</p></div>");
        sb.append("<div class='card'><h3 class='failed'>").append(failed).append("</h3><p>Failed</p></div>");
        sb.append("<div class='card'><h3 class='skipped'>").append(skipped).append("</h3><p>Skipped</p></div>");
        sb.append("</div>");

        sb.append("<table><thead><tr><th>Test ID</th><th>Module</th><th>Test Name</th><th>Priority</th><th>Status</th><th>Duration</th></tr></thead><tbody>");
        for (Map<String, String> r : results) {
            String st = r.getOrDefault("status", "PASSED");
            String badgeClass = "badge-pass";
            if ("FAILED".equalsIgnoreCase(st)) badgeClass = "badge-fail";
            else if ("SKIPPED".equalsIgnoreCase(st)) badgeClass = "badge-skip";

            sb.append("<tr>");
            sb.append("<td><strong>").append(r.getOrDefault("id", "")).append("</strong></td>");
            sb.append("<td>").append(r.getOrDefault("module", "")).append("</td>");
            sb.append("<td>").append(r.getOrDefault("name", "")).append("</td>");
            sb.append("<td>").append(r.getOrDefault("priority", "Medium")).append("</td>");
            sb.append("<td><span class='badge ").append(badgeClass).append("'>").append(st).append("</span></td>");
            sb.append("<td>").append(r.getOrDefault("time", "0")).append(" ms</td>");
            sb.append("</tr>");
        }
        sb.append("</tbody></table>");
        sb.append("</div></body></html>");

        try (FileWriter fw = new FileWriter(outputPath)) {
            fw.write(sb.toString());
        } catch (Exception e) {
            System.err.println("Failed to write HTML report: " + e.getMessage());
        }
    }
}
""")

write("utils/JsonReportGenerator.java", """
package com.adaptivetrust.qa.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonReportGenerator {
    public static void generateReport(String outputPath, List<Map<String, String>> results) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> root = new HashMap<>();
            root.put("project", "AdaptiveTrust Mobile E2E");
            root.put("timestamp", System.currentTimeMillis());
            root.put("total", results.size());
            root.put("results", results);
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputPath), root);
        } catch (Exception e) {
            System.err.println("Failed to generate JSON report: " + e.getMessage());
        }
    }
}
""")

# ==========================================================
# 2. DATA
# ==========================================================
write("data/TestData.java", """
package com.adaptivetrust.qa.data;

public class TestData {
    public static final String VALID_ADMIN_EMAIL = "admin@adaptivetrust.io";
    public static final String VALID_ADMIN_PASSWORD = "Password@123";
    public static final String VALID_EMP_EMAIL = "employee@adaptivetrust.io";
    public static final String VALID_EMP_PASSWORD = "Password@123";
    public static final String VALID_INVITE_CODE = "ADAPT888";
    public static final String INVALID_EMAIL = "not-an-email";
    public static final String SHORT_PASSWORD = "123";
    public static final String SQL_INJECTION = "' OR '1'='1";
    public static final String XSS_PAYLOAD = "<script>alert(1)</script>";
    public static final String LONG_STRING_500 = "A".repeat(500);
}
""")

write("data/DataProviderClass.java", """
package com.adaptivetrust.qa.data;

import org.testng.annotations.DataProvider;

public class DataProviderClass {
    @DataProvider(name = "invalidLoginData")
    public static Object[][] getInvalidLoginData() {
        return new Object[][] {
            {"", "Password@123", "Email required"},
            {"admin@adaptivetrust.io", "", "Password required"},
            {"invalid-email", "Password@123", "Invalid email format"},
            {"notfound@adaptivetrust.io", "WrongPass123", "Invalid credentials"}
        };
    }
}
""")

# ==========================================================
# 3. LISTENERS & RETRY
# ==========================================================
write("listeners/RetryAnalyzer.java", """
package com.adaptivetrust.qa.listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {
    private int retryCount = 0;
    private static final int maxRetryCount = 2;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < maxRetryCount) {
            retryCount++;
            return true;
        }
        return false;
    }
}
""")

write("listeners/TestListener.java", """
package com.adaptivetrust.qa.listeners;

import com.adaptivetrust.qa.drivers.DriverManager;
import com.adaptivetrust.qa.utils.ExcelReportGenerator;
import com.adaptivetrust.qa.utils.HTMLReportGenerator;
import com.adaptivetrust.qa.utils.JsonReportGenerator;
import com.adaptivetrust.qa.utils.ScreenshotUtil;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.util.*;

public class TestListener implements ITestListener {
    private static final List<Map<String, String>> testResults = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void onTestSuccess(ITestResult result) {
        recordResult(result, "PASSED", null);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String screenshot = null;
        if (DriverManager.getDriver() != null) {
            screenshot = ScreenshotUtil.captureScreenshot(DriverManager.getDriver(), result.getName());
        }
        String errorMsg = result.getThrowable() != null ? result.getThrowable().getMessage() : "Test Failed";
        recordResult(result, "FAILED", errorMsg);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        recordResult(result, "SKIPPED", result.getThrowable() != null ? result.getThrowable().getMessage() : "Skipped");
    }

    private void recordResult(ITestResult result, String status, String error) {
        Map<String, String> r = new HashMap<>();
        r.put("id", result.getName());
        r.put("module", result.getTestClass().getRealClass().getSimpleName());
        r.put("name", result.getMethod().getDescription() != null ? result.getMethod().getDescription() : result.getName());
        r.put("priority", "Medium");
        r.put("status", status);
        r.put("time", String.valueOf(result.getEndMillis() - result.getStartMillis()));
        r.put("error", error != null ? error : "");
        testResults.add(r);
    }

    @Override
    public void onFinish(ITestContext context) {
        new File("reports").mkdirs();
        ExcelReportGenerator.generateReport("reports/Automation_Test_Report.xlsx", testResults);
        HTMLReportGenerator.generateReport("reports/execution-report.html", testResults);
        JsonReportGenerator.generateReport("reports/execution-results.json", testResults);
    }
}
""")

# ==========================================================
# 4. RUNNER
# ==========================================================
write("runners/TestRunner.java", """
package com.adaptivetrust.qa.runners;

import org.testng.TestNG;
import java.util.Collections;

public class TestRunner {
    public static void main(String[] args) {
        TestNG testng = new TestNG();
        testng.setTestSuites(Collections.singletonList("testng.xml"));
        testng.run();
    }
}
""")

# ==========================================================
# 5. BASE TEST
# ==========================================================
write("tests/BaseTest.java", """
package com.adaptivetrust.qa.tests;

import com.adaptivetrust.qa.drivers.AppiumDriverFactory;
import com.adaptivetrust.qa.drivers.DriverManager;
import io.appium.java_client.android.AndroidDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class BaseTest {
    protected AndroidDriver driver;

    @BeforeMethod
    public void setUp() {
        try {
            driver = AppiumDriverFactory.createDriver();
            DriverManager.setDriver(driver);
        } catch (Exception e) {
            System.out.println("Appium driver init skipped in local environment: " + e.getMessage());
        }
    }

    @AfterMethod
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
""")

print("Utils, Data, Listeners, BaseTest written successfully.")

