/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 */
package com.chromeware.cyberware;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ImplantColors(Map<String, Integer> colors) {
    public static final List<String> IMPLANTS = List.of("sandevistan", "mantis_blades", "gorilla_arms", "projectile_launch_system", "monowire", "extendo_grip", "cyber_limbs");
    public static final List<String> PARTS = List.of("brass", "copper", "steel", "dark", "lights", "wire");
    public static final ImplantColors EMPTY = new ImplantColors(Map.of());
    public static final Codec<ImplantColors> CODEC = Codec.unboundedMap((Codec)Codec.STRING, (Codec)Codec.INT).xmap(ImplantColors::new, ImplantColors::colors);
    public static final StreamCodec<ByteBuf, ImplantColors> STREAM_CODEC = ByteBufCodecs.map(HashMap::new, (StreamCodec)ByteBufCodecs.STRING_UTF8, (StreamCodec)ByteBufCodecs.INT).map(ImplantColors::new, c -> new HashMap<String, Integer>(c.colors()));

    public ImplantColors {
        colors = Map.copyOf(colors);
    }

    public static String key(String implant, String part) {
        return implant + ":" + part;
    }

    public static boolean valid(String key) {
        int split = key.indexOf(58);
        return split > 0 && IMPLANTS.contains(key.substring(0, split)) && PARTS.contains(key.substring(split + 1));
    }

    public Integer get(String implant, String part) {
        return this.colors.get(ImplantColors.key(implant, part));
    }

    public boolean customizes(String implant) {
        return this.colors.keySet().stream().anyMatch(k -> k.startsWith(implant + ":"));
    }

    public String signature(String implant) {
        StringBuilder sb = new StringBuilder();
        for (String part : PARTS) {
            Integer c = this.get(implant, part);
            if (c == null) continue;
            sb.append(part).append('=').append(Integer.toHexString(c)).append(';');
        }
        return sb.toString();
    }

    public ImplantColors with(String key, int rgb) {
        HashMap<String, Integer> next = new HashMap<String, Integer>(this.colors);
        if (rgb < 0) {
            next.remove(key);
        } else {
            next.put(key, rgb & 0xFFFFFF);
        }
        return new ImplantColors(next);
    }
}

