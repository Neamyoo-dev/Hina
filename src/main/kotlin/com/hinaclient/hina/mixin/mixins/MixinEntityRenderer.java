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

import com.hinaclient.hina.MioHr;
import com.hinaclient.hina.mixin.utils.EntityRenderStateAccessor;
import com.hinaclient.hina.module.impl.render.ChamsModule;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer <T extends Entity, S extends EntityRenderState> {
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void hookInjectEntityIntoState(T entity, S entityRenderState, float f, CallbackInfo ci) {
        ((EntityRenderStateAccessor) entityRenderState).hina$setEntity(entity);
    }

    @Unique
    private static boolean hina$shouldRenderOutline(Entity entity) {
        ChamsModule module = (ChamsModule) MioHr.getINSTANCE().moduleManager.getModuleByName("Chams");
        return module.isEnabled() && module.shouldOutline(entity);
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean modifyShouldRenderOutline(Minecraft instance, Entity entity, Operation<Boolean> original) {
        return original.call(instance, entity) || hina$shouldRenderOutline(entity);
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getTeamColor()I"))
    private int injectTeamColor(Entity instance, Operation<Integer> original) {
        ChamsModule module = (ChamsModule) MioHr.getINSTANCE().moduleManager.getModuleByName("Chams");
        if (instance instanceof LivingEntity && module.shouldOutline(instance)) {
            return module.outlineColor.getColor();
        }
        return original.call(instance);
    }
}
