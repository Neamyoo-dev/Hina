package com.hinaclient.hina.ui;

import com.hinaclient.hina.module.impl.render.ClickGuiModule;

public final class Colors {

    public static final int TEXT_PRIMARY = 0xFF3C3934;
    public static final int TEXT_SECONDARY = 0xFF655E55;
    public static final int TEXT_MUTED = 0xFF81786C;

    public static final int GLASS_BG = 0xBFFFFFF7;
    public static final int GLASS_BORDER = 0xB8FFFFFF;
    public static final int GLASS_BORDER_ACTIVE = 0xD2789B82;
    public static final int GLASS_ITEM_BG = 0x70FFFFFF;
    public static final int GLASS_ITEM_HOVER = 0x90FFFFFF;
    public static final int GLASS_ITEM_ACTIVE = 0xA6789B82;

    public static final int SWITCH_TRACK_OFF = 0xB8C9C5B9;
    public static final int SLIDER_TRACK = 0x55796F60;
    public static final int SLIDER_FILL = 0xCC789B82;

    public static final int SCROLLBAR_TRACK = 0x30796F60;
    public static final int SCROLLBAR_THUMB = 0x99789B82;

    public static final int SHADOW = 0x263C3026;
    public static final int ACCENT = 0xFF789B82;
    public static final int ACCENT_LIGHT = 0xFFA8C6A7;
    public static final int SURFACE = 0xCCFFFDF8;
    public static final int SURFACE_SOFT = 0x8CFFFFFF;
    public static final int SURFACE_RECESSED = 0x5CFFF9F0;
    public static final int SCRIM = 0x4D3C3026;
    public static final int DANGER = 0xFFB96E68;
    public static final int ON_ACCENT = 0xFFFFFAF0;

    public static final float RADIUS_SMALL = 6f;
    public static final float RADIUS_MEDIUM = 10f;
    public static final float RADIUS_LARGE = 18f;

    public static int getThemeColor() {
        return ClickGuiModule.getThemeColor();
    }

    public static int getThemeWithAlpha(int alpha) {
        return (getThemeColor() & 0x00FFFFFF) | (alpha << 24);
    }

    private Colors() {}
}
