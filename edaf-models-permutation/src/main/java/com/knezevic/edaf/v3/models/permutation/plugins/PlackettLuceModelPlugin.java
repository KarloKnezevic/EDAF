package com.knezevic.edaf.v3.models.permutation.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.models.permutation.PlackettLuceModel;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.Map;

/**
 * Plugin factory for Plackett-Luce permutation model.
 */
public final class PlackettLuceModelPlugin implements ModelPlugin<PermutationVector> {

    @Override
    public String type() {
        return "plackett-luce";
    }

    @Override
    public String description() {
        return "Plackett-Luce permutation model";
    }

    @Override
    public PlackettLuceModel create(Map<String, Object> params) {
        return new PlackettLuceModel();
    }
}
