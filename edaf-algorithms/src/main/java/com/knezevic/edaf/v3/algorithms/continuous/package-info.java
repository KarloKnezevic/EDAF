/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

/**
 * Continuous-space EDA and NES/CMA-style drivers.
 *
 * <p>Includes Gaussian, mixture, KDE, copula, CEM and natural-gradient strategy variants.
 * All drivers preserve the same elite-fit-sample lifecycle while deferring update equations to
 * model implementations such as:
 * <pre>
 *   x ~ N(mu, Sigma),   mu <- mu + eta * grad_mu,   Sigma <- Sigma + eta * grad_Sigma
 * </pre>
 * depending on the chosen model family.</p>
 */
package com.knezevic.edaf.v3.algorithms.continuous;
