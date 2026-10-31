/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.particles.SimpleParticleType
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.tags.TagKey
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.NeutralMob
 *  net.minecraft.world.entity.ai.Brain
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.memory.MemoryModuleType
 *  net.minecraft.world.entity.boss.enderdragon.EnderDragon
 *  net.minecraft.world.entity.boss.wither.WitherBoss
 *  net.minecraft.world.entity.monster.ElderGuardian
 *  net.minecraft.world.entity.monster.Enemy
 *  net.minecraft.world.entity.monster.warden.Warden
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.FishingHook
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.item.BowItem
 *  net.minecraft.world.item.CrossbowItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.Level$ExplosionInteraction
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.common.NeoForgeMod
 *  net.neoforged.neoforge.event.entity.EntityJoinLevelEvent
 *  net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent
 *  net.neoforged.neoforge.event.entity.living.LivingDamageEvent$Post
 *  net.neoforged.neoforge.event.entity.living.LivingDeathEvent
 *  net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerChangedDimensionEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.neoforged.neoforge.event.tick.ServerTickEvent$Post
 */
package com.chromeware.netrunning;

import com.chromeware.body.BodyPart;
import com.chromeware.body.CyberLimbs;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbState;
import com.chromeware.compat.guns.GunCyberware;
import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.cyberware.CooldownNotice;
import com.chromeware.cyberware.CyberwareCapacity;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.DisabledNotice;
import com.chromeware.cyberware.EnemyKills;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantCooldowns;
import com.chromeware.cyberware.ImplantCriticals;
import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.cyberware.Installing;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.cyberware.QuantumTuner;
import com.chromeware.cyberware.SandevistanImplant;
import com.chromeware.cyberware.TrueDamage;
import com.chromeware.cyberware.ZetatechBerserk;
import com.chromeware.humanity.Humanity;
import com.chromeware.launcher.ProjectileLauncher;
import com.chromeware.netrunning.DeckState;
import com.chromeware.netrunning.Detonation;
import com.chromeware.netrunning.DeviceHacks;
import com.chromeware.netrunning.HackFx;
import com.chromeware.netrunning.NetrunnerImplants;
import com.chromeware.netrunning.NetrunningPackets;
import com.chromeware.netrunning.Quickhack;
import com.chromeware.netrunning.QuickhackItem;
import com.chromeware.sandevistan.Sandevistan;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid="chromeware")
public final class Netrunning {
    public static final double RANGE = 32.0;
    public static final int LOCKED = 1;
    public static final int JAMMED = 2;
    public static final int MALFUNCTION = 4;
    public static final int BLIND = 8;
    public static final TagKey<EntityType<?>> MECHANICAL = TagKey.create((ResourceKey)Registries.ENTITY_TYPE, (ResourceLocation)ResourceLocation.parse((String)"chromeware:mechanical_targets"));
    private static final int CONTROL_EXTENSION = 60;
    private static final int DISABLE_TICKS = 60;
    private static final int MALFUNCTION_MAX = 8;
    private static final int MALFUNCTION_PERMANENT = 2;
    private static final int OVERCLOCK_PER_KILL = 60;
    private static final float HITSCAN_MISS = 0.85f;
    private static final Set<Quickhack> CONTROL = EnumSet.of(Quickhack.REBOOT_OPTICS, Quickhack.WEAPON_GLITCH, Quickhack.CRIPPLE_MOVEMENT, Quickhack.CYBERWARE_MALFUNCTION);
    private static final int BACKUP_RANGE = 30;
    private static final int BAITED_BACKUP = 5;
    private static final int BLACKWALL_SPREADS = 3;
    private static final int BLACKWALL_RANGE = 20;
    private static final double ARRIVED = 2.0;
    private static final ResourceLocation MELT = ResourceLocation.parse((String)"chromeware:quickhack_overheat");
    private static final ResourceLocation NO_JUMP = ResourceLocation.parse((String)"chromeware:quickhack_cripple");
    private static final Map<ServerLevel, World> WORLDS = new WeakHashMap<ServerLevel, World>();
    public static Predicate<Entity> veiled = e -> false;
    private static final ThreadLocal<Boolean> SILENT = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> FORCING = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> HACK_DAMAGE = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> OVERHEAT_FIRE = ThreadLocal.withInitial(() -> false);

    private static World world(ServerLevel l) {
        return WORLDS.computeIfAbsent(l, ignored -> new World());
    }

    public static DeckState state(Player p) {
        return (DeckState)p.getData(PlayerData.CYBERDECK);
    }

    public static void state(Player p, DeckState s) {
        if (!s.equals(Netrunning.state(p))) {
            p.setData(PlayerData.CYBERDECK, (Object)s);
        }
    }

    public static boolean installed(Player p) {
        return SandevistanImplant.get(p).item().is(CyberwareItems.CYBERDECK);
    }

    public static boolean usable(Player p) {
        return Netrunning.runnable(p) && ImplantEffects.hasOptics(p);
    }

    public static boolean needsOptics(Player p) {
        return Netrunning.runnable(p) && !ImplantEffects.hasOptics(p);
    }

    private static boolean runnable(Player p) {
        LimbState body = LimbAttachments.get(p);
        return Netrunning.installed(p) && p.isAlive() && !p.isSpectator() && !body.isMissing(BodyPart.HEAD) && !body.isMissing(BodyPart.BODY) && (Netrunning.flags((Entity)p) & 4) == 0;
    }

    public static boolean silentDeath() {
        return SILENT.get();
    }

    public static int flags(Entity e) {
        return e.hasData(PlayerData.HACK_FLAGS) ? (Integer)e.getData(PlayerData.HACK_FLAGS) : 0;
    }

    public static int disabledTicks(Player p) {
        Effects e = Netrunning.effects((Entity)p);
        return e == null ? 0 : (e.stacks >= 2 ? -1 : Math.max(e.disabled, e.timers.getOrDefault((Object)Quickhack.CYBERWARE_MALFUNCTION, 0)));
    }

    public static void clearCyberpsychosis(Player p) {
        Effects e = Netrunning.effects((Entity)p);
        if (e == null) {
            return;
        }
        e.timers.remove((Object)Quickhack.CYBERPSYCHOSIS);
        e.psychosis = 1.0f;
        Netrunning.updateFlags((Entity)p, e);
    }

    public static boolean inaccurate(Entity e) {
        return (Netrunning.flags(e) & 0xA) != 0;
    }

    public static int maximum(Player p) {
        return NetrunnerImplants.maximum(p);
    }

    public static double available(Player p) {
        return Netrunning.state(p).available(Netrunning.maximum(p));
    }

    public static double recovery(Player p) {
        return NetrunnerImplants.recovery(p);
    }

    public static int uploadTicks(Player p, Quickhack h) {
        return Math.max(1, (int)Math.round((double)h.uploadTicks / NetrunnerImplants.uploadSpeed(p)));
    }

    public static void message(Player p, String key) {
        p.displayClientMessage((Component)Component.translatable((String)("message.chromeware.netrunning." + key)), true);
    }

    private static int discountPerStack(Quickhack h) {
        return switch (h) {
            case Quickhack.SYNAPSE_BURNOUT -> 2;
            case Quickhack.CYBERPSYCHOSIS -> 6;
            case Quickhack.SUICIDE -> 10;
            case Quickhack.DETONATE_GRENADE -> 15;
            default -> 0;
        };
    }

    private static int maxStacks(Quickhack h) {
        return h == Quickhack.SUICIDE ? 2 : (h == Quickhack.DETONATE_GRENADE ? 1 : 3);
    }

    public static int cost(Player p, Quickhack h) {
        Level level = p.level();
        if (!(level instanceof ServerLevel)) {
            return h.ram;
        }
        ServerLevel level2 = (ServerLevel)level;
        List list = (List)Netrunning.world((ServerLevel)level2).discounts.getOrDefault(p.getUUID(), new EnumMap(Quickhack.class)).get((Object)h);
        long now = level2.getGameTime();
        int stacks = list == null ? 0 : (int)list.stream().filter(until -> until > now).count();
        return Math.max(0, h.ram - stacks * Netrunning.discountPerStack(h));
    }

    private static void discount(Player p, Quickhack h, int ticks) {
        Level level = p.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        List list = Netrunning.world((ServerLevel)level2).discounts.computeIfAbsent(p.getUUID(), id -> new EnumMap(Quickhack.class)).computeIfAbsent(h, k -> new ArrayList());
        long now = level2.getGameTime();
        list.removeIf(until -> until <= now);
        if (list.size() >= Netrunning.maxStacks(h)) {
            list.removeFirst();
        }
        list.add(now + (long)ticks);
    }

    public static boolean installProgram(Player p, ItemStack stack) {
        Item item;
        if (!(Installing.canOperate(p) && Netrunning.installed(p) && (item = stack.getItem()) instanceof QuickhackItem)) {
            return false;
        }
        QuickhackItem item2 = (QuickhackItem)item;
        DeckState s = Netrunning.state(p);
        if (s.programs().contains(item2.hack.id)) {
            Netrunning.message(p, "duplicate");
            return false;
        }
        if (s.programs().size() >= 8) {
            Netrunning.message(p, "slots_full");
            return false;
        }
        ArrayList<String> programs = new ArrayList<String>(s.programs());
        programs.add(item2.hack.id);
        stack.shrink(1);
        Netrunning.state(p, s.programs(programs));
        Netrunning.message(p, "program_installed");
        return true;
    }

    public static boolean removeProgram(Player p, int index) {
        if (!Installing.canOperate(p) || !Netrunning.installed(p) || index < 0 || index >= Netrunning.state(p).programs().size()) {
            return false;
        }
        ArrayList<String> programs = new ArrayList<String>(Netrunning.state(p).programs());
        Quickhack hack = Quickhack.byId(programs.remove(index));
        ItemStack shard = CyberwareItems.QUICKHACKS.get((Object)hack).toStack();
        Netrunning.state(p, Netrunning.state(p).programs(programs));
        if (!p.getInventory().add(shard)) {
            p.drop(shard, false);
        }
        return true;
    }

    public static boolean targetAllowed(Player p, Entity target) {
        Player other;
        if (target == null || target == p || target.isRemoved() || !target.isAlive() || target.level() != p.level() || p.level().isClientSide() && veiled.test(target) || target instanceof Player && ((other = (Player)target).isSpectator() || !p.canHarmPlayer(other))) {
            return false;
        }
        Vec3 eye = p.getEyePosition();
        Vec3 point = target.getBoundingBox().getCenter();
        if (target.getBoundingBox().distanceToSqr(eye) > 1024.0) {
            return false;
        }
        BlockHitResult hit = p.level().clip(new ClipContext(eye, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)p));
        return hit.getType() == HitResult.Type.MISS || target.getBoundingBox().inflate(0.2).contains(hit.getLocation());
    }

    public static boolean applicable(Entity target, Quickhack hack) {
        if (hack.device()) {
            return false;
        }
        return switch (hack) {
            case Quickhack.SUICIDE, Quickhack.SYSTEM_COLLAPSE, Quickhack.BLACKWALL_GATEWAY -> {
                if (target instanceof LivingEntity && !Netrunning.boss(target)) {
                    yield true;
                }
                yield false;
            }
            case Quickhack.CONTAGION -> {
                if (!target.getType().is(MECHANICAL)) {
                    yield true;
                }
                yield false;
            }
            case Quickhack.DETONATE_GRENADE -> Detonation.applicable(target);
            default -> true;
        };
    }

    private static boolean boss(Entity e) {
        return e instanceof WitherBoss || e instanceof EnderDragon || e instanceof Warden || e instanceof ElderGuardian;
    }

    public static boolean spend(Player p, double cost) {
        if (!Double.isFinite(cost) || cost < 0.0 || !Netrunning.usable(p)) {
            return false;
        }
        DeckState s = Netrunning.state(p);
        double ram = Netrunning.available(p);
        double missing = Math.max(0.0, cost - ram);
        if (missing > 0.0 && (s.overclock() <= 0 || (double)p.getHealth() <= missing)) {
            Netrunning.message(p, "no_ram");
            return false;
        }
        if (missing > 0.0) {
            p.setHealth((float)((double)p.getHealth() - missing));
        }
        Netrunning.state(p, s.debt(s.debt() + Math.min(cost, ram)));
        NetrunnerImplants.thresholds(p);
        return true;
    }

    public static boolean cast(Player p, Entity target, Quickhack hack) {
        if (DisabledNotice.blocked(p)) {
            return false;
        }
        if (!(!p.level().isClientSide() && Installing.canOperate(p) && Netrunning.usable(p) && hack != null && Netrunning.state(p).programs().contains(hack.id) && Netrunning.targetAllowed(p, target))) {
            return false;
        }
        if (!Netrunning.applicable(target, hack) || hack == Quickhack.CYBERWARE_MALFUNCTION && Netrunning.stacks(target) >= 8) {
            Netrunning.message(p, "not_applicable");
            return false;
        }
        World w = Netrunning.world((ServerLevel)p.level());
        long count = w.uploads.stream().filter(u -> u.caster.equals(p.getUUID()) && u.target.equals(target.getUUID())).count();
        if (count >= 4L || Netrunning.queued(w, p) >= 32L) {
            Netrunning.message(p, "queue_full");
            return false;
        }
        if (!Netrunning.spend(p, Netrunning.cost(p, hack))) {
            return false;
        }
        w.uploads.add(new Upload(p, target, hack, Netrunning.uploadTicks(p, hack)));
        return true;
    }

    public static boolean castBlock(Player p, BlockPos pos, Quickhack hack) {
        if (DisabledNotice.blocked(p)) {
            return false;
        }
        if (!(!p.level().isClientSide() && Installing.canOperate(p) && Netrunning.usable(p) && hack != null && hack.blocks() && Netrunning.state(p).programs().contains(hack.id) && DeviceHacks.reachable(p, pos) && DeviceHacks.applicable(p.level(), pos, hack))) {
            return false;
        }
        World w = Netrunning.world((ServerLevel)p.level());
        long count = w.devices.stream().filter(u -> u.caster.equals(p.getUUID()) && u.pos.equals((Object)pos)).count();
        if (count >= 4L || Netrunning.queued(w, p) >= 32L) {
            Netrunning.message(p, "queue_full");
            return false;
        }
        if (!Netrunning.spend(p, Netrunning.cost(p, hack))) {
            return false;
        }
        w.devices.add(new DeviceUpload(p, pos, hack, Netrunning.uploadTicks(p, hack)));
        return true;
    }

    private static long queued(World w, Player p) {
        return w.uploads.stream().filter(u -> u.caster.equals(p.getUUID())).count() + w.devices.stream().filter(u -> u.caster.equals(p.getUUID())).count();
    }

    public static void overclock(Player p) {
        if (DisabledNotice.blocked(p)) {
            return;
        }
        if (!p.level().isClientSide() && Netrunning.needsOptics(p)) {
            Netrunning.message(p, "no_optics");
        }
        if (p.level().isClientSide() || !Netrunning.usable(p)) {
            return;
        }
        DeckState s = Netrunning.state(p);
        if (s.overclock() > 0) {
            Netrunning.state(p, new DeckState(s.debt(), s.programs(), 0, QuantumTuner.duration(p, ImplantSettings.cooldown("cyberdeck", 600)), s.camillo(), s.reallocator(), s.ice()));
            ImplantCooldowns.fullyUsed(p, ResourceLocation.parse((String)"chromeware:cyberdeck_overclock"));
            return;
        }
        if (s.cooldown() > 0) {
            CooldownNotice.show(p, CyberwareItems.CYBERDECK.toStack(), s.cooldown());
            return;
        }
        Netrunning.state(p, new DeckState(s.debt(), s.programs(), ImplantSettings.duration("cyberdeck", 280), 0, s.camillo(), s.reallocator(), s.ice()));
    }

    public static void disconnect(Player p) {
        if (p.level().isClientSide()) {
            return;
        }
        WORLDS.values().forEach(w -> {
            w.uploads.removeIf(u -> u.caster.equals(p.getUUID()));
            w.devices.removeIf(u -> u.caster.equals(p.getUUID()));
        });
        Netrunning.state(p, Netrunning.state(p).stopped());
    }

    public static List<Upload> uploads(Player p, Entity target) {
        Level level = p.level();
        if (!(level instanceof ServerLevel)) {
            return List.of();
        }
        ServerLevel level2 = (ServerLevel)level;
        return Netrunning.world((ServerLevel)level2).uploads.stream().filter(u -> u.caster.equals(p.getUUID()) && (target == null || u.target.equals(target.getUUID()))).toList();
    }

    public static List<DeviceUpload> deviceUploads(Player p, BlockPos pos) {
        Level level = p.level();
        if (!(level instanceof ServerLevel)) {
            return List.of();
        }
        ServerLevel level2 = (ServerLevel)level;
        return Netrunning.world((ServerLevel)level2).devices.stream().filter(u -> u.caster.equals(p.getUUID()) && (pos == null || u.pos.equals((Object)pos))).toList();
    }

    public static boolean has(Entity target, Quickhack hack) {
        Level level = target.level();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel level2 = (ServerLevel)level;
        Effects e = Netrunning.world((ServerLevel)level2).effects.get(target.getUUID());
        return e != null && e.timers.getOrDefault((Object)hack, 0) > 0;
    }

    public static Vec3 lure(Entity target) {
        Effects e = Netrunning.effects(target);
        return e != null && Netrunning.lured(e) ? e.lure : null;
    }

    public static int stacks(Entity target) {
        Level level = target.level();
        if (!(level instanceof ServerLevel)) {
            return 0;
        }
        ServerLevel level2 = (ServerLevel)level;
        Effects e = Netrunning.world((ServerLevel)level2).effects.get(target.getUUID());
        return e == null ? 0 : e.stacks;
    }

    public static void tickPlayer(Player p) {
        if (!p.level().isClientSide() && !Float.isFinite(p.getHealth())) {
            p.setHealth(0.0f);
            p.die(p.damageSources().genericKill());
            return;
        }
        DeckState s = Netrunning.state(p);
        Netrunning.state(p, s.tick(Netrunning.usable(p) ? Netrunning.recovery(p) / 20.0 : 0.0));
        if (s.overclock() == 1) {
            NetrunnerImplants.reduce(p, 0, ImplantSettings.cooldown("cyberdeck", 600) - QuantumTuner.duration(p, ImplantSettings.cooldown("cyberdeck", 600)));
            ImplantCooldowns.fullyUsed(p, ResourceLocation.parse((String)"chromeware:cyberdeck_overclock"));
        }
        if (!Netrunning.usable(p) && Netrunning.state(p).overclock() > 0) {
            Netrunning.state(p, Netrunning.state(p).stopped());
        }
        NetrunnerImplants.thresholds(p);
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (ServerPlayer p : level.players()) {
                Netrunning.tickPlayer((Player)p);
            }
            Netrunning.tickWorld(level);
        }
    }

    public static void tickWorld(ServerLevel level) {
        Entity entity;
        Entity target;
        World w = Netrunning.world(level);
        HashSet<CallSite> occupied = new HashSet<CallSite>();
        ArrayList<Object> finished = new ArrayList<Object>();
        Iterator<Upload> iterator = w.uploads.iterator();
        while (iterator.hasNext()) {
            Player player;
            Upload u = iterator.next();
            Entity entity2 = level.getEntity(u.caster);
            target = level.getEntity(u.target);
            if (!(entity2 instanceof Player) || !Netrunning.usable(player = (Player)entity2) || target == null || !target.isAlive() || target.isRemoved()) {
                iterator.remove();
                continue;
            }
            if (!occupied.add((CallSite)((Object)(String.valueOf(u.caster) + "/" + String.valueOf(u.target)))) || --u.remaining > 0) continue;
            finished.add(u);
            iterator.remove();
        }
        for (Upload upload : finished) {
            target = level.getEntity(upload.target);
            entity = level.getEntity(upload.caster);
            if (!(entity instanceof Player)) continue;
            Player player = (Player)entity;
            if (target == null) continue;
            Netrunning.apply(player, target, upload.hack);
            Humanity.quickhack(player, upload.hack.ram);
            if (upload.hack != Quickhack.BLACKWALL_GATEWAY) continue;
            Netrunning.spread(w, player, target, upload);
        }
        ArrayList<Object> finishedDevices = new ArrayList<Object>();
        Iterator<DeviceUpload> iterator2 = w.devices.iterator();
        while (iterator2.hasNext()) {
            Player player;
            DeviceUpload u = iterator2.next();
            entity = level.getEntity(u.caster);
            if (!(entity instanceof Player && Netrunning.usable(player = (Player)entity) && DeviceHacks.applicable((Level)level, u.pos, u.hack))) {
                iterator2.remove();
                continue;
            }
            if (!occupied.add((CallSite)((Object)(String.valueOf(u.caster) + "/" + String.valueOf(u.pos)))) || --u.remaining > 0) continue;
            finishedDevices.add(u);
            iterator2.remove();
        }
        for (DeviceUpload deviceUpload : finishedDevices) {
            Entity entity3 = level.getEntity(deviceUpload.caster);
            if (!(entity3 instanceof Player)) continue;
            Player p2 = (Player)entity3;
            DeviceHacks.apply(p2, deviceUpload.pos, deviceUpload.hack);
            Humanity.quickhack(p2, deviceUpload.hack.ram);
        }
        w.baited.values().removeIf(until -> until <= level.getGameTime());
        w.ticking = true;
        Iterator<Map.Entry<UUID, Effects>> effectsIterator = w.effects.entrySet().iterator();
        while (effectsIterator.hasNext()) {
            Player p3;
            Entity entity4;
            Player caster;
            Map.Entry<UUID, Effects> entry = effectsIterator.next();
            Entity target2 = level.getEntity(entry.getKey());
            Effects e = entry.getValue();
            if (target2 == null || target2.isRemoved() || !target2.isAlive()) {
                effectsIterator.remove();
                continue;
            }
            e.timers.replaceAll((h, ticks) -> ticks - 1);
            e.timers.values().removeIf(ticks -> ticks <= 0);
            if (!e.timers.containsKey((Object)Quickhack.CYBERWARE_MALFUNCTION) && e.stacks < 2) {
                e.stacks = 0;
            }
            if (!e.timers.containsKey((Object)Quickhack.CONTAGION)) {
                e.contagionScale = 1.0f;
            }
            if (e.disabled > 0) {
                --e.disabled;
            }
            Player player = caster = (entity4 = level.getEntity(e.caster)) instanceof Player ? (p3 = (Player)entity4) : null;
            if (e.timers.containsKey((Object)Quickhack.OVERHEAT) && !target2.isOnFire()) {
                e.timers.remove((Object)Quickhack.OVERHEAT);
            }
            Netrunning.updateFlags(target2, e);
            if (target2 instanceof Mob) {
                Mob mob = (Mob)target2;
                Netrunning.steer(level, mob, e, caster);
            }
            if (!(target2 instanceof LivingEntity) && Netrunning.lured(e)) {
                Vec3 direction = e.lure.subtract(target2.position());
                target2.setDeltaMovement(direction.lengthSqr() > 0.25 ? direction.normalize().scale(0.12) : Vec3.ZERO);
            }
            if (!(target2 instanceof LivingEntity) && e.timers.containsKey((Object)Quickhack.CYBERPSYCHOSIS)) {
                Vec3 motion = target2.getDeltaMovement();
                target2.setDeltaMovement(-motion.z, motion.y, motion.x);
            }
            HackFx.ongoing(target2, e.timers.keySet(), level.getGameTime());
            if (caster != null && level.getGameTime() % 20L == 0L) {
                if (e.disabled > 0) {
                    Netrunning.particles(target2, ParticleTypes.ELECTRIC_SPARK, 4);
                }
                if (e.stacks >= 3 && target2 instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity)target2;
                    Netrunning.trueDamage(caster, target2, living.getMaxHealth() * 0.02f);
                }
            }
            if (!e.timers.isEmpty() || e.disabled > 0 || e.stacks >= 2) continue;
            Netrunning.clearModifiers(target2);
            target2.setData(PlayerData.HACK_FLAGS, (Object)0);
            effectsIterator.remove();
        }
        w.ticking = false;
        List<Runnable> list = List.copyOf(w.later);
        w.later.clear();
        list.forEach(Runnable::run);
    }

    private static void later(ServerLevel level, Runnable work) {
        World w = Netrunning.world(level);
        if (w.ticking) {
            w.later.add(work);
        } else {
            work.run();
        }
    }

    private static void spread(World w, Player p, Entity from, Upload last) {
        if (last.chain >= 3) {
            return;
        }
        HashSet<UUID> hit = new HashSet<UUID>(last.chained);
        hit.add(from.getUUID());
        LivingEntity next = p.level().getEntitiesOfClass(LivingEntity.class, from.getBoundingBox().inflate(20.0), e -> e != p && e.isAlive() && !hit.contains(e.getUUID()) && !Netrunning.boss((Entity)e) && e.distanceToSqr(from) <= 400.0 && EnemyKills.eligible(p, e)).stream().min(Comparator.comparingDouble(arg_0 -> ((Entity)from).distanceToSqr(arg_0))).orElse(null);
        if (next == null || !Netrunning.spend(p, Quickhack.BLACKWALL_GATEWAY.ram + 2 * (last.chain + 1))) {
            return;
        }
        Upload u = new Upload(p, (Entity)next, Quickhack.BLACKWALL_GATEWAY, Math.max(1, last.total / 2));
        u.chain = last.chain + 1;
        u.chained.addAll(hit);
        w.uploads.add(u);
        HackFx.chain(from, (Entity)next, Quickhack.BLACKWALL_GATEWAY);
    }

    private static boolean lured(Effects e) {
        return e.timers.containsKey((Object)Quickhack.BAIT) || e.timers.containsKey((Object)Quickhack.REQUEST_BACKUP);
    }

    private static void steer(ServerLevel level, Mob mob, Effects e, Player caster) {
        int flags = Netrunning.flags((Entity)mob);
        if ((flags & 8) != 0 && !Netrunning.boss((Entity)mob) && mob.getTarget() != null) {
            mob.setTarget(null);
        }
        if ((flags & 1) != 0) {
            mob.getNavigation().stop();
        }
        if (e.timers.containsKey((Object)Quickhack.MEMORY_WIPE) && mob.getTarget() == caster) {
            mob.setTarget(null);
        }
        if (Netrunning.lured(e)) {
            Entity to;
            Entity entity;
            if (e.follow != null && (entity = level.getEntity(e.follow)) instanceof Entity && (to = entity).isAlive()) {
                e.lure = to.position();
            }
            mob.setTarget(null);
            if (mob.position().distanceToSqr(e.lure) < 4.0) {
                e.timers.remove((Object)Quickhack.BAIT);
                e.timers.remove((Object)Quickhack.REQUEST_BACKUP);
                e.follow = null;
                mob.getNavigation().stop();
            } else if (mob.getNavigation().isDone() || level.getGameTime() % 10L == 0L) {
                mob.getNavigation().moveTo(e.lure.x, e.lure.y, e.lure.z, 1.1);
            }
        }
        if (e.timers.containsKey((Object)Quickhack.CYBERPSYCHOSIS) && level.getGameTime() % 10L == 0L) {
            List<LivingEntity> victims = Netrunning.psychoVictims(level, mob, caster);
            if (victims.isEmpty()) {
                Netrunning.suicide(caster, (Entity)mob);
            } else {
                victims.stream().min(Comparator.comparingDouble(arg_0 -> ((Mob)mob).distanceToSqr(arg_0))).ifPresent(v -> Netrunning.forceTarget(mob, v));
            }
        }
    }

    private static List<LivingEntity> psychoVictims(ServerLevel level, Mob mob, Player caster) {
        return level.getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(16.0), v -> {
            Player player;
            return v != mob && v.isAlive() && v != caster && (!(v instanceof Player) || !(player = (Player)v).isCreative() && !player.isSpectator());
        });
    }

    private static void forceTarget(Mob mob, LivingEntity target) {
        FORCING.set(true);
        try {
            mob.setTarget(target);
        }
        finally {
            FORCING.remove();
        }
    }

    private static void updateFlags(Entity target, Effects e) {
        int flags = 0;
        if (e.timers.containsKey((Object)Quickhack.CRIPPLE_MOVEMENT)) {
            flags |= 1;
        }
        if (e.timers.containsKey((Object)Quickhack.WEAPON_GLITCH)) {
            flags |= 2;
        }
        if (e.timers.containsKey((Object)Quickhack.CYBERWARE_MALFUNCTION) || e.stacks >= 2 || e.disabled > 0) {
            flags |= 4;
        }
        if (e.timers.containsKey((Object)Quickhack.REBOOT_OPTICS)) {
            flags |= 8;
        }
        if (Netrunning.flags(target) != flags) {
            target.setData(PlayerData.HACK_FLAGS, (Object)flags);
        }
        if (!(target instanceof LivingEntity)) {
            return;
        }
        LivingEntity living = (LivingEntity)target;
        Netrunning.modifier(living, (Holder<Attribute>)Attributes.ARMOR, MELT, e.timers.containsKey((Object)Quickhack.OVERHEAT) ? -0.4 : 0.0);
        Netrunning.modifier(living, (Holder<Attribute>)Attributes.JUMP_STRENGTH, NO_JUMP, (flags & 1) != 0 ? -1.0 : 0.0);
    }

    private static void modifier(LivingEntity living, Holder<Attribute> attribute, ResourceLocation id, double amount) {
        AttributeInstance instance = living.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        AttributeModifier current = instance.getModifier(id);
        if (amount == 0.0) {
            if (current != null) {
                instance.removeModifier(id);
            }
            return;
        }
        if (current != null && current.amount() == amount) {
            return;
        }
        instance.removeModifier(id);
        instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    private static void clearModifiers(Entity target) {
        if (!(target instanceof LivingEntity)) {
            return;
        }
        LivingEntity living = (LivingEntity)target;
        Netrunning.modifier(living, (Holder<Attribute>)Attributes.ARMOR, MELT, 0.0);
        Netrunning.modifier(living, (Holder<Attribute>)Attributes.JUMP_STRENGTH, NO_JUMP, 0.0);
    }

    private static Effects effect(Player caster, Entity target, Quickhack h) {
        return Netrunning.effect(caster, target, h, h.duration);
    }

    private static Effects effect(Player caster, Entity target, Quickhack h, int duration) {
        Effects e = Netrunning.world((ServerLevel)((ServerLevel)target.level())).effects.computeIfAbsent(target.getUUID(), ignored -> new Effects());
        e.caster = caster.getUUID();
        e.timers.put(h, duration);
        Netrunning.updateFlags(target, e);
        return e;
    }

    private static boolean blocked(Player victim, Player attacker) {
        if (!NetrunnerImplants.block(victim)) {
            return false;
        }
        if (attacker != victim) {
            Netrunning.apply(victim, (Entity)attacker, Quickhack.SHORT_CIRCUIT);
        }
        return true;
    }

    private static int othersOn(Effects e, Quickhack self) {
        return (int)e.timers.keySet().stream().filter(h -> h != self).count();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Could not resolve type clashes
     * Unable to fully structure code
     */
    public static boolean apply(Player p, Entity target, Quickhack h) {
        block44: {
            block43: {
                var4_3 = p.level();
                if (!(var4_3 instanceof ServerLevel)) break block43;
                level = (ServerLevel)var4_3;
                if (target.level() == level && target.isAlive() && !h.device()) break block44;
            }
            return false;
        }
        if (target instanceof Player && Netrunning.blocked(victim = (Player)target, p)) {
            Netrunning.message(p, "blocked");
            return false;
        }
        mechanical = target.getType().is(Netrunning.MECHANICAL);
        HackFx.landed((Entity)p, target, h, mechanical);
        existing = Netrunning.world((ServerLevel)level).effects.get(target.getUUID());
        switch (1.$SwitchMap$com$chromeware$netrunning$Quickhack[h.ordinal()]) {
            case 8: {
                if (target instanceof LivingEntity) {
                    living = (LivingEntity)target;
                    v0 = living.getMaxHealth();
                } else {
                    v0 = 20.0f;
                }
                max = v0;
                chromed = target instanceof Player != false && CyberwareCapacity.used(victim = (Player)target) >= 50;
                Netrunning.trueDamage(p, target, max * 0.1f * (mechanical != false ? 1.5f : 1.0f) * (chromed != false ? 1.2f : 1.0f));
                e = Netrunning.effect(p, target, h, 1);
                e.timers.remove((Object)h);
                e.disabled = 60;
                if (target instanceof ServerPlayer) {
                    victim = (ServerPlayer)target;
                    Netrunning.interrupt(victim);
                }
                e.timers.replaceAll((BiFunction<Quickhack, Integer, Integer>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;, lambda$apply$25(com.chromeware.netrunning.Quickhack java.lang.Integer ), (Lcom/chromeware/netrunning/Quickhack;Ljava/lang/Integer;)Ljava/lang/Integer;)());
                Netrunning.updateFlags(target, e);
                Netrunning.discount(p, Quickhack.CYBERPSYCHOSIS, 200);
                break;
            }
            case 9: {
                Netrunning.effect(p, target, h);
                if (target.fireImmune()) break;
                target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), h.duration));
                break;
            }
            case 7: {
                if (mechanical) break;
                infected = new ArrayList<Object>();
                infected.add(target);
                for (Object other : Netrunning.nearby(p, target, 8)) {
                    if (infected.size() > 4) break;
                    if (!(other instanceof LivingEntity) || other.getType().is(Netrunning.MECHANICAL) || other instanceof Player && Netrunning.blocked(victim = (Player)other, p)) continue;
                    infected.add(other);
                }
                scale = 1.0f + 0.15f * (float)(infected.size() - 1);
                for (Entity each : infected) {
                    if (each != target) {
                        HackFx.chain(target, each, h);
                        HackFx.landed((Entity)p, each, h, false);
                    }
                    e = Netrunning.effect(p, each, h);
                    e.contagionScale = scale;
                    if (!(each instanceof LivingEntity)) continue;
                    living = (LivingEntity)each;
                    living.addEffect(new MobEffectInstance(MobEffects.POISON, h.duration, 1), (Entity)p);
                }
                break;
            }
            case 1: {
                bonus = Math.min(4.0, Netrunning.state(p).debt() * 0.1) * (double)(Netrunning.state(p).overclock() > 0 ? 2 : 1);
                Netrunning.damage(p, target, (float)(15.0 * (1.0 + bonus)));
                if (target.isAlive()) break;
                Netrunning.discount(p, Quickhack.SYNAPSE_BURNOUT, 300);
                s = Netrunning.state(p);
                if (s.overclock() <= 0) break;
                Netrunning.state(p, new DeckState(s.debt(), s.programs(), Math.min(ImplantSettings.duration("cyberdeck", 280), s.overclock() + ImplantSettings.ticks("cyberdeck", "overclock_extension_per_kill_seconds", 60)), s.cooldown(), s.camillo(), s.reallocator(), s.ice()));
                break;
            }
            case 10: {
                Netrunning.effect(p, target, h);
                if (target instanceof Mob && ((mob = (Mob)target).getTarget() == p || !Netrunning.boss((Entity)mob))) {
                    mob.setTarget(null);
                }
                if (!(target instanceof Player)) break;
                victim = (Player)target;
                victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, h.duration, 0, false, false));
                break;
            }
            case 11: {
                Netrunning.effect(p, target, h);
                if (target instanceof LivingEntity && WeaponGlitch.detonate(living = (LivingEntity)target)) {
                    level.explode(null, target.getX(), target.getY() + (double)target.getBbHeight() * 0.6, target.getZ(), 1.0f, true, Level.ExplosionInteraction.MOB);
                }
                Netrunning.particles(target, ParticleTypes.SMOKE, 10);
                break;
            }
            case 12: {
                Netrunning.effect(p, target, h);
                break;
            }
            case 13: {
                e = Netrunning.effect(p, target, h);
                e.stacks = Math.min(8, e.stacks + 1);
                Netrunning.updateFlags(target, e);
                Netrunning.discount(p, Quickhack.CYBERPSYCHOSIS, 200);
                if (target instanceof ServerPlayer) {
                    server = (ServerPlayer)target;
                    Netrunning.interrupt(server);
                }
                if (e.stacks != 8) break;
                level.explode(null, target.getX(), target.getY() + (double)target.getBbHeight() * 0.5, target.getZ(), 4.0f, Level.ExplosionInteraction.TNT);
                break;
            }
            case 14: {
                Netrunning.bait(level, p, target);
                break;
            }
            case 15: {
                baited = Netrunning.world((ServerLevel)level).baited.get(target.getUUID());
                calls = baited != null && baited > level.getGameTime() ? 5 : 1;
                helpers = level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(30.0), (Predicate<LivingEntity>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Z, lambda$apply$26(net.minecraft.world.entity.Entity net.minecraft.world.entity.player.Player net.minecraft.world.entity.LivingEntity ), (Lnet/minecraft/world/entity/LivingEntity;)Z)((Entity)target, (Player)p)).stream().sorted(Comparator.comparingDouble((ToDoubleFunction<LivingEntity>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)D, distanceToSqr(net.minecraft.world.entity.Entity ), (Lnet/minecraft/world/entity/LivingEntity;)D)((Entity)target))).limit(calls).toList();
                for (LivingEntity helper : helpers) {
                    if (helper instanceof ServerPlayer) {
                        player = (ServerPlayer)helper;
                        NetrunningPackets.marker(player, target.getUUID(), target.position(), target.getId(), h.duration);
                        continue;
                    }
                    e = Netrunning.effect(p, (Entity)helper, h);
                    e.follow = target.getUUID();
                    e.lure = target.position();
                    if (!(helper instanceof Mob)) continue;
                    mob = (Mob)helper;
                    mob.setTarget(null);
                }
                break;
            }
            case 16: {
                Netrunning.effect(p, target, h);
                if (!(target instanceof Mob)) ** GOTO lbl141
                mob = (Mob)target;
                if (mob.getTarget() == p) ** GOTO lbl140
                if (!(mob instanceof NeutralMob)) ** GOTO lbl141
                neutral = (NeutralMob)mob;
                if (!p.getUUID().equals(neutral.getPersistentAngerTarget())) ** GOTO lbl141
lbl140:
                // 2 sources

                Netrunning.forget((Entity)mob);
lbl141:
                // 4 sources

                if (!(target instanceof ServerPlayer)) break;
                victim = (ServerPlayer)target;
                NetrunningPackets.veil(victim, p.getId(), h.duration);
                break;
            }
            case 5: {
                if (!(target instanceof LivingEntity)) break;
                living = (LivingEntity)target;
                living.setLastHurtByPlayer(p);
                Netrunning.SILENT.set(true);
                try {
                    Netrunning.lethal(living, TrueDamage.source(TrueDamage.SYSTEM_COLLAPSE, (Entity)p));
                    break;
                }
                finally {
                    Netrunning.SILENT.remove();
                }
            }
            case 2: {
                others = existing == null ? 0 : Netrunning.othersOn(existing, h);
                ticks = Math.round((float)h.duration * (1.0f + 0.4f * (float)others));
                e = Netrunning.effect(p, target, h, ticks);
                e.psychosis = 1.0f + 0.4f * (float)others;
                if (target instanceof ServerPlayer) {
                    victim = (ServerPlayer)target;
                    Humanity.forcePsychosis(victim, ticks);
                    break;
                }
                if (!(target instanceof Mob)) break;
                mob = (Mob)target;
                victims = Netrunning.psychoVictims(level, mob, p);
                if (victims.isEmpty()) {
                    Netrunning.suicide(p, (Entity)mob);
                    break;
                }
                victims.stream().min(Comparator.comparingDouble((ToDoubleFunction<LivingEntity>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)D, distanceToSqr(net.minecraft.world.entity.Entity ), (Lnet/minecraft/world/entity/LivingEntity;)D)((Mob)mob))).ifPresent((Consumer<LivingEntity>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)V, lambda$apply$27(net.minecraft.world.entity.Mob net.minecraft.world.entity.LivingEntity ), (Lnet/minecraft/world/entity/LivingEntity;)V)((Mob)mob));
                break;
            }
            case 4: {
                if (!Detonation.entity(p, target)) break;
                Netrunning.detonated(p);
                break;
            }
            case 3: {
                if (!Netrunning.suicide(p, target)) break;
                p.heal(p.getMaxHealth() * 0.2f);
                break;
            }
            case 6: {
                if (target instanceof LivingEntity) {
                    living = (LivingEntity)target;
                    v1 = living.getMaxHealth() + living.getAbsorptionAmount();
                } else {
                    v1 = 20.0f;
                }
                health = v1;
                Netrunning.trueDamage(p, target, health * 2.0f);
                break;
            }
            default: {
                Netrunning.effect(p, target, h);
            }
        }
        return true;
    }

    private static void interrupt(ServerPlayer server) {
        if (Sandevistan.isActive((Entity)server)) {
            Sandevistan.interrupt(server);
        }
        ZetatechBerserk.end((Player)server, false);
        ArmImplants.retractAll((Player)server);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void trueDamage(Player p, Entity target, float amount) {
        int oldImmunity;
        LivingEntity living;
        float scale = NetrunnerImplants.critical(p) ? ImplantCriticals.multiplier(p, target, ImplantCriticals.Weapon.OTHER, 1.5f) : 1.0f;
        HACK_DAMAGE.set(true);
        if (target instanceof LivingEntity) {
            living = (LivingEntity)target;
            v0 = living.invulnerableTime;
        } else {
            v0 = oldImmunity = 0;
        }
        if (target instanceof LivingEntity) {
            living = (LivingEntity)target;
            living.invulnerableTime = 0;
        }
        try {
            target.hurt(TrueDamage.source((Entity)p), amount * scale);
        }
        catch (Throwable throwable) {
            if (target instanceof LivingEntity) {
                LivingEntity living2 = (LivingEntity)target;
                living2.invulnerableTime = oldImmunity;
            }
            HACK_DAMAGE.remove();
            throw throwable;
        }
        if (target instanceof LivingEntity) {
            living = (LivingEntity)target;
            living.invulnerableTime = oldImmunity;
        }
        HACK_DAMAGE.remove();
    }

    static void detonated(Player p) {
        Netrunning.discount(p, Quickhack.DETONATE_GRENADE, 60);
    }

    private static void bait(ServerLevel level, Player p, Entity target) {
        World w = Netrunning.world(level);
        String key = String.valueOf(p.getUUID()) + "/" + String.valueOf(target.getUUID());
        Vec3 origin = w.baitOrigins.remove(key);
        if (origin == null) {
            w.baitOrigins.put(key, target.position());
        }
        Vec3 to = origin != null ? origin : p.position();
        w.baited.put(target.getUUID(), level.getGameTime() + (long)Quickhack.BAIT.duration);
        if (target instanceof ServerPlayer) {
            ServerPlayer victim = (ServerPlayer)target;
            NetrunningPackets.marker(victim, p.getUUID(), to, -1, Quickhack.BAIT.duration);
        } else {
            Effects e = Netrunning.effect(p, target, Quickhack.BAIT);
            e.lure = to;
            e.follow = null;
            if (target instanceof Mob) {
                Mob mob = (Mob)target;
                mob.setTarget(null);
            }
        }
    }

    private static boolean backup(LivingEntity e) {
        if (e instanceof Player) {
            Player player = (Player)e;
            return !player.isCreative() && !player.isSpectator();
        }
        return e instanceof Mob && (e instanceof Enemy || e instanceof NeutralMob);
    }

    static boolean suicide(Player caster, Entity target) {
        if (!(target instanceof LivingEntity)) {
            return false;
        }
        LivingEntity living = (LivingEntity)target;
        if (caster != null) {
            living.setLastHurtByPlayer(caster);
        }
        Netrunning.lethal(living, TrueDamage.source(TrueDamage.SUICIDE, (Entity)living));
        return !living.isAlive();
    }

    private static void lethal(LivingEntity living, DamageSource source) {
        HACK_DAMAGE.set(true);
        int immunity = living.invulnerableTime;
        living.invulnerableTime = 0;
        try {
            living.hurt(source, (living.getMaxHealth() + living.getAbsorptionAmount()) * 10.0f);
        }
        finally {
            living.invulnerableTime = immunity;
            HACK_DAMAGE.remove();
        }
    }

    public static void forget(Entity target) {
        if (!(target instanceof Mob)) {
            return;
        }
        Mob mob = (Mob)target;
        mob.setTarget(null);
        mob.getNavigation().stop();
        if (mob instanceof NeutralMob) {
            NeutralMob neutral = (NeutralMob)mob;
            neutral.stopBeingAngry();
        }
        Brain brain = mob.getBrain();
        brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
        brain.eraseMemory(MemoryModuleType.ANGRY_AT);
    }

    private static List<Entity> nearby(Player p, Entity origin, int radius) {
        List result = p.level().getEntities(origin, origin.getBoundingBox().inflate((double)radius), e -> e != p && Netrunning.targetAllowed(p, e));
        result.sort(Comparator.comparing(e -> !(e instanceof LivingEntity)).thenComparingDouble(arg_0 -> ((Entity)origin).distanceToSqr(arg_0)));
        return result;
    }

    static void particles(Entity e, SimpleParticleType type, int count) {
        Level level = e.level();
        if (level instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            level2.sendParticles((ParticleOptions)type, e.getX(), e.getY() + (double)e.getBbHeight() * 0.5, e.getZ(), count, 0.3, 0.4, 0.3, 0.02);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void damage(Player p, Entity target, float amount) {
        int oldImmunity;
        LivingEntity living;
        float scale = 1.0f;
        if (NetrunnerImplants.critical(p)) {
            scale *= ImplantCriticals.multiplier(p, target, ImplantCriticals.Weapon.OTHER, 1.5f);
        }
        DamageSource source = p.damageSources().indirectMagic((Entity)p, (Entity)p);
        HACK_DAMAGE.set(true);
        if (target instanceof LivingEntity) {
            living = (LivingEntity)target;
            v0 = living.invulnerableTime;
        } else {
            v0 = oldImmunity = 0;
        }
        if (target instanceof LivingEntity) {
            living = (LivingEntity)target;
            living.invulnerableTime = 0;
        }
        try {
            target.hurt(source, amount * scale);
        }
        catch (Throwable throwable) {
            if (target instanceof LivingEntity) {
                LivingEntity living2 = (LivingEntity)target;
                living2.invulnerableTime = oldImmunity;
            }
            HACK_DAMAGE.remove();
            throw throwable;
        }
        if (target instanceof LivingEntity) {
            living = (LivingEntity)target;
            living.invulnerableTime = oldImmunity;
        }
        HACK_DAMAGE.remove();
    }

    static Effects effects(Entity e) {
        Effects effects;
        Level level = e.level();
        if (level instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            effects = Netrunning.world((ServerLevel)level2).effects.get(e.getUUID());
        } else {
            effects = null;
        }
        return effects;
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void incoming(LivingIncomingDamageEvent event) {
        Player target;
        LivingEntity livingEntity;
        Player p;
        DamageSource source = event.getSource();
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        Entity attacker = source.getEntity();
        boolean hack = HACK_DAMAGE.get();
        double amount = event.getAmount();
        if (attacker != null && Netrunning.inaccurate(attacker) && GunCyberware.firearmDamage(source) && !(source.getDirectEntity() instanceof Projectile) && attacker.level().random.nextFloat() < 0.85f) {
            event.setCanceled(true);
            return;
        }
        if (attacker instanceof Player && Implant.MECHATRONIC_CORE.active(p = (Player)attacker) && (livingEntity = event.getEntity()) instanceof Player && Netrunning.cyberLimbs(target = (Player)livingEntity)) {
            amount *= (double)(1.0f + ImplantSettings.fstat("mechatronic_core", "cyberlimb_target_damage_bonus", 0.15));
        }
        LivingEntity target2 = event.getEntity();
        Effects e = Netrunning.effects((Entity)target2);
        if (!hack && e != null) {
            boolean melee;
            boolean bl = melee = ImplantEffects.melee(source) || source.is(DamageTypes.MOB_ATTACK) && source.getDirectEntity() == attacker;
            if (e.timers.containsKey((Object)Quickhack.CRIPPLE_MOVEMENT) && melee) {
                amount *= (double)1.15f;
            }
        }
        if (e != null && e.stacks > 0) {
            amount *= (double)(1.0f + 0.15f * (float)e.stacks);
        }
        if (e != null && e.timers.containsKey((Object)Quickhack.CONTAGION) && source.is(NeoForgeMod.POISON_DAMAGE)) {
            amount *= (double)e.contagionScale;
        }
        if (e != null && e.timers.containsKey((Object)Quickhack.CYBERPSYCHOSIS)) {
            amount *= (double)e.psychosis;
        }
        event.setAmount((float)Math.min(3.4028234663852886E38, amount));
    }

    @SubscribeEvent
    public static void shot(EntityJoinLevelEvent event) {
        Projectile projectile;
        Entity entity;
        if (event.getLevel().isClientSide() || !((entity = event.getEntity()) instanceof Projectile) || (projectile = (Projectile)entity).getOwner() == null || projectile instanceof FishingHook) {
            return;
        }
        if (Netrunning.inaccurate(projectile.getOwner())) {
            Netrunning.deflect(projectile);
        }
    }

    static void deflect(Projectile projectile) {
        Vec3 motion = projectile.getDeltaMovement();
        double speed = motion.length();
        if (speed < 1.0E-4) {
            return;
        }
        RandomSource random = projectile.level().random;
        Vec3 forward = motion.normalize();
        Vec3 side = forward.cross(new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian())).normalize();
        double angle = Math.toRadians(18.0 + random.nextDouble() * 22.0);
        Vec3 direction = forward.scale(Math.cos(angle)).add(side.scale(Math.sin(angle)));
        projectile.shoot(direction.x, direction.y, direction.z, (float)speed, 0.0f);
    }

    @SubscribeEvent
    public static void retarget(LivingChangeTargetEvent event) {
        Mob mob;
        block8: {
            block7: {
                LivingEntity livingEntity;
                if (FORCING.get().booleanValue() || !((livingEntity = event.getEntity()) instanceof Mob)) break block7;
                mob = (Mob)livingEntity;
                if (event.getNewAboutToBeSetTarget() != null) break block8;
            }
            return;
        }
        LivingEntity next = event.getNewAboutToBeSetTarget();
        Effects e = Netrunning.effects((Entity)mob);
        if (e != null) {
            if (Netrunning.lured(e)) {
                event.setCanceled(true);
                return;
            }
            if ((Netrunning.flags((Entity)mob) & 8) != 0 && !Netrunning.boss((Entity)mob)) {
                event.setCanceled(true);
                return;
            }
            if (e.timers.containsKey((Object)Quickhack.MEMORY_WIPE) && next.getUUID().equals(e.caster)) {
                event.setCanceled(true);
                return;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void damaged(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();
        Level level = target.level();
        if (level instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            if (event.getNewDamage() > 0.0f) {
                Effects e = Netrunning.world((ServerLevel)level2).effects.get(target.getUUID());
                if (target instanceof Player) {
                    Player p = (Player)target;
                    NetrunnerImplants.recoup(p, event.getNewDamage());
                }
                if (e != null && target instanceof LivingEntity) {
                    LivingEntity living = target;
                    if (target.isAlive()) {
                        DamageSource source = event.getSource();
                        if (e.timers.containsKey((Object)Quickhack.CONTAGION) && source.is(DamageTypeTags.IS_FIRE)) {
                            e.timers.remove((Object)Quickhack.CONTAGION);
                            living.removeEffect(MobEffects.POISON);
                            level2.explode(null, target.getX(), target.getY() + (double)target.getBbHeight() * 0.5, target.getZ(), 1.0f, true, Level.ExplosionInteraction.MOB);
                        }
                        if (target.isAlive() && e.timers.containsKey((Object)Quickhack.OVERHEAT) && !OVERHEAT_FIRE.get().booleanValue()) {
                            OVERHEAT_FIRE.set(true);
                            int immunity = living.invulnerableTime;
                            living.invulnerableTime = 0;
                            try {
                                living.hurt(level2.damageSources().onFire(), event.getNewDamage() * 0.2f);
                            }
                            finally {
                                living.invulnerableTime = immunity;
                                OVERHEAT_FIRE.remove();
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void killed(LivingDeathEvent event) {
        Level level;
        Effects e;
        ServerLevel level2;
        Level level3;
        LivingEntity victim;
        Level level4;
        Player killer = EnemyKills.credit(event);
        if (killer != null) {
            NetrunnerImplants.kill(killer);
        }
        if (killer != null && ImplantEffects.melee(event.getSource())) {
            Netrunning.discount(killer, Quickhack.SUICIDE, 600);
        }
        if ((level4 = (victim = event.getEntity()).level()) instanceof ServerLevel) {
            ServerLevel level5 = (ServerLevel)level4;
            World w = Netrunning.world(level5);
            w.baited.remove(victim.getUUID());
            w.baitOrigins.keySet().removeIf(k -> k.endsWith("/" + String.valueOf(victim.getUUID())));
        }
        if ((level3 = victim.level()) instanceof ServerLevel) {
            level2 = (ServerLevel)level3;
            v0 = Netrunning.world((ServerLevel)level2).effects.get(victim.getUUID());
        } else {
            v0 = e = null;
        }
        if (e != null && (level = victim.level()) instanceof ServerLevel && (level = (level2 = (ServerLevel)level).getEntity(e.caster)) instanceof Player) {
            Player caster = (Player)level;
            if (e.timers.containsKey((Object)Quickhack.REBOOT_OPTICS)) {
                NetrunnerImplants.recover(caster, 3.0);
                LivingEntity next = level2.getEntitiesOfClass(LivingEntity.class, victim.getBoundingBox().inflate(8.0), v -> {
                    Player player;
                    return v != victim && v.isAlive() && v != caster && (!(v instanceof Player) || !(player = (Player)v).isCreative() && !player.isSpectator());
                }).stream().min(Comparator.comparingDouble(arg_0 -> ((LivingEntity)victim).distanceToSqr(arg_0))).orElse(null);
                if (next != null) {
                    Netrunning.later(level2, () -> Netrunning.apply(caster, (Entity)next, Quickhack.REBOOT_OPTICS));
                }
            }
            if (e.timers.containsKey((Object)Quickhack.CRIPPLE_MOVEMENT)) {
                caster.heal(caster.getMaxHealth() * 0.05f);
                NetrunnerImplants.recover(caster, 2.0);
            }
        }
        if (victim instanceof Player) {
            Player p = (Player)victim;
            Netrunning.disconnect(p);
        }
    }

    public static boolean cyberLimbs(Player p) {
        return CyberLimbs.PARTS.stream().filter(part -> part != BodyPart.BODY).anyMatch(part -> CyberLimbs.has(p, part)) || Arrays.stream(HumanoidArm.values()).anyMatch(side -> ArmImplants.get(p, side).replacesLimb());
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        Netrunning.disconnect(event.getEntity());
    }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Netrunning.disconnect(event.getEntity());
    }

    private static /* synthetic */ void lambda$apply$27(Mob mob, LivingEntity v) {
        Netrunning.forceTarget(mob, v);
    }

    private static /* synthetic */ boolean lambda$apply$26(Entity target, Player p, LivingEntity v) {
        return v != target && v != p && v.isAlive() && v.distanceToSqr(target) <= 900.0 && Netrunning.backup(v);
    }

    private static /* synthetic */ Integer lambda$apply$25(Quickhack hack, Integer ticks) {
        return CONTROL.contains((Object)hack) ? ticks + 60 : ticks;
    }

    private static final class World {
        final Map<UUID, Effects> effects = new HashMap<UUID, Effects>();
        final List<Upload> uploads = new ArrayList<Upload>();
        final List<DeviceUpload> devices = new ArrayList<DeviceUpload>();
        final Map<UUID, EnumMap<Quickhack, List<Long>>> discounts = new HashMap<UUID, EnumMap<Quickhack, List<Long>>>();
        final Map<String, Vec3> baitOrigins = new HashMap<String, Vec3>();
        final Map<UUID, Long> baited = new HashMap<UUID, Long>();
        final List<Runnable> later = new ArrayList<Runnable>();
        boolean ticking;

        private World() {
        }
    }

    private static final class Effects {
        final EnumMap<Quickhack, Integer> timers = new EnumMap(Quickhack.class);
        UUID caster;
        Vec3 lure = Vec3.ZERO;
        UUID follow;
        int stacks;
        int disabled;
        float contagionScale = 1.0f;
        float psychosis = 1.0f;

        private Effects() {
        }
    }

    public static final class Upload {
        public final UUID caster;
        public final UUID target;
        public final Quickhack hack;
        public final int total;
        public int remaining;
        int chain;
        final Set<UUID> chained = new HashSet<UUID>();

        Upload(Player caster, Entity target, Quickhack hack, int ticks) {
            this.caster = caster.getUUID();
            this.target = target.getUUID();
            this.hack = hack;
            this.total = this.remaining = ticks;
        }
    }

    public static final class DeviceUpload {
        public final UUID caster;
        public final BlockPos pos;
        public final Quickhack hack;
        public final int total;
        public int remaining;

        DeviceUpload(Player caster, BlockPos pos, Quickhack hack, int ticks) {
            this.caster = caster.getUUID();
            this.pos = pos.immutable();
            this.hack = hack;
            this.total = this.remaining = ticks;
        }
    }

    public static final class WeaponGlitch {
        private static final Set<String> GUN_MODS = Set.of("pointblank", "tacz", "superbwarfare", "scguns");

        public static boolean detonate(LivingEntity target) {
            ItemStack gun = WeaponGlitch.find(target, WeaponGlitch::gun);
            if (gun != null) {
                WeaponGlitch.breakItem(target, gun);
                return true;
            }
            if (target instanceof ServerPlayer) {
                ServerPlayer player = (ServerPlayer)target;
                for (HumanoidArm side : HumanoidArm.values()) {
                    if (!ArmImplants.has((Player)player, side, s -> s.is(CyberwareItems.PROJECTILE_LAUNCH_SYSTEM)) || LimbAttachments.get((Player)player).isMissing(ArmImplants.part(side))) continue;
                    ProjectileLauncher.retract((Player)player, side);
                    LimbAttachments.set((Player)player, LimbAttachments.get((Player)player).remove(ArmImplants.part(side)));
                    target.level().playSound(null, (Entity)target, SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0f, 0.7f);
                    return true;
                }
            }
            for (Predicate<ItemStack> kind : List.of(s -> s.getItem() instanceof CrossbowItem, s -> s.getItem() instanceof BowItem)) {
                ItemStack weapon = WeaponGlitch.find(target, kind);
                if (weapon == null) continue;
                WeaponGlitch.breakItem(target, weapon);
                return true;
            }
            return false;
        }

        static boolean gun(ItemStack stack) {
            if (stack.isEmpty() || !GUN_MODS.contains(BuiltInRegistries.ITEM.getKey((Object)stack.getItem()).getNamespace())) {
                return false;
            }
            for (Class<?> type = stack.getItem().getClass(); type != null; type = type.getSuperclass()) {
                if (!type.getSimpleName().contains("Gun")) continue;
                return true;
            }
            return false;
        }

        private static ItemStack find(LivingEntity target, Predicate<ItemStack> test) {
            for (InteractionHand hand : InteractionHand.values()) {
                if (!test.test(target.getItemInHand(hand))) continue;
                return target.getItemInHand(hand);
            }
            if (target instanceof Player) {
                Player player = (Player)target;
                for (ItemStack stack : player.getInventory().items) {
                    if (!test.test(stack)) continue;
                    return stack;
                }
            }
            return null;
        }

        private static void breakItem(LivingEntity target, ItemStack stack) {
            target.level().playSound(null, (Entity)target, SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0f, 0.7f);
            stack.shrink(stack.getCount());
        }
    }
}

