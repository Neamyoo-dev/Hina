package com.hinaclient.hina.ui.clickgui.setting;

import com.hinaclient.hina.setting.ColorSetting;
import com.hinaclient.hina.skia.font.FontManager;
import com.hinaclient.hina.ui.Colors;
import com.hinaclient.hina.ui.clickgui.Component;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;

public class ColorComponent extends Component {
    private final ColorSetting colorSetting;
    private float currentX, currentY;
    private boolean draggingHue, draggingSatVal;
    private float hue, saturation, brightness;

    public ColorComponent(ColorSetting setting, float width, float height) {
        super(setting, width, height);
        this.colorSetting = setting;
        updateHSB();
    }

    private void updateHSB() {
        float[] hsb = RGBtoHSB(colorSetting.getColor());
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
    }

    private void updateColor() {
        colorSetting.setValue(HSBtoRGB(hue, saturation, brightness));
    }

    @Override
    public void render(Canvas canvas, float x, float y, int mouseX, int mouseY) {
        if (!setting.isVisible()) return;
        this.currentX = x;
        this.currentY = y;

        if (!draggingHue && !draggingSatVal) updateHSB();

        float pad = 10;
        float contentW = width - pad * 2;
        float hueH = 12;
        float pickerH = height - hueH - pad * 3 - 22;
        float pickerX = x + pad, pickerY = y + 22;
        float hueX = x + pad, hueY = pickerY + pickerH + pad;

        if (draggingHue) {
            float diff = Math.clamp((mouseX - hueX) / contentW, 0f, 1f);
            hue = diff;
            updateColor();
        } else if (draggingSatVal) {
            float diffX = Math.clamp((mouseX - pickerX) / contentW, 0f, 1f);
            float diffY = Math.clamp((mouseY - pickerY) / pickerH, 0f, 1f);
            saturation = diffX;
            brightness = 1f - diffY;
            updateColor();
        }

        try (Paint bg = new Paint()) {
            bg.setColor(Colors.GLASS_ITEM_BG);
            canvas.drawRRect(RRect.makeXYWH(x, y, width, height, 9), bg);
        }

        try (Paint border = new Paint()) {
            border.setMode(PaintMode.STROKE);
            border.setStrokeWidth(1f);
            border.setColor(Colors.GLASS_BORDER);
            canvas.drawRRect(RRect.makeXYWH(x, y, width, height, 9), border);
        }

        try (Paint text = new Paint().setColor(Colors.TEXT_SECONDARY)) {
            Font font = FontManager.INSTANCE.getTextFont(11);
            canvas.drawString(setting.getName(), x + pad, y + 16, font, text);
        }

        try (Paint satPaint = new Paint()) {
            satPaint.setColor(HSBtoRGB(hue, 1f, 1f));
            canvas.drawRect(Rect.makeXYWH(pickerX, pickerY, contentW, pickerH), satPaint);

            try (Shader whiteGrad = Shader.makeLinearGradient(pickerX, pickerY, pickerX + contentW, pickerY,
                    new int[]{0xFFFFFFFF, 0x00FFFFFF})) {
                try (Paint gradPaint = new Paint().setShader(whiteGrad)) {
                    canvas.drawRect(Rect.makeXYWH(pickerX, pickerY, contentW, pickerH), gradPaint);
                }
            }

            try (Shader blackGrad = Shader.makeLinearGradient(pickerX, pickerY, pickerX, pickerY + pickerH,
                    new int[]{0x00000000, 0xFF000000})) {
                try (Paint gradPaint = new Paint().setShader(blackGrad)) {
                    canvas.drawRect(Rect.makeXYWH(pickerX, pickerY, contentW, pickerH), gradPaint);
                }
            }

            float indX = pickerX + saturation * contentW;
            float indY = pickerY + (1f - brightness) * pickerH;
            try (Paint ind = new Paint()) {
                ind.setColor(0xFFFFFFFF);
                ind.setMode(PaintMode.STROKE);
                ind.setStrokeWidth(2f);
                canvas.drawCircle(indX, indY, 5, ind);
            }
        }

        int[] hueColors = new int[36];
        for (int i = 0; i < hueColors.length; i++)
            hueColors[i] = HSBtoRGB((float) i / (hueColors.length - 1), 1f, 1f);
        try (Shader hueShader = Shader.makeLinearGradient(hueX, hueY, hueX + contentW, hueY, hueColors)) {
            try (Paint huePaint = new Paint().setShader(hueShader)) {
                canvas.drawRRect(RRect.makeXYWH(hueX, hueY, contentW, hueH, hueH / 2), huePaint);
            }
        }

        try (Paint marker = new Paint().setColor(0xFFFFFFFF).setMode(PaintMode.STROKE).setStrokeWidth(2)) {
            canvas.drawRect(Rect.makeXYWH(hueX + hue * contentW - 2, hueY - 1, 4, hueH + 2), marker);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!setting.isVisible()) return false;
        float pad = 10, contentW = width - pad * 2, hueH = 12, pickerH = height - hueH - pad * 3 - 22;
        float pickerX = currentX + pad, pickerY = currentY + 22;
        float hueX = currentX + pad, hueY = pickerY + pickerH + pad;
        if (button == 0) {
            if (mx >= pickerX && mx <= pickerX + contentW && my >= pickerY && my <= pickerY + pickerH) {
                draggingSatVal = true;
                return true;
            }
            if (mx >= hueX && mx <= hueX + contentW && my >= hueY && my <= hueY + hueH) {
                draggingHue = true;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0) {
            draggingHue = false;
            draggingSatVal = false;
        }
        return false;
    }

    private static float[] RGBtoHSB(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        float rf = r / 255f, gf = g / 255f, bf = b / 255f;
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;
        float hue = 0f;
        if (delta != 0) {
            if (max == rf) hue = ((gf - bf) / delta) % 6f;
            else if (max == gf) hue = ((bf - rf) / delta) + 2f;
            else hue = ((rf - gf) / delta) + 4f;
            hue /= 6f;
            if (hue < 0) hue += 1f;
        }
        float saturation = (max == 0) ? 0 : delta / max;
        float brightness = max;
        return new float[]{hue, saturation, brightness};
    }

    private static int HSBtoRGB(float hue, float saturation, float brightness) {
        float h = (hue - (float) Math.floor(hue)) * 6f;
        int i = (int) h;
        float f = h - i;
        float p = brightness * (1f - saturation);
        float q = brightness * (1f - saturation * f);
        float t = brightness * (1f - saturation * (1f - f));
        float r, g, b;
        switch (i) {
            case 0: r = brightness; g = t; b = p; break;
            case 1: r = q; g = brightness; b = p; break;
            case 2: r = p; g = brightness; b = t; break;
            case 3: r = p; g = q; b = brightness; break;
            case 4: r = t; g = p; b = brightness; break;
            case 5: r = brightness; g = p; b = q; break;
            default: r = g = b = 0; break;
        }
        return 0xFF000000 | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }
}
