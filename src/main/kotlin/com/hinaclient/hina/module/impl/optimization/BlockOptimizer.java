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

import com.hinaclient.hina.MioHr;
import com.hinaclient.hina.module.Category;
import com.hinaclient.hina.module.Module;
import com.hinaclient.hina.setting.BooleanSetting;
import com.hinaclient.hina.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * User-facing master switch for block entity optimizations.
 *
 * <p>Each block entity family (chests, signs, shulker boxes, ...) is implemented by a
 * dedicated internal tool class (e.g. {@link ChestOptimizer}, {@link SignOptimizer}) which
 * is never registered as a module. This class is the single entry point mixins talk to:
 * it checks the master enable state and per-family toggles, then delegates to the
 * matching tool. Adding a new family only requires a new tool class plus one setting and
 * one delegation branch here - no mixin changes.</p>
 */
public class BlockOptimizer extends Module {
    private static final int CACHE_REFRESH_TICKS = 5;

    // Mechanism toggles (master)
    public static final BooleanSetting stateCache = new BooleanSetting("State Cache", true);
    public static final NumberSetting renderDistance = new NumberSetting("Render Distance", 32.0, 0.0, 128.0, 1.0);
    public static final BooleanSetting lidAnimation = new BooleanSetting("Lid Animation", true);

    // Per-family toggles
    public static final BooleanSetting chests = new BooleanSetting("Chests", true);
    public static final BooleanSetting signs = new BooleanSetting("Signs", true);
    public static final BooleanSetting shulkers = new BooleanSetting("Shulkers", true);

    public BlockOptimizer() {
        super("BlockOptimizer", Category.OPTIMIZATION);
        addSetting(stateCache);
        addSetting(renderDistance);
        addSetting(lidAnimation);
        addSetting(chests);
        addSetting(signs);
        addSetting(shulkers);
    }

    /** Whether chest/ender-chest/shulker lid animation ticking should be skipped entirely. */
    public static boolean shouldDisableLidAnimation() {
        if (!isActive()) return false;
        if (!chests.getValue() && !shulkers.getValue()) return false;
        return !lidAnimation.getValue();
    }

    /** Whether a container block entity beyond the configured render distance should be culled. */
    public static boolean shouldCullBlockEntity(BlockEntity blockEntity, Vec3 cameraPos) {
        if (!isActive()) return false;
        double distance = renderDistance.getValue();
        if (distance <= 0.0) return false;
        if (chests.getValue() && ChestOptimizer.shouldCullBlockEntity(blockEntity, cameraPos, distance)) return true;
        return shulkers.getValue() && ShulkerOptimizer.shouldCullBlockEntity(blockEntity, cameraPos, distance);
    }

    /**
     * Whether the render state of this block entity may be reused from the cache.
     * Delegates to the tool of the matching family.
     */
    public static boolean shouldCacheState(BlockEntity blockEntity, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if (!isActive() || !stateCache.getValue()) return false;
        if (crumblingOverlay != null) return false;
        if (signs.getValue() && SignOptimizer.shouldCacheState(blockEntity)) return true;
        if (shulkers.getValue() && ShulkerOptimizer.shouldCacheState(blockEntity)) return true;
        return chests.getValue() && ChestOptimizer.shouldCacheState(blockEntity);
    }

    /**
     * Like {@link #shouldCacheState} but additionally forces a full extraction every few ticks
     * so cached light coordinates and edited sign text stay fresh.
     */
    public static boolean shouldUseCachedState(BlockEntity blockEntity, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if (!shouldCacheState(blockEntity, crumblingOverlay)) return false;
        if (Minecraft.getInstance().level == null) return false;
        return Minecraft.getInstance().level.getGameTime() % CACHE_REFRESH_TICKS != 0;
    }

    private static boolean isActive() {
        BlockOptimizer module = (BlockOptimizer) MioHr.getINSTANCE().moduleManager.getModuleByName("BlockOptimizer");
        return module != null && module.isEnabled();
    }
}
