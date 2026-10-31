/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.content.kinetics.mechanicalArm.ArmAngleTarget
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Holder$Reference
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageType
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomData
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.event.entity.EntityMountEvent
 */
package com.chromeware.surgery;

import com.chromeware.body.BodyPart;
import com.chromeware.body.CyberLimbs;
import com.chromeware.body.LimbAttachments;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.cyberware.CyberwareCapacity;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.CyberwareStats;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantCategory;
import com.chromeware.cyberware.Installing;
import com.chromeware.cyberware.InventoryImplants;
import com.chromeware.cyberware.SandevistanImplant;
import com.chromeware.extendo.ExtendoGrip;
import com.chromeware.netrunning.QuickhackItem;
import com.chromeware.surgery.SurgeryBlocks;
import com.chromeware.surgery.SurgeryChairBlock;
import com.chromeware.surgery.SurgeryClaw;
import com.chromeware.surgery.SurgerySeat;
import com.chromeware.surgery.SurgeryStock;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmAngleTarget;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityMountEvent;

public final class Surgery {
    public static final ResourceKey<DamageType> DAMAGE = ResourceKey.create((ResourceKey)Registries.DAMAGE_TYPE, (ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"surgery"));
    public static final int ARMS = -1;
    public static final int OPERATING_SYSTEM = -2;
    public static final int LIMBS = -3;

    public static BodyPart operated(SurgerySeat seat) {
        if (!seat.operating() || seat.patient() == null) {
            return null;
        }
        if (seat.part() != null) {
            return seat.part();
        }
        ItemStack implant = seat.implant();
        if (!Surgery.armHardware(implant)) {
            return null;
        }
        return ArmImplants.part(seat.removing() && seat.removal() != null ? Surgery.armSide(seat.removal()) : ArmImplants.installSide(seat.patient(), implant));
    }

    public static HumanoidArm armSide(Removal removal) {
        return removal.index() == 1 ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    public static BodyPart limb(Removal removal) {
        BodyPart[] parts = BodyPart.values();
        return parts[Math.clamp((long)removal.index(), (int)0, (int)(parts.length - 1))];
    }

    static boolean seat(Level level, BlockPos foot, Player player) {
        SurgerySeat seat;
        if (player.isPassenger() || player.isShiftKeyDown()) {
            return false;
        }
        BlockState state = level.getBlockState(foot);
        if (!(state.getBlock() instanceof SurgeryChairBlock)) {
            return false;
        }
        List existing = level.getEntitiesOfClass(SurgerySeat.class, new AABB(foot));
        if (!existing.isEmpty() && ((SurgerySeat)((Object)existing.getFirst())).patient() != null) {
            player.displayClientMessage((Component)Component.translatable((String)"message.chromeware.surgery_occupied"), true);
            return false;
        }
        if (!existing.isEmpty()) {
            seat = (SurgerySeat)((Object)existing.getFirst());
        } else {
            seat = new SurgerySeat((EntityType)SurgeryBlocks.SEAT.get(), level);
            Vec3 hip = SurgerySeat.hip(foot, (Direction)state.getValue((Property)SurgeryChairBlock.FACING));
            seat.setPos(hip.x, (double)foot.getY() + 0.01, hip.z);
            level.addFreshEntity((Entity)seat);
        }
        player.startRiding((Entity)seat, true);
        player.setYRot(seat.patientYaw());
        player.setYBodyRot(seat.patientYaw());
        player.setYHeadRot(seat.patientYaw());
        player.displayClientMessage((Component)Component.translatable((String)"message.chromeware.surgery_seated"), true);
        return true;
    }

    public static void mount(EntityMountEvent event) {
        SurgerySeat seat;
        Entity entity;
        if (event.isDismounting() && (entity = event.getEntityBeingMounted()) instanceof SurgerySeat && !(seat = (SurgerySeat)entity).canLeave() && seat.isAlive() && event.getEntityMounting().isAlive()) {
            event.setCanceled(true);
        }
    }

    static boolean installable(ItemStack stack) {
        return !stack.isEmpty() && !(stack.getItem() instanceof QuickhackItem) && Installing.supported(stack);
    }

    public static SurgerySeat.Kind kind(ItemStack stack) {
        if (stack.is(CyberwareItems.SANDEVISTAN)) {
            return SurgerySeat.Kind.SPINE;
        }
        if (CyberLimbs.frame(stack)) {
            return SurgerySeat.Kind.LIMB;
        }
        return SurgerySeat.Kind.STANDARD;
    }

    public static boolean armHardware(ItemStack stack) {
        return stack.is(CyberwareItems.GORILLA_ARMS) || stack.is(CyberwareItems.MANTIS_BLADES) || stack.is(CyberwareItems.MONOWIRE) || stack.is(CyberwareItems.PROJECTILE_LAUNCH_SYSTEM) || ExtendoGrip.is(stack);
    }

    public static boolean operatingSystem(ItemStack stack) {
        return stack.is(CyberwareItems.SANDEVISTAN) || stack.is(CyberwareItems.CYBERDECK) || Implant.CHROME_COMPRESSOR.matches(stack) || Implant.ZETATECH_BERSERK.matches(stack);
    }

    public static boolean duplicate(Player player, ItemStack stack, @Nullable HumanoidArm arm) {
        if (stack.isEmpty() || CyberLimbs.frame(stack)) {
            return false;
        }
        if (Surgery.armHardware(stack)) {
            return arm != null && ItemStack.isSameItem((ItemStack)ArmImplants.get(player, arm).item(), (ItemStack)stack);
        }
        if (Surgery.operatingSystem(stack)) {
            return ItemStack.isSameItem((ItemStack)SandevistanImplant.get(player).item(), (ItemStack)stack);
        }
        if (!InventoryImplants.supported(stack)) {
            return false;
        }
        return InventoryImplants.get(player, InventoryImplants.category(stack)).slots().stream().anyMatch(slot -> ItemStack.isSameItem((ItemStack)slot.item(), (ItemStack)stack));
    }

    static Component refusal(Player player, ItemStack stack) {
        ItemStack previous;
        if (CyberLimbs.frame(stack)) {
            return CyberLimbs.refusal(player, stack, CyberLimbs.target(player, stack));
        }
        if (Surgery.duplicate(player, stack, Surgery.armHardware(stack) ? ArmImplants.installSide(player, stack) : null)) {
            return Component.translatable((String)"message.chromeware.already_installed", (Object[])new Object[]{stack.getHoverName()});
        }
        if (Surgery.operatingSystem(stack)) {
            previous = SandevistanImplant.get(player).item();
        } else if (Surgery.armHardware(stack)) {
            previous = ArmImplants.get(player, ArmImplants.installSide(player, stack)).item();
        } else {
            int socket = InventoryImplants.socket(player, stack);
            if (socket < 0) {
                return Component.translatable((String)"message.chromeware.socket_full");
            }
            previous = InventoryImplants.get(player, InventoryImplants.category(stack)).get(socket).item();
        }
        if (!CyberwareCapacity.fits(player, stack, previous)) {
            return Component.translatable((String)"message.chromeware.capacity_full", (Object[])new Object[]{CyberwareCapacity.used(player) - CyberwareStats.get(previous).capacity() + CyberwareStats.get(stack).capacity(), CyberwareCapacity.limit(player)});
        }
        return null;
    }

    static ItemStack installed(Player player, Removal removal) {
        if (removal.category() == -1) {
            return ArmImplants.get(player, Surgery.armSide(removal)).item();
        }
        if (removal.category() == -3) {
            return CyberLimbs.has(player, Surgery.limb(removal)) ? CyberLimbs.item(Surgery.limb(removal)) : ItemStack.EMPTY;
        }
        if (removal.category() == -2) {
            return SandevistanImplant.get(player).item();
        }
        ImplantCategory group = ImplantCategory.byIndex(removal.category());
        if (group == null || group == ImplantCategory.OPERATING_SYSTEM || removal.index() < 0 || removal.index() >= group.sockets) {
            return ItemStack.EMPTY;
        }
        return InventoryImplants.get(player, removal.category()).get(removal.index()).item();
    }

    public static boolean belongs(ItemStack item, String category) {
        if (item.isEmpty()) {
            return false;
        }
        return switch (category) {
            case "arms" -> Surgery.armHardware(item);
            case "operating_system" -> Surgery.operatingSystem(item);
            case "limb" -> CyberLimbs.frame(item);
            default -> {
                ImplantCategory group = ImplantCategory.byKey(category);
                if (group != null && InventoryImplants.supported(item) && !Surgery.operatingSystem(item) && InventoryImplants.category(item) == group.index) {
                    yield true;
                }
                yield false;
            }
        };
    }

    public static void operate(Player player, ItemStack wanted, String category, int index) {
        Entity entity = player.getVehicle();
        if (!(entity instanceof SurgerySeat)) {
            player.displayClientMessage((Component)Component.translatable((String)"message.chromeware.surgery_needs_chair"), true);
            return;
        }
        SurgerySeat seat = (SurgerySeat)entity;
        if (!(Surgery.belongs(wanted, category) && Surgery.installable(wanted) && Installing.canOperate(player))) {
            return;
        }
        if (category.equals("limb") && (index < 0 || index >= BodyPart.values().length || !CyberLimbs.fits(wanted, BodyPart.values()[index]))) {
            return;
        }
        seat.schedule(player, new Request(wanted.copyWithCount(1), category, index, null));
    }

    static Component check(SurgerySeat seat, Player player, Request request) {
        if (request.removal() != null) {
            return Surgery.installed(player, request.removal()).isEmpty() ? Component.translatable((String)"message.chromeware.surgery_nothing_to_remove") : null;
        }
        if (SurgeryStock.supplier(seat, request.item(), null) == null) {
            return Component.translatable((String)"message.chromeware.surgery_not_in_stock", (Object[])new Object[]{request.item().getHoverName()});
        }
        Surgery.aim(player, request);
        Component refusal = Surgery.refusal(player, request.item());
        Surgery.clearRequests(player);
        seat.reaim(player);
        return refusal;
    }

    static void begin(SurgerySeat seat, Player player, Request request, boolean assist) {
        Component refusal = Surgery.check(seat, player, request);
        if (refusal != null) {
            player.displayClientMessage(refusal, true);
            return;
        }
        if (request.removal() != null) {
            ItemStack expected = Surgery.installed(player, request.removal());
            seat.beginRemoval(request.removal(), expected);
            player.displayClientMessage((Component)Component.translatable((String)"message.chromeware.surgery_removal_started", (Object[])new Object[]{expected.getHoverName()}), true);
            return;
        }
        BlockPos arm = SurgeryStock.supplier(seat, request.item(), assist ? seat.arm() : seat.assistArm());
        if (arm == null) {
            player.displayClientMessage((Component)Component.translatable((String)"message.chromeware.surgery_not_in_stock", (Object[])new Object[]{request.item().getHoverName()}), true);
            return;
        }
        if (assist) {
            seat.assist(request, arm);
        } else {
            Surgery.aim(player, request);
            seat.plan(request, request.item(), arm);
        }
        player.displayClientMessage((Component)Component.translatable((String)"message.chromeware.surgery_started", (Object[])new Object[]{request.item().getHoverName()}), true);
    }

    static void aim(Player player, Request request) {
        Surgery.clearRequests(player);
        switch (request.category()) {
            case "arms": {
                ArmImplants.request(player, request.index() == 1 ? HumanoidArm.LEFT : HumanoidArm.RIGHT);
                break;
            }
            case "limb": {
                CyberLimbs.request(player, BodyPart.values()[request.index()]);
                break;
            }
            case "operating_system": {
                break;
            }
            default: {
                InventoryImplants.request(player, request.index());
            }
        }
    }

    static void install(Player patient, Request request, ItemStack stack) {
        ItemStack implant;
        if (request != null) {
            Surgery.aim(patient, request);
        }
        if (!Installing.installStack(patient, implant = stack.copyWithCount(1))) {
            Installing.returnItem(patient, implant);
        }
        Surgery.clearRequests(patient);
    }

    static void clearRequests(Player player) {
        ArmImplants.request(player, null);
        InventoryImplants.request(player, null);
        CyberLimbs.request(player, null);
    }

    public static void requestRemoval(Player player, Removal removal) {
        Entity entity = player.getVehicle();
        if (!(entity instanceof SurgerySeat)) {
            player.displayClientMessage((Component)Component.translatable((String)"message.chromeware.surgery_needs_chair"), true);
            return;
        }
        SurgerySeat seat = (SurgerySeat)entity;
        ItemStack expected = Surgery.installed(player, removal);
        if (expected.isEmpty() || !Installing.canOperate(player)) {
            return;
        }
        seat.schedule(player, new Request(expected.copyWithCount(1), "", 0, removal));
    }

    static ItemStack remove(Player player, Removal removal) {
        List<ItemStack> removed = Installing.capture(() -> {
            if (removal.category() == -1) {
                ArmImplants.remove(player, Surgery.armSide(removal));
            } else if (removal.category() == -3) {
                CyberLimbs.remove(player, Surgery.limb(removal));
            } else if (removal.category() == -2) {
                SandevistanImplant.remove(player);
            } else {
                InventoryImplants.remove(player, removal.category(), removal.index());
            }
        });
        if (removed.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (int i = 1; i < removed.size(); ++i) {
            Installing.returnItem(player, removed.get(i));
        }
        return (ItemStack)removed.getFirst();
    }

    private static Frame frame(SurgerySeat seat, float partialTick) {
        Player patient = seat.patient();
        Direction toHead = seat.facing();
        Vec3 origin = patient != null ? patient.getPosition(partialTick) : seat.position().add(0.0, 0.04, 0.0);
        float degrees = seat.recline(partialTick);
        double angle = Math.toRadians(degrees);
        Vec3 up = new Vec3(0.0, 1.0, 0.0);
        Vec3 head = new Vec3((double)toHead.getStepX(), 0.0, (double)toHead.getStepZ());
        Vec3 axis = up.scale(Math.cos(angle)).add(head.scale(Math.sin(angle)));
        Vec3 front = up.scale(Math.sin(angle)).subtract(head.scale(Math.cos(angle)));
        Vec3 right = new Vec3(head.z, 0.0, -head.x);
        return new Frame(origin.add(0.0, 0.75, 0.0), axis, front, right, head, degrees);
    }

    public static Vec3 site(SurgerySeat seat, ItemStack implant, float partialTick) {
        Frame f = Surgery.frame(seat, partialTick);
        if (implant.isEmpty()) {
            return f.at(0.5, 0.25, 0.0);
        }
        if (Surgery.severed(implant) || CyberLimbs.frame(implant)) {
            BodyPart part = seat.part();
            if (part == null && seat.patient() != null && !seat.level().isClientSide()) {
                part = CyberLimbs.target(seat.patient(), implant);
            }
            return Surgery.limbSite(seat, part, partialTick);
        }
        if (Surgery.kind(implant) == SurgerySeat.Kind.SPINE) {
            return f.at(0.55, 0.0, 0.0).add(f.degrees() > 80.0f ? new Vec3(0.0, 0.25, 0.0) : f.front().scale(-0.25));
        }
        if (Surgery.armHardware(implant)) {
            HumanoidArm arm = seat.removing() && seat.removal() != null ? Surgery.armSide(seat.removal()) : (seat.patient() != null ? ArmImplants.installSide(seat.patient(), implant) : HumanoidArm.RIGHT);
            return f.at(0.45, 0.1, arm == HumanoidArm.LEFT ? -0.4 : 0.4);
        }
        if (Surgery.operatingSystem(implant)) {
            return f.at(0.7, 0.2, 0.0);
        }
        ImplantCategory implantCategory = ImplantCategory.byIndex(InventoryImplants.category(implant));
        int n = 0;
        return switch (SwitchBootstraps.enumSwitch("enumSwitch", new Object[]{"FRONTAL_CORTEX", "FACE", "NERVOUS_SYSTEM", "INTEGUMENTARY_SYSTEM", "HANDS", "LEGS"}, (ImplantCategory)implantCategory, n)) {
            case 0, 1 -> f.at(0.95, 0.3, 0.0);
            case 2 -> f.at(0.7, 0.2, 0.0);
            case 3 -> f.at(0.3, 0.2, 0.0);
            case 4 -> f.hip().add(f.right().scale(0.35)).add(f.head().scale(-0.3)).add(0.0, 0.05, 0.0);
            case 5 -> f.hip().add(f.head().scale(-0.45)).add(0.0, 0.15, 0.0);
            default -> f.at(0.5, 0.2, 0.0);
        };
    }

    public static Vec3 limbSite(SurgerySeat seat, BodyPart part, float partialTick) {
        Frame f = Surgery.frame(seat, partialTick);
        if (part == null) {
            return f.at(0.5, 0.25, 0.0);
        }
        return switch (part) {
            case BodyPart.BODY -> f.at(0.5, 0.25, 0.0);
            case BodyPart.LEFT_LEG, BodyPart.RIGHT_LEG -> f.hip().add(f.head().scale(-0.35)).add(f.right().scale(part == BodyPart.LEFT_LEG ? -0.12 : 0.12)).add(0.0, 0.15, 0.0);
            default -> f.at(0.5, 0.15, part == BodyPart.LEFT_ARM ? -0.38 : 0.38);
        };
    }

    public static List<SurgeryApproach.Box> obstacles(SurgerySeat seat, float partialTick) {
        Frame f = Surgery.frame(seat, partialTick);
        ArrayList<SurgeryApproach.Box> boxes = new ArrayList<SurgeryApproach.Box>();
        BiConsumer<double[], double[][]> body = (unused, r) -> {
            double[] along = r[0];
            double[] out = r[1];
            double[] side = r[2];
            Vec3 c = f.at((along[0] + along[1]) / 2.0, (out[0] + out[1]) / 2.0, (side[0] + side[1]) / 2.0);
            boxes.add(new SurgeryApproach.Box(c, f.axis(), f.front(), f.right(), (along[1] - along[0]) / 2.0, (out[1] - out[0]) / 2.0, (side[1] - side[0]) / 2.0));
        };
        body.accept(null, new double[][]{{0.0, 0.7}, {-0.12, 0.12}, {-0.23, 0.23}});
        body.accept(null, new double[][]{{0.7, 1.17}, {-0.23, 0.23}, {-0.23, 0.23}});
        body.accept(null, new double[][]{{0.0, 0.7}, {-0.12, 0.12}, {0.23, 0.47}});
        body.accept(null, new double[][]{{0.0, 0.7}, {-0.12, 0.12}, {-0.47, -0.23}});
        Vec3 up = new Vec3(0.0, 1.0, 0.0);
        boxes.add(new SurgeryApproach.Box(f.hip().subtract(f.head().scale(0.35)), f.head().scale(-1.0), up, f.right(), 0.35, 0.12, 0.23));
        Vec3 foot = Vec3.atLowerCornerOf((Vec3i)seat.chair());
        Vec3 x = new Vec3(1.0, 0.0, 0.0);
        Vec3 z = new Vec3(0.0, 0.0, 1.0);
        boxes.add(new SurgeryApproach.Box(foot.add(0.5, 0.3125, 0.5), x, up, z, 0.5, 0.3125, 0.5));
        Vec3 headBlock = foot.add(f.head());
        boxes.add(new SurgeryApproach.Box(headBlock.add(0.5, 0.21875, 0.5), x, up, z, 0.5, 0.21875, 0.5));
        Vec3 hinge = foot.add(0.5, 0.625, 0.5).add(f.head().scale(0.5));
        Vec3 side = new Vec3(Math.abs(f.right().x), 0.0, Math.abs(f.right().z));
        boxes.add(new SurgeryApproach.Box(hinge.add(f.axis().scale(0.6875)).add(f.front().scale(-0.0625)), f.axis(), f.front(), side, 0.6875, 0.125, 0.4375));
        return boxes;
    }

    public static ArmAngleTarget approach(SurgerySeat seat, BlockPos arm, boolean ceiling, Vec3 point, float partialTick) {
        return SurgeryApproach.solve(arm, ceiling, point, Surgery.obstacles(seat, partialTick));
    }

    public static Vec3 inward(SurgerySeat seat, float partialTick) {
        return Surgery.frame(seat, partialTick).front().scale(-0.12);
    }

    public static Vec3 handOver(SurgerySeat seat, float partialTick) {
        return Surgery.frame(seat, partialTick).at(0.4, 0.6, 0.0);
    }

    static void punch(Player patient, Vec3 at) {
        Surgery.hurt(patient, SurgerySeat.Kind.STANDARD);
        Level level = patient.level();
        level.playSound(null, at.x, at.y, at.z, SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.BLOCKS, 1.0f, 0.8f);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.25f, 1.6f);
        if (level instanceof ServerLevel) {
            ServerLevel server = (ServerLevel)level;
            server.sendParticles((ParticleOptions)ParticleTypes.CRIT, at.x, at.y, at.z, 10, 0.1, 0.1, 0.1, 0.2);
        }
    }

    static void rip(Player patient, BodyPart part) {
        Level level = patient.level();
        level.playSound(null, patient.getX(), patient.getY() + 1.0, patient.getZ(), SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH, SoundSource.PLAYERS, 1.0f, 0.6f);
        level.playSound(null, patient.getX(), patient.getY() + 1.0, patient.getZ(), SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 1.0f, 0.7f);
        if (part == null || part == BodyPart.BODY) {
            return;
        }
        LimbAttachments.set(patient, LimbAttachments.get(patient).remove(part));
    }

    static ItemStack severed(BodyPart part) {
        ItemStack stack = new ItemStack((ItemLike)SurgeryBlocks.SEVERED_LIMB.get());
        CompoundTag tag = new CompoundTag();
        tag.putString("part", part == null ? "BODY" : part.name());
        stack.set(DataComponents.CUSTOM_DATA, (Object)CustomData.of((CompoundTag)tag));
        return stack;
    }

    public static boolean severed(ItemStack stack) {
        return stack.is((Item)SurgeryBlocks.SEVERED_LIMB.get());
    }

    static void advance(SurgerySeat seat, Player patient) {
        int t = seat.stageTicks();
        SurgerySeat.Kind kind = seat.kind();
        switch (seat.stage()) {
            case RECLINE: {
                if (t < 30) break;
                seat.work();
                break;
            }
            case WORK: {
                if (!(seat.opened() ? seat.openTicks() >= 400 : t >= 400)) break;
                seat.giveUp();
                break;
            }
            case PRESENT: {
                if (t < 200) break;
                seat.giveUp();
                break;
            }
            case CLOSE: {
                if (t < SurgerySeat.closeTicks(kind)) {
                    return;
                }
                if (seat.treated()) {
                    Surgery.recover(patient, kind);
                }
                seat.next(SurgerySeat.raiseTicks(kind) > 0 ? SurgerySeat.Stage.RAISE : SurgerySeat.Stage.IDLE);
                break;
            }
            case RAISE: {
                if (t < SurgerySeat.raiseTicks(kind)) break;
                seat.next(SurgerySeat.Stage.IDLE);
                break;
            }
        }
    }

    static void hurt(Player patient, SurgerySeat.Kind kind) {
        float amount = Math.min(kind == SurgerySeat.Kind.SPINE ? 6.0f : (kind == SurgerySeat.Kind.LIMB ? 4.0f : 2.0f), patient.getHealth() - 1.0f);
        Holder.Reference holder = patient.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DAMAGE);
        if (amount > 0.0f) {
            patient.hurt(new DamageSource((Holder)holder), amount);
        }
    }

    static void recover(Player patient, SurgerySeat.Kind kind) {
        int scale = kind == SurgerySeat.Kind.SPINE ? 2 : 1;
        patient.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1200 * scale, 0));
        patient.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600 * scale, scale - 1));
        if (kind == SurgerySeat.Kind.SPINE) {
            patient.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
        }
    }

    public record Removal(int category, int index) {
    }

    public record Request(ItemStack item, String category, int index, Removal removal) {
        boolean same(Request other) {
            if (this.removal != null || other.removal != null) {
                return this.removal != null && this.removal.equals(other.removal);
            }
            return this.category.equals(other.category) && this.index == other.index;
        }
    }

    private record Frame(Vec3 hip, Vec3 axis, Vec3 front, Vec3 right, Vec3 head, float degrees) {
        Vec3 at(double along, double out, double side) {
            return this.hip.add(this.axis.scale(along)).add(this.front.scale(out)).add(this.right.scale(side));
        }
    }

    public static final class SurgeryApproach {
        private static final Direction[] CANDIDATES = new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

        public static ArmAngleTarget solve(BlockPos arm, boolean ceiling, Vec3 point, List<Box> obstacles) {
            ArmAngleTarget best = null;
            double bestScore = Double.MAX_VALUE;
            for (Direction facing : CANDIDATES) {
                ArmAngleTarget target = new ArmAngleTarget(arm, point, facing, ceiling);
                double score = SurgeryApproach.score(SurgeryClaw.joints(arm, ceiling, target), point, obstacles);
                if (!(score < bestScore - 1.0E-6)) continue;
                bestScore = score;
                best = target;
            }
            return best;
        }

        static double score(Vec3[] joints, Vec3 point, List<Box> obstacles) {
            Vec3 shoulder = joints[0];
            Vec3 elbow = joints[1];
            Vec3 wrist = joints[2];
            Vec3 claw = joints[3];
            int hits = 0;
            for (Box box : obstacles) {
                boolean target = box.contains(point, 0.05);
                if (!SurgeryApproach.passes(shoulder, elbow, box, null) && !SurgeryApproach.passes(elbow, wrist, box, null) && (target || !SurgeryApproach.passes(wrist, claw, box, claw))) continue;
                ++hits;
            }
            Vec3 upper = elbow.subtract(shoulder).normalize();
            Vec3 fore = wrist.subtract(elbow).normalize();
            double elbowAngle = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, upper.scale(-1.0).dot(fore)))));
            double miss = Math.max(0.0, claw.distanceTo(point) - 0.14);
            return (double)(hits * 25) + miss * 150.0 + Math.max(0.0, 70.0 - elbowAngle) * 0.5;
        }

        private static boolean passes(Vec3 from, Vec3 to, Box box, Vec3 tip) {
            for (int i = 0; i <= 24; ++i) {
                Vec3 p = from.lerp(to, (double)i / 24.0);
                if (tip != null && p.distanceTo(tip) < 0.12 || !box.contains(p, -0.02)) continue;
                return true;
            }
            return false;
        }

        public record Box(Vec3 center, Vec3 a, Vec3 b, Vec3 c, double ha, double hb, double hc) {
            boolean contains(Vec3 p, double margin) {
                Vec3 d = p.subtract(this.center);
                return Math.abs(d.dot(this.a)) <= this.ha + margin && Math.abs(d.dot(this.b)) <= this.hb + margin && Math.abs(d.dot(this.c)) <= this.hc + margin;
            }
        }
    }
}

