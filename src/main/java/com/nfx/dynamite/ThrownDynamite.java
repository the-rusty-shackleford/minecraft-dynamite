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

import com.nfx.dynamite.domain.Blast;
import com.nfx.dynamite.domain.Bounce;
import com.nfx.dynamite.domain.Fuse;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A bundle in the air or on the ground, lit. It flies like a snowball, but
 * where a snowball breaks it bounces -- off blocks, off whatever it hits --
 * and once it is barely moving on a floor it lies there. It hisses and
 * smokes the whole time, and when its fuse is up it goes off wherever it
 * is. The fuse, the bounce and the blast are the domain's; this class is
 * the game's side of them.
 */
public final class ThrownDynamite extends ThrowableItemProjectile {
    /** Ticks between hisses: the fuse sound is a little longer, so they run on. */
    private static final int HISS_EVERY = 10;
    private static final String LIT_TAG = "Lit";
    private int lit = 0;
    private boolean resting = false;

    public ThrownDynamite(EntityType<? extends ThrownDynamite> type, Level level) {
        super(type, level);
    }

    public ThrownDynamite(LivingEntity thrower, Level level) {
        super(ModEntities.THROWN_DYNAMITE.get(), thrower, level);
    }

    public ThrownDynamite(double x, double y, double z, Level level) {
        super(ModEntities.THROWN_DYNAMITE.get(), x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.DYNAMITE.get();
    }

    /** effects: returns how many ticks this bundle has been lit */
    public int lit() {
        return lit;
    }

    /** effects: returns whether the bundle lies still */
    public boolean isResting() {
        return resting;
    }

    @Override
    public void tick() {
        super.tick();
        Level level = level();
        if (level.isClientSide()) {
            smoke();
            lit++;
            return;
        }
        Fuse fuse = new Fuse(DynamiteConfig.FUSE_TICKS.get());
        if (fuse.hisses(lit, HISS_EVERY)) {
            level.playSound(null, getX(), getY(), getZ(), ModSounds.FUSE.get(), SoundSource.NEUTRAL, 0.6f, 1.0f);
        }
        lit++;
        if (fuse.goesOff(lit)) {
            goOff();
        }
    }

    /** The blast: the game's own explosion at the bundle's power, then the bundle is gone. */
    private void goOff() {
        Blast blast = new Blast(DynamiteConfig.POWER.get().floatValue(), DynamiteConfig.BREAK_BLOCKS.get());
        level().explode(this, getX(), getY(0.5), getZ(), blast.power(),
                blast.breaksBlocks() ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
        discard();
    }

    /** Smoke off the fuse every tick, and a spark now and then. */
    private void smoke() {
        Vec3 at = position();
        level().addParticle(ParticleTypes.SMOKE, at.x, at.y + 0.2, at.z, 0.0, 0.02, 0.0);
        if (lit % 3 == 0) {
            level().addParticle(ParticleTypes.SMALL_FLAME, at.x, at.y + 0.2, at.z,
                    (random.nextDouble() - 0.5) * 0.04, 0.03, (random.nextDouble() - 0.5) * 0.04);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        Direction face = result.getDirection();
        bounce(face.getStepX(), face.getStepY(), face.getStepZ());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        // Off whatever it hit, back the way it came along the axis it came in on.
        Vec3 v = getDeltaMovement();
        double ax = Math.abs(v.x);
        double ay = Math.abs(v.y);
        double az = Math.abs(v.z);
        if (ax >= ay && ax >= az) {
            bounce(v.x > 0 ? -1 : 1, 0, 0);
        } else if (ay >= az) {
            bounce(0, v.y > 0 ? -1 : 1, 0);
        } else {
            bounce(0, 0, v.z > 0 ? -1 : 1);
        }
    }

    private void bounce(int nx, int ny, int nz) {
        Vec3 v = getDeltaMovement();
        Bounce.Result r = Bounce.off(v.x, v.y, v.z, nx, ny, nz);
        boolean wasResting = resting;
        resting = r.atRest();
        setDeltaMovement(r.vx(), r.vy(), r.vz());
        if (!level().isClientSide() && !wasResting && v.length() > Bounce.REST_SPEED * 2.0) {
            level().playSound(null, getX(), getY(), getZ(), ModSounds.LAND.get(), SoundSource.NEUTRAL,
                    (float) Math.min(1.0, 0.3 + v.length() * 0.5), 0.9f + random.nextFloat() * 0.2f);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(LIT_TAG, lit);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        lit = tag.getInt(LIT_TAG);
    }
}
