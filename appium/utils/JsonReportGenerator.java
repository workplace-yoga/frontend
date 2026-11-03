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
