/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Optional type annotation for terminals and production rules.
 *
 * <p>This keeps the current untyped execution path lightweight while allowing
 * future strongly-typed GP extensions without refactoring core model classes.</p>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record TypeSignature(ValueType outputType, List<ValueType> inputTypes) {

    /**
     * Immutable constructor.
     */
    public TypeSignature {
        outputType = outputType == null ? ValueType.ANY : outputType;
        inputTypes = inputTypes == null
                ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(inputTypes));
    }

    /**
     * Convenience factory for leaf symbols with no arguments.
     * @param outputType the outputType argument
     * @return the leaf
     */
    public static TypeSignature leaf(ValueType outputType) {
        return new TypeSignature(outputType, List.of());
    }

    /**
     * Returns true when this signature has no explicit input argument types.
     * @return true if leaf; otherwise false
     */
    public boolean isLeaf() {
        return inputTypes.isEmpty();
    }
}
