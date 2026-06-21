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
import com.hinaclient.hina.management.RotationManager;
import com.hinaclient.hina.mixin.utils.EntityRenderStateAccessor;
import com.hinaclient.hina.mixin.mixins.accessors.render.RenderSetupAccessor;
import com.hinaclient.hina.mixin.mixins.accessors.render.RenderTypeAccessor;
import com.hinaclient.hina.module.impl.render.ChamsModule;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.jetbrains.annotations.Nullable;

@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer <S extends LivingEntityRenderState>{

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("RETURN"))
    private void onExtractRenderState(LivingEntity entity, LivingEntityRenderState state, float f, CallbackInfo ci) {
        if (entity == Minecraft.getInstance().player && RotationManager.INSTANCE.isRotating()) {
            float serverYaw = RotationManager.INSTANCE.getServerYaw();
            float serverPitch = RotationManager.INSTANCE.getServerPitch();

            state.yRot = serverYaw;
            state.xRot = serverPitch;
            state.bodyRot = serverYaw;
        }
    }

    @ModifyExpressionValue(
            method = "getRenderType",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderType(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;")
    )
    private @Nullable RenderType hina$renderChams(
            @Nullable RenderType original,
            @Local(argsOnly = true) S state,
            @Local Identifier texture
    ) {
        if (original == null) return null;

        Entity entity = ((EntityRenderStateAccessor) state).hina$getEntity();
        if (entity == null || !ChamsModule.shouldRender(entity)) return original;

        ChamsModule module = (ChamsModule) MioHr.getINSTANCE().moduleManager.getModuleByName("Chams");
        if (module.isEnabled()) {
            var renderSetup = ((RenderTypeAccessor) original).getState();
            boolean affectsOutline = ((RenderSetupAccessor) (Object) renderSetup).getOutlineProperty() == RenderSetup.OutlineProperty.AFFECTS_OUTLINE;

            return switch (((RenderTypeAccessor) original).getName()) {
                case "entity_translucent" -> ChamsModule.ENTITY_TRANSLUCENT.apply(texture, affectsOutline);
                case "entity_cutout" -> ChamsModule.ENTITY_CUTOUT.apply(texture);
                case "entity_cutout_no_cull" -> ChamsModule.ENTITY_CUTOUT_NO_CULL.apply(texture, affectsOutline);
                default -> original;
            };
        }

        return original;
    }
}