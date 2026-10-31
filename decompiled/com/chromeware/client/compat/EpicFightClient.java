/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  yesman.epicfight.api.client.event.EpicFightClientEventHooks$Render
 *  yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch
 *  yesman.epicfight.config.ClientConfig
 *  yesman.epicfight.world.capabilities.EpicFightCapabilities
 */
package com.chromeware.client.compat;

import com.chromeware.client.compat.CompatHands;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

public final class EpicFightClient {
    public static void register() {
        EpicFightClientEventHooks.Render.VALIDATE_PLAYER_MODEL_TO_RENDER.registerEvent(event -> {
            if (event.getShouldRender() && CompatHands.needsVanillaModel((Player)event.getPlayerPatch().getOriginal())) {
                event.setShouldRender(false);
            }
        });
    }

    public static boolean ownsFirstPersonHands() {
        LocalPlayerPatch patch = EpicFightCapabilities.getCachedLocalPlayerPatch();
        return patch != null && patch.isEpicFightMode() && ClientConfig.enableAnimatedFirstPersonModel;
    }
}

