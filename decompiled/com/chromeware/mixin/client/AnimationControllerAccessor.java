/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.zigythebird.playeranimcore.animation.AnimationController
 *  com.zigythebird.playeranimcore.bones.AdvancedPlayerAnimBone
 *  com.zigythebird.playeranimcore.bones.PlayerAnimBone
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package com.chromeware.mixin.client;

import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.bones.AdvancedPlayerAnimBone;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={AnimationController.class}, remap=false)
public interface AnimationControllerAccessor {
    @Accessor(value="bones")
    public Map<String, AdvancedPlayerAnimBone> chrome$registeredBones();

    @Accessor(value="activeBones")
    public Map<String, PlayerAnimBone> chrome$activeBones();
}

