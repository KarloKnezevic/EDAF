package com.knezevic.edaf.v3.models.discrete.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.discrete.MimicChowLiuModel;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin factory for MIMIC Chow-Liu dependency model.
 */
public final class MimicModelPlugin implements ModelPlugin<BitString> {

    @Override
    public String type() {
        return "mimic-chow-liu";
    }

    @Override
    public String description() {
        return "MIMIC Chow-Liu model with tree-conditional sampling";
    }

    @Override
    public MimicChowLiuModel create(Map<String, Object> params) {
        return new MimicChowLiuModel(Params.dbl(params, "smoothing", 0.5));
    }
}
