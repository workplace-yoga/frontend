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
