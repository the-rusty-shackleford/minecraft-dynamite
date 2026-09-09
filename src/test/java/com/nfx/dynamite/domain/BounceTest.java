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
 * Partitions. Surface: floor, ceiling, each wall. Motion: straight in,
 * glancing, already moving away, fast (bounces), slow on a floor (rests),
 * slow on a wall (does not rest). A bad normal.
 */
final class BounceTest {

    @Test
    void aBundleThrownDownAtAFloorComesBackUpAtAThirdOfItsSpeed() {
        Bounce.Result r = Bounce.off(0.0, -1.0, 0.0, 0, 1, 0);
        assertEquals(0.0, r.vx(), 1e-9);
        assertEquals(Bounce.RESTITUTION, r.vy(), 1e-9);
        assertEquals(0.0, r.vz(), 1e-9);
        assertFalse(r.atRest());
    }

    @Test
    void aGlancingHitKeepsMostOfItsSlideAndLittleOfItsDrop() {
        Bounce.Result r = Bounce.off(1.0, -0.5, 0.2, 0, 1, 0);
        assertEquals(1.0 * Bounce.FRICTION, r.vx(), 1e-9);
        assertEquals(0.5 * Bounce.RESTITUTION, r.vy(), 1e-9);
        assertEquals(0.2 * Bounce.FRICTION, r.vz(), 1e-9);
    }

    @Test
    void wallsAndCeilingsSendItBackTheOtherWay() {
        assertEquals(-1.0 * Bounce.RESTITUTION, Bounce.off(1.0, 0.0, 0.0, -1, 0, 0).vx(), 1e-9, "west wall");
        assertEquals(1.0 * Bounce.RESTITUTION, Bounce.off(-1.0, 0.0, 0.0, 1, 0, 0).vx(), 1e-9, "east wall");
        assertEquals(-0.8 * Bounce.RESTITUTION, Bounce.off(0.0, 0.8, 0.0, 0, -1, 0).vy(), 1e-9, "ceiling");
        assertEquals(-0.3 * Bounce.RESTITUTION, Bounce.off(0.0, 0.0, 0.3, 0, 0, -1).vz(), 1e-9, "north wall");
    }

    @Test
    void aBundleAlreadyMovingAwayIsNotTurnedRound() {
        Bounce.Result r = Bounce.off(0.0, 0.5, 0.0, 0, 1, 0);
        assertEquals(0.5, r.vy(), 1e-9);
    }

    @Test
    void aSlowBundleOnAFloorComesToRestButNotOnAWall() {
        Bounce.Result floor = Bounce.off(0.02, -0.05, 0.0, 0, 1, 0);
        assertTrue(floor.atRest());
        assertEquals(0.0, floor.speed(), 1e-9);
        Bounce.Result wall = Bounce.off(-0.05, 0.0, 0.0, 1, 0, 0);
        assertFalse(wall.atRest(), "a wall is not somewhere to lie");
        assertTrue(wall.speed() > 0.0);
    }

    @Test
    void theNormalMustBeAnAxis() {
        assertThrows(IllegalArgumentException.class, () -> Bounce.off(0, -1, 0, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> Bounce.off(0, -1, 0, 1, 1, 0));
    }
}
