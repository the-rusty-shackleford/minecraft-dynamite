/*
 * Dynamite - a three-stick bundle you can throw.
 * Copyright (C) 2026 Rusty Shackleford and nfx
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package com.nfx.dynamite.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Partitions. Power: the default, TNT's, bad. Reach: scales with power. */
final class BlastTest {

    @Test
    void theDefaultIsHalfATntAndReachesAFewBlocksThroughAir() {
        Blast blast = new Blast(2.0f, true);
        assertEquals(0.5, blast.ofTnt(), 1e-9);
        assertEquals(1.3 * 2.0 / 0.225 * 0.3, blast.reach(), 1e-9);
        assertTrue(blast.reach() > 3.0 && blast.reach() < 4.0, "a little area: " + blast.reach());
        assertEquals(2.0 * blast.reach(), new Blast(4.0f, true).reach(), 1e-9, "reach scales with power");
    }

    @Test
    void badPowerIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new Blast(0.0f, true));
        assertThrows(IllegalArgumentException.class, () -> new Blast(-1.0f, true));
        assertThrows(IllegalArgumentException.class, () -> new Blast(Float.POSITIVE_INFINITY, true));
    }
}
