package com.knezevic.edaf.genotype.permutation;

import com.knezevic.edaf.core.api.Genotype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A factory for creating permutation genotypes.
 */
public class PermutationGenotype implements Genotype<int[]> {

    private final int length;
    private final Random random;

    public PermutationGenotype(int length, Random random) {
        this.length = length;
        this.random = random;
    }

    @Override
    public int[] create() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            list.add(i);
        }
        Collections.shuffle(list, random);
        int[] genotype = new int[length];
        for (int i = 0; i < length; i++) {
            genotype[i] = list.get(i);
        }
        return genotype;
    }

    @Override
    public int getLength() {
        return length;
    }
}
