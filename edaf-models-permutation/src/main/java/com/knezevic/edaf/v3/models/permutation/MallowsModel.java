package com.knezevic.edaf.v3.models.permutation;

import com.knezevic.edaf.v3.core.api.ConstraintHandling;
import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.Model;
import com.knezevic.edaf.v3.core.api.ModelDiagnostics;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mallows model scaffold.
 *
 * TODO(priority=medium): Implement Kendall distance-based Mallows parameter estimation and exact sampler.
 */
public final class MallowsModel implements Model<PermutationVector> {

    private final PlackettLuceModel fallback = new PlackettLuceModel();

    @Override
    public String name() {
        return "mallows";
    }

    @Override
    public void fit(List<Individual<PermutationVector>> selected,
                    Representation<PermutationVector> representation,
                    RngStream rng) {
        fallback.fit(selected, representation, rng);
    }

    @Override
    public List<PermutationVector> sample(int count,
                                          Representation<PermutationVector> representation,
                                          Problem<PermutationVector> problem,
                                          ConstraintHandling<PermutationVector> constraintHandling,
                                          RngStream rng) {
        return fallback.sample(count, representation, problem, constraintHandling, rng);
    }

    @Override
    public ModelDiagnostics diagnostics() {
        Map<String, Double> values = new LinkedHashMap<>(fallback.diagnostics().numeric());
        values.put("mallows_theta", 0.0);
        return new ModelDiagnostics(values);
    }
}
