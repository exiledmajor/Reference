/*
 * Decompiled with CFR 0.152.
 */
package com.chromeware.humanity;

public final class HumanityRules {
    public static final double MAX = 100.0;
    public static final double[] THRESHOLDS = new double[]{30.0, 50.0, 70.0, 90.0, 100.0};
    public static final double REARM_MARGIN = 1.0;
    public static final double EPISODE_START = 60.0;
    public static final int SLOWEST_EPISODE = 600;
    public static final int FASTEST_EPISODE = 5;
    public static final double BERSERK_AT_START = 0.1;
    public static final double BERSERK_AT_MAX = 0.6;
    public static final int CAPACITY_CAP = 360;
    public static final double PERMANENT_PER_CAPACITY = 0.1388888888888889;
    public static final double USE_PER_CAPACITY = 0.125;
    public static final double ARM_HIT = 0.1;
    public static final double QUICKHACK_FULL_USE_RAM = 20.0;
    public static final double KILL_CREATURE = 0.25;
    public static final double KILL_PERSON = 1.0;
    public static final double OWN_PET_DIED = 15.0;
    public static final double OWN_PET_KILLED = 30.0;
    public static final double STRAIN_DECAY_PER_TICK = 0.1;
    public static final double PERMANENT_DECAY_PER_TICK = 8.333333333333334E-4;
    public static final double REMOVAL_RELIEF = 5.0;
    public static final double PET_RADIUS = 8.0;

    public static float episodeVolume(double loss) {
        return 1.0f - 0.5f * HumanityRules.intensity(loss);
    }

    public static double clamp(double value) {
        return Double.isFinite(value) ? Math.clamp((double)value, (double)0.0, (double)100.0) : 0.0;
    }

    public static double total(double permanent, double temporary) {
        return HumanityRules.clamp(HumanityRules.clamp(permanent) + HumanityRules.clamp(temporary));
    }

    public static double humanity(double loss) {
        return 100.0 - HumanityRules.clamp(loss);
    }

    public static double permanentFor(int capacityUsed) {
        return HumanityRules.clamp((double)Math.max(0, capacityUsed) * 0.1388888888888889);
    }

    public static double useCost(int capacity) {
        return (double)Math.max(0, capacity) * 0.125;
    }

    public static double quickhackCost(int deckCapacity, int ram) {
        return HumanityRules.useCost(deckCapacity) * (double)Math.max(0, ram) / 20.0;
    }

    public static int strainTicksRemaining(double temporary) {
        return HumanityRules.strainTicksRemaining(temporary, 0.1);
    }

    public static int strainTicksRemaining(double temporary, double decayPerTick) {
        return temporary <= 0.0 ? 0 : (int)Math.min(2.147483647E9, Math.ceil(HumanityRules.clamp(temporary) / decayPerTick));
    }

    public static int episodeInterval(double loss) {
        if (HumanityRules.clamp(loss) < 60.0) {
            return 0;
        }
        double f = (HumanityRules.clamp(loss) - 60.0) / 40.0;
        return (int)Math.round(600.0 * Math.pow(0.008333333333333333, f));
    }

    public static double berserkChance(double loss) {
        if (HumanityRules.clamp(loss) < 60.0) {
            return 0.0;
        }
        return 0.1 + 0.5 * (HumanityRules.clamp(loss) - 60.0) / 40.0;
    }

    public static float intensity(double loss) {
        return (float)Math.clamp((double)((HumanityRules.clamp(loss) - 60.0) / 40.0), (double)0.0, (double)1.0);
    }

    public static int reachedStages(double loss) {
        int mask = 0;
        for (int i = 0; i < THRESHOLDS.length; ++i) {
            if (!(loss + 1.0E-6 >= THRESHOLDS[i])) continue;
            mask |= 1 << i;
        }
        return mask;
    }

    public static int rearm(int mask, double loss) {
        for (int i = 0; i < THRESHOLDS.length; ++i) {
            if (!(loss < THRESHOLDS[i] - 1.0)) continue;
            mask &= ~(1 << i);
        }
        return mask;
    }

    public static int crossed(int mask, double loss) {
        int stage = -1;
        for (int i = 0; i < THRESHOLDS.length; ++i) {
            if (!(loss + 1.0E-6 >= THRESHOLDS[i]) || (mask & 1 << i) != 0) continue;
            stage = i;
        }
        return stage;
    }
}

