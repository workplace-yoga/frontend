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
