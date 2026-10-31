/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package com.chromeware.netrunning;

import java.util.Locale;
import net.minecraft.network.chat.Component;

public enum Quickhack {
    SHORT_CIRCUIT(Kind.COMBAT, 10, 10, 0, "Electrical burst and EMP; extends Control hacks; mechanical targets take more."),
    OVERHEAT(Kind.COMBAT, 9, 40, 100, "Sets the target on fire and melts its armor; detonates Contagion."),
    CONTAGION(Kind.COMBAT, 12, 40, 160, "Poison spreads to four nearby targets; fire detonates it."),
    SYNAPSE_BURNOUT(Kind.COMBAT, 16, 60, 0, "Heavy damage that grows with spent RAM; kills extend Overclock."),
    REBOOT_OPTICS(Kind.CONTROL, 7, 6, 300, "Blinds; critical and head hits deal more, and such a kill jumps on."),
    WEAPON_GLITCH(Kind.CONTROL, 6, 6, 280, "Pops the held weapon: no shooting or items, half melee damage."),
    CRIPPLE_MOVEMENT(Kind.CONTROL, 6, 6, 240, "Locks movement and jumping; melee hits on it deal more."),
    CYBERWARE_MALFUNCTION(Kind.CONTROL, 4, 6, 200, "Stacks to five: disables cyberware, burns, then overloads."),
    BAIT(Kind.COVERT, 4, 6, 300, "Lures the target to you; again, back where it came from. Players are pinged."),
    REQUEST_BACKUP(Kind.COVERT, 6, 40, 300, "Calls the nearest creature within 30 blocks; five on a Baited target."),
    MEMORY_WIPE(Kind.COVERT, 10, 30, 100, "The target forgets you for 5 seconds; a player cannot see you."),
    SYSTEM_COLLAPSE(Kind.ULTIMATE, 28, 20, 0, "Kills the target without a death message."),
    CYBERPSYCHOSIS(Kind.ULTIMATE, 22, 30, 400, "The target attacks others for 20 seconds, or kills itself alone."),
    DETONATE_GRENADE(Kind.ULTIMATE, 24, 20, 0, "Explodes what the target carries, or an explosive block or creature."),
    SUICIDE(Kind.ULTIMATE, 24, 20, 0, "The target kills itself."),
    BLACKWALL_GATEWAY(Kind.ULTIMATE, 12, 60, 0, "Lethal; spreads to three enemies, each spread faster and dearer."),
    REMOTE_ACTIVATION(Kind.DEVICE, 2, 10, 0, "Opens and closes doors, switches lamps and other redstone devices.");

    public final Kind kind;
    public final int ram;
    public final int uploadTicks;
    public final int duration;
    public final String id;
    public final String description;

    private Quickhack(Kind kind, int ram, int uploadTicks, int duration, String description) {
        this.kind = kind;
        this.ram = ram;
        this.uploadTicks = uploadTicks;
        this.duration = duration;
        this.description = description;
        this.id = this.name().toLowerCase(Locale.ROOT);
    }

    public boolean device() {
        return this.kind == Kind.DEVICE;
    }

    public boolean blocks() {
        return this.device() || this == DETONATE_GRENADE;
    }

    public Component title() {
        return Component.translatable((String)("quickhack.chromeware." + this.id));
    }

    public Component details() {
        return Component.translatable((String)("quickhack.chromeware." + this.id + ".description"));
    }

    public static Quickhack byId(String id) {
        for (Quickhack h : Quickhack.values()) {
            if (!h.id.equals(id)) continue;
            return h;
        }
        return null;
    }

    public static Quickhack byIndex(int index) {
        return index >= 0 && index < Quickhack.values().length ? Quickhack.values()[index] : null;
    }

    public static enum Kind {
        COMBAT,
        CONTROL,
        COVERT,
        ULTIMATE,
        DEVICE;

    }
}

