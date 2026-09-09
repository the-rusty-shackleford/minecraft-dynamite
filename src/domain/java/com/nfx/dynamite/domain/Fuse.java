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

/**
 * A fuse: lit the moment the bundle leaves the hand, and burning for a fixed
 * number of ticks wherever the bundle goes -- through the air, off a wall,
 * along the ground. It does not go off on impact; it goes off when it is due.
 *
 * <p>RI: {@code ticks >= 1}.<br>
 * AF: AF(ticks) = "a fuse that burns for {@code ticks} ticks".
 */
public record Fuse(int ticks) {
    public Fuse {
        if (ticks < 1) {
            throw new IllegalArgumentException("a fuse burns for at least one tick, not " + ticks);
        }
    }

    /** effects: returns whether the bundle goes off, {@code lit} ticks after lighting */
    public boolean goesOff(int lit) {
        return lit >= ticks;
    }

    /** effects: returns how much of the fuse is left, {@code lit} ticks after lighting, from 1 down to 0 */
    public double remaining(int lit) {
        return Math.max(0.0, Math.min(1.0, 1.0 - (double) lit / ticks));
    }

    /**
     * effects: returns whether the fuse hisses this tick: on lighting and
     * every {@code every} ticks after, so a short hiss sound played each
     * time runs on without gaps
     */
    public boolean hisses(int lit, int every) {
        if (every < 1) {
            throw new IllegalArgumentException("every must be >= 1, was " + every);
        }
        return lit >= 0 && !goesOff(lit) && lit % every == 0;
    }
}
