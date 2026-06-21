package com.hinaclient.hina.ui.clickgui;

import com.hinaclient.hina.module.Module;
import com.hinaclient.hina.setting.*;
import com.hinaclient.hina.skia.SkiaRenderer;
import com.hinaclient.hina.skia.font.FontManager;
import com.hinaclient.hina.skia.font.Icon;
import com.hinaclient.hina.ui.AnimationUtils;
import com.hinaclient.hina.ui.Colors;
import com.hinaclient.hina.ui.clickgui.setting.*;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import java.util.ArrayList;
import java.util.List;

public class ModuleButton {
    private final Module module;
    private final float height;
    private final List<Component> components = new ArrayList<>();
    private boolean extended = false;
    private float enableProgress = 0f;
    private float extensionProgress = 0f;
    private float hoverAlpha = 0f;
    private float renderedWidth = 0;
    private final float SETTING_HEIGHT = 32;
    private final float COLOR_HEIGHT = 120;

    public ModuleButton(Module module, float height) {
        this.module = module;
        this.height = height;
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof BooleanSetting)
                components.add(new CheckboxComponent((BooleanSetting) setting, 300, SETTING_HEIGHT));
            else if (setting instanceof NumberSetting)
                components.add(new SliderComponent((NumberSetting) setting, 300, SETTING_HEIGHT));
            else if (setting instanceof ModeSetting)
                components.add(new ModeComponent((ModeSetting) setting, 300, SETTING_HEIGHT));
            else if (setting instanceof ColorSetting)
                components.add(new ColorComponent((ColorSetting) setting, 300, COLOR_HEIGHT));
        }
        components.add(new BindComponent(module, 300, SETTING_HEIGHT));
    }

    public Module getModule() {
        return module;
    }

    public void update() {
        enableProgress = AnimationUtils.lerp(enableProgress, module.isEnabled() ? 1.0f : 0.0f, 0.2f);
        if (AnimationUtils.approx(enableProgress, module.isEnabled() ? 1.0f : 0.0f, 0.001f))
            enableProgress = module.isEnabled() ? 1.0f : 0.0f;

        extensionProgress = AnimationUtils.lerp(extensionProgress, extended ? 1.0f : 0.0f, 0.2f);
        if (AnimationUtils.approx(extensionProgress, extended ? 1.0f : 0.0f, 0.001f))
            extensionProgress = extended ? 1.0f : 0.0f;

        for (Component comp : components) comp.update();
    }

    public void render(Canvas canvas, float x, float y, int mouseX, int mouseY, float width) {
        this.renderedWidth = width;
        boolean hover = isHovered(mouseX, mouseY, x, y, width, height);
        hoverAlpha = AnimationUtils.lerp(hoverAlpha, hover ? 1.0f : 0f, 0.3f);

        try (Paint bg = new Paint()) {
            bg.setColor(enableProgress > 0.01f ? Colors.GLASS_ITEM_ACTIVE : Colors.GLASS_ITEM_BG);
            canvas.drawRRect(RRect.makeXYWH(x, y, width, height, 10), bg);
        }
        if (hoverAlpha > 0.01f) {
            try (Paint hoverPaint = new Paint()) {
                hoverPaint.setColor(Colors.GLASS_ITEM_HOVER);
                canvas.drawRRect(RRect.makeXYWH(x, y, width, height, 10), hoverPaint);
            }
        }

        if (enableProgress > 0.01f) {
            try (Paint accent = new Paint()) {
                accent.setColor(Colors.getThemeWithAlpha(0x60));
                float fillW = width * enableProgress;
                canvas.drawRRect(RRect.makeXYWH(x, y, fillW, height, 10), accent);
            }
            try (Paint accentLine = new Paint()) {
                accentLine.setColor(Colors.getThemeColor());
                canvas.drawRRect(RRect.makeXYWH(x, y + 3, Math.max(3f, width * enableProgress), height - 6, 1.5f), accentLine);
            }
        }

        try (Paint textPaint = new Paint().setColor(module.isEnabled() ? Colors.TEXT_PRIMARY : Colors.TEXT_SECONDARY)) {
            Font font = FontManager.INSTANCE.getTextFont(13);
            FontMetrics metrics = font.getMetrics();
            float textY = y + height / 2 - (metrics.getAscent() + metrics.getDescent()) / 2;
            canvas.drawString(module.getName(), x + 14, textY, font, textPaint);
        }

        String keyName = module.getKey() == -1 ? null : org.lwjgl.glfw.GLFW.glfwGetKeyName(module.getKey(), 0);
        if (keyName != null) {
            try (Paint keyPaint = new Paint().setColor(Colors.TEXT_MUTED)) {
                Font font = FontManager.INSTANCE.getTextFont(10);
                float kw = font.measureTextWidth(keyName, keyPaint);
                canvas.drawString(keyName, x + width - kw - 28, y + height / 2 + 3, font, keyPaint);
            }
        }

        SkiaRenderer.drawCenteredIcon(canvas, Icon.SETTINGS, x + width - 14, y + height / 2, 11,
                extended ? Colors.TEXT_PRIMARY : Colors.TEXT_MUTED);

        if (extensionProgress > 0.01f) {
            float yOff = y + height;
            canvas.save();
            float totalSettingsHeight = 0;
            for (Component c : components) totalSettingsHeight += c.getHeight();
            canvas.clipRect(Rect.makeXYWH(x, y + height, width, totalSettingsHeight * extensionProgress));
            for (Component comp : components) {
                comp.setWidth(width);
                comp.render(canvas, x, yOff, mouseX, mouseY);
                yOff += comp.getHeight();
            }
            canvas.restore();
        }
    }

    public float getTotalHeight() {
        float h = height;
        if (extensionProgress > 0.01f) {
            float settingsHeight = 0;
            for (Component comp : components) settingsHeight += comp.getHeight();
            h = height + settingsHeight * extensionProgress;
        }
        return h;
    }

    public boolean mouseClicked(double mx, double my, int btn, float x, float y) {
        if (isHovered(mx, my, x, y, renderedWidth, height)) {
            if (mx >= x + renderedWidth - 28 && mx <= x + renderedWidth) {
                extended = !extended;
                return true;
            }
            if (btn == 1) {
                extended = !extended;
                return true;
            }
            if (btn == 0) {
                module.toggle();
                return true;
            }
        }
        if (extended) {
            float yOff = y + height;
            for (Component comp : components) {
                if (comp.mouseClicked(mx, my, btn)) return true;
                yOff += comp.getHeight();
            }
        }
        return false;
    }

    public void mouseReleased(double mx, double my, int btn, float x, float y) {
        if (extended) {
            float yOff = y + height;
            for (Component comp : components) {
                comp.mouseReleased(mx, my, btn);
                yOff += comp.getHeight();
            }
        }
    }

    public boolean handleKeyPress(int keyCode) {
        if (extended) {
            for (Component comp : components) {
                if (comp instanceof BindComponent bind && bind.onKeyPressed(keyCode))
                    return true;
            }
        }
        return false;
    }

    private boolean isHovered(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
