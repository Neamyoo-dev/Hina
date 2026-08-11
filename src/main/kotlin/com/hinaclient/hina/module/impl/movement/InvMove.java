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
package com.hinaclient.hina.module.impl.movement;

import com.hinaclient.hina.event.EventListener;
import com.hinaclient.hina.event.impl.UpdateEvent;
import com.hinaclient.hina.event.impl.packet.PacketEvent;
import com.hinaclient.hina.event.impl.packet.PacketType;
import com.hinaclient.hina.module.Category;
import com.hinaclient.hina.module.Module;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

public class InvMove extends Module {

    private boolean dispatching = false;

    public InvMove() {
        super("InvMove", Category.HACKS);
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (client.screen == null || client.screen instanceof ChatScreen) return;

        KeyMapping[] moveKeys = {
                client.options.keyUp,
                client.options.keyDown,
                client.options.keyLeft,
                client.options.keyRight,
                client.options.keyJump,
                client.options.keySprint
        };

        var window = client.getWindow();
        for (KeyMapping key : moveKeys) {
            key.setDown(InputConstants.isKeyDown(window, key.getDefaultKey().getValue()));
        }
    }

    @EventListener
    public void onPacket(PacketEvent event) {
        if (event.getType() != PacketType.Send || dispatching) return;

        Packet<?> packet = event.getPacket();

        if (packet instanceof ServerboundContainerClickPacket || packet instanceof ServerboundContainerClosePacket) {
            if (client.player != null && client.player.isSprinting()) {
                event.setCancelled(true);

                client.player.setSprinting(false);
                dispatching = true;

                client.getConnection().send(new ServerboundPlayerCommandPacket(
                        client.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));

                client.getConnection().send(packet);

                client.getConnection().send(new ServerboundPlayerCommandPacket(
                        client.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));

                dispatching = false;
                client.player.setSprinting(true);
            }
        }
    }

    @Override
    protected void onDisable() {
        if (client.screen != null) {
            client.options.keyUp.setDown(false);
            client.options.keyDown.setDown(false);
            client.options.keyLeft.setDown(false);
            client.options.keyRight.setDown(false);
            client.options.keyJump.setDown(false);
            client.options.keySprint.setDown(false);
        }
        super.onDisable();
    }
}