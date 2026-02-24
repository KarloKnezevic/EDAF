/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.encoding;

import com.knezevic.edaf.v3.repr.grammar.build.GrammarConfig;
import com.knezevic.edaf.v3.repr.grammar.model.Grammar;

/**
 * Deterministic fixed-length encoding metadata for grammar derivation decisions.
 */
public record GrammarEncoding(
        int maxDepth,
        int maxNodes,
        int bitsPerDecision,
        int bitsPerErc,
        int genomeLength
) {

    /**
     * Creates encoding plan from grammar and config.
     */
    public static GrammarEncoding from(Grammar grammar, GrammarConfig config) {
        int maxAlternatives = Math.max(2, grammar.maxAlternatives());
        int bitsPerDecision = Math.max(config.bitsPerDecision(), bitsFor(maxAlternatives));
        int maxBranch = Math.max(1, grammar.maxBranchingFactor());
        int maxNodes = Math.min(config.maxNodes(), estimateMaxNodes(maxBranch, config.maxDepth()));

        // Reserve one ERC payload chunk per potential node to keep fixed-length encoding deterministic.
        int bitsPerErc = config.ephemeralConstants() ? config.bitsPerErc() : 0;
        long rawLength = (long) maxNodes * (bitsPerDecision + bitsPerErc);
        int genomeLength = (int) Math.max(64L, Math.min(Integer.MAX_VALUE, rawLength));

        return new GrammarEncoding(
                config.maxDepth(),
                maxNodes,
                bitsPerDecision,
                bitsPerErc,
                genomeLength
        );
    }

    private static int estimateMaxNodes(int maxBranch, int depth) {
        if (depth <= 0) {
            return 1;
        }
        if (maxBranch <= 1) {
            return depth + 1;
        }
        long numerator = 1;
        for (int i = 0; i <= depth; i++) {
            numerator *= maxBranch;
            if (numerator > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
        }
        long estimate = (numerator - 1) / (maxBranch - 1);
        return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, estimate));
    }

    private static int bitsFor(int alternatives) {
        int bits = 0;
        int value = 1;
        while (value < alternatives) {
            bits++;
            value <<= 1;
        }
        return Math.max(1, bits);
    }
}
