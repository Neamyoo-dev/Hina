package com.hinaclient.hina.module.impl.combat;

import com.hinaclient.hina.event.EventListener;
import com.hinaclient.hina.event.impl.AttackEvent;
import com.hinaclient.hina.event.impl.ClientTickEvent;
import com.hinaclient.hina.module.Category;
import com.hinaclient.hina.module.Module;
import com.hinaclient.hina.setting.BooleanSetting;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Random;

public class MacePlus extends Module {
    private static final Random RANDOM = new Random();

    private static final int ASB_IDLE = 0;
    private static final int ASB_AXE_CLICK = 1;
    private static final int ASB_AXE_SENT = 2;
    private static final int ASB_MACE_CLICK = 3;
    private static final int ASB_MACE_SENT = 4;
    private static final int ASB_DONE = 5;

    private final BooleanSetting alwaysShieldBreak = new BooleanSetting("AlwaysShieldBreak", false);

    private int originalSlot = -1;
    private long revertTime = -1;
    private boolean pendingRevert = false;

    private boolean shieldBreakInProgress = false;
    private int maceSlot = -1;
    private LivingEntity shieldTarget;

    private int asbState = ASB_IDLE;

    public MacePlus() {
        super("MacePlus", Category.HACKS);
        addSetting(alwaysShieldBreak);
    }

    @EventListener
    private void onAttack(AttackEvent event) {
        if (client.player == null || client.level == null) return;
        LivingEntity target = event.getTarget();
        if (target == null) return;

        if (alwaysShieldBreak.getValue()) {
            switch (asbState) {
                case ASB_AXE_SENT:
                    asbState = ASB_MACE_CLICK;
                    return;
                case ASB_MACE_SENT:
                    asbState = ASB_DONE;
                    return;
                case ASB_IDLE: {
                    int axe = findAxeSlot();
                    int bestMace = findBestMaceSlot();
                    if (axe == -1 || bestMace == -1) return;
                    originalSlot = client.player.getInventory().getSelectedSlot();
                    maceSlot = bestMace;
                    event.setCancelled(true);
                    asbState = ASB_AXE_CLICK;
                    return;
                }
                default:
                    event.setCancelled(true);
                    return;
            }
        }

        if (hasShield(target)) {
            int axeSlot = findAxeSlot();
            maceSlot = findBestMaceSlot();
            if (axeSlot == -1 || maceSlot == -1) return;

            originalSlot = client.player.getInventory().getSelectedSlot();
            client.player.getInventory().setSelectedSlot(axeSlot);
            shieldTarget = target;
            shieldBreakInProgress = true;
            return;
        }

        int currentSlot = client.player.getInventory().getSelectedSlot();
        int bestMace = findBestMaceSlot();
        if (bestMace == -1) return;

        if (currentSlot == bestMace) {
            if (pendingRevert) {
                revertTime = System.currentTimeMillis() + randomRevertDelay();
            }
            return;
        }

        originalSlot = currentSlot;
        client.player.getInventory().setSelectedSlot(bestMace);
        revertTime = System.currentTimeMillis() + randomRevertDelay();
        pendingRevert = true;
    }

    @EventListener
    private void onTick(ClientTickEvent event) {
        if (client.player == null || client.level == null) return;
        long now = System.currentTimeMillis();

        if (alwaysShieldBreak.getValue()) {
            switch (asbState) {
                case ASB_AXE_CLICK:
                    client.player.getInventory().setSelectedSlot(findAxeSlot());
                    KeyMapping.click(client.options.keyAttack.getDefaultKey());
                    asbState = ASB_AXE_SENT;
                    return;
                case ASB_MACE_CLICK:
                    client.player.getInventory().setSelectedSlot(maceSlot);
                    KeyMapping.click(client.options.keyAttack.getDefaultKey());
                    asbState = ASB_MACE_SENT;
                    return;
                case ASB_DONE:
                    if (originalSlot != -1) {
                        client.player.getInventory().setSelectedSlot(originalSlot);
                    }
                    originalSlot = -1;
                    maceSlot = -1;
                    asbState = ASB_IDLE;
                    return;
            }
            return;
        }

        if (shieldBreakInProgress) {
            if (shieldTarget == null || shieldTarget.isRemoved()) {
                revertAfterShieldBreak();
                return;
            }
            if (!shieldTarget.isBlocking()) {
                client.player.getInventory().setSelectedSlot(maceSlot);
                KeyMapping.click(client.options.keyAttack.getDefaultKey());
                revertAfterShieldBreak();
            }
            return;
        }

        if (pendingRevert && now >= revertTime) {
            client.player.getInventory().setSelectedSlot(originalSlot);
            originalSlot = -1;
            pendingRevert = false;
        }
    }

    private void revertAfterShieldBreak() {
        shieldBreakInProgress = false;
        maceSlot = -1;
        shieldTarget = null;
        if (client.player != null && originalSlot != -1) {
            client.player.getInventory().setSelectedSlot(originalSlot);
        }
        originalSlot = -1;
    }

    private boolean hasShield(LivingEntity entity) {
        return entity.isBlocking();
    }

    private int findAxeSlot() {
        for (int i = 0; i <= 8; i++) {
            ItemStack stack = client.player.getInventory().getItem(i);
            if (stack.getItem() instanceof AxeItem) return i;
        }
        return -1;
    }

    private int findBestMaceSlot() {
        if (client.player == null) return -1;
        boolean canUseHeavyMace = isFallingForHeavyMace();
        int breach = -1, density = -1, any = -1;
        for (int i = 0; i <= 8; i++) {
            ItemStack stack = client.player.getInventory().getItem(i);
            if (stack.getItem() != Items.MACE) continue;
            any = i;
            if (hasEnchant(stack, Enchantments.BREACH)) breach = i;
            if (hasEnchant(stack, Enchantments.DENSITY)) density = i;
        }
        if (canUseHeavyMace) {
            if (density != -1) return density;
            if (any != -1) return any;
            return breach;
        } else {
            if (breach != -1) return breach;
            if (any != -1) return any;
            return density;
        }
    }

    private boolean isFallingForHeavyMace() {
        if (client.player == null) return false;
        return client.player.getDeltaMovement().y < -0.1 && client.player.fallDistance >= 2.0;
    }

    private boolean hasEnchant(ItemStack stack, ResourceKey<Enchantment> key) {
        if (stack.isEmpty()) return false;
        Holder<Enchantment> holder = getEnchantmentsHolder(key);
        if (holder == null) return false;
        return EnchantmentHelper.getItemEnchantmentLevel(holder, stack) > 0;
    }

    private Holder<Enchantment> getEnchantmentsHolder(ResourceKey<Enchantment> key) {
        if (client.level == null) return null;
        var registry = client.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        return registry.get(key).orElse(null);
    }

    private int randomRevertDelay() {
        return 86 + RANDOM.nextInt(47);
    }

    @Override
    protected void onDisable() {
        if (shieldBreakInProgress) {
            revertAfterShieldBreak();
        } else if (pendingRevert && client.player != null && originalSlot != -1) {
            client.player.getInventory().setSelectedSlot(originalSlot);
        }
        originalSlot = -1;
        pendingRevert = false;
        asbState = ASB_IDLE;
        super.onDisable();
    }
}
