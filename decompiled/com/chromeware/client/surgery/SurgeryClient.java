/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.CameraType
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.ModelBlockRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.NoopRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.ModelResourceLocation
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.AABB
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  net.neoforged.neoforge.common.NeoForge
 */
package com.chromeware.client.surgery;

import com.chromeware.client.surgery.SurgeryAnimation;
import com.chromeware.surgery.SurgeryBlocks;
import com.chromeware.surgery.SurgeryChairBlock;
import com.chromeware.surgery.SurgeryChairBlockEntity;
import com.chromeware.surgery.SurgerySeat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.NeoForge;

public final class SurgeryClient {
    private static boolean forcedThirdPerson;

    public static void register(IEventBus modBus) {
        SurgeryAnimation.register();
        modBus.addListener(event -> {
            event.registerEntityRenderer((EntityType)SurgeryBlocks.SEAT.get(), NoopRenderer::new);
            event.registerBlockEntityRenderer((BlockEntityType)SurgeryBlocks.CHAIR_ENTITY.get(), SurgeryChairRenderer::new);
        });
        modBus.addListener(event -> event.register(SurgeryChairRenderer.BACKREST));
        NeoForge.EVENT_BUS.addListener(SurgeryClient::tick);
    }

    private static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) {
            return;
        }
        for (AbstractClientPlayer player : mc.level.players()) {
            SurgeryAnimation.update(player);
        }
        SurgeryClient.camera(mc);
    }

    private static void camera(Minecraft mc) {
        SurgerySeat seat;
        Entity entity;
        boolean operating;
        boolean bl = operating = mc.player != null && (entity = mc.player.getVehicle()) instanceof SurgerySeat && (seat = (SurgerySeat)entity).busy();
        if (operating && mc.options.getCameraType().isFirstPerson()) {
            SurgeryClient.setCamera(mc, CameraType.THIRD_PERSON_BACK);
            forcedThirdPerson = true;
        } else if (!operating && forcedThirdPerson) {
            forcedThirdPerson = false;
            if (!mc.options.getCameraType().isFirstPerson()) {
                SurgeryClient.setCamera(mc, CameraType.FIRST_PERSON);
            }
        }
    }

    private static void setCamera(Minecraft mc, CameraType type) {
        CameraType previous = mc.options.getCameraType();
        mc.options.setCameraType(type);
        if (previous.isFirstPerson() != type.isFirstPerson()) {
            mc.gameRenderer.checkEntityPostEffect(type.isFirstPerson() ? mc.getCameraEntity() : null);
        }
        mc.levelRenderer.needsUpdate();
    }

    public static float degrees(SurgerySeat seat, float partialTick) {
        return seat.recline(partialTick);
    }

    public static class SurgeryChairRenderer
    implements BlockEntityRenderer<SurgeryChairBlockEntity> {
        public static final ModelResourceLocation BACKREST = ModelResourceLocation.standalone((ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"block/surgery_chair_backrest"));
        private static final float HINGE_Y = 0.625f;
        private static final float HINGE_Z = 0.0f;

        public SurgeryChairRenderer(BlockEntityRendererProvider.Context context) {
        }

        public void render(SurgeryChairBlockEntity chair, float partialTick, PoseStack stack, MultiBufferSource buffers, int light, int overlay) {
            BlockState state = chair.getBlockState();
            if (!(state.getBlock() instanceof SurgeryChairBlock)) {
                return;
            }
            SurgerySeat seat = chair.seat();
            float degrees = seat == null ? 0.0f : SurgeryClient.degrees(seat, partialTick);
            float yRot = (((Direction)state.getValue((Property)SurgeryChairBlock.FACING)).toYRot() + 180.0f) % 360.0f;
            stack.pushPose();
            stack.rotateAround(Axis.YP.rotationDegrees(-yRot), 0.5f, 0.5f, 0.5f);
            stack.rotateAround(Axis.XN.rotationDegrees(degrees), 0.5f, 0.625f, 0.0f);
            ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
            VertexConsumer buffer = buffers.getBuffer(RenderType.cutout());
            BakedModel model = Minecraft.getInstance().getModelManager().getModel(BACKREST);
            renderer.renderModel(stack.last(), buffer, state, model, 1.0f, 1.0f, 1.0f, light, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
            stack.popPose();
        }

        public AABB getRenderBoundingBox(SurgeryChairBlockEntity chair) {
            return new AABB(chair.getBlockPos()).inflate(2.0);
        }
    }
}

