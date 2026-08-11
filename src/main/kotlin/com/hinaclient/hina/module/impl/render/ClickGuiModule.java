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

package com.hinaclient.hina.module.impl.render;

import com.hinaclient.hina.MioHr;
import com.hinaclient.hina.module.Category;
import com.hinaclient.hina.module.Module;
import com.hinaclient.hina.ui.ClickGuiScreen;
import com.hinaclient.hina.setting.*;

public class ClickGuiModule extends Module {
    public static ColorSetting themeColor = new ColorSetting("Theme Color", 0xFF789B82);
    public static ModeSetting test4 = new ModeSetting("Test 4", "a", "a", "b", "c");
    public static BooleanSetting debug = new BooleanSetting("debug", false);

    public ClickGuiModule() {
        super("ClickGUI", Category.RENDER);
        addSetting(new BooleanSetting("Test 1", true));
        addSetting(new NumberSetting("Test 2", 1.0, 0.1, 5.0, 0.1));
        addSetting(new ModeSetting("Test 3", "Modern", "Modern", "Classic", "Minimal"));
        addSetting(test4);
        addSetting(themeColor);
        addSetting(debug);

        test4.setMulti(true);
    }
    
    public static int getThemeColor() {
        return themeColor.getValue();
    }

    @Override
    protected void onEnable() {
        if (client.screen == null) {
            client.execute(() -> {
                client.setScreen(new ClickGuiScreen());
            });
        }
    }
    
    @Override
    protected void onDisable() {
        if (client.screen instanceof ClickGuiScreen screen) screen.closeGui();
        MioHr.getINSTANCE().configManager.save();
    }
}
