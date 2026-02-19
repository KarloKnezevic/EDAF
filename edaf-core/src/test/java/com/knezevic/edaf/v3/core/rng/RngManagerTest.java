package com.knezevic.edaf.v3.core.rng;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies deterministic and checkpoint-safe RNG stream behavior.
 */
class RngManagerTest {

    @Test
    void streamDerivationAndSnapshotsAreDeterministic() {
        RngManager managerA = new RngManager(42L);
        RngManager managerB = new RngManager(42L);

        double a1 = managerA.stream("selection").nextDouble();
        double b1 = managerB.stream("selection").nextDouble();
        assertEquals(a1, b1, 1e-12);

        RngSnapshot snapshot = managerA.snapshot();
        double a2 = managerA.stream("selection").nextDouble();

        managerA.restore(snapshot);
        double a2Restored = managerA.stream("selection").nextDouble();
        assertEquals(a2, a2Restored, 1e-12);
    }
}
