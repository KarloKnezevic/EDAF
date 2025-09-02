package com.knezevic.edaf.genotype.tree.primitives.spec;

import com.knezevic.edaf.genotype.tree.primitives.Function;

import java.util.List;

/**
 * Defines the standard primitive set for binary/boolean logic problems.
 * Note: The current GP framework evaluates trees to a double.
 * A real implementation of boolean GP would require a different evaluation mechanism
 * (e.g., returning booleans or objects). This class is provided to fulfill the prompt's
 * requirements, but using it would require further framework modifications.
 */
public final class BinaryPrimitives {

    private BinaryPrimitives() {
    }

    /**
     * Returns a list of standard functions for boolean logic problems.
     * Functions use 1.0 for true and 0.0 for false.
     * @return A list of Functions.
     */
    public static List<Function> getFunctionSet() {
        return List.of(
                new Function("AND", 2, args -> toDouble(toBool(args[0]) && toBool(args[1]))),
                new Function("OR", 2, args -> toDouble(toBool(args[0]) || toBool(args[1]))),
                new Function("NOT", 1, args -> toDouble(!toBool(args[0]))),
                new Function("XOR", 2, args -> toDouble(toBool(args[0]) ^ toBool(args[1]))),
                new Function("XNOR", 2, args -> toDouble(!(toBool(args[0]) ^ toBool(args[1]))))
        );
    }

    private static boolean toBool(double d) {
        return d == 1.0;
    }

    private static double toDouble(boolean b) {
        return b ? 1.0 : 0.0;
    }
}
