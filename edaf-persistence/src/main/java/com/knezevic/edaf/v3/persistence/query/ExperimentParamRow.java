/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query;

/**
 * Flattened experiment the input value row for searchable config exploration.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record ExperimentParamRow(
        long id,
        String experimentId,
        String section,
        String paramPath,
        String leafKey,
        String valueType,
        String valueText,
        Double valueNumber,
        Integer valueBoolean,
        String valueJson
) {
}
