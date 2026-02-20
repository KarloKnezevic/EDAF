package com.knezevic.edaf.v3.problems.permutation.tsplib;

import java.util.ArrayList;
import java.util.List;

/**
 * Parsed TSPLIB NODE_COORD_SECTION instance.
 */
public record TsplibInstance(String name, double[][] coordinates) {

    /**
     * Parses TSPLIB text containing NODE_COORD_SECTION and EOF.
     */
    public static TsplibInstance parse(String text) {
        String name = "tsplib";
        boolean inCoords = false;
        List<double[]> coords = new ArrayList<>();

        for (String raw : text.lines().toList()) {
            String line = raw.trim();
            if (line.isEmpty()) {
                continue;
            }
            String upper = line.toUpperCase();
            if (upper.startsWith("NAME")) {
                String[] parts = line.split(":", 2);
                name = parts.length == 2 ? parts[1].trim() : line;
                continue;
            }
            if (upper.startsWith("NODE_COORD_SECTION")) {
                inCoords = true;
                continue;
            }
            if (upper.startsWith("EOF")) {
                break;
            }
            if (!inCoords) {
                continue;
            }

            String[] parts = line.split("\\s+");
            if (parts.length < 3) {
                continue;
            }
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            coords.add(new double[]{x, y});
        }

        if (coords.isEmpty()) {
            throw new IllegalArgumentException("TSPLIB instance has no NODE_COORD_SECTION entries");
        }
        return new TsplibInstance(name, coords.toArray(double[][]::new));
    }
}
