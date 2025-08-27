package hr.fer.zemris.edaf.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import hr.fer.zemris.edaf.configuration.pojos.Configuration;

import java.io.File;
import java.io.IOException;

/**
 * Loads the configuration from a YAML file.
 */
public class ConfigurationLoader {

    private final ObjectMapper mapper;

    public ConfigurationLoader() {
        this.mapper = new ObjectMapper(new YAMLFactory());
    }

    /**
     * Loads the configuration from a YAML file.
     *
     * @param path The path to the YAML file.
     * @return The configuration object.
     * @throws IOException If an error occurs while reading the file.
     */
    public Configuration load(String path) throws IOException {
        return mapper.readValue(new File(path), Configuration.class);
    }
}
