package com.knezevic.edaf.v3.models.discrete.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.models.discrete.BmdaModel;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin factory for BMDA scaffold model.
 */
public final class BmdaModelPlugin implements ModelPlugin<BitString> {

    @Override
    public String type() {
        return "bmda";
    }

    @Override
    public String description() {
        return "BMDA scaffold with TODO for dependency graph learning";
    }

    @Override
    public BmdaModel create(Map<String, Object> params) {
        return new BmdaModel();
    }
}
