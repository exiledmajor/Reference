/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.chromeware.mixin;

import com.chromeware.cyberware.TitaniumBones;
import com.chromeware.cyberware.TitaniumStackAccess;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.lang.ref.WeakReference;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ItemStack.class})
public abstract class TitaniumItemStackMixin
implements TitaniumStackAccess {
    @Unique
    private WeakReference<Player> chrome$owner;

    @Override
    public Player chrome$inventoryOwner() {
        return this.chrome$owner == null ? null : (Player)this.chrome$owner.get();
    }

    @Override
    public void chrome$inventoryOwner(Player p) {
        this.chrome$owner = p == null ? null : new WeakReference<Player>(p);
    }

    @ModifyReturnValue(method={"getMaxStackSize"}, at={@At(value="RETURN")})
    private int chrome$personalLimit(int original) {
        ItemStack stack = (ItemStack)this;
        return TitaniumBones.active(this.chrome$inventoryOwner()) && !stack.isDamageableItem() ? TitaniumBones.capacity(original) : original;
    }

    @ModifyReturnValue(method={"copy"}, at={@At(value="RETURN")})
    private ItemStack chrome$copyOwner(ItemStack copy) {
        TitaniumBones.bind(copy, this.chrome$inventoryOwner());
        return copy;
    }
}

