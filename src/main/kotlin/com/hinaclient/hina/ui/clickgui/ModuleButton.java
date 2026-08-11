package com.hinaclient.hina.ui.clickgui;

import com.hinaclient.hina.module.Module;
import com.hinaclient.hina.setting.*;
import com.hinaclient.hina.skia.font.FontManager;
import com.hinaclient.hina.skia.font.Icon;
import com.hinaclient.hina.ui.AnimationUtils;
import com.hinaclient.hina.ui.Colors;
import com.hinaclient.hina.ui.clickgui.setting.*;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ModuleButton {
    private final Module module;
    private final float height;
    private final List<Component> components = new ArrayList<>();
    private boolean expanded;
    private float enableProgress;
    private float expandProgress;
    private float hoverProgress;
    private float renderedWidth;

    public ModuleButton(Module module, float height) {
        this.module = module;
        this.height = height;
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof BooleanSetting booleanSetting) components.add(new CheckboxComponent(booleanSetting, 300, ClickGuiMetrics.SETTING_HEIGHT));
            else if (setting instanceof NumberSetting numberSetting) components.add(new SliderComponent(numberSetting, 300, ClickGuiMetrics.SETTING_HEIGHT));
            else if (setting instanceof ModeSetting modeSetting) components.add(new ModeComponent(modeSetting, 300, ClickGuiMetrics.SETTING_HEIGHT));
            else if (setting instanceof ColorSetting colorSetting) components.add(new ColorComponent(colorSetting, 300, ClickGuiMetrics.COLOR_HEIGHT));
        }
        components.add(new BindComponent(module, 300, ClickGuiMetrics.SETTING_HEIGHT));
    }

    public Module getModule() {
        return module;
    }

    public void update() {
        enableProgress = AnimationUtils.lerp(enableProgress, module.isEnabled() ? 1f : 0f, 0.2f);
        expandProgress = AnimationUtils.lerp(expandProgress, expanded ? 1f : 0f, 0.2f);
        components.forEach(Component::update);
    }

    public void render(Canvas canvas, float x, float y, int mouseX, int mouseY, float width) {
        renderedWidth = width;
        boolean hovered = contains(mouseX, mouseY, x, y, width, height);
        hoverProgress = AnimationUtils.lerp(hoverProgress, hovered ? 1f : 0f, 0.25f);
        float settingsHeight = settingsHeight();
        float visibleSettings = settingsHeight * expandProgress;
        float totalHeight = height + visibleSettings;
        int borderColor = Color.makeLerp(Colors.GLASS_BORDER, Colors.GLASS_BORDER_ACTIVE, Math.max(hoverProgress, expandProgress));

        try (Paint surface = new Paint().setColor(Colors.GLASS_ITEM_BG);
             Paint border = new Paint().setColor(borderColor).setMode(PaintMode.STROKE).setStrokeWidth(1f)) {
            RRect card = RRect.makeXYWH(x, y, width, totalHeight, 9f);
            canvas.drawRRect(card, surface);
            canvas.drawRRect(card, border);
        }
        if (hoverProgress > 0.01f) {
            try (Paint highlight = new Paint().setColor(Color.makeLerp(0x00FFFFFF, 0x26FFFFFF, hoverProgress))) {
                canvas.drawRRect(RRect.makeXYWH(x + 1f, y + 1f, width - 2f, height - 2f, 8f), highlight);
            }
        }

        float iconX = x + 10f;
        float iconY = y + (height - 25f) / 2f;
        int iconBg = Color.makeLerp(0x4DF2F7EB, Colors.ACCENT, Math.max(enableProgress, expandProgress));
        int iconText = Color.makeLerp(Colors.ACCENT, Colors.ON_ACCENT, Math.max(enableProgress, expandProgress));
        try (Paint background = new Paint().setColor(iconBg);
             Paint text = new Paint().setColor(iconText)) {
            canvas.drawRRect(RRect.makeXYWH(iconX, iconY, 25f, 25f, 7f), background);
            String initials = initials(module.getName());
            float initialsWidth = FontManager.INSTANCE.getTextFont(9f).measureTextWidth(initials, text);
            canvas.drawString(initials, iconX + (25f - initialsWidth) / 2f, iconY + 17f, FontManager.INSTANCE.getTextFont(9f), text);
        }

        float copyX = iconX + 34f;
        try (Paint primary = new Paint().setColor(Colors.TEXT_PRIMARY);
             Paint muted = new Paint().setColor(Colors.TEXT_MUTED)) {
            canvas.drawString(module.getName(), copyX, y + 20f, FontManager.INSTANCE.getTextFont(10f), primary);
            String meta = module.getCategory().getName() + " · " + module.getSettings().size() + " settings";
            canvas.drawString(meta, copyX, y + 33f, FontManager.INSTANCE.getTextFont(7f), muted);
        }

        float switchW = 29f;
        float switchH = 17f;
        float switchX = x + width - 55f;
        float switchY = y + (height - switchH) / 2f;
        try (Paint track = new Paint().setColor(Color.makeLerp(Colors.SWITCH_TRACK_OFF, Colors.ACCENT, enableProgress));
             Paint knob = new Paint().setColor(Colors.ON_ACCENT)) {
            canvas.drawRRect(RRect.makeXYWH(switchX, switchY, switchW, switchH, switchH / 2f), track);
            float knobSize = 13f;
            float knobX = switchX + 2f + (switchW - knobSize - 4f) * enableProgress;
            canvas.drawCircle(knobX + knobSize / 2f, switchY + switchH / 2f, knobSize / 2f, knob);
        }
        try (Paint chevron = new Paint().setColor(expanded ? Colors.ACCENT : Colors.TEXT_MUTED)) {
            String glyph = expanded ? Icon.KEYBOARD_ARROW_UP : Icon.KEYBOARD_ARROW_DOWN;
            canvas.drawString(glyph, x + width - 19f, y + 28f, FontManager.INSTANCE.getIconFont(11f), chevron);
        }

        if (visibleSettings > 0.5f) {
            float settingsY = y + height;
            canvas.save();
            canvas.clipRect(Rect.makeXYWH(x, settingsY, width, visibleSettings));
            try (Paint divider = new Paint().setColor(0x28796F60);
                 Paint wash = new Paint().setColor(0x24FFF9F0)) {
                canvas.drawRect(Rect.makeXYWH(x, settingsY, width, 1f), divider);
                canvas.drawRect(Rect.makeXYWH(x, settingsY + 1f, width, settingsHeight), wash);
            }
            float componentY = settingsY + 6f;
            for (Component component : components) {
                component.setWidth(width - 20f);
                component.render(canvas, x + 10f, componentY, mouseX, mouseY);
                componentY += component.getHeight() + 7f;
            }
            canvas.restore();
        }
    }

    public float getTotalHeight() {
        return height + settingsHeight() * expandProgress;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, float x, float y) {
        if (contains(mouseX, mouseY, x, y, renderedWidth, height)) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT
                    && mouseX >= x + renderedWidth - 57f && mouseX <= x + renderedWidth - 24f
                    && mouseY >= y + (height - 21f) / 2f && mouseY <= y + (height + 21f) / 2f) {
                module.toggle();
                return true;
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                expanded = !expanded;
                return true;
            }
        }
        if (expanded && expandProgress > 0.8f) {
            for (Component component : components) if (component.mouseClicked(mouseX, mouseY, button)) return true;
        }
        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button, float x, float y) {
        if (expanded) components.forEach(component -> component.mouseReleased(mouseX, mouseY, button));
    }

    public boolean handleKeyPress(int keyCode) {
        if (!expanded) return false;
        for (Component component : components) {
            if (component instanceof BindComponent bind && bind.onKeyPressed(keyCode)) return true;
        }
        return false;
    }

    private float settingsHeight() {
        float result = 12f;
        for (Component component : components) result += component.getHeight() + 7f;
        return result;
    }

    private static String initials(String name) {
        StringBuilder result = new StringBuilder(2);
        for (int index = 0; index < name.length() && result.length() < 2; index++) {
            char character = name.charAt(index);
            if (Character.isUpperCase(character) || result.isEmpty() && Character.isLetterOrDigit(character)) result.append(Character.toUpperCase(character));
        }
        return result.isEmpty() ? "M" : result.toString();
    }

    private static boolean contains(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
