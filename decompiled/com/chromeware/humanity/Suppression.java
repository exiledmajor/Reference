/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 */
package com.chromeware.humanity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record Suppression(double storedPermanent, double storedTemporary, int protectionTicks, int returnInterval, int returnCountdown, Grade grade, int doses) {
    public static final Suppression NONE = new Suppression(0.0, 0.0, 0, 0, 0, Grade.NONE, 0);
    private static final Codec<Grade> GRADE = Codec.STRING.xmap(name -> {
        try {
            return Grade.valueOf(name);
        }
        catch (IllegalArgumentException unknown) {
            return Grade.NONE;
        }
    }, Enum::name);
    public static final Codec<Suppression> CODEC = RecordCodecBuilder.create(i -> i.group((App)Codec.DOUBLE.optionalFieldOf("stored_permanent", (Object)0.0).forGetter(Suppression::storedPermanent), (App)Codec.DOUBLE.optionalFieldOf("stored_temporary", (Object)0.0).forGetter(Suppression::storedTemporary), (App)Codec.INT.optionalFieldOf("protection", (Object)0).forGetter(Suppression::protectionTicks), (App)Codec.INT.optionalFieldOf("return_interval", (Object)0).forGetter(Suppression::returnInterval), (App)Codec.INT.optionalFieldOf("return_countdown", (Object)0).forGetter(Suppression::returnCountdown), (App)GRADE.optionalFieldOf("grade", (Object)Grade.NONE).forGetter(Suppression::grade), (App)Codec.INT.optionalFieldOf("doses", (Object)0).forGetter(Suppression::doses)).apply((Applicative)i, Suppression::new));
    public static final StreamCodec<ByteBuf, Suppression> STREAM_CODEC = new StreamCodec<ByteBuf, Suppression>(){

        public Suppression decode(ByteBuf b) {
            return new Suppression(b.readDouble(), b.readDouble(), (Integer)ByteBufCodecs.VAR_INT.decode((Object)b), (Integer)ByteBufCodecs.VAR_INT.decode((Object)b), (Integer)ByteBufCodecs.VAR_INT.decode((Object)b), Grade.values()[Math.clamp((long)b.readByte(), (int)0, (int)(Grade.values().length - 1))], (Integer)ByteBufCodecs.VAR_INT.decode((Object)b));
        }

        public void encode(ByteBuf b, Suppression s) {
            b.writeDouble(s.storedPermanent);
            b.writeDouble(s.storedTemporary);
            ByteBufCodecs.VAR_INT.encode((Object)b, (Object)s.protectionTicks);
            ByteBufCodecs.VAR_INT.encode((Object)b, (Object)s.returnInterval);
            ByteBufCodecs.VAR_INT.encode((Object)b, (Object)s.returnCountdown);
            b.writeByte(s.grade.ordinal());
            ByteBufCodecs.VAR_INT.encode((Object)b, (Object)s.doses);
        }
    };

    public Suppression {
        storedPermanent = Suppression.stored(storedPermanent);
        storedTemporary = Suppression.stored(storedTemporary);
        protectionTicks = Math.max(0, protectionTicks);
        doses = Math.clamp((long)doses, (int)0, (int)30);
        if (grade == null) {
            grade = Grade.NONE;
        }
        if (storedPermanent <= 1.0E-9 && storedTemporary <= 1.0E-9) {
            storedTemporary = 0.0;
            storedPermanent = 0.0;
            doses = 0;
            returnCountdown = 0;
            returnInterval = 0;
            protectionTicks = 0;
            grade = Grade.NONE;
        } else {
            returnInterval = Math.max(1, returnInterval);
            returnCountdown = Math.clamp((long)returnCountdown, (int)1, (int)returnInterval);
            if (grade == Grade.NONE && protectionTicks > 0) {
                grade = returnInterval <= Grade.MILITARY.returnInterval ? Grade.MILITARY : Grade.STANDARD;
            }
        }
    }

    public boolean protecting() {
        return this.protectionTicks > 0;
    }

    public boolean holding() {
        return this.storedPermanent > 0.0 || this.storedTemporary > 0.0;
    }

    public boolean returning() {
        return !this.protecting() && this.holding();
    }

    public double stored() {
        return this.storedPermanent + this.storedTemporary;
    }

    public Dose dose(double permanent, double temporary, Grade dose) {
        if (dose == null || dose == Grade.NONE) {
            return new Dose(this, 0.0, 0.0);
        }
        double remaining = dose.removal;
        double fromTemporary = Math.min(Math.max(0.0, temporary), remaining);
        double fromPermanent = Math.min(Math.max(0.0, permanent), remaining - fromTemporary);
        double heldPermanent = this.storedPermanent + fromPermanent;
        double heldTemporary = this.storedTemporary + fromTemporary;
        if (heldPermanent <= 1.0E-9 && heldTemporary <= 1.0E-9) {
            return new Dose(NONE, fromPermanent, fromTemporary);
        }
        Grade active = this.grade;
        int window = this.protectionTicks;
        int count = this.doses;
        if (!this.protecting()) {
            active = dose;
            count = 0;
            window = dose.protection;
        } else if (this.grade == Grade.STANDARD && dose == Grade.MILITARY) {
            active = Grade.MILITARY;
            count = 0;
            window = Grade.MILITARY.protection;
        } else {
            count = Math.min(30, count + 1);
            Grade timing = this.grade == Grade.MILITARY ? Grade.MILITARY : Grade.STANDARD;
            int exponent = count + (this.grade == Grade.MILITARY && dose == Grade.STANDARD ? 1 : 0);
            window = (int)Math.min(Integer.MAX_VALUE, (long)window + (long)(exponent >= 31 ? 0 : timing.protection >> exponent));
        }
        return new Dose(new Suppression(heldPermanent, heldTemporary, window, active.returnInterval, active.returnInterval, active, count), fromPermanent, fromTemporary);
    }

    public Returned tick(double room) {
        if (!this.holding()) {
            return new Returned(NONE, 0.0, 0.0);
        }
        if (this.protecting()) {
            int left;
            return new Returned(new Suppression(this.storedPermanent, this.storedTemporary, left, this.returnInterval, this.returnCountdown, (left = this.protectionTicks - 1) == 0 ? Grade.NONE : this.grade, left == 0 ? 0 : this.doses), 0.0, 0.0);
        }
        if (this.returnCountdown > 1) {
            return new Returned(this.withCountdown(this.returnCountdown - 1), 0.0, 0.0);
        }
        double budget = Math.clamp((double)room, (double)0.0, (double)1.0);
        if (budget <= 0.0) {
            return new Returned(this.withCountdown(this.returnInterval), 0.0, 0.0);
        }
        double permanent = Math.min(this.storedPermanent, budget);
        double temporary = Math.min(this.storedTemporary, budget - permanent);
        return new Returned(new Suppression(this.storedPermanent - permanent, this.storedTemporary - temporary, 0, this.returnInterval, this.returnInterval, this.grade, this.doses), permanent, temporary);
    }

    public Decayed decayTemporary(double budget) {
        double amount = Math.min(this.storedTemporary, Math.max(0.0, budget));
        return new Decayed(new Suppression(this.storedPermanent, this.storedTemporary - amount, this.protectionTicks, this.returnInterval, this.returnCountdown, this.grade, this.doses), amount);
    }

    public Decayed decayPermanent(double budget) {
        double amount = Math.min(this.storedPermanent, Math.max(0.0, budget));
        return new Decayed(new Suppression(this.storedPermanent - amount, this.storedTemporary, this.protectionTicks, this.returnInterval, this.returnCountdown, this.grade, this.doses), amount);
    }

    private Suppression withCountdown(int countdown) {
        return new Suppression(this.storedPermanent, this.storedTemporary, this.protectionTicks, this.returnInterval, countdown, this.grade, this.doses);
    }

    private static double stored(double value) {
        return Double.isFinite(value) ? Math.clamp((double)value, (double)0.0, (double)10000.0) : 0.0;
    }

    public static enum Grade {
        NONE(0.0, 0, 0),
        STANDARD(20.0, 12000, 100),
        MILITARY(100.0, 3600, 20);

        public final double removal;
        public final int protection;
        public final int returnInterval;

        private Grade(double removal, int protection, int returnInterval) {
            this.removal = removal;
            this.protection = protection;
            this.returnInterval = returnInterval;
        }

        public double returnPerSecond() {
            return this.returnInterval <= 0 ? 0.0 : 20.0 / (double)this.returnInterval;
        }
    }

    public record Dose(Suppression next, double permanent, double temporary) {
        public double total() {
            return this.permanent + this.temporary;
        }
    }

    public record Returned(Suppression next, double permanent, double temporary) {
        public double total() {
            return this.permanent + this.temporary;
        }
    }

    public record Decayed(Suppression next, double amount) {
    }
}

