package com.knezevic.edaf.v3.problems.plugins.discrete;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.problems.discrete.KnapsackProblem;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.List;
import java.util.Map;

/**
 * Plugin factory for 0/1 knapsack.
 */
public final class KnapsackProblemPlugin implements ProblemPlugin<BitString> {

    @Override
    public String type() {
        return "knapsack";
    }

    @Override
    public String description() {
        return "0/1 knapsack with configurable item set";
    }

    @Override
    public KnapsackProblem create(Map<String, Object> params) {
        List<Object> weightsRaw = Params.list(params, "weights");
        List<Object> valuesRaw = Params.list(params, "values");
        int[] weights;
        int[] values;

        if (weightsRaw.isEmpty() || valuesRaw.isEmpty()) {
            weights = defaultWeights();
            values = defaultValues();
        } else {
            if (weightsRaw.size() != valuesRaw.size()) {
                throw new IllegalArgumentException("problem.weights and problem.values must have equal length");
            }
            weights = toIntArray(weightsRaw);
            values = toIntArray(valuesRaw);
        }

        int defaultCapacity = (int) Math.round(java.util.Arrays.stream(weights).sum() * 0.4);
        int capacity = Params.integer(params, "capacity", defaultCapacity);
        double penalty = Params.dbl(params, "penaltyPerUnit", 5.0);
        return new KnapsackProblem(weights, values, capacity, penalty);
    }

    private static int[] toIntArray(List<Object> raw) {
        int[] out = new int[raw.size()];
        for (int i = 0; i < raw.size(); i++) {
            out[i] = Integer.parseInt(String.valueOf(raw.get(i)));
        }
        return out;
    }

    private static int[] defaultWeights() {
        return new int[]{12, 7, 11, 8, 9, 5, 14, 6, 7, 3, 4, 10, 13, 9, 8, 15, 6, 5, 12, 11};
    }

    private static int[] defaultValues() {
        return new int[]{24, 13, 23, 15, 16, 11, 28, 12, 15, 7, 9, 18, 25, 17, 14, 29, 10, 8, 21, 20};
    }
}
