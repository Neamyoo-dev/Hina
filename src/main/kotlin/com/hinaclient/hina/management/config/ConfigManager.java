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

package com.hinaclient.hina.management.config;

import com.hinaclient.hina.MioHr;
import com.hinaclient.hina.module.Module;
import com.hinaclient.hina.setting.*;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Path;

/**
 * @author Eatgrapes
 * @link github.com/Eatgrapes
 */
public class ConfigManager {
    private final File configDir;
    private final File configFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ConfigManager() {
        Path gameDir = FabricLoader.getInstance().getGameDir();
        File hinaFolder = new File(gameDir.toFile(), "Hina");
        this.configDir = new File(hinaFolder, "config");
        this.configFile = new File(configDir, "config.json");
        if (!configDir.exists()) configDir.mkdirs();
    }

    public void save() {
        JsonObject json = new JsonObject();
        JsonArray modulesArray = new JsonArray();
        for (Module module : MioHr.getINSTANCE().moduleManager.getModules()) {
            JsonObject moduleJson = new JsonObject();
            moduleJson.addProperty("name", module.getName());
            moduleJson.addProperty("enabled", module.isEnabled());
            moduleJson.addProperty("x", module.getX());
            moduleJson.addProperty("y", module.getY());
            JsonObject settingsJson = getJsonObject(module);
            moduleJson.add("settings", settingsJson);
            modulesArray.add(moduleJson);
        }
        json.add("modules", modulesArray);
        try (Writer writer = new FileWriter(configFile)) {
            gson.toJson(json, writer);
        } catch (IOException ignored) {
        }
    }

    private static @NotNull JsonObject getJsonObject(Module module) {
        JsonObject settingsJson = new JsonObject();
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof BooleanSetting) settingsJson.addProperty(setting.getName(), ((BooleanSetting) setting).getValue());
            else if (setting instanceof NumberSetting) settingsJson.addProperty(setting.getName(), ((NumberSetting) setting).getValue());
            else if (setting instanceof ModeSetting) settingsJson.addProperty(setting.getName(), ((ModeSetting) setting).getValue());
            else if (setting instanceof ColorSetting) settingsJson.addProperty(setting.getName(), ((ColorSetting) setting).getValue());
        }
        return settingsJson;
    }

    public void load() {
        if (!configFile.exists()) return;
        try (Reader reader = new FileReader(configFile)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (json.has("modules")) {
                JsonArray modulesArray = json.getAsJsonArray("modules");
                for (JsonElement element : modulesArray) {
                    JsonObject moduleJson = element.getAsJsonObject();
                    String name = moduleJson.get("name").getAsString();
                    Module module = MioHr.getINSTANCE().moduleManager.getModuleByName(name);
                    if (module != null) {
                        if (moduleJson.has("enabled")) module.setEnabled(moduleJson.get("enabled").getAsBoolean());
                        if (moduleJson.has("x")) module.setX(moduleJson.get("x").getAsDouble());
                        if (moduleJson.has("y")) module.setY(moduleJson.get("y").getAsDouble());
                        if (moduleJson.has("settings")) {
                            JsonObject settingsJson = moduleJson.getAsJsonObject("settings");
                            for (Setting<?> setting : module.getSettings()) {
                                if (settingsJson.has(setting.getName())) {
                                    JsonElement val = settingsJson.get(setting.getName());
                                    switch (setting) {
                                        case BooleanSetting booleanSetting ->
                                                booleanSetting.setValue(val.getAsBoolean());
                                        case NumberSetting numberSetting -> numberSetting.setValue(val.getAsDouble());
                                        case ModeSetting modeSetting -> modeSetting.setValue(val.getAsString());
                                        case ColorSetting colorSetting -> colorSetting.setValue(val.getAsInt());
                                        default -> {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}