/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.minecraft.core.Holder
 *  net.minecraft.core.UUIDUtil
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageType
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.EntityJoinLevelEvent
 *  net.neoforged.neoforge.event.server.ServerStoppedEvent
 *  net.neoforged.neoforge.event.tick.ServerTickEvent$Post
 */
package com.chromeware.cyberware;

import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.PlayerData;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid="chromeware")
public final class GorillaBleeding {
    public static final int DURATION = 60;
    public static final ResourceKey<DamageType> DAMAGE_TYPE = ResourceKey.create((ResourceKey)Registries.DAMAGE_TYPE, (ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"gorilla_bleed"));
    private static final Map<ServerLevel, Set<UUID>> ACTIVE = new WeakHashMap<ServerLevel, Set<UUID>>();

    public static boolean active(LivingEntity entity) {
        return entity.hasData(PlayerData.BLEEDING) && (Boolean)entity.getData(PlayerData.BLEEDING) != false;
    }

    public static void apply(LivingEntity target, Player attacker) {
        ServerLevel level;
        block3: {
            block2: {
                Level level2 = target.level();
                if (!(level2 instanceof ServerLevel)) break block2;
                level = (ServerLevel)level2;
                if (target.isAlive()) break block3;
            }
            return;
        }
        target.setData(PlayerData.GORILLA_BLEED, (Object)new State(target.getMaxHealth() * ImplantSettings.fstat("gorilla_arms", "bleed_health_fraction", 0.05), ImplantSettings.ticks("gorilla_arms", "bleed_duration_seconds", 60), Optional.of(attacker.getUUID())));
        target.setData(PlayerData.BLEEDING, (Object)true);
        target.setSprinting(false);
        ACTIVE.computeIfAbsent(level, key -> new HashSet()).add(target.getUUID());
    }

    public static void tick(LivingEntity target) {
        Level level = target.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        State s = (State)target.getData(PlayerData.GORILLA_BLEED);
        if (!target.isAlive()) {
            s = State.EMPTY;
        }
        if (s.ticks() > 0) {
            target.setSprinting(false);
            if (s.payment() > 0.0f) {
                Player owner = s.attacker().map(id -> level2.getServer().getPlayerList().getPlayer(id)).orElse(null);
                if (owner == null) {
                    owner = s.attacker().map(arg_0 -> ((ServerLevel)level2).getPlayerByUUID(arg_0)).orElse(null);
                }
                DamageSource source = new DamageSource((Holder)level2.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DAMAGE_TYPE), (Entity)owner);
                target.hurt(source, s.payment());
            }
            s = target.isAlive() ? s.tick() : State.EMPTY;
        }
        target.setData(PlayerData.GORILLA_BLEED, (Object)s);
        if (GorillaBleeding.active(target) != s.ticks() > 0) {
            target.setData(PlayerData.BLEEDING, (Object)(s.ticks() > 0 ? 1 : 0));
        }
    }

    @SubscribeEvent
    public static void joined(EntityJoinLevelEvent event) {
        Level level = event.getLevel();
        if (level instanceof ServerLevel) {
            LivingEntity e;
            ServerLevel level2 = (ServerLevel)level;
            level = event.getEntity();
            if (level instanceof LivingEntity && (e = (LivingEntity)level).hasData(PlayerData.GORILLA_BLEED)) {
                boolean active = ((State)e.getData(PlayerData.GORILLA_BLEED)).ticks() > 0;
                e.setData(PlayerData.BLEEDING, (Object)active);
                if (active) {
                    ACTIVE.computeIfAbsent(level2, key -> new HashSet()).add(e.getUUID());
                }
            }
        }
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            Set<UUID> ids = ACTIVE.get(level);
            if (ids == null) continue;
            for (UUID id : List.copyOf(ids)) {
                Entity entity = level.getEntity(id);
                if (entity instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity)entity;
                    GorillaBleeding.tick(target);
                    if (GorillaBleeding.active(target)) continue;
                    ids.remove(id);
                    continue;
                }
                ids.remove(id);
            }
            if (!ids.isEmpty()) continue;
            ACTIVE.remove(level);
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        ACTIVE.clear();
    }

    public record State(float remaining, int ticks, Optional<UUID> attacker) {
        public static final State EMPTY = new State(0.0f, 0, Optional.empty());
        public static final Codec<State> CODEC = RecordCodecBuilder.create(i -> i.group((App)Codec.floatRange((float)0.0f, (float)Float.MAX_VALUE).fieldOf("remaining").forGetter(State::remaining), (App)Codec.intRange((int)0, (int)60).fieldOf("ticks").forGetter(State::ticks), (App)UUIDUtil.CODEC.optionalFieldOf("attacker").forGetter(State::attacker)).apply((Applicative)i, State::new));

        public float payment() {
            return this.ticks > 0 && this.ticks % 20 == 1 ? this.remaining / (float)(1 + this.ticks / 20) : 0.0f;
        }

        public State tick() {
            return this.ticks > 1 ? new State(Math.max(0.0f, this.remaining - this.payment()), this.ticks - 1, this.attacker) : EMPTY;
        }
    }
}

