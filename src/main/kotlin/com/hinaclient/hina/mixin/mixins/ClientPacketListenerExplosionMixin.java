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

import com.hinaclient.hina.module.impl.optimization.AntiCrash;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skips the client-side handling of absurdly large explosions (block destruction animation,
 * particles, sounds) which would otherwise trigger a mesh rebuild storm.
 */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerExplosionMixin {
    @Inject(method = "handleExplosion", at = @At("HEAD"), cancellable = true)
    private void hina$limitExplosion(ClientboundExplodePacket packet, CallbackInfo ci) {
        int limit = AntiCrash.getExplosionBlockLimit();
        if (limit > 0 && packet.blockCount() > limit) {
            ci.cancel();
        }
    }
}
