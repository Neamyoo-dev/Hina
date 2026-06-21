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
package com.hinaclient.hina.mixin.mixins;

import com.hinaclient.hina.management.RotationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinEntity {
    @Inject(method = "getLookAngle", at = @At("HEAD"), cancellable = true)
    private void onGetLookAngle(CallbackInfoReturnable<Vec3> cir) {
        Entity self = (Entity) (Object) this;
        if (self == Minecraft.getInstance().player && RotationManager.INSTANCE.isRotating()) {
            float yaw = RotationManager.INSTANCE.getServerYaw();
            float pitch = RotationManager.INSTANCE.getServerPitch();
            if (Float.isNaN(yaw) || Float.isNaN(pitch)) return;
            cir.setReturnValue(Vec3.directionFromRotation(pitch, yaw));
        }
    }
}
