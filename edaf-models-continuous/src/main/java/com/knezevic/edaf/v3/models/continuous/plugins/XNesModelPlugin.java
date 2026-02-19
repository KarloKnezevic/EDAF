package com.knezevic.edaf.v3.models.continuous.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.models.continuous.XNesModel;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for xNES scaffold model.
 */
public final class XNesModelPlugin implements ModelPlugin<RealVector> {

    @Override
    public String type() {
        return "xnes";
    }

    @Override
    public String description() {
        return "xNES strategy scaffold";
    }

    @Override
    public XNesModel create(Map<String, Object> params) {
        return new XNesModel();
    }
}
