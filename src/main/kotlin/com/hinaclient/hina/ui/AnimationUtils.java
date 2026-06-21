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

package com.hinaclient.hina.ui;

public final class AnimationUtils {

    private static long lastFrameTime = System.nanoTime();
    private static float deltaTime = 1f / 60f;

    public static void tick() {
        long now = System.nanoTime();
        deltaTime = Math.min((now - lastFrameTime) / 1_000_000_000f, 0.05f);
        lastFrameTime = now;
    }

    public static float getDelta() {
        return deltaTime;
    }

    public static float lerp(float current, float target, float speed) {
        float lerpFactor = 1f - (float) Math.pow(1f - speed, deltaTime * 60f);
        return current + (target - current) * Math.min(lerpFactor, 1f);
    }

    public static boolean approx(float current, float target, float epsilon) {
        return Math.abs(current - target) < epsilon;
    }

    private AnimationUtils() {}
}
