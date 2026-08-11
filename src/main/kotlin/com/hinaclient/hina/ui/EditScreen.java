package com.hinaclient.hina.ui;

import com.hinaclient.hina.MioHr;
import com.hinaclient.hina.event.EventBus;
import com.hinaclient.hina.event.EventListener;
import com.hinaclient.hina.event.skia.EventSkiaDrawScene;
import com.hinaclient.hina.module.Module;
import com.hinaclient.hina.skia.font.FontManager;
import com.hinaclient.hina.ui.hud.HudElement;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.FontMetrics;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.PaintMode;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditScreen extends Screen {
    private final List<Module> draggableModules;
    private Module draggingModule = null;
    private double dragOffsetX, dragOffsetY;
    private final Set<Module> initializedModules = new HashSet<>();

    private int lastMouseRenderX;
    private int lastMouseRenderY;

    private static final float MODULE_CORNER = 4;
    private static final float GRID_COLUMNS = 5;
    private static final float GRID_STEP_X = 110;
    private static final float GRID_STEP_Y = 40;
    private static final float DEFAULT_X = 50;
    private static final float DEFAULT_Y = 50;

    public EditScreen() {
        super(Component.literal("EditScreen"));
        this.draggableModules = new ArrayList<>();
        List<Module> allModules = MioHr.getINSTANCE().moduleManager.getModules();
        int index = 0;
        for (Module module : allModules) {
            if (module instanceof HudElement) {
                if (isPositionDefault(module, index)) {
                    module.setX(DEFAULT_X + (index % GRID_COLUMNS) * GRID_STEP_X);
                    module.setY(DEFAULT_Y + (index / GRID_COLUMNS) * GRID_STEP_Y);
                    initializedModules.add(module);
                }
                draggableModules.add(module);
                index++;
            }
        }
    }

    private boolean isPositionDefault(Module module, int index) {
        if (initializedModules.contains(module)) return false;
        if (module.getX() == 0 && module.getY() == 0) return true;
        return index > 0 && module.getX() == DEFAULT_X && module.getY() == DEFAULT_Y;
    }

    @Override
    protected void init() {
        EventBus.INSTANCE.register(this);
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.lastMouseRenderX = mouseX;
        this.lastMouseRenderY = mouseY;
    }

    @EventListener
    public void onSkiaRender(@NotNull EventSkiaDrawScene event) {
        if (this.minecraft.screen != this) {
            EventBus.INSTANCE.unregister(this);
            return;
        }

        var canvas = event.getCanvas();
        float sw = (float) this.minecraft.getWindow().getGuiScaledWidth();
        float sh = (float) this.minecraft.getWindow().getGuiScaledHeight();
        int scale = this.minecraft.getWindow().getGuiScale();

        try (var p = new Paint().setColor(Colors.SHADOW)) {
            canvas.drawRect(Rect.makeWH(sw * scale, sh * scale), p);
        }

        if (draggingModule != null) {
            HudElement element = (HudElement) draggingModule;
            double nextX = Math.max(0, Math.min(sw - element.getHudWidth(), lastMouseRenderX - dragOffsetX));
            double nextY = Math.max(0, Math.min(sh - element.getHudHeight(), lastMouseRenderY - dragOffsetY));
            draggingModule.setX(nextX);
            draggingModule.setY(nextY);
        }

        canvas.save();
        canvas.scale(scale, scale);

        if (draggableModules.isEmpty()) {
            try (Paint textPaint = new Paint().setColor(Colors.TEXT_MUTED)) {
                Font font = FontManager.INSTANCE.getTextFont(14);
                FontMetrics metrics = font.getMetrics();
                float textY = sh / 2 - (metrics.getAscent() + metrics.getDescent()) / 2;
                canvas.drawString("No draggable modules", sw / 2 - 70, textY, font, textPaint);
            }
        }

        try (var p = new Paint().setMode(PaintMode.STROKE).setStrokeWidth(1f)) {
            for (Module mod : draggableModules) {
                HudElement element = (HudElement) mod;
                p.setColor(mod == draggingModule ? Colors.GLASS_BORDER_ACTIVE : Colors.GLASS_BORDER);
                canvas.drawRRect(
                        RRect.makeXYWH((float) mod.getX(), (float) mod.getY(), element.getHudWidth(), element.getHudHeight(), MODULE_CORNER),
                        p);
            }
        }
        canvas.restore();
    }

    @Override
    public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean bl) {
        double localX = event.x();
        double localY = event.y();

        for (Module mod : draggableModules) {
            HudElement element = (HudElement) mod;
            if (localX >= mod.getX() && localX <= mod.getX() + element.getHudWidth()
                    && localY >= mod.getY() && localY <= mod.getY() + element.getHudHeight()) {
                draggingModule = mod;
                dragOffsetX = localX - mod.getX();
                dragOffsetY = localY - mod.getY();
                return true;
            }
        }
        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseReleased(@NotNull MouseButtonEvent event) {
        if (draggingModule != null) {
            initializedModules.add(draggingModule);
            draggingModule = null;
        }
        return super.mouseReleased(event);
    }

    @Override
    public void removed() {
        EventBus.INSTANCE.unregister(this);
        MioHr.getINSTANCE().configManager.save();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
