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
import com.hinaclient.hina.event.impl.packet.PacketEvent;
import com.hinaclient.hina.event.impl.packet.PacketType;
import com.hinaclient.hina.mixin.mixins.accessors.ConnectionAccessor;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @Author: oneachina
 * @link: github.com/oneachina
 */
@Mixin(Connection.class)
public class ConnectionMixin {
    @Redirect(
            method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;sendPacket(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V")
    )
    private void redirectSendPacket(Connection instance, Packet<?> packet, ChannelFutureListener listener, boolean flush) {
        PacketEvent event = new PacketEvent(packet, PacketType.Send);
        EventBus.INSTANCE.post(event);

        if (!event.isCancelled()) {
            ((ConnectionAccessor)instance).invokeSendPacket(event.getPacket(), listener, flush);
        }
    }

    @Redirect(
            method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;genericsFtw(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;)V")
    )
    private void redirectGenericsFtw(Packet<?> packet, PacketListener listener) {
        PacketEvent event = new PacketEvent(packet, PacketType.Receive);
        EventBus.INSTANCE.post(event);

        if (!event.isCancelled()) {
            ConnectionAccessor.genericsFtw(event.getPacket(), listener);
        }
    }
}