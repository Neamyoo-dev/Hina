/*
 * Hina Client
 * Copyright (C) 2026 Hina Client
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.hinaclient.hina.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RotationUtils {
    private static final Minecraft mc = Minecraft.getInstance();

    /**
     * GCD Fix - Ensures rotations are aligned with mouse sensitivity grid.
     * Essential for bypassing advanced anticheats like GrimAC.
     */
    public static float[] applyGCD(float yaw, float pitch, float lastYaw, float lastPitch) {
        float sensitivity = mc.options.sensitivity().get().floatValue();
        float f = sensitivity * 0.6F + 0.2F;
        float gcd = f * f * f * 8.0F * 0.15F;

        if (gcd <= 0) return new float[]{yaw, pitch};

        float deltaYaw = yaw - lastYaw;
        float deltaPitch = pitch - lastPitch;

        float fixedDeltaYaw = deltaYaw - (deltaYaw % gcd);
        float fixedDeltaPitch = deltaPitch - (deltaPitch % gcd);

        float fixedYaw = lastYaw + fixedDeltaYaw;
        float fixedPitch = lastPitch + fixedDeltaPitch;

        return clampToValid(fixedYaw, fixedPitch);
    }

    /**
     * Smooths rotation from last to target with speed and random noise.
     */
    public static float[] smooth(float lastYaw, float lastPitch, float targetYaw, float targetPitch, double speed) {
        float yaw = targetYaw;
        float pitch = targetPitch;

        if (speed != 0) {
            float rotationSpeed = (float) speed;

            float deltaYaw = Mth.wrapDegrees(targetYaw - lastYaw);
            float deltaPitch = targetPitch - lastPitch;

            double distance = Math.sqrt(deltaYaw * deltaYaw + deltaPitch * deltaPitch);
            if (distance < 1e-6) {
                return applyGCD(targetYaw, targetPitch, lastYaw, lastPitch);
            }
            double distributionYaw = Math.abs(deltaYaw / distance);
            double distributionPitch = Math.abs(deltaPitch / distance);

            double maxYaw = rotationSpeed * distributionYaw;
            double maxPitch = rotationSpeed * distributionPitch;

            float moveYaw = (float) Math.max(Math.min(deltaYaw, maxYaw), -maxYaw);
            float movePitch = (float) Math.max(Math.min(deltaPitch, maxPitch), -maxPitch);

            yaw = lastYaw + moveYaw;
            pitch = lastPitch + movePitch;

            if (Math.abs(moveYaw) + Math.abs(movePitch) > 1) {
                yaw += (float) ((ThreadLocalRandom.current().nextFloat() - 0.5) / 1000);
                pitch -= (float) (ThreadLocalRandom.current().nextFloat() / 200);
            }
        }

        return applyGCD(yaw, pitch, lastYaw, lastPitch);
    }

    public static float[] clampToValid(float yaw, float pitch) {
        if (Float.isNaN(yaw) || Float.isInfinite(yaw)) yaw = 0f;
        if (Float.isNaN(pitch) || Float.isInfinite(pitch)) pitch = 0f;
        return new float[]{Mth.wrapDegrees(yaw), Mth.clamp(pitch, -90f, 90f)};
    }

    /**
     * Calculates rotations required to look at a vector from an origin.
     */
    public static float[] getRotations(Vec3 origin, Vec3 target) {
        double dx = target.x - origin.x;
        double dy = target.y - origin.y;
        double dz = target.z - origin.z;
        double distXZ = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) -(Math.atan2(dy, distXZ) * 180.0 / Math.PI);

        return clampToValid(yaw, pitch);
    }

    /**
     * Calculates rotations to look at an entity's eye position.
     */
    public static float[] getRotationsToEntity(Entity target) {
        return getRotations(mc.player.getEyePosition(), target.getEyePosition());
    }

    /**
     * Multi-point search for best rotation to an entity within its hitbox.
     */
    public static RotationData getBestRotationToEntity(Entity target, double range) {
        AABB box = target.getBoundingBox();
        Vec3 eye = mc.player.getEyePosition();
        
        Vec3 bestHitVec = null;
        float[] bestRotation = null;
        double minDiff = Double.MAX_VALUE;

        // Simple 3x3x3 search inside hitbox
        for (double x = 0.1; x <= 0.9; x += 0.4) {
            for (double y = 0.1; y <= 0.9; y += 0.4) {
                for (double z = 0.1; z <= 0.9; z += 0.4) {
                    Vec3 targetVec = new Vec3(
                        box.minX + (box.maxX - box.minX) * x,
                        box.minY + (box.maxY - box.minY) * y,
                        box.minZ + (box.maxZ - box.minZ) * z
                    );
                    
                    float[] rots = getRotations(eye, targetVec);
                    double diff = getRotationDifference(rots, new float[]{mc.player.getYRot(), mc.player.getXRot()});
                    
                    if (diff < minDiff) {
                        minDiff = diff;
                        bestHitVec = targetVec;
                        bestRotation = rots;
                    }
                }
            }
        }
        
        return new RotationData(bestHitVec, bestRotation);
    }

    public static double getRotationDifference(float[] a, float[] b) {
        return Math.hypot(Mth.wrapDegrees(a[0] - b[0]), a[1] - b[1]);
    }

    public static record RotationData(Vec3 hitVec, float[] rotation) {}
}
