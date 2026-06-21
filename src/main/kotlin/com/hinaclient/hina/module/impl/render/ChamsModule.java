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

import com.hinaclient.hina.MioHr;
import com.hinaclient.hina.module.Category;
import com.hinaclient.hina.module.Module;
import com.hinaclient.hina.setting.BooleanSetting;
import com.hinaclient.hina.setting.ColorSetting;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderSetup.OutlineProperty;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

import java.awt.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ChamsModule extends Module {
    private final BooleanSetting players = new BooleanSetting("Players", true);
    private final BooleanSetting mobs = new BooleanSetting("Mobs", false);
    private final BooleanSetting animals = new BooleanSetting("Animals", false);
    private final BooleanSetting outline = new BooleanSetting("Outline", false);
    public final ColorSetting outlineColor = new ColorSetting("Outline Color", Color.WHITE);

    private static final RenderPipeline PIPELINE_ENTITY_TRANSLUCENT = createPipeline(true, true);
    private static final RenderPipeline PIPELINE_ENTITY_CUTOUT = createPipeline(false, false);
    private static final RenderPipeline PIPELINE_ENTITY_CUTOUT_NO_CULL = createPipeline(false, true);

    public static final BiFunction<Identifier, Boolean, RenderType> ENTITY_TRANSLUCENT = Util.memoize((texture, affectsOutline) -> {
        RenderSetup setup = RenderSetup.builder(PIPELINE_ENTITY_TRANSLUCENT)
                .withTexture("Sampler0", texture)
                .useLightmap()
                .useOverlay()
                .affectsCrumbling()
                .sortOnUpload()
                .setOutline(OutlineProperty.AFFECTS_OUTLINE)
                .createRenderSetup();
        return RenderType.create("chams_entity_translucent", setup);
    });

    public static final Function<Identifier, RenderType> ENTITY_CUTOUT = Util.memoize(texture -> {
        RenderSetup setup = RenderSetup.builder(PIPELINE_ENTITY_CUTOUT)
                .withTexture("Sampler0", texture)
                .useLightmap()
                .useOverlay()
                .affectsCrumbling()
                .setOutline(OutlineProperty.AFFECTS_OUTLINE)
                .createRenderSetup();
        return RenderType.create("chams_entity_cutout", setup);
    });

    public static final BiFunction<Identifier, Boolean, RenderType> ENTITY_CUTOUT_NO_CULL = Util.memoize((texture, affectsOutline) -> {
        RenderSetup setup = RenderSetup.builder(PIPELINE_ENTITY_CUTOUT_NO_CULL)
                .withTexture("Sampler0", texture)
                .useLightmap()
                .useOverlay()
                .affectsCrumbling()
                .setOutline(OutlineProperty.AFFECTS_OUTLINE)
                .createRenderSetup();
        return RenderType.create("chams_entity_cutout_no_cull", setup);
    });

    public ChamsModule() {
        super("Chams", Category.RENDER);
        addSetting(players);
        addSetting(mobs);
        addSetting(animals);
        addSetting(outline);
        addSetting(outlineColor);
    }

    private static RenderPipeline createPipeline(boolean translucent, boolean noCull) {
        RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                .withShaderDefine("ALPHA_CUTOUT", 0.1f)
                .withSampler("Sampler1");

        if (translucent) {
            builder.withShaderDefine("PER_FACE_LIGHTING")
                    .withBlend(BlendFunction.TRANSLUCENT);
        } else {
            builder.withShaderDefine("PER_FACE_LIGHTING");
        }

        if (noCull) {
            builder.withCull(false);
        }

        builder.withDepthBias(1f, -1000000f);

        String name = "chams_" + (translucent ? "translucent" : "cutout") + (noCull ? "_no_cull" : "");
        builder.withLocation(Identifier.fromNamespaceAndPath("miohr", name));

        return builder.build();
    }

    public static boolean shouldRender(Entity entity) {
        ChamsModule module = (ChamsModule) MioHr.getINSTANCE().moduleManager.getModuleByName("Chams");
        if (module == null || !module.isEnabled()) return false;
        if (entity instanceof Player && module.players.getValue()) return true;
        if (entity instanceof Monster && module.mobs.getValue()) return true;
        return entity instanceof Animal && module.animals.getValue();
    }

    public boolean shouldOutline(Entity entity) {
        return this.isEnabled() && this.outline.getValue() && shouldRender(entity);
    }
}