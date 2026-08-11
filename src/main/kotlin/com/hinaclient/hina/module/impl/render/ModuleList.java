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
import com.hinaclient.hina.event.EventListener;
import com.hinaclient.hina.event.skia.EventSkiaDrawScene;
import com.hinaclient.hina.module.Category;
import com.hinaclient.hina.module.Module;
import com.hinaclient.hina.setting.BooleanSetting;
import com.hinaclient.hina.setting.ColorSetting;
import com.hinaclient.hina.skia.font.FontManager;
import com.hinaclient.hina.ui.Colors;
import com.hinaclient.hina.ui.hud.HudElement;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.RRect;

import java.util.*;
import java.util.Comparator;
import java.util.List;

public class ModuleList extends Module implements HudElement {
    private final BooleanSetting iconenb = new BooleanSetting("Icon", true);
    private final BooleanSetting useThemeColor = new BooleanSetting("Use Theme Color", true);
    private final ColorSetting textColor = new ColorSetting("Text Color", 0xFFFFFFFF);
    private final Map<Module, Float> moduleProgressMap = new HashMap<>();
    private float hudWidth = 120f;
    private float hudHeight = 40f;

    private static final float MODULE_HEIGHT = 30f;
    private static final float PADDING = 10f;
    private static final float SPACING = 2f;
    private static final float ACCENT_WIDTH = 3f;
    private static final float ACCENT_OFFSET = 2f;

    public ModuleList() {
        super("ModuleList", Category.RENDER);
        addSetting(iconenb);
        addSetting(useThemeColor);
        addSetting(textColor.setVisibility(() -> !useThemeColor.getValue()));
    }

    @EventListener
    public void onDraw(EventSkiaDrawScene event) {
        if (!FontManager.INSTANCE.isInitialized()) return;

        Canvas canvas = event.getCanvas();
        float scale = (float) client.getWindow().getGuiScale();
        canvas.save();
        canvas.scale(scale, scale);
        Font textFont = FontManager.INSTANCE.getTextFont(16f);
        Font iconFont = FontManager.INSTANCE.getIconFont(18f);

        for (Module m : MioHr.getINSTANCE().moduleManager.getModules()) {
            boolean enabled = m.isEnabled() && !Objects.equals(m.getName(), "ClickGUI") && !Objects.equals(m.getName(), "ModuleList");
            float target = enabled ? 1.0f : 0.0f;
            float current = moduleProgressMap.getOrDefault(m, 0.0f);

            if (Math.abs(current - target) > 0.001f) {
                current += (target - current) * 0.15f;
                moduleProgressMap.put(m, current);
            } else {
                current = target;
                if (current <= 0.0f) moduleProgressMap.remove(m);
            }
        }

        List<Module> displayModules = moduleProgressMap.keySet().stream()
                .sorted(Comparator.comparingDouble((Module m) -> getWidth(m, textFont, iconFont)).reversed())
                .toList();
        if (displayModules.isEmpty()) {
            canvas.restore();
            return;
        }

        float xOffset = (float) getX();
        float yOffset = (float) getY();

        float maxWidth = 0f;
        for (Module m : displayModules) {
            float w = getWidth(m, textFont, iconFont);
            if (w > maxWidth) maxWidth = w;
        }
        float panelWidth = maxWidth + PADDING * 2;
        float panelHeight = MODULE_HEIGHT * displayModules.size() + SPACING * (displayModules.size() - 1) + PADDING * 2;
        hudWidth = panelWidth;
        hudHeight = panelHeight;

        try (Paint surface = new Paint().setColor(Colors.SURFACE_SOFT);
             Paint border = new Paint().setColor(Colors.GLASS_BORDER).setMode(PaintMode.STROKE).setStrokeWidth(1f)) {
            RRect bounds = RRect.makeXYWH(xOffset, yOffset, panelWidth, panelHeight, Colors.RADIUS_MEDIUM);
            canvas.drawRRect(bounds, surface);
            canvas.drawRRect(bounds, border);
        }

        int accentColor;
        if (useThemeColor.getValue()) {
            accentColor = ClickGuiModule.getThemeColor();
        } else {
            accentColor = textColor.getValue();
        }

        float currentY = yOffset + PADDING;
        for (Module m : displayModules) {
            float progress = moduleProgressMap.getOrDefault(m, 0f);
            if (progress <= 0f) continue;

            float moduleX = xOffset + PADDING;
            float moduleY = currentY;
            float moduleWidth = maxWidth;
            float moduleHeight = MODULE_HEIGHT;

            canvas.save();
            canvas.translate(moduleX, moduleY);
            canvas.scale(progress, progress);

            try (Paint accentPaint = new Paint().setColor(accentColor)) {
                float accentHeight = moduleHeight - 6f;
                float accentY = (moduleHeight - accentHeight) / 2f;
                canvas.drawRRect(RRect.makeXYWH(ACCENT_OFFSET, accentY, ACCENT_WIDTH, accentHeight, 2f), accentPaint);
            }

            try (Paint textPaint = new Paint().setColor(Colors.TEXT_PRIMARY)) {
                float textX = ACCENT_OFFSET + ACCENT_WIDTH + 6f;
                float textY = moduleHeight / 2f + textFont.getMetrics().getCapHeight() / 2f - 2f;

                if (iconenb.getValue()) {
                    String icon = m.getCategory().getIcon();
                    canvas.drawString(icon, textX, textY + 1, iconFont, textPaint);
                    textX += 20;
                }

                canvas.drawString(m.getName(), textX, textY, textFont, textPaint);
            }

            canvas.restore();

            currentY += moduleHeight + SPACING;
        }
        canvas.restore();
    }

    @Override
    public float getHudWidth() {
        return hudWidth;
    }

    @Override
    public float getHudHeight() {
        return hudHeight;
    }

    private float getWidth(Module m, Font textFont, Font iconFont) {
        try (TextLine nameLine = TextLine.make(m.getName(), textFont);
             TextLine iconLine = TextLine.make(m.getCategory().getIcon(), iconFont)) {
            float base = nameLine.getWidth() + (iconenb.getValue() ? iconLine.getWidth() + 6 : 0);
            return base + ACCENT_OFFSET + ACCENT_WIDTH + 6;
        }
    }
}
