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
package com.nfx.dynamite.gametest;

import com.nfx.dynamite.Dynamite;
import com.nfx.dynamite.DynamiteConfig;
import com.nfx.dynamite.ModEntities;
import com.nfx.dynamite.ModItems;
import com.nfx.dynamite.ThrownDynamite;
import com.nfx.dynamite.domain.Blast;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/**
 * Dynamite on a real server, in a fifteen-block arena whose floor the test
 * lays itself: four layers of dirt, which a blast bites into deeper than
 * stone, so the crater is plain to see.
 *
 * <p>A bundle dropped on the floor lies still, hissing, and goes off when
 * its fuse is up and not before, leaving a crater under it and nothing
 * touched beyond the blast's reach. A bundle thrown at a wall bounces off
 * it. A dispenser throws a lit bundle. A player's stack shrinks by one per
 * throw, a creative player's does not, and a throw starts the cooldown.
 * With block breaking off, the floor is whole after the blast. The config
 * is one for the server, so tests needing different settings run in
 * batches.
 */
@GameTestHolder(Dynamite.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DynamiteGameTests {
    private static final int SIZE = 15;
    private static final int FLOOR = 4;
    private static final int MID = SIZE / 2;

    public DynamiteGameTests() {}

    /** Four layers of dirt across the arena, the top at y = FLOOR - 1. */
    private static void layFloor(GameTestHelper helper) {
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                for (int y = 0; y < FLOOR; y++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.DIRT);
                }
            }
        }
    }

    /** A lit bundle set on the floor at the arena's middle, still. */
    private static ThrownDynamite drop(GameTestHelper helper) {
        Vec3 at = helper.absoluteVec(new Vec3(MID + 0.5, FLOOR + 0.6, MID + 0.5));
        ThrownDynamite bundle = new ThrownDynamite(at.x, at.y, at.z, helper.getLevel());
        bundle.setItem(new ItemStack(ModItems.DYNAMITE.get()));
        bundle.setDeltaMovement(0.0, 0.0, 0.0);
        helper.getLevel().addFreshEntity(bundle);
        return bundle;
    }

    // The config is one for the whole server and the tests run in parallel,
    // so those needing different settings run in batches, one after another.

    /** A short fuse and a real blast. */
    @BeforeBatch(batch = "blast")
    public static void blast(ServerLevel level) {
        configure(40, 2.0, true);
    }

    /** A short fuse, no block breaking. */
    @BeforeBatch(batch = "gentle")
    public static void gentle(ServerLevel level) {
        configure(40, 2.0, false);
    }

    /** A long fuse: time to watch a bundle fly, bounce and settle. */
    @BeforeBatch(batch = "flight")
    public static void flight(ServerLevel level) {
        configure(100, 2.0, true);
    }

    private static void configure(int fuseTicks, double power, boolean breakBlocks) {
        DynamiteConfig.FUSE_TICKS.set(fuseTicks);
        DynamiteConfig.POWER.set(power);
        DynamiteConfig.BREAK_BLOCKS.set(breakBlocks);
        DynamiteConfig.COOLDOWN_TICKS.set(10);
    }

    @GameTest(template = "arena", batch = "blast", timeoutTicks = 200)
    public void aBundleOnTheFloorGoesOffWhenItsFuseIsUpAndLeavesACrater(GameTestHelper helper) {
        layFloor(helper);
        ThrownDynamite bundle = drop(helper);
        BlockPos under = new BlockPos(MID, FLOOR - 1, MID);
        BlockPos far = new BlockPos(MID + 6, FLOOR - 1, MID);
        double reach = new Blast(2.0f, true).reach();
        helper.runAtTickTime(30, () -> {
            helper.assertTrue(bundle.isAlive(), "the bundle is still there at tick 30 of a 40-tick fuse");
            helper.assertBlockPresent(Blocks.DIRT, under);
        });
        helper.runAtTickTime(48, () -> {
            helper.assertFalse(bundle.isAlive(), "the bundle has gone off");
            helper.assertBlockPresent(Blocks.AIR, under);
            helper.assertBlockPresent(Blocks.DIRT, far);
            helper.assertTrue(6 > reach, "the far block is beyond the blast's reach of " + reach);
            helper.succeed();
        });
    }

    @GameTest(template = "arena", batch = "flight", timeoutTicks = 120)
    public void aBundleThrownAtAWallBouncesBack(GameTestHelper helper) {
        layFloor(helper);
        for (int y = FLOOR; y < FLOOR + 4; y++) {
            for (int z = 0; z < SIZE; z++) {
                helper.setBlock(new BlockPos(SIZE - 1, y, z), Blocks.STONE);   // the east wall
            }
        }
        Vec3 at = helper.absoluteVec(new Vec3(MID + 0.5, FLOOR + 1.5, MID + 0.5));
        ThrownDynamite bundle = new ThrownDynamite(at.x, at.y, at.z, helper.getLevel());
        bundle.setItem(new ItemStack(ModItems.DYNAMITE.get()));
        bundle.setDeltaMovement(0.8, 0.1, 0.0);   // eastward, into the wall
        helper.getLevel().addFreshEntity(bundle);
        helper.runAtTickTime(20, () -> {
            helper.assertTrue(bundle.isAlive(), "still lit");
            helper.assertTrue(bundle.getX() < helper.absolutePos(new BlockPos(SIZE - 1, 0, 0)).getX(), "it did not pass through the wall");
            helper.assertTrue(bundle.getDeltaMovement().x <= 0.0 || bundle.isResting(),
                    "it came back off the wall or lies still; velocity x " + bundle.getDeltaMovement().x);
            helper.succeed();
        });
    }

    @GameTest(template = "arena", batch = "flight", timeoutTicks = 200)
    public void aBundleComesToRestOnTheFloorBeforeItGoesOff(GameTestHelper helper) {
        layFloor(helper);
        Vec3 at = helper.absoluteVec(new Vec3(3.5, FLOOR + 2.0, MID + 0.5));
        ThrownDynamite bundle = new ThrownDynamite(at.x, at.y, at.z, helper.getLevel());
        bundle.setItem(new ItemStack(ModItems.DYNAMITE.get()));
        bundle.setDeltaMovement(0.3, 0.0, 0.0);
        helper.getLevel().addFreshEntity(bundle);
        helper.runAtTickTime(60, () -> {
            helper.assertTrue(bundle.isAlive(), "still lit at 60 of 100");
            helper.assertTrue(bundle.isResting(), "lying still by now; velocity " + bundle.getDeltaMovement());
            helper.assertTrue(bundle.getY() < at.y, "it fell to the floor");
            helper.assertTrue(bundle.getX() > at.x + 1.0, "and travelled some way first: " + (bundle.getX() - at.x));
            helper.succeed();
        });
    }

    @GameTest(template = "arena", batch = "flight", timeoutTicks = 100)
    public void aFullSpeedThrowThudsDownAndLiesStillWithinAFewBlocks(GameTestHelper helper) {
        // A throw from the hand, 30 degrees down at the item's 1.5 blocks a
        // tick, starting at eye height: it lands two or three blocks out,
        // hops at most a fifth of a block off the floor, tumbles under a
        // block and a half, and lies still. LandingTest measures the same
        // off-game; this is the game agreeing, contact point and all.
        layFloor(helper);
        Vec3 at = helper.absoluteVec(new Vec3(1.5, FLOOR + 1.62, MID + 0.5));
        double floorTop = helper.absolutePos(new BlockPos(0, FLOOR, 0)).getY();
        ThrownDynamite bundle = new ThrownDynamite(at.x, at.y, at.z, helper.getLevel());
        bundle.setItem(new ItemStack(ModItems.DYNAMITE.get()));
        bundle.setDeltaMovement(1.5 * Math.cos(Math.toRadians(30.0)), -1.5 * Math.sin(Math.toRadians(30.0)), 0.0);
        helper.getLevel().addFreshEntity(bundle);
        double[] highestAfterLanding = {Double.NEGATIVE_INFINITY};
        boolean[] landed = {false};
        for (int t = 1; t <= 40; t++) {
            helper.runAtTickTime(t, () -> {
                if (bundle.getY() <= floorTop + 0.01) {
                    landed[0] = true;
                }
                if (landed[0]) {
                    highestAfterLanding[0] = Math.max(highestAfterLanding[0], bundle.getY() - floorTop);
                }
            });
        }
        helper.runAtTickTime(41, () -> {
            helper.assertTrue(bundle.isAlive(), "still lit");
            helper.assertTrue(landed[0], "it touched the floor");
            helper.assertTrue(bundle.isResting(), "lying still by now; velocity " + bundle.getDeltaMovement());
            helper.assertTrue(bundle.getY() <= floorTop + 0.01, "on the floor, not above it: " + (bundle.getY() - floorTop));
            helper.assertTrue(highestAfterLanding[0] <= 0.3, "no hop over a third of a block after landing: " + highestAfterLanding[0]);
            double travelled = bundle.getX() - at.x;
            helper.assertTrue(travelled >= 3.0 && travelled <= 5.5, "landed two or three blocks out and tumbled under a block and a half: " + travelled);
            helper.succeed();
        });
    }

    @GameTest(template = "arena", batch = "flight", timeoutTicks = 100)
    public void aDispenserThrowsALitBundle(GameTestHelper helper) {
        layFloor(helper);
        BlockPos dispenser = new BlockPos(2, FLOOR + 1, MID);
        helper.setBlock(dispenser, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.EAST));
        DispenserBlockEntity be = helper.getBlockEntity(dispenser);
        be.setItem(4, new ItemStack(ModItems.DYNAMITE.get(), 3));
        helper.pulseRedstone(dispenser.above(), 2);
        helper.runAtTickTime(10, () -> {
            List<ThrownDynamite> thrown = helper.getLevel().getEntitiesOfClass(ThrownDynamite.class, helper.getBounds());
            helper.assertTrue(thrown.size() == 1, "one bundle in the air, had " + thrown.size());
            helper.assertTrue(thrown.get(0).getX() > helper.absolutePos(dispenser).getX() + 0.5, "flying east, out of the dispenser");
            helper.assertTrue(be.getItem(4).getCount() == 2, "one bundle taken from the dispenser");
            helper.succeed();
        });
    }

    @GameTest(template = "arena", batch = "flight", timeoutTicks = 100)
    public void aThrowSpendsOneBundleStartsTheCooldownAndCreativeSpendsNone(GameTestHelper helper) {
        layFloor(helper);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(helper.absoluteVec(new Vec3(3.5, FLOOR, MID + 0.5)));
        player.setYRot(-90.0f);   // facing east
        ItemStack stack = new ItemStack(ModItems.DYNAMITE.get(), 5);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        ModItems.DYNAMITE.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 4, "one spent");
        helper.assertTrue(player.getCooldowns().isOnCooldown(ModItems.DYNAMITE.get()), "the cooldown started");
        Player creative = helper.makeMockPlayer(GameType.CREATIVE);
        creative.getAbilities().instabuild = true;   // a mock player's mode does not set its abilities
        creative.setPos(helper.absoluteVec(new Vec3(3.5, FLOOR, MID + 3.5)));
        creative.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.DYNAMITE.get(), 5));
        ModItems.DYNAMITE.get().use(helper.getLevel(), creative, InteractionHand.MAIN_HAND);
        helper.assertTrue(creative.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 5, "creative spends none");
        helper.runAtTickTime(5, () -> {
            List<ThrownDynamite> thrown = helper.getLevel().getEntitiesOfClass(ThrownDynamite.class, helper.getBounds());
            helper.assertTrue(thrown.size() == 2, "two bundles thrown, had " + thrown.size());
            helper.succeed();
        });
    }

    @GameTest(template = "arena", batch = "gentle", timeoutTicks = 200)
    public void withBlockBreakingOffTheFloorIsWholeAfterTheBlast(GameTestHelper helper) {
        layFloor(helper);
        ThrownDynamite bundle = drop(helper);
        BlockPos under = new BlockPos(MID, FLOOR - 1, MID);
        helper.runAtTickTime(48, () -> {
            helper.assertFalse(bundle.isAlive(), "the bundle has gone off");
            helper.assertBlockPresent(Blocks.DIRT, under);
            helper.succeed();
        });
    }

    @GameTest(template = "arena", batch = "flight", timeoutTicks = 100)
    public void theEntityTypeIsTheModsAndDrawsItsItem(GameTestHelper helper) {
        ThrownDynamite bundle = drop(helper);
        helper.assertTrue(bundle.getType() == ModEntities.THROWN_DYNAMITE.get(), "the bundle's type");
        helper.assertTrue(bundle.getItem().is(ModItems.DYNAMITE.get()), "it carries the item it is drawn as");
        bundle.discard();
        helper.succeed();
    }
}
