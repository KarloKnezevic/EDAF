/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Structured variable-length vector used as minimal non-fixed-length scaffold.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record VariableLengthVector<T>(List<T> values) {

    public VariableLengthVector {
        values = Collections.unmodifiableList(new ArrayList<>(values));
    }

    /**
     * Executes size.
     *
     * @return the number of elements
     */
    public int size() {
        return values.size();
    }

    /**
     * Converts to string.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return values.toString();
    }
}
