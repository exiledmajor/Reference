/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 */
package com.chromeware.client.body;

import com.chromeware.body.BodyPart;
import com.chromeware.body.CyberLimbs;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbRegrowth;
import com.chromeware.body.LimbState;
import com.chromeware.client.extendo.ExtendoClient;
import com.chromeware.client.mantis.MantisAnimation;
import com.chromeware.cyberware.ArmImplant;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.extendo.ExtendoGrip;
import com.chromeware.launcher.ProjectileLauncher;
import com.chromeware.mantis.MantisImplant;
import com.chromeware.surgery.SurgerySeat;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;

public final class LimbRenderContext
implements AutoCloseable {
    private static final Rule HIDDEN = new Rule(true, 0.0f, false, false, false, false);
    private static final Rule SHORTENED = new Rule(false, 9.0f, false, false, false, false);
    private static final Rule UPPER_ARM_ARMOR = new Rule(false, 6.0f, false, false, false, false);
    private static final Rule LAUNCHER_LEFT = new Rule(false, 0.0f, false, true, true, false);
    private static final Rule LAUNCHER_RIGHT = new Rule(false, 0.0f, false, false, true, false);
    private static final Rule MANTIS = new Rule(false, 0.0f, false, false, false, true);
    private static final Rule EXTENDO = new Rule(false, 0.0f, false, false, false, false, 1.0f, true);
    private static final ThreadLocal<Deque<LimbRenderContext>> SCOPES = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Deque<Anatomy>> EXTERNAL = ThreadLocal.withInitial(ArrayDeque::new);
    private final Map<ModelPart, Rule> rules = new IdentityHashMap<ModelPart, Rule>();
    private final boolean inherit;
    private final LimbState state;
    private final Player player;
    private final ArmImplant leftImplant;
    private final ArmImplant rightImplant;
    private final boolean leftLauncher;
    private final boolean rightLauncher;
    private final boolean leftMantis;
    private final boolean rightMantis;
    private final boolean mantisAnimating;
    private final boolean leftExtendo;
    private final boolean rightExtendo;
    private final boolean leftExtended;
    private final boolean rightExtended;
    private final CyberLimbs frames;
    private final boolean torsoRipped;

    private LimbRenderContext(Player player, HumanoidModel<?> model, boolean inherit) {
        SurgerySeat seat;
        AbstractClientPlayer client;
        this.inherit = inherit;
        this.state = LimbAttachments.get(player);
        this.player = player;
        this.leftImplant = ArmImplants.get(player, HumanoidArm.LEFT);
        this.rightImplant = ArmImplants.get(player, HumanoidArm.RIGHT);
        this.leftLauncher = ProjectileLauncher.active(player, HumanoidArm.LEFT);
        this.rightLauncher = ProjectileLauncher.active(player, HumanoidArm.RIGHT);
        this.leftMantis = MantisImplant.hasHardware(player, HumanoidArm.LEFT);
        this.rightMantis = MantisImplant.hasHardware(player, HumanoidArm.RIGHT);
        this.mantisAnimating = player instanceof AbstractClientPlayer && MantisAnimation.rendering(client = (AbstractClientPlayer)player);
        this.leftExtendo = ExtendoGrip.active(player, HumanoidArm.LEFT);
        this.rightExtendo = ExtendoGrip.active(player, HumanoidArm.RIGHT);
        this.leftExtended = this.leftExtendo && ExtendoClient.opened(player, HumanoidArm.LEFT);
        this.rightExtended = this.rightExtendo && ExtendoClient.opened(player, HumanoidArm.RIGHT);
        this.frames = CyberLimbs.get(player);
        Entity entity = player.getVehicle();
        this.torsoRipped = entity instanceof SurgerySeat && (seat = (SurgerySeat)entity).ripped() && seat.part() == BodyPart.BODY;
        this.addModel(model);
        SCOPES.get().push(this);
    }

    public static LimbRenderContext open(Player player, HumanoidModel<?> model, boolean inherit) {
        return new LimbRenderContext(player, model, inherit);
    }

    public void addModel(HumanoidModel<?> model) {
        if (this.state.isMissing(BodyPart.HEAD)) {
            this.rules.put(model.head, HIDDEN);
            this.rules.put(model.hat, HIDDEN);
        }
        this.torso(model.body);
        this.add(model.leftArm, BodyPart.LEFT_ARM, BodyPart.LEFT_HAND);
        this.add(model.rightArm, BodyPart.RIGHT_ARM, BodyPart.RIGHT_HAND);
        Function<ArmImplant, Rule> implantedArm = implant -> !(model instanceof PlayerModel) && implant.item().is(CyberwareItems.GORILLA_ARMS) ? UPPER_ARM_ARMOR : HIDDEN;
        if (this.leftImplant.replacesLimb()) {
            this.rules.put(model.leftArm, implantedArm.apply(this.leftImplant));
        }
        if (this.rightImplant.replacesLimb()) {
            this.rules.put(model.rightArm, implantedArm.apply(this.rightImplant));
        }
        if (!(model instanceof PlayerModel) && this.mantisAnimating) {
            if (this.leftMantis) {
                this.rules.put(model.leftArm, MANTIS);
            }
            if (this.rightMantis) {
                this.rules.put(model.rightArm, MANTIS);
            }
        }
        if (!(model instanceof PlayerModel)) {
            if (this.leftExtended) {
                this.rules.put(model.leftArm, EXTENDO);
            }
            if (this.rightExtended) {
                this.rules.put(model.rightArm, EXTENDO);
            }
        }
        this.add(model.leftLeg, BodyPart.LEFT_LEG, BodyPart.LEFT_FOOT);
        this.add(model.rightLeg, BodyPart.RIGHT_LEG, BodyPart.RIGHT_FOOT);
        if (model instanceof PlayerModel) {
            PlayerModel skin = (PlayerModel)model;
            if (this.leftLauncher) {
                this.rules.put(skin.leftArm, LAUNCHER_LEFT);
                this.rules.put(skin.leftSleeve, LAUNCHER_LEFT);
            }
            if (this.rightLauncher) {
                this.rules.put(skin.rightArm, LAUNCHER_RIGHT);
                this.rules.put(skin.rightSleeve, LAUNCHER_RIGHT);
            }
            if (this.leftMantis) {
                this.rules.put(skin.leftArm, MANTIS);
                this.rules.put(skin.leftSleeve, MANTIS);
            }
            if (this.rightMantis) {
                this.rules.put(skin.rightArm, MANTIS);
                this.rules.put(skin.rightSleeve, MANTIS);
            }
            if (this.leftExtendo) {
                this.rules.put(skin.leftArm, EXTENDO);
                this.rules.put(skin.leftSleeve, EXTENDO);
            }
            if (this.rightExtendo) {
                this.rules.put(skin.rightArm, EXTENDO);
                this.rules.put(skin.rightSleeve, EXTENDO);
            }
            this.torso(skin.jacket);
            this.add(skin.leftSleeve, BodyPart.LEFT_ARM, BodyPart.LEFT_HAND);
            this.add(skin.rightSleeve, BodyPart.RIGHT_ARM, BodyPart.RIGHT_HAND);
            if (this.leftImplant.replacesLimb()) {
                this.rules.put(skin.leftSleeve, HIDDEN);
            }
            if (this.rightImplant.replacesLimb()) {
                this.rules.put(skin.rightSleeve, HIDDEN);
            }
            this.add(skin.leftPants, BodyPart.LEFT_LEG, BodyPart.LEFT_FOOT);
            this.add(skin.rightPants, BodyPart.RIGHT_LEG, BodyPart.RIGHT_FOOT);
            this.chrome(BodyPart.RIGHT_ARM, skin.rightArm, skin.rightSleeve);
            this.chrome(BodyPart.LEFT_ARM, skin.leftArm, skin.leftSleeve);
            this.chrome(BodyPart.RIGHT_LEG, skin.rightLeg, skin.rightPants);
            this.chrome(BodyPart.LEFT_LEG, skin.leftLeg, skin.leftPants);
            this.chrome(BodyPart.BODY, skin.body, skin.jacket);
        }
    }

    private void chrome(BodyPart limb, ModelPart part, ModelPart layer) {
        if (!this.frames.has(limb) || this.frames.skinned(limb) || this.state.isMissing(limb)) {
            return;
        }
        this.rules.put(part, HIDDEN);
        this.rules.put(layer, HIDDEN);
    }

    private void torso(ModelPart part) {
        if (this.torsoRipped) {
            this.rules.put(part, HIDDEN);
        } else if (this.state.isMissing(BodyPart.BODY)) {
            float length = LimbRegrowth.length(this.player, BodyPart.BODY);
            this.rules.put(part, length < 1.0f ? new Rule(false, 0.0f, false, false, false, false, length) : HIDDEN);
        }
    }

    private void add(ModelPart modelPart, BodyPart limb, BodyPart end) {
        float growing = LimbRegrowth.length(this.player, limb);
        if (growing < 1.0f) {
            this.rules.put(modelPart, new Rule(false, 0.0f, false, false, false, false, growing));
        } else if (this.state.isMissing(limb)) {
            this.rules.put(modelPart, HIDDEN);
        } else if (this.state.isMissing(end)) {
            this.rules.put(modelPart, SHORTENED);
        }
    }

    public static Rule ruleFor(ModelPart part) {
        Rule rule;
        for (Anatomy anatomy : EXTERNAL.get()) {
            rule = anatomy.rules.get(part);
            if (rule == null) continue;
            return rule;
        }
        for (LimbRenderContext scope : SCOPES.get()) {
            rule = scope.rules.get(part);
            if (rule != null) {
                return rule;
            }
            if (scope.inherit) continue;
            break;
        }
        return null;
    }

    public static void addArmorModel(HumanoidModel<?> model) {
        LimbRenderContext scope;
        if (!EXTERNAL.get().isEmpty()) {
            LimbRenderContext.anatomyModel(EXTERNAL.get().peek(), model);
        }
        if ((scope = SCOPES.get().peek()) != null) {
            scope.addModel(model);
        }
    }

    public static Scope anatomy(LimbState state, HumanoidModel<?> model) {
        Anatomy scope = new Anatomy(state, new IdentityHashMap<ModelPart, Rule>());
        LimbRenderContext.anatomyModel(scope, model);
        EXTERNAL.get().push(scope);
        return () -> {
            EXTERNAL.get().pop();
            if (EXTERNAL.get().isEmpty()) {
                EXTERNAL.remove();
            }
        };
    }

    private static void anatomyModel(Anatomy a, HumanoidModel<?> m) {
        if (a.state.isMissing(BodyPart.HEAD)) {
            a.rules.put(m.head, HIDDEN);
            a.rules.put(m.hat, HIDDEN);
        }
        if (a.state.isMissing(BodyPart.BODY)) {
            a.rules.put(m.body, HIDDEN);
        }
        LimbRenderContext.anatomyLimb(a, m.leftArm, BodyPart.LEFT_ARM, BodyPart.LEFT_HAND);
        LimbRenderContext.anatomyLimb(a, m.rightArm, BodyPart.RIGHT_ARM, BodyPart.RIGHT_HAND);
        LimbRenderContext.anatomyLimb(a, m.leftLeg, BodyPart.LEFT_LEG, BodyPart.LEFT_FOOT);
        LimbRenderContext.anatomyLimb(a, m.rightLeg, BodyPart.RIGHT_LEG, BodyPart.RIGHT_FOOT);
        if (m instanceof PlayerModel) {
            PlayerModel skin = (PlayerModel)m;
            if (a.state.isMissing(BodyPart.BODY)) {
                a.rules.put(skin.jacket, HIDDEN);
            }
            LimbRenderContext.anatomyLimb(a, skin.leftSleeve, BodyPart.LEFT_ARM, BodyPart.LEFT_HAND);
            LimbRenderContext.anatomyLimb(a, skin.rightSleeve, BodyPart.RIGHT_ARM, BodyPart.RIGHT_HAND);
            LimbRenderContext.anatomyLimb(a, skin.leftPants, BodyPart.LEFT_LEG, BodyPart.LEFT_FOOT);
            LimbRenderContext.anatomyLimb(a, skin.rightPants, BodyPart.RIGHT_LEG, BodyPart.RIGHT_FOOT);
        }
    }

    private static void anatomyLimb(Anatomy a, ModelPart model, BodyPart part, BodyPart end) {
        if (a.state.isMissing(part)) {
            a.rules.put(model, HIDDEN);
        } else if (a.state.isMissing(end)) {
            a.rules.put(model, SHORTENED);
        }
    }

    @Override
    public void close() {
        SCOPES.get().pop();
        if (SCOPES.get().isEmpty()) {
            SCOPES.remove();
        }
    }

    public record Rule(boolean hidden, float retainedLength, boolean wristCutout, boolean leftWrist, boolean launcherCutout, boolean mantisCutout, float stretch, boolean extendoCutout) {
        Rule(boolean hidden, float retainedLength, boolean wristCutout, boolean leftWrist, boolean launcherCutout, boolean mantisCutout, float stretch) {
            this(hidden, retainedLength, wristCutout, leftWrist, launcherCutout, mantisCutout, stretch, false);
        }

        Rule(boolean hidden, float retainedLength, boolean wristCutout, boolean leftWrist, boolean launcherCutout, boolean mantisCutout) {
            this(hidden, retainedLength, wristCutout, leftWrist, launcherCutout, mantisCutout, 1.0f, false);
        }
    }

    private record Anatomy(LimbState state, Map<ModelPart, Rule> rules) {
    }

    public static interface Scope
    extends AutoCloseable {
        @Override
        public void close();
    }
}

