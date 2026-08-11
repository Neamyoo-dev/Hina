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

package com.hinaclient.hina.module.impl.movement;

import com.hinaclient.hina.event.EventListener;
import com.hinaclient.hina.event.impl.UpdateEvent;
import com.hinaclient.hina.module.Category;
import com.hinaclient.hina.module.Module;
import com.hinaclient.hina.setting.BooleanSetting;
import com.hinaclient.hina.setting.ModeSetting;
import com.hinaclient.hina.setting.NumberSetting;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

/**
 * @Author: oneachina
 * @Date: 2026/2/1 12:32
 */
public class Flight extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Creative", "Creative");

    private final NumberSetting vSpeed = new NumberSetting("C-Speed", 0.05, 0.01, 1.0, 0.01);
    private final BooleanSetting vSprint = new BooleanSetting("C-Sprint", true);
    private final NumberSetting vSprintSpeed = new NumberSetting("C-SprintSpeed", 0.1, 0.01, 1.0, 0.01);

    private final BooleanSetting bypass = new BooleanSetting("Bypass", true);

    public Flight() {
        super("Flight", Category.HACKS);

        addSetting(mode);
        addSetting(vSpeed);
        addSetting(vSprint);
        addSetting(vSprintSpeed);
        addSetting(bypass);

        vSpeed.setVisibility(() -> mode.getValue().equals("Creative"));
        vSprint.setVisibility(() -> mode.getValue().equals("Creative"));
        vSprintSpeed.setVisibility(() -> mode.getValue().equals("Creative") && vSprint.getValue());
    }

    @Override
    protected void onEnable() {
        if (client.player == null) return;
        if (mode.getValue().equals("Creative")) {
            client.player.getAbilities().mayfly = true;
            client.player.getAbilities().flying = true;
        }
    }

    @Override
    protected void onDisable() {
        if (client.player == null) return;
        client.player.getAbilities().flying = false;
        if (!client.player.isCreative()) {
            client.player.getAbilities().mayfly = false;
        }
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (client.player == null) return;
        handleVanilla();
    }

    private void handleVanilla() {
        assert client.player != null;
        client.player.getAbilities().flying = true;
        float speed = (vSprint.getValue() && client.options.keySprint.isDown()) ?
                vSprintSpeed.getValue().floatValue() : vSpeed.getValue().floatValue();
        client.player.getAbilities().setFlyingSpeed(speed);

        if (bypass.getValue() && client.player.tickCount % 40 == 0) {
            Vec3 currentMovement = client.player.getDeltaMovement();
            client.player.setDeltaMovement(currentMovement.x(), -0.04, currentMovement.z);
        }
    }
}