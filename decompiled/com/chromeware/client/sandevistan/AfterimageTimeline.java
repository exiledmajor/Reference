/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 */
package com.chromeware.client.sandevistan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import net.minecraft.util.Mth;

public final class AfterimageTimeline {
    public static final double SAMPLE_INTERVAL = 1.0;
    public static final double CLEAR_DURATION = 20.0;
    private final Map<UUID, Session> sessions = new HashMap<UUID, Session>();
    private double now;
    private long generation;

    public void update(double time, Set<UUID> activeOwners) {
        this.now = Math.max(this.now, time);
        for (UUID owner : activeOwners) {
            Session session = this.sessions.get(owner);
            if (session != null && session.active) continue;
            if (session != null) {
                session.clear();
            }
            this.sessions.put(owner, new Session(owner, this.now, ++this.generation));
        }
        for (Session session : this.sessions.values()) {
            if (activeOwners.contains(session.owner)) continue;
            if (session.active) {
                session.active = false;
                session.stopped = this.now;
                session.retiring = List.copyOf(session.frames.values());
            }
            double progress = Math.clamp((double)((this.now - session.stopped) / 20.0), (double)0.0, (double)1.0);
            int count = (int)Math.floor(progress * (double)session.retiring.size());
            for (int i = 0; i < count; ++i) {
                session.remove(session.retiring.get(i));
            }
        }
        this.sessions.entrySet().removeIf(e -> !((Session)e.getValue()).active && ((Session)e.getValue()).frames.isEmpty());
    }

    public Frame frame(UUID owner) {
        Session session = this.sessions.get(owner);
        if (session == null || !session.active) {
            return null;
        }
        long slot = (long)Math.floor((this.now - session.started) / 1.0);
        return session.frames.computeIfAbsent(slot, key -> new Frame((long)key, session.started + (double)key.longValue() * 1.0));
    }

    public long slot(UUID owner) {
        Session session = this.sessions.get(owner);
        return session == null || !session.active ? -1L : (long)Math.floor((this.now - session.started) / 1.0);
    }

    public long generation(UUID owner) {
        Session session = this.sessions.get(owner);
        return session == null ? -1L : session.generation;
    }

    public void add(Frame frame, int vertices) {
        if (frame == null || !frame.alive) {
            return;
        }
        frame.vertices += (long)Math.max(0, vertices);
    }

    public long vertices() {
        return this.sessions.values().stream().mapToLong(Session::vertices).sum();
    }

    public int frameCount() {
        return this.sessions.values().stream().mapToInt(s -> s.frames.size()).sum();
    }

    public void clear() {
        this.sessions.values().forEach(Session::clear);
        this.sessions.clear();
        this.now = 0.0;
    }

    private static final class Session {
        final UUID owner;
        final double started;
        final long generation;
        final NavigableMap<Long, Frame> frames = new TreeMap<Long, Frame>();
        List<Frame> retiring = List.of();
        double stopped = Double.NaN;
        boolean active = true;

        Session(UUID owner, double started, long generation) {
            this.owner = owner;
            this.started = started;
            this.generation = generation;
        }

        void remove(Frame frame) {
            frame.alive = false;
            this.frames.remove(frame.slot, frame);
        }

        void clear() {
            this.frames.values().forEach(f -> {
                f.alive = false;
            });
            this.frames.clear();
        }

        long vertices() {
            return this.frames.values().stream().mapToLong(f -> f.vertices).sum();
        }
    }

    public static final class Frame {
        private final long slot;
        private final double time;
        private final int color;
        private boolean alive = true;
        private long vertices;

        private Frame(long slot, double time) {
            this.slot = slot;
            this.time = time;
            this.color = Mth.hsvToRgb((float)((float)((105.0 + (double)(slot % 96L) * 3.75) % 360.0 / 360.0)), (float)0.72f, (float)1.0f);
        }

        public boolean alive() {
            return this.alive;
        }

        public long slot() {
            return this.slot;
        }

        public double time() {
            return this.time;
        }

        public int color() {
            return this.color;
        }
    }
}

