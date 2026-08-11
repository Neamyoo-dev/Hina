package com.hinaclient.hina.module.impl.combat;

import com.hinaclient.hina.event.EventListener;
import com.hinaclient.hina.event.impl.BlockPlaceEvent;
import com.hinaclient.hina.event.impl.ClientTickEvent;
import com.hinaclient.hina.module.Category;
import com.hinaclient.hina.module.Module;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.item.Items;

public class SafeAnchor extends Module {
    private static final int IDLE = 0;
    private static final int PLACED = 1;
    private static final int CHARGING = 2;
    private static final int DETONATING = 3;

    private int state = IDLE;
    private int originalSlot = -1;
    private int glowstoneSlot = -1;

    public SafeAnchor() {
        super("SafeAnchor", Category.HACKS);
    }

    @EventListener
    public void onBlockPlace(BlockPlaceEvent event) {
        if (client.player == null) return;
        if (event.getItem().getItem() != Items.RESPAWN_ANCHOR) return;

        glowstoneSlot = findGlowstoneSlot();
        if (glowstoneSlot == -1) return;

        originalSlot = client.player.getInventory().getSelectedSlot();
        state = PLACED;
    }

    @EventListener
    public void onTick(ClientTickEvent event) {
        if (client.player == null) return;

        if (state == PLACED) {
            client.player.getInventory().setSelectedSlot(glowstoneSlot);
            KeyMapping.click(client.options.keyUse.getDefaultKey());
            state = CHARGING;
            return;
        }

        if (state == CHARGING) {
            int detonateSlot = findDetonateSlot();
            if (detonateSlot == -1) {
                revert();
                return;
            }
            client.player.getInventory().setSelectedSlot(detonateSlot);
            KeyMapping.click(client.options.keyUse.getDefaultKey());
            state = DETONATING;
            return;
        }

        if (state == DETONATING) {
            revert();
        }
    }

    private void revert() {
        if (client.player != null && originalSlot != -1) {
            client.player.getInventory().setSelectedSlot(originalSlot);
        }
        originalSlot = -1;
        glowstoneSlot = -1;
        state = IDLE;
    }

    private int findGlowstoneSlot() {
        for (int i = 0; i <= 8; i++) {
            if (client.player.getInventory().getItem(i).is(Items.GLOWSTONE)) return i;
        }
        return -1;
    }

    private int findDetonateSlot() {
        for (int i = 0; i <= 8; i++) {
            if (client.player.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) return i;
        }
        for (int i = 0; i <= 8; i++) {
            if (!client.player.getInventory().getItem(i).is(Items.GLOWSTONE)) return i;
        }
        return client.player.getInventory().getSelectedSlot();
    }

    @Override
    protected void onDisable() {
        revert();
        super.onDisable();
    }
}
