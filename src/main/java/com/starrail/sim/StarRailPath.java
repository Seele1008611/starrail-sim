package com.starrail.sim;

import java.util.Arrays;

/** The nine playable paths planned for the first content phase. */
public enum StarRailPath {
    NONE("none", "未踏上命途"),
    PRESERVATION("preservation", "存护"),
    DESTRUCTION("destruction", "毁灭"),
    HUNT("hunt", "巡猎"),
    ERUDITION("erudition", "智识"),
    HARMONY("harmony", "同谐"),
    NIHILITY("nihility", "虚无"),
    ABUNDANCE("abundance", "丰饶"),
    REMEMBRANCE("remembrance", "记忆"),
    ELATION("elation", "欢愉");

    private final String id;
    private final String displayName;

    StarRailPath(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static StarRailPath byId(String id) {
        return Arrays.stream(values())
                .filter(path -> path.id.equalsIgnoreCase(id))
                .findFirst()
                .orElse(NONE);
    }

    public boolean isRealPath() {
        return this != NONE;
    }
}
