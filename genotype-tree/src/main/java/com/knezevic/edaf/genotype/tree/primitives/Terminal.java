package com.knezevic.edaf.genotype.tree.primitives;

import java.io.Serializable;

/**
 * Represents a terminal in the primitive set.
 * A terminal is a leaf node in the program tree. It can be a variable, a constant, or an ephemeral random constant template.
 */
public class Terminal implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final double value;
    private final boolean isVariable;
    private final double[] range; // For ephemeral constants [min, max]

    /**
     * Constructor for a variable terminal.
     * @param name The name of the variable (e.g., "x").
     */
    public Terminal(String name) {
        this.name = name;
        this.value = 0;
        this.isVariable = true;
        this.range = null;
    }

    /**
     * Constructor for a constant terminal.
     * @param name  The name of the constant (e.g., "5.0").
     * @param value The value of the constant.
     */
    public Terminal(String name, double value) {
        this.name = name;
        this.value = value;
        this.isVariable = false;
        this.range = null;
    }

    /**
     * Constructor for an ephemeral constant template.
     * @param name The name of the ephemeral constant (e.g., "ERC").
     * @param min The minimum value of the random range.
     * @param max The maximum value of the random range.
     */
    public Terminal(String name, double min, double max) {
        this.name = name;
        this.value = 0; // Placeholder, will be generated on use
        this.isVariable = false;
        this.range = new double[]{min, max};
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

    public boolean isEphemeral() {
        return this.range != null;
    }

    public double[] getRange() {
        return range;
    }
}
