package com.knezevic.edaf.v3.models.discrete.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.models.discrete.MimicChowLiuModel;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin factory for MIMIC scaffold model.
 */
public final class MimicModelPlugin implements ModelPlugin<BitString> {

    @Override
    public String type() {
        return "mimic-chow-liu";
    }

    @Override
    public String description() {
        return "MIMIC Chow-Liu scaffold model";
    }

    @Override
    public MimicChowLiuModel create(Map<String, Object> params) {
        return new MimicChowLiuModel();
    }
}
