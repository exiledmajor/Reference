/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  com.simibubi.create.content.equipment.extendoGrip.ExtendoGripRenderHandler
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.ItemInHandRenderer
 *  net.minecraft.client.renderer.entity.player.PlayerRenderer
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.CrossbowItem
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.client.ClientHooks
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  net.neoforged.neoforge.client.event.RenderHandEvent
 *  net.neoforged.neoforge.client.extensions.common.IClientItemExtensions
 *  net.neoforged.neoforge.common.NeoForge
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package com.chromeware.client.extendo;

import com.chromeware.body.LimbAttachments;
import com.chromeware.body.OneHanded;
import com.chromeware.client.cyberware.PreviewDeploy;
import com.chromeware.client.extendo.ExtendoRenderer;
import com.chromeware.extendo.ExtendoGrip;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripRenderHandler;
import java.lang.invoke.LambdaMetafactory;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class ExtendoClient {
    private static final float OPENING = 0.5f;
    private static final float CLOSING = 0.34f;
    private static final Map<Player, Arm[]> ARMS = new WeakHashMap<Player, Arm[]>();
    private static AbstractClientPlayer drawing;
    private static HumanoidArm drawingSide;
    private static ExtendoRenderer.Pose drawn;

    public static void register(IEventBus bus) {
        NeoForge.EVENT_BUS.addListener(ExtendoClient::tick);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, ExtendoClient::hand);
    }

    private static Arm arm(Player player, HumanoidArm side) {
        Arm[] arms = ARMS.get(player);
        return arms == null ? null : arms[side.ordinal()];
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     */
    private static void tick(ClientTickEvent.Post event) {
        mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) {
            return;
        }
        var2_2 = mc.level.players().iterator();
        block0: while (true) {
            if (!var2_2.hasNext()) {
                return;
            }
            player = (AbstractClientPlayer)var2_2.next();
            if (!ExtendoGrip.installed((Player)player)) {
                ExtendoClient.ARMS.remove(player);
                continue;
            }
            arms = ExtendoClient.ARMS.computeIfAbsent((Player)player, (Function<Player, Arm[]>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, lambda$tick$0(net.minecraft.world.entity.player.Player ), (Lnet/minecraft/world/entity/player/Player;)[Lcom/chromeware/client/extendo/ExtendoClient$Arm;)());
            var5_5 = HumanoidArm.values();
            var6_6 = var5_5.length;
            var7_7 = 0;
            while (true) {
                if (var7_7 < var6_6) ** break;
                continue block0;
                side = var5_5[var7_7];
                ExtendoClient.step((Player)player, side, arms[side.ordinal()]);
                ++var7_7;
            }
            break;
        }
    }

    private static void step(Player player, HumanoidArm side, Arm a) {
        a.oOut = a.out;
        a.oOpen = a.open;
        boolean live = ExtendoGrip.active(player, side) && !player.isSpectator();
        a.out *= Math.clamp((float)a.out, (float)0.8f, (float)0.99f);
        float swing = player.getAttackAnim(1.0f);
        if (live && ExtendoClient.swingingWith(player, side) && swing > 0.0f && 1.0f - swing > a.out) {
            a.out = 0.95f;
        }
        if (a.out < 0.02f) {
            a.out = 0.0f;
        }
        if (!live) {
            a.out = 0.0f;
        }
        a.open = a.out * a.out * a.out > 0.01f ? Math.min(1.0f, a.open + 0.5f) : Math.max(0.0f, a.open - 0.34f);
    }

    private static boolean swingingWith(Player player, HumanoidArm side) {
        return player.swinging && player.swingingArm != null && OneHanded.resolve(player, player.swingingArm) == side;
    }

    public static ExtendoRenderer.Pose preview(float seconds) {
        float open = Math.clamp((float)(seconds / 0.15f), (float)0.0f, (float)1.0f);
        return new ExtendoRenderer.Pose(open, 0.5f * ExtendoRenderer.ease((seconds - 0.1f) / 0.4f));
    }

    private static ExtendoRenderer.Pose pose(Arm a, float t) {
        float open = Mth.lerp((float)t, (float)a.oOpen, (float)a.open);
        float out = Mth.lerp((float)t, (float)a.oOut, (float)a.out);
        return new ExtendoRenderer.Pose(open, out * out * out * ExtendoRenderer.ease(open));
    }

    public static ExtendoRenderer.Pose pose(AbstractClientPlayer player, HumanoidArm side) {
        if (drawing == player && drawingSide == side) {
            return drawn;
        }
        float seconds = PreviewDeploy.seconds(player);
        if (seconds >= 0.0f) {
            return ExtendoClient.preview(seconds);
        }
        Arm a = ExtendoClient.arm((Player)player, side);
        return a == null ? ExtendoRenderer.Pose.REST : ExtendoClient.pose(a, Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true));
    }

    public static boolean opened(Player player, HumanoidArm side) {
        Arm a = ExtendoClient.arm(player, side);
        return a != null && (a.open > 0.0f || a.oOpen > 0.0f);
    }

    public static float handOffset(LivingEntity entity, HumanoidArm side) {
        AbstractClientPlayer player;
        if (!(entity instanceof AbstractClientPlayer) || !ExtendoGrip.active((Player)(player = (AbstractClientPlayer)entity), side)) {
            return 0.0f;
        }
        ExtendoRenderer.Pose pose = ExtendoClient.pose(player, side);
        return pose.open() <= 0.0f ? 0.0f : ExtendoRenderer.shift(pose.extension()) / 16.0f;
    }

    public static boolean straightArm() {
        return drawing != null;
    }

    private static void hand(RenderHandEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isInvisible() || player.isScoping()) {
            return;
        }
        ItemStack item = event.getItemStack();
        if (ExtendoGrip.is(player.getOffhandItem()) || ExtendoGrip.is(item)) {
            HumanoidArm main = player.getMainArm();
            Arm a = ExtendoClient.arm((Player)player, main);
            if (event.getHand() != InteractionHand.MAIN_HAND || a == null || !ExtendoGrip.active((Player)player, main)) {
                return;
            }
            ExtendoClient.grip(event, (AbstractClientPlayer)player, main, ExtendoClient.pose(a, event.getPartialTick()));
            event.setCanceled(true);
            return;
        }
        HumanoidArm side = LimbAttachments.armForHand((Player)player, event.getHand());
        if (side == null || !ExtendoGrip.active((Player)player, side)) {
            return;
        }
        Arm a = ExtendoClient.arm((Player)player, side);
        if (a == null || Mth.lerp((float)event.getPartialTick(), (float)a.oOpen, (float)a.open) <= 0.0f) {
            return;
        }
        if (item.isEmpty() || player.isUsingItem() && player.getUsedItemHand() == event.getHand() || item.is(Items.FILLED_MAP) || item.getItem() instanceof CrossbowItem) {
            return;
        }
        ExtendoClient.firstPerson(event, (AbstractClientPlayer)player, side, ExtendoClient.pose(a, event.getPartialTick()));
        event.setCanceled(true);
    }

    private static void firstPerson(RenderHandEvent event, AbstractClientPlayer player, HumanoidArm side, ExtendoRenderer.Pose pose) {
        Minecraft mc = Minecraft.getInstance();
        PoseStack stack = event.getPoseStack();
        ItemStack item = event.getItemStack();
        float f = side == HumanoidArm.RIGHT ? 1.0f : -1.0f;
        float t = event.getPartialTick();
        float equip = event.getEquipProgress();
        float swing = event.getSwingProgress();
        float e = ExtendoRenderer.ease(pose.open());
        Matrix4f arm = new Matrix4f().translate(0.0f, -(1.0f - e) * 0.4f, 0.0f).mul((Matrix4fc)ExtendoClient.gripArm(f, equip / 4.0f));
        Vector3f travel = ExtendoClient.drawArm(event, player, side, pose, arm);
        Vector3f anchor = new Vector3f(f * 0.56f, -0.52f + equip * -0.6f, -0.72f);
        Vector3f held = new Vector3f(f * 0.44f, -0.4f + equip / 4.0f * -0.6f, -0.72f);
        Vector3f offset = held.sub((Vector3fc)anchor).mul(e * e * e).add((Vector3fc)travel);
        stack.pushPose();
        stack.translate(offset.x, offset.y, offset.z);
        if (!IClientItemExtensions.of((ItemStack)item).applyForgeHandTransform(stack, mc.player, side, item, t, equip, swing)) {
            float settled = swing * (1.0f - e);
            float root = Mth.sqrt((float)settled);
            stack.translate(f * -0.4f * Mth.sin((float)(root * (float)Math.PI)), 0.2f * Mth.sin((float)(root * ((float)Math.PI * 2))), -0.2f * Mth.sin((float)(settled * (float)Math.PI)));
            stack.translate(anchor.x, anchor.y, anchor.z);
            float s = Mth.sin((float)(swing * swing * (float)Math.PI));
            float r = Mth.sin((float)(Mth.sqrt((float)swing) * (float)Math.PI));
            stack.mulPose(Axis.YP.rotationDegrees(f * (45.0f + s * -20.0f)));
            stack.mulPose(Axis.ZP.rotationDegrees(f * r * -20.0f));
            stack.mulPose(Axis.XP.rotationDegrees(r * -80.0f));
            stack.mulPose(Axis.YP.rotationDegrees(f * -45.0f));
        }
        mc.getEntityRenderDispatcher().getItemInHandRenderer().renderItem((LivingEntity)player, item, side == HumanoidArm.RIGHT ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, side == HumanoidArm.LEFT, stack, event.getMultiBufferSource(), event.getPackedLight());
        stack.popPose();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Vector3f drawArm(RenderHandEvent event, AbstractClientPlayer player, HumanoidArm side, ExtendoRenderer.Pose pose, Matrix4f arm) {
        PlayerRenderer skin = (PlayerRenderer)Minecraft.getInstance().getEntityRenderDispatcher().getRenderer((Entity)player);
        ModelPart part = side == HumanoidArm.LEFT ? ((PlayerModel)skin.getModel()).leftArm : ((PlayerModel)skin.getModel()).rightArm;
        float[] center = new float[]{side == HumanoidArm.RIGHT ? -1.0f : 1.0f};
        part.visit(new PoseStack(), (p, path, index, cube) -> {
            if (path.isEmpty() && index == 0) {
                center[0] = (cube.minX + cube.maxX) / 2.0f;
            }
        });
        PoseStack stack = event.getPoseStack();
        stack.pushPose();
        stack.mulPose(arm);
        drawing = player;
        drawingSide = side;
        drawn = pose;
        try {
            if (side == HumanoidArm.RIGHT) {
                skin.renderRightHand(stack, event.getMultiBufferSource(), event.getPackedLight(), player);
            } else {
                skin.renderLeftHand(stack, event.getMultiBufferSource(), event.getPackedLight(), player);
            }
        }
        finally {
            drawing = null;
            drawingSide = null;
            drawn = null;
            stack.popPose();
        }
        Vector3f hand = new Vector3f(center[0] / 16.0f, 0.53125f, 0.0f);
        return arm.transformPosition(new Vector3f((Vector3fc)hand).add(0.0f, ExtendoRenderer.shift(pose.extension()) / 16.0f, 0.0f)).sub((Vector3fc)arm.transformPosition(hand));
    }

    private static void grip(RenderHandEvent event, AbstractClientPlayer player, HumanoidArm side, ExtendoRenderer.Pose pose) {
        float equip;
        Minecraft mc = Minecraft.getInstance();
        PoseStack stack = event.getPoseStack();
        ItemStack held = event.getItemStack();
        ItemStack offhand = player.getOffhandItem();
        boolean inOffhand = ExtendoGrip.is(offhand);
        boolean right = side == HumanoidArm.RIGHT;
        boolean block = held.getItem() instanceof BlockItem;
        float f = right ? 1.0f : -1.0f;
        float swing = event.getSwingProgress();
        float f2 = equip = block ? 0.0f : event.getEquipProgress() / 4.0f;
        if (1.0f - swing > ExtendoGripRenderHandler.mainHandAnimation && swing > 0.0f) {
            ExtendoGripRenderHandler.mainHandAnimation = 0.95f;
        }
        float animation = Mth.lerp((float)event.getPartialTick(), (float)ExtendoGripRenderHandler.lastMainHandAnimation, (float)ExtendoGripRenderHandler.mainHandAnimation);
        animation = animation * animation * animation;
        Vector3f travel = ExtendoClient.drawArm(event, player, side, pose, ExtendoClient.gripArm(f, equip));
        ItemDisplayContext context = right ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        ItemInHandRenderer items = mc.getEntityRenderDispatcher().getItemInHandRenderer();
        stack.pushPose();
        stack.translate(travel.x, travel.y, travel.z);
        stack.translate(f * 0.54f, -0.4f + equip * -0.6f, -0.41999996f);
        stack.translate(f * -0.1f, 0.0f, -0.3f);
        items.renderItem((LivingEntity)player, inOffhand ? offhand : held, context, !right, stack, event.getMultiBufferSource(), event.getPackedLight());
        if (inOffhand) {
            ClientHooks.handleCameraTransforms((PoseStack)stack, (BakedModel)mc.getItemRenderer().getModel(offhand, null, null, 0), (ItemDisplayContext)context, (!right ? 1 : 0) != 0);
            stack.translate(f * -0.05f, 0.15f, -1.2f);
            stack.translate(0.0f, 0.0f, -animation * 2.25f);
            if (block && mc.getItemRenderer().getModel(held, null, null, 0).isGui3d()) {
                stack.mulPose(Axis.YP.rotationDegrees(f * 45.0f));
                stack.translate(f * 0.15f, -0.15f, -0.05f);
                stack.scale(1.25f, 1.25f, 1.25f);
            }
            items.renderItem((LivingEntity)player, held, context, !right, stack, event.getMultiBufferSource(), event.getPackedLight());
        }
        stack.popPose();
    }

    private static Matrix4f gripArm(float f, float equip) {
        Matrix4f m = new Matrix4f().translate(f * 0.54f, -0.4f + equip * -0.6f, -0.41999996f);
        m.rotate((Quaternionfc)Axis.YP.rotationDegrees(f * 75.0f));
        m.translate(f * -1.0f, 3.6f, 3.5f);
        m.rotate((Quaternionfc)Axis.ZP.rotationDegrees(f * 120.0f));
        m.rotate((Quaternionfc)Axis.XP.rotationDegrees(200.0f));
        m.rotate((Quaternionfc)Axis.YP.rotationDegrees(f * -135.0f));
        m.translate(f * 5.6f, 0.0f, 0.0f);
        m.rotate((Quaternionfc)Axis.YP.rotationDegrees(f * 40.0f));
        m.translate(f * 0.05f, -0.3f, -0.3f);
        return m.translate(f * -5.0f / 16.0f, 0.125f, 0.0f).rotate((Quaternionfc)Axis.ZP.rotation(f * 0.1f));
    }

    private static final class Arm {
        float out;
        float oOut;
        float open;
        float oOpen;

        private Arm() {
        }
    }
}

