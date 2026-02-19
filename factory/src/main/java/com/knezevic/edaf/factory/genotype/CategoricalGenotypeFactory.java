package com.knezevic.edaf.factory.genotype;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Genotype;
import com.knezevic.edaf.genotype.categorical.CategoricalGenotype;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A factory for creating {@link CategoricalGenotype} objects.
 */
public class CategoricalGenotypeFactory implements GenotypeFactory {
    @Override
    public Genotype create(Configuration config, Random random) throws Exception {
        int length = config.getProblem().getGenotype().getLength();
        // Try to read cardinalities from problem parameters
        Map<String, Object> params = config.getProblem().getParameters();
        int[] cardinalities;
        if (params != null && params.containsKey("cardinalities")) {
            Object cardObj = params.get("cardinalities");
            if (cardObj instanceof List<?> list) {
                cardinalities = new int[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    cardinalities[i] = ((Number) list.get(i)).intValue();
                }
            } else {
                // Default: each gene has 2 categories (binary-like)
                cardinalities = new int[length];
                Arrays.fill(cardinalities, 2);
            }
        } else {
            cardinalities = new int[length];
            Arrays.fill(cardinalities, 2);
        }
        return new CategoricalGenotype(cardinalities, random);
    }
}
