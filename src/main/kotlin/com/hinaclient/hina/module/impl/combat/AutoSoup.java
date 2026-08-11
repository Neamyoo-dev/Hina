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
package com.hinaclient.hina.module.impl.combat;

import com.hinaclient.hina.event.EventListener;
import com.hinaclient.hina.event.impl.ClientTickEvent;
import com.hinaclient.hina.module.Category;
import com.hinaclient.hina.module.Module;
import com.hinaclient.hina.setting.NumberSetting;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;

import java.util.concurrent.ThreadLocalRandom;

public class AutoSoup extends Module {
    private final NumberSetting healthThreshold = new NumberSetting("Health", 10.0, 1.0, 20.0, 0.5);
    private final NumberSetting minDelay = new NumberSetting("MinDelay", 46.0, 10.0, 500.0, 1.0);
    private final NumberSetting maxDelay = new NumberSetting("MaxDelay", 78.0, 10.0, 500.0, 1.0);

    private long lastUseTime = 0L;

    public AutoSoup() {
        super("AutoSoup", Category.HACKS);
        addSetting(healthThreshold);
        addSetting(minDelay);
        addSetting(maxDelay);
    }

    @EventListener
    public void onTick(ClientTickEvent event) {
        if (client.player == null || client.level == null) return;
        if (client.player.isUsingItem()) return;

        float health = client.player.getHealth();
        if (health >= healthThreshold.getValue().floatValue()) return;

        long now = Util.getMillis();
        long delay = ThreadLocalRandom.current().nextLong(minDelay.getValue().longValue(), maxDelay.getValue().longValue() + 1);
        if (now - lastUseTime < delay) return;

        int soupSlot = findSoupSlot();
        if (soupSlot != -1) {
            client.player.getInventory().setSelectedSlot(soupSlot);
            client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
            lastUseTime = now;
        }
    }

    private int findSoupSlot() {
        for (int i = 0; i < 9; i++) {
            var item = client.player.getInventory().getItem(i).getItem();
            if (item == Items.MUSHROOM_STEW || item == Items.BEETROOT_SOUP || item == Items.RABBIT_STEW || item == Items.SUSPICIOUS_STEW) {
                return i;
            }
        }
        return -1;
    }
}