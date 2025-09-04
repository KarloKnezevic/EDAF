package com.knezevic.edaf.algorithm.cgp.model;

import com.knezevic.edaf.genotype.tree.primitives.Function;

/**
 * Represents a node in the CGP program graph.
 * Each node has a function and a list of inputs, which are indices to other nodes or program inputs.
 */
public class CgpNode {

    private final Function function;
    private final int[] inputs;
    private boolean isActive = false;
    private double value;

    public CgpNode(Function function, int[] inputs) {
        this.function = function;
        this.inputs = inputs;
    }

    public Function getFunction() {
        return function;
    }

    public int[] getInputs() {
        return inputs;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
