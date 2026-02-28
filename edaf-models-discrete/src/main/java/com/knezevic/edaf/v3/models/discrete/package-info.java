/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

/**
 * Probabilistic models for discrete and binary search spaces.
 *
 * <p>This package implements families commonly used in EDA literature:
 * <ul>
 *     <li>factorized Bernoulli models (UMDA, PBIL, cGA-style updates)</li>
 *     <li>tree-structured dependency models (BMDA, Chow-Liu/MIMIC)</li>
 *     <li>sparse Bayesian-network models (BOA/EBNA/hBOA-inspired)</li>
 * </ul>
 *
 * <p>Canonical update templates implemented across classes:
 * <pre>
 *   p_i <- (1 - eta) p_i + eta * E[x_i | elite]
 *   p(x) = product_i p(x_i | Pa(x_i))
 * </pre>
 * where smoothing/clamping is applied to keep probabilities away from {0,1}.</p>
 *
 * <p>References:
 * <ul>
 *     <li>Mühlenbein and Paass (1996) - UMDA</li>
 *     <li>Baluja (1994), Sebag and Ducoulombier (1998) - PBIL family</li>
 *     <li>Pelikan, Goldberg, and Cantu-Paz (1999) - BOA</li>
 *     <li>Larrañaga and Lozano (2001) - EDA foundations</li>
 * </ul></p>
 */
package com.knezevic.edaf.v3.models.discrete;
