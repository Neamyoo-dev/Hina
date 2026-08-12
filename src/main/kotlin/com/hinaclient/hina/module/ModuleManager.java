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

package com.hinaclient.hina.module;

import com.hinaclient.hina.event.EventListener;
import com.hinaclient.hina.event.EventBus;
import com.hinaclient.hina.event.impl.KeyEvent;
import com.hinaclient.hina.module.impl.combat.*;
import com.hinaclient.hina.module.impl.movement.*;
import com.hinaclient.hina.module.impl.optimization.*;
import com.hinaclient.hina.module.impl.player.*;
import com.hinaclient.hina.module.impl.render.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {
    private ClickGuiModule clickGuiModule;
    private final List<Module> modules = new ArrayList<>();

    public void init() {
        EventBus.INSTANCE.register(this);
        clickGuiModule = new ClickGuiModule();
        modules.add(clickGuiModule);

        modules.add(new Flight());
        modules.add(new ChamsModule());
        modules.add(new FullbrightModule());
        modules.add(new NoFall());
        modules.add(new ModuleList());
        modules.add(new TargetHud());
        modules.add(new FastPlace());
        modules.add(new SafeAnchor());
        modules.add(new AutoTotem());
        modules.add(new InvMove());
        modules.add(new AutoLunge());
        modules.add(new AutoSoup());
        modules.add(new MacePlus());
        modules.add(new MaceKill());
        modules.add(new BlockOptimizer());
        modules.add(new AntiCrash());
        modules.add(new NoTotemAnimation());
    }

    @EventListener
    public void onKey(KeyEvent event) {
        if (event.getKey() <= 0) return;
        modules.forEach(m -> {
            if (m.getKey() == event.getKey()) {
                m.toggle();
            }
        });
    }

    public ClickGuiModule getClickGuiModule() {
        return clickGuiModule;
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesByCategory(Category category) {
        return modules.stream().filter(m -> m.getCategory() == category).collect(Collectors.toList());
    }

    public Module getModuleByName(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}