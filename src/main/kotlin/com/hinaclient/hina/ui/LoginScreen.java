package com.hinaclient.hina.ui;

import com.hinaclient.hina.event.EventBus;
import com.hinaclient.hina.event.EventListener;
import com.hinaclient.hina.event.skia.EventSkiaDrawScene;
import com.hinaclient.hina.skia.font.FontManager;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.io.InputStream;
import java.util.Random;

public class LoginScreen extends Screen {
    private final Screen nextscreen;
    private String username = "";
    private boolean isFocused = false;
    private Image backgroundImage;

    private long initTime;
    private long closingTime = 0;
    private boolean isClosing = false;

    private float hoverAlpha = 0f;
    private float textAnim = 0f;
    private final Random random = new Random();
    private Boolean login = false;

    private final int COLOR_HINA_PURPLE = 0xFFD1C4E9;
    private final int COLOR_GEHENNA_BLUE = 0xFF5C6BC0;
    private final int COLOR_TEXT_MAIN = 0xFF2D2D2D;
    private final int COLOR_CARD_BG = 0xCCFFFFFF;

    public LoginScreen(Screen parent) {
        super(Component.literal("login"));
        this.nextscreen = parent;
    }

    @Override
    protected void init() {
        if (login) { this.minecraft.setScreen(nextscreen); return; }
        EventBus.INSTANCE.register(this);
        initTime = System.currentTimeMillis();
        isClosing = false;
        if (!FontManager.INSTANCE.isInitialized()) FontManager.INSTANCE.init();
        if (backgroundImage == null) loadBackgroundImage();
    }

    private void loadBackgroundImage() {
        try {
            var resource = this.minecraft.getResourceManager().getResource(Identifier.fromNamespaceAndPath("miohr", "textures/gui/title/bg.png"));
            if (resource.isPresent()) {
                try (InputStream stream = resource.get().open()) {
                    this.backgroundImage = Image.makeDeferredFromEncodedBytes(stream.readAllBytes());
                }
            }
        } catch (Exception ignored) {}
    }

    private int getAnimatedColor(int color, float alpha) {
        int a = (int) (((color >> 24) & 0xFF) * alpha);
        return (color & 0x00FFFFFF) | (a << 24);
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    @EventListener
    public void onSkiaRender(@NotNull EventSkiaDrawScene event) {
        if (login) {
            this.minecraft.setScreen(nextscreen);
            EventBus.INSTANCE.unregister(this);
            return;
        }

        var canvas = event.getCanvas();
        long now = System.currentTimeMillis();

        float alpha = Math.min(1.0f, (now - initTime) / 500f);
        if (isClosing) {
            alpha = Math.max(0.0f, 1.0f - (now - closingTime) / 500f);
            if (alpha <= 0.0f) { this.minecraft.setScreen(nextscreen); return; }
        }

        int mcScale = this.minecraft.getWindow().getGuiScale();
        float sw = (float) this.minecraft.getWindow().getGuiScaledWidth();
        float sh = (float) this.minecraft.getWindow().getGuiScaledHeight();

        if (backgroundImage != null) {
            try (var p = new Paint().setAlpha((int)(255 * alpha))) {
                canvas.drawImageRect(backgroundImage, Rect.makeWH(sw * mcScale, sh * mcScale), p);
            }
        }

        try (var blur = ImageFilter.makeBlur(15 * alpha, 15 * alpha, FilterTileMode.CLAMP);
             var p = new Paint().setImageFilter(blur)) {
            canvas.saveLayer(Rect.makeWH(sw * mcScale, sh * mcScale), p);
            canvas.restore();
        }

        try (var p = new Paint().setColor(getAnimatedColor(0x50311B52, alpha))) {
            canvas.drawRect(Rect.makeWH(sw * mcScale, sh * mcScale), p);
        }

        canvas.save();
        canvas.scale(mcScale, mcScale);

        float boxW = 300, boxH = 380;
        float x = (sw - boxW) / 2f, y = (sh - boxH) / 2f;

        try (var s = new Paint().setColor(getAnimatedColor(0x40000000, alpha)).setMaskFilter(MaskFilter.makeBlur(FilterBlurMode.NORMAL, 12))) {
            canvas.drawRRect(RRect.makeXYWH(x + 4, y + 4, boxW, boxH, 20), s);
        }

        try (var p = new Paint().setColor(getAnimatedColor(COLOR_CARD_BG, alpha))) {
            canvas.drawRRect(RRect.makeXYWH(x, y, boxW, boxH, 20), p);
        }

        try (var p = new Paint().setColor(getAnimatedColor(COLOR_GEHENNA_BLUE, alpha))) {
            canvas.drawString("Hina Client", x + 35, y + 70, FontManager.INSTANCE.getTextFont(28f), p);
            p.setColor(getAnimatedColor(0xFF757575, alpha));
            canvas.drawString("Welcome to Hina Client! Enjoy your game", x + 35, y + 90, FontManager.INSTANCE.getTextFont(12f), p);
        }

        float inputY = y + 140;
        textAnim = Math.max(0, textAnim - 0.15f);
        try (var p = new Paint()) {
            p.setColor(getAnimatedColor(isFocused ? 0xFFFDFDFD : 0xFFF1F3F5, alpha));
            canvas.drawRRect(RRect.makeXYWH(x + 35, inputY, 230, 45, 10), p);
            if (isFocused) {
                p.setMode(PaintMode.STROKE).setStrokeWidth(2f).setColor(getAnimatedColor(COLOR_HINA_PURPLE, alpha));
                canvas.drawRRect(RRect.makeXYWH(x + 35, inputY, 230, 45, 10), p);
            }
            p.setMode(PaintMode.FILL).setColor(getAnimatedColor(username.isEmpty() ? 0xFFADB5BD : COLOR_TEXT_MAIN, alpha));
            String txt = username.isEmpty() ? "Input Username" : username;
            float offsetX = textAnim * 4f;
            canvas.drawString(txt, x + 50 + offsetX, inputY + 28, FontManager.INSTANCE.getTextFont(16f), p);

            if (isFocused && (now / 500) % 2 == 0) {
                float tw = FontManager.INSTANCE.getTextFont(16f).measureTextWidth(txt);
                canvas.drawRect(Rect.makeXYWH(x + 52 + tw + offsetX, inputY + 12, 1.5f, 20), p);
            }
        }

        float btnY = y + 215;
        boolean hover = isHovering(x + 35, btnY, 230, 50, sw, sh);
        hoverAlpha = hover ? Math.min(1f, hoverAlpha + 0.15f) : Math.max(0f, hoverAlpha - 0.15f);

        try (var p = new Paint()) {
            p.setColor(getAnimatedColor(COLOR_GEHENNA_BLUE, alpha));
            canvas.drawRRect(RRect.makeXYWH(x + 35, btnY, 230, 50, 10), p);
            if (hoverAlpha > 0) {
                p.setMode(PaintMode.STROKE).setStrokeWidth(2.5f * hoverAlpha).setColor(getAnimatedColor(COLOR_HINA_PURPLE, alpha * hoverAlpha));
                p.setMaskFilter(MaskFilter.makeBlur(FilterBlurMode.NORMAL, 4));
                canvas.drawRRect(RRect.makeXYWH(x + 35, btnY, 230, 50, 10), p);
            }
            p.setMode(PaintMode.FILL).setMaskFilter(null).setColor(getAnimatedColor(0xFFFFFFFF, alpha));
            canvas.drawString("LOGIN", x + 120, btnY + 32, FontManager.INSTANCE.getTextFont(18f), p);
        }

        float skipY = y + 310;
        try (var p = new Paint().setColor(getAnimatedColor(isHovering(x + 110, skipY, 80, 20, sw, sh) ? COLOR_GEHENNA_BLUE : 0xFF9E9E9E, alpha))) {
            canvas.drawString("Skip Entry >", x + 110, skipY + 15, FontManager.INSTANCE.getTextFont(14f), p);
        }
        canvas.restore();
    }

    @Override
    public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean bl) {
        if (isClosing) return false;
        float sw = (float) this.minecraft.getWindow().getGuiScaledWidth(), sh = (float) this.minecraft.getWindow().getGuiScaledHeight();
        float x = (sw - 300) / 2f, y = (sh - 380) / 2f;
        isFocused = isHovering(x + 35, y + 140, 230, 45, sw, sh);
        if (isHovering(x + 35, y + 215, 230, 50, sw, sh) && !username.isEmpty()) startClosing();
        if (isHovering(x + 110, y + 310, 80, 20, sw, sh)) {
            username = "Skip_User" + (100 + random.nextInt(900));
            startClosing();
        }
        return super.mouseClicked(event, bl);
    }

    private void startClosing() {
        login = true;
        isClosing = true;
        closingTime = System.currentTimeMillis();
    }

    @Override
    public boolean charTyped(@NotNull CharacterEvent event) {
        if (!isClosing && isFocused && username.length() < 16) {
            username += event.codepointAsString();
            textAnim = 1.0f;
            return true;
        }
        return super.charTyped(event);
    }

    @Override
    public boolean keyPressed(@NotNull KeyEvent event) {
        if (!isClosing && isFocused && event.key() == GLFW.GLFW_KEY_BACKSPACE && !username.isEmpty()) {
            username = username.substring(0, username.length() - 1);
            textAnim = 1.0f;
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_ENTER && !username.isEmpty()) startClosing();
        return super.keyPressed(event);
    }

    private boolean isHovering(float x, float y, float w, float h, float sw, float sh) {
        double mx = this.minecraft.mouseHandler.xpos() * (double) sw / (double) this.minecraft.getWindow().getScreenWidth();
        double my = this.minecraft.mouseHandler.ypos() * (double) sh / (double) this.minecraft.getWindow().getScreenHeight();
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public void removed() { EventBus.INSTANCE.unregister(this); super.removed(); }

    public Boolean isLogin() {
        return login;
    }
}