/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.advancements.AdvancementHolder
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.ServerAdvancementManager
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.player.AdvancementEvent$AdvancementEarnEvent
 *  net.neoforged.neoforge.event.entity.player.AdvancementEvent$AdvancementProgressEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerRespawnEvent
 */
package com.chromeware.cyberware;

import com.chromeware.cyberware.PlayerData;
import java.util.List;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid="chromeware")
public final class CapacityProgress {
    public static final int PER_ADVANCEMENT = 4;
    public static final List<String> CREATE_TASKS = List.of("andesite_alloy", "andesite_casing", "water_wheel", "mechanical_press", "windmill", "encased_fan", "shifting_gears", "fan_processing", "millstone", "saw_processing", "super_glue", "compacting", "contraption_actors", "belt", "portable_storage_interface", "funnel", "wrench_goggles", "chute", "stressometer", "cuckoo_clock", "mechanical_mixer", "burner", "cardboard", "copper", "packager", "copper_casing", "mechanical_pump_0", "stock_ticker", "frogport", "glass_pipe", "spout", "drain", "table_cloth_shop", "water_supply", "factory_gauge", "hose_pulley", "steam_engine", "cardboard_armor", "chocolate_bucket", "steam_whistle", "backtank", "honey_drain", "diving_suit", "brass", "brass_casing", "haunted_bell", "clockwork_bearing", "rose_quartz", "deployer", "display_link", "potato_cannon", "precision_mechanism", "extendo_grip", "speed_controller", "linked_controller", "mechanical_arm", "arm_blaze_burner", "mechanical_crafter", "crushing_wheel", "sturdy_sheet", "track_0", "train_casing_00", "train", "train_whistle", "conductor", "train_portal", "track_signal", "display_board_0");
    public static final int MAX_BONUS = CREATE_TASKS.size() * 4;

    public static int bonus(Player p) {
        return (Integer)p.getData(PlayerData.CAPACITY_BONUS);
    }

    public static int earned(ServerPlayer p) {
        return CapacityProgress.count(p, CREATE_TASKS);
    }

    private static int count(ServerPlayer p, List<String> ids) {
        ServerAdvancementManager advancements = p.server.getAdvancements();
        int done = 0;
        for (String id : ids) {
            AdvancementHolder holder = advancements.get(ResourceLocation.fromNamespaceAndPath((String)"create", (String)id));
            if (holder == null || !p.getAdvancements().getOrStartProgress(holder).isDone()) continue;
            ++done;
        }
        return done;
    }

    public static void refresh(ServerPlayer p) {
        int bonus = CapacityProgress.earned(p) * 4;
        if (bonus != CapacityProgress.bonus((Player)p)) {
            p.setData(PlayerData.CAPACITY_BONUS, (Object)bonus);
        }
    }

    private static boolean counts(ResourceLocation id) {
        return id.getNamespace().equals("create") && CREATE_TASKS.contains(id.getPath());
    }

    @SubscribeEvent
    public static void earned(AdvancementEvent.AdvancementEarnEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer p = (ServerPlayer)player;
            if (CapacityProgress.counts(event.getAdvancement().id())) {
                CapacityProgress.refresh(p);
            }
        }
    }

    @SubscribeEvent
    public static void progressed(AdvancementEvent.AdvancementProgressEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer p = (ServerPlayer)player;
            if (CapacityProgress.counts(event.getAdvancement().id())) {
                CapacityProgress.refresh(p);
            }
        }
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer p = (ServerPlayer)player;
            CapacityProgress.refresh(p);
        }
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer p = (ServerPlayer)player;
            CapacityProgress.refresh(p);
        }
    }
}

