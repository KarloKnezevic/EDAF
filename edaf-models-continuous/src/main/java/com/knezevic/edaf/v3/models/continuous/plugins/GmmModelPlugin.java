package com.knezevic.edaf.v3.models.continuous.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.continuous.GmmModel;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for GMM scaffold model.
 */
public final class GmmModelPlugin implements ModelPlugin<RealVector> {

    @Override
    public String type() {
        return "gmm";
    }

    @Override
    public String description() {
        return "Gaussian mixture model scaffold";
    }

    @Override
    public GmmModel create(Map<String, Object> params) {
        return new GmmModel(Params.integer(params, "components", 3));
    }
}
