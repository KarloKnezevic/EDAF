package com.knezevic.edaf.examples;

import com.knezevic.edaf.core.runtime.PopulationStatistics.Statistics;

/**
 * Formats population statistics as a beautiful ASCII table with colors.
 */
public class StatisticsTableFormatter {
    
    // ANSI color codes
    private static final String RESET = "\033[0m";
    private static final String BOLD = "\033[1m";
    private static final String CYAN = "\033[36m";
    private static final String GREEN = "\033[32m";
    private static final String YELLOW = "\033[33m";
    private static final String RED = "\033[31m";
    private static final String BLUE = "\033[34m";
    private static final String MAGENTA = "\033[35m";
    
    // Check if ANSI colors are supported (terminal should support colors)
    private static final boolean USE_COLORS = System.console() != null 
            || (System.getenv("TERM") != null && !System.getenv("TERM").equals("dumb"))
            || System.getProperty("java.class.path").contains("intellij");
    
    /**
     * Formats statistics as a beautiful table.
     */
    public static String format(int generation, Statistics stats) {
        if (stats == null || Double.isNaN(stats.best())) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        
        // Header
        sb.append("\n");
        sb.append(color(BOLD + CYAN, "╔══════════════════════════════════════════════════════════════════╗"));
        sb.append("\n");
        sb.append(color(BOLD + CYAN, "║"));
        sb.append("  ").append(color(BOLD, "Generation Statistics")).append("  ");
        sb.append(String.format("%43s", "")).append(color(BOLD + CYAN, "║"));
        sb.append("\n");
        sb.append(color(BOLD + CYAN, "╠══════════════════════════════════════════════════════════════════╣"));
        sb.append("\n");
        
        // Generation number
        sb.append(color(BOLD + CYAN, "║"));
        sb.append("  ").append(String.format("%-24s", color(BOLD, "Generation:")));
        sb.append("  ").append(String.format("%-33s", color(GREEN, String.format("%,d", generation))));
        sb.append(color(BOLD + CYAN, "  ║"));
        sb.append("\n");
        sb.append(color(BOLD + CYAN, "╠══════════════════════════════════════════════════════════════════╣"));
        sb.append("\n");
        
        // Statistics rows
        addStatRow(sb, "Best Fitness", stats.best(), GREEN);
        addStatRow(sb, "Worst Fitness", stats.worst(), RED);
        addStatRow(sb, "Average (μ)", stats.avg(), BLUE);
        addStatRow(sb, "Std Dev (σ)", stats.std(), YELLOW);
        addStatRow(sb, "Median", stats.median(), MAGENTA);
        
        // Footer
        sb.append(color(BOLD + CYAN, "╚══════════════════════════════════════════════════════════════════╝"));
        sb.append("\n");
        
        return sb.toString();
    }
    
    private static void addStatRow(StringBuilder sb, String label, double value, String colorCode) {
        String formattedValue;
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            formattedValue = color(RED, "N/A");
        } else {
            formattedValue = color(colorCode, String.format("%.6f", value));
        }
        
        sb.append(color(BOLD + CYAN, "║"));
        sb.append("  ").append(String.format("%-24s", color(BOLD, label)));
        sb.append("  ").append(String.format("%-33s", formattedValue));
        sb.append(color(BOLD + CYAN, "  ║"));
        sb.append("\n");
    }
    
    private static String color(String code, String text) {
        if (USE_COLORS) {
            return code + text + RESET;
        }
        return text;
    }
    
    /**
     * Formats statistics as a compact single-line format for progress bar.
     */
    public static String formatCompact(int generation, Statistics stats) {
        if (stats == null || Double.isNaN(stats.best())) {
            return String.format("Gen %d", generation);
        }
        
        return String.format("Gen %d | Best: %.4f | Worst: %.4f | Avg: %.4f | Std: %.4f | Median: %.4f",
                generation, stats.best(), stats.worst(), stats.avg(), stats.std(), stats.median());
    }
}

