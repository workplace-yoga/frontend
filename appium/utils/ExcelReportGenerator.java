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
