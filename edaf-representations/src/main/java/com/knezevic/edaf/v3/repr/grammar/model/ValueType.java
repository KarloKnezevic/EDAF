package com.knezevic.edaf.v3.repr.grammar.model;

/**
 * Runtime value type tags used by grammar symbols and production rules.
 */
public enum ValueType {

    /** Numeric expression value. */
    REAL,

    /** Boolean expression value. */
    BOOL,

    /** Unrestricted / not yet specified type. */
    ANY
}
