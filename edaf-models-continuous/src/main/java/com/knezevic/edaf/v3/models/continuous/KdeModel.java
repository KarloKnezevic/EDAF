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
 * KDE-EDA scaffold model.
 *
 * TODO(priority=high): Implement bandwidth adaptation and kernel mixture sampling.
 */
public final class KdeModel implements Model<RealVector> {

    private final double bandwidth;
    private final DiagonalGaussianModel fallback = new DiagonalGaussianModel(1e-6);

    public KdeModel(double bandwidth) {
        this.bandwidth = Math.max(1e-6, bandwidth);
    }

    @Override
    public String name() {
        return "kde";
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
        values.put("kde_bandwidth", bandwidth);
        return new ModelDiagnostics(values);
    }
}
