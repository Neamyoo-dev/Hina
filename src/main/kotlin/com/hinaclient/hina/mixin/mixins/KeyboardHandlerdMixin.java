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

package com.hinaclient.hina.mixin.mixins;

import com.hinaclient.hina.event.EventBus;
import com.hinaclient.hina.event.impl.KeyEvent;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @Author: oneachina
 * @Date: 2026/2/1 12:39
 */
@Mixin(KeyboardHandler.class)
public class KeyboardHandlerdMixin {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void keyPress(long window, int action, net.minecraft.client.input.KeyEvent keyEvent, CallbackInfo ci) {
        if (action == 1) {
            EventBus.INSTANCE.post(new KeyEvent(keyEvent.key()));
        }
    }
}
