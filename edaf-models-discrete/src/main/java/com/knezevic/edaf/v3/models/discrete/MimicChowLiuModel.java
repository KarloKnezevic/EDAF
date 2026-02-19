package com.knezevic.edaf.v3.models.discrete;

import com.knezevic.edaf.v3.core.api.ConstraintHandling;
import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.Model;
import com.knezevic.edaf.v3.core.api.ModelDiagnostics;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MIMIC (Chow-Liu style) scaffold model with univariate fallback.
 *
 * TODO(priority=high): Implement mutual-information matrix, Chow-Liu tree learning, and chain-conditional sampling.
 */
public final class MimicChowLiuModel implements Model<BitString> {

    private final BernoulliUmdaModel fallback = new BernoulliUmdaModel(0.01);

    @Override
    public String name() {
        return "mimic-chow-liu";
    }

    @Override
    public void fit(List<Individual<BitString>> selected, Representation<BitString> representation, RngStream rng) {
        fallback.fit(selected, representation, rng);
    }

    @Override
    public List<BitString> sample(int count,
                                  Representation<BitString> representation,
                                  Problem<BitString> problem,
                                  ConstraintHandling<BitString> constraintHandling,
                                  RngStream rng) {
        return fallback.sample(count, representation, problem, constraintHandling, rng);
    }

    @Override
    public ModelDiagnostics diagnostics() {
        Map<String, Double> metrics = new LinkedHashMap<>(fallback.diagnostics().numeric());
        metrics.put("mimic_tree_depth", 0.0);
        return new ModelDiagnostics(metrics);
    }
}
