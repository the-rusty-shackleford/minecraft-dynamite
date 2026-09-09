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
package com.nfx.dynamite;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * The server's knobs, {@code serverconfig/dynamite-server.toml} in the
 * world: how long a fuse burns, how hard a bundle goes off, whether it
 * breaks blocks, and how fast bundles can be thrown.
 */
public final class DynamiteConfig {
    private DynamiteConfig() {}

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue FUSE_TICKS;
    public static final ModConfigSpec.DoubleValue POWER;
    public static final ModConfigSpec.BooleanValue BREAK_BLOCKS;
    public static final ModConfigSpec.IntValue COOLDOWN_TICKS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("dynamite");
        FUSE_TICKS = b.comment("Ticks from the throw to the blast; 40 is two seconds.")
                .defineInRange("fuseTicks", 40, 5, 400);
        POWER = b.comment("The blast's power. A block of TNT is 4; 2 clears a small area.")
                .defineInRange("power", 2.0, 0.5, 8.0);
        BREAK_BLOCKS = b.comment("Whether the blast breaks blocks. Off, it only hurts what stands near it.")
                .define("breakBlocks", true);
        COOLDOWN_TICKS = b.comment("Ticks a player must wait between throws.")
                .defineInRange("cooldownTicks", 10, 0, 200);
        b.pop();
        SPEC = b.build();
    }
}
