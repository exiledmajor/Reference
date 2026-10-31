/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.simibubi.create.content.contraptions.AbstractContraptionEntity
 *  com.simibubi.create.content.contraptions.render.ContraptionVisual
 *  dev.engine_room.flywheel.api.visualization.VisualEmbedding
 *  dev.engine_room.flywheel.api.visualization.VisualizationContext
 *  dev.engine_room.flywheel.lib.visual.AbstractEntityVisual
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix3fc
 *  org.joml.Matrix4fc
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.client;

import com.chromeware.client.sandevistan.SandevistanClient;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.render.ContraptionVisual;
import dev.engine_room.flywheel.api.visualization.VisualEmbedding;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.AbstractEntityVisual;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ContraptionVisual.class}, remap=false)
public abstract class SandevistanContraptionVisualMixin
extends AbstractEntityVisual<AbstractContraptionEntity> {
    @Shadow
    @Final
    private PoseStack contraptionMatrix;
    @Shadow
    @Final
    protected VisualEmbedding embedding;

    protected SandevistanContraptionVisualMixin(VisualizationContext context, AbstractContraptionEntity entity, float partialTick) {
        super(context, (Entity)entity, partialTick);
    }

    @Inject(method={"setEmbeddingMatrices"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$logicalTransform(float partialTick, CallbackInfo ci) {
        if (!SandevistanClient.isDilated(this.entity)) {
            return;
        }
        Vec3 position = SandevistanClient.renderPosition(this.entity, partialTick);
        Vec3i origin = this.renderOrigin();
        this.contraptionMatrix.setIdentity();
        this.contraptionMatrix.translate(position.x - (double)origin.getX(), position.y - (double)origin.getY(), position.z - (double)origin.getZ());
        ((AbstractContraptionEntity)this.entity).applyLocalTransforms(this.contraptionMatrix, SandevistanClient.tickDelta(this.entity, partialTick));
        this.embedding.transforms((Matrix4fc)this.contraptionMatrix.last().pose(), (Matrix3fc)this.contraptionMatrix.last().normal());
        ci.cancel();
    }
}

