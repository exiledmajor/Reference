/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.color.item.ItemColor
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.ItemLike
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions
 *  net.neoforged.neoforge.client.model.DynamicFluidContainerModel$Colors
 *  net.neoforged.neoforge.fluids.FluidType
 */
package com.chromeware.client.humanity;

import com.chromeware.humanity.ImmunosuppressantFluids;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;
import net.neoforged.neoforge.fluids.FluidType;

public final class ImmunosuppressantFluidsClient {
    private static final ResourceLocation STILL = ResourceLocation.withDefaultNamespace((String)"block/water_still");
    private static final ResourceLocation FLOW = ResourceLocation.withDefaultNamespace((String)"block/water_flow");
    private static final ResourceLocation OVERLAY = ResourceLocation.withDefaultNamespace((String)"block/water_overlay");

    public static void register(IEventBus modBus) {
        modBus.addListener(event -> {
            for (final ImmunosuppressantFluids.Liquid liquid : ImmunosuppressantFluids.ALL) {
                event.registerFluidType(new IClientFluidTypeExtensions(){

                    public ResourceLocation getStillTexture() {
                        return STILL;
                    }

                    public ResourceLocation getFlowingTexture() {
                        return FLOW;
                    }

                    public ResourceLocation getOverlayTexture() {
                        return OVERLAY;
                    }

                    public int getTintColor() {
                        return liquid.color;
                    }
                }, new FluidType[]{(FluidType)liquid.type.get()});
            }
        });
        modBus.addListener(event -> {
            for (ImmunosuppressantFluids.Liquid liquid : ImmunosuppressantFluids.ALL) {
                event.register((ItemColor)new DynamicFluidContainerModel.Colors(), new ItemLike[]{(ItemLike)liquid.bucket.get()});
            }
        });
    }
}

