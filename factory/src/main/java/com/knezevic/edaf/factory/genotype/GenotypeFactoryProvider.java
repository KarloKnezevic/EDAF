package com.knezevic.edaf.factory.genotype;

import com.knezevic.edaf.configuration.pojos.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Provides a factory for creating {@link com.knezevic.edaf.core.api.Genotype} objects.
 * The factory is chosen based on the genotype type specified in the configuration.
 */
public class GenotypeFactoryProvider {
    private static final Map<String, Supplier<GenotypeFactory>> factoryMap = new HashMap<>();

    static {
        factoryMap.put("binary", BinaryGenotypeFactory::new);
        factoryMap.put("fp", FpGenotypeFactory::new);
        factoryMap.put("integer", IntegerGenotypeFactory::new);
        factoryMap.put("tree", TreeGenotypeFactory::new);
        factoryMap.put("permutation", PermutationGenotypeFactory::new);
    }

    /**
     * Returns a {@link GenotypeFactory} for the given configuration.
     *
     * @param config The configuration.
     * @return A {@link GenotypeFactory} instance, or {@code null} if no factory is found for the given configuration.
     */
    public static GenotypeFactory getFactory(Configuration config) {
        String type = config.getProblem().getGenotype().getType();
        Supplier<GenotypeFactory> factorySupplier = factoryMap.get(type);
        if (factorySupplier != null) {
            return factorySupplier.get();
        }
        return null;
    }
}
