/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

/**
 * Probabilistic models for continuous search spaces.
 *
 * <p>Implemented model families include:
 * <ul>
 *     <li>Gaussian estimators (diagonal and full covariance)</li>
 *     <li>mixture and non-parametric estimators (GMM, KDE)</li>
 *     <li>dependence-separating estimators (Gaussian copula baseline)</li>
 *     <li>natural-gradient evolution-strategy models (sNES, xNES, CMA-ES)</li>
 *     <li>flow-like transformed latent Gaussian baseline</li>
 * </ul>
 *
 * <p>Core formulas used throughout the package:
 * <pre>
 *   x ~ N(mu, Sigma)
 *   Sigma <- (1 - alpha) Sigma + alpha * Sigma_hat
 *   p(x) = sum_k pi_k N(x | mu_k, Sigma_k)
 * </pre>
 * with regularization and Cholesky-based sampling for numerical stability.</p>
 *
 * <p>References:
 * <ul>
 *     <li>Mühlenbein, Bendisch, and Voigt (1996) - UMDAc</li>
 *     <li>Hansen and Ostermeier (2001) - CMA-ES</li>
 *     <li>Wierstra et al. (2014) - NES</li>
 *     <li>Luo and Qian (2009) - KDE-based EDAs</li>
 * </ul></p>
 */
package com.knezevic.edaf.v3.models.continuous;
