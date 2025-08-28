package hr.fer.zemris.edaf.algorithm.ltga;

import hr.fer.zemris.edaf.core.*;
import hr.fer.zemris.edaf.genotype.binary.BinaryIndividual;

import java.util.*;

/**
 * The Linkage Tree Genetic Algorithm (LTGA).
 */
public class LTGA implements Algorithm<BinaryIndividual> {

    private final Problem<BinaryIndividual> problem;
    private final Population<BinaryIndividual> population;
    private final Selection<BinaryIndividual> selection;
    private final Mutation<BinaryIndividual> mutation;
    private final TerminationCondition<BinaryIndividual> terminationCondition;
    private final int length;
    private final Random random;
    private List<Set<Integer>> tree;

    private BinaryIndividual best;
    private int generation;

    public LTGA(Problem<BinaryIndividual> problem, Population<BinaryIndividual> population,
                Selection<BinaryIndividual> selection, Mutation<BinaryIndividual> mutation,
                TerminationCondition<BinaryIndividual> terminationCondition, int length, Random random) {
        this.problem = problem;
        this.population = population;
        this.selection = selection;
        this.mutation = mutation;
        this.terminationCondition = terminationCondition;
        this.length = length;
        this.random = random;
    }

    @Override
    public void run() {
        // 1. Initialize population
        for (BinaryIndividual individual : population) {
            problem.evaluate(individual);
        }
        population.sort();
        best = (BinaryIndividual) population.getBest().copy();
        generation = 0;

        // 2. Run generations
        while (!terminationCondition.shouldTerminate(this)) {
            // 2.1. Build linkage tree
            buildLinkageTree();

            // 2.2. Generate offspring
            Population<BinaryIndividual> parents = selection.select(population, 2);
            Crossover<BinaryIndividual> gpom = new GpomCrossover(tree, random);
            BinaryIndividual offspring = gpom.crossover(parents.get(0), parents.get(1));
            mutation.mutate(offspring);
            problem.evaluate(offspring);

            // 2.3. Update population
            population.sort();
            BinaryIndividual worst = population.getWorst();
            if (offspring.getFitness() < worst.getFitness()) {
                population.remove(worst);
                population.add(offspring);
            }

            // 2.4. Update best individual
            population.sort();
            BinaryIndividual currentBest = population.getBest();
            if (currentBest.getFitness() < best.getFitness()) {
                best = (BinaryIndividual) currentBest.copy();
            }

            generation++;
        }
    }

    private void buildLinkageTree() {
        // 1. Calculate univariate probabilities
        double[] px = new double[length];
        for (int i = 0; i < length; i++) {
            int count = 0;
            for (BinaryIndividual individual : population) {
                count += individual.getGenotype()[i];
            }
            px[i] = (double) count / population.size();
        }

        // 2. Calculate pairwise joint probabilities
        double[][][] pxy = new double[length][length][4];
        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j < length; j++) {
                int[] counts = new int[4];
                for (BinaryIndividual individual : population) {
                    int x = individual.getGenotype()[i];
                    int y = individual.getGenotype()[j];
                    if (x == 0 && y == 0) counts[0]++;
                    else if (x == 0 && y == 1) counts[1]++;
                    else if (x == 1 && y == 0) counts[2]++;
                    else counts[3]++;
                }
                for (int k = 0; k < 4; k++) {
                    pxy[i][j][k] = (double) counts[k] / population.size();
                }
            }
        }

        // 3. Calculate mutual information
        double[][] mi = new double[length][length];
        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j < length; j++) {
                double mutualInformation = 0;
                if (pxy[i][j][0] > 0) mutualInformation += pxy[i][j][0] * Math.log(pxy[i][j][0] / ((1 - px[i]) * (1 - px[j])));
                if (pxy[i][j][1] > 0) mutualInformation += pxy[i][j][1] * Math.log(pxy[i][j][1] / ((1 - px[i]) * px[j]));
                if (pxy[i][j][2] > 0) mutualInformation += pxy[i][j][2] * Math.log(pxy[i][j][2] / (px[i] * (1 - px[j])));
                if (pxy[i][j][3] > 0) mutualInformation += pxy[i][j][3] * Math.log(pxy[i][j][3] / (px[i] * px[j]));
                mi[i][j] = mi[j][i] = mutualInformation;
            }
        }

        // 4. Build linkage tree using UPGMA
        List<Set<Integer>> clusters = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            Set<Integer> cluster = new HashSet<>();
            cluster.add(i);
            clusters.add(cluster);
        }

        tree = new ArrayList<>(clusters);

        while (clusters.size() > 1) {
            double maxMi = -1;
            int c1 = -1, c2 = -1;
            for (int i = 0; i < clusters.size(); i++) {
                for (int j = i + 1; j < clusters.size(); j++) {
                    double avgMi = 0;
                    int count = 0;
                    for (int v1 : clusters.get(i)) {
                        for (int v2 : clusters.get(j)) {
                            avgMi += mi[v1][v2];
                            count++;
                        }
                    }
                    avgMi /= count;
                    if (avgMi > maxMi) {
                        maxMi = avgMi;
                        c1 = i;
                        c2 = j;
                    }
                }
            }

            Set<Integer> merged = new HashSet<>(clusters.get(c1));
            merged.addAll(clusters.get(c2));
            tree.add(merged);
            clusters.set(c1, merged);
            clusters.remove(c2);
        }
    }

    @Override
    public BinaryIndividual getBest() {
        return best;
    }

    @Override
    public int getGeneration() {
        return generation;
    }

    @Override
    public Population<BinaryIndividual> getPopulation() {
        return population;
    }
}
