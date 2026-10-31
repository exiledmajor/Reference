/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.zigythebird.playeranim.animation.PlayerAnimResources
 *  com.zigythebird.playeranim.animation.PlayerAnimationController
 *  com.zigythebird.playeranim.animation.PlayerRawAnimationBuilder
 *  com.zigythebird.playeranim.api.PlayerAnimationAccess
 *  com.zigythebird.playeranim.api.PlayerAnimationFactory
 *  com.zigythebird.playeranimcore.animation.layered.IAnimation
 *  com.zigythebird.playeranimcore.animation.layered.modifier.AbstractModifier
 *  com.zigythebird.playeranimcore.animation.layered.modifier.SpeedModifier
 *  com.zigythebird.playeranimcore.api.firstPerson.FirstPersonConfiguration
 *  com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode
 *  com.zigythebird.playeranimcore.enums.PlayState
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 */
package com.chromeware.client.humanity;

import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.animation.PlayerRawAnimationBuilder;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractModifier;
import com.zigythebird.playeranimcore.animation.layered.modifier.SpeedModifier;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class SyringeAnimation
extends PlayerAnimationController {
    private static final ResourceLocation ID = SyringeAnimation.id("syringe");
    private static final FirstPersonConfiguration CONFIGURATION = new FirstPersonConfiguration().setShowRightArm(true).setShowLeftArm(true).setShowRightItem(true).setShowLeftItem(true);
    private boolean other;

    private SyringeAnimation(AbstractClientPlayer player) {
        super(player, (controller, state, setter) -> PlayState.CONTINUE);
        this.setFirstPersonMode(FirstPersonMode.NONE);
        this.setFirstPersonConfiguration(CONFIGURATION);
    }

    public static void register() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ID, 1200, SyringeAnimation::new);
    }

    static SyringeAnimation controller(AbstractClientPlayer player) {
        SyringeAnimation a;
        IAnimation iAnimation = PlayerAnimationAccess.getPlayerAnimationLayer((AbstractClientPlayer)player, (ResourceLocation)ID);
        return iAnimation instanceof SyringeAnimation ? (a = (SyringeAnimation)iAnimation) : null;
    }

    static void play(AbstractClientPlayer player, boolean other, boolean accelerated) {
        SyringeAnimation controller = SyringeAnimation.controller(player);
        ResourceLocation clip = SyringeAnimation.id(other ? "syringe_outself" : "syringe_self");
        if (controller == null || !PlayerAnimResources.hasAnimation((ResourceLocation)clip)) {
            return;
        }
        controller.other = other;
        controller.removeAllModifiers();
        if (accelerated) {
            controller.addModifierLast((AbstractModifier)new SpeedModifier(2.0f));
        }
        controller.triggerAnimation(PlayerRawAnimationBuilder.begin().thenPlay(clip).build());
    }

    static boolean active(AbstractClientPlayer player) {
        SyringeAnimation c = SyringeAnimation.controller(player);
        return c != null && c.isActive();
    }

    static boolean selfInjecting(AbstractClientPlayer player) {
        SyringeAnimation c = SyringeAnimation.controller(player);
        return c != null && c.isActive() && !c.other;
    }

    static float ticks(AbstractClientPlayer player, float partialTick) {
        SyringeAnimation c = SyringeAnimation.controller(player);
        return c == null || !c.isActive() ? 0.0f : c.getAnimationTicks() * c.getAnimationSpeed();
    }

    static float drain(AbstractClientPlayer player) {
        SyringeAnimation c = SyringeAnimation.controller(player);
        if (c == null || !c.isActive()) {
            return 0.0f;
        }
        float tick = c.getAnimationTicks() * c.getAnimationSpeed();
        return Mth.clamp((float)((tick - 35.0f) / 45.0f), (float)0.0f, (float)1.0f);
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)name);
    }
}

