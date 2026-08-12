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

import com.hinaclient.hina.module.impl.optimization.AntiCrash;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Caps the number of entity render states submitted per frame to survive entity storms
 * (servers spawning hundreds of entities at once).
 */
@Mixin(LevelRenderer.class)
public class EntityExtractionLimitMixin {
    @Inject(method = "extractVisibleEntities", at = @At("RETURN"))
    private void hina$limitEntityRenderStates(Camera camera, Frustum frustum, DeltaTracker deltaTracker,
                                              LevelRenderState levelRenderState, CallbackInfo ci) {
        int limit = AntiCrash.getEntityLimit();
        if (limit <= 0) return;
        List<EntityRenderState> states = levelRenderState.entityRenderStates;
        if (states.size() > limit) {
            states.subList(limit, states.size()).clear();
        }
    }
}
