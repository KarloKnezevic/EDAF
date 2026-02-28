/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.types;

import java.util.Arrays;

/**
 * Fixed-length binary vector genotype.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record BitString(boolean[] genes) {

    public BitString {
        genes = Arrays.copyOf(genes, genes.length);
    }

    /**
     * Executes length.
     *
     * @return the computed length
     */
    public int length() {
        return genes.length;
    }

    /**
     * Executes ones.
     *
     * @return the computed ones
     */
    public int ones() {
        int count = 0;
        for (boolean gene : genes) {
            if (gene) {
                count++;
            }
        }
        return count;
    }

    /**
     * Converts to string.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(genes.length);
        for (boolean gene : genes) {
            sb.append(gene ? '1' : '0');
        }
        return sb.toString();
    }
}
