package com.knezevic.edaf.v3.problems.tree;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.types.VariableLengthVector;

import java.util.ArrayList;
import java.util.List;

/**
 * Nguyen symbolic regression benchmark using tokenized expression trees.
 *
 * <p>Genotype is interpreted as prefix tokens. This keeps tree-style search runnable
 * with the existing variable-length representation pipeline.</p>
 */
public final class NguyenSymbolicRegressionProblem implements Problem<VariableLengthVector<Integer>> {

    private final int variant;
    private final int sampleCount;
    private final double minX;
    private final double maxX;

    public NguyenSymbolicRegressionProblem(int variant, int sampleCount, double minX, double maxX) {
        this.variant = variant;
        this.sampleCount = Math.max(8, sampleCount);
        this.minX = minX;
        this.maxX = maxX;
    }

    @Override
    public String name() {
        return "nguyen-sr-" + variant;
    }

    @Override
    public ObjectiveSense objectiveSense() {
        return ObjectiveSense.MINIMIZE;
    }

    @Override
    public Fitness evaluate(VariableLengthVector<Integer> genotype) {
        TokenReader reader = new TokenReader(genotype.values());
        Expr expression = parseExpression(reader, 0);

        double mse = 0.0;
        for (double x : samplePoints()) {
            double target = targetFunction(x);
            double prediction = expression.eval(x);
            if (!Double.isFinite(prediction)) {
                mse += 1.0e6;
                continue;
            }
            double error = prediction - target;
            mse += error * error;
        }
        mse /= sampleCount;

        // Small size regularization prevents unbounded token growth.
        mse += 1.0e-3 * genotype.size();
        return new ScalarFitness(mse);
    }

    @Override
    public List<String> violations(VariableLengthVector<Integer> genotype) {
        if (genotype == null || genotype.size() == 0) {
            return List.of("Nguyen symbolic regression requires at least one token");
        }
        return List.of();
    }

    private List<Double> samplePoints() {
        List<Double> points = new ArrayList<>(sampleCount);
        if (sampleCount == 1) {
            points.add((minX + maxX) * 0.5);
            return points;
        }
        for (int i = 0; i < sampleCount; i++) {
            double t = i / (double) (sampleCount - 1);
            points.add(minX + t * (maxX - minX));
        }
        return points;
    }

    private double targetFunction(double x) {
        return switch (variant) {
            case 1 -> x * x * x + x * x + x;
            case 2 -> Math.pow(x, 4) + Math.pow(x, 3) + x * x + x;
            case 3 -> Math.pow(x, 5) + Math.pow(x, 4) + Math.pow(x, 3) + x * x + x;
            case 4 -> Math.pow(x, 6) + Math.pow(x, 5) + Math.pow(x, 4) + Math.pow(x, 3) + x * x + x;
            case 5 -> Math.sin(x * x) * Math.cos(x) - 1.0;
            case 6 -> Math.sin(x) + Math.sin(x + x * x);
            case 7 -> Math.log(Math.abs(x) + 1.0) + Math.log(x * x + 1.0);
            case 8 -> Math.sqrt(Math.abs(x));
            default -> throw new IllegalArgumentException("Unsupported Nguyen variant: " + variant);
        };
    }

    private Expr parseExpression(TokenReader reader, int depth) {
        if (depth > 24 || !reader.hasNext()) {
            return Expr.constant(0.0);
        }

        int token = Math.floorMod(reader.next(), 16);
        return switch (token) {
            case 0 -> Expr.variable();
            case 1 -> Expr.constant(-1.0);
            case 2 -> Expr.constant(0.5);
            case 3 -> Expr.constant(1.0);
            case 4 -> Expr.unary(Math::sin, parseExpression(reader, depth + 1));
            case 5 -> Expr.unary(Math::cos, parseExpression(reader, depth + 1));
            case 6 -> Expr.unary(Math::exp, parseExpression(reader, depth + 1));
            case 7 -> Expr.unary(v -> Math.log(Math.abs(v) + 1e-6), parseExpression(reader, depth + 1));
            case 8 -> Expr.unary(v -> Math.sqrt(Math.abs(v)), parseExpression(reader, depth + 1));
            case 9 -> Expr.binary((a, b) -> a + b, parseExpression(reader, depth + 1), parseExpression(reader, depth + 1));
            case 10 -> Expr.binary((a, b) -> a - b, parseExpression(reader, depth + 1), parseExpression(reader, depth + 1));
            case 11 -> Expr.binary((a, b) -> a * b, parseExpression(reader, depth + 1), parseExpression(reader, depth + 1));
            case 12 -> Expr.binary(NguyenSymbolicRegressionProblem::safeDiv,
                    parseExpression(reader, depth + 1),
                    parseExpression(reader, depth + 1));
            case 13 -> Expr.binary(Math::pow,
                    parseExpression(reader, depth + 1),
                    Expr.unary(v -> clamp(v, -4.0, 4.0), parseExpression(reader, depth + 1)));
            case 14 -> Expr.unary(v -> v * v, parseExpression(reader, depth + 1));
            case 15 -> Expr.unary(v -> v * v * v, parseExpression(reader, depth + 1));
            default -> Expr.constant(0.0);
        };
    }

    private static double safeDiv(double a, double b) {
        if (Math.abs(b) < 1e-6) {
            return a;
        }
        return a / b;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class TokenReader {
        private final List<Integer> tokens;
        private int index;

        private TokenReader(List<Integer> tokens) {
            this.tokens = tokens;
            this.index = 0;
        }

        private boolean hasNext() {
            return index < tokens.size();
        }

        private int next() {
            if (!hasNext()) {
                return 0;
            }
            return tokens.get(index++);
        }
    }

    @FunctionalInterface
    private interface DoubleUnary {
        double apply(double value);
    }

    @FunctionalInterface
    private interface DoubleBinary {
        double apply(double left, double right);
    }

    private interface Expr {
        double eval(double x);

        static Expr variable() {
            return x -> x;
        }

        static Expr constant(double value) {
            return x -> value;
        }

        static Expr unary(DoubleUnary op, Expr child) {
            return x -> op.apply(child.eval(x));
        }

        static Expr binary(DoubleBinary op, Expr left, Expr right) {
            return x -> op.apply(left.eval(x), right.eval(x));
        }
    }
}
