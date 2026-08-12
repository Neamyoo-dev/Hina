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
import com.hinaclient.hina.setting.NumberSetting;

/**
 * Anti-crash: caps the client-side processing of the common crash / lag attack vectors
 * (entity storms, particle storms, giant explosions, malicious item tooltips).
 * Each limit is a number setting; 0 disables that protection.
 */
public class AntiCrash extends Module {
    private final NumberSetting entityLimit = new NumberSetting("Entity Limit", 150.0, 0.0, 1000.0, 10.0);
    private final NumberSetting particleLimit = new NumberSetting("Particle Limit", 300.0, 0.0, 5000.0, 10.0);
    private final NumberSetting explosionBlocks = new NumberSetting("Explosion Blocks", 300.0, 0.0, 10000.0, 10.0);
    private final NumberSetting tooltipLines = new NumberSetting("Tooltip Lines", 15.0, 0.0, 100.0, 1.0);

    // Rolling window counter for particle spawns (reset roughly every tick).
    private int particleCount = 0;
    private long lastParticleReset = 0L;

    public AntiCrash() {
        super("AntiCrash", Category.OPTIMIZATION);
        addSetting(entityLimit);
        addSetting(particleLimit);
        addSetting(explosionBlocks);
        addSetting(tooltipLines);
    }

    public static int getEntityLimit() {
        AntiCrash module = getModule();
        return module != null && module.isEnabled() ? module.entityLimit.getValue().intValue() : 0;
    }

    public static int getExplosionBlockLimit() {
        AntiCrash module = getModule();
        return module != null && module.isEnabled() ? module.explosionBlocks.getValue().intValue() : 0;
    }

    public static int getTooltipLineLimit() {
        AntiCrash module = getModule();
        return module != null && module.isEnabled() ? module.tooltipLines.getValue().intValue() : 0;
    }

    /** Whether a new particle spawn should be rejected (rate limited). */
    public static boolean shouldRejectParticle() {
        AntiCrash module = getModule();
        if (module == null || !module.isEnabled()) return false;
        double limit = module.particleLimit.getValue();
        if (limit <= 0.0) return false;
        long now = System.currentTimeMillis();
        if (now - module.lastParticleReset > 50L) {
            module.particleCount = 0;
            module.lastParticleReset = now;
        }
        module.particleCount++;
        return module.particleCount > limit;
    }

    private static AntiCrash getModule() {
        return (AntiCrash) MioHr.getINSTANCE().moduleManager.getModuleByName("AntiCrash");
    }
}
