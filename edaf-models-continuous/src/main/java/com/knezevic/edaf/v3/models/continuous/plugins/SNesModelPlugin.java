package com.knezevic.edaf.v3.models.continuous.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.models.continuous.SNesModel;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for sNES scaffold model.
 */
public final class SNesModelPlugin implements ModelPlugin<RealVector> {

    @Override
    public String type() {
        return "snes";
    }

    @Override
    public String description() {
        return "Separable NES strategy scaffold";
    }

    @Override
    public SNesModel create(Map<String, Object> params) {
        return new SNesModel();
    }
}
