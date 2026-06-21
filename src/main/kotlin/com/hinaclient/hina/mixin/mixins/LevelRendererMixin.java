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
import com.hinaclient.hina.event.impl.Render3DEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Eatgrapes, oneachina
 * @link github.com/Eatgrapes
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "renderBlockOutline", at = @At("HEAD"))
    private void renderBlockOutline(MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, boolean bl, LevelRenderState levelRenderState, CallbackInfo ci) {
        EventBus.INSTANCE.post(new Render3DEvent(
                Minecraft.getInstance().getDeltaTracker(),
                poseStack,
                poseStack.last().pose(),
                bufferSource)
        );
    }
}