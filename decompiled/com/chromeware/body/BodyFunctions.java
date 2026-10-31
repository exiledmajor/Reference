/*
 * Decompiled with CFR 0.152.
 */
package com.chromeware.body;

import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbState;

public final class BodyFunctions {
    public static int usableHands(LimbState state) {
        return (state.isMissing(BodyPart.LEFT_HAND) ? 0 : 1) + (state.isMissing(BodyPart.RIGHT_HAND) ? 0 : 1);
    }

    public static Penalties penalties(LimbState state) {
        double speed;
        if (state.isMissing(BodyPart.BODY)) {
            return new Penalties(1.0, 1.0, 1.0, 0.0);
        }
        int legs = (state.isMissing(BodyPart.LEFT_LEG) ? 1 : 0) + (state.isMissing(BodyPart.RIGHT_LEG) ? 1 : 0);
        int feet = (state.isMissing(BodyPart.LEFT_FOOT) ? 1 : 0) + (state.isMissing(BodyPart.RIGHT_FOOT) ? 1 : 0);
        double d = legs == 2 ? 0.95 : (legs == 1 ? 0.45 : (feet == 2 ? 0.6 : (speed = feet == 1 ? 0.2 : 0.0)));
        double jump = legs == 2 ? 1.0 : (legs == 1 ? 0.6 : (feet == 2 ? 0.5 : (feet == 1 ? 0.2 : 0.0)));
        int hands = BodyFunctions.usableHands(state);
        return new Penalties(speed, jump, hands == 1 ? 0.25 : 0.0, hands == 0 ? 0.0 : (hands == 1 ? 0.75 : 1.0));
    }

    public static boolean canJump(LimbState state) {
        return BodyFunctions.penalties(state).jump() < 1.0;
    }

    public record Penalties(double speed, double jump, double attackSpeed, double mining) {
    }
}

