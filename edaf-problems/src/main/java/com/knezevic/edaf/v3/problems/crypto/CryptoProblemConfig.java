package com.knezevic.edaf.v3.problems.crypto;

import com.knezevic.edaf.v3.core.util.Params;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parsed configuration payload for crypto boolean-function problems.
 */
public record CryptoProblemConfig(
        int n,
        List<String> criteria,
        Map<String, Double> criterionWeights,
        double[] objectiveWeights,
        int maxDepth
) {

    /**
     * Parses common crypto-problem parameters from plugin params map.
     */
    public static CryptoProblemConfig from(Map<String, Object> params) {
        int n = Params.integer(params, "n", 6);

        List<Object> rawCriteria = Params.list(params, "criteria");
        List<String> criteria = new ArrayList<>();
        if (rawCriteria.isEmpty()) {
            criteria.add("balancedness");
            criteria.add("nonlinearity");
            criteria.add("algebraic-degree");
        } else {
            for (Object item : rawCriteria) {
                criteria.add(String.valueOf(item));
            }
        }

        Map<String, Object> rawWeights = Params.map(params, "criterionWeights");
        Map<String, Double> criterionWeights = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : rawWeights.entrySet()) {
            criterionWeights.put(entry.getKey(), Double.parseDouble(String.valueOf(entry.getValue())));
        }

        List<Object> rawObjectiveWeights = Params.list(params, "objectiveWeights");
        double[] objectiveWeights = new double[rawObjectiveWeights.size()];
        for (int i = 0; i < rawObjectiveWeights.size(); i++) {
            objectiveWeights[i] = Double.parseDouble(String.valueOf(rawObjectiveWeights.get(i)));
        }

        int maxDepth = Params.integer(params, "maxDepth", 10);

        return new CryptoProblemConfig(n, criteria, criterionWeights, objectiveWeights, maxDepth);
    }
}
