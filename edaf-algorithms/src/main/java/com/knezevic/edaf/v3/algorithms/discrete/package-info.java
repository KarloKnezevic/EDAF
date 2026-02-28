/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

/**
 * Discrete and binary EDA drivers.
 *
 * <p>Contains execution drivers for:
 * <ul>
 *     <li>factorized updates (UMDA, PBIL, cGA)</li>
 *     <li>tree/dependency models (MIMIC, BMDA, dependency-tree)</li>
 *     <li>Bayesian-network models (BOA, EBNA, hBOA-style)</li>
 * </ul>
 * Each driver reuses the same lifecycle and differs by model family and alias identity.</p>
 */
package com.knezevic.edaf.v3.algorithms.discrete;
