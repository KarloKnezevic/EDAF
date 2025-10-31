package com.knezevic.edaf.core.spi;

import com.knezevic.edaf.core.api.Selection;

/**
 * SPI for discovering selection strategies (tournament, roulette, truncation, etc.).
 */
public interface SelectionProvider {

    /**
     * Unique identifier used in configuration (e.g. "tournament", "roulette").
     */
    String id();

    /**
     * Returns a selection strategy instance.
     */
    Selection<?> create();
}


