package com.knezevic.edaf.v3.problems.continuous.cec;

import java.util.Arrays;
import java.util.SplittableRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Deterministic CEC 2014 style continuous benchmark function family.
 *
 * <p>This implementation keeps the same CEC-style API and function indexing (1..30)
 * with deterministic instance transforms. Formulas are intentionally compact and
 * engineering-focused for repeatable benchmarking inside EDAF.</p>
 */
public final class Cec2014Functions {

    private static final ConcurrentMap<Key, Transform> TRANSFORMS = new ConcurrentHashMap<>();

    private Cec2014Functions() {
        // utility class
    }

    /**
     * Evaluates one CEC-style function in range 1..30.
     */
    public static double evaluate(int functionId, double[] x, int instanceId) {
        if (functionId < 1 || functionId > 30) {
            throw new IllegalArgumentException("CEC2014 functionId must be in [1..30], got " + functionId);
        }

        int n = x.length;
        Transform t = TRANSFORMS.computeIfAbsent(new Key(functionId, instanceId, n), Cec2014Functions::buildTransform);
        double[] shifted = shift(x, t.shift());
        double[] rotated = rotate(shifted, t.rotation());

        return switch (functionId) {
            case 1 -> sphere(shifted);
            case 2 -> ellipsoid(rotated, 1.0e6);
            case 3 -> bentCigar(rotated);
            case 4 -> discus(rotated);
            case 5 -> rosenbrock(addScalar(rotated, 1.0));
            case 6 -> ackley(rotated);
            case 7 -> weierstrass(rotated);
            case 8 -> griewank(rotated);
            case 9 -> rastrigin(rotated);
            case 10 -> schwefel(shifted);
            case 11 -> katsuura(rotated);
            case 12 -> happyCat(rotated);
            case 13 -> hgbat(rotated);
            case 14 -> expandedGriewankRosenbrock(rotated);
            case 15 -> expandedScafferF6(rotated);
            case 16 -> hybrid(rotated, n, Base.bentCigar, Base.rastrigin, Base.griewank);
            case 17 -> hybrid(rotated, n, Base.ackley, Base.sphere, Base.weierstrass);
            case 18 -> hybrid(rotated, n, Base.schwefel, Base.discus, Base.katsuura);
            case 19 -> composition(rotated, t.seed(), n, Base.sphere, Base.rastrigin, Base.ackley);
            case 20 -> composition(rotated, t.seed(), n, Base.ellipsoid, Base.griewank, Base.weierstrass);
            case 21 -> composition(rotated, t.seed(), n, Base.rosenbrock, Base.schwefel, Base.katsuura);
            case 22 -> composition(rotated, t.seed(), n, Base.bentCigar, Base.hgbat, Base.happyCat);
            case 23 -> composition(rotated, t.seed(), n, Base.expandedGriewankRosenbrock, Base.rastrigin, Base.sphere);
            case 24 -> composition(rotated, t.seed(), n, Base.expandedScafferF6, Base.ackley, Base.griewank);
            case 25 -> composition(rotated, t.seed(), n, Base.discus, Base.happyCat, Base.schwefel);
            case 26 -> composition(rotated, t.seed(), n, Base.weierstrass, Base.katsuura, Base.ellipsoid);
            case 27 -> composition(rotated, t.seed(), n, Base.rastrigin, Base.rosenbrock, Base.expandedScafferF6);
            case 28 -> composition(rotated, t.seed(), n, Base.ackley, Base.hgbat, Base.expandedGriewankRosenbrock);
            case 29 -> composition(rotated, t.seed(), n, Base.schwefel, Base.bentCigar, Base.weierstrass);
            case 30 -> composition(rotated, t.seed(), n, Base.katsuura, Base.discus, Base.sphere);
            default -> throw new IllegalStateException("Unexpected functionId: " + functionId);
        };
    }

    private static Transform buildTransform(Key key) {
        long seed = mix64(0x9E3779B97F4A7C15L
                ^ (key.functionId() * 0xBF58476D1CE4E5B9L)
                ^ (key.instanceId() * 0x94D049BB133111EBL)
                ^ (key.dimension() * 0x4F1BBCDCBFA54001L));
        SplittableRandom random = new SplittableRandom(seed);

        double[] shift = new double[key.dimension()];
        for (int i = 0; i < shift.length; i++) {
            shift[i] = -80.0 + 160.0 * random.nextDouble();
        }
        double[][] rotation = orthogonal(random, key.dimension());
        return new Transform(shift, rotation, seed);
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

    private static double hybrid(double[] x,
                                 int n,
                                 Base first,
                                 Base second,
                                 Base third) {
        int cut1 = Math.max(1, (int) Math.round(n * 0.3));
        int cut2 = Math.max(cut1 + 1, (int) Math.round(n * 0.7));
        cut2 = Math.min(cut2, n - 1);

        double[] a = Arrays.copyOfRange(x, 0, cut1);
        double[] b = Arrays.copyOfRange(x, cut1, cut2);
        double[] c = Arrays.copyOfRange(x, cut2, n);

        return first.eval(a) + second.eval(b) + third.eval(c);
    }

    private static double composition(double[] x, long seed, int n, Base first, Base second, Base third) {
        SplittableRandom random = new SplittableRandom(mix64(seed ^ 0xA5A5A5A55A5A5A5AL));

        double[] c1 = randomVector(random, n, 80.0);
        double[] c2 = randomVector(random, n, 80.0);
        double[] c3 = randomVector(random, n, 80.0);

        double f1 = first.eval(subtract(x, c1));
        double f2 = second.eval(subtract(x, c2));
        double f3 = third.eval(subtract(x, c3));

        double w1 = weight(x, c1);
        double w2 = weight(x, c2);
        double w3 = weight(x, c3);

        double sum = w1 + w2 + w3;
        if (sum <= 0.0) {
            return (f1 + f2 + f3) / 3.0;
        }

        return (w1 * f1 + w2 * f2 + w3 * f3) / sum;
    }

    private static double[] randomVector(SplittableRandom random, int n, double range) {
        double[] out = new double[n];
        for (int i = 0; i < n; i++) {
            out[i] = -range + 2.0 * range * random.nextDouble();
        }
        return out;
    }

    private static double weight(double[] x, double[] center) {
        double dist2 = 0.0;
        for (int i = 0; i < x.length; i++) {
            double d = x[i] - center[i];
            dist2 += d * d;
        }
        return Math.exp(-dist2 / (2.0 * x.length * 100.0));
    }

    private static double[] subtract(double[] x, double[] y) {
        double[] out = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            out[i] = x[i] - y[i];
        }
        return out;
    }

    private static double sphere(double[] x) {
        double sum = 0.0;
        for (double xi : x) {
            sum += xi * xi;
        }
        return sum;
    }

    private static double ellipsoid(double[] x, double condition) {
        if (x.length == 0) {
            return 0.0;
        }
        if (x.length == 1) {
            return x[0] * x[0];
        }
        double sum = 0.0;
        for (int i = 0; i < x.length; i++) {
            double exponent = i / (double) (x.length - 1);
            double factor = Math.pow(condition, exponent);
            sum += factor * x[i] * x[i];
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

    private static double rosenbrock(double[] x) {
        if (x.length < 2) {
            return x.length == 0 ? 0.0 : x[0] * x[0];
        }
        double sum = 0.0;
        for (int i = 0; i < x.length - 1; i++) {
            double a = x[i] * x[i] - x[i + 1];
            double b = x[i] - 1.0;
            sum += 100.0 * a * a + b * b;
        }
        return sum;
    }

    private static double ackley(double[] x) {
        if (x.length == 0) {
            return 0.0;
        }
        double sumSq = 0.0;
        double sumCos = 0.0;
        for (double xi : x) {
            sumSq += xi * xi;
            sumCos += Math.cos(2.0 * Math.PI * xi);
        }
        double n = x.length;
        return -20.0 * Math.exp(-0.2 * Math.sqrt(sumSq / n))
                - Math.exp(sumCos / n)
                + 20.0
                + Math.E;
    }

    private static double weierstrass(double[] x) {
        final double a = 0.5;
        final double b = 3.0;
        final int kMax = 12;

        double c = 0.0;
        for (int k = 0; k <= kMax; k++) {
            c += Math.pow(a, k) * Math.cos(2.0 * Math.PI * Math.pow(b, k) * 0.5);
        }

        double sum = 0.0;
        for (double xi : x) {
            for (int k = 0; k <= kMax; k++) {
                sum += Math.pow(a, k) * Math.cos(2.0 * Math.PI * Math.pow(b, k) * (xi + 0.5));
            }
        }
        return sum - x.length * c;
    }

    private static double griewank(double[] x) {
        if (x.length == 0) {
            return 0.0;
        }
        double sum = 0.0;
        double prod = 1.0;
        for (int i = 0; i < x.length; i++) {
            sum += (x[i] * x[i]) / 4000.0;
            prod *= Math.cos(x[i] / Math.sqrt(i + 1.0));
        }
        return sum - prod + 1.0;
    }

    private static double rastrigin(double[] x) {
        double sum = 10.0 * x.length;
        for (double xi : x) {
            sum += xi * xi - 10.0 * Math.cos(2.0 * Math.PI * xi);
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

    private static double katsuura(double[] x) {
        if (x.length == 0) {
            return 0.0;
        }
        double prod = 1.0;
        int n = x.length;
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

    private static double happyCat(double[] x) {
        if (x.length == 0) {
            return 0.0;
        }
        double sumSq = 0.0;
        double sumX = 0.0;
        for (double xi : x) {
            sumSq += xi * xi;
            sumX += xi;
        }
        double n = x.length;
        return Math.pow(Math.abs(sumSq - n), 0.25) + (0.5 * sumSq + sumX) / n + 0.5;
    }

    private static double hgbat(double[] x) {
        if (x.length == 0) {
            return 0.0;
        }
        double sumSq = 0.0;
        double sumX = 0.0;
        for (double xi : x) {
            sumSq += xi * xi;
            sumX += xi;
        }
        double n = x.length;
        return Math.pow(Math.abs(sumSq * sumSq - sumX * sumX), 0.5) + (0.5 * sumSq + sumX) / n + 0.5;
    }

    private static double expandedGriewankRosenbrock(double[] x) {
        if (x.length < 2) {
            return x.length == 0 ? 0.0 : x[0] * x[0];
        }
        double sum = 0.0;
        for (int i = 0; i < x.length; i++) {
            double xi = x[i];
            double xnext = x[(i + 1) % x.length];
            double t = 100.0 * Math.pow(xi * xi - xnext, 2) + Math.pow(xi - 1.0, 2);
            sum += t * t / 4000.0 - Math.cos(t) + 1.0;
        }
        return sum;
    }

    private static double expandedScafferF6(double[] x) {
        if (x.length < 2) {
            return x.length == 0 ? 0.0 : x[0] * x[0];
        }
        double sum = 0.0;
        for (int i = 0; i < x.length; i++) {
            double a = x[i];
            double b = x[(i + 1) % x.length];
            double r2 = a * a + b * b;
            double num = Math.sin(Math.sqrt(r2));
            num *= num;
            double den = 1.0 + 0.001 * r2;
            sum += 0.5 + (num - 0.5) / (den * den);
        }
        return sum;
    }

    private static double[][] orthogonal(SplittableRandom random, int n) {
        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = random.nextDouble(-1.0, 1.0);
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                double dot = dot(matrix[i], matrix[j]);
                double norm = dot(matrix[j], matrix[j]);
                if (Math.abs(norm) > 1e-12) {
                    double scale = dot / norm;
                    for (int k = 0; k < n; k++) {
                        matrix[i][k] -= scale * matrix[j][k];
                    }
                }
            }
            double norm = Math.sqrt(Math.max(1e-12, dot(matrix[i], matrix[i])));
            for (int k = 0; k < n; k++) {
                matrix[i][k] /= norm;
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

    private static long mix64(long z) {
        z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdl;
        z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53l;
        return z ^ (z >>> 33);
    }

    private record Key(int functionId, int instanceId, int dimension) {
    }

    private record Transform(double[] shift, double[][] rotation, long seed) {
    }

    @FunctionalInterface
    private interface Base {
        double eval(double[] x);

        Base sphere = Cec2014Functions::sphere;
        Base ellipsoid = x -> Cec2014Functions.ellipsoid(x, 1.0e6);
        Base bentCigar = Cec2014Functions::bentCigar;
        Base discus = Cec2014Functions::discus;
        Base rosenbrock = Cec2014Functions::rosenbrock;
        Base ackley = Cec2014Functions::ackley;
        Base weierstrass = Cec2014Functions::weierstrass;
        Base griewank = Cec2014Functions::griewank;
        Base rastrigin = Cec2014Functions::rastrigin;
        Base schwefel = Cec2014Functions::schwefel;
        Base katsuura = Cec2014Functions::katsuura;
        Base happyCat = Cec2014Functions::happyCat;
        Base hgbat = Cec2014Functions::hgbat;
        Base expandedGriewankRosenbrock = Cec2014Functions::expandedGriewankRosenbrock;
        Base expandedScafferF6 = Cec2014Functions::expandedScafferF6;
    }
}
