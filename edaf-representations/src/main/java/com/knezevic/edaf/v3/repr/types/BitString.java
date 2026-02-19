package com.knezevic.edaf.v3.repr.types;

import java.util.Arrays;

/**
 * Fixed-length binary vector genotype.
 */
public record BitString(boolean[] genes) {

    public BitString {
        genes = Arrays.copyOf(genes, genes.length);
    }

    public int length() {
        return genes.length;
    }

    public int ones() {
        int count = 0;
        for (boolean gene : genes) {
            if (gene) {
                count++;
            }
        }
        return count;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(genes.length);
        for (boolean gene : genes) {
            sb.append(gene ? '1' : '0');
        }
        return sb.toString();
    }
}
