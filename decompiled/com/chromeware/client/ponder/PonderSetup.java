/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.createmod.ponder.api.registration.PonderPlugin
 *  net.createmod.ponder.api.registration.PonderSceneRegistrationHelper
 *  net.minecraft.resources.ResourceLocation
 */
package com.chromeware.client.ponder;

import com.chromeware.client.ponder.SurgeryScenes;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public final class PonderSetup
implements PonderPlugin {
    public String getModId() {
        return "chromeware";
    }

    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents((Object[])new ResourceLocation[]{ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"surgery_chair")}).addStoryBoard("surgery/clinic", SurgeryScenes::installation);
    }
}

