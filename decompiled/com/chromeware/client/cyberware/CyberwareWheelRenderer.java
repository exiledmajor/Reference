/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.BufferUploader
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.Tesselator
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.util.FormattedCharSequence
 *  org.joml.Matrix4f
 */
package com.chromeware.client.cyberware;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Objects;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

public final class CyberwareWheelRenderer {
    private static final int TILE_FILL = -719708125;
    private static final int SELECTED_FILL = -517328330;
    private static final int TILE_EDGE = -1200653957;
    private static final int SELECTED_EDGE = -459782;
    private static final double GAP_DEGREES = 3.0;
    private static final double SELECTED_OFFSET = 6.0;
    private static final double POINTER_DEAD_ZONE = 12.0;
    private static final int POINTER_COLOR = -731272;
    private static final int POINTER_INNER = -401072865;
    private static final int ARC_STEPS = 32;

    private CyberwareWheelRenderer() {
    }

    public static double innerRadius(int elementCount) {
        return 12.0 + (double)Math.max(3, elementCount) * 3.0;
    }

    public static double outerRadius(int elementCount) {
        return 51.0 + (double)Math.max(3, elementCount) * 9.0;
    }

    public static double wedgeStartAngle(int index, int elementCount) {
        return -90.0 - 180.0 / (double)elementCount + (double)index * 360.0 / (double)elementCount;
    }

    public static double wedgeEndAngle(int index, int elementCount) {
        return CyberwareWheelRenderer.wedgeStartAngle(index, elementCount) + 360.0 / (double)elementCount;
    }

    public static int selectedIndex(double mouseX, double mouseY, double cx, double cy, int elementCount) {
        double angle = Math.toDegrees(Math.atan2(mouseY - cy, mouseX - cx));
        double normalized = (angle + 90.0 + 180.0 / (double)elementCount + 360.0) % 360.0;
        return Math.min(elementCount - 1, (int)(normalized / (360.0 / (double)elementCount)));
    }

    public static void drawWedge(GuiGraphics context, Font textRenderer, int cx, int cy, double startAngle, double endAngle, int elementCount, boolean selected, Component label, Component detail) {
        context.flush();
        double innerRadius = CyberwareWheelRenderer.innerRadius(elementCount);
        double outerRadius = CyberwareWheelRenderer.outerRadius(elementCount);
        double middleAngle = (startAngle + endAngle) * 0.5;
        double middleRadians = Math.toRadians(middleAngle);
        double offset = selected ? 6.0 : 0.0;
        float tileCx = (float)((double)cx + Math.cos(middleRadians) * offset);
        float tileCy = (float)((double)cy + Math.sin(middleRadians) * offset);
        double start = startAngle + 3.0;
        double end = endAngle - 3.0;
        int edge = selected ? -459782 : -1200653957;
        float edgeWidth = selected ? 2.2f : 1.0f;
        Matrix4f matrix = context.pose().last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        CyberwareWheelRenderer.drawArcStrip(matrix, tileCx, tileCy, innerRadius, outerRadius, start, end, selected ? -517328330 : -719708125);
        CyberwareWheelRenderer.drawArcStrip(matrix, tileCx, tileCy, outerRadius - (double)edgeWidth, outerRadius, start, end, edge);
        CyberwareWheelRenderer.drawArcStrip(matrix, tileCx, tileCy, innerRadius, innerRadius + (double)edgeWidth, start, end, edge);
        CyberwareWheelRenderer.drawRadialEdge(matrix, tileCx, tileCy, innerRadius, outerRadius, start, edgeWidth, edge);
        CyberwareWheelRenderer.drawRadialEdge(matrix, tileCx, tileCy, innerRadius, outerRadius, end, edgeWidth, edge);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        double contentRadius = (innerRadius + outerRadius) * 0.5;
        int contentX = (int)Math.round((double)tileCx + Math.cos(middleRadians) * contentRadius);
        int contentY = (int)Math.round((double)tileCy + Math.sin(middleRadians) * contentRadius);
        int textWidth = (int)Math.min(outerRadius - innerRadius - 4.0, 2.0 * contentRadius * Math.sin(Math.toRadians((end - start) * 0.5)) - 4.0);
        CyberwareWheelRenderer.drawContent(context, textRenderer, contentX, contentY, textWidth, selected, label, detail);
    }

    public static void drawPointer(GuiGraphics context, int cx, int cy, int mouseX, int mouseY, int elementCount) {
        context.flush();
        double dx = mouseX - cx;
        double dy = mouseY - cy;
        double mouseDistance = Math.sqrt(dx * dx + dy * dy);
        Matrix4f matrix = context.pose().last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        if (mouseDistance <= 12.0) {
            CyberwareWheelRenderer.drawArcStrip(matrix, cx, cy, 5.2, 7.8, 0.0, 360.0, -731272);
            CyberwareWheelRenderer.drawArcStrip(matrix, cx, cy, 0.0, 3.2, 0.0, 360.0, -731272);
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            return;
        }
        double nx = dx / mouseDistance;
        double ny = dy / mouseDistance;
        double pointerLimit = (CyberwareWheelRenderer.innerRadius(elementCount) + CyberwareWheelRenderer.outerRadius(elementCount)) * 0.5;
        double tipDistance = Math.min(pointerLimit, Math.max(18.0, mouseDistance));
        double baseDistance = tipDistance - 10.0;
        double lineStart = 4.0;
        double lineEnd = Math.max(lineStart, baseDistance - 1.0);
        CyberwareWheelRenderer.drawLine(matrix, (float)((double)cx + nx * lineStart), (float)((double)cy + ny * lineStart), (float)((double)cx + nx * lineEnd), (float)((double)cy + ny * lineEnd), 1.5f, -731272);
        CyberwareWheelRenderer.drawArcStrip(matrix, cx, cy, 0.0, 2.3, 0.0, 360.0, -731272);
        double sideX = -ny;
        double sideY = nx;
        float tipX = (float)((double)cx + nx * tipDistance);
        float tipY = (float)((double)cy + ny * tipDistance);
        float leftX = (float)((double)cx + nx * baseDistance + sideX * 5.2);
        float leftY = (float)((double)cy + ny * baseDistance + sideY * 5.2);
        float rightX = (float)((double)cx + nx * baseDistance - sideX * 5.2);
        float rightY = (float)((double)cy + ny * baseDistance - sideY * 5.2);
        CyberwareWheelRenderer.drawTriangle(matrix, tipX, tipY, leftX, leftY, rightX, rightY, -731272);
        double innerTipDistance = tipDistance - 2.2;
        double innerBaseDistance = baseDistance + 2.2;
        CyberwareWheelRenderer.drawTriangle(matrix, (float)((double)cx + nx * innerTipDistance), (float)((double)cy + ny * innerTipDistance), (float)((double)cx + nx * innerBaseDistance + sideX * 2.4), (float)((double)cy + ny * innerBaseDistance + sideY * 2.4), (float)((double)cx + nx * innerBaseDistance - sideX * 2.4), (float)((double)cy + ny * innerBaseDistance - sideY * 2.4), -401072865);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    private static void drawContent(GuiGraphics context, Font textRenderer, int x, int y, int textWidth, boolean selected, Component label, Component detail) {
        int color = selected ? -1 : -2762281;
        context.pose().pushPose();
        context.pose().translate(0.0f, 0.0f, 125.0f);
        context.drawCenteredString(textRenderer, label, x, detail == null ? y - 4 : y - 9, color);
        if (detail != null) {
            int lineY = y + 3;
            for (FormattedCharSequence line : textRenderer.split((FormattedText)detail, textWidth)) {
                context.drawCenteredString(textRenderer, line, x, lineY, selected ? -1 : -6313818);
                Objects.requireNonNull(textRenderer);
                lineY += 9;
            }
        }
        context.pose().popPose();
    }

    private static void drawArcStrip(Matrix4f matrix, float cx, float cy, double innerRadius, double outerRadius, double startAngle, double endAngle, int color) {
        Tesselator tessellator = Tesselator.getInstance();
        float red = (float)(color >> 16 & 0xFF) / 255.0f;
        float green = (float)(color >> 8 & 0xFF) / 255.0f;
        float blue = (float)(color & 0xFF) / 255.0f;
        float alpha = (float)(color >>> 24) / 255.0f;
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= 32; ++i) {
            double angle = Math.toRadians(startAngle + (endAngle - startAngle) * (double)i / 32.0);
            float cos = (float)Math.cos(angle);
            float sin = (float)Math.sin(angle);
            buffer.addVertex(matrix, cx + cos * (float)outerRadius, cy + sin * (float)outerRadius, 0.0f).setColor(red, green, blue, alpha);
            buffer.addVertex(matrix, cx + cos * (float)innerRadius, cy + sin * (float)innerRadius, 0.0f).setColor(red, green, blue, alpha);
        }
        BufferUploader.drawWithShader((MeshData)buffer.buildOrThrow());
    }

    private static void drawRadialEdge(Matrix4f matrix, float cx, float cy, double innerRadius, double outerRadius, double angleDegrees, float width, int color) {
        double angle = Math.toRadians(angleDegrees);
        float directionX = (float)Math.cos(angle);
        float directionY = (float)Math.sin(angle);
        float sideX = -directionY * width * 0.5f;
        float sideY = directionX * width * 0.5f;
        float innerX = cx + directionX * (float)innerRadius;
        float innerY = cy + directionY * (float)innerRadius;
        float outerX = cx + directionX * (float)outerRadius;
        float outerY = cy + directionY * (float)outerRadius;
        float red = (float)(color >> 16 & 0xFF) / 255.0f;
        float green = (float)(color >> 8 & 0xFF) / 255.0f;
        float blue = (float)(color & 0xFF) / 255.0f;
        float alpha = (float)(color >>> 24) / 255.0f;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(matrix, innerX - sideX, innerY - sideY, 0.0f).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, outerX - sideX, outerY - sideY, 0.0f).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, outerX + sideX, outerY + sideY, 0.0f).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, innerX + sideX, innerY + sideY, 0.0f).setColor(red, green, blue, alpha);
        BufferUploader.drawWithShader((MeshData)buffer.buildOrThrow());
    }

    private static void drawLine(Matrix4f matrix, float startX, float startY, float endX, float endY, float width, int color) {
        float dx = endX - startX;
        float dy = endY - startY;
        float length = (float)Math.sqrt(dx * dx + dy * dy);
        if (length < 0.001f) {
            return;
        }
        float sideX = -dy / length * width * 0.5f;
        float sideY = dx / length * width * 0.5f;
        CyberwareWheelRenderer.drawQuad(matrix, startX - sideX, startY - sideY, endX - sideX, endY - sideY, endX + sideX, endY + sideY, startX + sideX, startY + sideY, color);
    }

    private static void drawQuad(Matrix4f matrix, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, int color) {
        float red = (float)(color >> 16 & 0xFF) / 255.0f;
        float green = (float)(color >> 8 & 0xFF) / 255.0f;
        float blue = (float)(color & 0xFF) / 255.0f;
        float alpha = (float)(color >>> 24) / 255.0f;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(matrix, x1, y1, 0.0f).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, 0.0f).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x3, y3, 0.0f).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x4, y4, 0.0f).setColor(red, green, blue, alpha);
        BufferUploader.drawWithShader((MeshData)buffer.buildOrThrow());
    }

    private static void drawTriangle(Matrix4f matrix, float x1, float y1, float x2, float y2, float x3, float y3, int color) {
        float red = (float)(color >> 16 & 0xFF) / 255.0f;
        float green = (float)(color >> 8 & 0xFF) / 255.0f;
        float blue = (float)(color & 0xFF) / 255.0f;
        float alpha = (float)(color >>> 24) / 255.0f;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(matrix, x1, y1, 0.0f).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, 0.0f).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x3, y3, 0.0f).setColor(red, green, blue, alpha);
        BufferUploader.drawWithShader((MeshData)buffer.buildOrThrow());
    }
}

