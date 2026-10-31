/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  org.joml.Matrix4f
 */
package com.chromeware.client.humanity;

import com.chromeware.client.humanity.SyringeAnimation;
import com.chromeware.humanity.EmptySyringeItem;
import com.chromeware.humanity.ImmunosuppressantItem;
import com.chromeware.humanity.SyringeInjection;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public final class SyringeLiquidRenderer {
    private static final float MIN = 0.4375f;
    private static final float MAX = 0.5625f;
    private static final float BOTTOM = 0.375f;
    private static final float FULL_TOP = 0.875f;
    private static final float EPSILON = 2.4414062E-4f;

    public static boolean isSyringe(ItemStack stack) {
        return stack.getItem() instanceof ImmunosuppressantItem || stack.getItem() instanceof EmptySyringeItem;
    }

    public static void render(ItemStack stack, LivingEntity holder, PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        boolean active;
        if (!SyringeLiquidRenderer.isSyringe(stack)) {
            return;
        }
        boolean empty = stack.getItem() instanceof EmptySyringeItem;
        float fill = empty ? 0.0f : 1.0f;
        boolean bl = active = SyringeInjection.hasActiveVisual(stack) && holder instanceof AbstractClientPlayer;
        if (active) {
            fill = 1.0f - SyringeAnimation.drain((AbstractClientPlayer)holder);
        }
        if (fill <= 2.4414062E-4f) {
            return;
        }
        ResourceLocation key = BuiltInRegistries.ITEM.getKey((Object)stack.getItem());
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)("item/" + key.getPath()));
        TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(texture);
        VertexConsumer vertices = ItemRenderer.getFoilBufferDirect((MultiBufferSource)buffers, (RenderType)RenderType.entityTranslucentCull((ResourceLocation)TextureAtlas.LOCATION_BLOCKS), (boolean)true, (boolean)stack.hasFoil());
        SyringeLiquidRenderer.drawCuboid(poses.last(), vertices, sprite, Math.min(1.0f, fill), light, overlay);
    }

    private static void drawCuboid(PoseStack.Pose pose, VertexConsumer out, TextureAtlasSprite sprite, float fill, int light, int overlay) {
        float top = 0.375f + 0.5f * fill;
        float sideTopV = 4.0f * (1.0f - fill);
        SyringeLiquidRenderer.quad(pose, out, sprite, 0.5625f, 0.375f, 0.4375f, 0.4375f, 0.375f, 0.4375f, 0.4375f, top, 0.4375f, 0.5625f, top, 0.4375f, 0.0f, 4.0f, 1.0f, sideTopV, 0.0f, 0.0f, -1.0f, light, overlay);
        SyringeLiquidRenderer.quad(pose, out, sprite, 0.5625f, 0.375f, 0.5625f, 0.5625f, 0.375f, 0.4375f, 0.5625f, top, 0.4375f, 0.5625f, top, 0.5625f, 1.0f, 4.0f, 2.0f, sideTopV, 1.0f, 0.0f, 0.0f, light, overlay);
        SyringeLiquidRenderer.quad(pose, out, sprite, 0.4375f, 0.375f, 0.5625f, 0.5625f, 0.375f, 0.5625f, 0.5625f, top, 0.5625f, 0.4375f, top, 0.5625f, 2.0f, 4.0f, 3.0f, sideTopV, 0.0f, 0.0f, 1.0f, light, overlay);
        SyringeLiquidRenderer.quad(pose, out, sprite, 0.4375f, 0.375f, 0.4375f, 0.4375f, 0.375f, 0.5625f, 0.4375f, top, 0.5625f, 0.4375f, top, 0.4375f, 3.0f, 4.0f, 4.0f, sideTopV, -1.0f, 0.0f, 0.0f, light, overlay);
        SyringeLiquidRenderer.quad(pose, out, sprite, 0.4375f, top, 0.5625f, 0.5625f, top, 0.5625f, 0.5625f, top, 0.4375f, 0.4375f, top, 0.4375f, 5.0f, 7.0f, 4.0f, 6.0f, 0.0f, 1.0f, 0.0f, light, overlay);
        SyringeLiquidRenderer.quad(pose, out, sprite, 0.4375f, 0.375f, 0.4375f, 0.5625f, 0.375f, 0.4375f, 0.5625f, 0.375f, 0.5625f, 0.4375f, 0.375f, 0.5625f, 5.0f, 7.0f, 4.0f, 8.0f, 0.0f, -1.0f, 0.0f, light, overlay);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer out, TextureAtlasSprite sprite, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float u0, float v0, float u1, float v1, float nx, float ny, float nz, int light, int overlay) {
        SyringeLiquidRenderer.vertex(out, pose, sprite, x0, y0, z0, u0, v0, nx, ny, nz, light, overlay);
        SyringeLiquidRenderer.vertex(out, pose, sprite, x1, y1, z1, u1, v0, nx, ny, nz, light, overlay);
        SyringeLiquidRenderer.vertex(out, pose, sprite, x2, y2, z2, u1, v1, nx, ny, nz, light, overlay);
        SyringeLiquidRenderer.vertex(out, pose, sprite, x3, y3, z3, u0, v1, nx, ny, nz, light, overlay);
    }

    private static void vertex(VertexConsumer out, PoseStack.Pose pose, TextureAtlasSprite sprite, float x, float y, float z, float u, float v, float nx, float ny, float nz, int light, int overlay) {
        Matrix4f position = pose.pose();
        out.addVertex(position, x, y, z).setColor(255, 255, 255, 255).setUv(sprite.getU(u / 16.0f), sprite.getV(v / 16.0f)).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
    }
}

