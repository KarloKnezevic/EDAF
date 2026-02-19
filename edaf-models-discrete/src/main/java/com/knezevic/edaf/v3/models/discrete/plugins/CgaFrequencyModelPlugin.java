package com.knezevic.edaf.v3.models.discrete.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.discrete.CompactGaModel;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin factory for compact GA model.
 */
public final class CgaFrequencyModelPlugin implements ModelPlugin<BitString> {

    @Override
    public String type() {
        return "cga-frequency";
    }

    @Override
    public String description() {
        return "Compact GA probability-vector model";
    }

    @Override
    public CompactGaModel create(Map<String, Object> params) {
        return new CompactGaModel(Params.dbl(params, "step", 0.02));
    }
}
