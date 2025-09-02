package com.knezevic.edaf.factory.algorithm;

import com.knezevic.edaf.configuration.pojos.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Provides a factory for creating {@link com.knezevic.edaf.core.api.Algorithm} objects.
 * The factory is chosen based on the algorithm name specified in the configuration.
 */
public class AlgorithmFactoryProvider {
    private static final Map<String, Supplier<AlgorithmFactory>> factoryMap = new HashMap<>();

    static {
        factoryMap.put("umda", UmdaFactory::new);
        factoryMap.put("pbil", PbilFactory::new);
        factoryMap.put("gga", GgaFactory::new);
        factoryMap.put("ega", EgaFactory::new);
        factoryMap.put("cga", CgaFactory::new);
        factoryMap.put("mimic", MimicFactory::new);
        factoryMap.put("boa", BoaFactory::new);
        factoryMap.put("ltga", LtgaFactory::new);
        factoryMap.put("bmda", BmdaFactory::new);
        factoryMap.put("gp", GpFactory::new);
    }

    /**
     * Returns an {@link AlgorithmFactory} for the given configuration.
     *
     * @param config The configuration.
     * @return An {@link AlgorithmFactory} instance, or {@code null} if no factory is found for the given configuration.
     */
    public static AlgorithmFactory getFactory(Configuration config) {
        String algorithmName = config.getAlgorithm().getName();
        Supplier<AlgorithmFactory> factorySupplier = factoryMap.get(algorithmName);
        if (factorySupplier != null) {
            return factorySupplier.get();
        }
        return null;
    }
}
