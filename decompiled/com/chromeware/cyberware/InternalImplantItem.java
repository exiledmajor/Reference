/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.flag.FeatureFlagSet
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 */
package com.chromeware.cyberware;

import com.chromeware.compat.guns.GunCyberware;
import com.chromeware.config.ConfiguredStats;
import com.chromeware.cyberware.CyberwareStats;
import com.chromeware.cyberware.Scarab;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class InternalImplantItem
extends Item {
    private final String implant;

    public InternalImplantItem(String implant) {
        super(new Item.Properties().stacksTo(1));
        this.implant = implant;
    }

    public boolean isEnabled(FeatureFlagSet flags) {
        return super.isEnabled(flags) && (!this.implant.equals("shock_absorber") && !this.implant.equals("immovable_force") && !this.implant.equals("microgenerator") || GunCyberware.available());
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        if (ConfiguredStats.appendIfCustomized(stack, lines)) {
            return;
        }
        for (String line : Component.translatable((String)("tooltip.chromeware." + this.implant)).getString().split("\n")) {
            lines.add((Component)Component.literal((String)line).withStyle(ChatFormatting.GRAY));
        }
        if (this.implant.equals("neofiber")) {
            lines.add((Component)Component.translatable((String)"tooltip.chromeware.neofiber_strength").withStyle(ChatFormatting.GRAY));
        }
        if (this.implant.equals("atomic_sensors")) {
            lines.add((Component)Component.translatable((String)"tooltip.chromeware.atomic_sensors_combat").withStyle(ChatFormatting.GRAY));
        }
        if (this.implant.equals("revulsor")) {
            lines.add((Component)Component.translatable((String)"tooltip.chromeware.revulsor_movement").withStyle(ChatFormatting.GRAY));
        }
        if (this.implant.equals("tyrosine_injector")) {
            lines.add((Component)Component.translatable((String)"tooltip.chromeware.tyrosine_injector_speed").withStyle(ChatFormatting.GRAY));
        }
        if (this.implant.equals("zetatech_berserk")) {
            for (String suffix : List.of("invulnerable", "items", "melee", "attack", "hunger", "movement", "damage", "end", "heal")) {
                lines.add((Component)Component.translatable((String)("tooltip.chromeware.zetatech_berserk_" + suffix)).withStyle(ChatFormatting.GRAY));
            }
        }
        if (this.implant.equals("sandevistan")) {
            for (String suffix : List.of("slow", "crit", "kills", "extension", "heal", "duration")) {
                lines.add((Component)Component.translatable((String)("tooltip.chromeware.sandevistan_" + suffix)).withStyle(ChatFormatting.GRAY));
            }
        }
        if (List.of("bioconductor", "cox_2_cybersomatic_optimizer").contains(this.implant)) {
            lines.add((Component)Component.translatable((String)("tooltip.chromeware." + this.implant + "_crit")).withStyle(ChatFormatting.GRAY));
        }
        if (List.of("bioconductor", "cox_2_cybersomatic_optimizer", "camillo_ram_manager", "mechatronic_core", "memory_boost", "ram_reallocator", "ram_upgrade", "self_ice").contains(this.implant)) {
            lines.add((Component)Component.translatable((String)("tooltip.chromeware." + this.implant + "_max")).withStyle(ChatFormatting.GRAY));
        }
        if (this.implant.equals("ex_disk")) {
            lines.add((Component)Component.translatable((String)"tooltip.chromeware.ex_disk_upload").withStyle(ChatFormatting.GRAY));
        }
        if (this.implant.equals("ram_recoup")) {
            lines.add((Component)Component.translatable((String)"tooltip.chromeware.ram_recoup_max").withStyle(ChatFormatting.GRAY));
        }
        if (this.implant.equals("scarab")) {
            lines.add((Component)Component.translatable((String)"tooltip.chromeware.scarab_armor", (Object[])new Object[]{CyberwareStats.formatArmor(Scarab.bonusArmor())}).withStyle(ChatFormatting.GRAY));
            lines.add((Component)Component.translatable((String)"tooltip.chromeware.scarab_speed").withStyle(ChatFormatting.GRAY));
        }
        if (this.implant.equals("universal_booster")) {
            for (String suffix : List.of("armor", "hunger", "stacking")) {
                lines.add((Component)Component.translatable((String)("tooltip.chromeware.universal_booster_" + suffix)).withStyle(ChatFormatting.GRAY));
            }
        }
        if (this.implant.equals("blood_pump")) {
            lines.add((Component)Component.translatable((String)"tooltip.chromeware.blood_pump_charges").withStyle(ChatFormatting.GRAY));
        }
        if (this.implant.equals("quantum_tuner")) {
            lines.add((Component)Component.translatable((String)"tooltip.chromeware.quantum_tuner_recovery").withStyle(ChatFormatting.GRAY));
        }
        if (this.implant.equals("fortified_ankles")) {
            lines.add((Component)Component.translatable((String)"tooltip.chromeware.fortified_ankles_charge").withStyle(ChatFormatting.GRAY));
        }
        if (this.implant.equals("lynx_paws")) {
            lines.add((Component)Component.translatable((String)"tooltip.chromeware.lynx_paws_crouch").withStyle(ChatFormatting.GRAY));
            lines.add((Component)Component.translatable((String)"tooltip.chromeware.lynx_paws_fall").withStyle(ChatFormatting.GRAY));
        }
        if (this.implant.equals("peripheral_inverse") || this.implant.equals("proxishield")) {
            lines.add((Component)Component.translatable((String)("tooltip.chromeware." + this.implant + "_close")).withStyle(ChatFormatting.GRAY));
            lines.add((Component)Component.translatable((String)("tooltip.chromeware." + this.implant + "_taper")).withStyle(ChatFormatting.GRAY));
        }
    }
}

