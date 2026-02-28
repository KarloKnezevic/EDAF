/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.model;

/**
 * Runtime value type tags used by grammar symbols and production rules.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public enum ValueType {

    /** Numeric expression value. */
    REAL,

    /** Boolean expression value. */
    BOOL,

    /** Unrestricted / not yet specified type. */
    ANY
}
