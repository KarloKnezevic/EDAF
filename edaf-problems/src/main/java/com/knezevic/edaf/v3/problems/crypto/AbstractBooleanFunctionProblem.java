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

    @Override
    public ObjectiveSense objectiveSense() {
        return ObjectiveSense.MAXIMIZE;
    }

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

    protected final VectorFitness evaluateVectorFitness(int[] truthTable, double[] scalarWeights) {
        BooleanFunctionStats stats = BooleanFunctionStats.of(n, truthTable);
        double[] objectiveValues = objectiveValues(stats);
        return new VectorFitness(objectiveValues, scalarWeights);
    }

    protected final double[] objectiveValues(BooleanFunctionStats stats) {
        double[] values = new double[criteria.size()];
        for (int i = 0; i < criteria.size(); i++) {
            values[i] = criteria.get(i).score(stats);
        }
        return values;
    }

    protected final int[] toTruthTableFromBits(boolean[] bits) {
        int[] table = new int[truthTableSize];
        int limit = Math.min(bits.length, table.length);
        for (int i = 0; i < limit; i++) {
            table[i] = bits[i] ? 1 : 0;
        }
        return table;
    }

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

    @Override
    public int objectiveCount() {
        return criteria.size();
    }

    public int variableCount() {
        return n;
    }

    public int truthTableSize() {
        return truthTableSize;
    }

    public List<String> criterionIds() {
        return Collections.unmodifiableList(criterionIds);
    }

    public abstract Fitness evaluate(G genotype);
}
