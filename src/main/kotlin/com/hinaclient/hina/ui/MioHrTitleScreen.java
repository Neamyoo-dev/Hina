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
package com.hinaclient.hina.ui;

import com.hinaclient.hina.event.EventBus;
import com.hinaclient.hina.event.EventListener;
import com.hinaclient.hina.event.skia.EventSkiaDrawScene;
import com.hinaclient.hina.skia.font.FontManager;
import com.hinaclient.hina.skia.font.Icon;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import ru.vidtu.ias.screen.AccountScreen;

import java.io.InputStream;
import java.util.List;

public class MioHrTitleScreen extends Screen {
    private final List<TitleAction> actions;
    private Image backgroundImage;
    private int mouseX;
    private int mouseY;

    public MioHrTitleScreen() {
        super(Component.literal("MioHr TitleScreen"));
        actions = List.of(
                new TitleAction("Single Player", "Local worlds", Icon.PEOPLE, () -> minecraft.setScreen(new SelectWorldScreen(this))),
                new TitleAction("Multi Player", "Online servers", Icon.LAN, () -> minecraft.setScreen(new JoinMultiplayerScreen(this))),
                new TitleAction("Alt Manager", "Accounts", Icon.MANAGE_ACCOUNTS, () -> minecraft.setScreen(new AccountScreen(this))),
                new TitleAction("Options", "Preferences", Icon.SETTINGS, () -> minecraft.setScreen(new OptionsScreen(this, minecraft.options))),
                new TitleAction("Quit Game", "Exit client", Icon.POWER_SETTINGS_NEW, () -> minecraft.stop())
        );
    }

    @Override
    public void init() {
        super.init();
        if (!FontManager.INSTANCE.isInitialized()) FontManager.INSTANCE.init();
        if (backgroundImage == null) loadImage();
        if (!EventBus.INSTANCE.isregister(this)) {
            EventBus.INSTANCE.register(this);
        }
    }

    @Override
    public void removed() {
        EventBus.INSTANCE.unregister(this);
        if (backgroundImage != null) {
            backgroundImage.close();
            backgroundImage = null;
        }
        super.removed();
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean bl) {
        for (TitleAction action : actions) {
            if (action.bounds != null && action.bounds.contains((float) event.x(), (float) event.y())) {
                Minecraft.getInstance().execute(action.action);
                return true;
            }
        }
        return super.mouseClicked(event, bl);
    }

    @EventListener
    public void onSkiaRender(EventSkiaDrawScene event) {
        if (this.minecraft.screen != this) {
            return;
        }

        var canvas = event.getCanvas();

        int mcScale = this.minecraft.getWindow().getGuiScale();
        float screenWidth = (float) this.minecraft.getWindow().getGuiScaledWidth();
        float screenHeight = (float) this.minecraft.getWindow().getGuiScaledHeight();

        drawbg(canvas);

        canvas.save();
        canvas.scale(mcScale, mcScale);
        drawInterface(canvas, screenWidth, screenHeight);
        canvas.restore();
    }

    private void loadImage() {
        if (backgroundImage == null) {
            try (InputStream stream = Minecraft.getInstance().getResourceManager()
                    .getResource(Identifier.fromNamespaceAndPath("miohr", "textures/gui/title/bg.png"))
                    .orElseThrow().open()) {
                backgroundImage = Image.makeDeferredFromEncodedBytes(stream.readAllBytes());
            } catch (Exception e) {
                backgroundImage = null;
            }
        }
    }

    private void drawbg(Canvas canvas) {
        int screenWidth = this.minecraft.getWindow().getWidth();
        int screenHeight = this.minecraft.getWindow().getHeight();

        if (backgroundImage != null) {
            canvas.drawImageRect(backgroundImage, Rect.makeXYWH(0, 0, screenWidth, screenHeight));
        }
        try (Paint tint = new Paint().setColor(0x70F4EDE3)) {
            canvas.drawRect(Rect.makeXYWH(0, 0, screenWidth, screenHeight), tint);
        }
    }

    private void drawInterface(Canvas canvas, float screenWidth, float screenHeight) {
        float margin = Math.max(18f, Math.min(screenWidth, screenHeight) * 0.055f);
        float x = margin;
        float y = margin;
        float width = screenWidth - margin * 2f;
        float height = screenHeight - margin * 2f;
        float railWidth = Math.max(170f, width * 0.24f);
        RRect shell = RRect.makeXYWH(x, y, width, height, Colors.RADIUS_LARGE);

        try (Paint shadow = new Paint().setColor(Colors.SHADOW).setMaskFilter(MaskFilter.makeBlur(FilterBlurMode.NORMAL, 18f));
             Paint surface = new Paint().setColor(Colors.SURFACE);
             Paint border = new Paint().setColor(Colors.GLASS_BORDER).setMode(PaintMode.STROKE).setStrokeWidth(1f)) {
            canvas.drawRRect(RRect.makeXYWH(x + 3, y + 6, width, height, Colors.RADIUS_LARGE), shadow);
            canvas.drawRRect(shell, surface);
            canvas.drawRRect(shell, border);
        }

        try (Paint rail = new Paint().setColor(Colors.SURFACE_RECESSED);
             Paint divider = new Paint().setColor(0x28796F60)) {
            canvas.save();
            canvas.clipRRect(shell, true);
            canvas.drawRect(Rect.makeXYWH(x, y, railWidth, height), rail);
            canvas.drawRect(Rect.makeXYWH(x + railWidth, y, 1f, height), divider);
            canvas.restore();
        }

        drawBrand(canvas, x + 24, y + 24);
        float buttonX = x + 18;
        float buttonWidth = railWidth - 36;
        float buttonY = y + Math.min(105f, height * 0.28f);
        float availableButtonHeight = y + height - 24f - buttonY;
        float buttonStep = Math.min(56f, availableButtonHeight / actions.size());
        float buttonHeight = Math.max(32f, buttonStep - 7f);
        for (int index = 0; index < actions.size(); index++) {
            TitleAction action = actions.get(index);
            action.bounds = Rect.makeXYWH(buttonX, buttonY + index * buttonStep, buttonWidth, buttonHeight);
            drawAction(canvas, action);
        }

        float contentX = x + railWidth + Math.max(38f, width * 0.06f);
        float contentY = y + height * 0.24f;
        try (Paint kicker = new Paint().setColor(Colors.ACCENT);
             Paint primary = new Paint().setColor(Colors.TEXT_PRIMARY);
             Paint muted = new Paint().setColor(Colors.TEXT_MUTED)) {
            canvas.drawString("MIOHR CLIENT / 1.21.11", contentX, contentY, FontManager.INSTANCE.getTextFont(12f), kicker);
            canvas.drawString("MioHr", contentX, contentY + 74f, FontManager.INSTANCE.getTextFont(Math.min(68f, width * 0.075f)), primary);
            canvas.drawString("A quieter way to play Minecraft.", contentX, contentY + 112f, FontManager.INSTANCE.getTextFont(16f), muted);
            canvas.drawString("Choose an activity from the menu to begin.", contentX, contentY + 137f, FontManager.INSTANCE.getTextFont(12f), muted);
        }

        float infoY = y + height - 54f;
        try (Paint line = new Paint().setColor(0x28796F60);
             Paint info = new Paint().setColor(Colors.TEXT_MUTED)) {
            canvas.drawRect(Rect.makeXYWH(contentX, infoY - 15f, width - railWidth - (contentX - x - railWidth) - 28f, 1f), line);
            canvas.drawString("FABRIC EDITION", contentX, infoY + 8f, FontManager.INSTANCE.getTextFont(10f), info);
            canvas.drawString("BUILD 2026.08", x + width - 105f, infoY + 8f, FontManager.INSTANCE.getTextFont(10f), info);
        }
    }

    private void drawBrand(Canvas canvas, float x, float y) {
        try (Paint mark = new Paint().setColor(Colors.ACCENT);
             Paint onAccent = new Paint().setColor(Colors.ON_ACCENT);
             Paint primary = new Paint().setColor(Colors.TEXT_PRIMARY);
             Paint muted = new Paint().setColor(Colors.TEXT_MUTED)) {
            canvas.drawRRect(RRect.makeXYWH(x, y, 36, 36, 10), mark);
            canvas.drawString("M", x + 10, y + 25, FontManager.INSTANCE.getTextFont(18f), onAccent);
            canvas.drawString("MIOHR CLIENT", x + 48, y + 15, FontManager.INSTANCE.getTextFont(14f), primary);
            canvas.drawString("MAIN MENU", x + 48, y + 31, FontManager.INSTANCE.getTextFont(9f), muted);
        }
    }

    private void drawAction(Canvas canvas, TitleAction action) {
        boolean hovered = action.bounds.contains(mouseX, mouseY);
        action.hover += ((hovered ? 1f : 0f) - action.hover) * 0.18f;
        int background = Color.makeLerp(Colors.SURFACE_SOFT, Colors.ACCENT, action.hover);
        int foreground = Color.makeLerp(Colors.TEXT_PRIMARY, Colors.ON_ACCENT, action.hover);
        float centerY = action.bounds.getTop() + action.bounds.getHeight() / 2f;
        try (Paint surface = new Paint().setColor(background);
             Paint text = new Paint().setColor(foreground)) {
            canvas.drawRRect(RRect.makeXYWH(action.bounds.getLeft(), action.bounds.getTop(), action.bounds.getWidth(), action.bounds.getHeight(), Colors.RADIUS_MEDIUM), surface);
            canvas.drawString(action.icon, action.bounds.getLeft() + 13, centerY + 6f, FontManager.INSTANCE.getIconFont(17f), text);
            if (action.bounds.getHeight() >= 40f) {
                canvas.drawString(action.label, action.bounds.getLeft() + 40, centerY - 2f, FontManager.INSTANCE.getTextFont(13f), text);
                text.setAlpha(hovered ? 210 : 150);
                canvas.drawString(action.detail, action.bounds.getLeft() + 40, centerY + 13f, FontManager.INSTANCE.getTextFont(9f), text);
            } else {
                canvas.drawString(action.label, action.bounds.getLeft() + 40, centerY + 5f, FontManager.INSTANCE.getTextFont(12f), text);
            }
        }
    }

    private static final class TitleAction {
        private final String label;
        private final String detail;
        private final String icon;
        private final Runnable action;
        private Rect bounds;
        private float hover;

        private TitleAction(String label, String detail, String icon, Runnable action) {
            this.label = label;
            this.detail = detail;
            this.icon = icon;
            this.action = action;
        }
    }
}
