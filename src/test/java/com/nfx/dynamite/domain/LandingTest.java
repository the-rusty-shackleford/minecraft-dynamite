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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * What a full-speed throw looks like when it lands, measured by playing the
 * game's own projectile tick over a flat floor with {@link Bounce} at each
 * contact. The game ({@code ThrowableProjectile.tick}, 1.21.1) finds the hit
 * along the tick's move, reflects, moves by the new velocity, then scales it
 * by 0.99 and takes 0.03 off it for gravity; the bundle is moved to the point
 * of contact before it reflects, as {@code ThrownDynamite.onHitBlock} does.
 *
 * <p>Partitions: the throw's angle -- steep down, down, nearly level, lobbed
 * up (a long flight, landing at the same speed). The property is the same at
 * each: at most one hop, and a small one; a short tumble; still within a few
 * ticks. "Way too bouncy" was two blocks, thirteen blocks and thirty-six ticks.
 */
final class LandingTest {
    private static final double SPEED = 1.5;      // a throw, blocks per tick
    private static final double EYE = 1.62;       // where a throw starts, above the floor
    private static final double DRAG = 0.99;
    private static final double GRAVITY = 0.03;

    /** One landing: where and when it first touched, its highest point after, how far it went, when it stopped. */
    private record Landing(int landedAt, double landedX, double landedSpeed, double hopHeight, double tumble, int restAt, int hops) {}

    private static Landing throwAt(double pitchDegrees) {
        double vx = SPEED * Math.cos(Math.toRadians(pitchDegrees));
        double vy = -SPEED * Math.sin(Math.toRadians(pitchDegrees));
        double x = 0.0;
        double y = EYE;     // the box's bottom; the floor's top is 0
        int landedAt = -1;
        double landedX = 0.0;
        double landedSpeed = 0.0;
        double peak = 0.0;
        int restAt = -1;
        int hops = 0;
        List<Double> impacts = new ArrayList<>();
        for (int t = 0; t < 400 && restAt < 0; t++) {
            if (vy < 0 && y + vy < 0.0) {
                x += vx * (y / -vy);        // to the point of contact
                y = 0.0;
                impacts.add(-vy);
                if (landedAt < 0) {
                    landedAt = t;
                    landedX = x;
                    landedSpeed = -vy;
                } else if (-vy > 0.05) {
                    hops++;
                }
                Bounce.Result r = Bounce.off(vx, vy, 0.0, 0, 1, 0);
                vx = r.vx();
                vy = r.vy();
                if (r.atRest()) {
                    restAt = t;
                }
            }
            x += vx;
            y += vy;
            if (landedAt >= 0) {
                peak = Math.max(peak, y);
            }
            vx *= DRAG;
            vy = vy * DRAG - GRAVITY;
        }
        assertTrue(landedAt >= 0 && restAt >= 0, "it landed and came to rest: " + impacts);
        return new Landing(landedAt, landedX, landedSpeed, peak, x - landedX, restAt, hops);
    }

    /** Steep down, down, nearly level, lobbed up. */
    private static final double[] PITCHES = {45.0, 30.0, 10.0, -10.0};

    @Test
    void aFullSpeedThrowThudsHopsOnceAtMostAndLiesStillWithinABlockAndAHalf() {
        for (double pitch : PITCHES) {
            Landing l = throwAt(pitch);
            String at = " (thrown " + pitch + " degrees down)";
            assertTrue(l.hops() <= 1, "at most one hop after landing, had " + l.hops() + at);
            assertTrue(l.hopHeight() <= 0.2, "the hop is small: " + l.hopHeight() + " blocks" + at);
            assertTrue(l.tumble() <= 1.5, "a short tumble: " + l.tumble() + " blocks" + at);
            assertTrue(l.restAt() - l.landedAt() <= 8,
                    "still within eight ticks of landing, took " + (l.restAt() - l.landedAt()) + at);
            assertTrue(l.tumble() > 0.3, "but it does not stop dead on the spot: " + l.tumble() + at);
        }
    }

    @Test
    void theHardestAndTheLongestThrowsLandAtSpeed() {
        // A steep throw from the hand and a lob after a long flight both
        // arrive at over a third of a block a tick, so the numbers above are
        // measured at speed, not at a drop.
        for (double pitch : new double[] {45.0, -10.0}) {
            Landing l = throwAt(pitch);
            assertTrue(l.landedSpeed() > 0.35, "landed at " + l.landedSpeed() + " (thrown " + pitch + ")");
        }
    }
}
