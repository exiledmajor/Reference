/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package com.chromeware.client.cyberware;

import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.Implant;
import com.chromeware.extendo.ExtendoGrip;
import com.chromeware.netrunning.QuickhackItem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public final class ImplantCreativeSections {
    private static final List<String> ORDER = List.of("frontal_cortex", "operating_system", "arms", "face", "skeleton", "hands", "nervous_system", "circulatory_system", "integumentary_system", "legs", "quickhacks", "misc");

    public static String category(ItemStack stack) {
        Implant definition = Implant.of(stack);
        if (definition != null) {
            return definition.category.key;
        }
        if (stack.getItem() instanceof QuickhackItem) {
            return "quickhacks";
        }
        if (stack.is(CyberwareItems.AXOLOTL) || stack.is(CyberwareItems.QUANTUM_TUNER)) {
            return "frontal_cortex";
        }
        if (stack.is(CyberwareItems.SANDEVISTAN) || stack.is(CyberwareItems.CYBERDECK)) {
            return "operating_system";
        }
        if (stack.is(CyberwareItems.DENSE_MARROW) || stack.is(CyberwareItems.RARA_AVIS) || stack.is(CyberwareItems.SCARAB)) {
            return "skeleton";
        }
        if (stack.is(CyberwareItems.BLOOD_PUMP)) {
            return "circulatory_system";
        }
        if (stack.is(CyberwareItems.GORILLA_ARMS) || stack.is(CyberwareItems.MANTIS_BLADES) || stack.is(CyberwareItems.MONOWIRE) || stack.is(CyberwareItems.PROJECTILE_LAUNCH_SYSTEM) || ExtendoGrip.is(stack)) {
            return "arms";
        }
        return "misc";
    }

    public static Layout layout(Collection<ItemStack> source) {
        HashMap<String, List> groups = new HashMap<String, List>();
        for (ItemStack stack : source) {
            if (stack.isEmpty()) continue;
            groups.computeIfAbsent(ImplantCreativeSections.category(stack), key -> new ArrayList()).add(stack);
        }
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        ArrayList<Heading> headings = new ArrayList<Heading>();
        for (String key2 : ORDER) {
            List group = (List)groups.get(key2);
            if (group == null || group.isEmpty()) continue;
            headings.add(new Heading(items.size() / 9, key2));
            for (int i = 0; i < 9; ++i) {
                items.add(ItemStack.EMPTY);
            }
            items.addAll(group);
            while (items.size() % 9 != 0) {
                items.add(ItemStack.EMPTY);
            }
        }
        return new Layout(List.copyOf(items), List.copyOf(headings));
    }

    public record Heading(int row, String category) {
    }

    public record Layout(List<ItemStack> items, List<Heading> headings) {
    }
}

