package com.knezevic.edaf.v3.repr.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Structured variable-length vector used as minimal non-fixed-length scaffold.
 */
public record VariableLengthVector<T>(List<T> values) {

    public VariableLengthVector {
        values = Collections.unmodifiableList(new ArrayList<>(values));
    }

    public int size() {
        return values.size();
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
