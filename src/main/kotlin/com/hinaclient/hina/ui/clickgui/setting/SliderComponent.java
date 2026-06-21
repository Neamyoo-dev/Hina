package com.hinaclient.hina.ui.clickgui.setting;

import com.hinaclient.hina.setting.NumberSetting;
import com.hinaclient.hina.skia.font.FontManager;
import com.hinaclient.hina.ui.Colors;
import com.hinaclient.hina.ui.clickgui.Component;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.RRect;

public class SliderComponent extends Component {
    private final NumberSetting numSetting;
    private float currentX, currentY;
    private boolean dragging;

    private static final float PAD = 12;
    private static final float SLIDER_H = 4;
    private static final float KNOB_R = 6;

    public SliderComponent(NumberSetting setting, float width, float height) {
        super(setting, width, height);
        this.numSetting = setting;
    }

    @Override
    public void render(Canvas canvas, float x, float y, int mouseX, int mouseY) {
        if (!setting.isVisible()) return;
        this.currentX = x;
        this.currentY = y;

        if (dragging) {
            float percent = (mouseX - (x + PAD)) / (width - PAD * 2);
            percent = Math.clamp(percent, 0, 1);
            double val = numSetting.getMin() + (numSetting.getMax() - numSetting.getMin()) * percent;
            if (numSetting.getIncrement() > 0)
                val = Math.round(val / numSetting.getIncrement()) * numSetting.getIncrement();
            numSetting.setValue(val);
        }

        try (Paint bg = new Paint()) {
            bg.setColor(Colors.GLASS_ITEM_BG);
            canvas.drawRRect(RRect.makeXYWH(x, y, width, height, 8), bg);
        }

        try (Paint textPaint = new Paint().setColor(Colors.TEXT_SECONDARY)) {
            Font font = FontManager.INSTANCE.getTextFont(13);
            FontMetrics metrics = font.getMetrics();
            float textY = y + height / 2 - 10 - (metrics.getAscent() + metrics.getDescent()) / 2;
            canvas.drawString(setting.getName(), x + PAD, textY, font, textPaint);
        }

        String valStr = String.format("%.1f", numSetting.getValue());
        try (Paint valPaint = new Paint().setColor(Colors.TEXT_PRIMARY)) {
            Font font = FontManager.INSTANCE.getTextFont(12);
            float valW = font.measureTextWidth(valStr, valPaint);
            canvas.drawString(valStr, x + width - valW - PAD, y + height / 2 - 10 + 4, font, valPaint);
        }

        float sliderX = x + PAD;
        float sliderY = y + height - PAD + 2;
        float sliderW = width - PAD * 2;

        try (Paint track = new Paint()) {
            track.setColor(Colors.SLIDER_TRACK);
            canvas.drawRRect(RRect.makeXYWH(sliderX, sliderY, sliderW, SLIDER_H, 2), track);
        }

        double percent = (numSetting.getValue() - numSetting.getMin()) / (numSetting.getMax() - numSetting.getMin());
        float fillW = (float) (sliderW * percent);
        try (Paint fill = new Paint()) {
            fill.setColor(Colors.getThemeColor());
            canvas.drawRRect(RRect.makeXYWH(sliderX, sliderY, fillW, SLIDER_H, 2), fill);
        }

        float knobX = sliderX + fillW;
        float knobY = sliderY + SLIDER_H / 2;
        try (Paint knob = new Paint()) {
            knob.setColor(0xFFFFFFFF);
            canvas.drawCircle(knobX, knobY, KNOB_R, knob);
        }
        try (Paint knobBorder = new Paint()) {
            knobBorder.setMode(PaintMode.STROKE);
            knobBorder.setStrokeWidth(1f);
            knobBorder.setColor(Colors.GLASS_BORDER);
            canvas.drawCircle(knobX, knobY, KNOB_R, knobBorder);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!setting.isVisible()) return false;
        if (isHovered(mouseX, mouseY, currentX, currentY) && button == 0) {
            dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
        return false;
    }
}
