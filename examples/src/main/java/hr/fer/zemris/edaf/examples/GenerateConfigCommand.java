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
        // This is a simplified example. A real implementation would have more
        // sophisticated logic to create defaults for different algorithms.
        Configuration config = new Configuration();

        Configuration.ProblemConfig problem = new Configuration.ProblemConfig();
        problem.setClassName("hr.fer.zemris.edaf.testing.maxones.MaxOnes");
        Configuration.GenotypeConfig genotype = new Configuration.GenotypeConfig();
        genotype.setType("binary");
        genotype.setLength(20);
        problem.setGenotype(genotype);
        config.setProblem(problem);

        Configuration.AlgorithmConfig algorithm = new Configuration.AlgorithmConfig();
        algorithm.setName(algorithmName);
        Configuration.PopulationConfig population = new Configuration.PopulationConfig();
        population.setSize(100);
        algorithm.setPopulation(population);
        Configuration.SelectionConfig selection = new Configuration.SelectionConfig();
        selection.setName("tournament");
        selection.setSize(2);
        algorithm.setSelection(selection);
        Configuration.TerminationConfig termination = new Configuration.TerminationConfig();
        termination.setMaxGenerations(100);
        algorithm.setTermination(termination);
        config.setAlgorithm(algorithm);

        return config;
    }
}
