package com.knezevic.edaf.examples.misc;

import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.genotype.permutation.PermutationIndividual;

/**
 * Implements the N-Queens problem.
 * The goal is to place N chess queens on an N×N chessboard so that no two queens threaten each other.
 * The genotype is a permutation of integers {0, 1, ..., N-1}, where the index of the
 * array represents the row and the value at that index represents the column.
 * This representation inherently prevents row and column conflicts.
 * The fitness function therefore only needs to count the number of diagonal conflicts.
 * A fitness of 0 represents a perfect solution.
 */
public class NQueensProblem implements Problem<PermutationIndividual> {

    @Override
    public void evaluate(PermutationIndividual individual) {
        int[] queens = individual.getGenotype();
        int n = queens.length;
        int conflicts = 0;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                // Check for diagonal conflicts.
                // Two queens (i, queens[i]) and (j, queens[j]) are on the same diagonal
                // if the absolute difference of their rows is equal to the absolute
                // difference of their columns.
                if (Math.abs(i - j) == Math.abs(queens[i] - queens[j])) {
                    conflicts++;
                }
            }
        }
        individual.setFitness(conflicts);
    }
}
