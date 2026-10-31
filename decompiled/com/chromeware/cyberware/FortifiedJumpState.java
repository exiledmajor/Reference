/*
 * Decompiled with CFR 0.152.
 */
package com.chromeware.cyberware;

import com.chromeware.config.ImplantSettings;

public final class FortifiedJumpState {
    public static final int MAX_CHARGE = 40;
    private boolean held;
    private boolean airborne;
    private int charge = -1;

    public void press(boolean grounded) {
        if (this.held) {
            return;
        }
        this.held = true;
        this.airborne = !grounded;
        this.charge = -1;
    }

    public void tick(boolean grounded) {
        if (!this.held) {
            return;
        }
        if (!grounded) {
            this.airborne = true;
            this.charge = -1;
        } else if (this.airborne) {
            this.charge = Math.min(ImplantSettings.ticks("fortified_ankles", "charge_seconds", 40), Math.max(0, this.charge) + 1);
        }
    }

    public boolean held() {
        return this.held;
    }

    public boolean charging() {
        return this.charge >= 0;
    }

    public double release(boolean grounded) {
        double velocity = grounded && this.charging() ? ImplantSettings.stat("fortified_ankles", "base_jump_velocity", 0.42) + ImplantSettings.stat("fortified_ankles", "charged_jump_velocity_bonus", 0.58) * (double)this.charge / (double)ImplantSettings.ticks("fortified_ankles", "charge_seconds", 40) : 0.0;
        this.cancel();
        return velocity;
    }

    public void cancel() {
        this.held = false;
        this.airborne = false;
        this.charge = -1;
    }
}

