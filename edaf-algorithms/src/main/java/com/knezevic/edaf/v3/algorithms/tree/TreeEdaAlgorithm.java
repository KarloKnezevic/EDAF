/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.tree;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;
import com.knezevic.edaf.v3.repr.types.VariableLengthVector;

/**
 * Ratio-based EDA driver for variable-length token tree representations.
 *
 * <p>Typical paired grammar/tree model factorizes production choices:
 * <pre>
 *   p(tree) = Π_n p(rule_n | context_n)
 * </pre>
 * and samples derivation decisions from learned production probabilities.
 *
 * <p>References:
 * <ol>
 *   <li>R. Poli, W. B. Langdon, and N. F. McPhee, "A Field Guide to Genetic Programming,"
 *   2008.</li>
 *   <li>P. Larranaga and J. A. Lozano (eds.), "Estimation of Distribution Algorithms,"
 *   Kluwer, 2001.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class TreeEdaAlgorithm extends RatioBasedEdaAlgorithm<VariableLengthVector<Integer>> {

    /**
     * Creates a new TreeEdaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public TreeEdaAlgorithm(double selectionRatio) {
        super("tree-eda", selectionRatio);
    }
}
