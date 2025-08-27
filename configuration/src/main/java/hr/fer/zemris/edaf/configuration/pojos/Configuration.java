package hr.fer.zemris.edaf.configuration.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Configuration {

    private ProblemConfig problem;
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
        private String className;
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
        private String type;
        private int length;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }
    }

    public static class AlgorithmConfig {
        private String name;
        private PopulationConfig population;
        private SelectionConfig selection;
        private TerminationConfig termination;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public PopulationConfig getPopulation() {
            return population;
        }

        public void setPopulation(PopulationConfig population) {
            this.population = population;
        }

        public SelectionConfig getSelection() {
            return selection;
        }

        public void setSelection(SelectionConfig selection) {
            this.selection = selection;
        }

        public TerminationConfig getTermination() {
            return termination;
        }

        public void setTermination(TerminationConfig termination) {
            this.termination = termination;
        }
    }

    public static class PopulationConfig {
        private int size;

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }

    public static class SelectionConfig {
        private String name;
        private int size;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }

    public static class TerminationConfig {
        @JsonProperty("max-generations")
        private int maxGenerations;

        public int getMaxGenerations() {
            return maxGenerations;
        }

        public void setMaxGenerations(int maxGenerations) {
            this.maxGenerations = maxGenerations;
        }
    }
}
