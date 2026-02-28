/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.types;

import java.util.Arrays;

/**
 * Categorical genotype where each position stores one symbol.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record CategoricalVector(String[] categories) {

    public CategoricalVector {
        categories = Arrays.copyOf(categories, categories.length);
    }

    /**
     * Executes length.
     *
     * @return the computed length
     */
    public int length() {
        return categories.length;
    }

    /**
     * Converts to string.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return Arrays.toString(categories);
    }
}
