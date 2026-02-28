/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Dedicated flow-inspired continuous EDA driver using non-linear density transforms.
 *
 * <p>Typical paired model draws latent Gaussian samples and applies a parameterized map:
 * <pre>
 *   z ~ N(0, I),   x = f_θ(z)
 * </pre>
 * where {@code f_θ} is updated from elite statistics.
 *
 * <p>References:
 * <ol>
 *   <li>D. J. Rezende and S. Mohamed, "Variational inference with normalizing flows,"
 *   ICML, 2015.</li>
 *   <li>D. Wierstra et al., "Natural evolution strategies," JMLR, 2014.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class FlowEdaAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    /**
     * Creates a new FlowEdaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public FlowEdaAlgorithm(double selectionRatio) {
        super("flow-eda", selectionRatio);
    }
}
