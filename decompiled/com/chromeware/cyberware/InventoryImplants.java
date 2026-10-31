/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package com.chromeware.cyberware;

import com.chromeware.cyberware.BloodPump;
import com.chromeware.cyberware.CyberwareArmor;
import com.chromeware.cyberware.CyberwareCapacity;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.CyberwareSlots;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantCategory;
import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.cyberware.InstalledImplant;
import com.chromeware.cyberware.Installing;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.cyberware.TitaniumBones;
import com.chromeware.humanity.Humanity;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class InventoryImplants {
    public static final int CIRCULATORY = 0;
    public static final int FRONTAL_CORTEX = 1;
    public static final int SKELETON = 2;
    private static final Map<Player, Integer> REQUESTED = new WeakHashMap<Player, Integer>();

    public static boolean supported(ItemStack item) {
        Implant definition = Implant.of(item);
        if (definition != null) {
            return definition.available() && definition.category != ImplantCategory.OPERATING_SYSTEM;
        }
        return item.is(CyberwareItems.BLOOD_PUMP) || item.is(CyberwareItems.AXOLOTL) || item.is(CyberwareItems.QUANTUM_TUNER) || item.is(CyberwareItems.DENSE_MARROW) || item.is(CyberwareItems.RARA_AVIS) || item.is(CyberwareItems.SCARAB);
    }

    public static CyberwareSlots get(Player player, int category) {
        return switch (category) {
            case 0 -> (CyberwareSlots)player.getData(PlayerData.CIRCULATORY);
            case 1 -> (CyberwareSlots)player.getData(PlayerData.FRONTAL_CORTEX);
            case 2 -> (CyberwareSlots)player.getData(PlayerData.SKELETON);
            case 3 -> (CyberwareSlots)player.getData(PlayerData.NERVOUS_SYSTEM);
            case 4 -> (CyberwareSlots)player.getData(PlayerData.INTEGUMENTARY_SYSTEM);
            case 5 -> (CyberwareSlots)player.getData(PlayerData.LEGS);
            case 6 -> (CyberwareSlots)player.getData(PlayerData.HANDS);
            case 7 -> (CyberwareSlots)player.getData(PlayerData.FACE);
            default -> CyberwareSlots.EMPTY;
        };
    }

    private static void set(Player player, int category, CyberwareSlots slots) {
        if (category == 0) {
            player.setData(PlayerData.CIRCULATORY, (Object)slots);
        } else if (category == 1) {
            player.setData(PlayerData.FRONTAL_CORTEX, (Object)slots);
        } else if (category == 2) {
            player.setData(PlayerData.SKELETON, (Object)slots);
        } else if (category == 3) {
            player.setData(PlayerData.NERVOUS_SYSTEM, (Object)slots);
        } else if (category == 4) {
            player.setData(PlayerData.INTEGUMENTARY_SYSTEM, (Object)slots);
        } else if (category == 5) {
            player.setData(PlayerData.LEGS, (Object)slots);
        } else if (category == 6) {
            player.setData(PlayerData.HANDS, (Object)slots);
        } else if (category == 7) {
            player.setData(PlayerData.FACE, (Object)slots);
        }
        CyberwareArmor.update(player);
        ImplantEffects.updateAttributes(player);
        TitaniumBones.reconcile(player);
    }

    public static boolean hasBloodPump(Player player) {
        return InventoryImplants.get(player, 0).slots().stream().anyMatch(i -> i.item().is(CyberwareItems.BLOOD_PUMP));
    }

    public static boolean hasAxolotl(Player player) {
        return InventoryImplants.get(player, 1).slots().stream().anyMatch(i -> i.item().is(CyberwareItems.AXOLOTL));
    }

    public static boolean hasQuantumTuner(Player player) {
        return InventoryImplants.get(player, 1).slots().stream().anyMatch(i -> i.item().is(CyberwareItems.QUANTUM_TUNER));
    }

    public static boolean hasDenseMarrow(Player player) {
        return InventoryImplants.get(player, 2).slots().stream().anyMatch(i -> i.item().is(CyberwareItems.DENSE_MARROW));
    }

    public static boolean hasRaraAvis(Player player) {
        return InventoryImplants.get(player, 2).slots().stream().anyMatch(i -> i.item().is(CyberwareItems.RARA_AVIS));
    }

    public static boolean hasScarab(Player player) {
        return InventoryImplants.get(player, 2).slots().stream().anyMatch(i -> i.item().is(CyberwareItems.SCARAB));
    }

    public static int category(ItemStack held) {
        Implant definition = Implant.of(held);
        if (definition != null) {
            return definition.category.index;
        }
        return held.is(CyberwareItems.BLOOD_PUMP) ? 0 : (held.is(CyberwareItems.DENSE_MARROW) || held.is(CyberwareItems.RARA_AVIS) || held.is(CyberwareItems.SCARAB) ? 2 : 1);
    }

    public static void request(Player player, Integer socket) {
        if (socket == null) {
            REQUESTED.remove(player);
        } else {
            REQUESTED.put(player, socket);
        }
    }

    public static int socket(Player player, ItemStack held) {
        CyberwareSlots slots = InventoryImplants.get(player, InventoryImplants.category(held));
        int count = ImplantCategory.byIndex((int)InventoryImplants.category((ItemStack)held)).sockets;
        for (int i = 0; i < count; ++i) {
            if (!ItemStack.isSameItem((ItemStack)slots.get(i).item(), (ItemStack)held)) continue;
            return i;
        }
        Integer requested = REQUESTED.get(player);
        if (requested != null && requested >= 0 && requested < count) {
            return requested;
        }
        for (int i = 0; i < count; ++i) {
            if (slots.get(i).installed()) continue;
            return i;
        }
        return count == 1 ? 0 : -1;
    }

    public static boolean installHeld(Player player) {
        if (!Installing.canOperate(player)) {
            return false;
        }
        InteractionHand hand = InventoryImplants.supported(player.getMainHandItem()) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack held = player.getItemInHand(hand);
        if (!InventoryImplants.supported(held)) {
            return false;
        }
        int category = InventoryImplants.category(held);
        Implant definition = Implant.of(held);
        if (definition != null && !ImplantEffects.functional(player, definition.category)) {
            return false;
        }
        CyberwareSlots slots = InventoryImplants.get(player, category);
        int index = InventoryImplants.socket(player, held);
        if (index < 0) {
            player.displayClientMessage((Component)Component.translatable((String)"message.chromeware.socket_full"), true);
            return false;
        }
        InstalledImplant next = new InstalledImplant(held);
        InstalledImplant previous = slots.get(index);
        if (!CyberwareCapacity.canInstall(player, held, previous.item())) {
            return false;
        }
        held.shrink(1);
        InventoryImplants.set(player, category, slots.with(index, next));
        if (previous.installed()) {
            Humanity.removed(player);
        }
        Installing.returnItem(player, previous.item());
        player.displayClientMessage((Component)Component.translatable((String)"message.chromeware.internal_installed", (Object[])new Object[]{next.item().getHoverName()}), true);
        return true;
    }

    public static boolean remove(Player player, int category, int index) {
        ImplantCategory group = ImplantCategory.byIndex(category);
        if (!Installing.canOperate(player) || group == null || group == ImplantCategory.OPERATING_SYSTEM || index < 0 || index >= group.sockets) {
            return false;
        }
        CyberwareSlots slots = InventoryImplants.get(player, category);
        InstalledImplant previous = slots.get(index);
        if (!previous.installed()) {
            return false;
        }
        InventoryImplants.set(player, category, slots.with(index, InstalledImplant.EMPTY));
        if (previous.item().is(CyberwareItems.BLOOD_PUMP)) {
            BloodPump.stopHealing(player);
        }
        ImplantEffects.removed(player, Implant.of(previous.item()));
        if (previous.installed()) {
            Humanity.removed(player);
        }
        Installing.returnItem(player, previous.item());
        player.displayClientMessage((Component)Component.translatable((String)"message.chromeware.internal_removed", (Object[])new Object[]{previous.item().getHoverName()}), true);
        return true;
    }
}

