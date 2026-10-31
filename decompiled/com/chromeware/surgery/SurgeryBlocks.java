/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.api.registry.CreateRegistries
 *  com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EntityType$Builder
 *  net.minecraft.world.entity.MobCategory
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.SoundType
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.entity.BlockEntityType$Builder
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.level.material.PushReaction
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.registries.DeferredBlock
 *  net.neoforged.neoforge.registries.DeferredHolder
 *  net.neoforged.neoforge.registries.DeferredItem
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.DeferredRegister$Blocks
 *  net.neoforged.neoforge.registries.DeferredRegister$Items
 */
package com.chromeware.surgery;

import com.chromeware.surgery.Surgery;
import com.chromeware.surgery.SurgeryArmPoint;
import com.chromeware.surgery.SurgeryChairBlock;
import com.chromeware.surgery.SurgeryChairBlockEntity;
import com.chromeware.surgery.SurgerySeat;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SurgeryBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks((String)"chromeware");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems((String)"chromeware");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create((ResourceKey)Registries.BLOCK_ENTITY_TYPE, (String)"chromeware");
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create((ResourceKey)Registries.ENTITY_TYPE, (String)"chromeware");
    public static final DeferredRegister<ArmInteractionPointType> ARM_POINTS = DeferredRegister.create((ResourceKey)CreateRegistries.ARM_INTERACTION_POINT_TYPE, (String)"chromeware");
    public static final DeferredBlock<SurgeryChairBlock> CHAIR = BLOCKS.register("surgery_chair", () -> new SurgeryChairBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(3.0f, 6.0f).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops().noOcclusion().pushReaction(PushReaction.BLOCK)));
    public static final DeferredItem<BlockItem> CHAIR_ITEM = ITEMS.register("surgery_chair", () -> new BlockItem((Block)CHAIR.get(), new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> SEVERED_LIMB = ITEMS.register("severed_limb", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SurgeryChairBlockEntity>> CHAIR_ENTITY = BLOCK_ENTITIES.register("surgery_chair", () -> BlockEntityType.Builder.of(SurgeryChairBlockEntity::new, (Block[])new Block[]{(Block)CHAIR.get()}).build(null));
    public static final DeferredHolder<EntityType<?>, EntityType<SurgerySeat>> SEAT = ENTITIES.register("surgery_seat", () -> EntityType.Builder.of(SurgerySeat::new, (MobCategory)MobCategory.MISC).sized(0.25f, 0.25f).noSave().noSummon().clientTrackingRange(8).updateInterval(Integer.MAX_VALUE).build("surgery_seat"));
    public static final DeferredHolder<ArmInteractionPointType, SurgeryArmPoint.Type> ARM_POINT = ARM_POINTS.register("surgery_chair", SurgeryArmPoint.Type::new);

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        ENTITIES.register(bus);
        ARM_POINTS.register(bus);
        NeoForge.EVENT_BUS.addListener(Surgery::mount);
    }
}

