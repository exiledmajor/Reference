/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.zigythebird.playeranim.animation.PlayerAnimResources
 *  com.zigythebird.playeranim.animation.PlayerAnimationController
 *  com.zigythebird.playeranim.animation.PlayerRawAnimationBuilder
 *  com.zigythebird.playeranim.api.PlayerAnimationAccess
 *  com.zigythebird.playeranim.api.PlayerAnimationFactory
 *  com.zigythebird.playeranimcore.animation.RawAnimation
 *  com.zigythebird.playeranimcore.animation.layered.IAnimation
 *  com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier
 *  com.zigythebird.playeranimcore.api.firstPerson.FirstPersonConfiguration
 *  com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode
 *  com.zigythebird.playeranimcore.easing.EasingType
 *  com.zigythebird.playeranimcore.enums.PlayState
 *  java.lang.MatchException
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 */
package com.chromeware.client.surgery;

import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbAttachments;
import com.chromeware.surgery.SurgerySeat;
import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.animation.PlayerRawAnimationBuilder;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranimcore.easing.EasingType;
import com.zigythebird.playeranimcore.enums.PlayState;
import java.util.Locale;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class SurgeryAnimation
extends PlayerAnimationController {
    public static final String IMPLANT = "surgery_implant";
    private static final ResourceLocation ID = SurgeryAnimation.id("surgery");
    private String clip = "";
    private int lastEntityTick = Integer.MIN_VALUE;

    private SurgeryAnimation(AbstractClientPlayer player) {
        super(player, (controller, state, setter) -> PlayState.CONTINUE);
        this.setFirstPersonMode(FirstPersonMode.NONE);
        this.setFirstPersonConfiguration(new FirstPersonConfiguration());
    }

    public void registerBones() {
        super.registerBones();
        for (String bone : new String[]{IMPLANT}) {
            this.registerPlayerAnimBone(bone);
        }
    }

    public static void register() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ID, 1100, SurgeryAnimation::new);
    }

    static SurgeryAnimation controller(AbstractClientPlayer player) {
        SurgeryAnimation a;
        IAnimation iAnimation = PlayerAnimationAccess.getPlayerAnimationLayer((AbstractClientPlayer)player, (ResourceLocation)ID);
        return iAnimation instanceof SurgeryAnimation ? (a = (SurgeryAnimation)iAnimation) : null;
    }

    public static void update(AbstractClientPlayer player) {
        SurgeryAnimation c = SurgeryAnimation.controller(player);
        if (c != null && c.lastEntityTick != player.tickCount) {
            c.lastEntityTick = player.tickCount;
            c.updateState();
        }
    }

    private void updateState() {
        RawAnimation raw;
        String next;
        Entity entity;
        if (!LimbAttachments.get((Player)this.player).isMissing(BodyPart.BODY) && (entity = this.player.getVehicle()) instanceof SurgerySeat) {
            SurgerySeat seat = (SurgerySeat)entity;
            v0 = SurgeryAnimation.clip(seat);
        } else {
            v0 = next = "";
        }
        if (next.equals(this.clip)) {
            return;
        }
        this.clip = next;
        if (next.isEmpty() || !PlayerAnimResources.hasAnimation((ResourceLocation)SurgeryAnimation.id(next))) {
            this.stop();
            return;
        }
        RawAnimation rawAnimation = raw = next.equals("surgery_sit") ? PlayerRawAnimationBuilder.begin().thenLoop(SurgeryAnimation.id(next)).build() : PlayerRawAnimationBuilder.begin().thenPlayAndHold(SurgeryAnimation.id(next)).build();
        if (this.isActive()) {
            this.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn((int)3, (EasingType)EasingType.LINEAR), raw, false);
        } else {
            this.triggerAnimation(raw);
        }
    }

    static String clip(SurgerySeat seat) {
        String kind = seat.kind().name().toLowerCase(Locale.ROOT);
        if (seat.kind() == SurgerySeat.Kind.STANDARD) {
            return "surgery_sit";
        }
        return switch (seat.stage()) {
            default -> throw new MatchException(null, null);
            case SurgerySeat.Stage.IDLE -> "surgery_sit";
            case SurgerySeat.Stage.RECLINE -> "surgery_recline_" + kind;
            case SurgerySeat.Stage.WORK, SurgerySeat.Stage.PRESENT -> (seat.removing() ? "surgery_extract_" : "surgery_install_") + kind;
            case SurgerySeat.Stage.CLOSE -> "surgery_close_" + kind;
            case SurgerySeat.Stage.RAISE -> "surgery_raise_" + kind;
        };
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)name);
    }
}

