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

import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The one item: the bundle. */
public final class ModItems {
    private ModItems() {}

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Dynamite.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Dynamite.MOD_ID);

    /** Stacks like the other things you throw. */
    public static final DeferredItem<DynamiteItem> DYNAMITE =
            ITEMS.registerItem("dynamite", DynamiteItem::new, new Item.Properties().stacksTo(16));

    static void register(IEventBus modBus) {
        TABS.register(modBus);
        ITEMS.register(modBus);
    }

    /** Every usable Dynamite item in its own Creative inventory tab. */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.dynamite"))
            .icon(() -> DYNAMITE.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(DYNAMITE.get());
            })
            .build());
}
