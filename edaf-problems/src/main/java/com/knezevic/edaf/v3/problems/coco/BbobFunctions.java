package com.knezevic.edaf.v3.problems.coco;

import java.util.Arrays;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deterministic COCO/BBOB-style objective functions with instance-dependent transforms.
 *
 * <p>This implementation focuses on reproducible benchmarking flows inside EDAF.
 * Formulas follow the standard BBOB family structure (shifted/rotated variants,
 * conditioning, and multimodality), while keeping implementation compact.</p>
 */
public final class BbobFunctions {

    private static final Map<Key, Transform> CACHE = new ConcurrentHashMap<>();

    private BbobFunctions() {
        // utility class
    }

    /**
     * Evaluates one BBOB function id for a given vector and instance.
     */
    public static double evaluate(int functionId, double[] x, int instanceId) {
        int n = x.length;
        Transform t = CACHE.computeIfAbsent(new Key(functionId, instanceId, n), BbobFunctions::buildTransform);

        return switch (functionId) {
            case 1 -> sphere(shift(x, t.shift));
            case 2 -> ellipsoid(shift(x, t.shift), 1.0e6);
            case 3 -> rastrigin(shift(x, t.shift));
            case 4 -> bucheRastrigin(shift(x, t.shift));
            case 5 -> linearSlope(x, t.shift);
            case 6 -> attractiveSector(rotate(shift(x, t.shift), t.rotation), t.shift);
            case 7 -> stepEllipsoid(rotate(shift(x, t.shift), t.rotation));
            case 8 -> rosenbrock(addScalar(shift(x, t.shift), 1.0));
            case 9 -> rosenbrock(addScalar(rotate(shift(x, t.shift), t.rotation), 1.0));
            case 10 -> ellipsoid(rotate(shift(x, t.shift), t.rotation), 1.0e6);
            case 11 -> discus(rotate(shift(x, t.shift), t.rotation));
            case 12 -> bentCigar(rotate(shift(x, t.shift), t.rotation));
            case 13 -> sharpRidge(rotate(shift(x, t.shift), t.rotation));
            case 14 -> differentPowers(rotate(shift(x, t.shift), t.rotation));
            case 15 -> rastrigin(rotate(shift(x, t.shift), t.rotation));
            case 16 -> weierstrass(rotate(shift(x, t.shift), t.rotation));
            case 17 -> schaffersF7(rotate(shift(x, t.shift), t.rotation), false);
            case 18 -> schaffersF7(rotate(shift(x, t.shift), t.rotation), true);
            case 19 -> griewankRosenbrock(rotate(shift(x, t.shift), t.rotation));
            case 20 -> schwefel(shift(x, t.shift));
            case 21 -> gallagher(rotate(shift(x, t.shift), t.rotation), t, 101);
            case 22 -> gallagher(rotate(shift(x, t.shift), t.rotation), t, 21);
            case 23 -> katsuura(rotate(shift(x, t.shift), t.rotation));
            case 24 -> lunacekBiRastrigin(rotate(shift(x, t.shift), t.rotation), t.shift);
            default -> throw new IllegalArgumentException("Unsupported BBOB function id: " + functionId);
        };
    }

    private static Transform buildTransform(Key key) {
        long seed = seedFor(key);
        SplittableRandom random = new SplittableRandom(seed);
        int n = key.dimension();

        double[] shift = new double[n];
        for (int i = 0; i < n; i++) {
            shift[i] = -4.0 + 8.0 * random.nextDouble();
        }

        double[][] rotation = orthogonal(random, n);
        return new Transform(shift, rotation, seed);
    }

    private static long seedFor(Key key) {
        long z = 0x9E3779B97F4A7C15L;
        z ^= 0xBF58476D1CE4E5B9L * key.functionId();
        z ^= 0x94D049BB133111EBL * key.instanceId();
        z ^= 0x4F1BBCDCBFA54001L * key.dimension();
        return mix64(z);
    }

    private static long mix64(long z) {
        z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdl;
        z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53l;
        return z ^ (z >>> 33);
    }

    private static double[] shift(double[] x, double[] shift) {
        double[] y = Arrays.copyOf(x, x.length);
        for (int i = 0; i < y.length; i++) {
            y[i] -= shift[i];
        }
        return y;
    }

    private static double[] addScalar(double[] x, double scalar) {
        double[] y = Arrays.copyOf(x, x.length);
        for (int i = 0; i < y.length; i++) {
            y[i] += scalar;
        }
        return y;
    }

    private static double[] rotate(double[] x, double[][] rotation) {
        int n = x.length;
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            double sum = 0.0;
            for (int j = 0; j < n; j++) {
                sum += rotation[i][j] * x[j];
            }
            y[i] = sum;
        }
        return y;
    }

    private static double sphere(double[] x) {
        double sum = 0.0;
        for (double xi : x) {
            sum += xi * xi;
        }
        return sum;
    }

    private static double ellipsoid(double[] x, double condition) {
        if (x.length == 1) {
            return x[0] * x[0];
        }
        double sum = 0.0;
        int n = x.length;
        for (int i = 0; i < n; i++) {
            double exponent = i / (double) (n - 1);
            double factor = Math.pow(condition, exponent);
            sum += factor * x[i] * x[i];
        }
        return sum;
    }

    private static double rastrigin(double[] x) {
        double sum = 10.0 * x.length;
        for (double xi : x) {
            sum += xi * xi - 10.0 * Math.cos(2.0 * Math.PI * xi);
        }
        return sum;
    }

    private static double bucheRastrigin(double[] x) {
        double[] y = Arrays.copyOf(x, x.length);
        for (int i = 0; i < y.length; i++) {
            if (y[i] > 0) {
                y[i] *= 10.0;
            }
        }
        return rastrigin(y) + boundaryPenalty(y, 5.0);
    }

    private static double linearSlope(double[] x, double[] shift) {
        int n = x.length;
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            double slope = Math.pow(10.0, i / (double) Math.max(1, n - 1));
            sum += slope * (5.0 * Math.signum(shift[i]) - x[i]);
        }
        return sum + boundaryPenalty(x, 5.0);
    }

    private static double attractiveSector(double[] y, double[] shift) {
        double sum = 0.0;
        for (int i = 0; i < y.length; i++) {
            double v = y[i];
            if (v * shift[i] > 0) {
                v *= 100.0;
            }
            sum += v * v;
        }
        return Math.pow(sum, 0.9);
    }

    private static double stepEllipsoid(double[] y) {
        double[] z = Arrays.copyOf(y, y.length);
        for (int i = 0; i < z.length; i++) {
            double value = z[i];
            double transformed = Math.floor(value + 0.5);
            z[i] = Math.abs(value) > 0.5 ? transformed : transformed / 10.0;
        }
        return ellipsoid(z, 1.0e6);
    }

    private static double rosenbrock(double[] x) {
        double sum = 0.0;
        for (int i = 0; i < x.length - 1; i++) {
            double a = x[i] * x[i] - x[i + 1];
            double b = x[i] - 1.0;
            sum += 100.0 * a * a + b * b;
        }
        return sum;
    }

    private static double discus(double[] x) {
        if (x.length == 0) {
            return 0.0;
        }
        double sum = 1.0e6 * x[0] * x[0];
        for (int i = 1; i < x.length; i++) {
            sum += x[i] * x[i];
        }
        return sum;
    }

    private static double bentCigar(double[] x) {
        if (x.length == 0) {
            return 0.0;
        }
        double sum = x[0] * x[0];
        for (int i = 1; i < x.length; i++) {
            sum += 1.0e6 * x[i] * x[i];
        }
        return sum;
    }

    private static double sharpRidge(double[] x) {
        if (x.length == 0) {
            return 0.0;
        }
        double tail = 0.0;
        for (int i = 1; i < x.length; i++) {
            tail += x[i] * x[i];
        }
        return x[0] * x[0] + 100.0 * Math.sqrt(tail);
    }

    private static double differentPowers(double[] x) {
        int n = x.length;
        if (n == 0) {
            return 0.0;
        }
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            double exp = 2.0 + 4.0 * i / (double) Math.max(1, n - 1);
            sum += Math.pow(Math.abs(x[i]), exp);
        }
        return Math.sqrt(sum);
    }

    private static double weierstrass(double[] x) {
        final double a = 0.5;
        final double b = 3.0;
        final int kMax = 12;

        double c = 0.0;
        for (int k = 0; k <= kMax; k++) {
            c += Math.pow(a, k) * Math.cos(2 * Math.PI * Math.pow(b, k) * 0.5);
        }

        double sum = 0.0;
        for (double xi : x) {
            for (int k = 0; k <= kMax; k++) {
                sum += Math.pow(a, k) * Math.cos(2 * Math.PI * Math.pow(b, k) * (xi + 0.5));
            }
        }
        return sum - x.length * c;
    }

    private static double schaffersF7(double[] x, boolean illConditioned) {
        int n = x.length;
        if (n <= 1) {
            return Math.sqrt(Math.abs(x[0])) + x[0] * x[0];
        }

        double[] y = Arrays.copyOf(x, x.length);
        if (illConditioned && n > 1) {
            for (int i = 0; i < n; i++) {
                y[i] *= Math.pow(10.0, i / (double) (n - 1));
            }
        }

        double sum = 0.0;
        for (int i = 0; i < n - 1; i++) {
            double si = Math.sqrt(y[i] * y[i] + y[i + 1] * y[i + 1]);
            double term = Math.sqrt(si) + Math.sqrt(si) * Math.pow(Math.sin(50.0 * Math.pow(si, 0.2)), 2);
            sum += term;
        }
        return (sum / (n - 1));
    }

    private static double griewankRosenbrock(double[] x) {
        int n = x.length;
        if (n < 2) {
            return x.length == 0 ? 0.0 : x[0] * x[0];
        }
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            double xi = x[i] + 1.0;
            double xnext = x[(i + 1) % n] + 1.0;
            double t = 100.0 * Math.pow(xi * xi - xnext, 2) + Math.pow(xi - 1.0, 2);
            sum += (t * t) / 4000.0 - Math.cos(t) + 1.0;
        }
        return sum;
    }

    private static double schwefel(double[] x) {
        int n = x.length;
        double sum = 0.0;
        for (double xi : x) {
            double z = xi + 420.9687462275036;
            sum += z * Math.sin(Math.sqrt(Math.abs(z)));
        }
        return 418.9829 * n - sum;
    }

    private static double gallagher(double[] x, Transform transform, int peaks) {
        int n = x.length;
        long seed = mix64(transform.seed() ^ 0xA55A5AA5L ^ (peaks * 31L) ^ (n * 7L));
        SplittableRandom random = new SplittableRandom(seed);

        double best = Double.NEGATIVE_INFINITY;
        for (int p = 0; p < peaks; p++) {
            double[] center = new double[n];
            for (int i = 0; i < n; i++) {
                center[i] = -4.0 + 8.0 * random.nextDouble();
            }
            double width = 0.05 + 2.95 * random.nextDouble();
            double height = 10.0 + 90.0 * random.nextDouble();
            double distance2 = 0.0;
            for (int i = 0; i < n; i++) {
                double diff = x[i] - center[i];
                distance2 += diff * diff;
            }
            double value = height * Math.exp(-0.5 * width * distance2);
            if (value > best) {
                best = value;
            }
        }
        return 10.0 - best + boundaryPenalty(x, 5.0);
    }

    private static double katsuura(double[] x) {
        int n = x.length;
        double prod = 1.0;
        for (int i = 0; i < n; i++) {
            double sum = 0.0;
            for (int j = 1; j <= 32; j++) {
                double twoJ = 1 << j;
                double term = Math.abs(twoJ * x[i] - Math.rint(twoJ * x[i])) / twoJ;
                sum += term;
            }
            prod *= Math.pow(1.0 + (i + 1) * sum, 10.0 / Math.pow(n, 1.2));
        }
        return (prod - 1.0) * 10.0 / (n * n);
    }

    private static double lunacekBiRastrigin(double[] x, double[] shift) {
        int n = x.length;
        double mu0 = 2.5;
        double d = 1.0;
        double s = 1.0 - 1.0 / (2.0 * Math.sqrt(n + 20.0) - 8.2);
        double mu1 = -Math.sqrt((mu0 * mu0 - d) / s);

        double sum1 = 0.0;
        double sum2 = 0.0;
        double cosine = 0.0;
        for (int i = 0; i < n; i++) {
            double zi = x[i] + Math.signum(shift[i]) * mu0;
            sum1 += (zi - mu0) * (zi - mu0);
            sum2 += (zi - mu1) * (zi - mu1);
            cosine += Math.cos(2.0 * Math.PI * (zi - mu0));
        }
        return Math.min(sum1, d * n + s * sum2) + 10.0 * (n - cosine) + boundaryPenalty(x, 5.0);
    }

    private static double boundaryPenalty(double[] x, double bound) {
        double sum = 0.0;
        for (double xi : x) {
            double excess = Math.max(0.0, Math.abs(xi) - bound);
            sum += excess * excess;
        }
        return 100.0 * sum;
    }

    private static double[][] orthogonal(SplittableRandom random, int n) {
        double[][] matrix = new double[n][n];
        GaussianSource gauss = new GaussianSource(random);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = gauss.nextGaussian();
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                double dot = dot(matrix[i], matrix[j]);
                for (int k = 0; k < n; k++) {
                    matrix[i][k] -= dot * matrix[j][k];
                }
            }
            double norm = Math.sqrt(dot(matrix[i], matrix[i]));
            if (norm < 1.0e-12) {
                Arrays.fill(matrix[i], 0.0);
                matrix[i][i] = 1.0;
            } else {
                for (int k = 0; k < n; k++) {
                    matrix[i][k] /= norm;
                }
            }
        }

        return matrix;
    }

    private static double dot(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    private record Key(int functionId, int instanceId, int dimension) {
    }

    private record Transform(double[] shift, double[][] rotation, long seed) {
    }

    private static final class GaussianSource {
        private final SplittableRandom random;
        private boolean hasSpare;
        private double spare;

        private GaussianSource(SplittableRandom random) {
            this.random = random;
        }

        private double nextGaussian() {
            if (hasSpare) {
                hasSpare = false;
                return spare;
            }
            double u;
            double v;
            double s;
            do {
                u = 2.0 * random.nextDouble() - 1.0;
                v = 2.0 * random.nextDouble() - 1.0;
                s = u * u + v * v;
            } while (s <= 0.0 || s >= 1.0);

            double mul = Math.sqrt(-2.0 * Math.log(s) / s);
            spare = v * mul;
            hasSpare = true;
            return u * mul;
        }
    }
}
