/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.content.contraptions.AbstractContraptionEntity
 *  com.simibubi.create.content.contraptions.Contraption
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.item.PrimedTnt
 *  net.minecraft.world.entity.monster.Creeper
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.vehicle.MinecartTNT
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.Level$ExplosionInteraction
 *  net.minecraft.world.level.block.BedBlock
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.TntBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.BedPart
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate$StructureBlockInfo
 *  net.minecraft.world.phys.Vec3
 */
package com.chromeware.netrunning;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;

public final class Detonation {
    private static final List<Tier> TIERS = List.of(new Tier("huge", 6.0f), new Tier("large", 4.0f), new Tier("medium", 3.0f), new Tier("small", 2.0f));

    public static boolean applicable(Entity target) {
        LivingEntity living;
        if (target instanceof Creeper || target instanceof PrimedTnt || target instanceof MinecartTNT || target instanceof EndCrystal) {
            return true;
        }
        if (target instanceof AbstractContraptionEntity) {
            AbstractContraptionEntity contraption = (AbstractContraptionEntity)target;
            return Detonation.tnt(contraption) != null;
        }
        if (target instanceof Player && target.level().isClientSide()) {
            return true;
        }
        return target instanceof LivingEntity && Detonation.carried(living = (LivingEntity)target) != null;
    }

    public static boolean entity(Player p, Entity target) {
        ServerLevel level = (ServerLevel)target.level();
        Entity entity = target;
        Objects.requireNonNull(entity);
        Entity entity2 = entity;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Creeper.class, PrimedTnt.class, MinecartTNT.class, EndCrystal.class, AbstractContraptionEntity.class}, (Object)entity2, n)) {
            case 0: {
                Creeper creeper = (Creeper)entity2;
                creeper.ignite();
                return true;
            }
            case 1: {
                PrimedTnt tnt = (PrimedTnt)entity2;
                tnt.setFuse(1);
                return true;
            }
            case 2: {
                MinecartTNT cart = (MinecartTNT)entity2;
                cart.primeFuse();
                return true;
            }
            case 3: {
                EndCrystal crystal = (EndCrystal)entity2;
                crystal.hurt(level.damageSources().playerAttack(p), 1.0f);
                return true;
            }
            case 4: {
                AbstractContraptionEntity contraption = (AbstractContraptionEntity)entity2;
                BlockPos local = Detonation.tnt(contraption);
                if (local == null) {
                    return false;
                }
                contraption.setBlock(local, new StructureTemplate.StructureBlockInfo(local, Blocks.AIR.defaultBlockState(), null));
                Detonation.explode(level, p, contraption.toGlobalVector(Vec3.atCenterOf((Vec3i)local), 1.0f), 4.0f, false, Level.ExplosionInteraction.TNT);
                return true;
            }
        }
        if (!(target instanceof LivingEntity)) {
            return false;
        }
        LivingEntity living = (LivingEntity)target;
        Charge charge = Detonation.carried(living);
        if (charge == null) {
            return false;
        }
        charge.stack.shrink(1);
        Detonation.explode(level, p, target.position().add(0.0, (double)target.getBbHeight() * 0.5, 0.0), charge.power, charge.fire, charge.interaction);
        return true;
    }

    public static boolean explosive(Level level, BlockState state) {
        return state.getBlock() instanceof TntBlock || state.getBlock() instanceof BedBlock && !level.dimensionType().bedWorks();
    }

    public static boolean block(Player p, BlockPos pos) {
        ServerLevel level = (ServerLevel)p.level();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof TntBlock) {
            state.onCaughtFire((Level)level, pos, null, (LivingEntity)p);
            level.removeBlock(pos, false);
            return true;
        }
        if (state.getBlock() instanceof BedBlock && !level.dimensionType().bedWorks()) {
            Direction facing = (Direction)state.getValue((Property)BedBlock.FACING);
            BlockPos head = state.getValue((Property)BedBlock.PART) == BedPart.HEAD ? pos : pos.relative(facing);
            level.removeBlock(head, false);
            BlockPos foot = head.relative(facing.getOpposite());
            if (level.getBlockState(foot).is(state.getBlock())) {
                level.removeBlock(foot, false);
            }
            Vec3 center = head.getCenter();
            level.explode(null, level.damageSources().badRespawnPointExplosion(center), null, center, 5.0f, true, Level.ExplosionInteraction.BLOCK);
            return true;
        }
        return false;
    }

    private static void explode(ServerLevel level, Player p, Vec3 at, float power, boolean fire, Level.ExplosionInteraction interaction) {
        level.explode(null, level.damageSources().explosion(null, (Entity)p), null, at.x, at.y, at.z, power, fire, interaction);
    }

    private static BlockPos tnt(AbstractContraptionEntity contraption) {
        Contraption c = contraption.getContraption();
        if (c == null) {
            return null;
        }
        for (Map.Entry e : c.getBlocks().entrySet()) {
            if (!(((StructureTemplate.StructureBlockInfo)e.getValue()).state().getBlock() instanceof TntBlock)) continue;
            return (BlockPos)e.getKey();
        }
        return null;
    }

    private static Charge carried(LivingEntity living) {
        ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            stacks.add(living.getItemBySlot(slot));
        }
        if (living instanceof Player) {
            Player player = (Player)living;
            for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
                stacks.add(player.getInventory().getItem(i));
            }
        }
        for (Tier tier : TIERS) {
            for (ItemStack stack : stacks) {
                if (stack.isEmpty() || !stack.is(tier.tag)) continue;
                return new Charge(stack, tier.power, false, stack.is(Items.END_CRYSTAL) ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.TNT);
            }
        }
        for (ItemStack stack : stacks) {
            if (!stack.is(Items.FIRE_CHARGE)) continue;
            return new Charge(stack, 1.0f, true, Level.ExplosionInteraction.MOB);
        }
        return null;
    }

    private record Charge(ItemStack stack, float power, boolean fire, Level.ExplosionInteraction interaction) {
    }

    private record Tier(TagKey<Item> tag, float power) {
        Tier(String name, float power) {
            this((TagKey<Item>)TagKey.create((ResourceKey)Registries.ITEM, (ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)("explosives/" + name))), power);
        }
    }
}

