/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.pipeline.TextureTarget
 *  com.mojang.blaze3d.platform.NativeImage
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.client.renderer.entity.RenderLayerParent
 *  net.minecraft.client.renderer.entity.layers.RenderLayer
 *  net.minecraft.client.renderer.entity.player.PlayerRenderer
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.server.packs.resources.ResourceManagerReloadListener
 *  net.minecraft.util.FastColor$ARGB32
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent$Stage
 *  net.neoforged.neoforge.common.NeoForge
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package com.chromeware.client.cyberware;

import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbAttachments;
import com.chromeware.client.cyberware.ImplantTint;
import com.chromeware.client.cyberware.SandevistanGeometry;
import com.chromeware.client.sandevistan.AfterimageRenderContext;
import com.chromeware.client.surgery.SurgeryPropLayer;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.cyberware.SandevistanImplant;
import com.chromeware.cyberware.SandevistanState;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class SandevistanLayer
extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation MESH = ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"models/cyberware/sandevistan_mesh.json");
    private static volatile SandevistanGeometry geometry;
    private static final List<Glow> GLOWS;
    private static ResourceLocation halo;
    private static TextureTarget depth;
    private static RenderType haze;
    private static boolean inLevel;

    public static SandevistanGeometry geometry() {
        return geometry;
    }

    public static void preview(ModelPart body, ModelPart head, PoseStack stack, MultiBufferSource buffers, ResourceLocation texture, int light) {
        if (geometry == null) {
            return;
        }
        geometry.render(stack, body, head, 0.0f, true, buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)texture)), light, OverlayTexture.NO_OVERLAY);
    }

    public static RenderType renderType(Player player) {
        return RenderType.entityCutoutNoCull((ResourceLocation)ImplantTint.texture(player, "sandevistan"));
    }

    public SandevistanLayer(PlayerRenderer renderer) {
        super((RenderLayerParent)renderer);
    }

    public static void registerReloads(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((PreparableReloadListener)((ResourceManagerReloadListener)manager -> {
            try (InputStreamReader reader = new InputStreamReader(manager.getResourceOrThrow(MESH).open(), StandardCharsets.UTF_8);){
                geometry = SandevistanGeometry.load(reader);
            }
            catch (IOException | IllegalArgumentException exception) {
                throw new IllegalStateException("Could not load the Sandevistan mesh", exception);
            }
        }));
    }

    public void render(PoseStack stack, MultiBufferSource buffers, int light, AbstractClientPlayer player, float swing, float amount, float tick, float age, float yaw, float pitch) {
        if (geometry == null || !SandevistanImplant.active((Player)player) || player.isInvisible() || player.isSpectator() || !((PlayerModel)this.getParentModel()).body.visible || SurgeryPropLayer.drawsSandevistan(player)) {
            return;
        }
        PlayerModel model = (PlayerModel)this.getParentModel();
        boolean showHead = model.head.visible && !LimbAttachments.get((Player)player).isMissing(BodyPart.HEAD);
        float dockOffset = model.hat.visible ? 0.009375f : 0.0f;
        geometry.render(stack, model.body, model.head, dockOffset, showHead, buffers.getBuffer(SandevistanLayer.renderType((Player)player)), light, LivingEntityRenderer.getOverlayCoords((LivingEntity)player, (float)0.0f));
        if (((SandevistanState)player.getData(PlayerData.SANDEVISTAN_STATE)).activeTicks() <= 0 || AfterimageRenderContext.isRendering() || !inLevel || buffers != Minecraft.getInstance().renderBuffers().bufferSource() && buffers != Minecraft.getInstance().renderBuffers().outlineBufferSource()) {
            return;
        }
        ResourceLocation lights = ImplantTint.lights((Player)player, "sandevistan");
        if (lights == null) {
            return;
        }
        geometry.render(stack, model.body, model.head, dockOffset, showHead, buffers.getBuffer(RenderType.entityTranslucentEmissive((ResourceLocation)lights)), 0xF000F0, OverlayTexture.NO_OVERLAY);
        Capture capture = new Capture();
        geometry.render(stack, model.body, model.head, dockOffset, showHead, capture, light, OverlayTexture.NO_OVERLAY);
        int color = ImplantTint.lightColor((Player)player, "sandevistan");
        float pulse = 1.0f + 0.12f * (float)Math.sin(age * 0.45f);
        for (Vector3f c : capture.centers()) {
            GLOWS.add(new Glow(c, color, pulse));
        }
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, SandevistanLayer::saveDepth);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, SandevistanLayer::bloom);
    }

    private static void saveDepth(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            inLevel = true;
            GLOWS.clear();
        }
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        inLevel = false;
        if (GLOWS.isEmpty()) {
            return;
        }
        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        if (depth == null) {
            depth = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
        } else if (SandevistanLayer.depth.width != main.width || SandevistanLayer.depth.height != main.height) {
            depth.resize(main.width, main.height, Minecraft.ON_OSX);
        }
        depth.copyDepthFrom(main);
        main.bindWrite(false);
    }

    private static void bloom(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL || GLOWS.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();
        if (depth != null) {
            main.copyDepthFrom((RenderTarget)depth);
        }
        main.bindWrite(false);
        Matrix4fStack view = RenderSystem.getModelViewStack();
        view.pushMatrix();
        view.mul((Matrix4fc)event.getModelViewMatrix());
        RenderSystem.applyModelViewMatrix();
        Camera camera = event.getCamera();
        MultiBufferSource.BufferSource source = mc.renderBuffers().bufferSource();
        RenderType type = RenderType.eyes((ResourceLocation)SandevistanLayer.halo());
        if (haze == null) {
            haze = new RenderType("chromeware_sandevistan_haze", type.format(), type.mode(), 1536, false, false, () -> {
                type.setupRenderState();
                RenderSystem.disableDepthTest();
            }, () -> {
                RenderSystem.enableDepthTest();
                type.clearRenderState();
            }){};
        }
        VertexConsumer buffer = source.getBuffer(haze);
        for (Glow glow : GLOWS) {
            SandevistanLayer.halo(buffer, camera, glow.center, 1.1f, SandevistanLayer.tint(SandevistanLayer.saturate(glow.color), 0.09f * glow.pulse));
        }
        source.endBatch(haze);
        buffer = source.getBuffer(type);
        for (Glow glow : GLOWS) {
            Vector3f c = glow.center;
            if (c.lengthSquared() > 1.0E-6f) {
                c.sub((Vector3fc)new Vector3f((Vector3fc)c).normalize().mul(0.15f));
            }
            SandevistanLayer.halo(buffer, camera, c, 0.5f, SandevistanLayer.tint(SandevistanLayer.saturate(glow.color), 0.38f * glow.pulse));
            SandevistanLayer.halo(buffer, camera, c, 0.11f, SandevistanLayer.tint(glow.color, 0.75f * glow.pulse));
        }
        source.endBatch(type);
        view.popMatrix();
        RenderSystem.applyModelViewMatrix();
        GLOWS.clear();
    }

    private static int saturate(int color) {
        int r = FastColor.ARGB32.red((int)color);
        int g = FastColor.ARGB32.green((int)color);
        int b = FastColor.ARGB32.blue((int)color);
        int max = Math.max(r, Math.max(g, b));
        return Math.max(0, max - (max - r) * 2) << 16 | Math.max(0, max - (max - g) * 2) << 8 | Math.max(0, max - (max - b) * 2);
    }

    private static int tint(int color, float strength) {
        return 0xFF000000 | Math.min(255, (int)((float)FastColor.ARGB32.red((int)color) * strength)) << 16 | Math.min(255, (int)((float)FastColor.ARGB32.green((int)color) * strength)) << 8 | Math.min(255, (int)((float)FastColor.ARGB32.blue((int)color) * strength));
    }

    private static void halo(VertexConsumer buffer, Camera camera, Vector3f c, float size, int argb) {
        Vector3f left = new Vector3f((Vector3fc)camera.getLeftVector()).mul(size);
        Vector3f up = new Vector3f((Vector3fc)camera.getUpVector()).mul(size);
        SandevistanLayer.corner(buffer, c, left, up, 1.0f, 1.0f, 0.0f, 0.0f, argb);
        SandevistanLayer.corner(buffer, c, left, up, 1.0f, -1.0f, 0.0f, 1.0f, argb);
        SandevistanLayer.corner(buffer, c, left, up, -1.0f, -1.0f, 1.0f, 1.0f, argb);
        SandevistanLayer.corner(buffer, c, left, up, -1.0f, 1.0f, 1.0f, 0.0f, argb);
        SandevistanLayer.corner(buffer, c, left, up, -1.0f, 1.0f, 1.0f, 0.0f, argb);
        SandevistanLayer.corner(buffer, c, left, up, -1.0f, -1.0f, 1.0f, 1.0f, argb);
        SandevistanLayer.corner(buffer, c, left, up, 1.0f, -1.0f, 0.0f, 1.0f, argb);
        SandevistanLayer.corner(buffer, c, left, up, 1.0f, 1.0f, 0.0f, 0.0f, argb);
    }

    private static void corner(VertexConsumer buffer, Vector3f c, Vector3f left, Vector3f up, float l, float u, float tu, float tv, int argb) {
        buffer.addVertex(c.x + left.x * l + up.x * u, c.y + left.y * l + up.y * u, c.z + left.z * l + up.z * u, argb, tu, tv, OverlayTexture.NO_OVERLAY, 0xF000F0, 0.0f, 1.0f, 0.0f);
    }

    private static ResourceLocation halo() {
        if (halo != null) {
            return halo;
        }
        NativeImage image = new NativeImage(32, 32, false);
        for (int y = 0; y < 32; ++y) {
            for (int x = 0; x < 32; ++x) {
                float d = (float)Math.hypot((double)x - 15.5, (double)y - 15.5) / 16.0f;
                float f = Math.max(0.0f, 1.0f - d);
                int v = Math.min(255, (int)(255.0f * (f * f * f * 0.7f + (float)Math.pow(f, 12.0) * 0.6f)));
                image.setPixelRGBA(x, y, 0xFF000000 | v << 16 | v << 8 | v);
            }
        }
        halo = ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"sandevistan_halo");
        Minecraft.getInstance().getTextureManager().register(halo, (AbstractTexture)new DynamicTexture(image));
        return halo;
    }

    static {
        GLOWS = new ArrayList<Glow>();
    }

    private static final class Capture
    implements VertexConsumer {
        private final List<float[]> vertices = new ArrayList<float[]>();

        private Capture() {
        }

        public void addVertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float nx, float ny, float nz) {
            this.vertices.add(new float[]{x, y, z, u, v});
        }

        public VertexConsumer addVertex(float x, float y, float z) {
            return this;
        }

        public VertexConsumer setColor(int r, int g, int b, int a) {
            return this;
        }

        public VertexConsumer setUv(float u, float v) {
            return this;
        }

        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        public VertexConsumer setNormal(float x, float y, float z) {
            return this;
        }

        List<Vector3f> centers() {
            ArrayList<Vector3f> centers = new ArrayList<Vector3f>();
            int i = 0;
            while (i + 3 < this.vertices.size()) {
                float u0 = 1.0f;
                float v0 = 1.0f;
                float u1 = 0.0f;
                float v1 = 0.0f;
                Vector3f center = new Vector3f();
                for (int j = i; j < i + 4; ++j) {
                    float[] p = this.vertices.get(j);
                    center.add(p[0] / 4.0f, p[1] / 4.0f, p[2] / 4.0f);
                    u0 = Math.min(u0, p[3]);
                    v0 = Math.min(v0, p[4]);
                    u1 = Math.max(u1, p[3]);
                    v1 = Math.max(v1, p[4]);
                }
                if (ImplantTint.lit("sandevistan", u0, v0, u1, v1) && centers.stream().noneMatch(c -> c.distanceSquared((Vector3fc)center) < 0.0064f)) {
                    centers.add(center);
                }
                i += 4;
            }
            return centers;
        }
    }

    private record Glow(Vector3f center, int color, float pulse) {
    }
}

