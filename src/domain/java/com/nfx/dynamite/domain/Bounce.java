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
 * How a thrown bundle meets a surface: it bounces, losing most of its speed
 * into the surface and some along it, and once it is barely moving on a
 * floor it comes to rest and lies there with its fuse burning.
 */
public final class Bounce {
    private Bounce() {}

    /** How much of the speed into a surface comes back out of it. */
    public static final double RESTITUTION = 0.35;
    /** How much of the speed along a surface survives the bounce: a paper bundle skids little. */
    public static final double FRICTION = 0.45;
    /** Below this speed, in blocks per tick, a bundle on a floor stops. */
    public static final double REST_SPEED = 0.08;

    /**
     * The velocity after a bounce, and whether the bundle has come to rest.
     *
     * @param vx     velocity after, blocks per tick
     * @param vy     velocity after
     * @param vz     velocity after
     * @param atRest whether the bundle now lies still
     */
    public record Result(double vx, double vy, double vz, boolean atRest) {
        /** effects: returns the speed after, in blocks per tick */
        public double speed() {
            return Math.sqrt(vx * vx + vy * vy + vz * vz);
        }
    }

    /**
     * effects: returns the bounce of a bundle moving at {@code (vx, vy, vz)}
     * off a surface whose outward normal is the axis {@code (nx, ny, nz)},
     * one of the six unit directions: the component into the surface comes
     * back at {@link #RESTITUTION}, the rest is kept at {@link #FRICTION};
     * on a floor (normal up) a bundle slower than {@link #REST_SPEED} after
     * the bounce is at rest with no velocity at all<br>
     * throws: {@link IllegalArgumentException} if the normal is not a unit axis
     */
    public static Result off(double vx, double vy, double vz, int nx, int ny, int nz) {
        if (Math.abs(nx) + Math.abs(ny) + Math.abs(nz) != 1) {
            throw new IllegalArgumentException("the normal must be a unit axis, was " + nx + "," + ny + "," + nz);
        }
        double into = vx * nx + vy * ny + vz * nz;   // negative when moving into the surface
        double rx = vx - into * nx;
        double ry = vy - into * ny;
        double rz = vz - into * nz;
        double back = into < 0 ? -into * RESTITUTION : into;   // a bundle moving away already keeps that
        double ox = rx * FRICTION + back * nx;
        double oy = ry * FRICTION + back * ny;
        double oz = rz * FRICTION + back * nz;
        Result result = new Result(ox, oy, oz, false);
        if (ny == 1 && result.speed() < REST_SPEED) {
            return new Result(0.0, 0.0, 0.0, true);
        }
        return result;
    }
}
