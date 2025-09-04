package com.knezevic.edaf.factory.problem;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Problem;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * A default implementation of the {@link ProblemFactory} interface.
 * <p>
 * This factory assumes that the problem class has a constructor that accepts a
 * {@code Map<String, Object>} as its only argument.
 */
public class DefaultProblemFactory implements ProblemFactory {
    @Override
    public Problem create(Configuration config) throws Exception {
        String className = config.getProblem().getClassName();
        Class<?> problemClass = Class.forName(className);
        Map<String, Object> parameters = config.getProblem().getParameters();

        // Add the optimization type to the parameters map.
        // This is a bit of a hack, but it allows the AbstractProblem to get the value
        // without a major refactoring of the factory system.
        parameters.put("optimizationType", config.getProblem().getOptimizationType());

        try {
            Constructor<?> constructor = problemClass.getConstructor(Map.class);
            return (Problem) constructor.newInstance(parameters);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException("Could not find a constructor for class " + className +
                    " that accepts a Map<String, Object>. " +
                    "Please make sure your problem class has a public constructor like: " +
                    "public " + problemClass.getSimpleName() + "(Map<String, Object> params)");
        }
    }
}
