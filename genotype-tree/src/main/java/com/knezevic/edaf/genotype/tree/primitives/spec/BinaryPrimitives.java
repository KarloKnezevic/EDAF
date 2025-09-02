package com.knezevic.edaf.genotype.tree.primitives.spec;

import com.knezevic.edaf.genotype.tree.primitives.Function;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Defines the standard primitive set for binary/boolean logic problems.
 */
public final class BinaryPrimitives {

    private BinaryPrimitives() {
    }

    private static final List<Function> ALL_FUNCTIONS = List.of(
            new Function("AND", 2, args -> toDouble(toBool(args[0]) && toBool(args[1]))),
            new Function("OR", 2, args -> toDouble(toBool(args[0]) || toBool(args[1]))),
            new Function("NOT", 1, args -> toDouble(!toBool(args[0]))),
            new Function("XOR", 2, args -> toDouble(toBool(args[0]) ^ toBool(args[1]))),
            new Function("XNOR", 2, args -> toDouble(!(toBool(args[0]) ^ toBool(args[1]))))
    );

    private static final Map<String, Function> FUNCTION_MAP = ALL_FUNCTIONS.stream()
            .collect(Collectors.toMap(f -> f.getName().toUpperCase(), f -> f));

    public static Map<String, Function> getFunctionMap() {
        return FUNCTION_MAP;
    }

    public static List<Function> getFunctionSet() {
        return ALL_FUNCTIONS;
    }

    private static boolean toBool(double d) {
        return d >= 0.5;
    }

    private static double toDouble(boolean b) {
        return b ? 1.0 : 0.0;
    }
}
