package com.knezevic.edaf.v3.models.permutation.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.models.permutation.MallowsModel;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.Map;

/**
 * Plugin factory for Mallows scaffold model.
 */
public final class MallowsModelPlugin implements ModelPlugin<PermutationVector> {

    @Override
    public String type() {
        return "mallows";
    }

    @Override
    public String description() {
        return "Mallows scaffold model";
    }

    @Override
    public MallowsModel create(Map<String, Object> params) {
        return new MallowsModel();
    }
}
