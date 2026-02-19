package com.knezevic.edaf.v3.models.continuous.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.continuous.KdeModel;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for KDE scaffold model.
 */
public final class KdeModelPlugin implements ModelPlugin<RealVector> {

    @Override
    public String type() {
        return "kde";
    }

    @Override
    public String description() {
        return "Kernel density estimator scaffold";
    }

    @Override
    public KdeModel create(Map<String, Object> params) {
        return new KdeModel(Params.dbl(params, "bandwidth", 0.5));
    }
}
