package com.starrail.sim;

import net.minecraft.network.chat.Component;

/** Relic sets available from the arena selector, kept in lockstep with RelicStackData.SetId. */
public enum RelicTrialSet {
    MUSKETEER("musketeer", false),
    PASSERBY("passerby", false),
    KNIGHT("knight", false),
    SPACE_STATION("space_station", true),
    FLEET("fleet", true),
    GUARD("guard", false),
    GENIUS("genius", false),
    DEAD_WATER_PIONEER("dead_water_pioneer", false),
    IRON_CAVALRY("iron_cavalry", false),
    VALOROUS("valorous", false),
    SCHOLAR("scholar", false),
    HERO("hero", false),
    SAVIOR("savior", false),
    MAGICAL_GIRL("magical_girl", false),
    NAVIGATOR("navigator", false),
    BELOBOG("belobog", true),
    PENACONY("penacony", true),
    AMPHOREUS("amphoreus", true),
    LIVE_STREAM("live_stream", true);

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
