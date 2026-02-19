package com.knezevic.edaf.v3.models.continuous.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.models.continuous.CopulaBaselineModel;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for copula baseline scaffold model.
 */
public final class CopulaBaselineModelPlugin implements ModelPlugin<RealVector> {

    @Override
    public String type() {
        return "copula-baseline";
    }

    @Override
    public String description() {
        return "Copula baseline scaffold model";
    }

    @Override
    public CopulaBaselineModel create(Map<String, Object> params) {
        return new CopulaBaselineModel();
    }
}
