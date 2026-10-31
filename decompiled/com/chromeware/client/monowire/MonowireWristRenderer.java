/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.RenderLayerParent
 *  net.minecraft.client.renderer.entity.layers.RenderLayer
 *  net.minecraft.client.renderer.entity.player.PlayerRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.server.packs.resources.ResourceManagerReloadListener
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent
 *  org.joml.Vector3f
 */
package com.chromeware.client.monowire;

import com.chromeware.client.body.CyberLimbRenderer;
import com.chromeware.client.cyberware.CyberwareRig;
import com.chromeware.client.cyberware.ImplantTint;
import com.chromeware.client.monowire.MonowireAnimation;
import com.chromeware.client.monowire.MonowireCableRenderer;
import com.chromeware.client.monowire.MonowireClient;
import com.chromeware.compat.HemodynamicsCompatibility;
import com.chromeware.monowire.MonowireImplant;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.joml.Vector3f;

public final class MonowireWristRenderer {
    public static final float OUTLET_X = 2.3f;
    public static final float OUTLET_Y = 7.85f;
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"textures/entity/monowire.png");
    private static final ResourceLocation MESH = ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"models/cyberware/monowire_rig.json");
    private static CyberwareRig geometry;

    public static void reload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((PreparableReloadListener)((ResourceManagerReloadListener)manager -> {
            try (InputStreamReader reader = new InputStreamReader(manager.getResourceOrThrow(MESH).open(), StandardCharsets.UTF_8);){
                geometry = CyberwareRig.load(reader);
            }
            catch (Exception e) {
                throw new IllegalStateException("Could not load Monowire wrist mesh", e);
            }
        }));
    }

    public static void render(AbstractClientPlayer player, HumanoidArm side, ModelPart arm, PoseStack stack, MultiBufferSource buffers, int light, boolean firstPerson) {
        HemodynamicsCompatibility.arm((LivingEntity)player, side, false, () -> MonowireWristRenderer.drawArm(player, side, arm, stack, buffers, light, firstPerson));
    }

    private static void drawArm(AbstractClientPlayer player, HumanoidArm side, ModelPart arm, PoseStack stack, MultiBufferSource buffers, int light, boolean firstPerson) {
        if (geometry == null || !arm.visible || !MonowireImplant.hasHardware((Player)player, side) || player.isInvisible() || player.isSpectator()) {
            return;
        }
        float[] center = new float[]{(float)(side == HumanoidArm.LEFT ? 1 : -1) / 16.0f};
        boolean[] slim = new boolean[]{false};
        arm.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (path.isEmpty() && index == 0) {
                center[0] = (cube.minX + cube.maxX) / 32.0f;
                slim[0] = cube.maxX - cube.minX < 3.5f;
            }
        });
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)ImplantTint.texture((Player)player, "monowire")));
        Function<String, CyberwareRig.Transform> animation = name -> MonowireAnimation.transform(player, side, name);
        Map<String, Vec3> points = CyberLimbRenderer.chrome(player, side) ? geometry.render(side == HumanoidArm.LEFT, slim[0], arm, center[0], stack, animation, buffer, light, OverlayTexture.NO_OVERLAY, MonowireWristRenderer::chromePart, 0.0f) : geometry.render(side == HumanoidArm.LEFT, slim[0], arm, center[0], stack, animation, buffer, light, OverlayTexture.NO_OVERLAY);
        MonowireClient.capture(player, side, points.get("wire_outlet"), firstPerson, false);
    }

    static boolean chromePart(String name) {
        return name.equals("monowire_feed") || name.equals("monowire_gate");
    }

    public static void previewArm(ModelPart arm, ModelPart sleeve, boolean slim, PoseStack stack, MultiBufferSource buffers, ResourceLocation texture, ResourceLocation skinTexture, Function<String, CyberwareRig.Transform> animation, int light, Vec3 origin, int wire) {
        if (geometry == null) {
            return;
        }
        VertexConsumer skin = buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)skinTexture));
        arm.render(stack, skin, light, OverlayTexture.NO_OVERLAY);
        sleeve.render(stack, skin, light, OverlayTexture.NO_OVERLAY);
        float[] center = new float[]{-0.0625f};
        arm.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (path.isEmpty() && index == 0) {
                center[0] = (cube.minX + cube.maxX) / 32.0f;
            }
        });
        Map<String, Vec3> points = geometry.render(false, slim, arm, center[0], stack, animation, buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)texture)), light, OverlayTexture.NO_OVERLAY);
        boolean open = animation.apply((String)"monowire_gate").position().y < -0.015625f;
        Dangle.draw(open ? points.get("wire_outlet") : null, origin, stack.last().pose().getScale((Vector3f)new Vector3f()).y, buffers, wire);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Vec3 socket(boolean left, boolean slim, float center, ModelPart arm, PoseStack stack) {
        stack.pushPose();
        try {
            arm.translateAndRotate(stack);
            stack.translate(center, 0.0f, 0.0f);
            stack.scale((float)(left ? -1 : 1) * (slim ? 0.75f : 1.0f), 1.0f, 1.0f);
            Vector3f p = stack.last().pose().transformPosition(new Vector3f(0.14375f, 0.490625f, 0.0f));
            Vec3 vec3 = new Vec3((double)p.x, (double)p.y, (double)p.z);
            return vec3;
        }
        finally {
            stack.popPose();
        }
    }

    public static void renderPair(AbstractClientPlayer player, HumanoidModel<?> model, PoseStack stack, MultiBufferSource buffers, int light) {
        MonowireWristRenderer.render(player, HumanoidArm.RIGHT, model.rightArm, stack, buffers, light, false);
        MonowireWristRenderer.render(player, HumanoidArm.LEFT, model.leftArm, stack, buffers, light, false);
        MonowireWristRenderer.captureGrips(player, model, stack);
    }

    public static void captureGrips(AbstractClientPlayer player, HumanoidModel<?> model, PoseStack stack) {
        for (HumanoidArm side : HumanoidArm.values()) {
            if (!MonowireAnimation.rendering(player, side)) continue;
            ModelPart off = side == HumanoidArm.LEFT ? model.rightArm : model.leftArm;
            MonowireClient.capture(player, side, Layer.grip(off, stack), false, true);
        }
    }

    private static final class Dangle {
        private static final int SEGMENTS = 12;
        private static final float LENGTH = 0.40625f;
        private static final float GRAVITY = 9.0f;
        private static final float STEP = 0.004166667f;
        private static final float FEED = 0.3f;
        private static Vec3[] now;
        private static Vec3[] before;
        private static long last;
        private static float fed;
        private static float spare;

        private Dangle() {
        }

        static void draw(Vec3 outlet, Vec3 origin, float unit, MultiBufferSource buffers, int rgb) {
            if (outlet == null) {
                now = null;
                return;
            }
            Vec3 anchor = outlet.subtract(origin).scale((double)(1.0f / unit));
            long time = System.nanoTime();
            if (now == null) {
                now = new Vec3[13];
                Arrays.fill(now, anchor);
                before = (Vec3[])now.clone();
                spare = 0.0f;
                fed = 0.0f;
                last = time;
            }
            spare = Math.min(0.1f, spare + (float)(time - last) / 1.0E9f);
            last = time;
            while (spare >= 0.004166667f) {
                Dangle.step(anchor);
                spare -= 0.004166667f;
            }
            Dangle.now[0] = anchor;
            Vec3[] points = new Vec3[13];
            for (int i = 0; i <= 12; ++i) {
                points[i] = origin.add(now[i].scale((double)unit));
            }
            MonowireCableRenderer.flat(new PoseStack().last(), buffers.getBuffer(MonowireCableRenderer.TYPE), points, unit * 2.5f, rgb);
        }

        private static void step(Vec3 anchor) {
            fed = Math.min(1.0f, fed + 0.013888889f);
            for (int i = 1; i <= 12; ++i) {
                Vec3 p = now[i];
                Dangle.now[i] = p.add(p.subtract(before[i]).scale(0.997)).add(0.0, 1.5625001105945557E-4, 0.0);
                Dangle.before[i] = p;
            }
            Dangle.now[0] = Dangle.before[0] = anchor;
            double rest = 0.40625f * fed / 12.0f;
            for (int k = 0; k < 4; ++k) {
                for (int i = 0; i < 12; ++i) {
                    Vec3 d = now[i + 1].subtract(now[i]);
                    double length = d.length();
                    if (length < 1.0E-6) continue;
                    Vec3 fix = d.scale((length - rest) / length);
                    if (i == 0) {
                        Dangle.now[1] = now[1].subtract(fix);
                        continue;
                    }
                    Dangle.now[i] = now[i].add(fix.scale(0.5));
                    Dangle.now[i + 1] = now[i + 1].subtract(fix.scale(0.5));
                }
            }
        }
    }

    public static final class Layer
    extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        public Layer(PlayerRenderer renderer) {
            super((RenderLayerParent)renderer);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public static Vec3 grip(ModelPart arm, PoseStack stack) {
            stack.pushPose();
            try {
                arm.translateAndRotate(stack);
                float[] center = new float[]{(arm.x > 0.0f ? 1.0f : -1.0f) / 16.0f};
                arm.visit(new PoseStack(), (pose, path, index, cube) -> {
                    if (path.isEmpty() && index == 0) {
                        center[0] = (cube.minX + cube.maxX) / 32.0f;
                    }
                });
                boolean left = arm.x > 0.0f;
                float[] width = new float[]{1.0f};
                arm.visit(new PoseStack(), (pose, path, index, cube) -> {
                    if (path.isEmpty() && index == 0) {
                        width[0] = cube.maxX - cube.minX < 3.5f ? 0.75f : 1.0f;
                    }
                });
                Vector3f p = stack.last().pose().transformPosition(new Vector3f(center[0] + (float)(left ? -1 : 1) * 1.95f * width[0] / 16.0f, 0.5625f, -0.040625f));
                Vec3 vec3 = new Vec3((double)p.x, (double)p.y, (double)p.z);
                return vec3;
            }
            finally {
                stack.popPose();
            }
        }

        public void render(PoseStack stack, MultiBufferSource buffers, int light, AbstractClientPlayer player, float swing, float amount, float tick, float age, float yaw, float pitch) {
            MonowireWristRenderer.renderPair(player, (HumanoidModel)this.getParentModel(), stack, buffers, light);
        }
    }
}

