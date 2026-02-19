package com.knezevic.edaf.v3.core.plugins;

import com.knezevic.edaf.v3.core.api.ConstraintHandling;
import com.knezevic.edaf.v3.core.api.Model;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.ReplacementPolicy;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.api.SelectionPolicy;
import com.knezevic.edaf.v3.core.api.StoppingCondition;

/**
 * Dependency bundle passed to algorithm plugins.
 */
public record AlgorithmDependencies<G>(
        Representation<G> representation,
        Problem<G> problem,
        Model<G> model,
        SelectionPolicy<G> selectionPolicy,
        ReplacementPolicy<G> replacementPolicy,
        StoppingCondition<G> stoppingCondition,
        ConstraintHandling<G> constraintHandling
) {
}
