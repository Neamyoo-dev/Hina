package com.hinaclient.hina.ui.clickgui;

import com.hinaclient.hina.MioHr;
import com.hinaclient.hina.module.Category;
import com.hinaclient.hina.module.Module;
import com.hinaclient.hina.skia.SkiaRenderer;
import com.hinaclient.hina.skia.font.FontManager;
import com.hinaclient.hina.skia.font.Icon;
import com.hinaclient.hina.ui.Colors;
import com.hinaclient.hina.ui.ClickGuiScreen;
import com.hinaclient.hina.ui.EditScreen;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Panel {
    private static final float NAV_ITEM_HEIGHT = 34f;
    private static final float SCROLL_SPEED = 24f;
    private final List<ModuleButton> modules = new ArrayList<>();
    private Category selectedCategory = Category.HACKS;
    private ClickGuiLayout layout;
    private String searchQuery = "";
    private boolean searchFocused;
    private float scrollOffset;
    private float maxScrollOffset;

    public Panel() {
        for (Category category : Category.values()) {
            for (Module module : MioHr.getINSTANCE().moduleManager.getModulesByCategory(category)) {
                modules.add(new ModuleButton(module, ClickGuiMetrics.MODULE_HEIGHT));
            }
        }
    }

    public void layout() {
        Minecraft minecraft = Minecraft.getInstance();
        layout = ClickGuiLayout.calculate(minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
    }

    public void update(int mouseX, int mouseY) {
        layout();
        modules.forEach(ModuleButton::update);
        float totalHeight = 0f;
        for (ModuleButton module : filteredModules()) totalHeight += module.getTotalHeight() + ClickGuiMetrics.MODULE_GAP;
        maxScrollOffset = Math.max(0f, totalHeight - layout.moduleViewport.getHeight());
        scrollOffset = Math.clamp(scrollOffset, 0f, maxScrollOffset);
    }

    public void render(Canvas canvas, int mouseX, int mouseY, Shader glassShader) {
        layout();
        float scale = Minecraft.getInstance().getWindow().getGuiScale();
        drawFramebufferGlass(canvas, glassShader, scale);
        canvas.save();
        canvas.scale(scale, scale);
        drawShell(canvas);
        drawSidebar(canvas, mouseX, mouseY);
        drawWorkspace(canvas, mouseX, mouseY);
        canvas.restore();
    }

    private void drawFramebufferGlass(Canvas canvas, Shader glassShader, float scale) {
        Rect panel = layout.panel;
        try (Paint shadow = new Paint().setColor(Colors.SHADOW).setMaskFilter(MaskFilter.makeBlur(FilterBlurMode.NORMAL, 20f))) {
            canvas.drawRRect(RRect.makeXYWH((panel.getLeft() + 3f) * scale, (panel.getTop() + 6f) * scale,
                    panel.getWidth() * scale, panel.getHeight() * scale, ClickGuiMetrics.PANEL_RADIUS * scale), shadow);
        }
        try (Paint surface = new Paint()) {
            if (glassShader != null) surface.setShader(glassShader); else surface.setColor(Colors.GLASS_BG);
            canvas.drawRRect(RRect.makeXYWH(panel.getLeft() * scale, panel.getTop() * scale,
                    panel.getWidth() * scale, panel.getHeight() * scale, ClickGuiMetrics.PANEL_RADIUS * scale), surface);
        }
    }

    private void drawShell(Canvas canvas) {
        Rect panel = layout.panel;
        RRect shell = RRect.makeXYWH(panel.getLeft(), panel.getTop(), panel.getWidth(), panel.getHeight(), ClickGuiMetrics.PANEL_RADIUS);
        try (Paint wash = new Paint().setColor(0x36FFFBF4);
             Paint border = new Paint().setColor(Colors.GLASS_BORDER).setMode(PaintMode.STROKE).setStrokeWidth(1f)) {
            canvas.drawRRect(shell, wash);
            canvas.drawRRect(shell, border);
        }
        canvas.save();
        canvas.clipRRect(shell, true);
        try (Paint rail = new Paint().setColor(0x38FFF9F0);
             Paint divider = new Paint().setColor(0x28796F60)) {
            canvas.drawRect(layout.sidebar, rail);
            canvas.drawRect(Rect.makeXYWH(layout.sidebar.getRight(), layout.panel.getTop(), 1f, layout.panel.getHeight()), divider);
        }
        canvas.restore();
    }

    private void drawSidebar(Canvas canvas, int mouseX, int mouseY) {
        Rect brand = layout.brand;
        float markSize = 26f;
        try (Paint mark = new Paint().setColor(Colors.ACCENT);
             Paint onAccent = new Paint().setColor(Colors.ON_ACCENT);
             Paint primary = new Paint().setColor(Colors.TEXT_PRIMARY);
             Paint muted = new Paint().setColor(Colors.TEXT_MUTED)) {
            canvas.drawRRect(RRect.makeXYWH(brand.getLeft(), brand.getTop(), markSize, markSize, 7f), mark);
            canvas.drawString("M", brand.getLeft() + 8f, brand.getTop() + 18f, FontManager.INSTANCE.getTextFont(13f), onAccent);
            float textX = brand.getLeft() + 35f;
            canvas.drawString("MIOHR CLIENT", textX, brand.getTop() + 11f, FontManager.INSTANCE.getTextFont(10f), primary);
            canvas.drawString("CONTROL SURFACE", textX, brand.getTop() + 23f, FontManager.INSTANCE.getTextFont(7f), muted);
            canvas.drawString("1.21.11", textX, brand.getTop() + 33f, FontManager.INSTANCE.getTextFont(7f), muted);
            canvas.drawString("MODULES", layout.navigation.getLeft(), layout.navigation.getTop() - 10f,
                    FontManager.INSTANCE.getTextFont(7f), muted);
        }

        Category[] categories = Category.values();
        for (int index = 0; index < categories.length; index++) {
            Category category = categories[index];
            Rect item = navItem(index);
            boolean selected = category == selectedCategory;
            boolean hovered = item.contains(mouseX, mouseY);
            if (selected || hovered) {
                try (Paint background = new Paint().setColor(selected ? Colors.ACCENT : Colors.GLASS_ITEM_HOVER)) {
                    canvas.drawRRect(RRect.makeXYWH(item.getLeft(), item.getTop(), item.getWidth(), item.getHeight(), ClickGuiMetrics.RADIUS), background);
                }
            }
            int foreground = selected ? Colors.ON_ACCENT : hovered ? Colors.TEXT_PRIMARY : Colors.TEXT_MUTED;
            SkiaRenderer.drawCenteredIcon(canvas, category.getIcon(), item.getLeft() + 15f, item.getTop() + item.getHeight() / 2f, 11f, foreground);
            try (Paint text = new Paint().setColor(foreground)) {
                canvas.drawString(category.getName(), item.getLeft() + 32f, item.getTop() + 22f,
                        FontManager.INSTANCE.getTextFont(selected ? 10f : 9f), text);
            }
        }

        try (Paint line = new Paint().setColor(0x28796F60);
             Paint muted = new Paint().setColor(Colors.TEXT_MUTED)) {
            canvas.drawRect(Rect.makeXYWH(layout.footer.getLeft(), layout.footer.getTop(), layout.footer.getWidth(), 1f), line);
            canvas.drawString("Right Shift · Toggle GUI", layout.footer.getLeft(), layout.footer.getTop() + 19f,
                    FontManager.INSTANCE.getTextFont(7f), muted);
        }
    }

    private void drawWorkspace(Canvas canvas, int mouseX, int mouseY) {
        Rect header = layout.header;
        try (Paint accent = new Paint().setColor(Colors.ACCENT);
             Paint primary = new Paint().setColor(Colors.TEXT_PRIMARY);
             Paint muted = new Paint().setColor(Colors.TEXT_MUTED)) {
            canvas.drawString("/ CONFIGURATION SPACE", header.getLeft(), header.getTop() + 8f,
                    FontManager.INSTANCE.getTextFont(7f), accent);
            canvas.drawString(selectedCategory.getName() + " modules", header.getLeft(), header.getTop() + 34f,
                    FontManager.INSTANCE.getTextFont(20f), primary);
            canvas.drawString("Tune active systems without leaving the game.", header.getLeft(), header.getTop() + 51f,
                    FontManager.INSTANCE.getTextFont(8f), muted);
        }
        drawAction(canvas, layout.hudAction, Icon.WIDGETS, mouseX, mouseY);
        drawAction(canvas, layout.closeAction, Icon.CLOSE, mouseX, mouseY);
        drawSearch(canvas, mouseX, mouseY);
        List<ModuleButton> filtered = filteredModules();
        try (Paint primary = new Paint().setColor(Colors.TEXT_PRIMARY);
             Paint muted = new Paint().setColor(Colors.TEXT_MUTED)) {
            canvas.drawString("Available modules", layout.section.getLeft(), layout.section.getTop() + 20f,
                    FontManager.INSTANCE.getTextFont(9f), primary);
            String count = String.format("%02d / %02d", filtered.size(), categoryModules().size());
            float countWidth = FontManager.INSTANCE.getTextFont(9f).measureTextWidth(count, muted);
            canvas.drawString(count, layout.section.getRight() - countWidth, layout.section.getTop() + 20f,
                    FontManager.INSTANCE.getTextFont(9f), muted);
        }
        drawModules(canvas, filtered, mouseX, mouseY);
    }

    private void drawAction(Canvas canvas, Rect bounds, String icon, int mouseX, int mouseY) {
        boolean hovered = bounds.contains(mouseX, mouseY);
        try (Paint background = new Paint().setColor(hovered ? Colors.GLASS_ITEM_ACTIVE : Colors.GLASS_ITEM_BG);
             Paint border = new Paint().setColor(hovered ? Colors.GLASS_BORDER_ACTIVE : Colors.GLASS_BORDER)
                     .setMode(PaintMode.STROKE).setStrokeWidth(1f)) {
            RRect shape = RRect.makeXYWH(bounds.getLeft(), bounds.getTop(), bounds.getWidth(), bounds.getHeight(), ClickGuiMetrics.RADIUS);
            canvas.drawRRect(shape, background);
            canvas.drawRRect(shape, border);
        }
        SkiaRenderer.drawCenteredIcon(canvas, icon, bounds.getLeft() + bounds.getWidth() / 2f,
                bounds.getTop() + bounds.getHeight() / 2f, 14f, hovered ? Colors.ACCENT : Colors.TEXT_MUTED);
    }

    private void drawSearch(Canvas canvas, int mouseX, int mouseY) {
        Rect search = layout.search;
        boolean hovered = search.contains(mouseX, mouseY);
        try (Paint background = new Paint().setColor(Colors.GLASS_ITEM_BG);
             Paint border = new Paint().setColor(searchFocused || hovered ? Colors.GLASS_BORDER_ACTIVE : Colors.GLASS_BORDER)
                     .setMode(PaintMode.STROKE).setStrokeWidth(1f)) {
            RRect shape = RRect.makeXYWH(search.getLeft(), search.getTop(), search.getWidth(), search.getHeight(), 12f);
            canvas.drawRRect(shape, background);
            canvas.drawRRect(shape, border);
        }
        SkiaRenderer.drawCenteredIcon(canvas, Icon.SEARCH, search.getLeft() + 16f, search.getTop() + search.getHeight() / 2f,
                12f, Colors.TEXT_MUTED);
        String text = searchQuery.isEmpty() ? "Search modules..." : searchQuery;
        try (Paint paint = new Paint().setColor(searchQuery.isEmpty() ? Colors.TEXT_MUTED : Colors.TEXT_PRIMARY)) {
            canvas.drawString(text, search.getLeft() + 31f, search.getTop() + 28f, FontManager.INSTANCE.getTextFont(11f), paint);
        }
        try (Paint key = new Paint().setColor(Colors.TEXT_MUTED)) {
            canvas.drawString("CTRL K", search.getRight() - 39f, search.getTop() + 27f, FontManager.INSTANCE.getTextFont(8f), key);
        }
    }

    private void drawModules(Canvas canvas, List<ModuleButton> filtered, int mouseX, int mouseY) {
        Rect viewport = layout.moduleViewport;
        canvas.save();
        canvas.clipRect(viewport);
        float y = viewport.getTop() - scrollOffset;
        for (ModuleButton module : filtered) {
            module.render(canvas, viewport.getLeft(), y, mouseX, mouseY, viewport.getWidth());
            y += module.getTotalHeight() + ClickGuiMetrics.MODULE_GAP;
        }
        canvas.restore();
        if (filtered.isEmpty()) {
            try (Paint muted = new Paint().setColor(Colors.TEXT_MUTED)) {
                canvas.drawString("No modules match your search", viewport.getLeft() + 16f, viewport.getTop() + 30f,
                        FontManager.INSTANCE.getTextFont(11f), muted);
            }
        }
        if (maxScrollOffset > 0f) {
            float trackHeight = viewport.getHeight();
            float thumbHeight = Math.max(20f, trackHeight * trackHeight / (trackHeight + maxScrollOffset));
            float thumbY = viewport.getTop() + scrollOffset / maxScrollOffset * (trackHeight - thumbHeight);
            try (Paint thumb = new Paint().setColor(Colors.SCROLLBAR_THUMB)) {
                canvas.drawRRect(RRect.makeXYWH(viewport.getRight() - 3f, thumbY, 3f, thumbHeight, 2f), thumb);
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (layout == null) layout();
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && layout.closeAction.contains((float) mouseX, (float) mouseY)) {
            ClickGuiScreen instance = ClickGuiScreen.Companion.getInstance();
            if (instance != null) instance.closeGui();
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && layout.hudAction.contains((float) mouseX, (float) mouseY)) {
            Minecraft.getInstance().setScreen(new EditScreen());
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && layout.search.contains((float) mouseX, (float) mouseY)) {
            searchFocused = true;
            return true;
        }
        for (int index = 0; index < Category.values().length; index++) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && navItem(index).contains((float) mouseX, (float) mouseY)) {
                selectedCategory = Category.values()[index];
                searchFocused = false;
                scrollOffset = 0f;
                return true;
            }
        }
        if (!layout.moduleViewport.contains((float) mouseX, (float) mouseY)) {
            searchFocused = false;
            return false;
        }
        float y = layout.moduleViewport.getTop() - scrollOffset;
        for (ModuleButton module : filteredModules()) {
            if (module.mouseClicked(mouseX, mouseY, button, layout.moduleViewport.getLeft(), y)) return true;
            y += module.getTotalHeight() + ClickGuiMetrics.MODULE_GAP;
        }
        searchFocused = false;
        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        float y = layout.moduleViewport.getTop() - scrollOffset;
        for (ModuleButton module : filteredModules()) {
            module.mouseReleased(mouseX, mouseY, button, layout.moduleViewport.getLeft(), y);
            y += module.getTotalHeight() + ClickGuiMetrics.MODULE_GAP;
        }
    }

    public boolean mouseScrolled(double mouseY, double delta) {
        if (layout == null || mouseY < layout.moduleViewport.getTop() || mouseY > layout.moduleViewport.getBottom()) return false;
        scrollOffset = Math.clamp(scrollOffset - (float) delta * SCROLL_SPEED, 0f, maxScrollOffset);
        return true;
    }

    public boolean handleKeyPress(int keyCode) {
        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searchFocused = false;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                scrollOffset = 0f;
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_K) {
            searchFocused = true;
            return true;
        }
        for (ModuleButton module : filteredModules()) if (module.handleKeyPress(keyCode)) return true;
        return false;
    }

    public boolean handleCharTyped(char chr) {
        if (!searchFocused || Character.isISOControl(chr)) return false;
        searchQuery += chr;
        scrollOffset = 0f;
        return true;
    }

    public void resetDrag() {
    }

    public float getX() { return layout == null ? 0f : layout.panel.getLeft(); }
    public float getY() { return layout == null ? 0f : layout.panel.getTop(); }
    public float getWidth() { return layout == null ? 0f : layout.panel.getWidth(); }
    public float getHeight() { return layout == null ? 0f : layout.panel.getHeight(); }

    private Rect navItem(int index) {
        return Rect.makeXYWH(layout.navigation.getLeft(), layout.navigation.getTop() + index * (NAV_ITEM_HEIGHT + 5f),
                layout.navigation.getWidth(), NAV_ITEM_HEIGHT);
    }

    private List<ModuleButton> categoryModules() {
        return modules.stream().filter(module -> module.getModule().getCategory() == selectedCategory).toList();
    }

    private List<ModuleButton> filteredModules() {
        String query = searchQuery.toLowerCase(Locale.ROOT);
        return modules.stream().filter(module -> module.getModule().getCategory() == selectedCategory)
                .filter(module -> query.isEmpty() || module.getModule().getName().toLowerCase(Locale.ROOT).contains(query)).toList();
    }
}
