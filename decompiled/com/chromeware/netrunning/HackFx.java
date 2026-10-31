/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.joml.Vector3f
 */
package com.chromeware.netrunning;

import com.chromeware.netrunning.Quickhack;
import java.util.Collection;
import java.util.function.Consumer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

public final class HackFx {
    public static final int LAND = 0;
    public static final int CHAIN = 1;
    public static Consumer<Fx> receiver = fx -> {};

    public static int color(Quickhack hack) {
        return switch (hack) {
            case Quickhack.SHORT_CIRCUIT -> 10148095;
            case Quickhack.CONTAGION -> 7069770;
            case Quickhack.BLACKWALL_GATEWAY -> 13635628;
            default -> {
                switch (hack.kind) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case COMBAT: {
                        yield 16747050;
                    }
                    case CONTROL: {
                        yield 16765498;
                    }
                    case COVERT: {
                        yield 2810056;
                    }
                    case ULTIMATE: {
                        yield 14696703;
                    }
                    case DEVICE: 
                }
                yield 8378623;
            }
        };
    }

    private static void send(Entity around, Fx fx) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf((Entity)around, (CustomPacketPayload)fx, (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    static void chain(Entity from, Entity to, Quickhack hack) {
        HackFx.send(to, new Fx(1, hack.ordinal(), from.getId(), to.getId(), 0));
    }

    static void landed(Entity caster, Entity target, Quickhack hack, boolean mechanical) {
        Level level = target.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        int n = hack.ordinal();
        int n2 = caster.getId();
        int n3 = target.getId();
        HackFx.send(target, new Fx(0, n, n2, n3, switch (hack) {
            case Quickhack.SHORT_CIRCUIT -> 12;
            case Quickhack.OVERHEAT -> hack.duration;
            case Quickhack.CONTAGION -> hack.duration;
            case Quickhack.BLACKWALL_GATEWAY -> 40;
            default -> 8;
        }));
        switch (hack) {
            case SHORT_CIRCUIT: {
                HackFx.burst(level2, target, (ParticleOptions)ParticleTypes.ELECTRIC_SPARK, mechanical ? 48 : 28, 0.25);
                HackFx.burst(level2, target, (ParticleOptions)HackFx.dust(0xDDF4FF, 1.2f), 16, 0.05);
                if (mechanical) {
                    HackFx.burst(level2, target, (ParticleOptions)ParticleTypes.LARGE_SMOKE, 6, 0.02);
                }
                HackFx.sound(target, SoundEvents.LIGHTNING_BOLT_IMPACT, 0.5f, 1.8f);
                HackFx.sound(target, SoundEvents.BEACON_DEACTIVATE, 0.6f, 2.0f);
                break;
            }
            case OVERHEAT: {
                HackFx.burst(level2, target, (ParticleOptions)ParticleTypes.WHITE_SMOKE, 14, 0.03);
                HackFx.burst(level2, target, (ParticleOptions)ParticleTypes.SMALL_FLAME, 12, 0.02);
                HackFx.burst(level2, target, (ParticleOptions)ParticleTypes.LAVA, 3, 0.0);
                HackFx.sound(target, SoundEvents.FIRECHARGE_USE, 0.8f, 0.8f);
                HackFx.sound(target, SoundEvents.LAVA_EXTINGUISH, 0.5f, 1.4f);
                break;
            }
            case CONTAGION: {
                HackFx.burst(level2, target, (ParticleOptions)HackFx.dust(7069770, 1.4f), 24, 0.02);
                HackFx.burst(level2, target, (ParticleOptions)HackFx.dust(3045918, 1.8f), 10, 0.01);
                HackFx.sound(target, SoundEvents.SLIME_SQUISH, 0.9f, 0.6f);
                HackFx.sound(target, SoundEvents.BREWING_STAND_BREW, 0.5f, 1.6f);
                break;
            }
            case BLACKWALL_GATEWAY: {
                HackFx.burst(level2, target, (ParticleOptions)HackFx.dust(13635628, 1.6f), 36, 0.03);
                HackFx.burst(level2, target, (ParticleOptions)HackFx.dust(1179652, 2.0f), 24, 0.02);
                HackFx.burst(level2, target, (ParticleOptions)ParticleTypes.ELECTRIC_SPARK, 10, 0.1);
                HackFx.sound(target, SoundEvents.WARDEN_SONIC_CHARGE, 0.9f, 0.6f);
                HackFx.sound(target, (SoundEvent)SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), 0.8f, 0.5f);
                break;
            }
            default: {
                HackFx.burst(level2, target, (ParticleOptions)ParticleTypes.ELECTRIC_SPARK, 12, 0.1);
                HackFx.sound(target, SoundEvents.BEACON_DEACTIVATE, 0.4f, 2.0f);
            }
        }
    }

    static void ongoing(Entity target, Collection<Quickhack> hacks, long time) {
        ServerLevel level;
        block8: {
            block7: {
                Level level2 = target.level();
                if (!(level2 instanceof ServerLevel)) break block7;
                level = (ServerLevel)level2;
                if (time % 5L == 0L) break block8;
            }
            return;
        }
        if (hacks.contains((Object)Quickhack.OVERHEAT)) {
            HackFx.burst(level, target, (ParticleOptions)ParticleTypes.FALLING_LAVA, 2, 0.0);
            HackFx.burst(level, target, (ParticleOptions)ParticleTypes.SMALL_FLAME, 2, 0.01);
            if (time % 10L == 0L) {
                HackFx.burst(level, target, (ParticleOptions)ParticleTypes.WHITE_SMOKE, 2, 0.02);
            }
        }
        if (hacks.contains((Object)Quickhack.CONTAGION)) {
            HackFx.burst(level, target, (ParticleOptions)HackFx.dust(7069770, 1.1f), 4, 0.01);
            if (time % 20L == 0L) {
                HackFx.burst(level, target, (ParticleOptions)ParticleTypes.SNEEZE, 2, 0.01);
            }
        }
    }

    private static DustParticleOptions dust(int rgb, float size) {
        return new DustParticleOptions(new Vector3f((float)(rgb >> 16 & 0xFF) / 255.0f, (float)(rgb >> 8 & 0xFF) / 255.0f, (float)(rgb & 0xFF) / 255.0f), size);
    }

    private static void burst(ServerLevel level, Entity e, ParticleOptions type, int count, double speed) {
        double w = (double)e.getBbWidth() * 0.45;
        double h = (double)e.getBbHeight() * 0.4;
        level.sendParticles(type, e.getX(), e.getY() + (double)e.getBbHeight() * 0.5, e.getZ(), count, w, h, w, speed);
    }

    private static void sound(Entity e, SoundEvent event, float volume, float pitch) {
        e.level().playSound(null, e.getX(), e.getY(), e.getZ(), event, SoundSource.PLAYERS, volume, pitch);
    }

    public record Fx(int kind, int hack, int from, int to, int ticks) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<Fx> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"hack_fx"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Fx> CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.VAR_INT, Fx::kind, (StreamCodec)ByteBufCodecs.VAR_INT, Fx::hack, (StreamCodec)ByteBufCodecs.VAR_INT, Fx::from, (StreamCodec)ByteBufCodecs.VAR_INT, Fx::to, (StreamCodec)ByteBufCodecs.VAR_INT, Fx::ticks, Fx::new);

        public CustomPacketPayload.Type<Fx> type() {
            return TYPE;
        }
    }
}

