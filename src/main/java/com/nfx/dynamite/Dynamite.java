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

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/**
 * A three-stick bundle of dynamite you can throw. Lit as it leaves the
 * hand, it flies, bounces, comes to rest, hisses, and goes off two seconds
 * after the throw with half the power of a block of TNT: enough to clear a
 * little area, precisely, without laying and lighting a block. Crafted
 * from gunpowder, paper and string. Dispensers throw it too.
 *
 * <p>Needed on the server and on every client: the bundle in flight is an
 * entity.
 */
@Mod(Dynamite.MOD_ID)
public final class Dynamite {
    public static final String MOD_ID = "dynamite";

    public Dynamite(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, DynamiteConfig.SPEC);
        ModItems.register(modBus);
        ModEntities.register(modBus);
        ModSounds.register(modBus);
        modBus.addListener(Dynamite::onCommonSetup);
        modBus.addListener(Dynamite::onBuildCreativeTabs);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        // A dispenser lights and throws a bundle the way it throws a snowball.
        event.enqueueWork(() -> DispenserBlock.registerProjectileBehavior(ModItems.DYNAMITE.get()));
    }

    private static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.DYNAMITE.get());
        }
    }
}
