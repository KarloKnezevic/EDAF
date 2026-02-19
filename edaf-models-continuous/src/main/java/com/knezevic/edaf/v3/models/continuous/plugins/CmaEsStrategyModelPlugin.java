package com.knezevic.edaf.v3.models.continuous.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.models.continuous.CmaEsStrategyModel;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for CMA-ES strategy scaffold model.
 */
public final class CmaEsStrategyModelPlugin implements ModelPlugin<RealVector> {

    @Override
    public String type() {
        return "cma-es";
    }

    @Override
    public String description() {
        return "CMA-ES strategy scaffold";
    }

    @Override
    public CmaEsStrategyModel create(Map<String, Object> params) {
        return new CmaEsStrategyModel();
    }
}
