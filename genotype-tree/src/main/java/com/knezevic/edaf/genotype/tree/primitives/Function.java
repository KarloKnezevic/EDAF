package com.knezevic.edaf.genotype.tree.primitives;

import java.io.Serializable;

/**
 * Represents a function in the primitive set.
 * A function is an internal node in the program tree.
 */
public class Function implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final int arity;
    private final transient IFunction op;

    /**
     * A functional interface for operations with a variable number of arguments.
     */
    @FunctionalInterface
    public interface IFunction {
        double apply(double... args);
    }

    /**
     * Constructs a new Function.
     *
     * @param name  The name of the function (e.g., "ADD").
     * @param arity The number of arguments the function takes.
     * @param op    The operation to perform.
     */
    public Function(String name, int arity, IFunction op) {
        this.name = name;
        this.arity = arity;
        this.op = op;
    }

    public String getName() {
        return name;
    }

    public int getArity() {
        return arity;
    }

    public IFunction getOp() {
        return op;
    }
}
