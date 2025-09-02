package com.knezevic.edaf.genotype.tree.primitives.spec;

import com.knezevic.edaf.genotype.tree.primitives.Function;
import com.knezevic.edaf.genotype.tree.primitives.Terminal;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Defines the primitive set for the Santa Fe Ant Trail problem.
 */
public final class AntPrimitives {

    private AntPrimitives() {
    }

    // The IFunction lambdas are empty because these functions are procedural
    // and are handled by the TreeInterpreter, not by returning a value.
    private static final List<Function> ALL_FUNCTIONS = List.of(
            new Function("IF_FOOD_AHEAD", 2, args -> 0.0),
            new Function("PROGN2", 2, args -> 0.0),
            new Function("PROGN3", 3, args -> 0.0)
    );

    private static final List<Terminal> ALL_TERMINALS = List.of(
            new Terminal("MOVE"),
            new Terminal("LEFT"),
            new Terminal("RIGHT")
    );

    private static final Map<String, Function> FUNCTION_MAP = ALL_FUNCTIONS.stream()
            .collect(Collectors.toMap(f -> f.getName().toUpperCase(), f -> f));

    private static final Map<String, Terminal> TERMINAL_MAP = ALL_TERMINALS.stream()
            .collect(Collectors.toMap(t -> t.getName().toUpperCase(), t -> t));

    public static Map<String, Function> getFunctionMap() {
        return FUNCTION_MAP;
    }

    public static Map<String, Terminal> getTerminalMap() {
        return TERMINAL_MAP;
    }

    public static List<Function> getFunctionSet() {
        return ALL_FUNCTIONS;
    }

    public static List<Terminal> getTerminalSet() {
        return ALL_TERMINALS;
    }
}
