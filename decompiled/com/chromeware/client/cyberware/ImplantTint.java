/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.Resource
 *  net.minecraft.util.FastColor$ABGR32
 *  net.minecraft.util.FastColor$ARGB32
 *  net.minecraft.world.entity.player.Player
 */
package com.chromeware.client.cyberware;

import com.chromeware.Chromeware;
import com.chromeware.cyberware.ImplantColors;
import com.chromeware.cyberware.PlayerData;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;

public final class ImplantTint {
    private static final Map<String, ResourceLocation> CACHE = new LinkedHashMap<String, ResourceLocation>(16, 0.75f, true);
    private static final Map<String, Analysis> ANALYSES = new HashMap<String, Analysis>();
    private static final int MAX = 64;
    private static final Map<String, List<String>> PARTS = Map.of("sandevistan", List.of("brass", "steel", "copper", "lights"), "mantis_blades", List.of("steel", "dark", "brass"), "gorilla_arms", List.of("steel", "copper", "brass", "dark"), "projectile_launch_system", List.of("steel", "dark", "brass"), "monowire", List.of("dark", "steel", "brass", "wire"), "extendo_grip", List.of("brass", "steel"), "cyber_limbs", List.of("brass", "steel", "dark"));

    public static ResourceLocation base(String implant) {
        return ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)("textures/entity/" + implant + ".png"));
    }

    public static ResourceLocation texture(Player player, String implant) {
        return ImplantTint.texture(player == null ? ImplantColors.EMPTY : (ImplantColors)player.getData(PlayerData.IMPLANT_COLORS), implant);
    }

    public static ResourceLocation texture(ImplantColors colors, String implant) {
        return ImplantTint.texture(colors, implant, implant);
    }

    public static ResourceLocation texture(ImplantColors colors, String implant, String sheet) {
        if (!colors.customizes(implant)) {
            return ImplantTint.base(sheet);
        }
        return ImplantTint.generate(colors, implant, sheet, false);
    }

    public static ResourceLocation lights(Player player, String implant) {
        return ImplantTint.generate((ImplantColors)player.getData(PlayerData.IMPLANT_COLORS), implant, implant, true);
    }

    private static ResourceLocation generate(ImplantColors colors, String implant, String sheet, boolean lights) {
        String key = sheet + "|" + implant + "|" + colors.signature(implant) + (lights ? "|lights" : "");
        ResourceLocation cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        Analysis analysis = ImplantTint.analysis(sheet);
        if (analysis == null) {
            return lights ? null : ImplantTint.base(sheet);
        }
        NativeImage image = new NativeImage(analysis.width, analysis.height, false);
        for (int y = 0; y < analysis.height; ++y) {
            for (int x = 0; x < analysis.width; ++x) {
                Integer target;
                int argb = analysis.pixels[y * analysis.width + x];
                int alpha = argb >>> 24;
                String group = analysis.groups[y * analysis.width + x];
                Integer n = target = group == null || !PARTS.getOrDefault(implant, List.of()).contains(group) ? null : colors.get(implant, group);
                if (target != null && alpha > 0) {
                    float shade = ImplantTint.luminance(argb) / Math.max(0.001f, analysis.mean.get(group).floatValue());
                    int r = ImplantTint.clamp((float)FastColor.ARGB32.red((int)target) * shade);
                    int g = ImplantTint.clamp((float)FastColor.ARGB32.green((int)target) * shade);
                    int b = ImplantTint.clamp((float)(target & 0xFF) * shade);
                    argb = alpha << 24 | r << 16 | g << 8 | b;
                }
                if (lights) {
                    argb = "lights".equals(group) ? ImplantTint.glow(argb) : 0;
                }
                image.setPixelRGBA(x, y, FastColor.ABGR32.fromArgb32((int)argb));
            }
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)("tint/" + sheet + "/" + Integer.toHexString(key.hashCode())));
        Minecraft.getInstance().getTextureManager().register(id, (AbstractTexture)new DynamicTexture(image));
        CACHE.put(key, id);
        if (CACHE.size() > 64) {
            Map.Entry<String, ResourceLocation> eldest = CACHE.entrySet().iterator().next();
            Minecraft.getInstance().getTextureManager().release(eldest.getValue());
            CACHE.remove(eldest.getKey());
        }
        return id;
    }

    public static boolean lit(String implant, float u0, float v0, float u1, float v1) {
        Analysis a = ImplantTint.analysis(implant);
        if (a == null) {
            return false;
        }
        int x0 = (int)Math.floor(Math.min(u0, u1) * (float)a.width);
        int y0 = (int)Math.floor(Math.min(v0, v1) * (float)a.height);
        int x1 = Math.max(x0 + 1, (int)Math.ceil(Math.max(u0, u1) * (float)a.width));
        int y1 = Math.max(y0 + 1, (int)Math.ceil(Math.max(v0, v1) * (float)a.height));
        for (int y = Math.max(0, y0); y < Math.min(a.height, y1); ++y) {
            for (int x = Math.max(0, x0); x < Math.min(a.width, x1); ++x) {
                if (!"lights".equals(a.groups[y * a.width + x])) continue;
                return true;
            }
        }
        return false;
    }

    public static int lightColor(Player player, String implant) {
        Integer color = ((ImplantColors)player.getData(PlayerData.IMPLANT_COLORS)).get(implant, "lights");
        return ImplantTint.glow(0xFF000000 | (color != null ? color : ImplantTint.defaultColor(implant, "lights"))) & 0xFFFFFF;
    }

    public static List<String> parts(String implant) {
        Analysis analysis = ImplantTint.analysis(implant);
        return analysis == null ? List.of() : PARTS.getOrDefault(implant, List.of()).stream().filter(p -> p.equals("wire") || analysis.mean.containsKey(p)).toList();
    }

    public static int defaultColor(String implant, String part) {
        if (part.equals("wire")) {
            return 16729376;
        }
        Analysis analysis = ImplantTint.analysis(implant);
        return analysis == null || !analysis.average.containsKey(part) ? 0x808080 : analysis.average.get(part);
    }

    public static String classify(int argb) {
        float s;
        float r = (float)FastColor.ARGB32.red((int)argb) / 255.0f;
        float g = (float)FastColor.ARGB32.green((int)argb) / 255.0f;
        float b = (float)FastColor.ARGB32.blue((int)argb) / 255.0f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float v = max;
        float f = s = max == 0.0f ? 0.0f : (max - min) / max;
        float h = max == min ? 0.0f : (max == r ? 60.0f * ((g - b) / (max - min) % 6.0f) : (max == g ? 60.0f * ((b - r) / (max - min) + 2.0f) : 60.0f * ((r - g) / (max - min) + 4.0f)));
        if (h < 0.0f) {
            h += 360.0f;
        }
        if (h >= 65.0f && h <= 170.0f && s > 0.12f && v > 0.3f) {
            return "lights";
        }
        if (v < 0.25f) {
            return "dark";
        }
        if (s > 0.28f && h >= 32.0f && h <= 62.0f) {
            return "brass";
        }
        if (s > 0.28f && (h < 32.0f || h > 330.0f)) {
            return "copper";
        }
        if (v < 0.3f) {
            return "dark";
        }
        return "steel";
    }

    private static Analysis analysis(String implant) {
        return ANALYSES.computeIfAbsent(implant, ImplantTint::analyse);
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private static Analysis analyse(String implant) {
        Optional resource = Minecraft.getInstance().getResourceManager().getResource(ImplantTint.base(implant));
        if (resource.isEmpty()) {
            return null;
        }
        try (InputStream stream = ((Resource)resource.get()).open();){
            Analysis analysis;
            block17: {
                NativeImage image = NativeImage.read((InputStream)stream);
                try {
                    int w = image.getWidth();
                    int h = image.getHeight();
                    int[] pixels = new int[w * h];
                    String[] groups = new String[w * h];
                    HashMap<String, float[]> sums = new HashMap<String, float[]>();
                    for (int y = 0; y < h; ++y) {
                        for (int x = 0; x < w; ++x) {
                            String group2;
                            int argb;
                            pixels[y * w + x] = argb = FastColor.ABGR32.fromArgb32((int)image.getPixelRGBA(x, y));
                            if (argb >>> 24 < 10) continue;
                            groups[y * w + x] = group2 = implant.equals("extendo_grip") ? (x < w / 2 ? "brass" : "steel") : ImplantTint.classify(argb);
                            float[] sum2 = sums.computeIfAbsent(group2, k -> new float[5]);
                            sum2[0] = sum2[0] + ImplantTint.luminance(argb);
                            sum2[1] = sum2[1] + (float)(argb >> 16 & 0xFF);
                            sum2[2] = sum2[2] + (float)(argb >> 8 & 0xFF);
                            sum2[3] = sum2[3] + (float)(argb & 0xFF);
                            sum2[4] = sum2[4] + 1.0f;
                        }
                    }
                    HashMap<String, Float> mean = new HashMap<String, Float>();
                    HashMap<String, Integer> average = new HashMap<String, Integer>();
                    sums.forEach((group, sum) -> {
                        mean.put((String)group, Float.valueOf(sum[0] / sum[4]));
                        average.put((String)group, (int)(sum[1] / sum[4]) << 16 | (int)(sum[2] / sum[4]) << 8 | (int)(sum[3] / sum[4]));
                    });
                    analysis = new Analysis(w, h, pixels, groups, mean, average);
                    if (image == null) break block17;
                }
                catch (Throwable throwable) {
                    if (image != null) {
                        try {
                            image.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                image.close();
            }
            return analysis;
        }
        catch (Exception e) {
            Chromeware.LOGGER.warn("Could not analyse {} for recolouring", (Object)implant, (Object)e);
            return null;
        }
    }

    private static float luminance(int argb) {
        return (0.2126f * (float)FastColor.ARGB32.red((int)argb) + 0.7152f * (float)FastColor.ARGB32.green((int)argb) + 0.0722f * (float)FastColor.ARGB32.blue((int)argb)) / 255.0f;
    }

    private static int glow(int argb) {
        float scale = 255.0f / (float)Math.max(1, Math.max(argb >> 16 & 0xFF, Math.max(argb >> 8 & 0xFF, argb & 0xFF)));
        return argb & 0xFF000000 | ImplantTint.clamp((float)FastColor.ARGB32.red((int)argb) * scale) << 16 | ImplantTint.clamp((float)FastColor.ARGB32.green((int)argb) * scale) << 8 | ImplantTint.clamp((float)FastColor.ARGB32.blue((int)argb) * scale);
    }

    private static int clamp(float v) {
        return Math.max(0, Math.min(255, Math.round(v)));
    }

    private record Analysis(int width, int height, int[] pixels, String[] groups, Map<String, Float> mean, Map<String, Integer> average) {
    }
}

