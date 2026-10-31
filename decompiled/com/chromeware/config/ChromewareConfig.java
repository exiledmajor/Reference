/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.neoforged.fml.ModContainer
 *  net.neoforged.fml.config.IConfigSpec
 *  net.neoforged.fml.config.ModConfig$Type
 *  net.neoforged.neoforge.common.ModConfigSpec
 *  net.neoforged.neoforge.common.ModConfigSpec$BooleanValue
 *  net.neoforged.neoforge.common.ModConfigSpec$Builder
 *  net.neoforged.neoforge.common.ModConfigSpec$ConfigValue
 *  net.neoforged.neoforge.common.ModConfigSpec$IntValue
 */
package com.chromeware.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.invoke.CallSite;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ChromewareConfig {
    public static final Map<String, Stat> DEFINITIONS = ChromewareConfig.definitions();
    public static final Map<String, ModConfigSpec.ConfigValue<? extends Number>> VALUES = new LinkedHashMap<String, ModConfigSpec.ConfigValue<? extends Number>>();
    public static final ModConfigSpec.BooleanValue HUMANITY_ENABLED;
    public static final ModConfigSpec.BooleanValue CAPACITY_ENABLED;
    public static final ModConfigSpec.IntValue STARTING_CAPACITY;
    public static final ModConfigSpec.BooleanValue PSYCHOSIS_VFX;
    public static final ModConfigSpec.BooleanValue TIME_SLOW_VFX;
    public static final ModConfigSpec.BooleanValue BERSERK_VFX;
    public static final ModConfigSpec.BooleanValue QUICKHACK_VFX;
    public static final ModConfigSpec SERVER;
    public static final ModConfigSpec CLIENT;

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, (IConfigSpec)SERVER);
        container.registerConfig(ModConfig.Type.CLIENT, (IConfigSpec)CLIENT);
    }

    public static boolean humanityEnabled() {
        return !SERVER.isLoaded() || (Boolean)HUMANITY_ENABLED.get() != false;
    }

    public static boolean capacityEnabled() {
        return !SERVER.isLoaded() || (Boolean)CAPACITY_ENABLED.get() != false;
    }

    public static int startingCapacity() {
        return SERVER.isLoaded() ? (Integer)STARTING_CAPACITY.get() : 38;
    }

    public static boolean psychosisVfx() {
        return !CLIENT.isLoaded() || (Boolean)PSYCHOSIS_VFX.get() != false;
    }

    public static boolean timeSlowVfx() {
        return !CLIENT.isLoaded() || (Boolean)TIME_SLOW_VFX.get() != false;
    }

    public static boolean berserkVfx() {
        return !CLIENT.isLoaded() || (Boolean)BERSERK_VFX.get() != false;
    }

    public static boolean quickhackVfx() {
        return !CLIENT.isLoaded() || (Boolean)QUICKHACK_VFX.get() != false;
    }

    private static Map<String, Stat> definitions() {
        LinkedHashMap<CallSite, Stat> definitions = new LinkedHashMap<CallSite, Stat>();
        try (InputStream stream = ChromewareConfig.class.getResourceAsStream("/config/chromeware-implant-defaults.json");){
            if (stream == null) {
                throw new IllegalStateException("Missing implant config defaults");
            }
            JsonObject json = JsonParser.parseReader((Reader)new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            for (Map.Entry implant : json.entrySet()) {
                for (Map.Entry entry : ((JsonElement)implant.getValue()).getAsJsonObject().entrySet()) {
                    JsonObject value = ((JsonElement)entry.getValue()).getAsJsonObject();
                    Stat stat = new Stat((String)implant.getKey(), (String)entry.getKey(), value.get("default").getAsDouble(), value.get("min").getAsDouble(), value.get("max").getAsDouble(), value.get("unit").getAsString());
                    definitions.put((CallSite)((Object)(stat.implant() + "." + stat.key())), stat);
                }
            }
        }
        catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
        return Collections.unmodifiableMap(definitions);
    }

    static {
        ModConfigSpec.Builder server = new ModConfigSpec.Builder();
        server.translation("config.chromeware.systems").push("systems");
        HUMANITY_ENABLED = server.translation("config.chromeware.humanity_enabled").define("humanity_enabled", true);
        CAPACITY_ENABLED = server.translation("config.chromeware.capacity_enabled").define("capacity_enabled", true);
        STARTING_CAPACITY = server.translation("config.chromeware.starting_capacity").defineInRange("starting_capacity", 38, 0, 10000);
        server.pop();
        server.translation("config.chromeware.implants").push("implants");
        String group = null;
        for (Stat definition : DEFINITIONS.values()) {
            if (!definition.implant().equals(group)) {
                if (group != null) {
                    server.pop();
                }
                group = definition.implant();
                server.translation("item.chromeware." + group).push(group);
            }
            ModConfigSpec.Builder builder = server.translation("config.chromeware.stat." + group + "." + definition.key());
            Object value = definition.unit().equals("half armor points") ? builder.define(definition.key(), (Object)definition.defaultValue(), raw -> {
                Number n;
                return raw instanceof Number && Double.isFinite((n = (Number)raw).doubleValue()) && n.doubleValue() >= definition.min() && n.doubleValue() <= definition.max() && Math.abs(n.doubleValue() * 2.0 - Math.rint(n.doubleValue() * 2.0)) < 1.0E-9;
            }) : (definition.unit().equals("integer") ? builder.defineInRange(definition.key(), (int)definition.defaultValue(), (int)definition.min(), (int)definition.max()) : builder.defineInRange(definition.key(), definition.defaultValue(), definition.min(), definition.max()));
            VALUES.put(group + "." + definition.key(), (ModConfigSpec.ConfigValue<? extends Number>)value);
        }
        if (group != null) {
            server.pop();
        }
        server.pop();
        SERVER = server.build();
        ModConfigSpec.Builder client = new ModConfigSpec.Builder();
        client.translation("config.chromeware.visuals").push("visuals");
        PSYCHOSIS_VFX = client.comment("Show cyberpsychosis screen distortion. Does not disable episodes, sounds or involuntary attacks.").translation("config.chromeware.psychosis_vfx").define("cyberpsychosis_vfx", true);
        TIME_SLOW_VFX = client.comment("Show time slow filter and afterimages.").translation("config.chromeware.time_slow_vfx").define("time_slow_vfx", true);
        BERSERK_VFX = client.comment("Show screen filter for Berserk.").translation("config.chromeware.berserk_vfx").define("berserk_vfx", true);
        QUICKHACK_VFX = client.comment("Show screen effects when you are hit by a quickhack. World effects stay visible.").translation("config.chromeware.quickhack_vfx").define("quickhack_vfx", true);
        client.pop();
        CLIENT = client.build();
    }

    public record Stat(String implant, String key, double defaultValue, double min, double max, String unit) {
    }
}

