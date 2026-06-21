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

import com.hinaclient.hina.event.EventBus;
import com.hinaclient.hina.event.impl.MoveEvent;
import com.hinaclient.hina.event.impl.MoveInputEvent;
import com.hinaclient.hina.event.impl.UpdateEvent;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @Author: oneachina
 * @Date: 2026/2/1 12:36
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends Entity {
    @Shadow public ClientInput input;

    public LocalPlayerMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        EventBus.INSTANCE.post(new UpdateEvent());
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/ClientInput;tick()V", shift = At.Shift.AFTER))
    private void onAiStep(CallbackInfo ci) {
        Input keys = input.keyPresses;
        Vec2 move = input.getMoveVector();
        float forward = move.y;
        float strafe = move.x;
        boolean jumping = keys.jump();
        boolean sneaking = keys.shift();
        MoveInputEvent event = new MoveInputEvent(forward, strafe, keys.jump(), keys.shift());
        EventBus.INSTANCE.post(event);

        if (Math.abs(event.getForward() - forward) < 1.0E-4F
                && Math.abs(event.getStrafe() - strafe) < 1.0E-4F
                && event.isJumping() == jumping
                && event.isSneaking() == sneaking) {
            return;
        }

        boolean fixedForward = event.getForward() > 0.0F;
        boolean fixedBackward = event.getForward() < 0.0F;
        boolean fixedLeft = event.getStrafe() > 0.0F;
        boolean fixedRight = event.getStrafe() < 0.0F;

        input.keyPresses = new Input(
                fixedForward,
                fixedBackward,
                fixedLeft,
                fixedRight,
                event.isJumping(),
                event.isSneaking(),
                keys.sprint()
        );
        input.moveVector = new Vec2(event.getStrafe(), event.getForward());
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void onMove(MoverType moverType, Vec3 vec3, CallbackInfo ci) {
        MoveEvent event = new MoveEvent(moverType, vec3);
        EventBus.INSTANCE.post(event);

        if (event.isCancelled()) {
            ci.cancel();
            return;
        }

        if (event.getVec() != vec3) {
            super.move(moverType, event.getVec());
            ci.cancel();
        }
    }
}
