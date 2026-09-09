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

import com.nfx.dynamite.ModItems;
import com.nfx.dynamite.ThrownDynamite;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The mod on film: in the {@code photoBooth} dev run the player stands on
 * the flat world with a bundle in hand, throws it through the real use
 * path, and the frames land in {@code run/booth/screenshots/}: the bundle
 * in the hand, in the air trailing smoke, lying lit, the blast, and the
 * crater. One {@code booth: PASS} or {@code booth: FAIL} line per check.
 * Client only, active only under {@code dynamite.photobooth}.
 */
@EventBusSubscriber(modid = BoothMod.MOD_ID, value = Dist.CLIENT)
public final class PhotoBooth {
    private PhotoBooth() {}

    private static final Logger LOG = LoggerFactory.getLogger("Dynamite booth");
    private static final boolean ACTIVE = Boolean.getBoolean("dynamite.photobooth");

    private record Step(int at, Runnable action) {}

    private static List<Step> steps;
    private static int tick = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!ACTIVE) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        if (steps == null) {
            steps = script(mc, player);
        }
        tick++;
        for (Step step : steps) {
            if (step.at() == tick) {
                step.action().run();
            }
        }
    }

    private static List<Step> script(Minecraft mc, LocalPlayer player) {
        List<Step> s = new ArrayList<>();
        int[] t = {40};
        s.add(new Step(t[0], () -> onServer(mc, sp -> {
            sp.setGameMode(GameType.CREATIVE);
            sp.teleportTo(sp.serverLevel(), 0.5, -60.0, 0.5, 0.0f, 12.0f);
            sp.getInventory().clearContent();
            sp.getInventory().setItem(0, new ItemStack(ModItems.DYNAMITE.get(), 16));
            sp.getInventory().selected = 0;
        })));
        s.add(new Step(t[0] += 30, () -> {
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            player.setYRot(0.0f);
            player.setXRot(12.0f);
        }));
        s.add(new Step(t[0] += 30, () -> shoot(mc, "booth-in-hand")));
        s.add(new Step(t[0] += 2, () -> {
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            player.setXRot(28.0f);   // down at the ground a few blocks ahead, where the camera can see it land
        }));
        s.add(new Step(t[0] += 5, () -> {
            var result = mc.gameMode.useItem(player, InteractionHand.MAIN_HAND);
            LOG.info("booth: threw, {}", result);
        }));
        s.add(new Step(t[0] += 6, () -> {
            var thrown = mc.level.getEntitiesOfClass(ThrownDynamite.class, player.getBoundingBox().inflate(40.0));
            verdict("a thrown bundle is in the air", thrown.size() == 1, "had " + thrown.size());
            shoot(mc, "booth-in-flight");
        }));
        s.add(new Step(t[0] += 28, () -> {
            var thrown = mc.level.getEntitiesOfClass(ThrownDynamite.class, player.getBoundingBox().inflate(40.0));
            verdict("the bundle lies lit before the blast", thrown.size() == 1 && thrown.get(0).getDeltaMovement().length() < 0.15,
                    thrown.isEmpty() ? "gone" : "moving at " + thrown.get(0).getDeltaMovement().length());
            shoot(mc, "booth-lying-lit");
        }));
        s.add(new Step(t[0] += 7, () -> shoot(mc, "booth-blast")));
        s.add(new Step(t[0] += 30, () -> {
            var thrown = mc.level.getEntitiesOfClass(ThrownDynamite.class, player.getBoundingBox().inflate(40.0));
            verdict("the bundle has gone off", thrown.isEmpty(), "still " + thrown.size());
            shoot(mc, "booth-crater");
        }));
        s.add(new Step(t[0] += 10, () -> {
            LOG.info("booth: PASS all checks ran");
            mc.stop();
        }));
        return s;
    }

    private static void onServer(Minecraft mc, Consumer<ServerPlayer> action) {
        var server = mc.getSingleplayerServer();
        if (server == null || mc.player == null) {
            LOG.warn("booth: no integrated server");
            return;
        }
        var uuid = mc.player.getUUID();
        server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(uuid);
            if (sp != null) {
                action.accept(sp);
            }
        });
    }

    private static void shoot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, name + ".png", mc.getMainRenderTarget(),
                message -> LOG.info("booth: {}", message.getString()));
    }

    private static void verdict(String what, boolean passed, String detail) {
        if (passed) {
            LOG.info("booth: PASS {}", what);
        } else {
            LOG.error("booth: FAIL {} -- {}", what, detail);
        }
    }
}
