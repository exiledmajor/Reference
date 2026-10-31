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
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.neoforge.attachment.AttachmentType
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package com.chromeware.client.launcher;

import com.chromeware.client.cyberware.CyberwareRig;
import com.chromeware.client.cyberware.PreviewDeploy;
import com.chromeware.client.sandevistan.AfterimagePlayerEntity;
import com.chromeware.client.sandevistan.FrozenPlayerAnimation;
import com.chromeware.cyberware.ArmHardware;
import com.chromeware.launcher.ProjectileLauncher;
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
import java.util.function.Supplier;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public abstract class LauncherAnimation
extends PlayerAnimationController {
    public static final String[] MECHANISM = new String[]{"launcher_frame", "launcher_panel_prox_front", "launcher_panel_prox_back", "launcher_panel_dist_front", "launcher_panel_dist_back", "launcher_under_prox", "launcher_under_dist", "launcher_under_dist_front", "launcher_under_dist_back", "launcher_hand", "launcher_carriage", "launcher_barrel", "launcher_view", "launcher_guide_palm", "launcher_link_front_front", "launcher_link_front_back", "launcher_link_rear_front", "launcher_link_rear_back", "launcher_muscle_front_a", "launcher_muscle_front_b", "launcher_muscle_back_a", "launcher_muscle_back_b", "launcher_guide_front_base", "launcher_guide_front_slide", "launcher_guide_front_tip", "launcher_guide_head_front", "launcher_guide_dist_front", "launcher_guide_back_base", "launcher_guide_back_slide", "launcher_guide_back_tip", "launcher_guide_head_back", "launcher_guide_dist_back"};
    private static final ResourceLocation LEFT_ID = LauncherAnimation.id("projectile_launcher");
    private static final ResourceLocation RIGHT_ID = LauncherAnimation.id("projectile_launcher_right");
    private final HumanoidArm side;
    private boolean deployed;
    private boolean yielding;
    private boolean atRest;
    private boolean charging;
    private int transitionTicks;
    private int retractTicks;
    private int drawTicks;
    private int shot;
    private int otherStrike;
    private int volley;
    private int charged;
    private static final int VOLLEY_TICKS = 20;
    private int lastEntityTick = Integer.MIN_VALUE;

    LauncherAnimation(AbstractClientPlayer player, HumanoidArm side) {
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
        for (String name : MECHANISM) {
            this.registerPlayerAnimBone(name);
        }
    }

    public static void register() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(LEFT_ID, 1001, Left::new);
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(RIGHT_ID, 1004, Right::new);
    }

    private static LauncherAnimation controller(AbstractClientPlayer player, HumanoidArm side) {
        LauncherAnimation a;
        IAnimation layer = PlayerAnimationAccess.getPlayerAnimationLayer((AbstractClientPlayer)player, (ResourceLocation)(side == HumanoidArm.LEFT ? LEFT_ID : RIGHT_ID));
        return layer instanceof LauncherAnimation ? (a = (LauncherAnimation)layer) : null;
    }

    private static Class<?> type(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? Left.class : Right.class;
    }

    public static void update(AbstractClientPlayer player) {
        for (HumanoidArm side : HumanoidArm.values()) {
            LauncherAnimation c = LauncherAnimation.controller(player, side);
            if (c == null || c.lastEntityTick == player.tickCount) continue;
            c.lastEntityTick = player.tickCount;
            c.updateState();
        }
    }

    public static boolean rendering(AbstractClientPlayer player) {
        return LauncherAnimation.rendering(player, HumanoidArm.LEFT) || LauncherAnimation.rendering(player, HumanoidArm.RIGHT);
    }

    public static boolean rendering(AbstractClientPlayer player, HumanoidArm side) {
        if (player instanceof AfterimagePlayerEntity) {
            return FrozenPlayerAnimation.layer(player, LauncherAnimation.type(side)) != null;
        }
        LauncherAnimation c = LauncherAnimation.controller(player, side);
        return c != null && c.isActive();
    }

    public static CyberwareRig.Transform transform(AbstractClientPlayer player, HumanoidArm side, String name) {
        FrozenPlayerAnimation.Layer frozen = FrozenPlayerAnimation.layer(player, LauncherAnimation.type(side));
        LauncherAnimation c = LauncherAnimation.controller(player, side);
        if (!(frozen != null || c != null && c.isActive())) {
            CyberwareRig.Transform shown = name.equals("launcher_view") ? null : PreviewDeploy.transform(player, "projectile_launch_system", side, name);
            return shown != null ? shown : CyberwareRig.Transform.IDENTITY;
        }
        PlayerAnimBone b = frozen != null ? frozen.raw(new PlayerAnimBone(name)) : c.get3DTransformRaw(new PlayerAnimBone(name));
        return new CyberwareRig.Transform(new Vector3f(b.positionX / 16.0f, -b.positionY / 16.0f, b.positionZ / 16.0f), new Vector3f(b.rotX, b.rotY, b.rotZ), new Vector3f(b.scaleX, b.scaleY, b.scaleZ));
    }

    public static CyberwareRig.Transform view(AbstractClientPlayer player, HumanoidArm side) {
        CyberwareRig.Transform t = LauncherAnimation.transform(player, side, "launcher_view");
        if (side == HumanoidArm.LEFT) {
            return t;
        }
        return new CyberwareRig.Transform(new Vector3f(-t.position().x, t.position().y, t.position().z), new Vector3f(t.rotation().x, -t.rotation().y, -t.rotation().z), new Vector3f((Vector3fc)t.scale()));
    }

    private void updateState() {
        if (this.transitionTicks > 0) {
            --this.transitionTicks;
        }
        if (this.retractTicks > 0) {
            --this.retractTicks;
        }
        if (this.drawTicks > 0) {
            --this.drawTicks;
        }
        Supplier<AttachmentType<Integer>> shotKey = ProjectileLauncher.shotKey(this.side);
        if (!ProjectileLauncher.canDeploy((Player)this.player, this.side)) {
            this.stop();
            this.deployed = false;
            this.drawTicks = 0;
            this.retractTicks = 0;
            this.transitionTicks = 0;
            this.shot = (Integer)this.player.getData(shotKey);
            this.volley = ProjectileLauncher.volleys((Player)this.player, this.side);
            this.charging = false;
            return;
        }
        if (!PlayerAnimResources.hasAnimation((ResourceLocation)LauncherAnimation.id("launcher_aim"))) {
            return;
        }
        boolean next = ProjectileLauncher.deployed((Player)this.player, this.side);
        int nextShot = (Integer)this.player.getData(shotKey);
        boolean other = ArmHardware.striking((Player)this.player, this.side.getOpposite());
        boolean charge = ProjectileLauncher.chargeShown((Player)this.player, this.side);
        int nextVolley = ProjectileLauncher.volleys((Player)this.player, this.side);
        int n = this.charged = charge ? this.charged + 1 : 0;
        if (next != this.deployed) {
            this.play(LauncherAnimation.hold(next ? "launcher_deploy" : "launcher_retract"));
            this.transitionTicks = 16;
            this.retractTicks = next ? 0 : 16;
            this.drawTicks = next ? 16 : 0;
            this.yielding = false;
            this.atRest = false;
        } else if (next && nextVolley != this.volley) {
            this.play(LauncherAnimation.hold("launcher_volley"));
            this.transitionTicks = 20;
            this.drawTicks = 0;
            this.yielding = false;
            this.atRest = false;
        } else if (next && nextShot != this.shot) {
            this.play(LauncherAnimation.hold("launcher_fire"));
            this.transitionTicks = 10;
            this.drawTicks = 0;
            this.yielding = false;
            this.atRest = false;
        } else if (charge && !this.charging) {
            this.play(LauncherAnimation.hold("launcher_charge"));
            this.transitionTicks = 0;
            this.drawTicks = 0;
            this.yielding = false;
            this.atRest = false;
        } else if (charge && this.charged == 15) {
            this.play(PlayerRawAnimationBuilder.begin().thenLoop(LauncherAnimation.id("launcher_charged")).build());
        } else if (!(!next || charge || this.drawTicks != 0 || !other || ProjectileLauncher.firing((Player)this.player, this.side) || this.yielding && ArmHardware.strike((Player)this.player, this.side.getOpposite()) == this.otherStrike)) {
            this.play(LauncherAnimation.hold("launcher_yield"));
            this.transitionTicks = 0;
            this.yielding = true;
            this.atRest = false;
        } else if (next && !charge && this.transitionTicks == 0 && !other && !this.atRest) {
            this.play(PlayerRawAnimationBuilder.begin().thenLoop(LauncherAnimation.id("launcher_aim")).build());
            this.yielding = false;
            this.atRest = true;
        }
        if (!next && this.retractTicks == 0) {
            this.stop();
        }
        this.deployed = next;
        this.shot = nextShot;
        this.volley = nextVolley;
        this.charging = charge;
        this.otherStrike = ArmHardware.strike((Player)this.player, this.side.getOpposite());
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)name);
    }

    private void play(RawAnimation clip) {
        if (this.isActive() && !this.atRest) {
            this.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn((int)2, (EasingType)EasingType.LINEAR), clip, false);
        } else {
            this.triggerAnimation(clip);
        }
    }

    private static RawAnimation hold(String once) {
        return PlayerRawAnimationBuilder.begin().thenPlayAndHold(LauncherAnimation.id(once)).build();
    }

    public static final class Left
    extends LauncherAnimation {
        Left(AbstractClientPlayer p) {
            super(p, HumanoidArm.LEFT);
        }
    }

    public static final class Right
    extends LauncherAnimation {
        Right(AbstractClientPlayer p) {
            super(p, HumanoidArm.RIGHT);
        }
    }
}

