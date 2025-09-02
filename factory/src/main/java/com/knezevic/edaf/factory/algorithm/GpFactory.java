package com.knezevic.edaf.factory.algorithm;

import com.knezevic.edaf.algorithm.gp.GeneticProgrammingAlgorithm;
import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.genotype.tree.TreeIndividual;
import com.knezevic.edaf.genotype.tree.operators.crossover.TreeCrossover;
import com.knezevic.edaf.genotype.tree.operators.mutation.TreeMutation;
import com.knezevic.edaf.genotype.tree.primitives.Function;
import com.knezevic.edaf.genotype.tree.primitives.PrimitiveSet;
import com.knezevic.edaf.genotype.tree.primitives.Terminal;
import com.knezevic.edaf.genotype.tree.primitives.spec.BinaryPrimitives;
import com.knezevic.edaf.genotype.tree.primitives.spec.RealValuedPrimitives;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes", "unchecked"})
public class GpFactory implements AlgorithmFactory<TreeIndividual> {

    private PrimitiveSet primitiveSet; // Cache the primitive set

    @Override
    public Algorithm<TreeIndividual> createAlgorithm(Configuration config, Problem<TreeIndividual> problem, Population<TreeIndividual> population,
                                                     Selection<TreeIndividual> selection, Statistics<TreeIndividual> statistics,
                                                     TerminationCondition<TreeIndividual> terminationCondition, Random random) {

        Crossover crossover = createCrossover(config, random);
        Mutation mutation = createMutation(config, random);

        double crossoverRate = config.getProblem().getGenotype().getCrossing().getProbability();
        double mutationRate = config.getProblem().getGenotype().getMutation().getProbability();
        int elitismSize = config.getAlgorithm().getElitism();

        return new GeneticProgrammingAlgorithm(problem, population, selection, crossover, mutation,
                terminationCondition, crossoverRate, mutationRate, elitismSize);
    }

    @Override
    public Crossover createCrossover(Configuration config, Random random) {
        return new TreeCrossover(random);
    }

    @Override
    public Mutation createMutation(Configuration config, Random random) {
        PrimitiveSet ps = getPrimitiveSet(config);
        int maxDepth = config.getProblem().getGenotype().getMaxDepth();
        return new TreeMutation(ps, maxDepth, random);
    }

    private PrimitiveSet getPrimitiveSet(Configuration config) {
        if (this.primitiveSet != null) {
            return this.primitiveSet;
        }

        Map<String, Object> primitivesConf = config.getProblem().getGenotype().getPrimitives();
        if (primitivesConf == null) {
            throw new IllegalArgumentException("Primitives configuration is missing for tree genotype.");
        }

        List<Function> functions = parseFunctionSet((String) primitivesConf.get("functionSet"));
        List<Terminal> terminals = parseTerminals((List<Map<String, Object>>) primitivesConf.get("terminals"));
        this.primitiveSet = new PrimitiveSet(functions, terminals, new HashMap<>());
        return this.primitiveSet;
    }

    private List<Function> parseFunctionSet(String functionSetNames) {
        if (functionSetNames == null || functionSetNames.isBlank()) {
            throw new IllegalArgumentException("Function set names string is missing or empty.");
        }

        List<String> requestedFunctions = Arrays.stream(functionSetNames.split(","))
                .map(String::trim).map(String::toUpperCase).toList();

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

    private List<Terminal> parseTerminals(List<Map<String, Object>> terminalConfs) {
        List<Terminal> terminals = new ArrayList<>();
        if (terminalConfs == null) return terminals;

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
