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
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Truncates item tooltips to a sane number of lines. Malicious items (recursive NBT,
 * enormous lore/components) can otherwise freeze or crash the client while rendering
 * the tooltip.
 */
@Mixin(ItemStack.class)
public class ItemStackTooltipMixin {
    @Inject(method = "getTooltipLines", at = @At("RETURN"))
    private void hina$limitTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag,
                                        CallbackInfoReturnable<List<Component>> cir) {
        int limit = AntiCrash.getTooltipLineLimit();
        if (limit <= 0) return;
        List<Component> lines = cir.getReturnValue();
        if (lines.size() > limit) {
            cir.setReturnValue(new ArrayList<>(lines.subList(0, limit)));
        }
    }
}
