/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
 *  net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen$ItemPickerMenu
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.client;

import com.chromeware.ChromewareTabs;
import com.chromeware.client.cyberware.ImplantCategoryBanner;
import com.chromeware.client.cyberware.ImplantCreativeSections;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={CreativeModeInventoryScreen.class})
public abstract class ImplantCreativeScreenMixin
extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
    @Shadow
    private static CreativeModeTab selectedTab;
    @Shadow
    private float scrollOffs;
    @Unique
    private List<ImplantCreativeSections.Heading> chrome$headings = List.of();
    @Unique
    private int chrome$refreshRow;

    protected ImplantCreativeScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory inventory, Component title) {
        super((AbstractContainerMenu)menu, inventory, title);
    }

    @Inject(method={"selectTab"}, at={@At(value="TAIL")})
    private void chrome$sections(CreativeModeTab tab, CallbackInfo ci) {
        this.chrome$padSections();
    }

    @Inject(method={"refreshCurrentTabContents"}, at={@At(value="TAIL")})
    private void chrome$refreshSections(Collection<ItemStack> items, CallbackInfo ci) {
        this.chrome$padSections();
        if (selectedTab == ChromewareTabs.IMPLANTS.get()) {
            int rows = Math.max(0, (int)Math.ceil((double)((CreativeModeInventoryScreen.ItemPickerMenu)this.menu).items.size() / 9.0) - 5);
            this.scrollOffs = rows == 0 ? 0.0f : Math.clamp((float)((float)this.chrome$refreshRow / (float)rows), (float)0.0f, (float)1.0f);
            ((CreativeModeInventoryScreen.ItemPickerMenu)this.menu).scrollTo(this.scrollOffs);
        }
    }

    @Inject(method={"refreshCurrentTabContents"}, at={@At(value="HEAD")})
    private void chrome$rememberRow(Collection<ItemStack> items, CallbackInfo ci) {
        this.chrome$refreshRow = Math.max(0, (int)((double)(this.scrollOffs * (float)Math.max(0, (int)Math.ceil((double)((CreativeModeInventoryScreen.ItemPickerMenu)this.menu).items.size() / 9.0) - 5)) + 0.5));
    }

    @Unique
    private void chrome$padSections() {
        this.chrome$headings = List.of();
        if (selectedTab != ChromewareTabs.IMPLANTS.get()) {
            return;
        }
        ImplantCreativeSections.Layout layout = ImplantCreativeSections.layout((Collection<ItemStack>)((CreativeModeInventoryScreen.ItemPickerMenu)this.menu).items);
        ((CreativeModeInventoryScreen.ItemPickerMenu)this.menu).items.clear();
        ((CreativeModeInventoryScreen.ItemPickerMenu)this.menu).items.addAll(layout.items());
        this.chrome$headings = layout.headings();
        ((CreativeModeInventoryScreen.ItemPickerMenu)this.menu).scrollTo(this.scrollOffs);
    }

    @Inject(method={"renderLabels"}, at={@At(value="TAIL")})
    private void chrome$renderSections(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (selectedTab != ChromewareTabs.IMPLANTS.get()) {
            return;
        }
        int start = Math.max(0, (int)((double)(this.scrollOffs * (float)Math.max(0, (int)Math.ceil((double)((CreativeModeInventoryScreen.ItemPickerMenu)this.menu).items.size() / 9.0) - 5)) + 0.5));
        for (ImplantCreativeSections.Heading header : this.chrome$headings) {
            int row = header.row() - start;
            if (row < 0 || row >= 5) continue;
            int y = 17 + row * 18;
            ImplantCategoryBanner.render(graphics, this.font, 8, y, header.category());
        }
    }
}

