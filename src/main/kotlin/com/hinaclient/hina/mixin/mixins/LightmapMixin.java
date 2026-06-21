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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.hinaclient.hina.MioHr;
import com.hinaclient.hina.module.impl.render.FullbrightModule;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * @author Eatgrapes
 * @link github.com/Eatgrapes
 */
@Mixin(LightTexture.class)
public class LightmapMixin {
    @ModifyExpressionValue(method = "updateLightTexture(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 1))
    private Object injectFullBright(Object original) {
        FullbrightModule fullbright = (FullbrightModule) MioHr.getINSTANCE().moduleManager.getModuleByName("Fullbright");
        if (fullbright != null && fullbright.isEnabled()) {
            return 10.0;
        }
        return original;
    }
}