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

package com.hinaclient.hina.skia.font;

import io.github.humbleui.skija.Data;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.Typeface;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Eatgrapes
 * @link github.com/Eatgrapes
 */
public class FontManager {
    public static final FontManager INSTANCE = new FontManager();
    
    private Typeface textTypeface;
    private Typeface iconTypeface;
    
    private final Map<Float, Font> textFonts = new HashMap<>();
    private final Map<Float, Font> iconFonts = new HashMap<>();

    private boolean initialized = false;

    public boolean isInitialized() {
        return initialized;
    }

    public void init() {
        if (initialized) return;
        try (InputStream textStream = Minecraft.getInstance().getResourceManager()
                     .getResource(Identifier.fromNamespaceAndPath("miohr", "fonts/pingfang-regular.ttf")).orElseThrow().open();
             InputStream iconStream = Minecraft.getInstance().getResourceManager()
                     .getResource(Identifier.fromNamespaceAndPath("miohr", "fonts/icon.ttf")).orElseThrow().open();
             Data textData = Data.makeFromBytes(textStream.readAllBytes());
             Data iconData = Data.makeFromBytes(iconStream.readAllBytes())) {
            textTypeface = Typeface.makeFromData(textData);
            iconTypeface = Typeface.makeFromData(iconData);
            initialized = true;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to initialize MioHr fonts", exception);
        }
    }

    public Font getIconFont(float size) {
        return iconFonts.computeIfAbsent(size, s -> new Font(iconTypeface, s));
    }

    public Font getTextFont(float size) {
        return textFonts.computeIfAbsent(size, s -> new Font(textTypeface, s));
    }

    public void close() {
        textFonts.values().forEach(Font::close);
        iconFonts.values().forEach(Font::close);
        textFonts.clear();
        iconFonts.clear();
        if (textTypeface != null) textTypeface.close();
        if (iconTypeface != null) iconTypeface.close();
        textTypeface = null;
        iconTypeface = null;
        initialized = false;
    }
}
