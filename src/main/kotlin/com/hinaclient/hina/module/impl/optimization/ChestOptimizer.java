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

import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Internal tool for chest-family optimizations (chests, trapped chests, ender chests, barrels).
 *
 * <p>Not a module - called only by {@link BlockOptimizer}, which checks the master enable
 * state and the per-family toggles before delegating here.</p>
 */
public final class ChestOptimizer {
    private ChestOptimizer() {
    }

    /** Whether this container is beyond the given render distance and should be culled. */
    public static boolean shouldCullBlockEntity(BlockEntity blockEntity, Vec3 cameraPos, double distance) {
        if (!isContainer(blockEntity)) return false;
        return blockEntity.getBlockPos().getCenter().distanceToSqr(cameraPos) > distance * distance;
    }

    /** Whether the render state of this closed chest may be reused from the cache. */
    public static boolean shouldCacheState(BlockEntity blockEntity) {
        return blockEntity instanceof LidBlockEntity lid && lid.getOpenNess(1.0f) <= 0.0f;
    }

    private static boolean isContainer(BlockEntity blockEntity) {
        return blockEntity instanceof ChestBlockEntity
                || blockEntity instanceof EnderChestBlockEntity
                || blockEntity instanceof BarrelBlockEntity;
    }
}
