package com.hinaclient.hina.ui.clickgui.setting;

import com.hinaclient.hina.module.Module;
import com.hinaclient.hina.skia.font.FontManager;
import com.hinaclient.hina.ui.Colors;
import com.hinaclient.hina.ui.clickgui.Component;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.RRect;
import org.lwjgl.glfw.GLFW;

public class BindComponent extends Component {
    private final Module module;
    private boolean listening;
    private float currentX, currentY;

    public BindComponent(Module module, float width, float height) {
        super(null, width, height);
        this.module = module;
    }

    @Override
    public void render(Canvas canvas, float x, float y, int mouseX, int mouseY) {
        this.currentX = x;
        this.currentY = y;

        try (Paint bg = new Paint()) {
            bg.setColor(Colors.GLASS_ITEM_BG);
            canvas.drawRRect(RRect.makeXYWH(x, y, width, height, 8), bg);
        }

        boolean hover = isHovered(mouseX, mouseY, x, y);
        if (hover) {
            try (Paint hoverPaint = new Paint()) {
                hoverPaint.setColor(Colors.GLASS_ITEM_HOVER);
                canvas.drawRRect(RRect.makeXYWH(x, y, width, height, 8), hoverPaint);
            }
        }

        String key = module.getKey() == -1 ? null : GLFW.glfwGetKeyName(module.getKey(), 0);
        if (key == null && module.getKey() != -1) {
            key = "Key#" + module.getKey();
        }
        String text;
        if (listening) {
            text = "Press a key...";
        } else if (key != null) {
            text = "Keybind: " + key.toUpperCase();
        } else {
            text = "Keybind: None";
        }

        try (Paint textPaint = new Paint().setColor(listening ? Colors.getThemeColor() : Colors.TEXT_SECONDARY)) {
            Font font = FontManager.INSTANCE.getTextFont(13);
            FontMetrics metrics = font.getMetrics();
            float textY = y + height / 2 - (metrics.getAscent() + metrics.getDescent()) / 2;
            canvas.drawString(text, x + 14, textY, font, textPaint);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY, currentX, currentY) && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            listening = !listening;
            return true;
        }
        return false;
    }

    public boolean onKeyPressed(int key) {
        if (listening) {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                listening = false;
            } else if (key == GLFW.GLFW_KEY_BACKSPACE) {
                module.setKey(-1);
                listening = false;
            } else {
                module.setKey(key);
                listening = false;
            }
            return true;
        }
        return false;
    }
}
