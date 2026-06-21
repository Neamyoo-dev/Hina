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

package com.hinaclient.hina

import com.hinaclient.hina.event.fabric.HinaHandler
import com.hinaclient.hina.management.RotationManager
import com.hinaclient.hina.management.config.ConfigManager
import com.hinaclient.hina.module.ModuleManager
import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.glfw.GLFW

class MioHr : ClientModInitializer {

    companion object {
        @JvmStatic
        lateinit var INSTANCE: MioHr
            private set
    }

    lateinit var moduleManager: ModuleManager
    lateinit var configManager: ConfigManager
    lateinit var clickGuiKey: KeyMapping
    lateinit var rotationManager: RotationManager
    var logger: Logger = LogManager.getLogger("MioHr")

    override fun onInitializeClient() {
        INSTANCE = this

        moduleManager = ModuleManager()
        moduleManager.init()

        configManager = ConfigManager()
        configManager.load()

        rotationManager = RotationManager.INSTANCE
        rotationManager.init()

        clickGuiKey = KeyBindingHelper.registerKeyBinding(
            KeyMapping(
                "key.miohr.clickgui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyMapping.Category.MISC
            )
        )

        HinaHandler.init()
        HinaHandler.initEvent()
    }
}