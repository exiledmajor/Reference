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
 *  com.zigythebird.playeranimcore.animation.layered.modifier.AbstractModifier
 *  com.zigythebird.playeranimcore.animation.layered.modifier.MirrorModifier
 *  com.zigythebird.playeranimcore.api.firstPerson.FirstPersonConfiguration
 *  com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode
 *  com.zigythebird.playeranimcore.bones.PlayerAnimBone
 *  com.zigythebird.playeranimcore.easing.EasingType
 *  com.zigythebird.playeranimcore.enums.PlayState
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.neoforge.common.NeoForge
 *  org.joml.Vector3f
 */
package com.chromeware.client.cyberware;

import com.chromeware.client.cyberware.CyberwareRig;
import com.chromeware.client.monowire.MonowireFirstPerson;
import com.chromeware.cyberware.ArmHardware;
import com.chromeware.cyberware.GorillaCombat;
import com.chromeware.cyberware.GorillaState;
import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.animation.PlayerRawAnimationBuilder;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractModifier;
import com.zigythebird.playeranimcore.animation.layered.modifier.MirrorModifier;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.easing.EasingType;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

public abstract class GorillaAnimation
extends PlayerAnimationController {
    private static final ResourceLocation LEFT_ID = GorillaAnimation.id("gorilla_arms");
    private static final ResourceLocation RIGHT_ID = GorillaAnimation.id("gorilla_arms_right");
    private static final String[] PUNCHES = new String[]{"gorilla_jab", "gorilla_hook", "gorilla_uppercut"};
    private static final int DEPLOY_TICKS = 10;
    private static final int RETRACT_TICKS = 8;
    private static final int SMASH_TICKS = 12;
    private static final int[] PUNCH_TICKS = new int[]{7, 8, 9};
    private final HumanoidArm side;
    private boolean deployed;
    private boolean yielding;
    private boolean atRest;
    private boolean charging;
    private int transitionTicks;
    private int retractTicks;
    private int sequence;
    private int otherStrike;
    private int charged;
    private int lastEntityTick = Integer.MIN_VALUE;

    GorillaAnimation(AbstractClientPlayer player, HumanoidArm side) {
        super(player, (controller, state, setter) -> PlayState.CONTINUE);
        this.side = side;
        this.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
        this.setFirstPersonFollowsCamera(false);
        this.setFirstPersonConfiguration(new FirstPersonConfiguration().setShowLeftArm(true).setShowRightArm(true).setShowLeftItem(false).setShowRightItem(false));
        if (side == HumanoidArm.RIGHT) {
            MirrorModifier mirror = new MirrorModifier();
            mirror.enabled = true;
            this.addModifierLast((AbstractModifier)mirror);
        }
    }

    public void registerBones() {
        super.registerBones();
        this.registerPlayerAnimBone("gorilla_camera");
    }

    public static void register() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(LEFT_ID, 1002, Left::new);
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(RIGHT_ID, 1005, Right::new);
        NeoForge.EVENT_BUS.addListener(event -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && !mc.isPaused()) {
                for (AbstractClientPlayer player : mc.level.players()) {
                    GorillaAnimation.update(player);
                }
            }
        });
    }

    private static GorillaAnimation controller(AbstractClientPlayer player, HumanoidArm side) {
        GorillaAnimation a;
        IAnimation layer = PlayerAnimationAccess.getPlayerAnimationLayer((AbstractClientPlayer)player, (ResourceLocation)(side == HumanoidArm.LEFT ? LEFT_ID : RIGHT_ID));
        return layer instanceof GorillaAnimation ? (a = (GorillaAnimation)layer) : null;
    }

    public static void update(AbstractClientPlayer player) {
        for (HumanoidArm side : HumanoidArm.values()) {
            GorillaAnimation c = GorillaAnimation.controller(player, side);
            if (c == null || c.lastEntityTick == player.tickCount) continue;
            c.lastEntityTick = player.tickCount;
            c.updateState();
        }
    }

    public static boolean rendering(AbstractClientPlayer player) {
        return GorillaAnimation.rendering(player, HumanoidArm.LEFT) || GorillaAnimation.rendering(player, HumanoidArm.RIGHT);
    }

    public static boolean rendering(AbstractClientPlayer player, HumanoidArm side) {
        GorillaAnimation c = GorillaAnimation.controller(player, side);
        return c != null && c.isActive();
    }

    public static void cameraArm(AbstractClientPlayer player, HumanoidArm side, ModelPart arm) {
        GorillaAnimation c = GorillaAnimation.controller(player, side);
        if (c == null || !c.isActive()) {
            return;
        }
        PlayerAnimBone b = c.get3DTransformRaw(new PlayerAnimBone("gorilla_camera"));
        float mirror = side == HumanoidArm.LEFT ? 1.0f : -1.0f;
        MonowireFirstPerson.cameraArm(arm, new CyberwareRig.Transform(new Vector3f(mirror * b.positionX / 16.0f, -b.positionY / 16.0f, b.positionZ / 16.0f), new Vector3f(b.rotX, mirror * b.rotY, mirror * b.rotZ), new Vector3f(1.0f)));
    }

    private void updateState() {
        if (this.transitionTicks > 0) {
            --this.transitionTicks;
        }
        if (this.retractTicks > 0) {
            --this.retractTicks;
        }
        GorillaState state = GorillaCombat.state((Player)this.player);
        int nextSequence = state.sequence(this.side);
        if (!GorillaCombat.functional((Player)this.player, this.side)) {
            this.stop();
            this.deployed = false;
            this.retractTicks = 0;
            this.transitionTicks = 0;
            this.sequence = nextSequence;
            return;
        }
        if (!PlayerAnimResources.hasAnimation((ResourceLocation)GorillaAnimation.id("gorilla_guard"))) {
            return;
        }
        boolean next = GorillaCombat.deployed((Player)this.player, this.side);
        HumanoidArm other = this.side.getOpposite();
        boolean otherStriking = ArmHardware.striking((Player)this.player, other);
        boolean charge = next && state.charging(this.side);
        int n = this.charged = charge ? this.charged + 1 : 0;
        if (next != this.deployed) {
            this.play(GorillaAnimation.hold(next ? "gorilla_deploy" : "gorilla_retract"));
            this.transitionTicks = next ? 10 : 8;
            this.retractTicks = next ? 0 : 8;
            this.yielding = false;
            this.atRest = false;
        } else if (next && nextSequence != this.sequence) {
            int punch = Math.floorMod(nextSequence - 1, PUNCHES.length);
            if (state.smash(this.side)) {
                this.play(GorillaAnimation.hold("gorilla_smash"));
                this.transitionTicks = 12;
            } else {
                this.play(GorillaAnimation.hold(PUNCHES[punch]));
                this.transitionTicks = PUNCH_TICKS[punch];
            }
            this.yielding = false;
            this.atRest = false;
        } else if (charge && !this.charging) {
            this.play(GorillaAnimation.hold("gorilla_charge"));
            this.transitionTicks = 0;
            this.yielding = false;
            this.atRest = false;
        } else if (charge && this.charged == 10) {
            this.play(PlayerRawAnimationBuilder.begin().thenLoop(GorillaAnimation.id("gorilla_charged")).build());
        } else if (next && !charge && this.transitionTicks == 0 && otherStriking && (!this.yielding || ArmHardware.strike((Player)this.player, other) != this.otherStrike)) {
            this.play(GorillaAnimation.hold("gorilla_yield"));
            this.yielding = true;
            this.atRest = false;
        } else if (next && !charge && this.transitionTicks == 0 && !otherStriking && !this.atRest) {
            this.play(PlayerRawAnimationBuilder.begin().thenLoop(GorillaAnimation.id("gorilla_guard")).build());
            this.yielding = false;
            this.atRest = true;
        }
        if (!next && this.retractTicks == 0) {
            this.stop();
        }
        this.deployed = next;
        this.sequence = nextSequence;
        this.otherStrike = ArmHardware.strike((Player)this.player, other);
        this.charging = charge;
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)name);
    }

    private void play(RawAnimation clip) {
        if (this.isActive() && !this.atRest) {
            this.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn((int)2, (EasingType)EasingType.LINEAR), clip, false);
        } else if (this.isActive()) {
            this.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn((int)1, (EasingType)EasingType.LINEAR), clip, false);
        } else {
            this.triggerAnimation(clip);
        }
    }

    private static RawAnimation hold(String once) {
        return PlayerRawAnimationBuilder.begin().thenPlayAndHold(GorillaAnimation.id(once)).build();
    }

    public static final class Right
    extends GorillaAnimation {
        Right(AbstractClientPlayer p) {
            super(p, HumanoidArm.RIGHT);
        }
    }

    public static final class Left
    extends GorillaAnimation {
        Left(AbstractClientPlayer p) {
            super(p, HumanoidArm.LEFT);
        }
    }
}

