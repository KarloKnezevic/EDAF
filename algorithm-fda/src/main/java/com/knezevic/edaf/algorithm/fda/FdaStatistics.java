package com.knezevic.edaf.algorithm.fda;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.SimplePopulation;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;

import java.util.*;

/**
 * Statistics component for the Factorized Distribution Algorithm (FDA).
 * 
 * This implementation uses a Bayesian network to model dependencies between variables.
 * The network structure is learned using a greedy parent selection algorithm based on
 * mutual information.
 */
public class FdaStatistics implements Statistics<BinaryIndividual> {

    private final int length;
    private final Random random;
    private OptimizationType optimizationType = OptimizationType.min;
    
    // Bayesian network structure: parent[i] contains the list of parent indices for variable i
    private List<List<Integer>> parents;
    
    // Conditional probability tables
    // cpt[i][parentCombination] = P(X_i = 1 | parents = parentCombination)
    private Map<Integer, double[]> cpt;
    
    // Marginal probabilities for variables with no parents
    private double[] marginals;

    public FdaStatistics(int length, Random random) {
        this.length = length;
        this.random = random;
        this.parents = new ArrayList<>();
        this.cpt = new HashMap<>();
        this.marginals = new double[length];
        
        for (int i = 0; i < length; i++) {
            parents.add(new ArrayList<>());
        }
    }

    @Override
    public void estimate(Population<BinaryIndividual> population) {
        if (population.getSize() == 0) {
            return;
        }
        optimizationType = population.getOptimizationType();

        // 1. Calculate univariate marginal probabilities
        double[] px = new double[length];
        for (int i = 0; i < length; i++) {
            int count = 0;
            for (BinaryIndividual individual : population) {
                count += individual.getGenotype()[i];
            }
            px[i] = (double) count / population.getSize();
        }

        // 2. Calculate pairwise mutual information
        double[][] mi = calculateMutualInformation(population, px);

        // 3. Learn Bayesian network structure using greedy parent selection
        learnBayesianNetwork(mi, px, population);

        // 4. Estimate conditional probability tables
        estimateCPTs(population, px);
    }

    private double[][] calculateMutualInformation(Population<BinaryIndividual> population, double[] px) {
        double[][] mi = new double[length][length];
        
        // Calculate joint probabilities
        int[][][] counts = new int[length][length][4]; // [00, 01, 10, 11]
        
        for (BinaryIndividual individual : population) {
            byte[] genotype = individual.getGenotype();
            for (int i = 0; i < length; i++) {
                for (int j = i + 1; j < length; j++) {
                    int x = genotype[i];
                    int y = genotype[j];
                    int idx = (x << 1) | y;
                    counts[i][j][idx]++;
                }
            }
        }

        // Calculate mutual information
        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j < length; j++) {
                double mutualInfo = 0.0;
                double p_i = px[i];
                double p_j = px[j];
                int size = population.getSize();

                for (int k = 0; k < 4; k++) {
                    double p_xy = (double) counts[i][j][k] / size;
                    if (p_xy > 0) {
                        double p_x, p_y;
                        if (k == 0) { p_x = 1 - p_i; p_y = 1 - p_j; }
                        else if (k == 1) { p_x = 1 - p_i; p_y = p_j; }
                        else if (k == 2) { p_x = p_i; p_y = 1 - p_j; }
                        else { p_x = p_i; p_y = p_j; }
                        
                        if (p_x > 0 && p_y > 0) {
                            mutualInfo += p_xy * Math.log(p_xy / (p_x * p_y));
                        }
                    }
                }
                
                if (Double.isNaN(mutualInfo) || Double.isInfinite(mutualInfo)) {
                    mutualInfo = 0.0;
                }
                mi[i][j] = mi[j][i] = mutualInfo;
            }
        }

        return mi;
    }

    private void learnBayesianNetwork(double[][] mi, double[] px, Population<BinaryIndividual> population) {
        // Initialize: clear all parent lists
        for (int i = 0; i < length; i++) {
            parents.get(i).clear();
        }
        cpt.clear();

        // Greedy parent selection: for each variable, add the variable with highest MI as parent
        // Limit to maximum of 2 parents per variable to keep complexity manageable
        final int maxParents = 2;
        
        boolean[][] hasEdge = new boolean[length][length];
        
        for (int i = 0; i < length; i++) {
            List<Integer> candidateParents = new ArrayList<>();
            
            // Find candidate parents (variables with high MI)
            for (int j = 0; j < length; j++) {
                if (i != j && mi[i][j] > 0) {
                    candidateParents.add(j);
                }
            }
            
            // Sort by MI (descending)
            final int idxI = i;
            candidateParents.sort((a, b) -> Double.compare(mi[idxI][b], mi[idxI][a]));
            
            // Add up to maxParents parents
            int added = 0;
            for (int candidate : candidateParents) {
                if (added >= maxParents) break;
                // Check for cycles (simple check: don't add if candidate already has i as ancestor)
                if (!wouldCreateCycle(i, candidate, hasEdge)) {
                    parents.get(i).add(candidate);
                    hasEdge[candidate][i] = true;
                    added++;
                }
            }
        }
    }

    private boolean wouldCreateCycle(int target, int candidate, boolean[][] hasEdge) {
        // Simple cycle detection: use DFS to check if target is reachable from candidate
        boolean[] visited = new boolean[length];
        return dfs(candidate, target, hasEdge, visited);
    }

    private boolean dfs(int current, int target, boolean[][] hasEdge, boolean[] visited) {
        if (current == target) return true;
        if (visited[current]) return false;
        visited[current] = true;
        
        for (int i = 0; i < length; i++) {
            if (hasEdge[current][i] && dfs(i, target, hasEdge, visited)) {
                return true;
            }
        }
        return false;
    }

    private void estimateCPTs(Population<BinaryIndividual> population, double[] px) {
        // Initialize marginals
        System.arraycopy(px, 0, marginals, 0, length);

        for (int i = 0; i < length; i++) {
            List<Integer> parentList = parents.get(i);
            
            if (parentList.isEmpty()) {
                // No parents: use marginal probability
                continue;
            }
            
            // Build conditional probability table
            int numParentCombinations = 1 << parentList.size(); // 2^k for k parents
            double[] cptTable = new double[numParentCombinations];
            
            // Count combinations with Laplace smoothing
            int[] counts = new int[numParentCombinations];
            int[] total = new int[numParentCombinations];
            
            for (BinaryIndividual individual : population) {
                byte[] genotype = individual.getGenotype();
                
                // Encode parent combination
                int parentCombo = 0;
                for (int p = 0; p < parentList.size(); p++) {
                    int parentIdx = parentList.get(p);
                    if (genotype[parentIdx] == 1) {
                        parentCombo |= (1 << p);
                    }
                }
                
                total[parentCombo]++;
                if (genotype[i] == 1) {
                    counts[parentCombo]++;
                }
            }
            
            // Estimate probabilities with Laplace smoothing
            for (int combo = 0; combo < numParentCombinations; combo++) {
                cptTable[combo] = (double) (counts[combo] + 1) / (total[combo] + 2);
            }
            
            cpt.put(i, cptTable);
        }
    }

    @Override
    public void update(BinaryIndividual individual, double learningRate) {
        // Incremental update: blend with current CPT
        // This is a simplified update - full implementation would update all CPTs
        byte[] genotype = individual.getGenotype();
        
        for (int i = 0; i < length; i++) {
            if (parents.get(i).isEmpty()) {
                // Update marginal
                marginals[i] = (1 - learningRate) * marginals[i] + learningRate * genotype[i];
            } else {
                // Update CPT entry for this parent combination
                List<Integer> parentList = parents.get(i);
                int parentCombo = 0;
                for (int p = 0; p < parentList.size(); p++) {
                    int parentIdx = parentList.get(p);
                    if (genotype[parentIdx] == 1) {
                        parentCombo |= (1 << p);
                    }
                }
                
                double[] cptTable = cpt.get(i);
                if (cptTable != null) {
                    double currentProb = cptTable[parentCombo];
                    double newValue = genotype[i];
                    cptTable[parentCombo] = (1 - learningRate) * currentProb + learningRate * newValue;
                }
            }
        }
    }

    @Override
    public Population<BinaryIndividual> sample(int size) {
        Population<BinaryIndividual> newPopulation = new SimplePopulation<>(optimizationType);
        
        for (int i = 0; i < size; i++) {
            byte[] genotype = new byte[length];
            
            // Topological sort for sampling order
            List<Integer> samplingOrder = getTopologicalOrder();
            
            for (int var : samplingOrder) {
                List<Integer> parentList = parents.get(var);
                
                if (parentList.isEmpty()) {
                    // Sample from marginal
                    genotype[var] = random.nextDouble() < marginals[var] ? (byte) 1 : (byte) 0;
                } else {
                    // Sample from conditional distribution
                    int parentCombo = 0;
                    for (int p = 0; p < parentList.size(); p++) {
                        int parentIdx = parentList.get(p);
                        if (genotype[parentIdx] == 1) {
                            parentCombo |= (1 << p);
                        }
                    }
                    
                    double[] cptTable = cpt.get(var);
                    if (cptTable != null) {
                        double prob = cptTable[parentCombo];
                        genotype[var] = random.nextDouble() < prob ? (byte) 1 : (byte) 0;
                    } else {
                        // Fallback to marginal
                        genotype[var] = random.nextDouble() < marginals[var] ? (byte) 1 : (byte) 0;
                    }
                }
            }
            
            newPopulation.add(new BinaryIndividual(genotype));
        }
        
        return newPopulation;
    }

    private List<Integer> getTopologicalOrder() {
        // Simple topological sort: variables with no parents first
        List<Integer> order = new ArrayList<>();
        boolean[] visited = new boolean[length];
        
        // Add variables with no parents first
        for (int i = 0; i < length; i++) {
            if (parents.get(i).isEmpty() && !visited[i]) {
                order.add(i);
                visited[i] = true;
            }
        }
        
        // Add remaining variables (simplified - assumes no cycles)
        for (int i = 0; i < length; i++) {
            if (!visited[i]) {
                order.add(i);
                visited[i] = true;
            }
        }
        
        return order;
    }
}

