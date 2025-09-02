package com.knezevic.edaf.factory.genotype;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Genotype;
import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.genotype.tree.Node;
import com.knezevic.edaf.genotype.tree.TreeGenotype;
import com.knezevic.edaf.genotype.tree.TreeIndividual;
import com.knezevic.edaf.genotype.tree.primitives.Function;
import com.knezevic.edaf.genotype.tree.primitives.PrimitiveSet;
import com.knezevic.edaf.genotype.tree.primitives.Terminal;
import com.knezevic.edaf.genotype.tree.primitives.spec.BinaryPrimitives;
import com.knezevic.edaf.genotype.tree.primitives.spec.RealValuedPrimitives;

import java.util.*;
import java.util.stream.Collectors;

public class TreeGenotypeFactory implements GenotypeFactory {

    @Override
    @SuppressWarnings("unchecked")
    public Genotype create(Configuration config, Random random) {
        Configuration.GenotypeConfig genotypeConfig = config.getProblem().getGenotype();
        int maxDepth = genotypeConfig.getMaxDepth();
        Map<String, Object> primitivesConf = genotypeConfig.getPrimitives();

        if (primitivesConf == null) {
            throw new IllegalArgumentException("Primitives configuration is missing for tree genotype.");
        }

        List<Function> functions = parseFunctionSet((String) primitivesConf.get("functionSet"));
        List<Terminal> terminals = parseTerminals((List<Map<String, Object>>) primitivesConf.get("terminals"));
        Map<String, Double> terminalValues = new HashMap<>();

        PrimitiveSet primitiveSet = new PrimitiveSet(functions, terminals, terminalValues);

        return new TreeGenotype(primitiveSet, maxDepth, random);
    }

    public Individual createIndividual(Object genotype) {
        if (!(genotype instanceof Node)) {
            throw new IllegalArgumentException("Genotype for TreeIndividual must be a Node.");
        }
        return new TreeIndividual((Node) genotype);
    }

    private List<Function> parseFunctionSet(String functionSetNames) {
        if (functionSetNames == null || functionSetNames.isBlank()) {
            throw new IllegalArgumentException("Function set names string is missing or empty.");
        }

        List<String> requestedFunctions = Arrays.stream(functionSetNames.split(","))
                .map(String::trim).map(String::toUpperCase).toList();

        // A bit of a heuristic to decide which map to use.
        // A more robust solution might have an explicit config for this.
        Map<String, Function> functionMap;
        if (requestedFunctions.contains("ADD") || requestedFunctions.contains("SUB")) {
            functionMap = RealValuedPrimitives.getFunctionMap();
        } else {
            functionMap = BinaryPrimitives.getFunctionMap();
        }

        return requestedFunctions.stream()
                .map(name -> {
                    Function func = functionMap.get(name);
                    if (func == null) {
                        throw new IllegalArgumentException("Unknown function name in functionSet: " + name);
                    }
                    return func;
                })
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private List<Terminal> parseTerminals(List<Map<String, Object>> terminalConfs) {
        List<Terminal> terminals = new ArrayList<>();
        if (terminalConfs == null) {
            return terminals;
        }

        for (Map<String, Object> conf : terminalConfs) {
            String name = (String) conf.get("name");
            String type = (String) conf.get("type");

            if ("variable".equalsIgnoreCase(type)) {
                terminals.add(new Terminal(name));
            } else if ("ephemeral".equalsIgnoreCase(type)) {
                List<Double> range = (List<Double>) conf.get("range");
                terminals.add(new Terminal(name, range.get(0), range.get(1)));
            }
        }
        return terminals;
    }
}
