package com.knezevic.edaf.factory.selection;

import com.knezevic.edaf.configuration.pojos.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Provides a factory for creating {@link com.knezevic.edaf.core.api.Selection} objects.
 * The factory is chosen based on the selection operator name specified in the configuration.
 */
public class SelectionFactoryProvider {
    private static final Map<String, Supplier<SelectionFactory>> factoryMap = new HashMap<>();

    static {
        factoryMap.put("tournament", TournamentSelectionFactory::new);
        factoryMap.put("rouletteWheel", RouletteWheelSelectionFactory::new);
        factoryMap.put("kTournament", KTournamentSelectionFactory::new);
        factoryMap.put("muCommaLambda", MuCommaLambdaSelectionFactory::new);
        factoryMap.put("muPlusLambda", MuPlusLambdaSelectionFactory::new);
        factoryMap.put("proportional", ProportionalSelectionFactory::new);
        factoryMap.put("simpleTournament", SimpleTournamentSelectionFactory::new);
        factoryMap.put("stochasticUniversalSampling", StochasticUniversalSamplingSelectionFactory::new);
        factoryMap.put("truncated", TruncatedSelectionFactory::new);
    }

    /**
     * Returns a {@link SelectionFactory} for the given configuration.
     *
     * @param config The configuration.
     * @return A {@link SelectionFactory} instance, or {@code null} if no factory is found for the given configuration.
     */
    public static SelectionFactory getFactory(Configuration config) {
        if (config.getAlgorithm().getSelection() == null) {
            return null;
        }
        String selectionName = config.getAlgorithm().getSelection().getName();
        Supplier<SelectionFactory> factorySupplier = factoryMap.get(selectionName);
        if (factorySupplier != null) {
            return factorySupplier.get();
        }
        return null;
    }
}
