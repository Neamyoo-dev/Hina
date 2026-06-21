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

import com.hinaclient.hina.MioHr;
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

import java.util.ArrayList;
import java.util.List;

public class MioHrTitleScreen extends Screen {
    private final List<HinaTitleButton> buttons = new ArrayList<>();

    private int mx, my;
    private float delta;
    private static Image Image = null;
    private boolean nrb = true;

    public MioHrTitleScreen() {
        super(Component.literal("MioHr TitleScreen"));
        loadImage();
    }

    @Override
    public void init() {
        super.init();
        nrb = true;
        if (!EventBus.INSTANCE.isregister(this)) {
            EventBus.INSTANCE.register(this);
        }
    }

    @Override
    public void removed() {
        EventBus.INSTANCE.unregister(this);
        super.removed();
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        mx = mouseX;
        my = mouseY;
        this.delta = delta;
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean bl) {
        for (HinaTitleButton btn : buttons) {
            if (btn.isHovered((int) event.x(), (int) event.y())) {
                btn.onClick();
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

        float centerX = screenWidth / 2f;
        float centerY = screenHeight / 2f;

        drawbg(canvas);

        canvas.save();
        canvas.scale(mcScale, mcScale);

        try (TextLine line = TextLine.make("Hina Client", FontManager.INSTANCE.getTextFont(30));
             Paint paint = new Paint()) {
            paint.setARGB(255, 240, 210, 244);
            canvas.drawTextLine(line, centerX - 76, centerY - 200, paint);
        }

        float x = centerX - 160;
        float y = centerY - 178;
        float w = 325;
        float h = 325;
        float radius = 15;
        float footerHeight = 75;

        try (var paint = new Paint()) {
            paint.setAntiAlias(true);
            RRect mainBox = RRect.makeXYWH(x, y, w, h, radius);

            paint.setARGB(200, 255, 253, 246);
            canvas.drawRRect(mainBox, paint);

            canvas.save();
            canvas.clipRRect(mainBox, true);
            paint.setARGB(255, 240, 210, 244);
            canvas.drawRect(Rect.makeXYWH(x, y + h - footerHeight, w, footerHeight), paint);
            paint.setARGB(40, 0, 0, 0);
            canvas.drawLine(x, y + h - footerHeight, x + w, y + h - footerHeight, paint);
            canvas.restore();

            drawknowtext(canvas, paint, x, y);

            try (var line = TextLine.make("Minecraft 1.21.11", FontManager.INSTANCE.getTextFont(14))) {
                paint.setARGB(255, 255, 255, 255);
                float tx = x + (w - line.getWidth()) / 2f;
                float ty = (y + h - footerHeight / 2f) + (line.getHeight() / 2f);
                canvas.drawTextLine(line, tx, ty, paint);
            }
        }

        for (HinaTitleButton btn : buttons) {
            btn.render(canvas, mx, my, delta);
        }

        if (nrb || buttons.isEmpty()) {
            addButtons(centerX, centerY);
        }

        canvas.restore();
    }

    private void loadImage() {
        if (Image == null) {
            try {
                var is = Minecraft.getInstance().getResourceManager()
                        .getResource(Identifier.fromNamespaceAndPath("miohr", "textures/gui/title/bg.png"))
                        .get().open();

                byte[] bytes = is.readAllBytes();
                Image = io.github.humbleui.skija.Image.makeDeferredFromEncodedBytes(bytes);
            } catch (Exception e) {
                MioHr.getINSTANCE().getLogger().error("Failed to load HINA image", e);
            }
        }
    }

    private void drawbg(Canvas canvas) {
        int screenWidth = this.minecraft.getWindow().getWidth();
        int screenHeight = this.minecraft.getWindow().getHeight();

        if (Image != null) {
            canvas.save();

            canvas.drawImageRect(
                    Image,
                    Rect.makeXYWH(0, 0, screenWidth, screenHeight)
            );

            canvas.restore();
        }
    }

    private void drawknowtext(Canvas canvas, Paint paint, float x, float y) {
        float rightOffset = 165f;

        paint.setARGB(255, 155, 120, 230);
        try (var title = TextLine.make("你知道吗?", FontManager.INSTANCE.getTextFont(18))) {
            canvas.drawTextLine(title, x + rightOffset, y + 45, paint);
        }

        paint.setARGB(255, 100, 100, 100);

        try (var content = TextLine.make("所有外挂都打的过此外挂", FontManager.INSTANCE.getTextFont(10))) {
            canvas.drawTextLine(content, x + rightOffset, y + 75, paint);

            try (var content1 = TextLine.make("包括zen", FontManager.INSTANCE.getTextFont(10))) {
                canvas.drawTextLine(content1, x + rightOffset, y + 80 + content.getHeight(), paint);
            }
        }
    }

    private void addButtons(float centerX, float centerY) {
        buttons.clear();

        float btnX = centerX - 150;
        float btnW = 140;
        float btnH = 40;
        float startY = centerY - 160;

        buttons.add(new HinaTitleButton("Single Player", Icon.PEOPLE, btnX, startY, btnW, btnH, () -> {
            this.minecraft.setScreen(new SelectWorldScreen(this));
        }));
        buttons.add(new HinaTitleButton("Multi Player", Icon.LAN, btnX, startY + 45, btnW, btnH, () -> {
            this.minecraft.setScreen(new JoinMultiplayerScreen(this));
        }));
        buttons.add(new HinaTitleButton("Alt Manager", Icon.MANAGE_ACCOUNTS, btnX, startY + 90, btnW, btnH, () -> {
            this.minecraft.setScreen(new AccountScreen(this));
        }));
        buttons.add(new HinaTitleButton("Options", Icon.SETTINGS, btnX, startY + 135, btnW, btnH, () -> {
            this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
        }));
        buttons.add(new HinaTitleButton("Shut Down", Icon.POWER_SETTINGS_NEW, btnX, startY + 180, btnW, btnH, this.minecraft::stop));

        nrb = false;
    }

    public static class HinaTitleButton {
        private final String text;
        private final String icon;
        private final Runnable action;
        private final float x, y, width, height;
        private float hoverLerp = 0f;

        public HinaTitleButton(String text, String icon, float x, float y, float width, float height, Runnable action) {
            this.text = text;
            this.icon = icon;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.action = action;
        }

        public void render(Canvas canvas, int mouseX, int mouseY, float delta) {
            boolean hovered = isHovered(mouseX, mouseY);
            hoverLerp = Math.max(0, Math.min(1, hoverLerp + (hovered ? 0.2f : -0.2f) * delta));

            canvas.save();

            float offsetX = 5f * hoverLerp;
            canvas.translate(offsetX, 0);

            try (var paint = new Paint()) {
                paint.setAntiAlias(true);

                paint.setARGB((int) (255 * (0.3f + 0.7f * hoverLerp)), 240, 210, 244);
                float lineW = 3f;
                canvas.drawRect(Rect.makeXYWH(x, y + 5, lineW, height - 10), paint);

                if (hoverLerp > 0) {
                    paint.setARGB((int) (120 * hoverLerp), 240, 210, 244);
                    paint.setMaskFilter(MaskFilter.makeBlur(FilterBlurMode.NORMAL, 3f));
                    canvas.drawRect(Rect.makeXYWH(x, y + 5, lineW, height - 10), paint);
                    paint.setMaskFilter(null);
                }
            }

            float padding = 12f;
            float currentX = x + padding;

            try (var paint = new Paint()) {
                paint.setAntiAlias(true);
                int textAlpha = (int) (180 + (75 * hoverLerp));
                paint.setARGB(textAlpha, 80, 80, 80);

                if (icon != null && !icon.isEmpty()) {
                    try (var iconLine = TextLine.make(icon, FontManager.INSTANCE.getIconFont(18))) {
                        canvas.drawTextLine(iconLine, currentX, y + (height + iconLine.getCapHeight()) / 2f + 1, paint);
                        currentX += iconLine.getWidth() + 8f;
                    }
                }

                try (var textLine = TextLine.make(text, FontManager.INSTANCE.getTextFont(16))) {
                    canvas.drawTextLine(textLine, currentX, y + (height + textLine.getCapHeight()) / 2f, paint);
                }
            }

            canvas.restore();
        }

        public boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        public void onClick() {
            Minecraft.getInstance().execute(action);
        }
    }
}
