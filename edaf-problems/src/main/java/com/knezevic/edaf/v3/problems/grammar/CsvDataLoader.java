/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.grammar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lightweight CSV loader for symbolic regression and classification datasets.
 */
public final class CsvDataLoader {

    private CsvDataLoader() {
        // utility class
    }

    /**
     * Loads regression dataset with numeric target column.
     */
    public static RegressionData loadRegression(String path, String targetColumn) {
        ParsedCsv parsed = parseCsv(path);
        int targetIndex = columnIndex(parsed.header(), targetColumn, parsed.pathDescription());
        List<RegressionData.Row> rows = new ArrayList<>();

        for (String[] values : parsed.rows()) {
            if (values.length != parsed.header().length) {
                continue;
            }
            Map<String, Double> features = new LinkedHashMap<>();
            for (int i = 0; i < parsed.header().length; i++) {
                if (i == targetIndex) {
                    continue;
                }
                features.put(parsed.header()[i], parseDouble(values[i], parsed.pathDescription(), parsed.header()[i]));
            }
            double target = parseDouble(values[targetIndex], parsed.pathDescription(), targetColumn);
            rows.add(new RegressionData.Row(features, target));
        }

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Regression dataset has no valid rows: " + parsed.pathDescription());
        }

        return new RegressionData(List.copyOf(rows), inferFeatureOrder(rows));
    }

    /**
     * Loads generic classification dataset and infers class order from first appearance.
     */
    public static ClassificationData loadClassification(String path, String targetColumn) {
        return loadClassification(path, targetColumn, List.of());
    }

    /**
     * Loads generic classification dataset.
     * If {@code classValues} is provided, labels are validated against the provided class set.
     */
    public static ClassificationData loadClassification(String path, String targetColumn, List<String> classValues) {
        ParsedCsv parsed = parseCsv(path);
        int targetIndex = columnIndex(parsed.header(), targetColumn, parsed.pathDescription());
        List<String> normalizedClassValues = normalizeClassValues(classValues);
        Set<String> allowedLabels = new LinkedHashSet<>(normalizedClassValues);
        Set<String> discoveredLabels = new LinkedHashSet<>();

        List<ClassificationData.Row> rows = new ArrayList<>();
        for (String[] values : parsed.rows()) {
            if (values.length != parsed.header().length) {
                continue;
            }
            Map<String, Double> features = new LinkedHashMap<>();
            for (int i = 0; i < parsed.header().length; i++) {
                if (i == targetIndex) {
                    continue;
                }
                features.put(parsed.header()[i], parseDouble(values[i], parsed.pathDescription(), parsed.header()[i]));
            }
            String label = parseLabelRaw(values[targetIndex], parsed.pathDescription(), targetColumn);
            if (!allowedLabels.isEmpty() && !allowedLabels.contains(label)) {
                throw new IllegalArgumentException("Label '" + label + "' from " + parsed.pathDescription()
                        + " is not listed in classValues=" + normalizedClassValues);
            }
            discoveredLabels.add(label);
            rows.add(new ClassificationData.Row(features, label));
        }

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Classification dataset has no valid rows: " + parsed.pathDescription());
        }

        List<String> resolvedClassValues = !normalizedClassValues.isEmpty()
                ? normalizedClassValues
                : List.copyOf(discoveredLabels);
        if (resolvedClassValues.size() < 2) {
            throw new IllegalArgumentException("Classification dataset must contain at least 2 classes: "
                    + parsed.pathDescription() + ", found=" + resolvedClassValues);
        }

        Map<String, Integer> indexByLabel = new LinkedHashMap<>();
        for (int i = 0; i < resolvedClassValues.size(); i++) {
            indexByLabel.put(resolvedClassValues.get(i), i);
        }

        return new ClassificationData(List.copyOf(rows), inferFeatureOrder(rows), resolvedClassValues, Map.copyOf(indexByLabel));
    }

    /**
     * Loads binary classification dataset using positive-vs-rest mapping.
     */
    public static ClassificationData loadClassification(String path, String targetColumn, String positiveLabel) {
        ClassificationData generic = loadClassification(path, targetColumn);
        String positive = normalizePositiveLabel(positiveLabel);

        List<ClassificationData.Row> rows = new ArrayList<>(generic.rows().size());
        for (ClassificationData.Row row : generic.rows()) {
            boolean isPositive = isPositiveLabel(row.label(), positive);
            rows.add(new ClassificationData.Row(row.features(), isPositive ? "1" : "0"));
        }

        Map<String, Integer> indexByLabel = Map.of("0", 0, "1", 1);
        return new ClassificationData(List.copyOf(rows), generic.featureOrder(), List.of("0", "1"), indexByLabel);
    }

    private static List<String> inferFeatureOrder(List<? extends FeatureRow> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        return List.copyOf(rows.getFirst().features().keySet());
    }

    private static ParsedCsv parseCsv(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("dataset path must not be blank");
        }

        List<String> lines = readLines(path);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Dataset is empty: " + path);
        }

        String[] header = splitCsvLine(lines.getFirst());
        if (header.length < 2) {
            throw new IllegalArgumentException("Dataset must have at least two columns: " + path);
        }
        for (int i = 0; i < header.length; i++) {
            header[i] = header[i].trim();
        }

        List<String[]> rows = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || line.isBlank()) {
                continue;
            }
            rows.add(splitCsvLine(line));
        }

        return new ParsedCsv(header, rows, path);
    }

    private static int columnIndex(String[] header, String targetColumn, String source) {
        if (targetColumn == null || targetColumn.isBlank()) {
            throw new IllegalArgumentException("targetColumn is required for dataset " + source);
        }
        String normalized = targetColumn.trim();
        for (int i = 0; i < header.length; i++) {
            if (header[i].equals(normalized)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Target column '" + targetColumn + "' not found in dataset " + source);
    }

    private static double parseDouble(String value, String source, String column) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed parsing numeric value in " + source
                    + " column '" + column + "': '" + value + "'");
        }
    }

    private static String parseLabelRaw(String value, String source, String column) {
        String normalized = value.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Empty label in " + source + " column '" + column + "'");
        }
        return normalized;
    }

    private static String normalizePositiveLabel(String positiveLabel) {
        if (positiveLabel == null || positiveLabel.isBlank()) {
            return "1";
        }
        return positiveLabel.trim();
    }

    private static boolean isPositiveLabel(String value, String positiveLabel) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.equals(positiveLabel)) {
            return true;
        }
        return "1".equals(normalized) || "true".equalsIgnoreCase(normalized) || "yes".equalsIgnoreCase(normalized);
    }

    private static List<String> normalizeClassValues(List<String> classValues) {
        if (classValues == null || classValues.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String value : classValues) {
            if (value == null || value.isBlank()) {
                continue;
            }
            normalized.add(value.trim());
        }
        return List.copyOf(normalized);
    }

    private static List<String> readLines(String path) {
        Path file = Path.of(path);
        if (Files.exists(file)) {
            try {
                return Files.readAllLines(file, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed reading dataset file: " + path, e);
            }
        }

        String classpath = path;
        if (classpath.startsWith("classpath:")) {
            classpath = classpath.substring("classpath:".length());
        }
        if (classpath.startsWith("/")) {
            classpath = classpath.substring(1);
        }

        try (InputStream stream = CsvDataLoader.class.getClassLoader().getResourceAsStream(classpath)) {
            if (stream == null) {
                throw new IllegalArgumentException("Dataset not found: " + path);
            }
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
            return lines;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed reading dataset resource: " + path, e);
        }
    }

    private static String[] splitCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (ch == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(ch);
        }
        values.add(current.toString());
        return values.toArray(String[]::new);
    }

    private record ParsedCsv(String[] header, List<String[]> rows, String pathDescription) {
    }

    private interface FeatureRow {
        Map<String, Double> features();
    }

    /**
     * Regression dataset rows and feature order.
     */
    public record RegressionData(List<Row> rows, List<String> featureOrder) {

        /**
         * One regression row.
         */
        public record Row(Map<String, Double> features, double target) implements FeatureRow {
        }
    }

    /**
     * Classification dataset rows and feature order.
     */
    public record ClassificationData(List<Row> rows,
                                     List<String> featureOrder,
                                     List<String> classValues,
                                     Map<String, Integer> classIndexByLabel) {

        /**
         * Number of distinct classes.
         */
        public int classCount() {
            return classValues.size();
        }

        /**
         * Resolves class label into a dense class index.
         */
        public int classIndex(String label) {
            Integer index = classIndexByLabel.get(label);
            if (index == null) {
                throw new IllegalArgumentException("Unknown class label: '" + label + "', supported=" + classValues);
            }
            return index;
        }

        /**
         * One classification row.
         */
        public record Row(Map<String, Double> features, String label) implements FeatureRow {
        }
    }
}
