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
 * BOA/EBNA scaffold model with univariate fallback.
 *
 * TODO(priority=high): Implement full Bayesian network structure learning and ancestral sampling.
 */
public final class BoaEbnaModel implements Model<BitString> {

    private final BernoulliUmdaModel fallback = new BernoulliUmdaModel(0.01);

    @Override
    public String name() {
        return "boa-ebna";
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
        metrics.put("boa_network_edges", 0.0);
        return new ModelDiagnostics(metrics);
    }
}
