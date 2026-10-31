/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package com.chromeware.cyberware;

import com.chromeware.config.ImplantSettings;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record ImplantData(Map<String, Integer> clocks, List<Booster> boosters, List<Debt> debts, List<Trauma> trauma) {
    private static final String PAYMENT_CLOCK = "painducer_payment";
    public static final Set<String> WORLD_CLOCKS = Set.of("converter", "adreno", "reflex_tuner_cooldown", "revulsor_cooldown");
    public static final ImplantData EMPTY = new ImplantData(Map.of(), List.of(), List.of(), List.of());
    public static final Codec<ImplantData> CODEC = RecordCodecBuilder.create(i -> i.group((App)Codec.unboundedMap((Codec)Codec.STRING, (Codec)Codec.INT).optionalFieldOf("clocks", Map.of()).forGetter(ImplantData::clocks), (App)Booster.CODEC.listOf().optionalFieldOf("boosters", List.of()).forGetter(ImplantData::boosters), (App)Debt.CODEC.listOf().optionalFieldOf("debts", List.of()).forGetter(ImplantData::debts), (App)Trauma.CODEC.listOf().optionalFieldOf("trauma", List.of()).forGetter(ImplantData::trauma)).apply((Applicative)i, ImplantData::new));

    public ImplantData {
        HashMap<String, Integer> timers = new HashMap<String, Integer>(clocks);
        if (debts.isEmpty()) {
            timers.remove(PAYMENT_CLOCK);
        } else {
            timers.putIfAbsent(PAYMENT_CLOCK, 20);
        }
        clocks = Map.copyOf(timers);
        boosters = List.copyOf(boosters);
        debts = List.copyOf(debts);
        trauma = List.copyOf(trauma);
    }

    public int clock(String key) {
        return this.clocks.getOrDefault(key, 0);
    }

    public ImplantData clock(String key, int ticks) {
        HashMap<String, Integer> next = new HashMap<String, Integer>(this.clocks);
        if (ticks > 0) {
            next.put(key, ticks);
        } else {
            next.remove(key);
        }
        return new ImplantData(next, this.boosters, this.debts, this.trauma);
    }

    public ImplantData boost() {
        ArrayList<Booster> next = new ArrayList<Booster>(this.boosters);
        next.add(new Booster(ImplantSettings.ticks("universal_booster", "armor_duration_seconds", 120), ImplantSettings.ticks("universal_booster", "hunger_duration_seconds", 100)));
        return new ImplantData(this.clocks, next, this.debts, this.trauma);
    }

    public ImplantData defer(float amount) {
        ArrayList<Debt> next = new ArrayList<Debt>(this.debts);
        next.add(new Debt(amount, ImplantSettings.duration("painducer", 100)));
        return new ImplantData(this.clocks, this.boosters, next, this.trauma);
    }

    public ImplantData trauma(float fraction) {
        ArrayList<Trauma> next = new ArrayList<Trauma>(this.trauma);
        next.add(new Trauma(fraction, ImplantSettings.ticks("countershell", "trauma_window_seconds", 60)));
        return new ImplantData(this.clocks, this.boosters, this.debts, next);
    }

    public float payment() {
        return this.clock(PAYMENT_CLOCK) == 1 ? (float)this.debts.stream().mapToDouble(Debt::payment).sum() : 0.0f;
    }

    public ImplantData tick() {
        HashMap<String, Integer> next = new HashMap<String, Integer>();
        this.clocks.forEach((key, value) -> {
            if (WORLD_CLOCKS.contains(key)) {
                next.put((String)key, (Integer)value);
            } else if (value > 1) {
                next.put((String)key, value - 1);
            }
        });
        boolean pay = this.clock(PAYMENT_CLOCK) == 1;
        return new ImplantData(next, this.boosters.stream().filter(b -> b.armorTicks > 1).map(Booster::tick).toList(), pay ? this.debts.stream().filter(d -> d.ticks > 20).map(d -> new Debt(Math.max(0.0f, d.remaining - d.payment()), d.ticks - 20)).toList() : this.debts, this.trauma.stream().filter(t -> t.ticks > 1).map(t -> new Trauma(t.fraction, t.ticks - 1)).toList());
    }

    public ImplantData clearTransient() {
        HashMap<String, Integer> next = new HashMap<String, Integer>(this.clocks);
        next.keySet().removeIf(k -> !k.endsWith("_cooldown"));
        return new ImplantData(next, List.of(), List.of(), List.of());
    }

    public record Booster(int armorTicks, int hungerTicks) {
        private static final Codec<Booster> TIMERS = RecordCodecBuilder.create(i -> i.group((App)Codec.intRange((int)1, (int)Integer.MAX_VALUE).fieldOf("armor_ticks").forGetter(Booster::armorTicks), (App)Codec.intRange((int)0, (int)Integer.MAX_VALUE).fieldOf("hunger_ticks").forGetter(Booster::hungerTicks)).apply((Applicative)i, Booster::new));
        public static final Codec<Booster> CODEC = Codec.either((Codec)Codec.intRange((int)1, (int)100), TIMERS).xmap(e -> (Booster)e.map(t -> new Booster((int)t, (int)t), b -> b), b -> Either.right((Object)b));

        public Booster tick() {
            return new Booster(this.armorTicks - 1, Math.max(0, this.hungerTicks - 1));
        }
    }

    public record Debt(float remaining, int ticks) {
        public static final Codec<Debt> CODEC = RecordCodecBuilder.create(i -> i.group((App)Codec.floatRange((float)0.0f, (float)Float.MAX_VALUE).fieldOf("remaining").forGetter(Debt::remaining), (App)Codec.intRange((int)1, (int)Integer.MAX_VALUE).fieldOf("ticks").forGetter(Debt::ticks)).apply((Applicative)i, Debt::new));

        public float payment() {
            return this.remaining / (float)((this.ticks + 19) / 20);
        }
    }

    public record Trauma(float fraction, int ticks) {
        public static final Codec<Trauma> CODEC = RecordCodecBuilder.create(i -> i.group((App)Codec.FLOAT.fieldOf("fraction").forGetter(Trauma::fraction), (App)Codec.INT.fieldOf("ticks").forGetter(Trauma::ticks)).apply((Applicative)i, Trauma::new));
    }
}

