package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * KDE-EDA driver
 */
public final class KdeEdaAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    public KdeEdaAlgorithm(double selectionRatio) {
        super("kde-eda", selectionRatio);
    }
}
