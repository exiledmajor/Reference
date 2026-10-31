/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.core.Registry
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.neoforged.neoforge.attachment.AttachmentType
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.NeoForgeRegistries
 */
package com.chromeware.cyberware;

import com.chromeware.cyberware.ArmImplant;
import com.chromeware.cyberware.ArmRecharge;
import com.chromeware.cyberware.BerserkState;
import com.chromeware.cyberware.BloodPumpState;
import com.chromeware.cyberware.CyberwareSlots;
import com.chromeware.cyberware.GorillaBleeding;
import com.chromeware.cyberware.GorillaState;
import com.chromeware.cyberware.ImplantColors;
import com.chromeware.cyberware.ImplantData;
import com.chromeware.cyberware.InstalledImplant;
import com.chromeware.cyberware.QuantumTunerState;
import com.chromeware.cyberware.SandevistanState;
import com.chromeware.mantis.MantisState;
import com.chromeware.monowire.MonowireState;
import com.chromeware.netrunning.DeckState;
import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class PlayerData {
    public static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create((Registry)NeoForgeRegistries.ATTACHMENT_TYPES, (String)"chromeware");
    public static final Supplier<AttachmentType<Integer>> HACK_FLAGS = TYPES.register("hack_flags", () -> AttachmentType.builder(() -> 0).sync(ByteBufCodecs.VAR_INT).build());
    public static final Supplier<AttachmentType<Boolean>> DECK_OPEN = TYPES.register("deck_open", () -> AttachmentType.builder(() -> false).sync(ByteBufCodecs.BOOL).build());
    public static final Supplier<AttachmentType<DeckState>> CYBERDECK = TYPES.register("cyberdeck_state", () -> AttachmentType.builder(() -> DeckState.EMPTY).serialize(DeckState.CODEC).copyOnDeath().sync(DeckState.STREAM_CODEC).build());
    public static final Supplier<AttachmentType<ArmImplant>> ARM_IMPLANT = TYPES.register("arm_implant", () -> AttachmentType.builder(() -> ArmImplant.EMPTY).serialize(ArmImplant.CODEC).copyOnDeath().sync(ArmImplant.STREAM_CODEC).build());
    public static final Supplier<AttachmentType<ImplantColors>> IMPLANT_COLORS = TYPES.register("implant_colors", () -> AttachmentType.builder(() -> ImplantColors.EMPTY).serialize(ImplantColors.CODEC).copyOnDeath().sync(ImplantColors.STREAM_CODEC).build());
    public static final Supplier<AttachmentType<ArmImplant>> ARM_IMPLANT_LEFT = TYPES.register("arm_implant_left", () -> AttachmentType.builder(() -> ArmImplant.EMPTY).serialize(ArmImplant.CODEC).copyOnDeath().sync(ArmImplant.STREAM_CODEC).build());
    public static final Supplier<AttachmentType<InstalledImplant>> OPERATING_SYSTEM = TYPES.register("operating_system", () -> AttachmentType.builder(() -> InstalledImplant.EMPTY).serialize(InstalledImplant.CODEC).copyOnDeath().sync(InstalledImplant.STREAM_CODEC).build());
    public static final Supplier<AttachmentType<CyberwareSlots>> CIRCULATORY = TYPES.register("circulatory_system", () -> AttachmentType.builder(() -> CyberwareSlots.EMPTY).serialize(CyberwareSlots.CODEC).copyOnDeath().sync(CyberwareSlots.STREAM_CODEC).build());
    public static final Supplier<AttachmentType<CyberwareSlots>> FRONTAL_CORTEX = TYPES.register("frontal_cortex", () -> AttachmentType.builder(() -> CyberwareSlots.EMPTY).serialize(CyberwareSlots.CODEC).copyOnDeath().sync(CyberwareSlots.STREAM_CODEC).build());
    public static final Supplier<AttachmentType<BloodPumpState>> BLOOD_PUMP_STATE = TYPES.register("blood_pump_state", () -> AttachmentType.builder(() -> new BloodPumpState(BloodPumpState.maxCharges(), 0, 0)).serialize(BloodPumpState.CODEC).copyOnDeath().sync(BloodPumpState.STREAM_CODEC).build());
    public static final Supplier<AttachmentType<CyberwareSlots>> SKELETON = TYPES.register("skeleton", () -> AttachmentType.builder(() -> CyberwareSlots.EMPTY).serialize(CyberwareSlots.CODEC).copyOnDeath().sync(CyberwareSlots.STREAM_CODEC).build());
    public static final Supplier<AttachmentType<Integer>> CAPACITY_LIMIT = TYPES.register("capacity_limit", () -> AttachmentType.builder(() -> 38).serialize(Codec.intRange((int)0, (int)10000).xmap(limit -> limit == 100 ? 38 : limit, limit -> limit)).copyOnDeath().sync(ByteBufCodecs.VAR_INT).build());
    public static final Supplier<AttachmentType<Boolean>> CAPACITY_CUSTOM_LIMIT = TYPES.register("capacity_custom_limit", () -> AttachmentType.builder(() -> false).serialize((Codec)Codec.BOOL).copyOnDeath().sync(ByteBufCodecs.BOOL).build());
    public static final Supplier<AttachmentType<Integer>> CAPACITY_BONUS = TYPES.register("capacity_bonus", () -> AttachmentType.builder(() -> 0).serialize(Codec.intRange((int)0, (int)10000)).copyOnDeath().sync(ByteBufCodecs.VAR_INT).build());
    public static final Supplier<AttachmentType<CyberwareSlots>> NERVOUS_SYSTEM = PlayerData.sockets("nervous_system");
    public static final Supplier<AttachmentType<CyberwareSlots>> INTEGUMENTARY_SYSTEM = PlayerData.sockets("integumentary_system");
    public static final Supplier<AttachmentType<CyberwareSlots>> LEGS = PlayerData.sockets("legs");
    public static final Supplier<AttachmentType<CyberwareSlots>> HANDS = PlayerData.sockets("hands");
    public static final Supplier<AttachmentType<CyberwareSlots>> FACE = PlayerData.sockets("face");
    public static final Supplier<AttachmentType<ImplantData>> CATALOGUE_STATE = TYPES.register("catalogue_state", () -> AttachmentType.builder(() -> ImplantData.EMPTY).serialize(ImplantData.CODEC).copyOnDeath().build());
    public static final Supplier<AttachmentType<Integer>> CATALOGUE_FLAGS = TYPES.register("catalogue_flags", () -> AttachmentType.builder(() -> 0).sync(ByteBufCodecs.VAR_INT).build());
    public static final Supplier<AttachmentType<Integer>> COMBAT_TICKS = TYPES.register("combat_ticks", () -> AttachmentType.builder(() -> 0).serialize(Codec.intRange((int)0, (int)200)).sync(ByteBufCodecs.VAR_INT).build());
    public static final Supplier<AttachmentType<Integer>> TYROSINE_TICKS = TYPES.register("tyrosine_ticks", () -> AttachmentType.builder(() -> 0).sync(ByteBufCodecs.VAR_INT).build());
    public static final Supplier<AttachmentType<Integer>> TYROSINE_STACKS = TYPES.register("tyrosine_stacks", () -> AttachmentType.builder(() -> 0).sync(ByteBufCodecs.VAR_INT).build());
    public static final Supplier<AttachmentType<BerserkState>> BERSERK_STATE = TYPES.register("berserk_state", () -> AttachmentType.builder(() -> BerserkState.READY).serialize(BerserkState.CODEC).copyOnDeath().sync(BerserkState.STREAM_CODEC).build());
    public static final Supplier<AttachmentType<SandevistanState>> SANDEVISTAN_STATE = TYPES.register("sandevistan_state", () -> AttachmentType.builder(() -> SandevistanState.READY).serialize(SandevistanState.CODEC).copyOnDeath().sync(SandevistanState.STREAM_CODEC).build());
    public static final Supplier<AttachmentType<Integer>> HUNGER_REFUND_MILLI = TYPES.register("hunger_refund_milli", () -> AttachmentType.builder(() -> 0).serialize(Codec.intRange((int)0, (int)999)).build());
    public static final Supplier<AttachmentType<Boolean>> MAMBA_POISON = TYPES.register("mamba_poison", () -> AttachmentType.builder(() -> false).serialize((Codec)Codec.BOOL).build());
    public static final Supplier<AttachmentType<Boolean>> CAPACITY_ENFORCED = TYPES.register("capacity_enforced", () -> AttachmentType.builder(() -> true).serialize((Codec)Codec.BOOL).copyOnDeath().sync(ByteBufCodecs.BOOL).build());
    public static final Supplier<AttachmentType<QuantumTunerState>> QUANTUM_TUNER_STATE = TYPES.register("quantum_tuner_state", () -> AttachmentType.builder(() -> new QuantumTunerState(QuantumTunerState.reserveCapacity(), 0, 0)).serialize(QuantumTunerState.CODEC).copyOnDeath().sync(QuantumTunerState.STREAM_CODEC).build());
    public static final Supplier<AttachmentType<GorillaState>> GORILLA_STATE = TYPES.register("gorilla_state", () -> AttachmentType.builder(() -> GorillaState.NONE).sync(GorillaState.STREAM_CODEC).build());
    public static final Supplier<AttachmentType<GorillaBleeding.State>> GORILLA_BLEED = TYPES.register("gorilla_bleed", () -> AttachmentType.builder(() -> GorillaBleeding.State.EMPTY).serialize(GorillaBleeding.State.CODEC).build());
    public static final Supplier<AttachmentType<Boolean>> BLEEDING = TYPES.register("bleeding", () -> AttachmentType.builder(() -> false).sync(ByteBufCodecs.BOOL).build());
    public static final Supplier<AttachmentType<MonowireState>> MONOWIRE_STATE = TYPES.register("monowire_state", () -> AttachmentType.builder(() -> MonowireState.RETRACTED).sync(MonowireState.STREAM_CODEC).build());
    public static final Supplier<AttachmentType<MonowireState>> MONOWIRE_STATE_LEFT = TYPES.register("monowire_state_left", () -> AttachmentType.builder(() -> MonowireState.RETRACTED).sync(MonowireState.STREAM_CODEC).build());
    public static final Supplier<AttachmentType<Boolean>> LAUNCHER_DEPLOYED = TYPES.register("launcher_deployed", () -> AttachmentType.builder(() -> false).sync(ByteBufCodecs.BOOL).build());
    public static final Supplier<AttachmentType<Integer>> LAUNCHER_SHOT = TYPES.register("launcher_shot", () -> AttachmentType.builder(() -> 0).sync(ByteBufCodecs.VAR_INT).build());
    public static final Supplier<AttachmentType<Boolean>> LAUNCHER_DEPLOYED_RIGHT = TYPES.register("launcher_deployed_right", () -> AttachmentType.builder(() -> false).sync(ByteBufCodecs.BOOL).build());
    public static final Supplier<AttachmentType<Integer>> LAUNCHER_SHOT_RIGHT = TYPES.register("launcher_shot_right", () -> AttachmentType.builder(() -> 0).sync(ByteBufCodecs.VAR_INT).build());
    public static final Supplier<AttachmentType<Boolean>> LAUNCHER_FIRING = TYPES.register("launcher_firing", () -> AttachmentType.builder(() -> false).sync(ByteBufCodecs.BOOL).build());
    public static final Supplier<AttachmentType<Boolean>> LAUNCHER_FIRING_RIGHT = TYPES.register("launcher_firing_right", () -> AttachmentType.builder(() -> false).sync(ByteBufCodecs.BOOL).build());
    public static final Supplier<AttachmentType<Integer>> LAUNCHER_HEAVY = TYPES.register("launcher_heavy", () -> AttachmentType.builder(() -> 0).sync(ByteBufCodecs.VAR_INT).build());
    public static final Supplier<AttachmentType<Integer>> LAUNCHER_HEAVY_RIGHT = TYPES.register("launcher_heavy_right", () -> AttachmentType.builder(() -> 0).sync(ByteBufCodecs.VAR_INT).build());
    public static final Supplier<AttachmentType<MantisState>> MANTIS_STATE = TYPES.register("mantis_state", () -> AttachmentType.builder(() -> MantisState.RETRACTED).sync(MantisState.STREAM_CODEC).build());
    public static final Supplier<AttachmentType<MantisState>> MANTIS_STATE_LEFT = TYPES.register("mantis_state_left", () -> AttachmentType.builder(() -> MantisState.RETRACTED).sync(MantisState.STREAM_CODEC).build());
    public static final Supplier<AttachmentType<ArmRecharge.State>> ARM_RECHARGE = TYPES.register("arm_recharge", () -> AttachmentType.builder(() -> ArmRecharge.State.NONE).sync(ArmRecharge.State.STREAM_CODEC).build());

    private static Supplier<AttachmentType<CyberwareSlots>> sockets(String key) {
        return TYPES.register(key, () -> AttachmentType.builder(() -> CyberwareSlots.EMPTY).serialize(CyberwareSlots.CODEC).copyOnDeath().sync(CyberwareSlots.STREAM_CODEC).build());
    }
}

