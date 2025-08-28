package hr.fer.zemris.edaf.configuration.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class Configuration {

    @NotNull(message = "Problem configuration is missing.")
    @Valid
    private ProblemConfig problem;

    @NotNull(message = "Algorithm configuration is missing.")
    @Valid
    private AlgorithmConfig algorithm;

    public ProblemConfig getProblem() {
        return problem;
    }

    public void setProblem(ProblemConfig problem) {
        this.problem = problem;
    }

    public AlgorithmConfig getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(AlgorithmConfig algorithm) {
        this.algorithm = algorithm;
    }

    public static class ProblemConfig {
        @JsonProperty("class")
        @NotEmpty(message = "Problem class name is missing.")
        private String className;

        @NotNull(message = "Genotype configuration is missing.")
        @Valid
        private GenotypeConfig genotype;

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public GenotypeConfig getGenotype() {
            return genotype;
        }

        public void setGenotype(GenotypeConfig genotype) {
            this.genotype = genotype;
        }
    }

    public static class GenotypeConfig {
        @NotEmpty(message = "Genotype type is missing.")
        private String type;
        private String encoding;
        @JsonProperty("l-bound")
        private double lowerBound;
        @JsonProperty("u-bound")
        private double upperBound;
        @Min(value = 1, message = "Genotype length must be at least 1.")
        private int length;
        private int precision;
        @Valid
        private CrossingConfig crossing;
        @Valid
        private MutationConfig mutation;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getEncoding() { return encoding; }
        public void setEncoding(String encoding) { this.encoding = encoding; }
        public double getLowerBound() { return lowerBound; }
        public void setLowerBound(double lowerBound) { this.lowerBound = lowerBound; }
        public double getUpperBound() { return upperBound; }
        public void setUpperBound(double upperBound) { this.upperBound = upperBound; }
        public int getLength() { return length; }
        public void setLength(int length) { this.length = length; }
        public int getPrecision() { return precision; }
        public void setPrecision(int precision) { this.precision = precision; }
        public CrossingConfig getCrossing() { return crossing; }
        public void setCrossing(CrossingConfig crossing) { this.crossing = crossing; }
        public MutationConfig getMutation() { return mutation; }
        public void setMutation(MutationConfig mutation) { this.mutation = mutation; }
    }

    public static class CrossingConfig {
        @NotEmpty(message = "Crossing name is missing.")
        private String name;
        private double probability;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getProbability() { return probability; }
        public void setProbability(double probability) { this.probability = probability; }
    }

    public static class MutationConfig {
        @NotEmpty(message = "Mutation name is missing.")
        private String name;
        private double probability;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getProbability() { return probability; }
        public void setProbability(double probability) { this.probability = probability; }
    }

    public static class AlgorithmConfig {
        @NotEmpty(message = "Algorithm name is missing.")
        private String name;
        @NotNull(message = "Population configuration is missing.")
        @Valid
        private PopulationConfig population;
        @NotNull(message = "Selection configuration is missing.")
        @Valid
        private SelectionConfig selection;
        @NotNull(message = "Termination configuration is missing.")
        @Valid
        private TerminationConfig termination;
        private int elitism;
        private double mortality;
        private int stagnation;
        @JsonProperty("log-frequency")
        private int logFrequency;
        @JsonProperty("log-directory")
        private String logDirectory;
        private Map<String, Object> parameters;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public PopulationConfig getPopulation() { return population; }
        public void setPopulation(PopulationConfig population) { this.population = population; }
        public SelectionConfig getSelection() { return selection; }
        public void setSelection(SelectionConfig selection) { this.selection = selection; }
        public TerminationConfig getTermination() { return termination; }
        public void setTermination(TerminationConfig termination) { this.termination = termination; }
        public int getElitism() { return elitism; }
        public void setElitism(int elitism) { this.elitism = elitism; }
        public double getMortality() { return mortality; }
        public void setMortality(double mortality) { this.mortality = mortality; }
        public int getStagnation() { return stagnation; }
        public void setStagnation(int stagnation) { this.stagnation = stagnation; }
        public int getLogFrequency() { return logFrequency; }
        public void setLogFrequency(int logFrequency) { this.logFrequency = logFrequency; }
        public String getLogDirectory() { return logDirectory; }
        public void setLogDirectory(String logDirectory) { this.logDirectory = logDirectory; }
        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    }

    public static class PopulationConfig {
        @Min(value = 1, message = "Population size must be at least 1.")
        private int size;

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }

    public static class SelectionConfig {
        @NotEmpty(message = "Selection name is missing.")
        private String name;
        private int size;
        private double ratio;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
        public double getRatio() { return ratio; }
        public void setRatio(double ratio) { this.ratio = ratio; }
    }

    public static class TerminationConfig {
        @JsonProperty("max-generations")
        @Min(value = 1, message = "Max generations must be at least 1.")
        private int maxGenerations;

        public int getMaxGenerations() { return maxGenerations; }
        public void setMaxGenerations(int maxGenerations) { this.maxGenerations = maxGenerations; }
    }
}
