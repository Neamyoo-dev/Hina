package com.hinaclient.hina.ui.clickgui;

import com.hinaclient.hina.MioHr;
import com.hinaclient.hina.module.Category;
import com.hinaclient.hina.module.Module;
import com.hinaclient.hina.skia.SkiaRenderer;
import com.hinaclient.hina.skia.font.FontManager;
import com.hinaclient.hina.skia.font.Icon;
import com.hinaclient.hina.ui.Colors;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Panel {
    private static final float PANEL_WIDTH_RATIO = 0.55f;
    private static final float PANEL_HEIGHT_RATIO = 0.65f;
    private static final float CORNER_RADIUS = 16;
    private static final float TAB_WIDTH = 72;
    private static final float SEARCH_BAR_HEIGHT = 42;
    private static final float HEADER_PADDING = 12;
    private static final float TAB_ICON_SIZE = 22;
    private static final float TAB_LABEL_SIZE = 10;
    private static final float FONT_SIZE_SEARCH = 14;
    private static final float MODULE_ROW_HEIGHT = 36;
    private static final float MAX_VISIBLE_CONTENT_HEIGHT = 440;

    private float panelX, panelY, panelW, panelH;
    private boolean dragging;
    private float dragOffX, dragOffY;
    private Category selectedCategory = Category.COMBAT;
    private float tabHoverAlpha = 0f;
    private int hoveredTabIndex = -1;

    private String searchQuery = "";
    private boolean searchFocused = false;

    private final List<ModuleButton> allModuleButtons = new ArrayList<>();
    private float scrollOffset = 0f;
    private float maxScrollOffset = 0f;
    private static final float SCROLL_SPEED = 25f;

    private float openAnimProgress = 1f;

    public Panel() {
        for (Category category : Category.values()) {
            for (Module module : MioHr.getINSTANCE().moduleManager.getModulesByCategory(category)) {
                allModuleButtons.add(new ModuleButton(module, MODULE_ROW_HEIGHT));
            }
        }
    }

    public void update(int mouseX, int mouseY) {
        if (dragging) {
            panelX = mouseX - dragOffX;
            panelY = mouseY - dragOffY;
            clampToScreen();
        }

        for (ModuleButton btn : allModuleButtons) btn.update();

        Category[] cats = Category.values();
        hoveredTabIndex = -1;
        float tabStartY = panelY + SEARCH_BAR_HEIGHT + HEADER_PADDING;
        for (int i = 0; i < cats.length; i++) {
            float tabY = tabStartY + i * (TAB_ICON_SIZE + 24);
            if (mouseX >= panelX + HEADER_PADDING && mouseX <= panelX + HEADER_PADDING + TAB_WIDTH
                    && mouseY >= tabY - 6 && mouseY <= tabY + TAB_ICON_SIZE + 18) {
                hoveredTabIndex = i;
                break;
            }
        }

        List<ModuleButton> filtered = getFilteredButtons();
        float totalHeight = 0;
        for (ModuleButton btn : filtered) totalHeight += btn.getTotalHeight();
        float contentAreaH = getContentHeight();
        maxScrollOffset = Math.max(0, totalHeight - contentAreaH);
        if (scrollOffset > maxScrollOffset) scrollOffset = maxScrollOffset;
        if (scrollOffset < 0) scrollOffset = 0;
    }

    public void render(Canvas canvas, int mouseX, int mouseY, Shader glassShader) {
        if (openAnimProgress < 0.01f) return;

        float sw = (float) Minecraft.getInstance().getWindow().getWidth();
        float sh = (float) Minecraft.getInstance().getWindow().getHeight();
        panelW = sw * PANEL_WIDTH_RATIO;
        panelH = sh * PANEL_HEIGHT_RATIO;
        if (!dragging) {
            panelX = (sw - panelW) / 2;
            panelY = (sh - panelH) / 2;
        }

        float drawH = panelH * openAnimProgress;

        try (Paint shadow = new Paint()) {
            shadow.setColor(Colors.SHADOW);
            shadow.setMaskFilter(MaskFilter.makeBlur(FilterBlurMode.NORMAL, 20));
            canvas.drawRRect(RRect.makeXYWH(panelX + 3, panelY + 6, panelW, drawH, CORNER_RADIUS), shadow);
        }

        if (glassShader != null) {
            try (Paint glassPaint = new Paint().setShader(glassShader)) {
                canvas.drawRRect(RRect.makeXYWH(panelX, panelY, panelW, drawH, CORNER_RADIUS), glassPaint);
            }
        } else {
            try (Paint fallback = new Paint().setColor(Colors.GLASS_BG)) {
                canvas.drawRRect(RRect.makeXYWH(panelX, panelY, panelW, drawH, CORNER_RADIUS), fallback);
            }
        }

        try (Paint border = new Paint()) {
            border.setMode(PaintMode.STROKE);
            border.setStrokeWidth(1.5f);
            border.setColor(Colors.GLASS_BORDER);
            canvas.drawRRect(RRect.makeXYWH(panelX, panelY, panelW, drawH, CORNER_RADIUS), border);
        }

        drawSearchBar(canvas, mouseX, mouseY);
        drawTabBar(canvas, mouseX, mouseY);
        drawContent(canvas, mouseX, mouseY, drawH);
    }

    private void drawSearchBar(Canvas canvas, int mouseX, int mouseY) {
        float sx = panelX + HEADER_PADDING + TAB_WIDTH + 14;
        float sy = panelY + HEADER_PADDING;
        float sw = panelW - HEADER_PADDING - (HEADER_PADDING + TAB_WIDTH + 14) - HEADER_PADDING;
        float sh = SEARCH_BAR_HEIGHT - HEADER_PADDING * 2;

        boolean hover = mouseX >= sx && mouseX <= sx + sw && mouseY >= sy && mouseY <= sy + sh;

        try (Paint bg = new Paint()) {
            bg.setColor(hover || searchFocused ? Colors.GLASS_ITEM_HOVER : Colors.GLASS_ITEM_BG);
            canvas.drawRRect(RRect.makeXYWH(sx, sy, sw, sh, sh / 2), bg);
        }

        if (searchQuery.isEmpty() && !searchFocused) {
            try (Paint hint = new Paint().setColor(Colors.TEXT_MUTED)) {
                Font font = FontManager.INSTANCE.getTextFont(FONT_SIZE_SEARCH);
                FontMetrics metrics = font.getMetrics();
                float textY = sy + sh / 2 - (metrics.getAscent() + metrics.getDescent()) / 2;
                canvas.drawString("Search modules...", sx + 10, textY, font, hint);
            }
        } else {
            try (Paint textPaint = new Paint().setColor(Colors.TEXT_PRIMARY)) {
                Font font = FontManager.INSTANCE.getTextFont(FONT_SIZE_SEARCH);
                FontMetrics metrics = font.getMetrics();
                float textY = sy + sh / 2 - (metrics.getAscent() + metrics.getDescent()) / 2;
                canvas.drawString(searchQuery, sx + 10, textY, font, textPaint);
            }
        }

        SkiaRenderer.drawCenteredIcon(canvas, Icon.SEARCH, sx + sw - 16, sy + sh / 2, 14, Colors.TEXT_MUTED);
    }

    private void drawTabBar(Canvas canvas, int mouseX, int mouseY) {
        Category[] cats = Category.values();
        float tabStartX = panelX + HEADER_PADDING;
        float tabStartY = panelY + SEARCH_BAR_HEIGHT + HEADER_PADDING;

        for (int i = 0; i < cats.length; i++) {
            Category cat = cats[i];
            float tabY = tabStartY + i * (TAB_ICON_SIZE + 24);
            boolean selected = cat == selectedCategory;
            boolean hover = i == hoveredTabIndex;

            if (selected) {
                try (Paint selBg = new Paint()) {
                    selBg.setColor(Colors.GLASS_ITEM_ACTIVE);
                    canvas.drawRRect(RRect.makeXYWH(tabStartX, tabY - 4, TAB_WIDTH, TAB_ICON_SIZE + 24, 8), selBg);
                }
                try (Paint selLine = new Paint()) {
                    selLine.setColor(Colors.getThemeColor());
                    canvas.drawRRect(RRect.makeXYWH(tabStartX, tabY - 4, 3, TAB_ICON_SIZE + 24, 1.5f), selLine);
                }
            } else if (hover) {
                try (Paint hoverBg = new Paint()) {
                    hoverBg.setColor(Colors.GLASS_ITEM_HOVER);
                    canvas.drawRRect(RRect.makeXYWH(tabStartX, tabY - 4, TAB_WIDTH, TAB_ICON_SIZE + 24, 8), hoverBg);
                }
            }

            SkiaRenderer.drawCenteredIcon(canvas, cat.getIcon(), tabStartX + TAB_WIDTH / 2, tabY + TAB_ICON_SIZE / 2,
                    TAB_ICON_SIZE, selected ? Colors.TEXT_PRIMARY : Colors.TEXT_MUTED);
            try (Paint labelPaint = new Paint().setColor(selected ? Colors.TEXT_PRIMARY : Colors.TEXT_MUTED)) {
                Font font = FontManager.INSTANCE.getTextFont(TAB_LABEL_SIZE);
                canvas.drawString(cat.getName(), tabStartX + (TAB_WIDTH - font.measureTextWidth(cat.getName(), labelPaint)) / 2,
                        tabY + TAB_ICON_SIZE + 16, font, labelPaint);
            }
        }
    }

    private void drawContent(Canvas canvas, int mouseX, int mouseY, float drawH) {
        List<ModuleButton> filtered = getFilteredButtons();
        float contentX = panelX + HEADER_PADDING + TAB_WIDTH + 14;
        float contentY = panelY + SEARCH_BAR_HEIGHT;
        float contentW = panelW - contentX + panelX - HEADER_PADDING;
        float contentH = drawH - SEARCH_BAR_HEIGHT - HEADER_PADDING;

        canvas.save();
        canvas.clipRect(Rect.makeXYWH(contentX, contentY, contentW, contentH));

        float yOff = contentY + HEADER_PADDING - scrollOffset;
        for (ModuleButton btn : filtered) {
            btn.render(canvas, contentX + 8, yOff, mouseX, mouseY, contentW - 16);
            yOff += btn.getTotalHeight();
        }
        canvas.restore();

        if (maxScrollOffset > 0) {
            drawScrollIndicator(canvas, contentY, contentH);
        }

        if (filtered.isEmpty()) {
            try (Paint emptyPaint = new Paint().setColor(Colors.TEXT_MUTED)) {
                Font font = FontManager.INSTANCE.getTextFont(14);
                canvas.drawString("No modules found", contentX + contentW / 2 - 60,
                        contentY + contentH / 2, font, emptyPaint);
            }
        }
    }

    private void drawScrollIndicator(Canvas canvas, float contentY, float contentH) {
        float barX = panelX + panelW - HEADER_PADDING - 5;
        float barY = contentY + HEADER_PADDING;
        float barH = contentH - HEADER_PADDING * 2;
        float indicatorH = Math.max(20, barH * (barH / (barH + maxScrollOffset)));
        float indicatorY = barY + (scrollOffset / maxScrollOffset) * (barH - indicatorH);

        try (Paint track = new Paint()) {
            track.setColor(Colors.SCROLLBAR_TRACK);
            canvas.drawRRect(RRect.makeXYWH(barX, barY, 3, barH, 1.5f), track);
        }
        try (Paint thumb = new Paint()) {
            thumb.setColor(Colors.SCROLLBAR_THUMB);
            canvas.drawRRect(RRect.makeXYWH(barX, indicatorY, 3, indicatorH, 1.5f), thumb);
        }
    }

    private float getContentHeight() {
        return Math.min(panelH - SEARCH_BAR_HEIGHT - HEADER_PADDING, MAX_VISIBLE_CONTENT_HEIGHT);
    }

    private List<ModuleButton> getFilteredButtons() {
        return allModuleButtons.stream()
                .filter(btn -> btn.getModule().getCategory() == selectedCategory)
                .filter(btn -> searchQuery.isEmpty()
                        || btn.getModule().getName().toLowerCase().contains(searchQuery.toLowerCase()))
                .collect(Collectors.toList());
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float sx = panelX + HEADER_PADDING;
        float sy = panelY + HEADER_PADDING;
        float sw = panelW - HEADER_PADDING - HEADER_PADDING;

        if (button == 0 && mouseX >= sx && mouseX <= sx + sw
                && mouseY >= sy && mouseY <= sy + SEARCH_BAR_HEIGHT) {
            searchFocused = true;
            return true;
        }

        Category[] cats = Category.values();
        float tabStartX = panelX + HEADER_PADDING;
        float tabStartY = panelY + SEARCH_BAR_HEIGHT + HEADER_PADDING;
        for (int i = 0; i < cats.length; i++) {
            float tabY = tabStartY + i * (TAB_ICON_SIZE + 24);
            if (mouseX >= tabStartX && mouseX <= tabStartX + TAB_WIDTH
                    && mouseY >= tabY - 4 && mouseY <= tabY + TAB_ICON_SIZE + 24) {
                if (button == 0) {
                    selectedCategory = cats[i];
                    scrollOffset = 0;
                    return true;
                }
            }
        }

        if (button == 0 && mouseX >= panelX && mouseX <= panelX + panelW
                && mouseY >= panelY && mouseY <= panelY + panelH) {
            dragging = true;
            dragOffX = (float) mouseX - panelX;
            dragOffY = (float) mouseY - panelY;
            return true;
        }

        List<ModuleButton> filtered = getFilteredButtons();
        float contentX = panelX + HEADER_PADDING + TAB_WIDTH + 14;
        float contentY = panelY + SEARCH_BAR_HEIGHT;
        float yOff = contentY + HEADER_PADDING - scrollOffset;
        for (ModuleButton btn : filtered) {
            if (btn.mouseClicked(mouseX, mouseY, button, contentX + 8, yOff)) return true;
            yOff += btn.getTotalHeight();
        }

        return false;
    }

    public boolean mouseScrolled(double mouseY, double delta) {
        if (maxScrollOffset <= 0) return false;
        float contentY = panelY + SEARCH_BAR_HEIGHT;
        if (mouseY >= contentY && mouseY <= panelY + panelH) {
            scrollOffset -= (float) delta * SCROLL_SPEED;
            scrollOffset = Math.clamp(scrollOffset, 0, maxScrollOffset);
            return true;
        }
        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
        List<ModuleButton> filtered = getFilteredButtons();
        float contentX = panelX + HEADER_PADDING + TAB_WIDTH + 14;
        float contentY = panelY + SEARCH_BAR_HEIGHT;
        float yOff = contentY + HEADER_PADDING - scrollOffset;
        for (ModuleButton btn : filtered) {
            btn.mouseReleased(mouseX, mouseY, button, contentX + 8, yOff);
            yOff += btn.getTotalHeight();
        }
    }

    public boolean handleKeyPress(int keyCode) {
        if (searchFocused) {
            if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
                searchFocused = false;
                searchQuery = "";
                return true;
            }
            return true;
        }
        List<ModuleButton> filtered = getFilteredButtons();
        for (ModuleButton btn : filtered) {
            if (btn.handleKeyPress(keyCode)) return true;
        }
        return false;
    }

    public boolean handleCharTyped(char chr) {
        if (searchFocused) {
            if (chr == '\b' && !searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            } else if (chr >= 32 && chr < 127) {
                searchQuery += chr;
            }
            scrollOffset = 0;
            return true;
        }
        return false;
    }

    public void resetDrag() {
        dragging = false;
    }

    public boolean isSearchFocused() {
        return searchFocused;
    }

    private void clampToScreen() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() == null) return;
        int sw = mc.getWindow().getWidth();
        int sh = mc.getWindow().getHeight();
        panelX = Math.clamp(panelX, 0, sw - 200);
        panelY = Math.clamp(panelY, 0, sh - 200);
    }

    public float getX() { return panelX; }
    public float getY() { return panelY; }
    public float getWidth() { return panelW; }
    public float getHeight() { return panelH; }
}
