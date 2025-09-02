package com.knezevic.edaf.genotype.tree.primitives;

import java.io.Serializable;

/**
 * Represents a terminal in the primitive set.
 * A terminal is a leaf node in the program tree. It can be a variable or a constant.
 */
public class Terminal implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final double value;
    private final boolean isVariable;

    /**
     * Constructor for a variable terminal.
     *
     * @param name The name of the variable (e.g., "x").
     */
    public Terminal(String name) {
        this.name = name;
        this.value = 0; // Default value, will be replaced by input
        this.isVariable = true;
    }

    /**
     * Constructor for a constant terminal.
     *
     * @param name  The name of the constant (e.g., "5.0").
     * @param value The value of the constant.
     */
    public Terminal(String name, double value) {
        this.name = name;
        this.value = value;
        this.isVariable = false;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public boolean isVariable() {
        return isVariable;
    }
}
