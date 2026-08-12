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
import com.hinaclient.hina.event.impl.ClientTickEvent;
import com.hinaclient.hina.event.impl.packet.PacketEvent;
import com.hinaclient.hina.event.impl.packet.PacketType;
import com.hinaclient.hina.module.Category;
import com.hinaclient.hina.module.Module;
import com.hinaclient.hina.setting.BooleanSetting;
import com.hinaclient.hina.setting.NumberSetting;
import com.hinaclient.hina.utils.PacketUtil;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.phys.Vec3;

/**
 * Forced mace smash attack ("mace kill"), works while gliding with an elytra and can fake
 * any fall distance.
 *
 * <p>The server grants the smash attack when {@code fallDistance > 1.5 && !isFallFlying()}
 * ({@code MaceItem.canSmashAttack}) and derives the damage from the server-side fallDistance.
 * The server only accumulates fallDistance while its own simulation moves the player down
 * (movement packets are applied with absSnapTo and do NOT add fallDistance).</p>
 *
 * <p>This module exploits that: it teleports the server-side player up by the configured
 * height (small steps, far below the movement validation limit), then lets the server's own
 * free-fall physics accumulate fallDistance. When the estimated fall distance reaches the
 * target, the server-side position has fallen right back to the player's real position, so
 * the attack hits with the full smash damage. Real movement packets are suppressed while
 * charging so they cannot overwrite the server-side position. The gliding state is stopped
 * before the fake fall (START_FALL_FLYING toggles server-side) and restored afterwards.</p>
 */
public class MaceKill extends Module {
    private static final double ASCEND_SPEED = 5.0;      // server-side y gain per tick
    private static final double GRAVITY = 0.08;          // player gravity (blocks/tick^2)
    private static final int IDLE = 0;
    private static final int ASCEND = 1;
    private static final int FALLING = 2;

    private final NumberSetting fallHeight = new NumberSetting("Fall Height", 3.0, 1.6, 100.0, 0.1);
    private final BooleanSetting elytraRestore = new BooleanSetting("Elytra Restore", true);
    private final BooleanSetting autoMace = new BooleanSetting("Auto Mace", true);
    private final NumberSetting cooldown = new NumberSetting("Cooldown", 300.0, 0.0, 2000.0, 50.0);

    private int state = IDLE;
    private long lastSmash = 0L;
    private LivingEntity target;
    private boolean wasFlying;
    private double height;
    private double serverY;
    private double originY;
    private int fallTicks;

    public MaceKill() {
        super("MaceKill", Category.HACKS);
        addSetting(fallHeight);
        addSetting(elytraRestore);
        addSetting(autoMace);
        addSetting(cooldown);
    }

    @EventListener
    private void onAttack(AttackEvent event) {
        if (client.player == null || client.level == null || state != IDLE) return;
        if (System.currentTimeMillis() - lastSmash < cooldown.getValue()) return;

        // The server-side player must fall freely: standing on the ground would reset fallDistance.
        if (client.player.onGround()) return;

        if (!(client.player.getMainHandItem().getItem() instanceof MaceItem)) {
            if (!autoMace.getValue()) return;
            int slot = findMaceSlot();
            if (slot == -1) return;
            client.player.getInventory().setSelectedSlot(slot);
        }

        // Stop gliding first so the server stops resetting fallDistance.
        wasFlying = client.player.isFallFlying();
        if (wasFlying) {
            PacketUtil.sendNoEvent(new ServerboundPlayerCommandPacket(client.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        }

        height = fallHeight.getValue();
        originY = client.player.position().y;
        serverY = originY;
        target = event.getTarget();
        state = ASCEND;
        fallTicks = 0;
        event.setCancelled(true);
    }

    @EventListener
    private void onTick(ClientTickEvent event) {
        if (client.player == null || client.level == null || state == IDLE) return;

        if (state == ASCEND) {
            double step = Math.min(ASCEND_SPEED, originY + height - serverY);
            if (step <= 0.01) {
                state = FALLING;
                fallTicks = 0;
            } else {
                serverY += step;
                sendServerPos(serverY);
            }
            return;
        }

        if (state == FALLING) {
            fallTicks++;
            // Free fall distance after t ticks: 0.08 * (1 + 2 + ... + t)
            double fallen = GRAVITY * fallTicks * (fallTicks + 1) / 2.0;
            // Strike slightly early so the server-side position is still near the target;
            // at that point the server's fallDistance has (almost) reached the target height.
            if (fallen >= height - 2.0 || fallTicks > 5 * Math.sqrt(height) + 20) {
                strike();
            }
        }
    }

    @EventListener
    private void onPacket(PacketEvent event) {
        if (state == IDLE || event.getType() != PacketType.Send) return;
        // Suppress real movement while the fake fall is running so the server-side
        // position is not overwritten. Rotation/status-only packets are fine.
        if (event.getPacket() instanceof ServerboundMovePlayerPacket.Pos
                || event.getPacket() instanceof ServerboundMovePlayerPacket.PosRot) {
            event.setCancelled(true);
        }
    }

    private void strike() {
        if (client.player == null || client.level == null || target == null) {
            state = IDLE;
            return;
        }
        PacketUtil.sendNoEvent(ServerboundInteractPacket.createAttackPacket(target, true));
        client.player.swing(client.player.getUsedItemHand());
        if (wasFlying && elytraRestore.getValue()) {
            PacketUtil.sendNoEvent(new ServerboundPlayerCommandPacket(client.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        }
        state = IDLE;
        lastSmash = System.currentTimeMillis();
    }

    private void sendServerPos(double y) {
        Vec3 pos = client.player.position();
        PacketUtil.sendNoEvent(new ServerboundMovePlayerPacket.PosRot(
                pos.x, y, pos.z,
                client.player.getYRot(), client.player.getXRot(),
                false, false));
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
