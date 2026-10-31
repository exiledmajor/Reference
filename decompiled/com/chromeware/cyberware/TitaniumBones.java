/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.EntityJoinLevelEvent
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.chromeware.cyberware;

import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.TitaniumStackAccess;
import java.util.ArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="chromeware")
public final class TitaniumBones {
    public static boolean active(Player p) {
        return p != null && Implant.TITANIUM_BONES.active(p);
    }

    public static int base(ItemStack stack) {
        return stack.getItem().getMaxStackSize(stack);
    }

    public static int capacity(int base) {
        return base <= 1 ? base : Math.max(base, Math.min(99, base + (int)Math.floor((double)base * ImplantSettings.stat("titanium_bones", "stack_capacity_bonus_fraction", 0.5))));
    }

    public static int limit(Player p, ItemStack stack) {
        return TitaniumBones.active(p) && !stack.isDamageableItem() ? TitaniumBones.capacity(TitaniumBones.base(stack)) : TitaniumBones.base(stack);
    }

    public static void bind(ItemStack stack, Player p) {
        if (stack != ItemStack.EMPTY) {
            ((TitaniumStackAccess)stack).chrome$inventoryOwner(p);
        }
    }

    public static Player owner(ItemStack stack) {
        return ((TitaniumStackAccess)stack).chrome$inventoryOwner();
    }

    public static Player owner(AbstractContainerMenu menu) {
        for (Slot slot : menu.slots) {
            Container container = slot.container;
            if (!(container instanceof Inventory)) continue;
            Inventory inventory = (Inventory)container;
            return inventory.player;
        }
        return null;
    }

    public static void refresh(Player p) {
        for (ItemStack stack : p.getInventory().items) {
            TitaniumBones.bind(stack, p);
        }
        for (ItemStack stack : p.getInventory().offhand) {
            TitaniumBones.bind(stack, p);
        }
        TitaniumBones.bind(p.containerMenu.getCarried(), p);
    }

    public static void reconcile(Player p) {
        TitaniumBones.refresh(p);
        if (p.level().isClientSide()) {
            return;
        }
        ArrayList<ItemStack> excess = new ArrayList<ItemStack>();
        for (int i = 0; i < p.getInventory().getContainerSize(); ++i) {
            ItemStack stack = p.getInventory().getItem(i);
            int limit = TitaniumBones.limit(p, stack);
            if (stack.getCount() <= limit) continue;
            excess.add(stack.split(stack.getCount() - limit));
        }
        ItemStack carried = p.containerMenu.getCarried();
        int limit = TitaniumBones.limit(p, carried);
        if (carried.getCount() > limit) {
            excess.add(carried.split(carried.getCount() - limit));
        }
        for (ItemStack stack : excess) {
            int count = stack.getCount();
            for (int i = 0; i < 36 && count > 0; ++i) {
                int room;
                int moved;
                ItemStack dest = p.getInventory().getItem(i);
                if (!dest.isEmpty() && !ItemStack.isSameItemSameComponents((ItemStack)stack, (ItemStack)dest) || (moved = Math.min(count, Math.max(0, room = TitaniumBones.limit(p, stack) - dest.getCount()))) == 0) continue;
                if (dest.isEmpty()) {
                    p.getInventory().setItem(i, stack.copyWithCount(moved));
                } else {
                    dest.grow(moved);
                }
                count -= moved;
            }
            if (count <= 0) continue;
            p.drop(stack.copyWithCount(count), false);
        }
        if (!excess.isEmpty()) {
            p.getInventory().setChanged();
        }
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        TitaniumBones.reconcile(event.getEntity());
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void spawned(EntityJoinLevelEvent event) {
        Entity entity;
        if (event.getLevel().isClientSide() || !((entity = event.getEntity()) instanceof ItemEntity)) {
            return;
        }
        ItemEntity entity2 = (ItemEntity)entity;
        ItemStack stack = entity2.getItem();
        TitaniumBones.bind(stack, null);
        int limit = TitaniumBones.base(stack);
        if (stack.getCount() <= limit) {
            return;
        }
        int count = stack.getCount();
        entity2.setItem(stack.copyWithCount(limit));
        CompoundTag saved = entity2.saveWithoutId(new CompoundTag());
        saved.remove("UUID");
        count -= limit;
        while (count > 0) {
            ItemEntity part = new ItemEntity(EntityType.ITEM, event.getLevel());
            part.load(saved);
            part.setItem(stack.copyWithCount(Math.min(count, limit)));
            TitaniumBones.bind(part.getItem(), null);
            event.getLevel().addFreshEntity((Entity)part);
            count -= limit;
        }
    }
}

