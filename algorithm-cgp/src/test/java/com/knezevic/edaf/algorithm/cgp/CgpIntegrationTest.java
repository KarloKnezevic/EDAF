package com.knezevic.edaf.algorithm.cgp;

import com.knezevic.edaf.algorithm.cgp.operator.CgpCrossoverOperator;
import com.knezevic.edaf.algorithm.cgp.operator.CgpMutationOperator;
import com.knezevic.edaf.core.api.Algorithm;
import com.knezevic.edaf.core.api.Population;
import com.knezevic.edaf.core.api.Selection;
import com.knezevic.edaf.core.api.TerminationCondition;
import com.knezevic.edaf.core.impl.MaxGenerations;
import com.knezevic.edaf.genotype.tree.primitives.Function;
import com.knezevic.edaf.selection.TournamentSelection;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CgpIntegrationTest {

    @Test
    void testCgpOnSymbolicRegression() {
        // 1. Create dependencies manually
        CgpConfig config = new CgpConfig();
        config.setPopulationSize(50);
        config.setGenerations(200);
        config.setMutationRate(0.05);
        config.setRows(1);
        config.setCols(20);
        config.setLevelsBack(10);
        config.setUseCrossover(false);

        Random random = new Random(42);
        SymbolicRegressionProblem problem = new SymbolicRegressionProblem();

        List<Function> functionSet = List.of(
            new Function("ADD", 2, args -> args[0] + args[1]),
            new Function("SUB", 2, args -> args[0] - args[1]),
            new Function("MUL", 2, args -> args[0] * args[1]),
            new Function("DIV", 2, args -> {
                if (Math.abs(args[1]) < 1e-6) return 1.0; // Protected division
                return args[0] / args[1];
            })
        );

        CgpDecoder decoder = new CgpDecoder(config, functionSet, problem.getNumInputs(), problem.getNumOutputs());
        CgpGenotypeFactory genotypeFactory = new CgpGenotypeFactory(config, functionSet, problem.getNumInputs(), problem.getNumOutputs(), random);

        CgpMutationOperator mutation = new CgpMutationOperator(config, functionSet, problem.getNumInputs(), problem.getNumOutputs(), random);
        CgpCrossoverOperator crossover = new CgpCrossoverOperator(random);
        Selection<CgpIndividual> selection = new TournamentSelection<>(random, 5);
        TerminationCondition<CgpIndividual> terminationCondition = new MaxGenerations<>(config.getGenerations());

        // 2. Create algorithm instance
        CgpAlgorithm algorithm = new CgpAlgorithm(config, problem, decoder, genotypeFactory, selection, mutation, crossover, random, terminationCondition);

        algorithm.setProgressListener((generation, bestInGeneration, population) -> {
            // System.out.println("Gen: " + generation + ", Fitness: " + bestInGeneration.getFitness());
        });

        // 3. Run algorithm
        algorithm.run();

        // 4. Assert results
        CgpIndividual best = algorithm.getBest();
        assertNotNull(best);
        assertTrue(best.getFitness() < 0.1, "Fitness should be less than 0.1, but was " + best.getFitness());
    }
}
