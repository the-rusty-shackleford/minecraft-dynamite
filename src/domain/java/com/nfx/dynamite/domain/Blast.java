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
 * What goes off: an explosion of a given power, half a block of TNT's four
 * by default -- enough to clear a little area, not to level a hillside --
 * and whether it breaks blocks at all.
 *
 * <p>RI: {@code power > 0}.<br>
 * AF: AF(power, breaksBlocks) = "an explosion of {@code power}, breaking
 * blocks iff {@code breaksBlocks}".
 */
public record Blast(float power, boolean breaksBlocks) {
    /** TNT's power, for scale. */
    public static final float TNT = 4.0f;
    /** The game's explosion rays lose this much per {@code 0.3} blocks of air. */
    private static final double DECAY_PER_STEP = 0.225;
    private static final double STEP = 0.3;

    public Blast {
        if (!(power > 0.0f) || Float.isInfinite(power)) {
            throw new IllegalArgumentException("power must be finite and > 0, was " + power);
        }
    }

    /**
     * effects: returns how far, in blocks, the strongest ray of this blast
     * reaches through open air before it is spent: the game's rays start at
     * up to 1.3 times the power and lose {@code 0.225} per {@code 0.3}
     * blocks. Nothing beyond it can be touched; blocks in the way stop rays
     * far sooner.
     */
    public double reach() {
        return 1.3 * power / DECAY_PER_STEP * STEP;
    }

    /** effects: returns this blast as a fraction of TNT's */
    public double ofTnt() {
        return power / TNT;
    }
}
