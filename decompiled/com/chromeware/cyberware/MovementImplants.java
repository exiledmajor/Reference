/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 */
package com.chromeware.cyberware;

import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.DisabledNotice;
import com.chromeware.cyberware.FortifiedJumpState;
import com.chromeware.cyberware.GorillaBleeding;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.Installing;
import com.chromeware.netrunning.Netrunning;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class MovementImplants {
    private static final Map<Player, Session> SESSIONS = new WeakHashMap<Player, Session>();

    public static void tick(Player p) {
        Session session = SESSIONS.computeIfAbsent(p, key -> new Session());
        if (p.onGround()) {
            session.canDouble = true;
        }
        if (!MovementImplants.valid(p) || !Implant.FORTIFIED_ANKLES.active(p)) {
            session.ankles.cancel();
        } else {
            session.ankles.tick(p.onGround());
        }
    }

    private static boolean valid(Player p) {
        return Installing.canOperate(p) && !GorillaBleeding.active((LivingEntity)p) && !p.isPassenger() && !p.getAbilities().flying && (Netrunning.flags((Entity)p) & 1) == 0 && !p.isFallFlying() && !p.isInWaterOrBubble() && !p.isInLava() && !p.onClimbable();
    }

    public static boolean request(Player p, int action) {
        if (p.level().isClientSide()) {
            return false;
        }
        Session s = SESSIONS.computeIfAbsent(p, key -> new Session());
        if (action == 3) {
            s.ankles.cancel();
            return true;
        }
        if (DisabledNotice.blocked(p)) {
            return false;
        }
        if (!MovementImplants.valid(p)) {
            s.ankles.cancel();
            return false;
        }
        if (action == 1 && Implant.FORTIFIED_ANKLES.active(p)) {
            s.ankles.press(p.onGround());
            return true;
        }
        if (action == 2) {
            double velocity = s.ankles.release(p.onGround());
            if (!Implant.FORTIFIED_ANKLES.active(p) || velocity == 0.0) {
                return false;
            }
            MovementImplants.jump(p, velocity);
            return true;
        }
        if (action == 0 && Implant.REINFORCED_TENDONS.active(p) && !p.onGround() && s.canDouble) {
            s.canDouble = false;
            MovementImplants.jump(p, ImplantSettings.stat("reinforced_tendons", "double_jump_velocity", 0.42));
            Level level = p.level();
            if (level instanceof ServerLevel) {
                ServerLevel level2 = (ServerLevel)level;
                level2.sendParticles((ParticleOptions)ParticleTypes.GUST_EMITTER_SMALL, p.getX(), p.getY() + 0.1, p.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            }
            return true;
        }
        return false;
    }

    private static void jump(Player p, double velocity) {
        p.setDeltaMovement(p.getDeltaMovement().x, velocity, p.getDeltaMovement().z);
        p.setOnGround(false);
        p.fallDistance = 0.0f;
        p.hasImpulse = true;
        p.causeFoodExhaustion(p.isSprinting() ? 0.2f : 0.05f);
    }

    private static final class Session {
        final FortifiedJumpState ankles = new FortifiedJumpState();
        boolean canDouble;

        private Session() {
        }
    }
}

