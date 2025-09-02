package com.knezevic.edaf.factory.algorithm;

import com.knezevic.edaf.configuration.pojos.Configuration;

public class AlgorithmFactoryProvider {
    public static AlgorithmFactory getFactory(Configuration config) {
        String algorithmName = config.getAlgorithm().getName();
        if ("umda".equals(algorithmName)) {
            return new UmdaFactory();
        } else if ("pbil".equals(algorithmName)) {
            return new PbilFactory();
        } else if ("gga".equals(algorithmName)) {
            return new GgaFactory();
        } else if ("ega".equals(algorithmName)) {
            return new EgaFactory();
        } else if ("cga".equals(algorithmName)) {
            return new CgaFactory();
        } else if ("mimic".equals(algorithmName)) {
            return new MimicFactory();
        } else if ("boa".equals(algorithmName)) {
            return new BoaFactory();
        } else if ("ltga".equals(algorithmName)) {
            return new LtgaFactory();
        } else if ("bmda".equals(algorithmName)) {
            return new BmdaFactory();
        } else if ("gp".equals(algorithmName)) {
            return new GpFactory();
        }
        return null;
    }
}
