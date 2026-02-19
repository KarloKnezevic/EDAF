package com.knezevic.edaf.v3.repr.types;

import java.util.Arrays;

/**
 * Categorical genotype where each position stores one symbol.
 */
public record CategoricalVector(String[] categories) {

    public CategoricalVector {
        categories = Arrays.copyOf(categories, categories.length);
    }

    public int length() {
        return categories.length;
    }

    @Override
    public String toString() {
        return Arrays.toString(categories);
    }
}
