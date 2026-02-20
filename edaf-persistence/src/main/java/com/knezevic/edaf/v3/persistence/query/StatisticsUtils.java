package com.knezevic.edaf.v3.persistence.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Numerical helpers for descriptive statistics and nonparametric significance tests.
 */
final class StatisticsUtils {

    private static final double[] LANCZOS = {
            676.5203681218851,
            -1259.1392167224028,
            771.3234287776531,
            -176.6150291621406,
            12.507343278686905,
            -0.13857109526572012,
            9.9843695780195716e-6,
            1.5056327351493116e-7
    };

    private StatisticsUtils() {
    }

    static Double mean(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.size();
    }

    static Double stdDev(List<Double> values) {
        if (values == null || values.size() < 2) {
            return null;
        }
        double mean = mean(values);
        double sumSq = 0.0;
        for (double value : values) {
            double d = value - mean;
            sumSq += d * d;
        }
        return Math.sqrt(sumSq / (values.size() - 1));
    }

    static Double quantile(List<Double> values, double p) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        double[] sorted = values.stream().mapToDouble(Double::doubleValue).sorted().toArray();
        if (sorted.length == 1) {
            return sorted[0];
        }
        double clamped = Math.max(0.0, Math.min(1.0, p));
        double index = clamped * (sorted.length - 1);
        int low = (int) Math.floor(index);
        int high = (int) Math.ceil(index);
        if (low == high) {
            return sorted[low];
        }
        double t = index - low;
        return sorted[low] * (1.0 - t) + sorted[high] * t;
    }

    static BoxPlotStats boxPlot(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return new BoxPlotStats(null, null, null, null, null, null, null);
        }
        List<Double> sorted = values.stream().sorted().toList();
        return new BoxPlotStats(
                sorted.getFirst(),
                quantile(sorted, 0.25),
                quantile(sorted, 0.50),
                quantile(sorted, 0.75),
                sorted.getLast(),
                mean(sorted),
                stdDev(sorted)
        );
    }

    static double wilcoxonRankSumPValue(List<Double> x, List<Double> y) {
        if (x == null || y == null || x.isEmpty() || y.isEmpty()) {
            return 1.0;
        }

        List<RankValue> all = new ArrayList<>(x.size() + y.size());
        for (double v : x) {
            all.add(new RankValue(v, 0));
        }
        for (double v : y) {
            all.add(new RankValue(v, 1));
        }
        all.sort(Comparator.comparingDouble(RankValue::value));

        double[] ranks = new double[all.size()];
        int i = 0;
        while (i < all.size()) {
            int j = i + 1;
            while (j < all.size() && Double.compare(all.get(j).value(), all.get(i).value()) == 0) {
                j++;
            }
            double avgRank = (i + j + 1) / 2.0;
            for (int k = i; k < j; k++) {
                ranks[k] = avgRank;
            }
            i = j;
        }

        double rankSumX = 0.0;
        for (int idx = 0; idx < all.size(); idx++) {
            if (all.get(idx).group() == 0) {
                rankSumX += ranks[idx];
            }
        }

        int n1 = x.size();
        int n2 = y.size();
        double u1 = rankSumX - (double) n1 * (n1 + 1) / 2.0;
        double meanU = (double) n1 * n2 / 2.0;

        double tieCorrection = 0.0;
        i = 0;
        while (i < all.size()) {
            int j = i + 1;
            while (j < all.size() && Double.compare(all.get(j).value(), all.get(i).value()) == 0) {
                j++;
            }
            int t = j - i;
            if (t > 1) {
                tieCorrection += (double) t * t * t - t;
            }
            i = j;
        }

        int n = n1 + n2;
        double variance = (double) n1 * n2 / 12.0
                * ((n + 1.0) - tieCorrection / ((double) n * (n - 1.0)));
        if (variance <= 0.0) {
            return 1.0;
        }

        double continuity = Math.signum(u1 - meanU) * 0.5;
        double z = (u1 - meanU - continuity) / Math.sqrt(variance);
        double p = 2.0 * (1.0 - normalCdf(Math.abs(z)));
        if (Double.isNaN(p)) {
            return 1.0;
        }
        return Math.max(0.0, Math.min(1.0, p));
    }

    static FriedmanComputation friedman(double[][] matrix, List<String> algorithmOrder, boolean minimize) {
        int n = matrix.length;
        int k = algorithmOrder.size();
        if (n < 2 || k < 2) {
            return new FriedmanComputation(null, null, List.of());
        }

        double[] rankSums = new double[k];
        for (double[] row : matrix) {
            double[] normalized = Arrays.copyOf(row, row.length);
            if (!minimize) {
                for (int i = 0; i < normalized.length; i++) {
                    normalized[i] = -normalized[i];
                }
            }
            double[] rowRanks = averageRanks(normalized);
            for (int j = 0; j < k; j++) {
                rankSums[j] += rowRanks[j];
            }
        }

        double sumSquares = 0.0;
        for (double r : rankSums) {
            sumSquares += r * r;
        }
        double statistic = (12.0 / (n * k * (k + 1.0))) * sumSquares - 3.0 * n * (k + 1.0);
        double pValue = 1.0 - chiSquareCdf(statistic, k - 1);

        List<FriedmanRank> ranks = new ArrayList<>(k);
        for (int j = 0; j < k; j++) {
            ranks.add(new FriedmanRank(algorithmOrder.get(j), rankSums[j] / n));
        }
        ranks.sort(Comparator.comparingDouble(FriedmanRank::averageRank));

        return new FriedmanComputation(statistic, pValue, ranks);
    }

    static double[] holmAdjust(List<Double> pValues) {
        int m = pValues.size();
        Integer[] order = new Integer[m];
        for (int i = 0; i < m; i++) {
            order[i] = i;
        }
        Arrays.sort(order, Comparator.comparingDouble(pValues::get));

        double[] adjustedSorted = new double[m];
        double runningMax = 0.0;
        for (int i = 0; i < m; i++) {
            int index = order[i];
            double raw = pValues.get(index);
            double adjusted = Math.min(1.0, (m - i) * raw);
            runningMax = Math.max(runningMax, adjusted);
            adjustedSorted[i] = runningMax;
        }

        double[] adjusted = new double[m];
        for (int i = 0; i < m; i++) {
            adjusted[order[i]] = adjustedSorted[i];
        }
        return adjusted;
    }

    static double[] averageRanks(double[] values) {
        int n = values.length;
        RankIndex[] order = new RankIndex[n];
        for (int i = 0; i < n; i++) {
            order[i] = new RankIndex(i, values[i]);
        }
        Arrays.sort(order, Comparator.comparingDouble(RankIndex::value));

        double[] ranks = new double[n];
        int i = 0;
        while (i < n) {
            int j = i + 1;
            while (j < n && Double.compare(order[j].value(), order[i].value()) == 0) {
                j++;
            }
            double rank = (i + j + 1) / 2.0;
            for (int k = i; k < j; k++) {
                ranks[order[k].index()] = rank;
            }
            i = j;
        }
        return ranks;
    }

    static double normalCdf(double x) {
        return 0.5 * (1.0 + erf(x / Math.sqrt(2.0)));
    }

    private static double erf(double x) {
        // Abramowitz-Stegun approximation.
        double sign = Math.signum(x);
        double a = Math.abs(x);
        double t = 1.0 / (1.0 + 0.3275911 * a);
        double y = 1.0 - (((((1.061405429 * t - 1.453152027) * t) + 1.421413741) * t - 0.284496736) * t
                + 0.254829592) * t * Math.exp(-a * a);
        return sign * y;
    }

    private static double chiSquareCdf(double x, int df) {
        if (x <= 0.0 || df <= 0) {
            return 0.0;
        }
        return regularizedGammaP(df / 2.0, x / 2.0);
    }

    private static double regularizedGammaP(double a, double x) {
        if (x < 0 || a <= 0) {
            return Double.NaN;
        }
        if (x == 0) {
            return 0.0;
        }
        if (x < a + 1.0) {
            double ap = a;
            double sum = 1.0 / a;
            double del = sum;
            for (int n = 1; n <= 10_000; n++) {
                ap += 1.0;
                del *= x / ap;
                sum += del;
                if (Math.abs(del) < Math.abs(sum) * 1e-14) {
                    break;
                }
            }
            return sum * Math.exp(-x + a * Math.log(x) - logGamma(a));
        }

        double b = x + 1.0 - a;
        double c = 1.0 / 1e-30;
        double d = 1.0 / b;
        double h = d;
        for (int i = 1; i <= 10_000; i++) {
            double an = -i * (i - a);
            b += 2.0;
            d = an * d + b;
            if (Math.abs(d) < 1e-30) {
                d = 1e-30;
            }
            c = b + an / c;
            if (Math.abs(c) < 1e-30) {
                c = 1e-30;
            }
            d = 1.0 / d;
            double del = d * c;
            h *= del;
            if (Math.abs(del - 1.0) < 1e-14) {
                break;
            }
        }
        return 1.0 - Math.exp(-x + a * Math.log(x) - logGamma(a)) * h;
    }

    private static double logGamma(double z) {
        if (z < 0.5) {
            return Math.log(Math.PI) - Math.log(Math.sin(Math.PI * z)) - logGamma(1.0 - z);
        }
        double x = 0.9999999999998099;
        double zp = z - 1.0;
        for (int i = 0; i < LANCZOS.length; i++) {
            x += LANCZOS[i] / (zp + i + 1.0);
        }
        double t = zp + LANCZOS.length - 0.5;
        return 0.5 * Math.log(2.0 * Math.PI) + (zp + 0.5) * Math.log(t) - t + Math.log(x);
    }

    record FriedmanComputation(Double statistic, Double pValue, List<FriedmanRank> ranks) {
    }

    private record RankValue(double value, int group) {
    }

    private record RankIndex(int index, double value) {
    }
}
