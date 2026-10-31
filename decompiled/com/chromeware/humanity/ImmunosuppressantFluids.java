/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.BucketItem
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.block.LiquidBlock
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.material.FlowingFluid
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.level.material.PushReaction
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.fluids.BaseFlowingFluid$Flowing
 *  net.neoforged.neoforge.fluids.BaseFlowingFluid$Properties
 *  net.neoforged.neoforge.fluids.BaseFlowingFluid$Source
 *  net.neoforged.neoforge.fluids.FluidType
 *  net.neoforged.neoforge.fluids.FluidType$Properties
 *  net.neoforged.neoforge.registries.DeferredBlock
 *  net.neoforged.neoforge.registries.DeferredHolder
 *  net.neoforged.neoforge.registries.DeferredItem
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.DeferredRegister$Blocks
 *  net.neoforged.neoforge.registries.DeferredRegister$Items
 *  net.neoforged.neoforge.registries.NeoForgeRegistries$Keys
 */
package com.chromeware.humanity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ImmunosuppressantFluids {
    public static final int STANDARD_COLOR = -15532254;
    public static final int MILITARY_COLOR = -15747990;
    public static final DeferredRegister<FluidType> TYPES = DeferredRegister.create((ResourceKey)NeoForgeRegistries.Keys.FLUID_TYPES, (String)"chromeware");
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create((ResourceKey)Registries.FLUID, (String)"chromeware");
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks((String)"chromeware");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems((String)"chromeware");
    public static final Liquid STANDARD = new Liquid("standard_immunosuppressant", -15532254);
    public static final Liquid MILITARY = new Liquid("military_immunosuppressant", -15747990);
    public static final Liquid[] ALL = new Liquid[]{STANDARD, MILITARY};

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        FLUIDS.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }

    public static final class Liquid {
        public final String name;
        public final int color;
        public final DeferredHolder<FluidType, FluidType> type;
        public final DeferredHolder<Fluid, FlowingFluid> source;
        public final DeferredHolder<Fluid, FlowingFluid> flowing;
        public final DeferredBlock<LiquidBlock> block;
        public final DeferredItem<BucketItem> bucket;
        private BaseFlowingFluid.Properties properties;

        private Liquid(String name, int color) {
            this.name = name;
            this.color = color;
            this.type = TYPES.register(name, () -> new FluidType(FluidType.Properties.create().descriptionId("fluid.chromeware." + name).density(1100).viscosity(1300).canDrown(true).canSwim(true).canExtinguish(true).supportsBoating(false)));
            this.source = FLUIDS.register(name, () -> new BaseFlowingFluid.Source(this.properties()));
            this.flowing = FLUIDS.register("flowing_" + name, () -> new BaseFlowingFluid.Flowing(this.properties()));
            this.block = BLOCKS.register(name, () -> new LiquidBlock((FlowingFluid)this.source.get(), BlockBehaviour.Properties.of().mapColor(MapColor.EMERALD).replaceable().noCollission().strength(100.0f).pushReaction(PushReaction.DESTROY).noLootTable().liquid()));
            this.bucket = ITEMS.register(name + "_bucket", () -> new BucketItem((Fluid)this.source.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
        }

        private BaseFlowingFluid.Properties properties() {
            if (this.properties == null) {
                this.properties = new BaseFlowingFluid.Properties(this.type, this.source, this.flowing).block(this.block).bucket(this.bucket).slopeFindDistance(3).levelDecreasePerBlock(2).tickRate(15);
            }
            return this.properties;
        }

        public ResourceLocation id() {
            return ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)this.name);
        }
    }
}

