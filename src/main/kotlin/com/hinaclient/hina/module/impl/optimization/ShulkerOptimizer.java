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

package com.hinaclient.hina.module.impl.optimization;

import com.hinaclient.hina.mixin.mixins.accessors.ShulkerBoxBlockEntityAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Internal tool for shulker box optimizations.
 *
 * <p>Not a module - called only by {@link BlockOptimizer}, which checks the master enable
 * state and the per-family toggles before delegating here.</p>
 */
public final class ShulkerOptimizer {
    private ShulkerOptimizer() {
    }

    /** Whether this shulker box is beyond the given render distance and should be culled. */
    public static boolean shouldCullBlockEntity(BlockEntity blockEntity, Vec3 cameraPos, double distance) {
        return blockEntity.getBlockPos().getCenter().distanceToSqr(cameraPos) > distance * distance;
    }

    /**
     * Whether the render state of this shulker box may be reused from the cache. Only when it
     * is fully retracted (lid closed, animation finished) and nobody is opening it.
     */
    public static boolean shouldCacheState(BlockEntity blockEntity) {
        if (!(blockEntity instanceof ShulkerBoxBlockEntity shulker)) return false;
        ShulkerBoxBlockEntityAccessor accessor = (ShulkerBoxBlockEntityAccessor) shulker;
        return accessor.getOpenCount() <= 0 && accessor.getProgress() <= 0.0f && accessor.getProgressOld() <= 0.0f;
    }
}
