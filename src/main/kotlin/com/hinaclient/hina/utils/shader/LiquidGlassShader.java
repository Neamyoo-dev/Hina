package com.hinaclient.hina.utils.shader;

import io.github.humbleui.skija.*;
import io.github.humbleui.types.Rect;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class LiquidGlassShader {
    private static RuntimeEffect effect;
    private static Image cachedBackground;
    private static Shader cachedShader;
    private static Rect cachedRect;

    public static void init() {
        try (InputStream is = LiquidGlassShader.class.getResourceAsStream("/shaders/liquid_glass.sksl")) {
            if (is == null) throw new RuntimeException("Shader not found");
            String sksl = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            effect = RuntimeEffect.makeForShader(sksl);
        } catch (Exception e) {
            e.printStackTrace();
            effect = null;
        }
    }

    public static Shader makeShader(Image background, Rect glassRect) {
        if (effect == null) return null;

        if (cachedBackground == background && cachedRect != null
                && cachedRect.getLeft() == glassRect.getLeft()
                && cachedRect.getTop() == glassRect.getTop()
                && cachedRect.getWidth() == glassRect.getWidth()
                && cachedRect.getHeight() == glassRect.getHeight()
                && cachedShader != null) {
            return cachedShader;
        }

        cachedBackground = background;
        cachedRect = Rect.makeXYWH(glassRect.getLeft(), glassRect.getTop(), glassRect.getWidth(), glassRect.getHeight());

        Data uniformData = Data.makeFromBytes(createUniforms(glassRect));
        Shader[] children = new Shader[]{ background.makeShader() };
        cachedShader = effect.makeShader(uniformData, children, null);
        return cachedShader;
    }

    public static void invalidateCache() {
        if (cachedShader != null) {
            cachedShader.close();
            cachedShader = null;
        }
        cachedBackground = null;
        cachedRect = null;
    }

    private static byte[] createUniforms(Rect glass) {
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(16).order(java.nio.ByteOrder.nativeOrder());
        buf.putFloat(glass.getLeft());
        buf.putFloat(glass.getTop());
        buf.putFloat(glass.getWidth());
        buf.putFloat(glass.getHeight());
        return buf.array();
    }
}
