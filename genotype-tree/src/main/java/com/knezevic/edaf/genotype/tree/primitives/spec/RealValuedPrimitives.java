package com.knezevic.edaf.genotype.tree.primitives.spec;

import com.knezevic.edaf.genotype.tree.primitives.Function;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Defines the standard primitive set for real-valued symbolic regression problems.
 */
public final class RealValuedPrimitives {

    private RealValuedPrimitives() {
    }

    private static final List<Function> ALL_FUNCTIONS = List.of(
            new Function("ADD", 2, args -> args[0] + args[1]),
            new Function("SUB", 2, args -> args[0] - args[1]),
            new Function("MUL", 2, args -> args[0] * args[1]),
            new Function("DIV", 2, RealValuedPrimitives::protectedDiv),
            new Function("POW", 2, RealValuedPrimitives::protectedPow),
            new Function("LOG", 1, RealValuedPrimitives::protectedLog),
            new Function("SIN", 1, args -> Math.sin(args[0])),
            new Function("COS", 1, args -> Math.cos(args[0])),
            new Function("TAN", 1, args -> Math.tan(args[0]))
    );

    private static final Map<String, Function> FUNCTION_MAP = ALL_FUNCTIONS.stream()
            .collect(Collectors.toMap(f -> f.getName().toUpperCase(), f -> f));

    /**
     * Returns a map of all available real-valued functions, keyed by name.
     * @return A map of functions.
     */
    public static Map<String, Function> getFunctionMap() {
        return FUNCTION_MAP;
    }

    private static double protectedDiv(double... args) {
        if (args[1] == 0.0) {
            return 1.0;
        }
        return args[0] / args[1];
    }

    private static double protectedLog(double... args) {
        if (args[0] <= 0.0) {
            return 0.0;
        }
        return Math.log(args[0]);
    }

    private static double protectedPow(double... args) {
        try {
            double base = args[0];
            double exp = args[1];
            if (Math.abs(exp) > 10) {
                exp = 10 * Math.signum(exp);
            }
            return Math.pow(base, exp);
        } catch (Exception e) {
            return 1.0;
        }
    }
}
