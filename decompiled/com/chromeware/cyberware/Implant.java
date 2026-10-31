/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package com.chromeware.cyberware;

import com.chromeware.compat.guns.GunCyberware;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.ImplantCategory;
import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.cyberware.InventoryImplants;
import com.chromeware.cyberware.SandevistanImplant;
import java.util.Locale;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public enum Implant {
    NEWTON_MODULE(ImplantCategory.FRONTAL_CORTEX, 14, 0),
    BIONIC_JOINTS(ImplantCategory.SKELETON, 8, 130),
    EPIMORPHIC_SKELETON(ImplantCategory.SKELETON, 40, 186),
    KINETIC_FRAME(ImplantCategory.SKELETON, 16, 117),
    PARA_BELLUM(ImplantCategory.SKELETON, 25, 150),
    SCAR_COALESCER(ImplantCategory.SKELETON, 20, 46),
    SPRING_JOINTS(ImplantCategory.SKELETON, 16, 46),
    UNIVERSAL_BOOSTER(ImplantCategory.SKELETON, 25, 186),
    ADRENALINE_CONVERTER(ImplantCategory.NERVOUS_SYSTEM, 6, 0),
    ADRENO_TRIGGER(ImplantCategory.NERVOUS_SYSTEM, 20, 0),
    NEOFIBER(ImplantCategory.NERVOUS_SYSTEM, 14, 0),
    STABBER(ImplantCategory.NERVOUS_SYSTEM, 12, 0),
    VISUAL_CORTEX_SUPPORT(ImplantCategory.NERVOUS_SYSTEM, 20, 0),
    DEEP_FIELD_VISUAL_INTERFACE(ImplantCategory.NERVOUS_SYSTEM, 40, 0),
    REFLEX_TUNER(ImplantCategory.NERVOUS_SYSTEM, 5, 0),
    REVULSOR(ImplantCategory.NERVOUS_SYSTEM, 35, 0),
    ADRENALINE_BOOSTER(ImplantCategory.CIRCULATORY, 14, 0),
    BIOMONITOR(ImplantCategory.CIRCULATORY, 14, 0),
    BLACK_MAMBA(ImplantCategory.CIRCULATORY, 16, 0),
    HEAL_ON_KILL(ImplantCategory.CIRCULATORY, 10, 0),
    ISOMETRIC_STABILIZER(ImplantCategory.CIRCULATORY, 40, 0),
    MICROROTORS(ImplantCategory.CIRCULATORY, 12, 0),
    SECOND_HEART(ImplantCategory.CIRCULATORY, 30, 0),
    THREAT_EVAC(ImplantCategory.CIRCULATORY, 5, 0),
    CARAPACE(ImplantCategory.INTEGUMENTARY_SYSTEM, 16, 108),
    COUNTERSHELL(ImplantCategory.INTEGUMENTARY_SYSTEM, 12, 77),
    PAIN_EDITOR(ImplantCategory.INTEGUMENTARY_SYSTEM, 35, 148),
    PAINDUCER(ImplantCategory.INTEGUMENTARY_SYSTEM, 30, 186),
    PERIPHERAL_INVERSE(ImplantCategory.INTEGUMENTARY_SYSTEM, 30, 60),
    PROXISHIELD(ImplantCategory.INTEGUMENTARY_SYSTEM, 10, 38),
    RANGEGUARD(ImplantCategory.INTEGUMENTARY_SYSTEM, 14, 46),
    SHOCK_N_AWE(ImplantCategory.INTEGUMENTARY_SYSTEM, 25, 124),
    SUBDERMAL_ARMOR(ImplantCategory.INTEGUMENTARY_SYSTEM, 5, 55),
    OPTICAL_CAMO(ImplantCategory.INTEGUMENTARY_SYSTEM, 20, 40),
    CHITIN(ImplantCategory.INTEGUMENTARY_SYSTEM, 50, 232),
    JENKINS_TENDONS(ImplantCategory.LEGS, 6, 62),
    LEEROY_LIGAMENT_SYSTEM(ImplantCategory.LEGS, 8, 34),
    LYNX_PAWS(ImplantCategory.LEGS, 5, 46),
    FORTIFIED_ANKLES(ImplantCategory.LEGS, 6, 103),
    REINFORCED_TENDONS(ImplantCategory.LEGS, 8, 12),
    HANDLE_WRAP(ImplantCategory.HANDS, 8, 0),
    SHOCK_ABSORBER(ImplantCategory.HANDS, 12, 0),
    IMMOVABLE_FORCE(ImplantCategory.HANDS, 35, 0),
    MICROGENERATOR(ImplantCategory.HANDS, 9, 0),
    CLUTCH_PADDING(ImplantCategory.CIRCULATORY, 12, 0),
    FEEDBACK_CIRCUIT(ImplantCategory.CIRCULATORY, 14, 0),
    ELECTROMAG_RECYCLER(ImplantCategory.CIRCULATORY, 40, 0),
    STALKER(ImplantCategory.FACE, 2, 0),
    SENTRY(ImplantCategory.FACE, 2, 0),
    THE_ORACLE(ImplantCategory.FACE, 10, 0),
    CLAIRVOYANT(ImplantCategory.FACE, 8, 0),
    COCKATRICE(ImplantCategory.FACE, 30, 0),
    DOOMSAYER(ImplantCategory.FACE, 2, 0),
    CHROME_COMPRESSOR(ImplantCategory.OPERATING_SYSTEM, 0, 0),
    ZETATECH_BERSERK(ImplantCategory.OPERATING_SYSTEM, 35, 0),
    TYROSINE_INJECTOR(ImplantCategory.NERVOUS_SYSTEM, 8, 0),
    ATOMIC_SENSORS(ImplantCategory.NERVOUS_SYSTEM, 5, 0),
    TITANIUM_BONES(ImplantCategory.SKELETON, 6, 46),
    BIOCONDUCTOR(ImplantCategory.FRONTAL_CORTEX, 16, 0),
    EX_DISK(ImplantCategory.FRONTAL_CORTEX, 10, 0),
    CAMILLO_RAM_MANAGER(ImplantCategory.FRONTAL_CORTEX, 8, 0),
    COX_2_CYBERSOMATIC_OPTIMIZER(ImplantCategory.FRONTAL_CORTEX, 50, 0),
    MECHATRONIC_CORE(ImplantCategory.FRONTAL_CORTEX, 5, 0),
    MEMORY_BOOST(ImplantCategory.FRONTAL_CORTEX, 18, 0),
    RAM_REALLOCATOR(ImplantCategory.FRONTAL_CORTEX, 40, 0),
    RAM_UPGRADE(ImplantCategory.FRONTAL_CORTEX, 8, 0),
    SELF_ICE(ImplantCategory.FRONTAL_CORTEX, 5, 0),
    FEEN_X(ImplantCategory.SKELETON, 16, 38),
    RAM_RECOUP(ImplantCategory.SKELETON, 14, 46),
    COGITO_LATTICE(ImplantCategory.INTEGUMENTARY_SYSTEM, 12, 62),
    KIROSHI_OPTICS(ImplantCategory.FACE, 2, 0);

    public final ImplantCategory category;
    public final int capacity;
    public final int cyberpunkArmor;
    public final String id;

    private Implant(ImplantCategory category, int capacity, int armor) {
        this.category = category;
        this.capacity = capacity;
        this.cyberpunkArmor = armor;
        this.id = this.name().toLowerCase(Locale.ROOT);
    }

    public ItemStack stack() {
        return CyberwareItems.CATALOGUE.get((Object)this).toStack();
    }

    public boolean installed(Player player) {
        if (this.category == ImplantCategory.OPERATING_SYSTEM) {
            return SandevistanImplant.get(player).item().is((Holder)CyberwareItems.CATALOGUE.get((Object)this));
        }
        return InventoryImplants.get(player, this.category.index).slots().stream().anyMatch(slot -> this.matches(slot.item()));
    }

    public boolean matches(ItemStack stack) {
        return stack.is((Holder)CyberwareItems.CATALOGUE.get((Object)this));
    }

    public boolean available() {
        return this != SHOCK_ABSORBER && this != IMMOVABLE_FORCE && this != MICROGENERATOR || GunCyberware.available();
    }

    public boolean active(Player player) {
        return this.available() && this.installed(player) && ImplantEffects.functional(player, this.category);
    }

    public static Implant of(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        ResourceLocation key = BuiltInRegistries.ITEM.getKey((Object)stack.getItem());
        if (!key.getNamespace().equals("chromeware")) {
            return null;
        }
        for (Implant implant : Implant.values()) {
            if (!implant.id.equals(key.getPath())) continue;
            return implant;
        }
        return null;
    }
}

