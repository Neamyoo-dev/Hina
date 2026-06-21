package com.hinaclient.hina.ui.clickgui.setting;

import com.hinaclient.hina.setting.BooleanSetting;
import com.hinaclient.hina.skia.font.FontManager;
import com.hinaclient.hina.ui.Colors;
import com.hinaclient.hina.ui.clickgui.Component;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.RRect;

public class CheckboxComponent extends Component {
    private final BooleanSetting boolSetting;
    private float currentX, currentY;
    private float animationProgress = 0f;

    public CheckboxComponent(BooleanSetting setting, float width, float height) {
        super(setting, width, height);
        this.boolSetting = setting;
    }

    @Override
    public void update() {
        float target = boolSetting.getValue() ? 1.0f : 0.0f;
        animationProgress += (target - animationProgress) * 0.2f;
        if (Math.abs(target - animationProgress) < 0.001f) animationProgress = target;
        super.update();
    }

    @Override
    public void render(Canvas canvas, float x, float y, int mouseX, int mouseY) {
        if (!setting.isVisible()) return;
        this.currentX = x;
        this.currentY = y;

        try (Paint bg = new Paint()) {
            bg.setColor(Colors.GLASS_ITEM_BG);
            canvas.drawRRect(RRect.makeXYWH(x, y, width, height, 8), bg);
        }

        try (Paint textPaint = new Paint().setColor(Colors.TEXT_SECONDARY)) {
            Font font = FontManager.INSTANCE.getTextFont(13);
            FontMetrics metrics = font.getMetrics();
            float textY = y + height / 2 - (metrics.getAscent() + metrics.getDescent()) / 2;
            canvas.drawString(setting.getName(), x + 14, textY, font, textPaint);
        }

        float switchW = 38, switchH = 20;
        float switchX = x + width - switchW - 14;
        float switchY = y + (height - switchH) / 2;

        try (Paint track = new Paint()) {
            track.setColor(animationProgress > 0.5f ? Colors.getThemeColor() : Colors.SWITCH_TRACK_OFF);
            canvas.drawRRect(RRect.makeXYWH(switchX, switchY, switchW, switchH, switchH / 2), track);
        }

        try (Paint border = new Paint()) {
            border.setMode(PaintMode.STROKE);
            border.setStrokeWidth(1f);
            border.setColor(Colors.GLASS_BORDER);
            canvas.drawRRect(RRect.makeXYWH(switchX, switchY, switchW, switchH, switchH / 2), border);
        }

        float knobSize = switchH - 4;
        float knobX = switchX + 2 + (switchW - knobSize - 4) * animationProgress;
        float knobY = switchY + 2;
        try (Paint knob = new Paint()) {
            knob.setColor(0xFFFFFFFF);
            canvas.drawCircle(knobX + knobSize / 2, knobY + knobSize / 2, knobSize / 2, knob);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!setting.isVisible()) return false;
        if (isHovered(mouseX, mouseY, currentX, currentY) && button == 0) {
            boolSetting.toggle();
            return true;
        }
        return false;
    }
}
