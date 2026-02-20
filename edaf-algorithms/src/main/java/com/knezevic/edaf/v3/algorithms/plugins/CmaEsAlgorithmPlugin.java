package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Plugin for CMA-ES driver.
 */
public final class CmaEsAlgorithmPlugin extends BaseRatioAlgorithmPlugin<RealVector> {

    @Override
    public String type() {
        return "cma-es";
    }

    @Override
    public String description() {
        return "CMA-ES strategy driver";
    }
}
