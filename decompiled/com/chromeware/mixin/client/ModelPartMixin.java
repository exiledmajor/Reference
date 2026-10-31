/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.model.geom.ModelPart$Cube
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.client;

import com.chromeware.client.body.ClippedLimbRenderer;
import com.chromeware.client.body.LimbRenderContext;
import com.chromeware.client.launcher.LauncherForearmCutout;
import com.chromeware.client.mantis.MantisForearmCutout;
import com.chromeware.client.monowire.MonowireWristCutout;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ModelPart.class})
public abstract class ModelPartMixin {
    @Shadow
    @Final
    private List<ModelPart.Cube> cubes;
    @Shadow
    @Final
    private Map<String, ModelPart> children;
    @Shadow
    public boolean visible;
    @Shadow
    public boolean skipDraw;

    @Shadow
    public abstract void translateAndRotate(PoseStack var1);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Inject(method={"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$renderBiologicalPart(PoseStack stack, VertexConsumer buffer, int light, int overlay, int color, CallbackInfo ci) {
        LimbRenderContext.Rule rule = LimbRenderContext.ruleFor((ModelPart)this);
        if (rule == null) {
            return;
        }
        ci.cancel();
        if (rule.hidden() || !this.visible) {
            return;
        }
        stack.pushPose();
        try {
            this.translateAndRotate(stack);
            if (!this.skipDraw) {
                for (ModelPart.Cube cube : this.cubes) {
                    if (rule.wristCutout()) {
                        MonowireWristCutout.render(cube, rule.leftWrist(), stack.last(), buffer, light, overlay, color);
                        continue;
                    }
                    if (rule.launcherCutout()) {
                        LauncherForearmCutout.render(cube, rule.leftWrist(), stack.last(), buffer, light, overlay, color);
                        continue;
                    }
                    if (rule.mantisCutout()) {
                        MantisForearmCutout.render(cube, stack.last(), buffer, light, overlay, color);
                        continue;
                    }
                    if (rule.extendoCutout()) {
                        ClippedLimbRenderer.render(cube, 3.0f - cube.minY, stack.last(), buffer, light, overlay, color);
                        continue;
                    }
                    if (rule.stretch() < 1.0f) {
                        ClippedLimbRenderer.stretched(cube, rule.stretch(), stack.last(), buffer, light, overlay, color);
                        continue;
                    }
                    ClippedLimbRenderer.render(cube, rule.retainedLength(), stack.last(), buffer, light, overlay, color);
                }
            }
            for (ModelPart child : this.children.values()) {
                child.render(stack, buffer, light, overlay, color);
            }
        }
        finally {
            stack.popPose();
        }
    }
}

