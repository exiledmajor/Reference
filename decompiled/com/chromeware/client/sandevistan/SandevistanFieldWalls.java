/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent
 *  org.joml.Matrix4f
 */
package com.chromeware.client.sandevistan;

import com.chromeware.client.sandevistan.SandevistanClient;
import com.chromeware.sandevistan.TimeField;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

final class SandevistanFieldWalls {
    SandevistanFieldWalls() {
    }

    static void render(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        RenderType type = RenderType.debugQuads();
        VertexConsumer vertices = buffers.getBuffer(type);
        Matrix4f matrix = event.getPoseStack().last().pose();
        Vec3 camera = event.getCamera().getPosition();
        ArrayList<FieldShell> shells = new ArrayList<FieldShell>();
        for (TimeField f : SandevistanClient.fieldViews()) {
            Player owner = mc.level.getPlayerByUUID(f.owner());
            Vec3 center = owner == null ? new Vec3(f.x(), f.y(), f.z()) : owner.getPosition(event.getPartialTick().getGameTimeDeltaPartialTick(false));
            shells.add(new FieldShell(center.x - f.radius(), center.x + f.radius(), center.z - f.radius(), center.z + f.radius()));
        }
        int alpha = Math.round(115.0f * (0.72f + 0.18f * (float)Math.sin((double)((float)mc.level.getGameTime() + event.getPartialTick().getGameTimeDeltaPartialTick(false)) * 0.18)));
        for (FieldBoundary edge : SandevistanFieldWalls.unionBoundary(shells)) {
            SandevistanFieldWalls.drawWall(matrix, vertices, edge.x1, edge.z1, edge.x2, edge.z2, mc.level.getMinBuildHeight(), mc.level.getMaxBuildHeight(), camera, alpha);
        }
        buffers.endBatch(type);
    }

    private static List<FieldBoundary> unionBoundary(List<FieldShell> shells) {
        if (shells.isEmpty()) {
            return List.of();
        }
        List<Double> xCoordinates = new ArrayList<Double>(shells.size() * 2);
        List<Double> zCoordinates = new ArrayList(shells.size() * 2);
        for (FieldShell fieldShell : shells) {
            xCoordinates.add(fieldShell.minX);
            xCoordinates.add(fieldShell.maxX);
            zCoordinates.add(fieldShell.minZ);
            zCoordinates.add(fieldShell.maxZ);
        }
        xCoordinates = xCoordinates.stream().distinct().sorted().toList();
        zCoordinates = zCoordinates.stream().distinct().sorted().toList();
        boolean[][] occupied = new boolean[xCoordinates.size() - 1][zCoordinates.size() - 1];
        for (FieldShell shell : shells) {
            int minimumX = Collections.binarySearch(xCoordinates, shell.minX);
            int maximumX = Collections.binarySearch(xCoordinates, shell.maxX);
            int minimumZ = Collections.binarySearch(zCoordinates, shell.minZ);
            int maximumZ = Collections.binarySearch(zCoordinates, shell.maxZ);
            for (int x = minimumX; x < maximumX; ++x) {
                for (int z = minimumZ; z < maximumZ; ++z) {
                    occupied[x][z] = true;
                }
            }
        }
        ArrayList<FieldBoundary> arrayList = new ArrayList<FieldBoundary>();
        for (int x = 0; x < occupied.length; ++x) {
            for (int z = 0; z < occupied[x].length; ++z) {
                if (!occupied[x][z]) continue;
                double minX = (Double)xCoordinates.get(x);
                double maxX = (Double)xCoordinates.get(x + 1);
                double minZ = (Double)zCoordinates.get(z);
                double maxZ = (Double)zCoordinates.get(z + 1);
                if (x == 0 || !occupied[x - 1][z]) {
                    arrayList.add(new FieldBoundary(minX, maxZ, minX, minZ));
                }
                if (x == occupied.length - 1 || !occupied[x + 1][z]) {
                    arrayList.add(new FieldBoundary(maxX, minZ, maxX, maxZ));
                }
                if (z == 0 || !occupied[x][z - 1]) {
                    arrayList.add(new FieldBoundary(minX, minZ, maxX, minZ));
                }
                if (z != occupied[x].length - 1 && occupied[x][z + 1]) continue;
                arrayList.add(new FieldBoundary(maxX, maxZ, minX, maxZ));
            }
        }
        return arrayList;
    }

    private static void drawWall(Matrix4f matrix, VertexConsumer vertices, double x1, double z1, double x2, double z2, double minY, double maxY, Vec3 camera, int alpha) {
        float ax = (float)(x1 - camera.x);
        float az = (float)(z1 - camera.z);
        float bx = (float)(x2 - camera.x);
        float bz = (float)(z2 - camera.z);
        float bottom = (float)(minY - camera.y);
        float top = (float)(maxY - camera.y);
        vertices.addVertex(matrix, ax, bottom, az).setColor(45, 232, 190, alpha);
        vertices.addVertex(matrix, bx, bottom, bz).setColor(45, 232, 190, alpha);
        vertices.addVertex(matrix, bx, top, bz).setColor(150, 250, 220, alpha);
        vertices.addVertex(matrix, ax, top, az).setColor(150, 250, 220, alpha);
    }

    private record FieldShell(double minX, double maxX, double minZ, double maxZ) {
    }

    private record FieldBoundary(double x1, double z1, double x2, double z2) {
    }
}

