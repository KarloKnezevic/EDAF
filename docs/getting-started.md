# Getting Started

This guide will walk you through the process of building and running the EDAF framework.

## How to Build

The project is built with Maven. To build the project, run the following command from the root directory:

```
mvn clean install
```

This will compile all the modules and create the JAR files in the `target` directory of each module.

## How to Run

The framework is run from the command line using the executable JAR file produced by the `examples` module.
After building the project with `mvn clean install`, you can find this JAR at `examples/target/examples-2.0.0-SNAPSHOT-jar-with-dependencies.jar`.

The framework now provides a command-line interface (CLI) with several options. You can get help by running:
```
java -jar examples/target/examples-2.0.0-SNAPSHOT-jar-with-dependencies.jar --help
```

### Running an Experiment

To run an experiment, simply provide the path to a valid configuration file:
```
java -jar examples/target/examples-2.0.0-SNAPSHOT-jar-with-dependencies.jar examples/config/cga-max-ones.yaml
```
The framework will print its progress to the console.

### Generating a Configuration Template

To make it easier to create new configuration files, you can use the `generate-config` command.
This will generate a template for a given algorithm and print it to the console.

```
java -jar examples/target/examples-2.0.0-SNAPSHOT-jar-with-dependencies.jar generate-config --algorithm cGA
```
You can redirect the output to a file:
```
java -jar examples/target/examples-2.0.0-SNAPSHOT-jar-with-dependencies.jar generate-config -a eGA > my-ega-config.yaml
```

## Logging

The framework uses a robust logging system.
*   **Console Output:** General progress and informational messages are printed to the console in a human-readable format.
*   **Log File:** Detailed logs are written to `edaf.log`.
*   **Results File:** The final result of each run is written to `results.json` in a structured JSON format, which is easy to parse for automated analysis.
