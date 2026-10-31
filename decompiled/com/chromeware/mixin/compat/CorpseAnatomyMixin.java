/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  de.maxhenkel.corpse.corelib.death.Death
 *  de.maxhenkel.corpse.entities.CorpseEntity
 *  net.minecraft.world.entity.player.Player
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.chromeware.mixin.compat;

import com.chromeware.body.CyberLimbs;
import com.chromeware.body.LimbAttachments;
import de.maxhenkel.corpse.corelib.death.Death;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets={"de.maxhenkel.corpse.entities.CorpseEntity"}, remap=false)
public abstract class CorpseAnatomyMixin {
    @Inject(method={"createFromDeath"}, at={@At(value="RETURN")})
    private static void chrome$anatomy(Player player, Death death, CallbackInfoReturnable<CorpseEntity> cir) {
        CorpseEntity corpse = (CorpseEntity)cir.getReturnValue();
        if (corpse == null) {
            return;
        }
        corpse.setData(LimbAttachments.LIMBS, (Object)LimbAttachments.functional(player));
        corpse.setData(CyberLimbs.FRAMES, (Object)CyberLimbs.get(player));
    }
}

