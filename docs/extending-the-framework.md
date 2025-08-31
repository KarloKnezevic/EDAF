# Extending the Framework

The EDAF framework is designed to be easily extensible. You can add your own custom components, such as problems, algorithms, and genotypes, without modifying the core framework.

## Adding a New Problem

To add a new problem, you need to:

1.  **Create a new problem class.** Your class must implement the `com.knezevic.edaf.core.api.Problem` interface.
    This interface has a single method, `evaluate(Individual individual)`, which you need to implement.
    For example, you could create a `MyProblem.java` file in your own package.

    ```java
    package com.mycompany.myproject;

    import com.knezevic.edaf.core.api.Individual;
    import com.knezevic.edaf.core.api.Problem;

    public class MyProblem implements Problem {
        @Override
        public void evaluate(Individual individual) {
            // Your evaluation logic here
        }
    }
    ```

2.  **Update the configuration file.** In your YAML configuration file, you need to specify the fully qualified name of your new problem class.

    ```yaml
    problem:
        class: com.mycompany.myproject.MyProblem
        # other problem parameters...
    ```

3.  **Build and run.** Rebuild the project with `mvn clean install` to include your new class.
    Then, you can run the framework with your new configuration file.

## Adding a New Algorithm

To add a new algorithm, you need to:

1.  **Create a new algorithm class.** Your class must implement the `com.knezevic.edaf.core.api.Algorithm` interface.
2.  **Create a new algorithm factory.** Your factory must implement the `com.knezevic.edaf.factory.algorithm.AlgorithmFactory` interface. This factory will be responsible for creating your new algorithm and any specific crossover or mutation operators it requires.
3.  **Update the `AlgorithmFactoryProvider`.** Add a new case to the `getFactory` method in `com.knezevic.edaf.factory.algorithm.AlgorithmFactoryProvider` to return your new factory when the algorithm name is specified in the configuration.

## Adding a New Genotype

To add a new genotype, you need to:

1.  **Create a new genotype class.** Your class must implement the `com.knezevic.edaf.core.api.Genotype` interface.
2.  **Create a new genotype factory.** Your factory must implement the `com.knezevic.edaf.factory.genotype.GenotypeFactory` interface.
3.  **Update the `GenotypeFactoryProvider`.** Add a new case to the `getFactory` method in `com.knezevic.edaf.factory.genotype.GenotypeFactoryProvider` to return your new factory when the genotype name is specified in the configuration.

## Adding a New Selection Method

To add a new selection method, you need to:

1.  **Create a new selection class.** Your class must implement the `com.knezevic.edaf.core.api.Selection` interface.
2.  **Create a new selection factory.** Your factory must implement the `com.knezevic.edaf.factory.selection.SelectionFactory` interface.
3.  **Update the `SelectionFactoryProvider`.** Add a new case to the `getFactory` method in `com.knezevic.edaf.factory.selection.SelectionFactoryProvider` to return your new factory when the selection method name is specified in the configuration.
