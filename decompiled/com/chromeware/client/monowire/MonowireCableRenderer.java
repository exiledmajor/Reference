/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 */
package com.chromeware.client.monowire;

import com.chromeware.client.sandevistan.AfterimageRenderContext;
import com.chromeware.cyberware.ImplantColors;
import com.chromeware.cyberware.PlayerData;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class MonowireCableRenderer
extends RenderType {
    public static final int WIRE = 16729376;
    public static final RenderType TYPE = MonowireCableRenderer.create((String)"chromeware_monowire", (VertexFormat)DefaultVertexFormat.POSITION_COLOR, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)1536, (boolean)false, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.builder().setShaderState(RENDERTYPE_LIGHTNING_SHADER).setTransparencyState(LIGHTNING_TRANSPARENCY).setCullState(NO_CULL).setWriteMaskState(COLOR_WRITE).createCompositeState(false));

    private MonowireCableRenderer() {
        super("unused", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 1536, false, false, () -> {}, () -> {});
    }

    public static int color(Player player) {
        Integer color = ((ImplantColors)player.getData(PlayerData.IMPLANT_COLORS)).get("monowire", "wire");
        return color == null ? -1 : color;
    }

    public static void curve(PoseStack.Pose pose, VertexConsumer buffer, Vec3[] points, Vec3 camera) {
        MonowireCableRenderer.curve(pose, buffer, points, camera, 0, points.length - 1, -1);
    }

    public static void curve(PoseStack.Pose pose, VertexConsumer buffer, Vec3[] points, Vec3 camera, int from, int to, int rgb) {
        Vec3[] sides = new Vec3[points.length];
        for (int i = 0; i < points.length; ++i) {
            Vec3 tangent = points[Math.min(i + 1, points.length - 1)].subtract(points[Math.max(i - 1, 0)]).normalize();
            Vec3 side = tangent.cross(camera.subtract(points[i])).normalize();
            if (side.lengthSqr() < 0.5) {
                Vec3 vec3 = side = i > 0 ? sides[i - 1] : new Vec3(1.0, 0.0, 0.0);
            }
            if (i > 0 && side.dot(sides[i - 1]) < 0.0) {
                side = side.scale(-1.0);
            }
            sides[i] = side;
        }
        MonowireCableRenderer.layers(pose, buffer, points, sides, camera, from, to, 1.0, rgb);
    }

    public static void flat(PoseStack.Pose pose, VertexConsumer buffer, Vec3[] points, double width, int rgb) {
        Vec3[] sides = new Vec3[points.length];
        for (int i = 0; i < points.length; ++i) {
            Vec3 side = points[Math.min(i + 1, points.length - 1)].subtract(points[Math.max(i - 1, 0)]).cross(new Vec3(0.0, 0.0, 1.0)).normalize();
            sides[i] = side.lengthSqr() < 0.5 ? (i > 0 ? sides[i - 1] : new Vec3(1.0, 0.0, 0.0)) : side;
        }
        MonowireCableRenderer.layers(pose, buffer, points, sides, Vec3.ZERO, 0, points.length - 1, width, rgb);
    }

    private static void layers(PoseStack.Pose pose, VertexConsumer buffer, Vec3[] points, Vec3[] sides, Vec3 camera, int from, int to, double width, int rgb) {
        int[] colors = MonowireCableRenderer.colors(rgb);
        MonowireCableRenderer.ribbon(pose, buffer, points, sides, camera, from, to, 0.011 * width, colors[0]);
        MonowireCableRenderer.ribbon(pose, buffer, points, sides, camera, from, to, 0.0048 * width, colors[1]);
        MonowireCableRenderer.ribbon(pose, buffer, points, sides, camera, from, to, 0.0022 * width, colors[2]);
    }

    private static int[] colors(int rgb) {
        if (rgb < 0) {
            return new int[]{822027280, -1325447904, -13693};
        }
        int r = rgb >> 16 & 0xFF;
        int g = rgb >> 8 & 0xFF;
        int b = rgb & 0xFF;
        return new int[]{0x30000000 | rgb, 0xB0000000 | rgb, 0xFF000000 | (r + 255) / 2 << 16 | (g + 255) / 2 << 8 | (b + 255) / 2};
    }

    public static void dot(PoseStack.Pose pose, VertexConsumer buffer, Vec3 at, Vec3 camera, int rgb) {
        Vec3 view = camera.subtract(at).normalize();
        at = at.add(view.scale(0.006));
        Vec3 right = view.cross(Math.abs(view.y) > 0.9 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0)).normalize();
        Vec3 up = right.cross(view);
        int[] colors = MonowireCableRenderer.colors(rgb);
        MonowireCableRenderer.quad(pose, buffer, at.subtract(camera), right, up, 0.012, colors[0] + 0x10000000);
        MonowireCableRenderer.quad(pose, buffer, at.subtract(camera), right, up, 0.0055, colors[1] + 0x10000000);
        MonowireCableRenderer.quad(pose, buffer, at.subtract(camera), right, up, 0.0028, colors[2]);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, Vec3 at, Vec3 right, Vec3 up, double radius, int color) {
        MonowireCableRenderer.vertex(pose, buffer, at.add(right.scale(-radius)).add(up.scale(-radius)), color);
        MonowireCableRenderer.vertex(pose, buffer, at.add(right.scale(radius)).add(up.scale(-radius)), color);
        MonowireCableRenderer.vertex(pose, buffer, at.add(right.scale(radius)).add(up.scale(radius)), color);
        MonowireCableRenderer.vertex(pose, buffer, at.add(right.scale(-radius)).add(up.scale(radius)), color);
    }

    private static void ribbon(PoseStack.Pose pose, VertexConsumer buffer, Vec3[] points, Vec3[] sides, Vec3 camera, int from, int to, double radius, int color) {
        for (int i = Math.max(0, from); i < Math.min(to, points.length - 1); ++i) {
            MonowireCableRenderer.vertex(pose, buffer, points[i].subtract(camera).add(sides[i].scale(radius)), color);
            MonowireCableRenderer.vertex(pose, buffer, points[i + 1].subtract(camera).add(sides[i + 1].scale(radius)), color);
            MonowireCableRenderer.vertex(pose, buffer, points[i + 1].subtract(camera).subtract(sides[i + 1].scale(radius)), color);
            MonowireCableRenderer.vertex(pose, buffer, points[i].subtract(camera).subtract(sides[i].scale(radius)), color);
        }
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, Vec3 p, int color) {
        AfterimageRenderContext.State echo = AfterimageRenderContext.current();
        if (echo != null && !echo.palette()) {
            int alpha = (int)((float)(color >>> 24 & 0xFF) * echo.alpha());
            int red = (int)((float)(color >>> 16 & 0xFF) * echo.red());
            int green = (int)((float)(color >>> 8 & 0xFF) * echo.green());
            int blue = (int)((float)(color & 0xFF) * echo.blue());
            color = alpha << 24 | red << 16 | green << 8 | blue;
        }
        buffer.addVertex(pose.pose(), (float)p.x, (float)p.y, (float)p.z).setColor(color);
    }
}

