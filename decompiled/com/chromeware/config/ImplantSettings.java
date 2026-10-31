/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.neoforge.common.ModConfigSpec$ConfigValue
 */
package com.chromeware.config;

import com.chromeware.config.ChromewareConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ImplantSettings {
    public static double stat(String implant, String key, double fallback) {
        ModConfigSpec.ConfigValue<? extends Number> value = ChromewareConfig.VALUES.get(implant + "." + key);
        return value != null && ChromewareConfig.SERVER.isLoaded() ? ((Number)value.get()).doubleValue() : fallback;
    }

    public static float fstat(String implant, String key, double fallback) {
        return (float)ImplantSettings.stat(implant, key, fallback);
    }

    public static int istat(String implant, String key, int fallback) {
        return (int)Math.round(ImplantSettings.stat(implant, key, fallback));
    }

    public static int ticks(String implant, String key, int fallback) {
        return (int)Math.round(ImplantSettings.stat(implant, key, (double)fallback / 20.0) * 20.0);
    }

    public static int cooldown(String implant, int fallback) {
        return ImplantSettings.ticks(ImplantSettings.canonical(implant), "cooldown_seconds", fallback);
    }

    public static int duration(String implant, int fallback) {
        return ImplantSettings.ticks(ImplantSettings.canonical(implant), "duration_seconds", fallback);
    }

    public static String canonical(String implant) {
        return switch (implant) {
            case "launcher" -> "projectile_launch_system";
            case "monowire", "mantis", "gorilla" -> {
                if (implant.equals("mantis")) {
                    yield "mantis_blades";
                }
                if (implant.equals("gorilla")) {
                    yield "gorilla_arms";
                }
                yield implant;
            }
            case "cyberdeck_overclock" -> "cyberdeck";
            default -> implant;
        };
    }
}

