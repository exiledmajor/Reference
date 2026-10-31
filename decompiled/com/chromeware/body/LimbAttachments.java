/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Registry
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.fml.ModList
 *  net.neoforged.neoforge.attachment.AttachmentType
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.NeoForgeRegistries
 */
package com.chromeware.body;

import com.chromeware.body.BodyPart;
import com.chromeware.body.BodyRestrictions;
import com.chromeware.body.CyberLimbs;
import com.chromeware.body.LimbIntegrity;
import com.chromeware.body.LimbRegrowth;
import com.chromeware.body.LimbState;
import com.chromeware.body.hitbox.BodyHitboxes;
import com.chromeware.compat.ragdoll.DetachedRagdolls;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.humanity.Humanity;
import com.chromeware.surgery.Surgery;
import com.chromeware.surgery.SurgerySeat;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class LimbAttachments {
    public static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create((Registry)NeoForgeRegistries.ATTACHMENT_TYPES, (String)"chromeware");
    public static final Supplier<AttachmentType<LimbState>> LIMBS = TYPES.register("limbs", () -> AttachmentType.builder(() -> LimbState.INTACT).serialize(LimbState.CODEC).sync(LimbState.STREAM_CODEC).build());
    public static final Supplier<AttachmentType<LimbIntegrity>> INTEGRITY = TYPES.register("integrity", () -> AttachmentType.builder(() -> LimbIntegrity.INTACT).serialize(LimbIntegrity.CODEC).sync(LimbIntegrity.STREAM_CODEC).build());
    public static final Supplier<AttachmentType<LimbRegrowth>> REGROWTH = TYPES.register("regrowth", () -> AttachmentType.builder(() -> LimbRegrowth.NONE).serialize(LimbRegrowth.CODEC).sync(LimbRegrowth.STREAM_CODEC).build());

    public static LimbState get(Player player) {
        return (LimbState)player.getData(LIMBS);
    }

    public static LimbState functional(Player player) {
        return ArmImplants.functionalState(player, LimbAttachments.get(player));
    }

    public static void set(Player player, LimbState state) {
        if (player.level().isClientSide()) {
            throw new IllegalStateException("Biological limbs are controlled by the server");
        }
        LimbState previous = LimbAttachments.get(player);
        LimbLoss.changing(player, previous, state);
        player.setData(INTEGRITY, (Object)((LimbIntegrity)player.getData(INTEGRITY)).reconcile(previous, state));
        player.setData(LIMBS, (Object)state);
        player.refreshDimensions();
        BodyRestrictions.apply(player);
        if (!previous.isMissing(BodyPart.HEAD) && state.isMissing(BodyPart.HEAD) && player.isAlive()) {
            DamageSource source = player.damageSources().genericKill();
            player.setHealth(0.0f);
            player.die(source);
        }
    }

    public static boolean hasArm(Player player, HumanoidArm arm) {
        return !LimbAttachments.functional(player).isMissing(arm == HumanoidArm.LEFT ? BodyPart.LEFT_ARM : BodyPart.RIGHT_ARM);
    }

    public static HumanoidArm armForHand(Player player, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
    }

    public static final class LimbLoss {
        static void changing(Player player, LimbState previous, LimbState next) {
            if (previous.isMissing(BodyPart.BODY) || !next.isMissing(BodyPart.BODY)) {
                return;
            }
            CyberLimbs frames = CyberLimbs.get(player);
            for (BodyPart part : new BodyPart[]{BodyPart.LEFT_ARM, BodyPart.RIGHT_ARM, BodyPart.LEFT_LEG, BodyPart.RIGHT_LEG}) {
                if (previous.isMissing(part)) continue;
                boolean cyber = frames.has(part);
                if (cyber) {
                    player.setData(CyberLimbs.FRAMES, (Object)CyberLimbs.get(player).with(part, false));
                    Humanity.removed(player);
                }
                ItemStack item = cyber ? CyberLimbs.item(part) : new ItemStack((ItemLike)Items.ROTTEN_FLESH);
                Vec3 position = BodyHitboxes.boxes(player, 1.0f).stream().filter(box -> box.part() == part).map(box -> box.center()).findFirst().orElse(player.position().add(0.0, 0.6, 0.0));
                Entity entity = player.getVehicle();
                if (entity instanceof SurgerySeat) {
                    SurgerySeat seat = (SurgerySeat)entity;
                    position = Surgery.limbSite(seat, part, 1.0f);
                }
                LimbLoss.drop(player, part, cyber, item, position);
            }
        }

        public static void drop(Player player, BodyPart part, boolean cyber, ItemStack item, Vec3 position) {
            ServerLevel level;
            block5: {
                block4: {
                    Level level2 = player.level();
                    if (!(level2 instanceof ServerLevel)) break block4;
                    level = (ServerLevel)level2;
                    if (!item.isEmpty()) break block5;
                }
                return;
            }
            if (ModList.get().isLoaded("sable_player_ragdoll") && DetachedRagdolls.spawn(player, part, cyber, item, position)) {
                return;
            }
            ItemEntity dropped = new ItemEntity((Level)level, position.x, position.y, position.z, item.copy());
            dropped.setDefaultPickUpDelay();
            level.addFreshEntity((Entity)dropped);
        }
    }
}

