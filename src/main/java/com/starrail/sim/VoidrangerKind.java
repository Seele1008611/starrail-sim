package com.starrail.sim;

/** Stable rendering/behavior key for the four stage-2 voidrangers. */
public enum VoidrangerKind {
    REAVER("voidranger_reaver"),
    DISTORTER("voidranger_distorter"),
    TRAMPLER("voidranger_trampler"),
    ELIMINATOR("voidranger_eliminator");

    private final String id;

    VoidrangerKind(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }
}
