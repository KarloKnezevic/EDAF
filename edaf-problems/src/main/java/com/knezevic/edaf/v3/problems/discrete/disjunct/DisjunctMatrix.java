package com.knezevic.edaf.v3.problems.discrete.disjunct;

import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Arrays;
import java.util.BitSet;

/**
 * Immutable binary matrix view optimized for column-support operations used by
 * disjunct/resolvable/almost-disjunct definitions.
 *
 * <p>The matrix is represented by support bitsets of each column:
 * {@code supp(x_j) = { i in [M] : A[i,j] = 1 }}.</p>
 */
public final class DisjunctMatrix {

    private final int rows;
    private final int columns;
    private final BitSet[] columnSupports;

    private DisjunctMatrix(int rows, int columns, BitSet[] columnSupports) {
        this.rows = rows;
        this.columns = columns;
        this.columnSupports = new BitSet[columnSupports.length];
        for (int i = 0; i < columnSupports.length; i++) {
            this.columnSupports[i] = (BitSet) columnSupports[i].clone();
        }
    }

    /**
     * Builds matrix from a bitstring encoded in column-major layout:
     * bits {@code [j * M, j * M + 1, ..., j * M + (M-1)]} correspond to column {@code j}.
     */
    public static DisjunctMatrix fromBitString(BitString genotype, int rows, int columns) {
        if (rows <= 0) {
            throw new IllegalArgumentException("rows (M) must be > 0");
        }
        if (columns <= 0) {
            throw new IllegalArgumentException("columns (N) must be > 0");
        }
        int requiredLength = Math.multiplyExact(rows, columns);
        if (genotype.length() != requiredLength) {
            throw new IllegalArgumentException(
                    "Bitstring length must equal M*N (" + requiredLength + "), got " + genotype.length()
            );
        }

        boolean[] genes = genotype.genes();
        BitSet[] supports = new BitSet[columns];
        for (int column = 0; column < columns; column++) {
            BitSet support = new BitSet(rows);
            int offset = column * rows;
            for (int row = 0; row < rows; row++) {
                if (genes[offset + row]) {
                    support.set(row);
                }
            }
            supports[column] = support;
        }
        return new DisjunctMatrix(rows, columns, supports);
    }

    /**
     * Builds matrix from dense row-major values where {@code values[row][column]} is matrix entry.
     */
    public static DisjunctMatrix fromDense(boolean[][] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("matrix must contain at least one row");
        }
        int rows = values.length;
        int columns = values[0].length;
        if (columns == 0) {
            throw new IllegalArgumentException("matrix must contain at least one column");
        }
        for (int row = 0; row < rows; row++) {
            if (values[row] == null || values[row].length != columns) {
                throw new IllegalArgumentException("matrix rows must be non-null and rectangular");
            }
        }

        BitSet[] supports = new BitSet[columns];
        Arrays.setAll(supports, idx -> new BitSet(rows));
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (values[row][column]) {
                    supports[column].set(row);
                }
            }
        }
        return new DisjunctMatrix(rows, columns, supports);
    }

    public int rows() {
        return rows;
    }

    public int columns() {
        return columns;
    }

    /**
     * Returns a defensive copy of support bitset for one column.
     */
    public BitSet support(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= columns) {
            throw new IllegalArgumentException("column index out of bounds: " + columnIndex);
        }
        return (BitSet) columnSupports[columnIndex].clone();
    }

    BitSet supportRef(int columnIndex) {
        return columnSupports[columnIndex];
    }
}
