package com.knezevic.edaf.core.spi;

import com.knezevic.edaf.core.api.Genotype;

/**
 * SPI for discovering genotype families (binary, integer, floating point, tree, permutation, etc.).
 */
public interface GenotypeProvider {

    /**
     * Unique identifier used in configuration (e.g. "binary", "integer", "fp", "tree", "perm").
     */
    String id();

    /**
     * The root type of the genotype this provider supports.
     */
    Class<? extends Genotype> genotypeType();
}


