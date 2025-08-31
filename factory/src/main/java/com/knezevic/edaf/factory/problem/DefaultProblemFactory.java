package com.knezevic.edaf.factory.problem;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Problem;

import java.lang.reflect.Constructor;
import java.util.Map;

public class DefaultProblemFactory implements ProblemFactory {
    @Override
    public Problem create(Configuration config) throws Exception {
        Class<?> problemClass = Class.forName(config.getProblem().getClassName());
        Map<String, Object> parameters = config.getProblem().getParameters();

        if (parameters == null || parameters.isEmpty()) {
            Constructor<?> problemConstructor = problemClass.getConstructor();
            return (Problem) problemConstructor.newInstance();
        }

        Constructor<?>[] constructors = problemClass.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == parameters.size()) {
                // This is a simplistic check. A more robust implementation would check
                // parameter names and types.
                try {
                    Object[] args = new Object[parameters.size()];
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    int i = 0;
                    for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                        args[i] = convert(entry.getValue(), paramTypes[i]);
                        i++;
                    }
                    return (Problem) constructor.newInstance(args);
                } catch (Exception e) {
                    // Ignore and try the next constructor
                }
            }
        }

        throw new NoSuchMethodException("No suitable constructor found for problem class " + problemClass.getName() + " with parameters " + parameters);
    }

    private Object convert(Object value, Class<?> targetType) {
        if (targetType.isInstance(value)) {
            return value;
        }
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value.toString());
        }
        if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value.toString());
        }
        if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(value.toString());
        }
        if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value.toString());
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value.toString());
        }
        return value;
    }
}
