package com.knezevic.edaf.v3.repr.grammar.encoding;

/**
 * Cyclic bit reader used for deterministic genotype decoding.
 */
final class BitCursor {

    private final boolean[] bits;
    private int offset;

    BitCursor(boolean[] bits) {
        this.bits = bits == null ? new boolean[0] : bits;
        this.offset = 0;
    }

    int nextInt(int width) {
        if (width <= 0) {
            return 0;
        }
        if (bits.length == 0) {
            return 0;
        }

        int value = 0;
        for (int i = 0; i < width; i++) {
            value <<= 1;
            if (nextBit()) {
                value |= 1;
            }
        }
        return value;
    }

    int consumedBits() {
        return offset;
    }

    private boolean nextBit() {
        boolean value = bits[offset % bits.length];
        offset++;
        return value;
    }
}
