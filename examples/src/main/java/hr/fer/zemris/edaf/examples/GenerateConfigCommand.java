package hr.fer.zemris.edaf.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import hr.fer.zemris.edaf.configuration.pojos.Configuration;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "generate-config", mixinStandardHelpOptions = true,
        description = "Generates a template configuration file for an algorithm and prints it to standard output.")
public class GenerateConfigCommand implements Callable<Integer> {

    @Option(names = {"-a", "--algorithm"}, required = true, description = "The name of the algorithm (e.g., cGA, eGA, gGA).")
    private String algorithmName;

    @Override
    public Integer call() throws Exception {
        Configuration config = createDefaultConfig(algorithmName);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(config));

        return 0;
    }

    private Configuration createDefaultConfig(String algorithmName) {
        Configuration config = new Configuration();

        // Problem section
        Configuration.ProblemConfig problem = new Configuration.ProblemConfig();
        problem.setClassName("hr.fer.zemris.edaf.testing.problems.MaxOnes");

        // Genotype section
        Configuration.GenotypeConfig genotype = new Configuration.GenotypeConfig();
        genotype.setType("binary"); // binary, fp, or integer
        genotype.setLength(20);
        // For fp genotype
        genotype.setLowerBound(0.0);
        genotype.setUpperBound(1.0);
        // For integer genotype
        genotype.setMinBound(0);
        genotype.setMaxBound(1);

        // Crossing section
        Configuration.CrossingConfig crossing = new Configuration.CrossingConfig();
        crossing.setName("onePoint"); // e.g., onePoint, uniform, sbx
        crossing.setProbability(0.8);
        crossing.setDistributionIndex(20.0); // For SBX
        genotype.setCrossing(crossing);

        // Mutation section
        Configuration.MutationConfig mutation = new Configuration.MutationConfig();
        mutation.setName("simple"); // e.g., simple, polynomial
        mutation.setProbability(0.05);
        mutation.setDistributionIndex(20.0); // For Polynomial Mutation
        genotype.setMutation(mutation);

        problem.setGenotype(genotype);
        config.setProblem(problem);

        // Algorithm section
        Configuration.AlgorithmConfig algorithm = new Configuration.AlgorithmConfig();
        algorithm.setName(algorithmName);

        Configuration.PopulationConfig population = new Configuration.PopulationConfig();
        population.setSize(100);
        algorithm.setPopulation(population);

        Configuration.SelectionConfig selection = new Configuration.SelectionConfig();
        selection.setName("tournament"); // e.g., tournament, rouletteWheel
        selection.setSize(3);
        algorithm.setSelection(selection);

        Configuration.TerminationConfig termination = new Configuration.TerminationConfig();
        termination.setMaxGenerations(100);
        algorithm.setTermination(termination);

        algorithm.setElitism(1);

        config.setAlgorithm(algorithm);

        return config;
    }
}
