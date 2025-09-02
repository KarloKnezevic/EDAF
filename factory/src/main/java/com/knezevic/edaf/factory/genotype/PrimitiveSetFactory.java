package com.knezevic.edaf.factory.genotype;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.genotype.tree.primitives.Function;
import com.knezevic.edaf.genotype.tree.primitives.PrimitiveSet;
import com.knezevic.edaf.genotype.tree.primitives.Terminal;
import com.knezevic.edaf.genotype.tree.primitives.spec.AntPrimitives;
import com.knezevic.edaf.genotype.tree.primitives.spec.BinaryPrimitives;
import com.knezevic.edaf.genotype.tree.primitives.spec.RealValuedPrimitives;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A factory for creating {@link PrimitiveSet} objects from configuration.
 */
public final class PrimitiveSetFactory {

    private PrimitiveSetFactory() {}

    /**
     * Creates a {@link PrimitiveSet} from the given configuration.
     *
     * @param config The configuration.
     * @return A {@link PrimitiveSet} instance.
     */
    @SuppressWarnings("unchecked")
    public static PrimitiveSet create(Configuration config) {
        Map<String, Object> primitivesConf = config.getProblem().getGenotype().getPrimitives();
        if (primitivesConf == null) {
            throw new IllegalArgumentException("Primitives configuration is missing for tree genotype.");
        }

        String type = (String) primitivesConf.get("type");
        String functionSetNames = (String) primitivesConf.get("functionSet");
        List<Map<String, Object>> terminalConfs = (List<Map<String, Object>>) primitivesConf.get("terminals");

        List<Function> functions = parseFunctionSet(type, functionSetNames);
        List<Terminal> terminals = parseTerminals(type, terminalConfs);

        return new PrimitiveSet(functions, terminals, new HashMap<>());
    }

    /**
     * Parses the function set from the configuration.
     *
     * @param type The type of the primitive set.
     * @param functionSetNames The names of the functions in the set.
     * @return A list of {@link Function} objects.
     */
    private static List<Function> parseFunctionSet(String type, String functionSetNames) {
        if (functionSetNames == null || functionSetNames.isBlank()) {
            return Collections.emptyList();
        }

        Map<String, Function> functionMap = getFunctionMapForType(type);

        return Arrays.stream(functionSetNames.split(","))
                .map(String::trim).map(String::toUpperCase)
                .map(name -> {
                    Function func = functionMap.get(name);
                    if (func == null) {
                        throw new IllegalArgumentException("Unknown function name '" + name + "' for primitive set type '" + type + "'");
                    }
                    return func;
                })
                .collect(Collectors.toList());
    }

    /**
     * Parses the terminals from the configuration.
     *
     * @param type The type of the primitive set.
     * @param terminalConfs The terminal configurations.
     * @return A list of {@link Terminal} objects.
     */
    private static List<Terminal> parseTerminals(String type, List<Map<String, Object>> terminalConfs) {
        if (terminalConfs == null) {
            return Collections.emptyList();
        }

        // For now, parsing is simple and doesn't depend on type, but it could in the future.
        List<Terminal> terminals = new ArrayList<>();
        for (Map<String, Object> conf : terminalConfs) {
            String name = (String) conf.get("name");
            String termType = (String) conf.get("type");

            if ("variable".equalsIgnoreCase(termType)) {
                terminals.add(new Terminal(name));
            } else if ("ephemeral".equalsIgnoreCase(termType)) {
                List<Double> range = (List<Double>) conf.get("range");
                terminals.add(new Terminal(name, range.get(0), range.get(1)));
            }
        }
        return terminals;
    }

    /**
     * Returns a map of functions for the given primitive set type.
     *
     * @param type The type of the primitive set.
     * @return A map of functions.
     */
    private static Map<String, Function> getFunctionMapForType(String type) {
        if ("RealValued".equalsIgnoreCase(type)) {
            return RealValuedPrimitives.getFunctionMap();
        } else if ("Binary".equalsIgnoreCase(type)) {
            return BinaryPrimitives.getFunctionMap();
        } else if ("Ant".equalsIgnoreCase(type)) {
            return AntPrimitives.getFunctionMap();
        }
        throw new IllegalArgumentException("Unknown primitive set type: " + type);
    }
}
