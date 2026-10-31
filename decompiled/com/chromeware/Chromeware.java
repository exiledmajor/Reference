/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.simibubi.create.content.equipment.goggles.GogglesItem
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.fml.ModContainer
 *  net.neoforged.fml.ModList
 *  net.neoforged.fml.common.Mod
 *  net.neoforged.neoforge.common.NeoForge
 *  org.slf4j.Logger
 */
package com.chromeware;

import com.chromeware.ChromewareTabs;
import com.chromeware.body.CyberLimbs;
import com.chromeware.body.DetachedLimb;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbCommands;
import com.chromeware.compat.guns.GunComponents;
import com.chromeware.compat.guns.GunCyberware;
import com.chromeware.compat.guns.SuperbIntegration;
import com.chromeware.config.ChromewareConfig;
import com.chromeware.config.ConfigSync;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.cyberware.ImplantPackets;
import com.chromeware.cyberware.PainducerPackets;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.humanity.Humanity;
import com.chromeware.humanity.HumanityCommands;
import com.chromeware.humanity.HumanityPackets;
import com.chromeware.humanity.HumanitySounds;
import com.chromeware.humanity.ImmunosuppressantFluids;
import com.chromeware.humanity.SyringeInjection;
import com.chromeware.launcher.LauncherEntities;
import com.chromeware.mantis.MantisPackets;
import com.chromeware.netrunning.NetrunningPackets;
import com.chromeware.netrunning.NetrunningSounds;
import com.chromeware.sandevistan.SandevistanPackets;
import com.chromeware.sandevistan.SandevistanSounds;
import com.chromeware.surgery.SurgeryBlocks;
import com.chromeware.surgery.SurgeryPackets;
import com.mojang.logging.LogUtils;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(value="chromeware")
public final class Chromeware {
    public static final String MOD_ID = "chromeware";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Chromeware(IEventBus modEventBus, ModContainer container) {
        ChromewareConfig.register(container);
        GunComponents.TYPES.register(modEventBus);
        if (ModList.get().isLoaded("superbwarfare")) {
            SuperbIntegration.register();
        }
        if (ModList.get().isLoaded("scguns")) {
            GunCyberware.Scorched.register();
        }
        LimbAttachments.TYPES.register(modEventBus);
        DetachedLimb.ENTITIES.register(modEventBus);
        CyberLimbs.TYPES.register(modEventBus);
        PlayerData.TYPES.register(modEventBus);
        Humanity.TYPES.register(modEventBus);
        SyringeInjection.COMPONENTS.register(modEventBus);
        ImmunosuppressantFluids.register(modEventBus);
        CyberwareItems.ITEMS.register(modEventBus);
        SurgeryBlocks.register(modEventBus);
        GogglesItem.addIsWearingPredicate(ImplantEffects::scanning);
        LauncherEntities.register(modEventBus);
        ChromewareTabs.TABS.register(modEventBus);
        modEventBus.addListener(ImplantPackets::register);
        modEventBus.addListener(PainducerPackets::register);
        modEventBus.addListener(SurgeryPackets::register);
        modEventBus.addListener(NetrunningPackets::register);
        modEventBus.addListener(SandevistanPackets::register);
        modEventBus.addListener(MantisPackets::register);
        modEventBus.addListener(HumanityPackets::register);
        modEventBus.addListener(ConfigSync::register);
        SandevistanSounds.SOUNDS.register(modEventBus);
        HumanitySounds.SOUNDS.register(modEventBus);
        NetrunningSounds.SOUNDS.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(LimbCommands::register);
        NeoForge.EVENT_BUS.addListener(HumanityCommands::register);
        LOGGER.info("Create: Chromeware is initializing");
    }
}

