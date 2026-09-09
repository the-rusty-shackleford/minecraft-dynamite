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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The one entity: a bundle in flight or lying lit. */
public final class ModEntities {
    private ModEntities() {}

    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Dynamite.MOD_ID);

    /** A snowball's size, tracked as closely, so the bounce reads right. */
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownDynamite>> THROWN_DYNAMITE =
            ENTITIES.register("thrown_dynamite", () -> EntityType.Builder.<ThrownDynamite>of(ThrownDynamite::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .eyeHeight(0.125f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(Dynamite.MOD_ID + ":thrown_dynamite"));

    static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }
}
