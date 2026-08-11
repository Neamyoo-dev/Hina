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

/**
 * Removes the fullscreen totem of undying activation animation that blocks the view.
 * The totem sound and green particles still play - only the screen-covering overlay is gone.
 */
public class NoTotemAnimation extends Module {
    public NoTotemAnimation() {
        super("NoTotemAnimation", Category.RENDER);
    }

    public static boolean shouldDisable() {
        NoTotemAnimation module = (NoTotemAnimation) MioHr.getINSTANCE().moduleManager.getModuleByName("NoTotemAnimation");
        return module != null && module.isEnabled();
    }
}
