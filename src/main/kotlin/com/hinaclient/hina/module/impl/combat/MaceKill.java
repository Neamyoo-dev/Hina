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
import com.hinaclient.hina.event.impl.AttackEvent;
import com.hinaclient.hina.module.Category;
import com.hinaclient.hina.module.Module;
import com.hinaclient.hina.setting.BooleanSetting;
import com.hinaclient.hina.setting.NumberSetting;
import com.hinaclient.hina.utils.PacketUtil;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.phys.Vec3;

/**
 * Forced mace smash attack ("mace kill").
 *
 * <p>The server only grants the smash attack when {@code fallDistance > 1.5 && !isFallFlying()}
 * (see {@code MaceItem.canSmashAttack}), which makes it impossible to use while gliding with
 * an elytra. This module fakes the fall from the server's point of view using only legitimate
 * packets: it stops the gliding state (START_FALL_FLYING acts as a toggle server-side), moves
 * the server-side player down in small steps so the server accumulates fallDistance, then
 * attacks with the mace. After the hit the gliding state is restored.</p>
 */
public class MaceKill extends Module {
    private static final double DROP_STEP = 0.5;

    private final NumberSetting fallHeight = new NumberSetting("Fall Height", 3.0, 1.6, 8.0, 0.1);
    private final BooleanSetting elytraRestore = new BooleanSetting("Elytra Restore", true);
    private final BooleanSetting autoMace = new BooleanSetting("Auto Mace", true);
    private final NumberSetting cooldown = new NumberSetting("Cooldown", 300.0, 0.0, 2000.0, 50.0);

    private long lastSmash = 0L;

    public MaceKill() {
        super("MaceKill", Category.HACKS);
        addSetting(fallHeight);
        addSetting(elytraRestore);
        addSetting(autoMace);
        addSetting(cooldown);
    }

    @EventListener
    private void onAttack(AttackEvent event) {
        if (client.player == null || client.level == null) return;
        if (System.currentTimeMillis() - lastSmash < cooldown.getValue()) return;

        // The server only accumulates fallDistance while the player is airborne.
        if (client.player.onGround()) return;

        if (!(client.player.getMainHandItem().getItem() instanceof MaceItem)) {
            if (!autoMace.getValue()) return;
            int slot = findMaceSlot();
            if (slot == -1) return;
            client.player.getInventory().setSelectedSlot(slot);
        }

        // 1. Stop gliding if needed (START_FALL_FLYING toggles the state server-side).
        boolean wasFlying = client.player.isFallFlying();
        if (wasFlying) {
            PacketUtil.sendNoEvent(new ServerboundPlayerCommandPacket(client.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        }

        // 2. Fake a fall: move the server-side player down in small steps so the server
        //    accumulates fallDistance. Each step is far below the movement validation limit.
        Vec3 pos = client.player.position();
        int steps = (int) Math.ceil(fallHeight.getValue() / DROP_STEP);
        for (int i = 0; i < steps; i++) {
            pos = pos.add(0.0, -DROP_STEP, 0.0);
            PacketUtil.sendNoEvent(new ServerboundMovePlayerPacket.PosRot(
                    pos.x, pos.y, pos.z,
                    client.player.getYRot(), client.player.getXRot(),
                    false, false));
        }

        // 3. Smash attack.
        PacketUtil.sendNoEvent(ServerboundInteractPacket.createAttackPacket(event.getTarget(), true));
        client.player.swing(client.player.getUsedItemHand());

        // 4. Restore the gliding state.
        if (wasFlying && elytraRestore.getValue()) {
            PacketUtil.sendNoEvent(new ServerboundPlayerCommandPacket(client.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        }

        lastSmash = System.currentTimeMillis();
        event.setCancelled(true);
    }

    private int findMaceSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getItem(i);
            if (stack.getItem() instanceof MaceItem) {
                return i;
            }
        }
        return -1;
    }
}
