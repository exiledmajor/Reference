/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package com.chromeware.cyberware;

import com.chromeware.cyberware.ArmControls;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.cyberware.BloodPump;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.DisabledNotice;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.cyberware.Installing;
import com.chromeware.cyberware.InventoryImplants;
import com.chromeware.cyberware.KiroshiOptics;
import com.chromeware.cyberware.SandevistanImplant;
import com.chromeware.cyberware.ZetatechBerserk;
import com.chromeware.netrunning.Netrunning;
import com.chromeware.sandevistan.Sandevistan;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public enum ImplantAbility {
    BLOOD_PUMP,
    SANDEVISTAN,
    ARM_IMPLANT,
    OPTICAL_CAMO,
    OPTICAL_SCAN,
    BERSERK,
    SUPERHERO_LANDING,
    OVERCLOCK;


    public Component label(Player player) {
        if (this == OVERCLOCK) {
            return Component.translatable((String)"ability.chromeware.overclock");
        }
        if (this == OPTICAL_SCAN) {
            return Component.translatable((String)"screen.chromeware.wheel.scanner");
        }
        if (this == SUPERHERO_LANDING) {
            return Component.translatable((String)"screen.chromeware.wheel.superhero_landing");
        }
        return this.icon(player).getHoverName();
    }

    public boolean installed(Player player) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> InventoryImplants.hasBloodPump(player);
            case 1 -> SandevistanImplant.active(player);
            case 2 -> ArmControls.deployable(player);
            case 3 -> Implant.OPTICAL_CAMO.active(player);
            case 4 -> ImplantEffects.hasOptics(player);
            case 5 -> Implant.ZETATECH_BERSERK.active(player);
            case 6 -> ZetatechBerserk.active(player);
            case 7 -> Netrunning.usable(player) || Netrunning.needsOptics(player);
        };
    }

    public ItemStack icon(Player player) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> CyberwareItems.BLOOD_PUMP.toStack();
            case 1 -> SandevistanImplant.get(player).item();
            case 2 -> {
                HumanoidArm side = ArmImplants.sideOf(player, s -> true);
                if (side == null) {
                    yield ItemStack.EMPTY;
                }
                yield ArmImplants.get(player, side).item();
            }
            case 3 -> Implant.OPTICAL_CAMO.stack();
            case 4 -> {
                Implant optic = KiroshiOptics.installed(player);
                yield (optic == null ? Implant.KIROSHI_OPTICS : optic).stack();
            }
            case 5, 6 -> Implant.ZETATECH_BERSERK.stack();
            case 7 -> CyberwareItems.CYBERDECK.toStack();
        };
    }

    public void activate(Player player) {
        if (DisabledNotice.blocked(player)) {
            return;
        }
        if (!Installing.canOperate(player) || !this.installed(player) || (Netrunning.flags((Entity)player) & 4) != 0) {
            return;
        }
        switch (this.ordinal()) {
            case 7: {
                Netrunning.overclock(player);
                break;
            }
            case 0: {
                BloodPump.activate(player);
                break;
            }
            case 1: {
                if (!(player instanceof ServerPlayer)) break;
                ServerPlayer server = (ServerPlayer)player;
                Sandevistan.toggle(server);
                break;
            }
            case 2: {
                ArmControls.toggle(player);
                break;
            }
            case 3: {
                ImplantEffects.activateCamo(player);
                break;
            }
            case 4: {
                ImplantEffects.toggleScan(player);
                break;
            }
            case 5: {
                ZetatechBerserk.toggle(player);
                break;
            }
            case 6: {
                ZetatechBerserk.land(player);
            }
        }
    }
}

