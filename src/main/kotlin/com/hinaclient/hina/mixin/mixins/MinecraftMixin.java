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
import com.hinaclient.hina.event.impl.ClientTickEvent;
import com.hinaclient.hina.event.impl.UseCooldownEvent;
import com.hinaclient.hina.event.skia.EventSkiaInit;
import com.hinaclient.hina.skia.SkiaContext;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.Window;
import org.lwjgl.glfw.GLFW;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @Author: Eatgrapes, oneachina
 * @link: github.com/oneachina
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow @Final private Window window;

    @Shadow protected abstract String createTitle();

    @Shadow
    private int rightClickDelay;

    /**
     * @author Neamyoo
     * @reason miohro
     */
    @Overwrite
    public void updateTitle() {
        this.window.setTitle("MioHr Client for " + createTitle());
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        SkiaContext instance = SkiaContext.INSTANCE;

        int[] width = new int[1];
        int[] height = new int[1];

        long windowHandle = Minecraft.getInstance().getWindow().handle();
        GLFW.glfwGetFramebufferSize(windowHandle, width, height);

        int finalWidth = Math.max(width[0], 1);
        int finalHeight = Math.max(height[0], 1);

        EventBus.INSTANCE.post(new EventSkiaInit(finalWidth, finalHeight));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void onClientTick(CallbackInfo ci) {
        EventBus.INSTANCE.post(new ClientTickEvent());
    }

    @Inject(method = "startUseItem", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;rightClickDelay:I", shift = At.Shift.AFTER, opcode = Opcodes.PUTFIELD))
    private void hookItemUseCooldown(CallbackInfo callbackInfo) {
        UseCooldownEvent useCooldownEvent = new UseCooldownEvent(rightClickDelay);
        EventBus.INSTANCE.post(useCooldownEvent);
        rightClickDelay = useCooldownEvent.getCooldown();
    }
}