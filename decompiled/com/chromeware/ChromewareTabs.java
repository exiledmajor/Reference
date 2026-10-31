/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.AllItems
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.level.ItemLike
 *  net.neoforged.neoforge.registries.DeferredHolder
 *  net.neoforged.neoforge.registries.DeferredRegister
 */
package com.chromeware;

import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.humanity.ImmunosuppressantFluids;
import com.chromeware.surgery.SurgeryBlocks;
import com.simibubi.create.AllItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ChromewareTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create((ResourceKey)Registries.CREATIVE_MODE_TAB, (String)"chromeware");
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> IMPLANTS = TABS.register("implants", () -> CreativeModeTab.builder().title((Component)Component.translatable((String)"itemGroup.chromeware.implants")).icon(() -> CyberwareItems.BLOOD_PUMP.toStack()).displayItems((parameters, output) -> {
        output.accept((ItemLike)SurgeryBlocks.CHAIR_ITEM.get());
        CyberwareItems.ITEMS.getEntries().forEach(item -> output.accept((ItemLike)item.get()));
        output.accept((ItemLike)AllItems.EXTENDO_GRIP.get());
        ImmunosuppressantFluids.ITEMS.getEntries().forEach(item -> output.accept((ItemLike)item.get()));
    }).build());
}

