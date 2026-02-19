package com.knezevic.edaf.v3.models.continuous;

import com.knezevic.edaf.v3.core.api.ConstraintHandling;
import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.Model;
import com.knezevic.edaf.v3.core.api.ModelDiagnostics;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * GMM-EDA scaffold model.
 *
 * TODO(priority=high): Implement multi-component EM fitting and mixture sampling.
 */
public final class GmmModel implements Model<RealVector> {

    private final int components;
    private final DiagonalGaussianModel fallback = new DiagonalGaussianModel(1e-6);

    public GmmModel(int components) {
        this.components = Math.max(1, components);
    }

    @Override
    public String name() {
        return "gmm";
    }

    @Override
    public void fit(List<Individual<RealVector>> selected, Representation<RealVector> representation, RngStream rng) {
        fallback.fit(selected, representation, rng);
    }

    @Override
    public List<RealVector> sample(int count,
                                   Representation<RealVector> representation,
                                   Problem<RealVector> problem,
                                   ConstraintHandling<RealVector> constraintHandling,
                                   RngStream rng) {
        return fallback.sample(count, representation, problem, constraintHandling, rng);
    }

    @Override
    public ModelDiagnostics diagnostics() {
        Map<String, Double> values = new LinkedHashMap<>(fallback.diagnostics().numeric());
        values.put("gmm_components", (double) components);
        return new ModelDiagnostics(values);
    }
}
