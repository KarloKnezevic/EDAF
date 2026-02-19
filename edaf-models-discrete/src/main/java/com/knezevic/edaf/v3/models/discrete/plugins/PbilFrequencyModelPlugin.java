package com.knezevic.edaf.v3.models.discrete.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.discrete.PbilFrequencyModel;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin factory for PBIL frequency model.
 */
public final class PbilFrequencyModelPlugin implements ModelPlugin<BitString> {

    @Override
    public String type() {
        return "pbil-frequency";
    }

    @Override
    public String description() {
        return "PBIL moving-average frequency model";
    }

    @Override
    public PbilFrequencyModel create(Map<String, Object> params) {
        return new PbilFrequencyModel(Params.dbl(params, "learningRate", 0.1));
    }
}
