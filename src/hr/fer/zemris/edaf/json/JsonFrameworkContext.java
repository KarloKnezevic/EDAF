package hr.fer.zemris.edaf.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.fer.zemris.edaf.IFrameworkContext;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class JsonFrameworkContext implements IFrameworkContext {

    private final Configuration config;
    private final Random rand;

    public JsonFrameworkContext(String jsonFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        config = mapper.readValue(new File(jsonFile), Configuration.class);
        rand = new Random();
    }

    @Override
    public String getAlgorithmName() {
        return config.algorithm.name;
    }

    @Override
    public String getWorkEnvironment() {
        return config.workEnvironment.name;
    }

    @Override
    public String getGenotype() {
        return config.genotype.gene;
    }

    @Override
    public String getEncoding() {
        return config.genotype.encoding;
    }

    @Override
    public int getLBound() {
        return config.genotype.lBound;
    }

    @Override
    public int getUBound() {
        return config.genotype.uBound;
    }

    @Override
    public int getDimension() {
        return config.genotype.dimension;
    }

    @Override
    public int getPrecision() {
        return config.genotype.precision.value;
    }

    @Override
    public String getPrecisionDescription() {
        return config.genotype.precision.key;
    }

    @Override
    public String getCrossing() {
        return config.genotype.crossing.key;
    }

    @Override
    public int getNi() {
        return config.genotype.crossing.ni;
    }

    @Override
    public double getCrossingProb() {
        return config.genotype.crossing.value;
    }

    @Override
    public String getMutation() {
        return config.genotype.mutation.key;
    }

    @Override
    public double getMutationProb() {
        return config.genotype.mutation.value;
    }

    @Override
    public int getPopulationSize() {
        return config.registry.populationSize;
    }

    @Override
    public double getEstimationProbability() {
        return config.registry.estimationProbability;
    }

    @Override
    public int getElitism() {
        return config.registry.elitism;
    }

    @Override
    public double getMortality() {
        return config.registry.mortality;
    }

    @Override
    public int getMaxNumberOfGen() {
        return config.registry.maxNumberOfGen;
    }

    @Override
    public int getStagnation() {
        return config.registry.stagnation;
    }

    @Override
    public double getDestValue() {
        return config.registry.destValue;
    }

    @Override
    public int getLogFrequency() {
        return config.registry.logFrequency;
    }

    @Override
    public String getLogDirectory() {
        return config.registry.logDirectory;
    }

    @Override
    public String getSelection() {
        return config.registry.selection.key;
    }

    @Override
    public double getSelectionRatio() {
        return config.registry.selection.ratio;
    }

    @Override
    public double getSelectionParam() {
        return config.registry.selection.value;
    }

    @Override
    public String getRatioSelector() {
        return config.algorithmParam.ratioSelector.value;
    }

    @Override
    public double getRatioSelectorRatio() {
        return config.algorithmParam.ratioSelector.ratio;
    }

    @Override
    public Random getRand() {
        return rand;
    }

    public static class Configuration {
        public Algorithm algorithm;
        public WorkEnvironment workEnvironment;
        public Genotype genotype;
        public Registry registry;
        public AlgorithmParam algorithmParam;
    }

    public static class Algorithm {
        public String name;
    }

    public static class WorkEnvironment {
        public String name;
    }

    public static class Genotype {
        public String gene;
        public String encoding;
        public int lBound;
        public int uBound;
        public int dimension;
        public Precision precision;
        public Crossing crossing;
        public Mutation mutation;
    }

    public static class Precision {
        public String key;
        public int value;
    }

    public static class Crossing {
        public String key;
        public double value;
        public int ni;
    }

    public static class Mutation {
        public String key;
        public double value;
    }

    public static class Registry {
        public int populationSize;
        public double estimationProbability;
        public int elitism;
        public double mortality;
        public int maxNumberOfGen;
        public int stagnation;
        public double destValue;
        public int logFrequency;
        public String logDirectory;
        public Selection selection;
    }

    public static class Selection {
        public String key;
        public double ratio;
        public double value;
    }

    public static class AlgorithmParam {
        public RatioSelector ratioSelector;
    }

    public static class RatioSelector {
        public String value;
        public double ratio;
    }
}
