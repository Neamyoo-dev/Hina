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

package com.hinaclient.hina.event.fabric;

import com.hinaclient.hina.MioHr;
import com.hinaclient.hina.event.EventBus;
import com.hinaclient.hina.event.impl.server.ServerMessageEvent;
import com.hinaclient.hina.skia.font.FontManager;
import com.hinaclient.hina.ui.ClickGuiScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

public class HinaHandler {
    public HinaHandler() {}

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            client.getResourceManager();
            if (!FontManager.INSTANCE.isInitialized()) {
                try {
                    FontManager.INSTANCE.init();
                } catch (Exception ignored) {
                }
            }
            if (MioHr.getINSTANCE().clickGuiKey.consumeClick()
                    && !(client.screen instanceof ClickGuiScreen)) {
                MioHr.getINSTANCE().moduleManager.getClickGuiModule().setEnabled(true);
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            MioHr.getINSTANCE().configManager.save();
            FontManager.INSTANCE.close();
        });
    }

    public static void initEvent() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            EventBus.INSTANCE.post(new ServerMessageEvent(message, overlay));
        });
    }
}
