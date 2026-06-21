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
import com.hinaclient.hina.event.skia.EventSkiaDraw;
import com.hinaclient.hina.event.skia.EventSkiaInit;
import com.mojang.blaze3d.TracyFrameCapture;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Eatgrapes
 * @link github.com/Eatgrapes
 */
@Mixin(Window.class)
public class WindowMixin {
    @Inject(method = "onFramebufferResize", at = @At("RETURN"))
    private void onFramebufferResize(long window, int width, int height, CallbackInfo ci) {
        int finalWidth = Math.max(width, 1);
        int finalHeight = Math.max(height, 1);

        EventBus.INSTANCE.post(new EventSkiaInit(finalWidth, finalHeight));
    }

    @Inject(method = "updateDisplay", at = @At("HEAD"))
    private void onUpdateDisplay(TracyFrameCapture capturer, CallbackInfo ci) {
        EventBus.INSTANCE.post(EventSkiaDraw.INSTANCE);
    }
}
