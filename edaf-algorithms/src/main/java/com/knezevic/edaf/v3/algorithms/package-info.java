/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

/**
 * Top-level algorithm drivers and reusable base classes.
 *
 * <p>Drivers in this package execute the canonical EDA loop:
 * <pre>
 *   P_t -> select(elite) -> fit(model) -> sample -> evaluate -> replace -> P_{t+1}
 * </pre>
 * while delegating distribution specifics to {@code Model} implementations.</p>
 *
 * <p>Most concrete drivers are thin, strongly-typed aliases that bind:
 * <ul>
 *     <li>algorithm identity for CLI/config/reporting</li>
 *     <li>compatible representation family</li>
 *     <li>expected probabilistic model class</li>
 * </ul></p>
 */
package com.knezevic.edaf.v3.algorithms;
