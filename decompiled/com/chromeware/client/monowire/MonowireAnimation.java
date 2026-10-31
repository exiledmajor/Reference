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
 *  com.zigythebird.playeranimcore.enums.State
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  org.joml.Vector3f
 */
package com.chromeware.client.monowire;

import com.chromeware.client.cyberware.CyberwareRig;
import com.chromeware.client.cyberware.PreviewDeploy;
import com.chromeware.client.sandevistan.AfterimagePlayerEntity;
import com.chromeware.client.sandevistan.FrozenPlayerAnimation;
import com.chromeware.cyberware.ArmHardware;
import com.chromeware.monowire.MonowireCombat;
import com.chromeware.monowire.MonowireImplant;
import com.chromeware.monowire.MonowirePhysics;
import com.chromeware.monowire.MonowireState;
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
import com.zigythebird.playeranimcore.enums.State;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

public abstract class MonowireAnimation
extends PlayerAnimationController {
    private static final ResourceLocation RIGHT_ID = MonowireAnimation.id("monowire");
    private static final ResourceLocation LEFT_ID = MonowireAnimation.id("monowire_left");
    private final HumanoidArm side;
    private boolean deployed;
    private boolean held;
    private boolean atRest;
    private boolean yielding;
    private int stroke;
    private int action;
    private int otherStrike;
    private int drawTicks;
    private int slashTicks;
    private int retractTicks;
    private int lastEntityTick = Integer.MIN_VALUE;

    MonowireAnimation(AbstractClientPlayer player, HumanoidArm side) {
        super(player, (controller, state, setter) -> PlayState.CONTINUE);
        this.side = side;
        this.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
        this.setFirstPersonFollowsCamera(false);
        this.setFirstPersonConfiguration(new FirstPersonConfiguration().setShowRightArm(true).setShowLeftArm(true).setShowRightItem(false).setShowLeftItem(false));
        if (side == HumanoidArm.LEFT) {
            MirrorModifier mirror = new MirrorModifier();
            mirror.enabled = true;
            this.addModifierLast((AbstractModifier)mirror);
        }
    }

    public void registerBones() {
        super.registerBones();
        for (String bone : new String[]{"monowire_gate", "monowire_feed", "wire_view", "offhand_view"}) {
            this.registerPlayerAnimBone(bone);
        }
    }

    public static void register() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(RIGHT_ID, 1000, Right::new);
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(LEFT_ID, 1005, Left::new);
    }

    private static MonowireAnimation controller(AbstractClientPlayer p, HumanoidArm side) {
        MonowireAnimation a;
        IAnimation layer = PlayerAnimationAccess.getPlayerAnimationLayer((AbstractClientPlayer)p, (ResourceLocation)(side == HumanoidArm.LEFT ? LEFT_ID : RIGHT_ID));
        return layer instanceof MonowireAnimation ? (a = (MonowireAnimation)layer) : null;
    }

    private static Class<?> type(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? Left.class : Right.class;
    }

    public static void update(AbstractClientPlayer p) {
        for (HumanoidArm side : HumanoidArm.values()) {
            MonowireAnimation c = MonowireAnimation.controller(p, side);
            if (c == null || c.lastEntityTick == p.tickCount) continue;
            c.lastEntityTick = p.tickCount;
            c.updateState();
        }
    }

    public static boolean rendering(AbstractClientPlayer p) {
        return MonowireAnimation.rendering(p, HumanoidArm.RIGHT) || MonowireAnimation.rendering(p, HumanoidArm.LEFT);
    }

    public static boolean rendering(AbstractClientPlayer p, HumanoidArm side) {
        if (p instanceof AfterimagePlayerEntity) {
            return FrozenPlayerAnimation.layer(p, MonowireAnimation.type(side)) != null;
        }
        MonowireAnimation c = MonowireAnimation.controller(p, side);
        return c != null && c.isActive();
    }

    public static boolean holdsOther(AbstractClientPlayer p, HumanoidArm side) {
        MonowireAnimation c = MonowireAnimation.controller(p, side);
        return c != null && c.isActive() && c.held;
    }

    public static boolean gripping(AbstractClientPlayer p, HumanoidArm side) {
        MonowireAnimation c = MonowireAnimation.controller(p, side);
        return c != null && c.held && (c.deployed || c.retractTicks > 0) && c.slashTicks == 0 && c.drawTicks <= 4;
    }

    public static int slashAge(AbstractClientPlayer p) {
        return MonowireAnimation.slashAge(p, MonowireImplant.side((Player)p));
    }

    public static int slashAge(AbstractClientPlayer p, HumanoidArm side) {
        MonowireAnimation c = MonowireAnimation.controller(p, side);
        return c == null ? 10 : 10 - c.slashTicks;
    }

    public static CyberwareRig.Transform transform(AbstractClientPlayer p, HumanoidArm side, String name) {
        FrozenPlayerAnimation.Layer frozen = FrozenPlayerAnimation.layer(p, MonowireAnimation.type(side));
        MonowireAnimation c = MonowireAnimation.controller(p, side);
        if (!(frozen != null || c != null && c.isActive())) {
            CyberwareRig.Transform shown = name.endsWith("_view") ? null : PreviewDeploy.transform(p, "monowire", side, name);
            return shown != null ? shown : CyberwareRig.Transform.IDENTITY;
        }
        PlayerAnimBone b = frozen != null ? frozen.raw(new PlayerAnimBone(name)) : c.get3DTransformRaw(new PlayerAnimBone(name));
        return new CyberwareRig.Transform(new Vector3f(b.positionX / 16.0f, -b.positionY / 16.0f, b.positionZ / 16.0f), new Vector3f(b.rotX, b.rotY, b.rotZ), new Vector3f(b.scaleX, b.scaleY, b.scaleZ));
    }

    public static CyberwareRig.Transform view(AbstractClientPlayer p, HumanoidArm side, String name) {
        CyberwareRig.Transform t = MonowireAnimation.transform(p, side, name);
        if (side != HumanoidArm.LEFT) {
            return t;
        }
        return new CyberwareRig.Transform(new Vector3f(-t.position().x, t.position().y, t.position().z), new Vector3f(t.rotation().x, -t.rotation().y, -t.rotation().z), t.scale());
    }

    public static boolean outletOpen(AbstractClientPlayer p, HumanoidArm side) {
        return MonowireAnimation.transform((AbstractClientPlayer)p, (HumanoidArm)side, (String)"monowire_gate").position().y < -0.015625f;
    }

    private void updateState() {
        boolean safe;
        if (this.drawTicks > 0) {
            --this.drawTicks;
        }
        if (this.slashTicks > 0) {
            --this.slashTicks;
        }
        if (this.retractTicks > 0) {
            --this.retractTicks;
        }
        boolean next = MonowireImplant.deployed((Player)this.player, this.side);
        boolean bl = safe = MonowireImplant.hasHardware((Player)this.player, this.side) && MonowireCombat.canUse((Player)this.player, this.side);
        if (!safe) {
            this.stop();
            this.deployed = false;
            this.retractTicks = 0;
            this.slashTicks = 0;
            this.drawTicks = 0;
            return;
        }
        if (!PlayerAnimResources.hasAnimation((ResourceLocation)MonowireAnimation.id("monowire_idle_alone"))) {
            return;
        }
        MonowireState state = MonowireImplant.state((Player)this.player, this.side);
        boolean nextHeld = MonowireCombat.otherHandFree((Player)this.player, this.side);
        if (next && !this.deployed) {
            this.held = nextHeld;
            this.atRest = false;
            this.yielding = false;
            this.play(MonowireAnimation.clip(this.held ? "monowire_draw" : "monowire_draw_alone", false));
            this.drawTicks = 8;
            this.retractTicks = 0;
            this.slashTicks = 0;
        } else if (!next && this.deployed) {
            this.play(MonowireAnimation.clip(this.held ? "monowire_retract" : "monowire_retract_alone", false));
            this.retractTicks = 8;
            this.slashTicks = 0;
            this.drawTicks = 0;
            this.atRest = false;
            this.yielding = false;
        } else if (next && state.stroke() != this.stroke && state.action() == 3) {
            this.play(MonowireAnimation.clip(this.side == HumanoidArm.LEFT ? "monowire_cross_far_alone" : "monowire_cross_alone", false));
            this.slashTicks = 10;
            this.atRest = false;
            this.yielding = false;
        } else if (next && state.stroke() != this.stroke && state.action() == 1) {
            String lash = "monowire_lash_" + "abc".charAt(MonowirePhysics.Lash.of(state.stroke()).ordinal());
            this.play(MonowireAnimation.clip(this.held ? lash : lash + "_alone", false));
            this.slashTicks = 10;
            this.atRest = false;
            this.yielding = false;
        } else if (next && state.action() == 2 && this.action != 2) {
            this.play(MonowireAnimation.clip("monowire_charge_alone", false));
            this.atRest = false;
            this.yielding = false;
        } else if (next && this.drawTicks == 0 && ArmHardware.striking((Player)this.player, this.side.getOpposite()) && ArmHardware.strike((Player)this.player, this.side.getOpposite()) != this.otherStrike && state.action() != 1) {
            this.play(MonowireAnimation.clip("monowire_yield_alone", false));
            this.yielding = true;
            this.atRest = false;
            this.slashTicks = 0;
        } else if (next && this.drawTicks == 0 && state.action() == 0 && (nextHeld != this.held && !this.yielding && this.slashTicks == 0 || !this.atRest && !this.yielding && this.slashTicks == 0 && (this.action != 0 || this.isHoldingLastFrame()) || this.yielding && !ArmHardware.striking((Player)this.player, this.side.getOpposite()) && this.isHoldingLastFrame())) {
            this.held = nextHeld;
            this.play(this.idleLoop());
            this.atRest = true;
            this.yielding = false;
        }
        if (!next && this.retractTicks == 0) {
            this.stop();
        }
        this.deployed = next;
        this.stroke = state.stroke();
        this.action = state.action();
        this.otherStrike = ArmHardware.strike((Player)this.player, this.side.getOpposite());
    }

    private RawAnimation idleLoop() {
        return PlayerRawAnimationBuilder.begin().thenLoop(MonowireAnimation.id(this.held ? "monowire_idle" : "monowire_idle_alone")).build();
    }

    private boolean isHoldingLastFrame() {
        return this.getAnimationState() == State.PAUSED;
    }

    private static RawAnimation clip(String once, boolean loop) {
        return loop ? PlayerRawAnimationBuilder.begin().thenLoop(MonowireAnimation.id(once)).build() : PlayerRawAnimationBuilder.begin().thenPlayAndHold(MonowireAnimation.id(once)).build();
    }

    private void play(RawAnimation clip) {
        if (!this.atRest && this.isActive()) {
            this.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn((int)2, (EasingType)EasingType.LINEAR), clip, false);
        } else {
            this.triggerAnimation(clip);
        }
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)name);
    }

    public static final class Left
    extends MonowireAnimation {
        Left(AbstractClientPlayer p) {
            super(p, HumanoidArm.LEFT);
        }
    }

    public static final class Right
    extends MonowireAnimation {
        Right(AbstractClientPlayer p) {
            super(p, HumanoidArm.RIGHT);
        }
    }
}

