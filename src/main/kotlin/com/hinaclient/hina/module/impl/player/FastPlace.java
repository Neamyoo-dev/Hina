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
package com.hinaclient.hina.module.impl.player;

import com.hinaclient.hina.event.EventListener;
import com.hinaclient.hina.event.impl.ClientTickEvent;
import com.hinaclient.hina.event.impl.UseCooldownEvent;
import com.hinaclient.hina.module.Category;
import com.hinaclient.hina.module.Module;
import com.hinaclient.hina.setting.BooleanSetting;
import com.hinaclient.hina.setting.NumberSetting;
import net.minecraft.world.item.BlockItem;

public class FastPlace extends Module {
    private long lastPressTime = -1L;
    private long pressDuration;

    private final NumberSetting startDelay = new NumberSetting("StartDelay", 0, 0, 1000, 1);
    private final BooleanSetting onlyBlock = new BooleanSetting("only Block", true);

    public FastPlace() {
        super("FastPlace", Category.HACKS);

        addSetting(startDelay);
        addSetting(onlyBlock);
    }

    @EventListener
    public void onUseCoolDown(UseCooldownEvent e) {
        if (client.player == null) return;
        var player = client.player;

        var mainHandItem = player.getMainHandItem().getItem();
        var offHandItem = player.getOffhandItem().getItem();

        if (((mainHandItem instanceof BlockItem || offHandItem instanceof BlockItem) && onlyBlock.getValue()) &&
                (startDelay.getValue() <= 0 || pressDuration >= startDelay.getValue())) {
            e.setCooldown(0);
        };
    }

    @EventListener
    public void onTick(ClientTickEvent event) {
        if (client.player == null) return;

        boolean isKeyDown = client.options.keyUse.isDown();

        if (isKeyDown) {
            if (lastPressTime == -1L) {
                lastPressTime = System.currentTimeMillis();
            }

            pressDuration = System.currentTimeMillis() - lastPressTime;
        } else {
            lastPressTime = -1L;
        }
    }
}
