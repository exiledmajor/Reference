/*
 * Decompiled with CFR 0.152.
 */
package com.chromeware.update;

import com.chromeware.update.MissedReleases;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class UpdatePrompt {
    private final CompletableFuture<Optional<MissedReleases.Result>> result;
    private boolean handled;

    public UpdatePrompt(CompletableFuture<Optional<MissedReleases.Result>> result) {
        this.result = result;
    }

    public Optional<MissedReleases.Result> poll(boolean titleReady) {
        if (this.handled || !titleReady || !this.result.isDone()) {
            return Optional.empty();
        }
        this.handled = true;
        return this.result.getNow(Optional.empty());
    }
}

