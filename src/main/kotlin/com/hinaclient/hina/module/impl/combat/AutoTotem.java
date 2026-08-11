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
import com.hinaclient.hina.setting.BooleanSetting;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.concurrent.ThreadLocalRandom;

public class AutoTotem extends Module {
    private final BooleanSetting inventory = new BooleanSetting("Inventory", true);
    private int cooldown = 0;
    private int stage = 0;
    private int totemSlot = -1;
    private boolean wasSprinting = false;

    public AutoTotem() {
        super("AutoTotem", Category.HACKS);
        addSetting(inventory);
    }

    @EventListener
    public void onTick(ClientTickEvent event) {
        if (client.player == null || client.gameMode == null) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (client.player.getOffhandItem().getItem() == Items.TOTEM_OF_UNDYING) {
            stage = 0;
            return;
        }

        switch (stage) {
            case 0 -> {
                totemSlot = findTotemSlot();
                if (totemSlot == -1) return;
                stage = 1;
                cooldown = ThreadLocalRandom.current().nextInt(2, 5);
            }
            case 1 -> {
                wasSprinting = client.player.isSprinting();
                if (wasSprinting) {
                    client.player.connection.send(new ServerboundPlayerCommandPacket(client.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
                }
                client.player.connection.send(new ServerboundPlayerInputPacket(Input.EMPTY));
                stage = 2;
                cooldown = 0;
            }
            case 2 -> {
                int offhandSlot = 45;
                client.gameMode.handleInventoryMouseClick(client.player.containerMenu.containerId, totemSlot, 0, ClickType.PICKUP, client.player);
                client.gameMode.handleInventoryMouseClick(client.player.containerMenu.containerId, offhandSlot, 0, ClickType.PICKUP, client.player);
                ItemStack offhandItem = client.player.getOffhandItem();
                if (!offhandItem.isEmpty() && offhandItem.getItem() != Items.TOTEM_OF_UNDYING) {
                    client.gameMode.handleInventoryMouseClick(client.player.containerMenu.containerId, totemSlot, 0, ClickType.PICKUP, client.player);
                }
                stage = 3;
                cooldown = ThreadLocalRandom.current().nextInt(1, 3);
            }
            case 3 -> {
                if (wasSprinting) {
                    client.player.connection.send(new ServerboundPlayerCommandPacket(client.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
                }
                stage = 0;
                cooldown = ThreadLocalRandom.current().nextInt(5, 10);
            }
        }
    }

    private int findTotemSlot() {
        for (int i = 0; i < 45; i++) {
            ItemStack stack = client.player.getInventory().getItem(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                if (i < 9) return i + 36;
                return i;
            }
        }
        return -1;
    }
}
