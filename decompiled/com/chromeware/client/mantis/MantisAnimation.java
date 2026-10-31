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
package com.chromeware.client.mantis;

import com.chromeware.client.cyberware.CyberwareRig;
import com.chromeware.client.cyberware.PreviewDeploy;
import com.chromeware.client.sandevistan.AfterimagePlayerEntity;
import com.chromeware.client.sandevistan.FrozenPlayerAnimation;
import com.chromeware.cyberware.ArmHardware;
import com.chromeware.mantis.MantisImplant;
import com.chromeware.mantis.MantisState;
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
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.easing.EasingType;
import com.zigythebird.playeranimcore.enums.PlayState;
import com.zigythebird.playeranimcore.enums.State;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

public abstract class MantisAnimation
extends PlayerAnimationController {
    public static final String[] PARTS = new String[]{"frame", "forearm", "strut", "strut_slide", "plate_outer_front", "frame_cover_front", "frame_cover_back", "plate_outer_back", "plate_under_front", "plate_under_back", "blade", "tip", "extension", "hand", "view", "plate_guide_mount", "plate_guide", "plate_guide_slide", "plate_guide_tip", "plate_guide_head"};
    private final HumanoidArm side;
    private final String suffix;
    private boolean deployed;
    private int sequence;
    private int otherStrike;
    private int deployTicks;
    private int retractTicks;
    private boolean yielding;
    private boolean atRest;
    private int lastEntityTick = Integer.MIN_VALUE;
    private static final ResourceLocation RIGHT_ID = MantisAnimation.id("mantis_blades");
    private static final ResourceLocation LEFT_ID = MantisAnimation.id("mantis_blades_left");

    MantisAnimation(AbstractClientPlayer player, HumanoidArm side) {
        super(player, (controller, state, setter) -> PlayState.CONTINUE);
        this.side = side;
        this.suffix = side == HumanoidArm.LEFT ? "_left" : "_right";
        this.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
        this.setFirstPersonFollowsCamera(false);
        this.setFirstPersonConfiguration(new FirstPersonConfiguration().setShowLeftArm(true).setShowRightArm(true).setShowLeftItem(false).setShowRightItem(false));
    }

    public void registerBones() {
        super.registerBones();
        for (String side : new String[]{"_right", "_left"}) {
            for (String part : PARTS) {
                this.registerPlayerAnimBone("mantis" + side + "_" + part);
            }
        }
    }

    public static void register() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(RIGHT_ID, 1002, Right::new);
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(LEFT_ID, 1003, Left::new);
    }

    private static MantisAnimation controller(AbstractClientPlayer p, HumanoidArm side) {
        MantisAnimation a;
        IAnimation layer = PlayerAnimationAccess.getPlayerAnimationLayer((AbstractClientPlayer)p, (ResourceLocation)(side == HumanoidArm.LEFT ? LEFT_ID : RIGHT_ID));
        return layer instanceof MantisAnimation ? (a = (MantisAnimation)layer) : null;
    }

    private static Class<?> type(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? Left.class : Right.class;
    }

    public static void update(AbstractClientPlayer p) {
        for (HumanoidArm side : HumanoidArm.values()) {
            MantisAnimation c = MantisAnimation.controller(p, side);
            if (c == null || c.lastEntityTick == p.tickCount) continue;
            c.lastEntityTick = p.tickCount;
            c.updateState();
        }
    }

    public static boolean rendering(AbstractClientPlayer p) {
        return MantisAnimation.rendering(p, HumanoidArm.RIGHT) || MantisAnimation.rendering(p, HumanoidArm.LEFT);
    }

    public static boolean rendering(AbstractClientPlayer p, HumanoidArm side) {
        if (p instanceof AfterimagePlayerEntity) {
            return FrozenPlayerAnimation.layer(p, MantisAnimation.type(side)) != null;
        }
        MantisAnimation c = MantisAnimation.controller(p, side);
        return c != null && c.isActive();
    }

    public static float headFollowWeight(AbstractClientPlayer p, float tick) {
        if (p instanceof AfterimagePlayerEntity) {
            AfterimagePlayerEntity echo = (AfterimagePlayerEntity)p;
            return echo.mantisHeadFollow;
        }
        float weight = 0.0f;
        for (HumanoidArm side : HumanoidArm.values()) {
            MantisAnimation c = MantisAnimation.controller(p, side);
            if (c == null || !c.isActive()) continue;
            float phase = c.deployed ? 1.0f - ((float)c.deployTicks - tick) / 16.0f : ((float)c.retractTicks - tick) / 16.0f;
            weight = Math.max(weight, Math.clamp((float)phase, (float)0.0f, (float)1.0f));
        }
        return weight;
    }

    public static float drawWeight(AbstractClientPlayer p, HumanoidArm side, float tick) {
        MantisAnimation c = MantisAnimation.controller(p, side);
        if (c == null || !c.isActive()) {
            return 0.0f;
        }
        return Math.clamp((float)(c.deployed ? 1.0f - ((float)c.deployTicks - tick) / 16.0f : ((float)c.retractTicks - tick) / 16.0f), (float)0.0f, (float)1.0f);
    }

    public static CyberwareRig.Transform transform(AbstractClientPlayer p, String name) {
        HumanoidArm side = name.startsWith("mantis_left_") ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        FrozenPlayerAnimation.Layer frozen = FrozenPlayerAnimation.layer(p, MantisAnimation.type(side));
        MantisAnimation c = MantisAnimation.controller(p, side);
        if (!(frozen != null || c != null && c.isActive())) {
            CyberwareRig.Transform shown = PreviewDeploy.transform(p, "mantis_blades", side, name);
            return shown != null ? shown : CyberwareRig.Transform.IDENTITY;
        }
        PlayerAnimBone b = frozen != null ? frozen.get3DTransform(new PlayerAnimBone(name)) : c.get3DTransform(new PlayerAnimBone(name));
        return new CyberwareRig.Transform(new Vector3f(b.positionX / 16.0f, -b.positionY / 16.0f, b.positionZ / 16.0f), new Vector3f(b.rotX, b.rotY, b.rotZ), new Vector3f(b.scaleX, b.scaleY, b.scaleZ));
    }

    private void updateState() {
        if (this.retractTicks > 0) {
            --this.retractTicks;
        }
        if (this.deployTicks > 0) {
            --this.deployTicks;
        }
        MantisState state = MantisImplant.state((Player)this.player, this.side);
        if (!MantisImplant.canDeploy((Player)this.player, this.side)) {
            this.stop();
            this.deployed = false;
            this.retractTicks = 0;
            this.deployTicks = 0;
            this.sequence = state.sequence();
            return;
        }
        if (!PlayerAnimResources.hasAnimation((ResourceLocation)MantisAnimation.id("mantis_idle" + this.suffix))) {
            return;
        }
        if (state.deployed() != this.deployed) {
            RawAnimation clip = MantisAnimation.hold((state.deployed() ? "mantis_deploy" : "mantis_retract") + this.suffix);
            if (this.isActive()) {
                this.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn((int)3, (EasingType)EasingType.LINEAR), clip, false);
            } else {
                this.triggerAnimation(clip);
            }
            this.retractTicks = state.deployed() ? 0 : 16;
            this.deployTicks = state.deployed() ? 16 : 0;
            this.atRest = false;
        } else if (state.deployed() && state.sequence() != this.sequence && state.action() == 4) {
            this.atRest = false;
            this.yielding = false;
        } else if (state.deployed() && state.sequence() != this.sequence) {
            String name = (switch (state.action()) {
                case 1 -> "mantis_cut";
                case 2 -> "mantis_charge";
                case 3 -> "mantis_cross";
                default -> ArmHardware.striking((Player)this.player, this.side.getOpposite()) ? "mantis_yield" : "mantis_idle";
            }) + this.suffix;
            boolean loop = name.startsWith("mantis_idle");
            this.play(loop ? this.loop() : MantisAnimation.hold(name));
            this.deployTicks = 0;
            this.yielding = name.startsWith("mantis_yield");
            this.atRest = loop;
        } else if (state.deployed() && this.deployTicks == 0 && state.action() == 0 && ArmHardware.striking((Player)this.player, this.side.getOpposite()) && ArmHardware.strike((Player)this.player, this.side.getOpposite()) != this.otherStrike) {
            this.play(MantisAnimation.hold("mantis_yield" + this.suffix));
            this.yielding = true;
            this.atRest = false;
        } else if (state.deployed() && this.yielding && !ArmHardware.striking((Player)this.player, this.side.getOpposite())) {
            this.play(this.loop());
            this.yielding = false;
            this.atRest = true;
        } else if (state.deployed() && this.deployTicks == 0 && state.action() == 0 && !this.yielding && this.isHoldingLastFrame()) {
            this.play(this.loop());
            this.atRest = true;
        }
        if (!state.deployed() && this.retractTicks == 0) {
            this.stop();
        }
        this.deployed = state.deployed();
        this.sequence = state.sequence();
        this.otherStrike = ArmHardware.strike((Player)this.player, this.side.getOpposite());
    }

    private static ResourceLocation id(String n) {
        return ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)n);
    }

    private RawAnimation loop() {
        return PlayerRawAnimationBuilder.begin().thenLoop(MantisAnimation.id("mantis_idle" + this.suffix)).build();
    }

    private void play(RawAnimation clip) {
        if (!this.atRest && this.isActive()) {
            this.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn((int)2, (EasingType)EasingType.LINEAR), clip, false);
        } else {
            this.triggerAnimation(clip);
        }
    }

    private boolean isHoldingLastFrame() {
        return this.getAnimationState() == State.PAUSED;
    }

    private static RawAnimation hold(String once) {
        return PlayerRawAnimationBuilder.begin().thenPlayAndHold(MantisAnimation.id(once)).build();
    }

    public static final class Left
    extends MantisAnimation {
        Left(AbstractClientPlayer p) {
            super(p, HumanoidArm.LEFT);
        }
    }

    public static final class Right
    extends MantisAnimation {
        Right(AbstractClientPlayer p) {
            super(p, HumanoidArm.RIGHT);
        }
    }
}

