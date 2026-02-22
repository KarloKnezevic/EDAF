package com.knezevic.edaf.v3.experiments.factory;

import com.knezevic.edaf.v3.core.api.ConstraintHandling;
import com.knezevic.edaf.v3.core.api.LocalSearch;
import com.knezevic.edaf.v3.core.api.NichingPolicy;
import com.knezevic.edaf.v3.core.api.ReplacementPolicy;
import com.knezevic.edaf.v3.core.api.RestartPolicy;
import com.knezevic.edaf.v3.core.api.SelectionPolicy;
import com.knezevic.edaf.v3.core.api.StoppingCondition;
import com.knezevic.edaf.v3.core.api.defaults.BudgetOrTargetStoppingCondition;
import com.knezevic.edaf.v3.core.api.defaults.ElitistReplacementPolicy;
import com.knezevic.edaf.v3.core.api.defaults.FitnessSharingNichingPolicy;
import com.knezevic.edaf.v3.core.api.defaults.IdentityConstraintHandling;
import com.knezevic.edaf.v3.core.api.defaults.MaxIterationsStoppingCondition;
import com.knezevic.edaf.v3.core.api.defaults.NoNichingPolicy;
import com.knezevic.edaf.v3.core.api.defaults.NoOpLocalSearch;
import com.knezevic.edaf.v3.core.api.defaults.NoRestartPolicy;
import com.knezevic.edaf.v3.core.api.defaults.PenaltyConstraintHandling;
import com.knezevic.edaf.v3.core.api.defaults.RejectionConstraintHandling;
import com.knezevic.edaf.v3.core.api.defaults.StagnationRestartPolicy;
import com.knezevic.edaf.v3.core.api.defaults.TournamentSelectionPolicy;
import com.knezevic.edaf.v3.core.api.defaults.TruncationSelectionPolicy;
import com.knezevic.edaf.v3.core.config.ExperimentConfig;
import com.knezevic.edaf.v3.core.util.Params;

/**
 * Constructs policy components from config sections.
 */
public final class PolicyFactory {

    private PolicyFactory() {
        // utility class
    }

    public static <G> SelectionPolicy<G> createSelection(ExperimentConfig config) {
        String type = config.getSelection().getType().toLowerCase(java.util.Locale.ROOT);
        return switch (type) {
            case "tournament" -> new TournamentSelectionPolicy<>(Params.integer(config.getSelection().getParams(), "k", 3));
            case "truncation" -> new TruncationSelectionPolicy<>();
            default -> new TruncationSelectionPolicy<>();
        };
    }

    public static <G> ReplacementPolicy<G> createReplacement(ExperimentConfig config) {
        String type = config.getReplacement().getType().toLowerCase(java.util.Locale.ROOT);
        return switch (type) {
            case "elitist", "generational" -> new ElitistReplacementPolicy<>();
            default -> new ElitistReplacementPolicy<>();
        };
    }

    public static <G> StoppingCondition<G> createStopping(ExperimentConfig config) {
        String type = config.getStopping().getType().toLowerCase(java.util.Locale.ROOT);
        return switch (type) {
            case "max-iterations" -> new MaxIterationsStoppingCondition<>(config.getStopping().getMaxIterations());
            case "budget-or-target", "max-evaluations-or-target" ->
                    new BudgetOrTargetStoppingCondition<>(
                            config.getStopping().getMaxIterations(),
                            config.getStopping().getMaxEvaluations(),
                            config.getStopping().getTargetFitness()
                    );
            default -> new MaxIterationsStoppingCondition<>(config.getStopping().getMaxIterations());
        };
    }

    public static <G> ConstraintHandling<G> createConstraintHandling(ExperimentConfig config) {
        String type = config.getConstraints().getType().toLowerCase(java.util.Locale.ROOT);
        return switch (type) {
            case "rejection" -> new RejectionConstraintHandling<>(Params.integer(config.getConstraints().getParams(), "maxRetries", 10));
            case "penalty" -> new PenaltyConstraintHandling<>();
            case "repair", "identity" -> new IdentityConstraintHandling<>();
            default -> new IdentityConstraintHandling<>();
        };
    }

    public static <G> LocalSearch<G> createLocalSearch(ExperimentConfig config) {
        return new NoOpLocalSearch<>();
    }

    public static <G> RestartPolicy<G> createRestartPolicy(ExperimentConfig config) {
        String type = config.getRestart().getType().toLowerCase(java.util.Locale.ROOT);
        return switch (type) {
            case "stagnation" -> new StagnationRestartPolicy<>(Params.integer(config.getRestart().getParams(), "patience", 1000));
            default -> new NoRestartPolicy<>();
        };
    }

    public static <G> NichingPolicy<G> createNichingPolicy(ExperimentConfig config) {
        String type = config.getNiching().getType().toLowerCase(java.util.Locale.ROOT);
        return switch (type) {
            case "fitness-sharing" -> new FitnessSharingNichingPolicy<>();
            default -> new NoNichingPolicy<>();
        };
    }
}
