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
 * BMDA scaffold model with univariate fallback.
 *
 * TODO(priority=high): Replace fallback fit/sample with full bivariate dependency learning and conditional sampling.
 */
public final class BmdaModel implements Model<BitString> {

    private final BernoulliUmdaModel fallback = new BernoulliUmdaModel(0.01);

    @Override
    public String name() {
        return "bmda";
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
        metrics.put("bmda_dependency_edges", 0.0);
        return new ModelDiagnostics(metrics);
    }
}
