package com.knezevic.edaf.algorithm.gp.operators;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Mutation;
import com.knezevic.edaf.genotype.tree.Node;
import com.knezevic.edaf.genotype.tree.TreeGenotype;
import com.knezevic.edaf.genotype.tree.TreeIndividual;
import com.knezevic.edaf.genotype.tree.primitives.Function;
import com.knezevic.edaf.genotype.tree.primitives.PrimitiveSet;
import com.knezevic.edaf.genotype.tree.primitives.Terminal;
import com.knezevic.edaf.genotype.tree.primitives.spec.RealValuedPrimitives;
import com.knezevic.edaf.genotype.tree.util.TreeUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Performs subtree mutation for Genetic Programming.
 * It replaces a random subtree with a new, randomly generated subtree.
 */
public class TreeMutation implements Mutation<TreeIndividual> {

    private final TreeGenotype subtreeGenerator;
    private final Random random;

    @SuppressWarnings("unchecked")
    public TreeMutation(Configuration config, Random random) {
        this.random = random;

        Configuration.GenotypeConfig genotypeConfig = config.getProblem().getGenotype();
        int maxDepth = genotypeConfig.getMaxDepth() / 2;
        if (maxDepth < 1) maxDepth = 1;

        Map<String, Object> primitivesConf = genotypeConfig.getPrimitives();
        String functionSetNames = (String) primitivesConf.get("functionSet");
        List<Map<String, Object>> terminalConfs = (List<Map<String, Object>>) primitivesConf.get("terminals");

        List<Function> functions = parseFunctionSet(functionSetNames);
        List<Terminal> terminals = parseTerminals(terminalConfs, random);
        PrimitiveSet primitiveSet = new PrimitiveSet(functions, terminals, new HashMap<>());

        this.subtreeGenerator = new TreeGenotype(primitiveSet, maxDepth, random);
    }

    @Override
    public void mutate(TreeIndividual individual) {
        Node root = individual.getGenotype();
        Node mutationPoint = TreeUtils.getRandomNode(root, random);
        Node newSubtree = subtreeGenerator.create();

        if (root == mutationPoint) {
            if (!root.getChildren().isEmpty()) {
                Node childToMutate = root.getChildren().get(random.nextInt(root.getChildren().size()));
                TreeUtils.replaceNode(root, childToMutate, newSubtree);
            }
        } else {
            TreeUtils.replaceNode(root, mutationPoint, newSubtree);
        }
    }

    private List<Function> parseFunctionSet(String functionSetNames) {
        if (functionSetNames == null || functionSetNames.isBlank()) {
            throw new IllegalArgumentException("Function set names string is missing or empty.");
        }
        Map<String, Function> allRealFunctions = RealValuedPrimitives.getFunctionMap();
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
        if (terminalConfs == null) return terminals;

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
