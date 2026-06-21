package com.hinaclient.hina.ui;

import com.hinaclient.hina.module.impl.render.ClickGuiModule;

public final class Colors {

    public static final int TEXT_PRIMARY = 0xFFFFFFFF;
    public static final int TEXT_SECONDARY = 0xCCFFFFFF;
    public static final int TEXT_MUTED = 0x99FFFFFF;

    public static final int GLASS_BG = 0xC0B2DFDB;
    public static final int GLASS_BORDER = 0x33FFFFFF;
    public static final int GLASS_BORDER_ACTIVE = 0x66FFFFFF;
    public static final int GLASS_ITEM_BG = 0x18FFFFFF;
    public static final int GLASS_ITEM_HOVER = 0x25FFFFFF;
    public static final int GLASS_ITEM_ACTIVE = 0x30FFFFFF;

    public static final int SWITCH_TRACK_OFF = 0xAA555555;
    public static final int SLIDER_TRACK = 0x40FFFFFF;
    public static final int SLIDER_FILL = 0x80FFFFFF;

    public static final int SCROLLBAR_TRACK = 0x15FFFFFF;
    public static final int SCROLLBAR_THUMB = 0x40FFFFFF;

    public static final int SHADOW = 0x40000000;

    public static int getThemeColor() {
        return ClickGuiModule.getThemeColor();
    }

    public static int getThemeWithAlpha(int alpha) {
        return (getThemeColor() & 0x00FFFFFF) | (alpha << 24);
    }

    private Colors() {}
}
