/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

/**
 * Event bus, run events and sink contracts used for observability and persistence.
 *
 * <p>Supports synchronous and asynchronous sinks so optimization loops remain decoupled from telemetry transport/storage concerns.</p>
 */
package com.knezevic.edaf.v3.core.events;
