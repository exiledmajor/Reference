/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.zigythebird.playeranim.animation.PlayerAnimResources
 *  com.zigythebird.playeranimcore.animation.Animation
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.server.packs.resources.ResourceManagerReloadListener
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent
 *  net.neoforged.neoforge.client.extensions.common.IClientItemExtensions
 *  net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent
 */
package com.chromeware.client.cyberware;

import com.chromeware.client.cyberware.CyberwareRig;
import com.chromeware.client.cyberware.HardwareItemGeometry;
import com.chromeware.client.cyberware.ImplantTint;
import com.chromeware.cyberware.CyberwareItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranimcore.animation.Animation;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

public final class HardwareItems
extends BlockEntityWithoutLevelRenderer {
    private static final Map<HardwareItemGeometry.Kind, CyberwareRig> RIGS = new EnumMap<HardwareItemGeometry.Kind, CyberwareRig>(HardwareItemGeometry.Kind.class);
    private static final Map<HardwareItemGeometry.Kind, Function<String, CyberwareRig.Transform>> POSES = new EnumMap<HardwareItemGeometry.Kind, Function<String, CyberwareRig.Transform>>(HardwareItemGeometry.Kind.class);
    private static HardwareItems instance;

    private HardwareItems() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static void register(RegisterClientExtensionsEvent event) {
        IClientItemExtensions extensions = new IClientItemExtensions(){

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (instance == null) {
                    instance = new HardwareItems();
                }
                return instance;
            }
        };
        event.registerItem(extensions, new Item[]{(Item)CyberwareItems.MANTIS_BLADES.get(), (Item)CyberwareItems.MONOWIRE.get(), (Item)CyberwareItems.PROJECTILE_LAUNCH_SYSTEM.get()});
    }

    public static void registerReloads(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((PreparableReloadListener)((ResourceManagerReloadListener)manager -> {
            RIGS.clear();
            POSES.clear();
            for (HardwareItemGeometry.Kind kind : HardwareItemGeometry.Kind.values()) {
                try (InputStreamReader reader = new InputStreamReader(manager.getResourceOrThrow(ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)kind.rig())).open(), StandardCharsets.UTF_8);){
                    RIGS.put(kind, CyberwareRig.load(reader));
                }
                catch (Exception e) {
                    throw new IllegalStateException("Could not load the " + String.valueOf((Object)kind) + " item rig", e);
                }
            }
        }));
    }

    static HardwareItemGeometry.Kind kind(ItemStack stack) {
        if (stack.is(CyberwareItems.MANTIS_BLADES)) {
            return HardwareItemGeometry.Kind.MANTIS;
        }
        if (stack.is(CyberwareItems.MONOWIRE)) {
            return HardwareItemGeometry.Kind.MONOWIRE;
        }
        if (stack.is(CyberwareItems.PROJECTILE_LAUNCH_SYSTEM)) {
            return HardwareItemGeometry.Kind.LAUNCHER;
        }
        return null;
    }

    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        CyberwareRig rig;
        HardwareItemGeometry.Kind kind = HardwareItems.kind(stack);
        CyberwareRig cyberwareRig = rig = kind == null ? null : RIGS.get((Object)kind);
        if (rig == null) {
            return;
        }
        Function animation = POSES.computeIfAbsent(kind, k -> {
            Animation clip = PlayerAnimResources.getAnimationOptional((ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)k.clip())).orElse(null);
            return clip == null ? null : HardwareItemGeometry.pose(clip, 0.0f, rig.bones().stream().map(CyberwareRig.Bone::name).toList());
        });
        Function posed = animation == null ? name -> CyberwareRig.Transform.IDENTITY : animation;
        HardwareItemGeometry.draw(kind, rig, posed, pose, buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)ImplantTint.base(kind.implant()))), null, light, overlay);
        if (kind == HardwareItemGeometry.Kind.MONOWIRE) {
            HardwareItemGeometry.draw(kind, rig, posed, pose, new HardwareItemGeometry.Bounds(), buffers.getBuffer(RenderType.gui()), light, overlay);
        }
    }
}

