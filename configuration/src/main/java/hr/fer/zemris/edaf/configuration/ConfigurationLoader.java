package hr.fer.zemris.edaf.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import hr.fer.zemris.edaf.configuration.pojos.Configuration;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Loads the configuration from a YAML file.
 */
public class ConfigurationLoader {

    private final ObjectMapper mapper;
    private final Validator validator;

    public ConfigurationLoader() {
        this.mapper = new ObjectMapper(new YAMLFactory());
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
    }

    /**
     * Loads and validates the configuration from a YAML file.
     *
     * @param path The path to the YAML file.
     * @return The configuration object.
     * @throws IOException If an error occurs while reading the file.
     * @throws RuntimeException If the configuration is invalid.
     */
    public Configuration load(String path) throws IOException {
        Configuration config = mapper.readValue(new File(path), Configuration.class);
        Set<ConstraintViolation<Configuration>> violations = validator.validate(config);

        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Configuration file '").append(path).append("' is invalid. Violations:\n");
            for (ConstraintViolation<Configuration> violation : violations) {
                sb.append("  - ").append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("\n");
            }
            throw new RuntimeException(sb.toString());
        }

        return config;
    }
}
