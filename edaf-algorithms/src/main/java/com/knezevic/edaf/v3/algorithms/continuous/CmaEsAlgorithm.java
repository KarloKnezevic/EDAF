package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * CMA-ES strategy driver
 */
public final class CmaEsAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    public CmaEsAlgorithm(double selectionRatio) {
        super("cma-es", selectionRatio);
    }
}
