/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.tags.ItemTags
 *  net.minecraft.world.damagesource.CombatRules
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.Brain
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.memory.MemoryModuleType
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.food.FoodData
 *  net.minecraft.world.item.DiggerItem
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.common.NeoForgeMod
 *  net.neoforged.neoforge.common.damagesource.DamageContainer$Reduction
 *  net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent
 *  net.neoforged.neoforge.event.entity.living.LivingDamageEvent$Post
 *  net.neoforged.neoforge.event.entity.living.LivingDamageEvent$Pre
 *  net.neoforged.neoforge.event.entity.living.LivingDeathEvent
 *  net.neoforged.neoforge.event.entity.living.LivingFallEvent
 *  net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent
 *  net.neoforged.neoforge.event.entity.living.MobEffectEvent$Added
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$Clone
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.chromeware.cyberware;

import com.chromeware.body.BodyFunctions;
import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbState;
import com.chromeware.compat.guns.GunCyberware;
import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.Axolotl;
import com.chromeware.cyberware.BloodPump;
import com.chromeware.cyberware.CooldownNotice;
import com.chromeware.cyberware.CyberwareArmor;
import com.chromeware.cyberware.CyberwareStats;
import com.chromeware.cyberware.DenseMarrow;
import com.chromeware.cyberware.EnemyKills;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantCategory;
import com.chromeware.cyberware.ImplantCooldowns;
import com.chromeware.cyberware.ImplantCriticals;
import com.chromeware.cyberware.ImplantData;
import com.chromeware.cyberware.InCombat;
import com.chromeware.cyberware.Installing;
import com.chromeware.cyberware.KiroshiOptics;
import com.chromeware.cyberware.MovementImplants;
import com.chromeware.cyberware.PainducerPackets;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.cyberware.QuantumTuner;
import com.chromeware.cyberware.RangedHits;
import com.chromeware.cyberware.TitaniumBones;
import com.chromeware.cyberware.TrueDamage;
import com.chromeware.cyberware.ZetatechBerserk;
import com.chromeware.monowire.MonowireCombat;
import com.chromeware.netrunning.Netrunning;
import com.chromeware.sandevistan.Sandevistan;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="chromeware")
public final class ImplantEffects {
    private static final String CAMO_SINCE = "chromeware_camo_since";
    private static final String SPOTTED_BY = "chromeware_camo_spotted_by";
    private static final String SPOTTED_AT = "chromeware_camo_spotted_at";
    public static final int CAMO_FLAG = 1;
    public static final int SCAN_FLAG = 2;
    private static final Set<Player> DISCHARGING = Collections.newSetFromMap(new WeakHashMap());

    public static ImplantData state(Player p) {
        return (ImplantData)p.getData(PlayerData.CATALOGUE_STATE);
    }

    public static void state(Player p, ImplantData s) {
        if (!s.equals(ImplantEffects.state(p))) {
            p.setData(PlayerData.CATALOGUE_STATE, (Object)s);
        }
    }

    public static void clock(Player p, String key, int ticks) {
        ImplantEffects.state(p, ImplantEffects.state(p).clock(key, ticks));
    }

    public static boolean functional(Player p, ImplantCategory category) {
        if (!p.isAlive() || p.isSpectator()) {
            return false;
        }
        LimbState body = LimbAttachments.get(p);
        if (body.isMissing(BodyPart.BODY)) {
            return false;
        }
        if (category == ImplantCategory.FACE || category == ImplantCategory.FRONTAL_CORTEX || category == ImplantCategory.NERVOUS_SYSTEM) {
            return !body.isMissing(BodyPart.HEAD);
        }
        if (category == ImplantCategory.LEGS) {
            return !body.isMissing(BodyPart.LEFT_LEG) && !body.isMissing(BodyPart.RIGHT_LEG);
        }
        if (category == ImplantCategory.HANDS) {
            return BodyFunctions.usableHands(LimbAttachments.functional(p)) > 0;
        }
        return true;
    }

    public static int boosters(Player p) {
        return Implant.UNIVERSAL_BOOSTER.active(p) ? ImplantEffects.state(p).boosters().size() : 0;
    }

    public static int hungerBoosters(Player p) {
        return Implant.UNIVERSAL_BOOSTER.active(p) ? (int)ImplantEffects.state(p).boosters().stream().filter(b -> b.hungerTicks() > 0).count() : 0;
    }

    public static float allExhaustion(Player p, float amount) {
        if (ZetatechBerserk.active(p)) {
            return 0.0f;
        }
        return amount * (DenseMarrow.active(p) ? ImplantSettings.fstat("dense_marrow", "hunger_multiplier", 0.8) : 1.0f) * (Implant.CLUTCH_PADDING.active(p) ? ImplantSettings.fstat("clutch_padding", "hunger_multiplier", 0.5) : 1.0f) * (Implant.ISOMETRIC_STABILIZER.active(p) ? ImplantSettings.fstat("isometric_stabilizer", "hunger_multiplier", 0.0) : 1.0f) * (float)Math.pow(ImplantSettings.stat("universal_booster", "hunger_multiplier", 0.7), ImplantEffects.hungerBoosters(p));
    }

    public static float attackExhaustion(Player p, float amount) {
        return ZetatechBerserk.active(p) ? 0.0f : amount;
    }

    public static int meleeInterval(Player p, int ticks) {
        return Math.max(1, (int)Math.round((double)ticks / ((Implant.MICROROTORS.active(p) ? 1.0 + ImplantSettings.stat("microrotors", "attack_speed_bonus", 0.25) : 1.0) * (ZetatechBerserk.active(p) ? 1.0 + ImplantSettings.stat("zetatech_berserk", "attack_speed_bonus", 0.3) : 1.0))));
    }

    public static void healthItem(Player p) {
        if (Implant.UNIVERSAL_BOOSTER.active(p)) {
            ImplantEffects.state(p, ImplantEffects.state(p).boost());
        }
        CyberwareArmor.update(p);
    }

    public static double armorMultiplier(Player p) {
        return (Implant.PARA_BELLUM.active(p) ? ImplantSettings.stat("para_bellum", "armor_bonus", 0.1) : 0.0) + (Implant.SCAR_COALESCER.active(p) && (double)p.getHealth() < (double)p.getMaxHealth() * ImplantSettings.stat("scar_coalescer", "health_threshold_fraction", 0.5) ? ImplantSettings.stat("scar_coalescer", "armor_bonus", 0.2) : 0.0) + (double)ImplantEffects.boosters(p) * ImplantSettings.stat("universal_booster", "armor_bonus_per_stack", 0.1);
    }

    public static double conditionalArmor(Player p) {
        double lattice = Implant.COGITO_LATTICE.active(p) && Netrunning.usable(p) && Netrunning.available(p) < ImplantSettings.stat("cogito_lattice", "ram_threshold", 10.0) ? CyberwareStats.get(Implant.COGITO_LATTICE.stack()).armor() * ImplantSettings.stat("cogito_lattice", "conditional_armor_multiplier", 2.4) : 0.0;
        return lattice + (Implant.RANGEGUARD.active(p) && p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(ImplantSettings.stat("rangeguard", "enemy_radius", 6.0)), e -> e != p && e.isAlive() && !e.isSpectator() && e.distanceToSqr((Entity)p) <= Math.pow(ImplantSettings.stat("rangeguard", "enemy_radius", 6.0), 2.0) && (e instanceof Player || Axolotl.enemy(p, e))).isEmpty() ? CyberwareStats.get(Implant.RANGEGUARD.stack()).armor() * ImplantSettings.stat("rangeguard", "conditional_armor_multiplier", 3.0) : 0.0);
    }

    public static void updateAttributes(Player p) {
        if (p.level().isClientSide()) {
            return;
        }
        ImplantEffects.modifier(p, (Holder<Attribute>)Attributes.MAX_HEALTH, "epimorphic_health", Implant.EPIMORPHIC_SKELETON.active(p) ? ImplantSettings.stat("epimorphic_skeleton", "health_bonus", 0.5) : 0.0);
        if (p.getHealth() > p.getMaxHealth()) {
            p.setHealth(p.getMaxHealth());
        }
        ImplantEffects.modifier(p, (Holder<Attribute>)Attributes.ATTACK_SPEED, "microrotors_speed", Implant.MICROROTORS.active(p) ? ImplantSettings.stat("microrotors", "attack_speed_bonus", 0.25) : 0.0);
        ImplantEffects.modifier(p, (Holder<Attribute>)Attributes.SNEAKING_SPEED, "lynx_crouch", Implant.LYNX_PAWS.active(p) ? ImplantSettings.stat("lynx_paws", "crouch_speed_bonus", 0.5) : 0.0);
        double speed = (Implant.LEEROY_LIGAMENT_SYSTEM.active(p) ? ImplantSettings.stat("leeroy_ligament_system", "movement_speed_bonus", 0.4) : 0.0) + (Implant.ADRENALINE_CONVERTER.active(p) && ImplantEffects.state(p).clock("converter") > 0 ? ImplantSettings.stat("adrenaline_converter", "movement_speed_bonus", 0.6) : 0.0) + (Implant.ADRENO_TRIGGER.active(p) && ImplantEffects.state(p).clock("adreno") > 0 ? ImplantSettings.stat("adreno_trigger", "movement_speed_bonus", 0.4) : 0.0) + (TyrosineInjector.boosted(p) ? ImplantSettings.stat("tyrosine_injector", "movement_speed_bonus", 0.1) * (double)((Integer)p.getData(PlayerData.TYROSINE_STACKS)).intValue() : 0.0) + ImplantEffects.atomicSpeed(p);
        if (Implant.JENKINS_TENDONS.active(p) && p.isSprinting()) {
            speed += ImplantSettings.stat("jenkins_tendons", "initial_sprint_bonus", 1.0) - ImplantSettings.stat("jenkins_tendons", "sprint_bonus_decay", 0.8) * (double)Math.min(ImplantSettings.ticks("jenkins_tendons", "fatigue_seconds", 100), ImplantEffects.state(p).clock("sprint_fatigue")) / (double)ImplantSettings.ticks("jenkins_tendons", "fatigue_seconds", 100);
        }
        double fraction = p.getHealth() / p.getMaxHealth();
        double threshold = ImplantSettings.stat("threat_evac", "health_threshold_fraction", 0.25);
        if (Implant.THREAT_EVAC.active(p) && fraction <= threshold && threshold > 0.0) {
            speed += ImplantSettings.stat("threat_evac", "movement_speed_bonus", 0.5) + ImplantSettings.stat("threat_evac", "extra_low_health_bonus", 0.5) * (1.0 - Math.max(0.0, fraction) / threshold);
        }
        ImplantEffects.modifier(p, (Holder<Attribute>)Attributes.MOVEMENT_SPEED, "catalogue_speed", speed);
    }

    public static double atomicSpeed(Player p) {
        if (!Implant.ATOMIC_SENSORS.active(p) || InCombat.active(p)) {
            return 0.0;
        }
        double radius = ImplantSettings.stat("atomic_sensors", "enemy_radius", 24.0);
        boolean player = p.level().players().stream().anyMatch(other -> other != p && other.isAlive() && !other.isSpectator() && p.distanceToSqr((Entity)other) <= radius * radius);
        return player || !p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(radius), m -> m.isAlive() && m.getTarget() == p && Axolotl.enemy(p, (LivingEntity)m) && p.distanceToSqr((Entity)m) <= radius * radius).isEmpty() ? ImplantSettings.stat("atomic_sensors", "movement_speed_bonus", 0.6) : 0.0;
    }

    private static void modifier(Player p, Holder<Attribute> attribute, String key, double amount) {
        AttributeInstance instance = p.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)key);
        AttributeModifier old = instance.getModifier(id);
        if (old != null && old.amount() == amount) {
            return;
        }
        instance.removeModifier(id);
        if (amount != 0.0) {
            instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    public static boolean melee(DamageSource source) {
        return (source.is(DamageTypes.PLAYER_ATTACK) || source.is(MonowireCombat.DAMAGE_TYPE)) && source.getDirectEntity() instanceof Player && !GunCyberware.firearmDamage(source);
    }

    public static double mitigationChance(Player p) {
        return Math.min(1.0, (Implant.NEOFIBER.active(p) ? ImplantSettings.stat("neofiber", "mitigation_chance", 0.1) : 0.0) + (Implant.KINETIC_FRAME.active(p) && p.getFoodData().getFoodLevel() > ImplantSettings.istat("kinetic_frame", "hunger_threshold", 16) ? ImplantSettings.stat("kinetic_frame", "mitigation_chance", 0.2) : 0.0) + (Implant.COUNTERSHELL.active(p) && ImplantEffects.state(p).clock("countershell") > 0 ? ImplantSettings.stat("countershell", "mitigation_chance", 0.5) : 0.0));
    }

    public static double mitigationStrength(Player p) {
        return Math.min(0.9, 0.5 + (Implant.NEOFIBER.active(p) ? ImplantSettings.stat("neofiber", "mitigation_strength_bonus", 0.1) : 0.0) + (Implant.SPRING_JOINTS.active(p) ? ImplantSettings.stat("spring_joints", "mitigation_strength_bonus", 0.2) : 0.0));
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public static void incoming(LivingIncomingDamageEvent event) {
        Entity entity;
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        DamageSource source = event.getSource();
        float amount = event.getAmount();
        if (source.is(NeoForgeMod.POISON_DAMAGE) && ((Boolean)event.getEntity().getData(PlayerData.MAMBA_POISON)).booleanValue()) {
            amount *= ImplantSettings.fstat("black_mamba", "poison_damage_multiplier", 2.0);
        }
        if ((entity = source.getEntity()) instanceof Player) {
            Player attacker = (Player)entity;
            if (Implant.BLACK_MAMBA.active(attacker) && event.getEntity().hasEffect(MobEffects.POISON)) {
                amount *= ImplantSettings.fstat("black_mamba", "poisoned_target_damage_multiplier", 1.2f);
            }
            if (Implant.HANDLE_WRAP.active(attacker) && RangedHits.ranged(source)) {
                amount *= ImplantSettings.fstat("handle_wrap", "ranged_damage_multiplier", 1.3f);
            }
        }
        if ((entity = event.getEntity()) instanceof Player) {
            Player p = (Player)entity;
            if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !TrueDamage.is(source)) {
                double chance;
                Entity enemy;
                if (Implant.PAIN_EDITOR.active(p)) {
                    amount *= ImplantSettings.fstat("pain_editor", "damage_multiplier", 0.9f);
                }
                Entity entity2 = enemy = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
                if (enemy != null && enemy != p) {
                    double near = Math.clamp((double)((ImplantSettings.stat("peripheral_inverse", "falloff_end_range", 6.0) - (double)p.distanceTo(enemy)) / Math.max(0.01, ImplantSettings.stat("peripheral_inverse", "falloff_end_range", 6.0) - ImplantSettings.stat("peripheral_inverse", "full_strength_range", 3.0))), (double)0.0, (double)1.0);
                    if (Implant.PERIPHERAL_INVERSE.active(p)) {
                        amount *= (float)(1.0 - ImplantSettings.stat("peripheral_inverse", "damage_reduction_fraction", 0.4) * near);
                    }
                    if (Implant.PROXISHIELD.active(p)) {
                        amount *= (float)(1.0 - ImplantSettings.stat("proxishield", "damage_reduction_fraction", 0.2) * Math.clamp((double)((ImplantSettings.stat("proxishield", "falloff_end_range", 6.0) - (double)p.distanceTo(enemy)) / Math.max(0.01, ImplantSettings.stat("proxishield", "falloff_end_range", 6.0) - ImplantSettings.stat("proxishield", "full_strength_range", 3.0))), (double)0.0, (double)1.0));
                    }
                }
                if ((chance = ImplantEffects.mitigationChance(p)) > 0.0 && p.getRandom().nextDouble() < chance) {
                    amount *= (float)(1.0 - ImplantEffects.mitigationStrength(p));
                }
                if (Implant.CARAPACE.active(p) && !source.is(DamageTypeTags.BYPASSES_ARMOR) && ImplantEffects.sideOrRear(p, source)) {
                    event.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, reduction) -> {
                        float damage = container.getNewDamage();
                        float armor = p.getArmorValue();
                        float toughness = (float)p.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
                        float normal = CombatRules.getDamageAfterAbsorb((LivingEntity)p, (float)damage, (DamageSource)source, (float)armor, (float)toughness);
                        float boosted = CombatRules.getDamageAfterAbsorb((LivingEntity)p, (float)damage, (DamageSource)source, (float)armor, (float)(toughness + ImplantSettings.fstat("carapace", "directional_toughness", 3.0)));
                        return Math.min(damage, reduction + normal - boosted);
                    });
                }
            }
        }
        event.setAmount(amount);
    }

    public static boolean sideOrRear(Player player, DamageSource source) {
        if (source.getSourcePosition() == null) {
            return false;
        }
        Vec3 direction = source.getSourcePosition().subtract(player.position()).multiply(1.0, 0.0, 1.0);
        return direction.lengthSqr() > 1.0E-8 && Vec3.directionFromRotation((float)0.0f, (float)player.getYRot()).dot(direction) <= 1.0E-7;
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void defer(LivingDamageEvent.Pre event) {
        Player p;
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player) || (p = (Player)livingEntity).level().isClientSide() || !Implant.PAINDUCER.active(p) || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY) || TrueDamage.is(event.getSource()) || event.getNewDamage() <= 0.0f) {
            return;
        }
        float deferred = event.getNewDamage() * ImplantSettings.fstat("painducer", "deferred_damage_fraction", 0.3f);
        ImplantEffects.state(p, ImplantEffects.state(p).defer(deferred));
        event.setNewDamage(event.getNewDamage() - deferred);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void preventDeath(LivingDeathEvent event) {
        Player p;
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof Player && Implant.SECOND_HEART.installed(p = (Player)livingEntity) && !p.isSpectator() && !LimbAttachments.get(p).isMissing(BodyPart.BODY) && ImplantEffects.state(p).clock("second_heart_cooldown") == 0 && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            event.setCanceled(true);
            p.setHealth(p.getMaxHealth());
            p.invulnerableTime = 20;
            ImplantEffects.clock(p, "second_heart_cooldown", QuantumTuner.duration(p, ImplantSettings.cooldown("second_heart", 4400)));
            ImplantCooldowns.fullyUsed(p, ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"second_heart"));
            return;
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void killed(LivingDeathEvent event) {
        Player p = EnemyKills.credit(event);
        if (p == null) {
            return;
        }
        if (Implant.HEAL_ON_KILL.active(p)) {
            p.heal(p.getMaxHealth() * ImplantSettings.fstat("heal_on_kill", "heal_fraction", 0.075f));
        }
        if (Implant.NEWTON_MODULE.active(p)) {
            ImplantCooldowns.reduce(p, ImplantSettings.stat("newton_module", "cooldown_refund_fraction", 0.015));
        }
        if (Implant.ADRENALINE_BOOSTER.active(p) && ImplantEffects.meleeWeapon(p, event.getSource())) {
            ImplantEffects.restoreHunger(p, ImplantSettings.fstat("adrenaline_booster", "hunger_refill_fraction", 0.25));
        }
    }

    private static boolean meleeWeapon(Player p, DamageSource source) {
        if (!ImplantEffects.melee(source)) {
            return false;
        }
        ImplantCriticals.Weapon weapon = ImplantCriticals.weapon(p, source);
        return weapon == ImplantCriticals.Weapon.BLADE || weapon == ImplantCriticals.Weapon.FIST || source.is(MonowireCombat.DAMAGE_TYPE) || p.getMainHandItem().getItem() instanceof DiggerItem || p.getMainHandItem().is(ItemTags.SWORDS) || p.getMainHandItem().is(Items.TRIDENT);
    }

    public static void restoreHunger(Player p, float fraction) {
        if (p.isCreative()) {
            return;
        }
        FoodData food = p.getFoodData();
        int milli = food.getFoodLevel() >= 20 ? 0 : (Integer)p.getData(PlayerData.HUNGER_REFUND_MILLI) + Math.round(20000.0f * fraction);
        food.setFoodLevel(Math.min(20, food.getFoodLevel() + milli / 1000));
        p.setData(PlayerData.HUNGER_REFUND_MILLI, (Object)(food.getFoodLevel() >= 20 ? 0 : milli % 1000));
        food.setExhaustion(Math.max(0.0f, food.getExhaustionLevel() - 4.0f * fraction));
    }

    @SubscribeEvent
    public static void damaged(LivingDamageEvent.Post event) {
        Player p;
        Player attacker;
        if (event.getEntity().level().isClientSide() || event.getNewDamage() <= 0.0f) {
            return;
        }
        Entity entity = event.getSource().getEntity();
        if (entity instanceof Player && ImplantEffects.camouflaged(attacker = (Player)entity)) {
            CompoundTag tag = event.getEntity().getPersistentData();
            tag.putUUID(SPOTTED_BY, attacker.getUUID());
            tag.putLong(SPOTTED_AT, attacker.level().getGameTime());
        }
        if ((entity = event.getEntity()) instanceof Player && (p = (Player)entity).isAlive()) {
            ImplantEffects.triggerReflex(p);
            if (Implant.COUNTERSHELL.active(p)) {
                ImplantEffects.state(p, ImplantEffects.state(p).trauma(event.getNewDamage() / p.getMaxHealth()));
                if (ImplantEffects.state(p).clock("countershell_cooldown") == 0 && ImplantEffects.state(p).trauma().stream().mapToDouble(ImplantData.Trauma::fraction).sum() >= ImplantSettings.stat("countershell", "trauma_threshold_fraction", 0.35)) {
                    ImplantEffects.clock(p, "countershell", ImplantSettings.duration("countershell", 80));
                    ImplantEffects.clock(p, "countershell_cooldown", QuantumTuner.duration(p, ImplantSettings.cooldown("countershell", 120)));
                    ImplantCooldowns.fullyUsed(p, ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"countershell"));
                }
            }
            ImplantEffects.tryDischarge(p);
        }
    }

    public static void enterCombat(Player p) {
        if (Implant.ADRENALINE_CONVERTER.active(p)) {
            ImplantEffects.clock(p, "converter", ImplantSettings.duration("adrenaline_converter", 180));
        }
        if (Implant.ADRENO_TRIGGER.active(p)) {
            ImplantEffects.clock(p, "adreno", ImplantSettings.duration("adreno_trigger", 700));
        }
    }

    public static void discharge(Player p) {
        DISCHARGING.add(p);
        try {
            for (Entity target : p.level().getEntities((Entity)p, p.getBoundingBox().inflate(ImplantSettings.stat("shock_n_awe", "discharge_radius", 6.0)), e -> e.isAlive() && !e.isSpectator() && e.distanceToSqr((Entity)p) <= Math.pow(ImplantSettings.stat("shock_n_awe", "discharge_radius", 6.0), 2.0))) {
                target.hurt(p.damageSources().indirectMagic((Entity)p, (Entity)p), ImplantSettings.fstat("shock_n_awe", "discharge_damage", 10.0));
            }
            Level level = p.level();
            if (level instanceof ServerLevel) {
                ServerLevel level2 = (ServerLevel)level;
                level2.sendParticles((ParticleOptions)ParticleTypes.ELECTRIC_SPARK, p.getX(), p.getY() + 1.0, p.getZ(), 96, 3.0, 1.0, 3.0, 0.2);
                level2.playSound(null, p.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.6f, 1.5f);
            }
        }
        finally {
            DISCHARGING.remove(p);
        }
    }

    public static boolean tryDischarge(Player p) {
        if (!Implant.SHOCK_N_AWE.active(p) || DISCHARGING.contains(p) || p.getRandom().nextFloat() >= ImplantSettings.fstat("shock_n_awe", "discharge_chance", 0.2f)) {
            return false;
        }
        ImplantEffects.discharge(p);
        return true;
    }

    @SubscribeEvent
    public static void fall(LivingFallEvent event) {
        Player p;
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof Player && Implant.LYNX_PAWS.active(p = (Player)livingEntity)) {
            event.setDamageMultiplier(event.getDamageMultiplier() * ImplantSettings.fstat("lynx_paws", "fall_damage_multiplier", 0.8f));
        }
    }

    public static float naturalRegeneration(Player p, float amount) {
        return Implant.CHITIN.active(p) ? amount * ImplantSettings.fstat("chitin", "natural_healing_multiplier", 2.0) : amount;
    }

    public static void triggerReflex(Player p) {
        if (!(p instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer server = (ServerPlayer)p;
        for (Implant implant : List.of(Implant.REVULSOR, Implant.REFLEX_TUNER)) {
            if (!implant.active(p) || !((double)p.getHealth() < (double)p.getMaxHealth() * ImplantSettings.stat(implant.id, "health_threshold_fraction", 0.25)) || ImplantEffects.state(p).clock(implant.id + "_cooldown") != 0 || !Sandevistan.automatic(server, implant == Implant.REVULSOR)) continue;
            ImplantEffects.clock(p, implant.id + "_cooldown", QuantumTuner.duration(p, ImplantSettings.cooldown(implant.id, 700)));
            ImplantCooldowns.fullyUsed(p, ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)implant.id));
            break;
        }
    }

    @SubscribeEvent
    public static void poison(MobEffectEvent.Added event) {
        Player p;
        if (event.getEntity().level().isClientSide() || !event.getEffectInstance().is(MobEffects.POISON)) {
            return;
        }
        Entity source = event.getEffectSource();
        if (source instanceof Projectile) {
            Projectile projectile = (Projectile)source;
            source = projectile.getOwner();
        }
        event.getEntity().setData(PlayerData.MAMBA_POISON, (Object)(source instanceof Player && Implant.BLACK_MAMBA.active(p = (Player)source) ? 1 : 0));
    }

    public static boolean camouflaged(Player p) {
        return ((Integer)p.getData(PlayerData.CATALOGUE_FLAGS) & 1) != 0 && Implant.OPTICAL_CAMO.active(p);
    }

    private static boolean hunting(Mob mob, Player p) {
        Brain brain = mob.getBrain();
        return mob.getTarget() == p || brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null) == p;
    }

    private static boolean spotted(LivingEntity mob, Player p) {
        CompoundTag tag = mob.getPersistentData();
        return tag.hasUUID(SPOTTED_BY) && tag.getUUID(SPOTTED_BY).equals(p.getUUID()) && tag.getLong(SPOTTED_AT) >= p.getPersistentData().getLong(CAMO_SINCE);
    }

    @SubscribeEvent
    public static void unseen(LivingChangeTargetEvent event) {
        Player p;
        LivingEntity livingEntity = event.getNewAboutToBeSetTarget();
        if (livingEntity instanceof Player && ImplantEffects.camouflaged(p = (Player)livingEntity) && !ImplantEffects.spotted(event.getEntity(), p)) {
            event.setCanceled(true);
        }
    }

    public static boolean scanning(Player p) {
        return ((Integer)p.getData(PlayerData.CATALOGUE_FLAGS) & 2) != 0 && ImplantEffects.hasOptics(p);
    }

    public static boolean hasOptics(Player p) {
        return KiroshiOptics.installed(p) != null;
    }

    public static boolean activateCamo(Player p) {
        if (!Installing.canOperate(p) || !Implant.OPTICAL_CAMO.active(p)) {
            return false;
        }
        if (ImplantEffects.state(p).clock("optical_camo_cooldown") > 0) {
            CooldownNotice.show(p, Implant.OPTICAL_CAMO.stack(), ImplantEffects.state(p).clock("optical_camo_cooldown"));
            return false;
        }
        ImplantEffects.clock(p, "optical_camo", ImplantSettings.duration("optical_camo", 140));
        ImplantEffects.clock(p, "optical_camo_cooldown", QuantumTuner.duration(p, ImplantSettings.cooldown("optical_camo", 1000)));
        ImplantEffects.flags(p);
        p.getPersistentData().putLong(CAMO_SINCE, p.level().getGameTime());
        for (Mob mob : p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(64.0), m -> ImplantEffects.hunting(m, p))) {
            Netrunning.forget((Entity)mob);
        }
        ImplantCooldowns.fullyUsed(p, ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"optical_camo"));
        return true;
    }

    public static void toggleScan(Player p) {
        if (!Installing.canOperate(p) || !ImplantEffects.hasOptics(p)) {
            return;
        }
        if (ImplantEffects.scanning(p)) {
            ImplantEffects.clock(p, "scan_after", ImplantSettings.ticks(KiroshiOptics.installed((Player)p).id, "after_scan_seconds", 1200));
        }
        ImplantEffects.clock(p, "scan", ImplantEffects.scanning(p) ? 0 : Integer.MAX_VALUE);
        ImplantEffects.flags(p);
    }

    private static void flags(Player p) {
        int flags = (ImplantEffects.state(p).clock("optical_camo") > 0 && Implant.OPTICAL_CAMO.active(p) ? 1 : 0) | (ImplantEffects.state(p).clock("scan") > 0 && ImplantEffects.hasOptics(p) ? 2 : 0);
        if (flags != (Integer)p.getData(PlayerData.CATALOGUE_FLAGS)) {
            p.setData(PlayerData.CATALOGUE_FLAGS, (Object)flags);
        }
    }

    public static void removed(Player p, Implant implant) {
        if ((implant == Implant.REFLEX_TUNER || implant == Implant.REVULSOR) && p instanceof ServerPlayer) {
            ServerPlayer server = (ServerPlayer)p;
            if (Sandevistan.fields(p.level()).stream().anyMatch(f -> f.owner().equals(p.getUUID()) && f.mode() == (implant == Implant.REVULSOR ? 2 : 1))) {
                Sandevistan.deactivate(server);
            }
        }
        if (implant == Implant.OPTICAL_CAMO) {
            ImplantEffects.clock(p, "optical_camo", 0);
        }
        if (implant == Implant.TYROSINE_INJECTOR) {
            p.setData(PlayerData.TYROSINE_TICKS, (Object)0);
            p.setData(PlayerData.TYROSINE_STACKS, (Object)0);
        }
        if (implant == Implant.TITANIUM_BONES) {
            TitaniumBones.reconcile(p);
        }
        if (KiroshiOptics.optic(implant)) {
            ImplantEffects.clock(p, "scan", 0);
            ImplantEffects.clock(p, "scan_after", 0);
        }
        if (implant == Implant.UNIVERSAL_BOOSTER) {
            ImplantEffects.state(p, new ImplantData(ImplantEffects.state(p).clocks(), List.of(), ImplantEffects.state(p).debts(), ImplantEffects.state(p).trauma()));
        }
        ImplantEffects.flags(p);
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        Player p = event.getEntity();
        if (p.level().isClientSide() || !p.isAlive()) {
            return;
        }
        ImplantData before = ImplantEffects.state(p);
        float payment = before.payment();
        ImplantEffects.state(p, before.tick());
        if (payment > 0.0f && !ZetatechBerserk.active(p)) {
            float previousHealth = p.getHealth();
            float absorbed = Math.min(p.getAbsorptionAmount(), payment);
            p.setAbsorptionAmount(p.getAbsorptionAmount() - absorbed);
            p.setHealth(ZetatechBerserk.protectedHealth(p, p.damageSources().generic(), Math.max(0.0f, p.getHealth() - payment + absorbed)));
            if (p.getHealth() < previousHealth && p instanceof ServerPlayer) {
                ServerPlayer server = (ServerPlayer)p;
                PainducerPackets.send(server);
            }
            if (p.getHealth() <= 0.0f) {
                p.die(p.damageSources().generic());
            } else {
                ImplantEffects.triggerReflex(p);
            }
        }
        if (!p.isAlive()) {
            return;
        }
        if (Implant.BIOMONITOR.active(p) && (double)p.getHealth() < (double)p.getMaxHealth() * ImplantSettings.stat("biomonitor", "pump_health_threshold_fraction", 0.5) && BloodPump.available(p) && BloodPump.state(p).healingTicks() == 0) {
            BloodPump.activate(p, false);
        }
        int fatigue = before.clock("sprint_fatigue");
        ImplantEffects.clock(p, "sprint_fatigue", Implant.JENKINS_TENDONS.active(p) && p.isSprinting() ? Math.min(ImplantSettings.ticks("jenkins_tendons", "fatigue_seconds", 100), fatigue + 1) : Math.max(0, fatigue - 1));
        ImplantEffects.updateAttributes(p);
        ImplantEffects.flags(p);
        MovementImplants.tick(p);
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        ImplantEffects.state(event.getEntity(), ImplantEffects.state(event.getEntity()).clearTransient());
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player p = event.getEntity();
        ImplantEffects.clock(p, "scan", 0);
        ImplantEffects.clock(p, "optical_camo", 0);
        ImplantEffects.flags(p);
    }

    @EventBusSubscriber(modid="chromeware")
    public static final class TyrosineInjector {
        public static final int DURATION = 300;

        public static boolean boosted(Player p) {
            return Implant.TYROSINE_INJECTOR.active(p) && (Integer)p.getData(PlayerData.TYROSINE_TICKS) > 0;
        }

        @SubscribeEvent(priority=EventPriority.LOWEST)
        public static void killed(LivingDeathEvent event) {
            if (event.isCanceled()) {
                return;
            }
            Player p = EnemyKills.credit(event);
            if (p == null || !Implant.TYROSINE_INJECTOR.active(p)) {
                return;
            }
            p.setData(PlayerData.TYROSINE_STACKS, (Object)Math.min(ImplantSettings.istat("tyrosine_injector", "maximum_stacks", 10), (Integer)p.getData(PlayerData.TYROSINE_STACKS) + 1));
            p.setData(PlayerData.TYROSINE_TICKS, (Object)ImplantSettings.duration("tyrosine_injector", 300));
            ImplantEffects.updateAttributes(p);
        }
    }
}

