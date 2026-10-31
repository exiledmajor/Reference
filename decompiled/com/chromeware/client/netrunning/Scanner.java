/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  com.simibubi.create.AllSoundEvents
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.RenderLayerParent
 *  net.minecraft.client.renderer.entity.layers.RenderLayer
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Pre
 *  net.neoforged.neoforge.client.event.InputEvent$InteractionKeyMappingTriggered
 *  net.neoforged.neoforge.client.event.InputEvent$MouseScrollingEvent
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.joml.Matrix4f
 */
package com.chromeware.client.netrunning;

import com.chromeware.client.cyberware.KiroshiHighlights;
import com.chromeware.client.netrunning.NetrunningClient;
import com.chromeware.client.netrunning.ScannerHud;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.netrunning.DeviceHacks;
import com.chromeware.netrunning.Netrunning;
import com.chromeware.netrunning.NetrunningPackets;
import com.chromeware.netrunning.Quickhack;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.AllSoundEvents;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;

public final class Scanner {
    private static final int STICKY = 10;
    private static boolean active;
    private static boolean attackWasDown;
    private static Entity target;
    private static BlockPos device;
    private static int sticky;
    private static int openTicks;
    private static int denyTicks;
    private static int statusClock;
    private static String selected;

    public static boolean active() {
        return active;
    }

    public static int openTicks() {
        return openTicks;
    }

    public static int denyTicks() {
        return denyTicks;
    }

    public static Entity target() {
        return active && target != null && !target.isRemoved() && target.isAlive() ? target : null;
    }

    public static BlockPos device() {
        return active && Scanner.target() == null ? device : null;
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(Scanner::clicks);
        NeoForge.EVENT_BUS.addListener(Scanner::tick);
        NeoForge.EVENT_BUS.addListener(Scanner::scroll);
        NeoForge.EVENT_BUS.addListener(Scanner::click);
    }

    public static void toggle() {
        Minecraft mc = Minecraft.getInstance();
        if (active) {
            Scanner.close();
            return;
        }
        if (mc.player == null || !Netrunning.usable((Player)mc.player)) {
            if (mc.player != null && Netrunning.needsOptics((Player)mc.player)) {
                mc.player.displayClientMessage((Component)Component.translatable((String)"message.chromeware.netrunning.no_optics"), true);
            }
            Scanner.sound(AllSoundEvents.DENY.getMainEvent(), 1.0f, 0.5f);
            return;
        }
        active = true;
        openTicks = 0;
        target = null;
        device = null;
        sticky = 0;
        ScannerHud.opened();
        Scanner.request(5, 1);
        Scanner.sound(AllSoundEvents.CONTROLLER_CLICK.getMainEvent(), 1.2f, 0.6f);
    }

    public static void close() {
        if (!active) {
            return;
        }
        active = false;
        target = null;
        device = null;
        if (Minecraft.getInstance().getConnection() != null) {
            Scanner.request(5, 0);
        }
        Scanner.sound(AllSoundEvents.CONTROLLER_CLICK.getMainEvent(), 0.8f, 0.6f);
    }

    public static List<Quickhack> programs() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return List.of();
        }
        return Netrunning.state((Player)player).programs().stream().map(Quickhack::byId).toList();
    }

    public static int selectedIndex() {
        List<Quickhack> list = Scanner.programs();
        for (int i = 0; i < list.size(); ++i) {
            if (!list.get((int)i).id.equals(selected)) continue;
            return i;
        }
        return 0;
    }

    public static Quickhack selectedHack() {
        List<Quickhack> list = Scanner.programs();
        return list.isEmpty() ? null : list.get(Math.clamp((long)Scanner.selectedIndex(), (int)0, (int)(list.size() - 1)));
    }

    public static int cost(Quickhack hack) {
        CompoundTag costs = NetrunningClient.status.getCompound("costs");
        return costs.contains(hack.id) ? costs.getInt(hack.id) : hack.ram;
    }

    public static boolean applicable(Quickhack hack) {
        Entity t = Scanner.target();
        if (t != null) {
            return Netrunning.applicable(t, hack);
        }
        BlockPos pos = Scanner.device();
        return pos != null && DeviceHacks.applicable((Level)Minecraft.getInstance().level, pos, hack);
    }

    private static void clicks(ClientTickEvent.Pre event) {
        boolean mouse;
        Minecraft mc = Minecraft.getInstance();
        KeyMapping key = mc.options.keyAttack;
        boolean wasDown = attackWasDown;
        attackWasDown = key.isDown();
        if (!active || mc.screen != null || mc.player == null) {
            return;
        }
        int clicks = 0;
        while (key.consumeClick()) {
            ++clicks;
        }
        boolean bl = mouse = key.getKey().getType() == InputConstants.Type.MOUSE;
        if (clicks > 0 && (mouse || !wasDown)) {
            Scanner.upload();
        }
    }

    private static void tick(ClientTickEvent.Post event) {
        Quickhack hack;
        Minecraft mc = Minecraft.getInstance();
        if (denyTicks > 0) {
            --denyTicks;
        }
        if (!active) {
            return;
        }
        if (mc.player == null || mc.level == null || !Netrunning.usable((Player)mc.player)) {
            Scanner.close();
            return;
        }
        ++openTicks;
        Entity aimed = Scanner.aim();
        if (aimed != null) {
            if (aimed != target) {
                statusClock = 0;
            }
            target = aimed;
            sticky = 10;
            device = null;
        } else if (!(target == null || --sticky > 0 && Netrunning.targetAllowed((Player)mc.player, target))) {
            target = null;
            statusClock = 0;
        }
        if (target == null) {
            BlockPos pos = Scanner.aimDevice();
            if (pos != null && !pos.equals((Object)device)) {
                statusClock = 0;
            }
            device = pos;
        }
        String string = selected = (hack = Scanner.selectedHack()) == null ? null : hack.id;
        if (statusClock-- <= 0) {
            if (Scanner.device() != null) {
                PacketDistributor.sendToServer((CustomPacketPayload)new NetrunningPackets.DeviceAction(3, device, -1), (CustomPacketPayload[])new CustomPacketPayload[0]);
            } else {
                Scanner.request(3, -1);
            }
            statusClock = 5;
        }
    }

    private static void scroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!active || mc.screen != null || event.getScrollDeltaY() == 0.0) {
            return;
        }
        event.setCanceled(true);
        List<Quickhack> list = Scanner.programs();
        if (list.isEmpty()) {
            return;
        }
        int next = Math.clamp((long)(Scanner.selectedIndex() - (int)Math.signum(event.getScrollDeltaY())), (int)0, (int)(list.size() - 1));
        if (next == Scanner.selectedIndex()) {
            return;
        }
        selected = list.get((int)next).id;
        ScannerHud.brighten();
        Scanner.sound(AllSoundEvents.SCROLL_VALUE.getMainEvent(), 1.15f + 0.35f * (float)next / (float)Math.max(1, list.size() - 1), 0.25f);
    }

    private static void click(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (!active || mc.screen != null || !event.isAttack()) {
            return;
        }
        event.setSwingHand(false);
        event.setCanceled(true);
    }

    private static void upload() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        Quickhack hack = Scanner.selectedHack();
        if (p == null || hack == null || !Scanner.applicable(hack) || !Scanner.affordable((Player)p, hack)) {
            Scanner.deny();
            return;
        }
        BlockPos pos = Scanner.device();
        if (pos != null && hack.blocks()) {
            if (Scanner.queued() >= 4) {
                Scanner.deny();
                return;
            }
            PacketDistributor.sendToServer((CustomPacketPayload)new NetrunningPackets.DeviceAction(0, pos, hack.ordinal()), (CustomPacketPayload[])new CustomPacketPayload[0]);
        } else {
            Entity t = Scanner.target();
            if (t == null || !Netrunning.targetAllowed((Player)p, t) || Scanner.queued() >= 4) {
                Scanner.deny();
                return;
            }
            Scanner.request(0, hack.ordinal());
        }
        statusClock = 1;
        Scanner.sound(AllSoundEvents.CONFIRM.getMainEvent(), 1.3f, 0.5f);
    }

    public static boolean affordable(Player p, Quickhack hack) {
        double missing = (double)Scanner.cost(hack) - Netrunning.available(p);
        return missing <= 0.0 || Netrunning.state(p).overclock() > 0 && (double)p.getHealth() > missing;
    }

    private static void deny() {
        denyTicks = 12;
        Scanner.sound(AllSoundEvents.DENY.getMainEvent(), 1.0f, 0.5f);
    }

    public static int queued() {
        CompoundTag status = NetrunningClient.status;
        Entity t = Scanner.target();
        if (t != null) {
            return status.getInt("target") == t.getId() ? status.getList("queue", 10).size() : 0;
        }
        BlockPos d = Scanner.device();
        return d != null && status.contains("device") && status.getLong("device") == d.asLong() ? status.getList("queue", 10).size() : 0;
    }

    private static void request(int action, int hack) {
        Entity t = Scanner.target();
        PacketDistributor.sendToServer((CustomPacketPayload)new NetrunningPackets.Action(action, t == null ? -1 : t.getId(), hack), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    private static void sound(SoundEvent event, float pitch, float volume) {
        Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)event, (float)pitch, (float)volume));
    }

    private static Entity aim() {
        Vec3 look;
        Vec3 end;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        Vec3 eye = p.getEyePosition();
        BlockHitResult wall = mc.level.clip(new ClipContext(eye, end = eye.add((look = p.getLookAngle()).scale(32.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)p));
        double reach = wall.getType() == HitResult.Type.MISS ? 32.0 : wall.getLocation().distanceTo(eye) + 0.5;
        Entity best = null;
        double bestScore = Double.MAX_VALUE;
        double currentScore = Double.MAX_VALUE;
        for (Entity e2 : mc.level.getEntities((Entity)p, new AABB(eye, end).inflate(2.0), e -> e.isAlive() && !e.isRemoved())) {
            Vec3 center = e2.getBoundingBox().getCenter();
            double along = center.subtract(eye).dot(look);
            if (along <= 0.0 || along > reach) continue;
            AABB box = e2.getBoundingBox().inflate(0.25 + along * 0.02);
            Optional hit = box.clip(eye, end);
            if (!box.contains(eye) && hit.isEmpty()) continue;
            double off = center.subtract(eye.add(look.scale(along))).length() / Math.max(0.5, (double)e2.getBbWidth());
            double score = off * 4.0 + along * 0.05;
            if (!Netrunning.targetAllowed((Player)p, e2)) continue;
            if (e2 == target) {
                currentScore = score;
            }
            if (!(score < bestScore)) continue;
            best = e2;
            bestScore = score;
        }
        return currentScore < Double.MAX_VALUE && bestScore > currentScore * 0.6 ? target : best;
    }

    private static BlockPos aimDevice() {
        Vec3 look;
        Vec3 end;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        Vec3 eye = p.getEyePosition();
        BlockHitResult hit = mc.level.clip(new ClipContext(eye, end = eye.add((look = p.getLookAngle()).scale(32.0)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)p));
        if (hit instanceof BlockHitResult) {
            BlockHitResult block = hit;
            if (hit.getType() == HitResult.Type.BLOCK && DeviceHacks.target((Level)mc.level, block.getBlockPos())) {
                return block.getBlockPos().immutable();
            }
        }
        BlockPos best = null;
        double bestDot = 0.9985;
        for (BlockPos pos : KiroshiHighlights.revealed()) {
            double dot;
            Vec3 to = DeviceHacks.center((Level)mc.level, pos).subtract(eye);
            if (to.lengthSqr() > 1024.0 || !DeviceHacks.target((Level)mc.level, pos) || !((dot = to.normalize().dot(look)) > bestDot)) continue;
            best = pos;
            bestDot = dot;
        }
        return best;
    }

    public static class EyeLayer
    extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        private static final int COLOR = -4603;

        public EyeLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
            super(parent);
        }

        public void render(PoseStack stack, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbAmount, float partialTick, float age, float headYaw, float headPitch) {
            ModelPart head = ((PlayerModel)this.getParentModel()).head;
            if (!((Boolean)player.getData(PlayerData.DECK_OPEN)).booleanValue() || player.isInvisible() || !head.visible) {
                return;
            }
            VertexConsumer buffer = buffers.getBuffer(RenderType.debugQuads());
            stack.pushPose();
            head.translateAndRotate(stack);
            stack.translate(-0.125f, -0.21875f, -0.2875f);
            stack.scale(0.0625f, 0.0625f, 0.0625f);
            stack.pushPose();
            stack.mulPose(Axis.ZP.rotationDegrees(age * 6.0f));
            Matrix4f m = stack.last().pose();
            float o = 1.05f;
            float t = 0.3f;
            EyeLayer.quad(buffer, m, -o, -o, o, -o + t, 0.0f);
            EyeLayer.quad(buffer, m, -o, o - t, o, o, 0.0f);
            EyeLayer.quad(buffer, m, -o, -o + t, -o + t, o - t, 0.0f);
            EyeLayer.quad(buffer, m, o - t, -o + t, o, o - t, 0.0f);
            stack.popPose();
            stack.mulPose(Axis.ZP.rotationDegrees(-age * 12.0f));
            EyeLayer.quad(buffer, stack.last().pose(), -0.45f, -0.45f, 0.45f, 0.45f, -0.02f);
            stack.popPose();
        }

        private static void quad(VertexConsumer buffer, Matrix4f m, float x0, float y0, float x1, float y1, float z) {
            buffer.addVertex(m, x0, y0, z).setColor(-4603);
            buffer.addVertex(m, x0, y1, z).setColor(-4603);
            buffer.addVertex(m, x1, y1, z).setColor(-4603);
            buffer.addVertex(m, x1, y0, z).setColor(-4603);
        }
    }
}

