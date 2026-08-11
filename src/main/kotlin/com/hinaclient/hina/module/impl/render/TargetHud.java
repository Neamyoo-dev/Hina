/*
 * Hina Client
 * Copyright (C) 2026 Hina Client
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.hinaclient.hina.module.impl.render;

import com.hinaclient.hina.event.EventListener;
import com.hinaclient.hina.event.impl.AttackEvent;
import com.hinaclient.hina.event.skia.EventSkiaDrawScene;
import com.hinaclient.hina.module.Category;
import com.hinaclient.hina.module.Module;
import com.hinaclient.hina.skia.font.FontManager;
import com.hinaclient.hina.ui.Colors;
import com.hinaclient.hina.ui.hud.HudElement;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerSkin;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TargetHud extends Module implements HudElement {
    private final Minecraft mc = Minecraft.getInstance();
    private final Map<String, Image> headCache = new HashMap<>();

    private float hpAnim = 0f;
    private float displayAnim = 0f;
    private float damageFlash = 0f;
    private float lastHp = -1f;

    private LivingEntity currentTarget = null;
    private LivingEntity lastTarget = null;
    private long lastSeenTime = 0L;

    private static final float HUD_WIDTH = 250f;
    private static final float HUD_HEIGHT = 58f;

    public TargetHud() {
        super("TargetHud", Category.RENDER);
    }

    @EventListener
    public void render(EventSkiaDrawScene event) {
        if (!FontManager.INSTANCE.isInitialized()) FontManager.INSTANCE.init();

        var canvas = event.getCanvas();
        if (currentTarget != null && (!currentTarget.isAlive() || Util.getMillis() - lastSeenTime >= 30000)) {
            currentTarget = null;
        }

        boolean shouldShow = currentTarget != null || (lastTarget != null && (Util.getMillis() - lastSeenTime) < 30000);

        if (!shouldShow) {
            displayAnim = Mth.lerp(0.12f, displayAnim, 0f);
            lastHp = -1f;
            lastTarget = null;
            if (displayAnim < 0.01f) {
                closeHeadCache();
            }
        } else {
            displayAnim = Mth.lerp(0.12f, displayAnim, 1f);
            LivingEntity displayEntity = currentTarget != null ? currentTarget : lastTarget;

            if (lastHp == -1f) lastHp = displayEntity.getHealth();
            if (displayEntity.getHealth() < lastHp) damageFlash = 1f;
            lastHp = displayEntity.getHealth();
            damageFlash = Mth.lerp(0.08f, damageFlash, 0f);

            float scale = (float) mc.getWindow().getGuiScale();
            canvas.save();
            canvas.scale(scale, scale);
            renderHud(canvas, displayEntity);
            canvas.restore();
        }
    }

    @EventListener
    public void onAttack(AttackEvent event) {
        currentTarget = event.getTarget();
        lastTarget = event.getTarget();
        lastSeenTime = Util.getMillis();
    }

    private void renderHud(Canvas canvas, LivingEntity target) {
        float w = HUD_WIDTH;
        float h = HUD_HEIGHT;
        float x = (float) getX();
        float y = (float) getY();

        canvas.save();
        canvas.translate(x + w / 2, y + h / 2);
        canvas.scale(displayAnim, displayAnim);
        canvas.translate(-w / 2, -h / 2);

        try (var p = new Paint()) {
            p.setColor(Colors.SURFACE);
            canvas.drawRRect(RRect.makeXYWH(0, 0, w, h, Colors.RADIUS_MEDIUM), p);
            int sideColor = Color.makeLerp(Colors.ACCENT, Colors.DANGER, damageFlash);
            p.setColor(sideColor);
            canvas.drawRect(Rect.makeXYWH(0, 8, 3f, h - 16), p);
            p.setColor(Colors.GLASS_BORDER);
            p.setMode(PaintMode.STROKE);
            p.setStrokeWidth(1f);
            canvas.drawRRect(RRect.makeXYWH(0, 0, w, h, Colors.RADIUS_MEDIUM), p);
        }

        if (target instanceof Player p) {
            Image img = getSkin(p);
            if (img != null) drawHead(canvas, img, 8, 10, h - 20, h - 20, 5);
        }

        Font fontBold = FontManager.INSTANCE.getTextFont(15f);
        Font fontSmall = FontManager.INSTANCE.getTextFont(11f);

        try (var p = new Paint().setColor(Colors.TEXT_PRIMARY)) {
            canvas.drawString(target.getName().getString(), 48, 20, fontBold, p);
            p.setAlpha(160);
            canvas.drawString(String.format("%.1f HP", target.getHealth()), 48, 32, fontSmall, p);
            if (mc.player != null) {
                canvas.drawString(String.format("%.1fm", mc.player.distanceTo(target)), w - 120, 32, fontSmall, p);
            }
        }

        drawHealthBar(canvas, target, 48, 38, 95, 4.5f);
        drawEffects(canvas, target, 48, 46);
        canvas.restore();
    }

    private void drawEffects(Canvas canvas, LivingEntity target, float x, float y) {
        float offset = 0;
        for (MobEffectInstance effect : target.getActiveEffects()) {
            if (offset > 90) break;
            int color = effect.getEffect().value().getColor();
            try (var p = new Paint().setColor(color).setAlpha(200)) {
                canvas.drawRect(Rect.makeXYWH(x + offset, y, 8, 2), p);
            }
            offset += 10;
        }
    }

    private void drawHealthBar(Canvas canvas, LivingEntity target, float x, float y, float w, float h) {
        float pct = Mth.clamp(target.getHealth() / target.getMaxHealth(), 0, 1);
        hpAnim = Mth.lerp(0.1f, hpAnim, pct);

        try (var p = new Paint()) {
            p.setColor(0x44000000);
            canvas.drawRRect(RRect.makeXYWH(x, y, w, h, 1f), p);
            int c1 = Color.makeLerp(Colors.ACCENT, Colors.DANGER, damageFlash);
            int c2 = Color.makeLerp(Colors.ACCENT_LIGHT, Colors.DANGER, damageFlash);
            try (var s = Shader.makeLinearGradient(x, y, x + w * hpAnim, y, new int[]{c1, c2})) {
                p.setShader(s);
                canvas.drawRRect(RRect.makeXYWH(x, y, w * hpAnim, h, 1f), p);
            }
        }
    }

    private void drawHead(Canvas canvas, Image img, float x, float y, float width, float height, float radius) {
        float ratio = img.getWidth() / 64.0f;

        Rect srcRect = Rect.makeXYWH(8 * ratio, 8 * ratio, 8 * ratio, 8 * ratio);
        Rect srcRect1 = Rect.makeXYWH(40 * ratio, 8 * ratio, 8 * ratio, 8 * ratio);

        Rect dstRect = Rect.makeXYWH(x, y, width, height);
        RRect rrect = RRect.makeXYWH(x, y, width, height, radius);

        canvas.save();

        canvas.clipRRect(rrect, ClipMode.INTERSECT, true);

        canvas.drawImageRect(img, srcRect, dstRect, SamplingMode.DEFAULT, null, false);
        canvas.drawImageRect(img, srcRect1, dstRect, SamplingMode.DEFAULT, null, false);

        canvas.restore();
    }

    private Image getSkin(Player player) {
        GameProfile profile = player.getGameProfile();
        String name = profile.name();
        if (headCache.containsKey(name)) return headCache.get(name);
        var future = mc.getSkinManager().get(profile);
        Optional<PlayerSkin> optionalSkin = future.getNow(Optional.empty());
        if (optionalSkin.isPresent()) {
            var identifier = optionalSkin.get().body().texturePath();
            var tex = mc.getTextureManager().getTexture(identifier);
            if (tex instanceof DynamicTexture dt) {
                NativeImage ni = dt.getPixels();
                if (ni != null) {
                    Image img = nativeImageToSkijaImage(ni);
                    headCache.put(name, img);
                    return img;
                }
            }
        }
        return null;
    }

    public static Image nativeImageToSkijaImage(NativeImage nativeImage) {
        int[] pixels = nativeImage.getPixelsABGR();

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(pixels.length * 4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.asIntBuffer().put(pixels);

        byte[] byteArray = new byte[byteBuffer.remaining()];
        byteBuffer.get(byteArray);

        ImageInfo info = new ImageInfo(
                nativeImage.getWidth(),
                nativeImage.getHeight(),
                ColorType.RGBA_8888,
                ColorAlphaType.PREMUL
        );

        return Image.makeRasterFromBytes(info, byteArray, nativeImage.getWidth() * 4L);
    }

    private void closeHeadCache() {
        headCache.values().forEach(Image::close);
        headCache.clear();
    }

    @Override
    protected void onDisable() {
        closeHeadCache();
        currentTarget = null;
        lastTarget = null;
        super.onDisable();
    }

    @Override
    public float getHudWidth() {
        return HUD_WIDTH;
    }

    @Override
    public float getHudHeight() {
        return HUD_HEIGHT;
    }
}
