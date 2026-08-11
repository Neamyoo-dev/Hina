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
package com.hinaclient.hina.module.impl.combat

import com.hinaclient.hina.event.EventListener
import com.hinaclient.hina.event.impl.ClientTickEvent
import com.hinaclient.hina.module.Category
import com.hinaclient.hina.module.Module
import net.minecraft.client.KeyMapping
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments


class AutoLunge : Module("AutoLunge", Category.HACKS) {
    private var originalSlot = -1
    private var lungeSlot = -1
    private var stage = 0

    @EventListener
    fun onTick(event: ClientTickEvent) {
        if (!this.isEnabled) return
        val player = client.player ?: return

        when (stage) {
            0 -> {
                if (!(player.getFoodData().hasEnoughFood() || player.abilities.mayfly)) return

                lungeSlot = findLungeItem()
                if (lungeSlot == -1) {
                    this.toggle()
                    return
                }

                originalSlot = player.inventory.selectedSlot
                player.inventory.selectedSlot = lungeSlot

                KeyMapping.click(client.options.keyAttack.defaultKey);

                stage = 1
            }

            1 -> {
                player.inventory.selectedSlot = originalSlot
                stage = 0
                this.toggle()
            }
        }
    }

    private fun findLungeItem(): Int {
        val player = client.player ?: return -1
        for (i in 0..8) {
            val stack = player.inventory.getItem(i)
            if (hasLunge(stack)) return i
        }
        return -1
    }

    private fun hasLunge(stack: ItemStack): Boolean {
        if (stack.isEmpty) return false
        val lunge = getLungeHolder() ?: return false
        return EnchantmentHelper.getItemEnchantmentLevel(lunge, stack) > 0
    }

    private fun getLungeHolder(): Holder<Enchantment>? {
        val level = client.level ?: return null
        val registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
        return registry.get(Enchantments.LUNGE).orElse(null)
    }
}
