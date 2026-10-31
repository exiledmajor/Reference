/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Rarity
 *  net.neoforged.neoforge.registries.DeferredItem
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.DeferredRegister$Items
 */
package com.chromeware.cyberware;

import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.InternalImplantItem;
import com.chromeware.humanity.EmptySyringeItem;
import com.chromeware.humanity.ImmunosuppressantItem;
import com.chromeware.humanity.Suppression;
import com.chromeware.netrunning.Quickhack;
import com.chromeware.netrunning.QuickhackItem;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CyberwareItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems((String)"chromeware");
    public static final DeferredItem<Item> CYBERDECK = ITEMS.register("cyberdeck", () -> new InternalImplantItem("cyberdeck"));
    public static final Map<Quickhack, DeferredItem<Item>> QUICKHACKS = CyberwareItems.registerQuickhacks();
    public static final DeferredItem<Item> EMPTY_SYRINGE = ITEMS.register("empty_syringe", () -> new EmptySyringeItem(new Item.Properties().stacksTo(16).rarity(Rarity.COMMON)));
    public static final DeferredItem<Item> STANDARD_IMMUNOSUPPRESSANT = ITEMS.register("standard_grade_immunosuppressant", () -> new ImmunosuppressantItem(Suppression.Grade.STANDARD, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> MILITARY_IMMUNOSUPPRESSANT = ITEMS.register("military_grade_immunosuppressant", () -> new ImmunosuppressantItem(Suppression.Grade.MILITARY, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> SANDEVISTAN = ITEMS.register("sandevistan", () -> new InternalImplantItem("sandevistan"));
    public static final DeferredItem<Item> GORILLA_ARMS = ITEMS.registerSimpleItem("gorilla_arms", new Item.Properties().stacksTo(2));
    public static final DeferredItem<Item> MONOWIRE = ITEMS.registerSimpleItem("monowire", new Item.Properties().stacksTo(2));
    public static final DeferredItem<Item> PROJECTILE_LAUNCH_SYSTEM = ITEMS.registerSimpleItem("projectile_launch_system", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> MANTIS_BLADES = ITEMS.registerSimpleItem("mantis_blades", new Item.Properties().stacksTo(2));
    public static final DeferredItem<Item> CYBER_ARM = ITEMS.registerSimpleItem("cyber_arm", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> CYBER_LEG = ITEMS.registerSimpleItem("cyber_leg", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> CYBER_TORSO = ITEMS.registerSimpleItem("cyber_torso", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> BLOOD_PUMP = ITEMS.register("blood_pump", () -> new InternalImplantItem("blood_pump"));
    public static final DeferredItem<Item> AXOLOTL = ITEMS.register("axolotl", () -> new InternalImplantItem("axolotl"));
    public static final DeferredItem<Item> QUANTUM_TUNER = ITEMS.register("quantum_tuner", () -> new InternalImplantItem("quantum_tuner"));
    public static final DeferredItem<Item> DENSE_MARROW = ITEMS.register("dense_marrow", () -> new InternalImplantItem("dense_marrow"));
    public static final DeferredItem<Item> RARA_AVIS = ITEMS.register("rara_avis", () -> new InternalImplantItem("rara_avis"));
    public static final DeferredItem<Item> SCARAB = ITEMS.register("scarab", () -> new InternalImplantItem("scarab"));
    public static final Map<Implant, DeferredItem<Item>> CATALOGUE = CyberwareItems.registerCatalogue();

    private static Map<Quickhack, DeferredItem<Item>> registerQuickhacks() {
        EnumMap<Quickhack, DeferredItem> result = new EnumMap<Quickhack, DeferredItem>(Quickhack.class);
        for (Quickhack hack : Quickhack.values()) {
            result.put(hack, ITEMS.register("quickhack_" + hack.id, () -> new QuickhackItem(hack)));
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<Implant, DeferredItem<Item>> registerCatalogue() {
        EnumMap<Implant, DeferredItem> items = new EnumMap<Implant, DeferredItem>(Implant.class);
        for (Implant implant : Implant.values()) {
            items.put(implant, ITEMS.register(implant.id, () -> new InternalImplantItem(implant.id)));
        }
        return Collections.unmodifiableMap(items);
    }
}

