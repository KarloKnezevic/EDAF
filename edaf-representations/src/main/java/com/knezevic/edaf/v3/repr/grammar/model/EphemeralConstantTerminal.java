package com.knezevic.edaf.v3.repr.grammar.model;

/**
 * Ephemeral random constant (ERC) terminal definition.
 *
 * <p>Sampling is performed while decoding a tree and sampled value is persisted in the
 * terminal AST node.</p>
 */
public final class EphemeralConstantTerminal extends Terminal {

    private final String distributionSpec;
    private final double min;
    private final double max;

    /**
     * Creates ERC definition with inclusive bounds.
     */
    public EphemeralConstantTerminal(String distributionSpec, double min, double max) {
        super("ERC", TypeSignature.leaf(ValueType.REAL));
        this.distributionSpec = (distributionSpec == null || distributionSpec.isBlank())
                ? "uniform"
                : distributionSpec.trim();
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
    }

    /**
     * ERC distribution name used in serialization payloads.
     */
    public String distributionSpec() {
        return distributionSpec;
    }

    /**
     * Minimum sampled value.
     */
    public double min() {
        return min;
    }

    /**
     * Maximum sampled value.
     */
    public double max() {
        return max;
    }
}
