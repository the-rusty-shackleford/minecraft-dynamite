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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Partitions. Length: one tick, forty, none. Going off: before, at, after.
 * Remaining: at lighting, half way, at the end, past it. Hissing: on
 * lighting, between, on the beat, at the end, a bad beat.
 */
final class FuseTest {

    @Test
    void aFuseGoesOffWhenItsTicksAreUpAndNotBefore() {
        Fuse fuse = new Fuse(40);
        assertFalse(fuse.goesOff(0));
        assertFalse(fuse.goesOff(39));
        assertTrue(fuse.goesOff(40));
        assertTrue(fuse.goesOff(41));
        assertTrue(new Fuse(1).goesOff(1));
        assertThrows(IllegalArgumentException.class, () -> new Fuse(0));
    }

    @Test
    void whatIsLeftRunsFromOneToNothing() {
        Fuse fuse = new Fuse(40);
        assertEquals(1.0, fuse.remaining(0), 1e-9);
        assertEquals(0.5, fuse.remaining(20), 1e-9);
        assertEquals(0.0, fuse.remaining(40), 1e-9);
        assertEquals(0.0, fuse.remaining(400), 1e-9);
    }

    @Test
    void itHissesOnLightingAndOnEveryBeatUntilItGoesOff() {
        Fuse fuse = new Fuse(40);
        assertTrue(fuse.hisses(0, 10));
        assertFalse(fuse.hisses(5, 10));
        assertTrue(fuse.hisses(10, 10));
        assertTrue(fuse.hisses(30, 10));
        assertFalse(fuse.hisses(40, 10), "gone off: no more hissing");
        assertThrows(IllegalArgumentException.class, () -> fuse.hisses(0, 0));
    }
}
