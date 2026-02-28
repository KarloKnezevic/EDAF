/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.discrete.maxsat;

import java.util.ArrayList;
import java.util.List;

/**
 * Parsed DIMACS CNF formula.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record DimacsCnf(int variableCount, int[][] clauses) {

    /**
     * Parses DIMACS text format.
     * @param text the text argument
     * @return the parse
     */
    public static DimacsCnf parse(String text) {
        int variableCount = -1;
        List<int[]> clauses = new ArrayList<>();

        for (String raw : text.lines().toList()) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("c")) {
                continue;
            }
            if (line.startsWith("p")) {
                String[] parts = line.split("\\s+");
                if (parts.length < 4 || !"cnf".equalsIgnoreCase(parts[1])) {
                    throw new IllegalArgumentException("Invalid DIMACS header: " + line);
                }
                variableCount = Integer.parseInt(parts[2]);
                continue;
            }

            String[] parts = line.split("\\s+");
            List<Integer> literals = new ArrayList<>();
            for (String part : parts) {
                if (part.isBlank()) {
                    continue;
                }
                int literal = Integer.parseInt(part);
                if (literal == 0) {
                    break;
                }
                literals.add(literal);
            }
            if (!literals.isEmpty()) {
                int[] clause = new int[literals.size()];
                for (int i = 0; i < literals.size(); i++) {
                    clause[i] = literals.get(i);
                }
                clauses.add(clause);
            }
        }

        if (variableCount <= 0) {
            throw new IllegalArgumentException("DIMACS header with variable count is required");
        }
        return new DimacsCnf(variableCount, clauses.toArray(int[][]::new));
    }
}
