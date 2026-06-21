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
package com.hinaclient.hina.module.impl.combat;

import com.hinaclient.hina.event.EventListener;
import com.hinaclient.hina.event.impl.ClientTickEvent;
import com.hinaclient.hina.management.RotationManager;
import com.hinaclient.hina.module.Category;
import com.hinaclient.hina.module.Module;
import com.hinaclient.hina.setting.BooleanSetting;
import com.hinaclient.hina.setting.ModeSetting;
import com.hinaclient.hina.setting.NumberSetting;
import com.hinaclient.hina.utils.RotationUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LegitCrystal extends Module {
    private final NumberSetting speed = new NumberSetting("Speed", 1.0, 0.01, 1.0, 0.01);
    private final NumberSetting range = new NumberSetting("Range", 4.5, 3.0, 8.0, 0.1);
    private final NumberSetting fov = new NumberSetting("FOV", 90.0, 10.0, 360.0, 1.0);
    private final BooleanSetting autoAttack = new BooleanSetting("Attack", true);
    private final BooleanSetting slient = new BooleanSetting("slient (danger)", false);

    private float targetYaw;
    private float targetPitch;
    private int attackDelayTicks = 0;

    public LegitCrystal() {
        super("LegitCrystal", Category.COMBAT);
        addSetting(speed);
        addSetting(range);
        addSetting(fov);
        addSetting(autoAttack);
        addSetting(slient);
    }

    @EventListener
    public void onTick(ClientTickEvent event) {
        if (client.player == null || client.level == null) return;

        if (attackDelayTicks > 0) attackDelayTicks--;

        double r = range.getValue();
        AABB searchBox = client.player.getBoundingBox().inflate(r);
        List<EndCrystal> targets = new ArrayList<>();

        client.level.getEntities().get(searchBox, entity -> {
            if (entity instanceof EndCrystal crystal && entity.isAlive()
                    && client.player.distanceTo(crystal) <= r && isInFov(crystal)) {
                targets.add(crystal);
            }
        });

        EndCrystal target = targets.stream()
                .min(Comparator.comparingDouble(e -> client.player.distanceTo(e)))
                .orElse(null);

        if (target != null) {
            applyRotations(target);
            if (autoAttack.getValue()) handleAttack(target);
        } else if (!slient.getValue()) {
            targetYaw = client.player.getYRot();
            targetPitch = client.player.getXRot();
        }

        if (!slient.getValue()) {
            float currentYaw = client.player.getYRot();
            float currentPitch = client.player.getXRot();
            float yawDiff = Mth.wrapDegrees(targetYaw - currentYaw);
            float pitchDiff = targetPitch - currentPitch;
            float step = speed.getValue().floatValue();
            float newYaw = currentYaw + yawDiff * step;
            float newPitch = currentPitch + pitchDiff * step;
            client.player.setYRot(newYaw);
            client.player.setXRot(newPitch);
            client.player.yRotO = newYaw;
            client.player.xRotO = newPitch;
        }
    }

    private void handleAttack(EndCrystal target) {
        if (client.player.distanceTo(target) > 3.0) return;
        if (attackDelayTicks > 0) return;

        client.gameMode.attack(client.player, target);
        client.player.swing(InteractionHand.MAIN_HAND);
        attackDelayTicks = 3;
    }

    private boolean isInFov(EndCrystal target) {
        Vec3 playerEye = client.player.getEyePosition();
        double dx = target.getX() - playerEye.x;
        double dz = target.getZ() - playerEye.z;
        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
        return Math.abs(Mth.wrapDegrees(yaw - client.player.getYRot())) <= fov.getValue() / 2.0;
    }

    private void applyRotations(EndCrystal target) {
        Vec3 targetPoint = target.position();
        Vec3 playerEye = client.player.getEyePosition();

        double dx = targetPoint.x - playerEye.x;
        double dy = targetPoint.y - playerEye.y;
        double dz = targetPoint.z - playerEye.z;
        double distXZ = Math.sqrt(dx * dx + dz * dz);

        float exactYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
        float exactPitch = (float) -(Math.atan2(dy, distXZ) * 180.0 / Math.PI);
        float[] clamped = RotationUtils.clampToValid(exactYaw, exactPitch);
        exactYaw = clamped[0];
        exactPitch = clamped[1];

        if (slient.getValue()) {
            RotationManager.INSTANCE.apply(exactYaw, exactPitch, 2);
        } else {
            targetYaw = exactYaw;
            targetPitch = exactPitch;
        }
    }
}