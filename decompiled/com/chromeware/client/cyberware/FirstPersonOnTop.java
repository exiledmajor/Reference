/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.Lighting
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderDispatcher
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 */
package com.chromeware.client.cyberware;

import com.chromeware.client.monowire.MonowireClient;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;

public final class FirstPersonOnTop {
    private static Entity deferred;
    private static float tick;
    private static boolean drawing;

    public static boolean defer(Entity entity, float partialTick) {
        if (drawing || !FirstPersonMode.isFirstPersonPass() || entity != Minecraft.getInstance().getCameraEntity()) {
            return false;
        }
        deferred = entity;
        tick = partialTick;
        FirstPersonMode.setFirstPersonPass((boolean)false);
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void draw(Camera camera, Matrix4f cameraRotation) {
        Entity entity = deferred;
        deferred = null;
        if (entity == null || !entity.isAlive()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix().mul((Matrix4fc)cameraRotation);
        RenderSystem.applyModelViewMatrix();
        mc.gameRenderer.lightTexture().turnOnLightLayer();
        Lighting.setupLevel();
        drawing = true;
        FirstPersonMode.setFirstPersonPass((boolean)true);
        try {
            Vec3 cam = camera.getPosition();
            double x = Mth.lerp((double)tick, (double)entity.xOld, (double)entity.getX()) - cam.x;
            double y = Mth.lerp((double)tick, (double)entity.yOld, (double)entity.getY()) - cam.y;
            double z = Mth.lerp((double)tick, (double)entity.zOld, (double)entity.getZ()) - cam.z;
            float yaw = Mth.lerp((float)tick, (float)entity.yRotO, (float)entity.getYRot());
            dispatcher.render(entity, x, y, z, yaw, tick, new PoseStack(), (MultiBufferSource)buffers, dispatcher.getPackedLightCoords(entity, tick));
            buffers.endBatch();
            MonowireClient.firstPerson(camera, tick, new PoseStack());
        }
        finally {
            FirstPersonMode.setFirstPersonPass((boolean)false);
            drawing = false;
            modelView.popMatrix();
            RenderSystem.applyModelViewMatrix();
        }
    }
}

