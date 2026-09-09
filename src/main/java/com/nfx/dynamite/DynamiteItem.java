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

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;

/**
 * The bundle in the hand. Right-click lights it and throws it, snowball
 * fashion, with a short cooldown so an area is not carpeted in a second;
 * a dispenser throws it the same way.
 */
public final class DynamiteItem extends Item implements ProjectileItem {
    /** Blocks per tick off the hand: a snowball's speed. */
    public static final float THROW_SPEED = 1.5f;
    /** How wide of the aim a throw can go: none. */
    public static final float THROW_SPREAD = 1.0f;

    public DynamiteItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.THROW.get(), SoundSource.NEUTRAL,
                0.5f, 0.9f + level.getRandom().nextFloat() * 0.2f);
        int cooldown = DynamiteConfig.COOLDOWN_TICKS.get();
        if (cooldown > 0) {
            player.getCooldowns().addCooldown(this, cooldown);
        }
        if (!level.isClientSide()) {
            ThrownDynamite thrown = new ThrownDynamite(player, level);
            thrown.setItem(stack);
            thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, THROW_SPEED, THROW_SPREAD);
            level.addFreshEntity(thrown);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        stack.consume(1, player);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        ThrownDynamite thrown = new ThrownDynamite(pos.x(), pos.y(), pos.z(), level);
        thrown.setItem(stack);
        return thrown;
    }
}
