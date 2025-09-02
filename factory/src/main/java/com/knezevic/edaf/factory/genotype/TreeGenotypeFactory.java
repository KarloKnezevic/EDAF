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
        List<Terminal> terminals = parseTerminals((List<Map<String, Object>>) primitivesConf.get("terminals"), random);
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

        Map<String, Function> allRealFunctions = RealValuedPrimitives.getFunctionMap();
        // In a more complete implementation, we would also check BinaryPrimitives, etc.

        return Arrays.stream(functionSetNames.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .map(name -> {
                    Function func = allRealFunctions.get(name);
                    if (func == null) {
                        throw new IllegalArgumentException("Unknown function name in functionSet: " + name);
                    }
                    return func;
                })
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private List<Terminal> parseTerminals(List<Map<String, Object>> terminalConfs, Random random) {
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
                double value = random.nextDouble() * (range.get(1) - range.get(0)) + range.get(0);
                terminals.add(new Terminal(String.format("%.3f", value), value));
            }
        }
        return terminals;
    }
}
