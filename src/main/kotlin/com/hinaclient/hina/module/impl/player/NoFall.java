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
import com.hinaclient.hina.event.impl.packet.PacketEvent;
import com.hinaclient.hina.event.impl.packet.PacketType;
import com.hinaclient.hina.mixin.mixins.accessors.ServerboundMovePlayerPacketAccessor;
import com.hinaclient.hina.module.Category;
import com.hinaclient.hina.module.Module;
import com.hinaclient.hina.setting.ModeSetting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

/**
 * @Author: oneachina
 * @Date: 2026/2/1 16:49
 */
public class NoFall extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "No Ground", "No Ground");

    public NoFall() {
        super("NoFall", Category.HACKS);
        addSetting(mode);
    }

    @EventListener
    public void onPacket(PacketEvent event) {
        if (client.player == null) return;

        if (event.getType() == PacketType.Send && mode.getValue().equals("No Ground") && event.getPacket() instanceof ServerboundMovePlayerPacket packet) {
            ((ServerboundMovePlayerPacketAccessor) packet).setOnGround(false);
        }
    }
}
