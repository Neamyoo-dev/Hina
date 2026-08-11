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

import com.hinaclient.hina.module.impl.optimization.BlockOptimizer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Caches the render state of closed (lid fully shut) chest-like block entities. In 1.21.2+
 * the render pipeline extracts a fresh render state for every block entity every tick, which
 * is the dominant cost around large container rooms. Closed chests look identical tick after
 * tick, so their state can be reused (refreshed every few ticks to keep light coordinates
 * up to date) while open/animating chests keep extracting normally.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    @Unique
    private static final Map<BlockEntity, BlockEntityRenderState> hina$renderStateCache = new WeakHashMap<>();

    @Inject(method = "tryExtractRenderState", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity, S extends BlockEntityRenderState> void hina$reuseCachedState(
            E blockEntity, float partialTick, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, CallbackInfoReturnable<S> cir) {
        if (BlockOptimizer.shouldUseCachedState(blockEntity, crumblingOverlay)) {
            S cached = (S) hina$renderStateCache.get(blockEntity);
            if (cached != null) {
                cir.setReturnValue(cached);
            }
        }
    }

    @Inject(method = "tryExtractRenderState", at = @At("RETURN"))
    private <E extends BlockEntity, S extends BlockEntityRenderState> void hina$storeCachedState(
            E blockEntity, float partialTick, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, CallbackInfoReturnable<S> cir) {
        if (BlockOptimizer.shouldCacheState(blockEntity, crumblingOverlay)) {
            S state = cir.getReturnValue();
            if (state != null) {
                hina$renderStateCache.put(blockEntity, state);
            }
        }
    }
}
