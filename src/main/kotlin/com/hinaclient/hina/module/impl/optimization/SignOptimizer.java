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

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;

/**
 * Internal tool for sign optimizations (signs and hanging signs).
 *
 * <p>Not a module - called only by {@link BlockOptimizer}, which checks the master enable
 * state and the per-family toggles before delegating here.</p>
 */
public final class SignOptimizer {
    private SignOptimizer() {
    }

    /** Signs are fully static, their render state can always be reused from the cache. */
    public static boolean shouldCacheState(BlockEntity blockEntity) {
        return blockEntity instanceof SignBlockEntity;
    }
}
