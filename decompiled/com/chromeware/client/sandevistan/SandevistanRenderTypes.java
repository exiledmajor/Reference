/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.RenderStateShard$EmptyTextureStateShard
 *  net.minecraft.client.renderer.RenderStateShard$ShaderStateShard
 *  net.minecraft.client.renderer.RenderStateShard$TextureStateShard
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeRenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  net.minecraft.client.renderer.RenderType$CompositeState$CompositeStateBuilder
 *  net.minecraft.client.renderer.ShaderInstance
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.neoforged.neoforge.client.event.RegisterShadersEvent
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 */
package com.chromeware.client.sandevistan;

import com.chromeware.Chromeware;
import com.chromeware.mixin.client.SandevistanCompositeAccessor;
import com.chromeware.mixin.client.SandevistanRenderTypeAccessor;
import com.chromeware.mixin.client.SandevistanTextureAccessor;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

final class SandevistanRenderTypes
extends RenderType {
    private static RenderTarget target;
    private static final Map<RenderType, RenderType> LAYERS;
    private static final Map<PaletteLayer, RenderType> PALETTE_LAYERS;
    private static final Map<RenderType, RenderType> CACHED_PALETTES;
    private static final Map<RenderType, RenderType> CACHED_LIVE;
    private static final Map<Integer, SandevistanPalette> CACHED_COLORS;
    private static final Matrix4f IDENTITY_TRANSFORM;
    private static final Matrix3f IDENTITY_NORMAL;
    private static ShaderInstance paletteShader;
    private static ShaderInstance wirePaletteShader;
    private static ShaderInstance liveShader;

    private SandevistanRenderTypes() {
        super("unused", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, false, () -> {}, () -> {});
    }

    static void target(RenderTarget next) {
        target = next;
    }

    static void clear() {
        target = null;
        LAYERS.clear();
        PALETTE_LAYERS.clear();
        CACHED_PALETTES.clear();
        CACHED_LIVE.clear();
        CACHED_COLORS.clear();
    }

    static void registerPaletteShaders(RegisterShadersEvent event) {
        liveShader = null;
        wirePaletteShader = null;
        paletteShader = null;
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"sandevistan_palette"), DefaultVertexFormat.NEW_ENTITY), shader -> {
                paletteShader = shader;
            });
            event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"sandevistan_palette_wire"), DefaultVertexFormat.POSITION_COLOR), shader -> {
                wirePaletteShader = shader;
            });
            event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"sandevistan_live"), DefaultVertexFormat.NEW_ENTITY), shader -> {
                liveShader = shader;
            });
        }
        catch (IOException failure) {
            Chromeware.LOGGER.error("Cannot load Sandevistan palette shaders; retaining original afterimage textures", (Throwable)failure);
        }
    }

    static RenderType cached(RenderType source, boolean historical) {
        boolean wire;
        block4: {
            block3: {
                boolean bl = wire = source.format() == DefaultVertexFormat.POSITION_COLOR;
                if (source.mode() != VertexFormat.Mode.QUADS || wire && !historical || !historical && source.sortOnUpload() || !wire && source.format() != DefaultVertexFormat.BLOCK && source.format() != DefaultVertexFormat.NEW_ENTITY) break block3;
                if ((historical ? (wire ? wirePaletteShader : paletteShader) : liveShader) != null) break block4;
            }
            return null;
        }
        return (historical ? CACHED_PALETTES : CACHED_LIVE).computeIfAbsent(source, original -> {
            RenderType.CompositeState.CompositeStateBuilder state = RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(() -> historical ? (wire ? wirePaletteShader : paletteShader) : liveShader)).setTransparencyState(historical ? NO_TRANSPARENCY : TRANSLUCENT_TRANSPARENCY).setCullState(historical || source.format() != DefaultVertexFormat.BLOCK ? NO_CULL : CULL).setWriteMaskState(COLOR_DEPTH_WRITE);
            if (!wire) {
                state.setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(SandevistanRenderTypes.texture(source), false, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY);
            }
            return SandevistanRenderTypes.create((String)"chrome_sandy_cached", (VertexFormat)(wire ? DefaultVertexFormat.POSITION_COLOR : DefaultVertexFormat.NEW_ENTITY), (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)1536, (boolean)false, (boolean)false, (RenderType.CompositeState)state.createCompositeState(false));
        });
    }

    static ShaderInstance prepareCached(RenderType source, PoseStack.Pose pose, int color, boolean historical) {
        ShaderInstance shader;
        boolean wire;
        boolean bl = wire = source.format() == DefaultVertexFormat.POSITION_COLOR;
        ShaderInstance shaderInstance = historical ? (wire ? wirePaletteShader : paletteShader) : (shader = liveShader);
        if (historical) {
            SandevistanRenderTypes.setPalette(shader, color);
        }
        shader.safeGetUniform("EchoTransform").set(pose.pose());
        if (!wire) {
            shader.safeGetUniform("EchoNormalMat").set(pose.normal());
            shader.safeGetUniform("BlockShading").set(source.format() == DefaultVertexFormat.BLOCK ? 1.0f : 0.0f);
            shader.safeGetUniform("EchoFullBright").set(historical ? 1.0f : 0.0f);
        }
        if (target != null) {
            target.bindWrite(false);
        }
        return shader;
    }

    static void resetCached(ShaderInstance shader) {
        shader.safeGetUniform("EchoTransform").set(IDENTITY_TRANSFORM);
        shader.safeGetUniform("EchoNormalMat").set(IDENTITY_NORMAL);
        shader.safeGetUniform("EchoFullBright").set(0.0f);
    }

    private static ResourceLocation texture(RenderType source) {
        if (source instanceof SandevistanRenderTypeAccessor) {
            SandevistanRenderTypeAccessor access = (SandevistanRenderTypeAccessor)source;
            SandevistanCompositeAccessor original = (SandevistanCompositeAccessor)access.chrome$state();
            return ((SandevistanTextureAccessor)original.chrome$texture()).chrome$id().orElse(TextureAtlas.LOCATION_BLOCKS);
        }
        return TextureAtlas.LOCATION_BLOCKS;
    }

    private static void setPalette(ShaderInstance shader, int color) {
        SandevistanPalette colors = CACHED_COLORS.computeIfAbsent(color, SandevistanPalette::forColor);
        SandevistanRenderTypes.setColor(shader, "PaletteShadow", colors.shadow());
        SandevistanRenderTypes.setColor(shader, "PaletteDark", colors.dark());
        SandevistanRenderTypes.setColor(shader, "PaletteMidtone", colors.midtone());
        SandevistanRenderTypes.setColor(shader, "PaletteHighlight", colors.highlight());
    }

    static RenderType palette(RenderType source, int color) {
        boolean wire;
        block3: {
            block2: {
                boolean bl = wire = source.format() == DefaultVertexFormat.POSITION_COLOR;
                if (source.mode() != VertexFormat.Mode.QUADS || (!wire ? paletteShader == null : wirePaletteShader == null)) break block2;
                if (wire || source.format() == DefaultVertexFormat.BLOCK || source.format() == DefaultVertexFormat.NEW_ENTITY) break block3;
            }
            return SandevistanRenderTypes.map(source);
        }
        return PALETTE_LAYERS.computeIfAbsent(new PaletteLayer(source, color), key -> {
            SandevistanPalette colors = SandevistanPalette.forColor(key.color());
            RenderType.CompositeState.CompositeStateBuilder state = RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(() -> wire ? wirePaletteShader : paletteShader)).setTransparencyState(NO_TRANSPARENCY).setCullState(NO_CULL).setWriteMaskState(COLOR_DEPTH_WRITE);
            if (!wire) {
                ResourceLocation texture = TextureAtlas.LOCATION_BLOCKS;
                if (source instanceof SandevistanRenderTypeAccessor) {
                    SandevistanRenderTypeAccessor access = (SandevistanRenderTypeAccessor)source;
                    SandevistanCompositeAccessor original = (SandevistanCompositeAccessor)access.chrome$state();
                    texture = ((SandevistanTextureAccessor)original.chrome$texture()).chrome$id().orElse(texture);
                }
                state.setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(texture, false, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY);
            }
            RenderType.CompositeRenderType layer = SandevistanRenderTypes.create((String)"chrome_sandy_palette", (VertexFormat)(wire ? DefaultVertexFormat.POSITION_COLOR : DefaultVertexFormat.NEW_ENTITY), (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)1536, (boolean)false, (boolean)false, (RenderType.CompositeState)state.createCompositeState(false));
            return new RenderType("chrome_sandy_palette_" + Integer.toHexString(System.identityHashCode(source)) + "_" + Integer.toHexString(color), layer.format(), layer.mode(), layer.bufferSize(), false, false, () -> SandevistanRenderTypes.lambda$palette$8((RenderType)layer, wire, colors, source), () -> SandevistanRenderTypes.lambda$palette$9((RenderType)layer)){};
        });
    }

    private static void setColor(ShaderInstance shader, String name, int color) {
        shader.safeGetUniform(name).set((float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f);
    }

    static RenderType map(RenderType source) {
        return LAYERS.computeIfAbsent(source, original -> {
            RenderType translucent = original;
            if (source.format() == DefaultVertexFormat.BLOCK) {
                translucent = RenderType.translucent();
            }
            if (source instanceof SandevistanRenderTypeAccessor) {
                SandevistanCompositeAccessor state;
                Optional<ResourceLocation> texture;
                SandevistanRenderTypeAccessor access = (SandevistanRenderTypeAccessor)source;
                if (source.format() == DefaultVertexFormat.NEW_ENTITY && !source.isOutline() && (texture = ((SandevistanTextureAccessor)(state = (SandevistanCompositeAccessor)access.chrome$state()).chrome$texture()).chrome$id()).isPresent()) {
                    translucent = RenderType.entityTranslucent((ResourceLocation)texture.get());
                }
            }
            RenderType layer = translucent;
            return new RenderType("chrome_sandy_echo_" + Integer.toHexString(System.identityHashCode(original)), layer.format(), layer.mode(), layer.bufferSize(), false, layer.sortOnUpload(), () -> {
                layer.setupRenderState();
                if (target != null) {
                    target.bindWrite(false);
                }
            }, () -> {
                layer.clearRenderState();
                Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
            }){};
        });
    }

    private static /* synthetic */ void lambda$palette$9(RenderType layer) {
        layer.clearRenderState();
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
    }

    private static /* synthetic */ void lambda$palette$8(RenderType layer, boolean wire, SandevistanPalette colors, RenderType source) {
        layer.setupRenderState();
        ShaderInstance shader = wire ? wirePaletteShader : paletteShader;
        SandevistanRenderTypes.resetCached(shader);
        SandevistanRenderTypes.setColor(shader, "PaletteShadow", colors.shadow());
        SandevistanRenderTypes.setColor(shader, "PaletteDark", colors.dark());
        SandevistanRenderTypes.setColor(shader, "PaletteMidtone", colors.midtone());
        SandevistanRenderTypes.setColor(shader, "PaletteHighlight", colors.highlight());
        if (!wire) {
            shader.safeGetUniform("BlockShading").set(source.format() == DefaultVertexFormat.BLOCK ? 1.0f : 0.0f);
        }
        if (target != null) {
            target.bindWrite(false);
        }
    }

    static {
        LAYERS = new IdentityHashMap<RenderType, RenderType>();
        PALETTE_LAYERS = new HashMap<PaletteLayer, RenderType>();
        CACHED_PALETTES = new IdentityHashMap<RenderType, RenderType>();
        CACHED_LIVE = new IdentityHashMap<RenderType, RenderType>();
        CACHED_COLORS = new HashMap<Integer, SandevistanPalette>();
        IDENTITY_TRANSFORM = new Matrix4f();
        IDENTITY_NORMAL = new Matrix3f();
    }

    public record SandevistanPalette(int shadow, int dark, int midtone, int highlight) {
        public static SandevistanPalette forColor(int color) {
            float min;
            float red = (float)(color >> 16 & 0xFF) / 255.0f;
            float green = (float)(color >> 8 & 0xFF) / 255.0f;
            float blue = (float)(color & 0xFF) / 255.0f;
            float max = Math.max(red, Math.max(green, blue));
            float span = max - (min = Math.min(red, Math.min(green, blue)));
            float hue = span == 0.0f ? 0.0f : (max == red ? (green - blue) / span / 6.0f : (max == green ? ((blue - red) / span + 2.0f) / 6.0f : ((red - green) / span + 4.0f) / 6.0f));
            return new SandevistanPalette(SandevistanPalette.hsv(hue + 0.015f, 0.85f, 0.18f), SandevistanPalette.hsv(hue, 0.92f, 0.42f), SandevistanPalette.hsv(hue, 0.88f, 0.82f), SandevistanPalette.hsv(hue - 0.035f, 0.55f, 1.0f));
        }

        private static int hsv(float hue, float saturation, float value) {
            return Mth.hsvToRgb((float)(hue - (float)Math.floor(hue)), (float)saturation, (float)value);
        }
    }

    private record PaletteLayer(RenderType source, int color) {
    }
}

