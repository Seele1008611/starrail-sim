package com.starrail.sim;

import net.minecraft.network.chat.Component;

/** The five relic sets currently available from the arena prototype selector. */
public enum RelicTrialSet {
    MUSKETEER("musketeer", false),
    PASSERBY("passerby", false),
    KNIGHT("knight", false),
    SPACE_STATION("space_station", true),
    FLEET("fleet", true);

    private final String id;
    private final boolean inner;

    RelicTrialSet(String id, boolean inner) {
        this.id = id;
        this.inner = inner;
    }

    public String id() {
        return id;
    }

    public boolean inner() {
        return inner;
    }

    public Component displayName() {
        return Component.translatable("ui.starrail_sim.relic.set." + id);
    }

    public static RelicTrialSet byIndex(int index) {
        RelicTrialSet[] values = values();
        return values[Math.max(0, Math.min(values.length - 1, index))];
    }

    public static int clampIndex(int index) {
        return Math.max(0, Math.min(values().length - 1, index));
    }
}
