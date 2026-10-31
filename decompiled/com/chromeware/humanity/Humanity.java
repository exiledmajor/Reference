/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Registry
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.OwnableEntity
 *  net.minecraft.world.entity.TamableAnimal
 *  net.minecraft.world.entity.animal.horse.AbstractHorse
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.npc.AbstractVillager
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.level.Level
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.attachment.AttachmentType
 *  net.neoforged.neoforge.event.entity.living.LivingDamageEvent$Post
 *  net.neoforged.neoforge.event.entity.living.LivingDeathEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$Clone
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.NeoForgeRegistries
 */
package com.chromeware.humanity;

import com.chromeware.body.BodyPart;
import com.chromeware.body.CyberLimbs;
import com.chromeware.config.ChromewareConfig;
import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.CyberwareCapacity;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.CyberwareStats;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantCooldowns;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.cyberware.SandevistanState;
import com.chromeware.cyberware.ZetatechBerserk;
import com.chromeware.humanity.HumanityPackets;
import com.chromeware.humanity.HumanityRules;
import com.chromeware.humanity.HumanityState;
import com.chromeware.humanity.Suppression;
import com.chromeware.launcher.LauncherShots;
import com.chromeware.netrunning.Netrunning;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@EventBusSubscriber(modid="chromeware")
public final class Humanity {
    public static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create((Registry)NeoForgeRegistries.ATTACHMENT_TYPES, (String)"chromeware");
    public static final Supplier<AttachmentType<HumanityState>> STATE = TYPES.register("humanity", () -> AttachmentType.builder(() -> HumanityState.EMPTY).serialize(HumanityState.CODEC).copyOnDeath().build());
    public static final Supplier<AttachmentType<HumanityState.View>> VIEW = TYPES.register("humanity_view", () -> AttachmentType.builder(() -> HumanityState.View.EMPTY).sync(HumanityState.View.STREAM_CODEC).build());
    private static final Set<ResourceLocation> NOT_USES = Set.of(ImplantCooldowns.LAUNCHER, ImplantCooldowns.MONOWIRE, ImplantCooldowns.MANTIS, ImplantCooldowns.GORILLA, ImplantCooldowns.QUANTUM_TUNER, ImplantCooldowns.SANDEVISTAN, ZetatechBerserk.TIMER);
    private static final Map<UUID, Watch> WATCH = new HashMap<UUID, Watch>();

    public static HumanityState state(Player p) {
        return ((HumanityState)p.getData(STATE)).initialized();
    }

    public static HumanityState.View view(Player p) {
        return ChromewareConfig.humanityEnabled() ? (HumanityState.View)p.getData(VIEW) : HumanityState.View.EMPTY;
    }

    public static double chrome(Player p) {
        double limbs = 0.0;
        for (BodyPart part : CyberLimbs.PARTS) {
            if (!CyberLimbs.has(p, part)) continue;
            limbs += ImplantSettings.stat(BuiltInRegistries.ITEM.getKey((Object)CyberLimbs.item(part).getItem()).getPath(), "humanity_loss_percent", 5.0);
        }
        return HumanityRules.clamp(HumanityRules.permanentFor(CyberwareCapacity.used(p)) + limbs);
    }

    public static void strain(Player p, double amount) {
        if (p.level().isClientSide() || amount <= 0.0 || !ChromewareConfig.humanityEnabled()) {
            return;
        }
        HumanityState s = Humanity.state(p);
        p.setData(STATE, (Object)s.with(s.permanent(), s.temporary() + amount));
        Humanity.sync(p, true);
    }

    public static void setStrain(Player p, double temporary) {
        HumanityState s = Humanity.state(p).with(Humanity.state(p).permanent(), temporary);
        p.setData(STATE, (Object)s.stages(HumanityRules.rearm(s.stages(), s.loss())));
        Humanity.sync(p, true);
    }

    public static void clear(Player p) {
        HumanityState s = new HumanityState(Humanity.chrome(p), 0.0, -1, Suppression.NONE).initialized();
        p.setData(STATE, (Object)s);
        Humanity.sync(p, true);
    }

    public static void used(Player p, int capacity) {
        Humanity.strain(p, HumanityRules.useCost(capacity));
    }

    public static void removed(Player p) {
        if (p.level().isClientSide() || !ChromewareConfig.humanityEnabled()) {
            return;
        }
        HumanityState next = Humanity.state(p).relieveStrain(5.0);
        p.setData(STATE, (Object)next.stages(HumanityRules.rearm(next.stages(), next.loss())));
        Humanity.sync(p, true);
    }

    public static double strainDecayPerTick(Player p) {
        double radius = 8.0;
        boolean companion = !p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(radius), mob -> {
            OwnableEntity owned;
            AbstractHorse horse;
            TamableAnimal animal;
            return mob.isAlive() && mob.distanceToSqr((Entity)p) <= radius * radius && (mob instanceof TamableAnimal && (animal = (TamableAnimal)mob).isTame() || mob instanceof AbstractHorse && (horse = (AbstractHorse)mob).isTamed() || mob instanceof OwnableEntity && (owned = (OwnableEntity)mob).getOwnerUUID() != null);
        }).isEmpty();
        return 0.1 * (double)(companion ? 2 : 1);
    }

    public static void armHit(Player p) {
        Humanity.strain(p, 0.1);
    }

    @SubscribeEvent
    public static void launcherHit(LivingDamageEvent.Post event) {
        ServerPlayer p;
        Entity entity;
        if (event.getNewDamage() > 0.0f && LauncherShots.launcherShot(event.getSource().getDirectEntity()) && (entity = event.getSource().getEntity()) instanceof ServerPlayer && (p = (ServerPlayer)entity) != event.getEntity()) {
            Humanity.armHit((Player)p);
        }
    }

    public static void forcePsychosis(ServerPlayer p, int ticks) {
        if (!ChromewareConfig.humanityEnabled()) {
            return;
        }
        Watch watch = WATCH.computeIfAbsent(p.getUUID(), id -> Humanity.watch(p, new Watch()));
        watch.psychosis = Math.max(watch.psychosis, ticks);
        Humanity.sync((Player)p, true);
    }

    public static boolean psychotic(Player p) {
        if (!ChromewareConfig.humanityEnabled()) {
            return false;
        }
        Watch watch = WATCH.get(p.getUUID());
        return watch != null && watch.psychosis > 0;
    }

    public static void quickhack(Player p, int ram) {
        Humanity.strain(p, HumanityRules.quickhackCost(CyberwareStats.get(CyberwareItems.CYBERDECK.toStack()).capacity(), ram));
    }

    public static Application suppress(Player p, Suppression.Grade grade) {
        if (!ChromewareConfig.humanityEnabled()) {
            return new Application(0.0, 0, false);
        }
        if (grade != null && grade != Suppression.Grade.NONE && !p.level().isClientSide()) {
            boolean forced;
            Watch watch = WATCH.get(p.getUUID());
            boolean bl = forced = watch != null && watch.psychosis > 0;
            if (watch != null) {
                watch.psychosis = 0;
                watch.episodeIn = 0;
            }
            Netrunning.clearCyberpsychosis(p);
            if (forced && p instanceof ServerPlayer) {
                ServerPlayer server = (ServerPlayer)p;
                HumanityPackets.send(server, new HumanityPackets.Episode(2, 0, 0.0f, false));
            }
        }
        HumanityState s = Humanity.state(p);
        boolean redose = s.suppression().protecting();
        Suppression.Dose dose = s.suppression().dose(s.permanent(), s.temporary(), grade);
        HumanityState next = new HumanityState(s.permanent() - dose.permanent(), s.temporary() - dose.temporary(), s.stages(), dose.next());
        p.setData(STATE, (Object)next.stages(HumanityRules.rearm(next.stages(), next.loss())));
        Humanity.sync(p, true);
        return new Application(dose.total(), dose.next().protectionTicks(), redose);
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer p = (ServerPlayer)player;
            Humanity.tick(p);
        }
    }

    public static void tick(ServerPlayer p) {
        double loss;
        if (!ChromewareConfig.humanityEnabled()) {
            Watch disabled = WATCH.computeIfAbsent(p.getUUID(), id -> new Watch());
            Humanity.watch(p, disabled);
            disabled.episodeIn = 0;
            disabled.psychosis = 0;
            if (!((HumanityState.View)p.getData(VIEW)).equals(HumanityState.View.EMPTY)) {
                p.setData(VIEW, (Object)HumanityState.View.EMPTY);
            }
            return;
        }
        Watch watch = WATCH.computeIfAbsent(p.getUUID(), id -> Humanity.watch(p, new Watch()));
        Humanity.detectUses(p, watch);
        boolean fresh = ((HumanityState)p.getData(STATE)).stages() < 0;
        HumanityState s = Humanity.state((Player)p);
        double permanent = s.permanent();
        double temporary = s.temporary();
        Suppression held = s.suppression();
        double baseline = Humanity.chrome((Player)p);
        double burden = permanent + held.storedPermanent();
        if (burden < baseline) {
            permanent += baseline - burden;
        } else if (burden > baseline) {
            Suppression.Decayed decayed = held.decayPermanent(Math.min(8.333333333333334E-4, burden - baseline));
            held = decayed.next();
            permanent -= Math.min(8.333333333333334E-4, burden - baseline) - decayed.amount();
        }
        double recovery = temporary > 0.0 || held.storedTemporary() > 0.0 ? Humanity.strainDecayPerTick((Player)p) : 0.1;
        Suppression.Decayed decayed = held.decayTemporary(recovery);
        held = decayed.next();
        boolean protecting = held.protecting();
        Suppression.Returned returned = held.tick(100.0 - HumanityRules.clamp(permanent) - HumanityRules.clamp(temporary -= recovery - decayed.amount()));
        held = returned.next();
        permanent += returned.permanent();
        temporary += returned.temporary();
        if (protecting && !held.protecting() && held.holding()) {
            HumanityPackets.send(p, new HumanityPackets.Notice(1, held.stored()));
        }
        HumanityState next = new HumanityState(permanent, temporary, s.stages(), held);
        if (watch.psychosis > 0) {
            --watch.psychosis;
        }
        double d = loss = watch.psychosis > 0 ? 100.0 : next.loss();
        int stages = fresh ? HumanityRules.reachedStages(loss) : (held.holding() ? next.stages() : HumanityRules.rearm(next.stages(), loss));
        int crossed = HumanityRules.crossed(stages, loss);
        if (crossed >= 0) {
            stages |= HumanityRules.reachedStages(loss);
            if (p.isAlive() && !p.isSpectator()) {
                HumanityPackets.send(p, new HumanityPackets.Episode(0, crossed, HumanityRules.intensity(loss), false));
            }
        }
        p.setData(STATE, (Object)next.stages(stages));
        Humanity.episodes(p, watch, loss);
        Humanity.sync((Player)p, crossed >= 0 || p.tickCount % 10 == 0);
    }

    private static void episodes(ServerPlayer p, Watch watch, double loss) {
        int left;
        int interval = HumanityRules.episodeInterval(loss);
        if (interval == 0 || !p.isAlive() || p.isSpectator()) {
            watch.episodeIn = 0;
            return;
        }
        int n = left = watch.episodeIn <= 0 ? interval : Math.min(watch.episodeIn, interval);
        if (--left <= 0) {
            boolean berserk = p.getRandom().nextDouble() < HumanityRules.berserkChance(loss);
            HumanityPackets.send(p, new HumanityPackets.Episode(1, -1, HumanityRules.intensity(loss), berserk));
            left = interval;
        }
        watch.episodeIn = left;
    }

    private static void detectUses(ServerPlayer p, Watch watch) {
        boolean berserk;
        boolean sandevistan;
        boolean bl = sandevistan = ((SandevistanState)p.getData(PlayerData.SANDEVISTAN_STATE)).activeTicks() > 0;
        if (sandevistan && !watch.sandevistan) {
            Humanity.used((Player)p, CyberwareStats.get(CyberwareItems.SANDEVISTAN.toStack()).capacity());
        }
        if ((berserk = ZetatechBerserk.active((Player)p)) && !watch.berserk) {
            Humanity.used((Player)p, CyberwareStats.get(Implant.ZETATECH_BERSERK.stack()).capacity());
        }
        Map<ResourceLocation, Integer> before = Map.copyOf(watch.timers);
        Humanity.watch(p, watch);
        watch.timers.forEach((id, remaining) -> {
            Integer previous = (Integer)before.get(id);
            if (previous != null && remaining > previous && !NOT_USES.contains(id)) {
                Humanity.used((Player)p, Humanity.timerCapacity(id));
            }
        });
    }

    private static Watch watch(ServerPlayer p, Watch watch) {
        watch.sandevistan = ((SandevistanState)p.getData(PlayerData.SANDEVISTAN_STATE)).activeTicks() > 0;
        watch.berserk = ZetatechBerserk.active((Player)p);
        watch.timers.clear();
        ImplantCooldowns.forEachRemaining((Player)p, watch.timers::put);
        return watch;
    }

    private static int timerCapacity(ResourceLocation id) {
        Item item = id.getPath().equals("cyberdeck_overclock") ? (Item)CyberwareItems.CYBERDECK.get() : (Item)BuiltInRegistries.ITEM.get(id);
        return CyberwareStats.get(item.getDefaultInstance()).capacity();
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void died(LivingDeathEvent event) {
        OwnableEntity pet;
        ServerPlayer p;
        ServerPlayer killer;
        LivingEntity attacker;
        LivingEntity victim = event.getEntity();
        if (event.isCanceled() || victim.level().isClientSide() || victim instanceof ArmorStand) {
            return;
        }
        Entity entity = event.getSource().getEntity();
        LivingEntity credit = entity instanceof LivingEntity ? (attacker = (LivingEntity)entity) : victim.getKillCredit();
        ServerPlayer serverPlayer = killer = credit instanceof ServerPlayer && (p = (ServerPlayer)credit) != victim && !p.isSpectator() ? p : null;
        if (victim instanceof OwnableEntity && (pet = (OwnableEntity)victim).getOwnerUUID() != null) {
            ServerPlayer owner = Humanity.player(victim.level(), pet.getOwnerUUID());
            if (killer != null && killer == owner) {
                Humanity.strain((Player)owner, 30.0);
                return;
            }
            if (owner != null) {
                Humanity.strain((Player)owner, 15.0);
            }
            if (killer != null) {
                Humanity.strain((Player)killer, 1.0);
            }
            return;
        }
        if (killer == null) {
            return;
        }
        Humanity.strain((Player)killer, victim instanceof Player || victim instanceof AbstractVillager ? 1.0 : 0.25);
    }

    private static ServerPlayer player(Level level, UUID id) {
        if (!(level instanceof ServerLevel)) {
            return null;
        }
        ServerLevel server = (ServerLevel)level;
        ServerPlayer listed = server.getServer().getPlayerList().getPlayer(id);
        if (listed != null) {
            return listed;
        }
        for (ServerLevel other : server.getServer().getAllLevels()) {
            Player player = other.getPlayerByUUID(id);
            if (!(player instanceof ServerPlayer)) continue;
            ServerPlayer p = (ServerPlayer)player;
            return p;
        }
        return null;
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        WATCH.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void respawned(PlayerEvent.Clone event) {
        if (!event.isWasDeath() || event.getEntity().level().isClientSide() || !ChromewareConfig.humanityEnabled()) {
            return;
        }
        Player p = event.getEntity();
        HumanityState s = Humanity.state(p);
        Watch watch = WATCH.get(p.getUUID());
        if (watch != null) {
            watch.psychosis = 0;
        }
        Suppression held = s.suppression().decayTemporary(Double.MAX_VALUE).next();
        p.setData(STATE, (Object)new HumanityState(s.permanent(), 0.0, HumanityRules.rearm(s.stages(), s.permanent() + 0.0), held));
        Humanity.sync(p, true);
    }

    private static void sync(Player p, boolean due) {
        if (!due) {
            return;
        }
        HumanityState s = Humanity.state(p);
        Suppression held = s.suppression();
        double temporary = Humanity.psychotic(p) ? 100.0 - HumanityRules.clamp(s.permanent()) : s.temporary();
        HumanityState.View view = new HumanityState.View((float)s.permanent(), (float)temporary, (float)Math.min(Humanity.chrome(p), s.permanent()), (float)held.stored(), (held.protectionTicks() + 19) / 20 * 20, held.returning());
        if (!view.equals(p.getData(VIEW))) {
            p.setData(VIEW, (Object)view);
        }
    }

    private static final class Watch {
        boolean sandevistan;
        boolean berserk;
        final Map<ResourceLocation, Integer> timers = new HashMap<ResourceLocation, Integer>();
        int episodeIn;
        int psychosis;

        private Watch() {
        }
    }

    public record Application(double removed, int protectionTicks, boolean redose) {
    }
}

