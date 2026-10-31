/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EntityDimensions
 *  net.minecraft.world.entity.player.Player
 */
package com.chromeware.body;

import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbRegrowth;
import com.chromeware.body.LimbState;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;

public final class BodyGeometry {
    public static final float MODEL_SCALE = 0.9375f;
    public static final float SEGMENT = 0.703125f;

    private static float growing(LimbRegrowth growth, BodyPart part, float start) {
        return growth.growing() == part ? start + (1.0f - start) * growth.progress() : 0.0f;
    }

    public static float legLength(LimbState state, LimbRegrowth growth, BodyPart leg, BodyPart foot) {
        if (state.isMissing(leg)) {
            return BodyGeometry.growing(growth, leg, 0.15f);
        }
        if (state.isMissing(foot)) {
            return growth.growing() == foot ? 0.75f + 0.25f * growth.progress() : 0.75f;
        }
        return 1.0f;
    }

    public static float drop(LimbState state, LimbRegrowth growth, boolean passenger) {
        if (passenger && !state.isMissing(BodyPart.BODY)) {
            return 0.0f;
        }
        float torso = state.isMissing(BodyPart.BODY) ? BodyGeometry.growing(growth, BodyPart.BODY, 0.15f) : 1.0f;
        float legs = Math.max(BodyGeometry.legLength(state, growth, BodyPart.LEFT_LEG, BodyPart.LEFT_FOOT), BodyGeometry.legLength(state, growth, BodyPart.RIGHT_LEG, BodyPart.RIGHT_FOOT));
        if (state.isMissing(BodyPart.BODY)) {
            legs = 0.0f;
        }
        return 0.703125f * (2.0f - torso - legs);
    }

    public static float drop(Player player) {
        if (player.getPersistentData().getBoolean("ChromeCorpse")) {
            return 0.0f;
        }
        return BodyGeometry.drop(LimbAttachments.functional(player), LimbRegrowth.get(player), player.isPassenger()) * player.getScale();
    }

    public static EntityDimensions dimensions(Player player, EntityDimensions original) {
        if (original.height() < 1.0f && !LimbAttachments.get(player).isMissing(BodyPart.BODY)) {
            return original;
        }
        float down = BodyGeometry.drop(player);
        if (down == 0.0f) {
            return original;
        }
        float min = Math.min(original.height(), 0.5f * player.getScale());
        float height = Math.max(min, original.height() - down);
        float eye = Math.max(0.12f * player.getScale(), Math.min(height - 0.05f, original.eyeHeight() - down));
        return new EntityDimensions(original.width(), height, eye, original.attachments(), original.fixed());
    }
}

