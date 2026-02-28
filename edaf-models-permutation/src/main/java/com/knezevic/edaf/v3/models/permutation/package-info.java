/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

/**
 * Probabilistic models for permutation spaces.
 *
 * <p>The package provides complementary views of permutation structure:
 * <ul>
 *     <li>adjacency models via edge histograms ({@code P(j|i)})</li>
 *     <li>consensus-distance models via Mallows/Kendall</li>
 *     <li>sequential choice models via Plackett-Luce weights</li>
 * </ul>
 *
 * <p>Typical factorization forms:
 * <pre>
 *   P(next = j | current = i)
 *   P(pi) proportional exp(-theta * d_K(pi, pi0))
 *   P(i at step k) = w_i / sum_{j in remaining} w_j
 * </pre>
 * where {@code d_K} is Kendall tau distance.</p>
 *
 * <p>References:
 * <ul>
 *     <li>Ceberio et al. - EHBSA and permutation EDAs</li>
 *     <li>Fligner and Verducci (1986) - distance-based ranking models</li>
 *     <li>Plackett (1975), Luce (1959) - PL model</li>
 * </ul></p>
 */
package com.knezevic.edaf.v3.models.permutation;
