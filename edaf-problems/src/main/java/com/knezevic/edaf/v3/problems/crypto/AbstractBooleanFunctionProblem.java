/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.crypto;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.core.api.VectorFitness;
import com.knezevic.edaf.v3.problems.crypto.criteria.CryptoFitnessCriterion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Shared base for cryptographic boolean-function optimization problems.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public abstract class AbstractBooleanFunctionProblem<G> implements Problem<G> {

    protected final int n;
    protected final int truthTableSize;
    protected final List<CryptoFitnessCriterion> criteria;
    protected final List<String> criterionIds;
    protected final double[] criterionWeights;
    protected final double criterionWeightSum;

    protected AbstractBooleanFunctionProblem(int n,
                                             List<String> criterionIds,
                                             Map<String, Double> criterionWeights) {
        if (n < 2 || n > 12) {
            throw new IllegalArgumentException("Boolean-function variable count n must be in [2,12], got " + n);
        }
        this.n = n;
        this.truthTableSize = 1 << n;

        List<String> ids = criterionIds == null || criterionIds.isEmpty()
                ? List.of("balancedness", "nonlinearity", "algebraic-degree")
                : criterionIds;

        this.criteria = new ArrayList<>(ids.size());
        this.criterionIds = new ArrayList<>(ids.size());
        this.criterionWeights = new double[ids.size()];

        double sum = 0.0;
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            CryptoFitnessCriterion criterion = CryptoCriteriaFactory.create(id);
            this.criteria.add(criterion);
            this.criterionIds.add(criterion.id());

            double weight = criterionWeights == null
                    ? 1.0
                    : criterionWeights.getOrDefault(criterion.id(), criterionWeights.getOrDefault(id, 1.0));
            weight = Math.max(0.0, weight);
            this.criterionWeights[i] = weight;
            sum += weight;
        }

        this.criterionWeightSum = sum <= 0.0 ? 1.0 : sum;
    }

    /**
     * Returns objective optimization sense.
     *
     * @return objective sense
     */
    @Override
    public ObjectiveSense objectiveSense() {
        return ObjectiveSense.MAXIMIZE;
    }

    /**
     * Executes evaluate scalar fitness.
     *
     * @param truthTable boolean function truth table
     * @return the evaluate scalar fitness
     */
    protected final ScalarFitness evaluateScalarFitness(int[] truthTable) {
        BooleanFunctionStats stats = BooleanFunctionStats.of(n, truthTable);
        double[] objectiveValues = objectiveValues(stats);

        double scalar = 0.0;
        for (int i = 0; i < objectiveValues.length; i++) {
            scalar += criterionWeights[i] * objectiveValues[i];
        }
        scalar /= criterionWeightSum;
        return new ScalarFitness(scalar);
    }

    /**
     * Executes evaluate vector fitness.
     *
     * @param truthTable boolean function truth table
     * @param scalarWeights the scalarWeights argument
     * @return the evaluate vector fitness
     */
    protected final VectorFitness evaluateVectorFitness(int[] truthTable, double[] scalarWeights) {
        BooleanFunctionStats stats = BooleanFunctionStats.of(n, truthTable);
        double[] objectiveValues = objectiveValues(stats);
        return new VectorFitness(objectiveValues, scalarWeights);
    }

    /**
     * Executes objective values.
     *
     * @param stats the stats argument
     * @return the objective values
     */
    protected final double[] objectiveValues(BooleanFunctionStats stats) {
        double[] values = new double[criteria.size()];
        for (int i = 0; i < criteria.size(); i++) {
            values[i] = criteria.get(i).score(stats);
        }
        return values;
    }

    /**
     * Converts to truth table from bits.
     *
     * @param bits the bits argument
     * @return the truth table from bits representation
     */
    protected final int[] toTruthTableFromBits(boolean[] bits) {
        int[] table = new int[truthTableSize];
        int limit = Math.min(bits.length, table.length);
        for (int i = 0; i < limit; i++) {
            table[i] = bits[i] ? 1 : 0;
        }
        return table;
    }

    /**
     * Converts to truth table from balanced permutation.
     *
     * @param permutation the permutation argument
     * @return the truth table from balanced permutation representation
     */
    protected final int[] toTruthTableFromBalancedPermutation(int[] permutation) {
        int[] table = new int[truthTableSize];
        int onesCount = truthTableSize / 2;
        int limit = Math.min(onesCount, permutation.length);
        for (int i = 0; i < limit; i++) {
            int position = permutation[i];
            if (position >= 0 && position < truthTableSize) {
                table[position] = 1;
            }
        }
        return table;
    }

    /**
     * Returns number of objectives.
     *
     * @return objective count
     */
    @Override
    public int objectiveCount() {
        return criteria.size();
    }

    /**
     * Executes variable count.
     *
     * @return the computed variable count
     */
    public int variableCount() {
        return n;
    }

    /**
     * Executes truth table size.
     *
     * @return the computed truth table size
     */
    public int truthTableSize() {
        return truthTableSize;
    }

    /**
     * Executes criterion ids.
     *
     * @return the criterion ids
     */
    public List<String> criterionIds() {
        return Collections.unmodifiableList(criterionIds);
    }

    /**
     * Evaluates candidate solution.
     *
     * @param genotype candidate genotype
     * @return fitness value
     */
    public abstract Fitness evaluate(G genotype);
}
