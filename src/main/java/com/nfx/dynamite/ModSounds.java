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

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Three sounds: the throw, the fuse hissing, and the bundle landing. The
 * blast itself is the game's own explosion, so it sounds like the one
 * everyone knows. Each is cut from a CC0 recording; see
 * {@code devtools/art/sounds/SOURCES.md}.
 */
public final class ModSounds {
    private ModSounds() {}

    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, Dynamite.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> THROW = sound("throw");
    public static final DeferredHolder<SoundEvent, SoundEvent> FUSE = sound("fuse");
    public static final DeferredHolder<SoundEvent, SoundEvent> LAND = sound("land");

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(Dynamite.MOD_ID, name)));
    }

    static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
