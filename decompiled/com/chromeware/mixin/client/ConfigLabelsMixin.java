/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.createmod.catnip.config.ui.SubMenuConfigScreen
 *  net.minecraft.network.chat.Component
 *  net.neoforged.neoforge.common.ModConfigSpec
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.chromeware.mixin.client;

import com.chromeware.config.ChromewareConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.createmod.catnip.config.ui.SubMenuConfigScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={SubMenuConfigScreen.class}, remap=false)
public abstract class ConfigLabelsMixin {
    @Shadow
    protected ModConfigSpec spec;

    @ModifyExpressionValue(method={"lambda$init$8"}, at={@At(value="INVOKE", target="Lnet/createmod/catnip/config/ui/SubMenuConfigScreen;toHumanReadable(Ljava/lang/String;)Ljava/lang/String;")})
    private String chrome$visualLabels(String original, @Local(argsOnly=true) String key) {
        if (this.spec != ChromewareConfig.CLIENT) {
            return original;
        }
        return switch (key) {
            case "cyberpsychosis_vfx" -> Component.translatable((String)"config.chromeware.psychosis_vfx").getString();
            case "time_slow_vfx" -> Component.translatable((String)"config.chromeware.time_slow_vfx").getString();
            case "berserk_vfx" -> Component.translatable((String)"config.chromeware.berserk_vfx").getString();
            case "quickhack_vfx" -> Component.translatable((String)"config.chromeware.quickhack_vfx").getString();
            default -> original;
        };
    }
}

