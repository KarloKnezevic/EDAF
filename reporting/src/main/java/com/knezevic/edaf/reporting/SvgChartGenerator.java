package com.knezevic.edaf.reporting;

import com.knezevic.edaf.persistence.api.GenerationRecord;

import java.util.List;
import java.util.Locale;

/**
 * Generates an inline SVG line chart from generation fitness data.
 */
final class SvgChartGenerator {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;
    private static final int MARGIN_LEFT = 80;
    private static final int MARGIN_RIGHT = 20;
    private static final int MARGIN_TOP = 20;
    private static final int MARGIN_BOTTOM = 50;

    private SvgChartGenerator() {}

    static String generateConvergenceChart(List<GenerationRecord> generations) {
        if (generations.isEmpty()) return "";

        int plotW = WIDTH - MARGIN_LEFT - MARGIN_RIGHT;
        int plotH = HEIGHT - MARGIN_TOP - MARGIN_BOTTOM;

        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        int maxGen = 0;
        for (GenerationRecord g : generations) {
            if (!Double.isNaN(g.bestFitness())) {
                minY = Math.min(minY, g.bestFitness());
                maxY = Math.max(maxY, g.bestFitness());
            }
            if (!Double.isNaN(g.avgFitness())) {
                minY = Math.min(minY, g.avgFitness());
                maxY = Math.max(maxY, g.avgFitness());
            }
            maxGen = Math.max(maxGen, g.generation());
        }

        if (maxGen == 0 || minY == Double.MAX_VALUE) return "";

        // Add 10% padding to Y range
        double yRange = maxY - minY;
        if (yRange < 1e-10) yRange = 1.0;
        minY -= yRange * 0.05;
        maxY += yRange * 0.05;
        yRange = maxY - minY;

        StringBuilder svg = new StringBuilder();
        svg.append(String.format("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 %d %d\" ", WIDTH, HEIGHT));
        svg.append("style=\"max-width:800px;width:100%;background:#fafafa;border:1px solid #ddd;border-radius:4px\">\n");

        // Grid lines
        svg.append("<g stroke=\"#e0e0e0\" stroke-width=\"0.5\">\n");
        for (int i = 0; i <= 5; i++) {
            int y = MARGIN_TOP + (int)(plotH * i / 5.0);
            svg.append(String.format("  <line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\"/>\n",
                MARGIN_LEFT, y, MARGIN_LEFT + plotW, y));
        }
        svg.append("</g>\n");

        // Y-axis labels
        svg.append("<g fill=\"#666\" font-size=\"11\" text-anchor=\"end\">\n");
        for (int i = 0; i <= 5; i++) {
            int y = MARGIN_TOP + (int)(plotH * i / 5.0);
            double val = maxY - (yRange * i / 5.0);
            svg.append(String.format(Locale.US, "  <text x=\"%d\" y=\"%d\">%.4g</text>\n",
                MARGIN_LEFT - 8, y + 4, val));
        }
        svg.append("</g>\n");

        // X-axis labels
        svg.append("<g fill=\"#666\" font-size=\"11\" text-anchor=\"middle\">\n");
        for (int i = 0; i <= 5; i++) {
            int x = MARGIN_LEFT + (int)(plotW * i / 5.0);
            int gen = maxGen * i / 5;
            svg.append(String.format("  <text x=\"%d\" y=\"%d\">%d</text>\n",
                x, HEIGHT - MARGIN_BOTTOM + 20, gen));
        }
        svg.append("</g>\n");

        // Axis labels
        svg.append(String.format("<text x=\"%d\" y=\"%d\" text-anchor=\"middle\" fill=\"#333\" font-size=\"13\">Generation</text>\n",
            MARGIN_LEFT + plotW / 2, HEIGHT - 5));
        svg.append(String.format("<text x=\"15\" y=\"%d\" text-anchor=\"middle\" fill=\"#333\" font-size=\"13\" " +
            "transform=\"rotate(-90,15,%d)\">Fitness</text>\n",
            MARGIN_TOP + plotH / 2, MARGIN_TOP + plotH / 2));

        // Best fitness line
        svg.append(buildPolyline(generations, plotW, plotH, maxGen, minY, yRange, true, "#2196F3"));
        // Average fitness line
        svg.append(buildPolyline(generations, plotW, plotH, maxGen, minY, yRange, false, "#FF9800"));

        // Legend
        svg.append(String.format("<rect x=\"%d\" y=\"%d\" width=\"12\" height=\"3\" fill=\"#2196F3\"/>\n",
            MARGIN_LEFT + 10, MARGIN_TOP + 10));
        svg.append(String.format("<text x=\"%d\" y=\"%d\" fill=\"#333\" font-size=\"11\">Best</text>\n",
            MARGIN_LEFT + 26, MARGIN_TOP + 14));
        svg.append(String.format("<rect x=\"%d\" y=\"%d\" width=\"12\" height=\"3\" fill=\"#FF9800\"/>\n",
            MARGIN_LEFT + 70, MARGIN_TOP + 10));
        svg.append(String.format("<text x=\"%d\" y=\"%d\" fill=\"#333\" font-size=\"11\">Average</text>\n",
            MARGIN_LEFT + 86, MARGIN_TOP + 14));

        svg.append("</svg>");
        return svg.toString();
    }

    private static String buildPolyline(List<GenerationRecord> generations,
                                         int plotW, int plotH, int maxGen,
                                         double minY, double yRange,
                                         boolean useBest, String color) {
        StringBuilder points = new StringBuilder();
        for (GenerationRecord g : generations) {
            double val = useBest ? g.bestFitness() : g.avgFitness();
            if (Double.isNaN(val)) continue;
            int x = MARGIN_LEFT + (int)((double)g.generation() / maxGen * plotW);
            int y = MARGIN_TOP + (int)((maxY(minY, yRange) - val) / yRange * plotH);
            if (!points.isEmpty()) points.append(" ");
            points.append(x).append(",").append(y);
        }
        return String.format("<polyline points=\"%s\" fill=\"none\" stroke=\"%s\" stroke-width=\"2\"/>\n",
            points, color);
    }

    private static double maxY(double minY, double yRange) {
        return minY + yRange;
    }
}
