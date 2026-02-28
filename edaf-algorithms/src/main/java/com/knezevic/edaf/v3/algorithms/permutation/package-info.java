/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

/**
 * Permutation-focused EDA drivers.
 *
 * <p>Implements routing/order optimization aliases on top of edge, position and ranking models.
 * Typical modeled quantities include:
 * <pre>
 *   P(next = j | current = i),  P(pi | theta, pi0),  P(item at rank k)
 * </pre>
 * to capture adjacency, consensus distance and positional preferences.</p>
 */
package com.knezevic.edaf.v3.algorithms.permutation;
