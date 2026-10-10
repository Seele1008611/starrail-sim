package com.starrail.sim;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/** Startup attribute ranges, server combat rules, and local HUD preferences. */
public final class StarRailStatusConfig {
    public static final ForgeConfigSpec COMMON, SERVER, CLIENT;
    public static final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> LIMITS;
    public static final ForgeConfigSpec.BooleanValue EXTEND_LIMITS, EXTENDED_DEFENSE, HUD, LEFT_CORNER, EXACT;
    public static final ForgeConfigSpec.DoubleValue SCALE, DEFENSE_CAP, DEFENSE_CURVE, TOUGHNESS_WEIGHT;
    static {
        var b = new ForgeConfigSpec.Builder();
        EXTEND_LIMITS = b.comment("Restart required. AttributeFix, when installed, owns vanilla ranges.")
                .define("extendVanillaLimits", true);
        LIMITS = b.comment("registry_id=maximum; applied on both physical sides before worlds load.")
                .defineList("attributeMaximums", java.util.List.of("minecraft:generic.max_health=1000000",
                        "minecraft:generic.armor=1000000", "minecraft:generic.armor_toughness=1000000",
                        "minecraft:generic.attack_damage=1000000", "minecraft:generic.attack_knockback=1000000"),
                        v -> v instanceof String s && s.matches("[a-z0-9_.-]+:[a-z0-9_./-]+=[0-9]+(?:\\.[0-9]+)?"));
        COMMON = b.build();
        b = new ForgeConfigSpec.Builder();
        EXTENDED_DEFENSE = b.comment("Players only. Vanilla damage tags, armor wear and downstream calculations remain in force.")
                .define("extendedPlayerDefense", true);
        DEFENSE_CAP = b.defineInRange("armorReductionCeiling", .95, .8, .99);
        DEFENSE_CURVE = b.defineInRange("excessArmorCurve", 80.0, 1.0, 1000000.0);
        TOUGHNESS_WEIGHT = b.defineInRange("excessToughnessWeight", .5, 0.0, 10.0);
        SERVER = b.build();
        b = new ForgeConfigSpec.Builder();
        HUD = b.comment("False restores all replaced vanilla survival overlays. Also toggled by the HUD key.")
                .define("playerStatusHud", true);
        LEFT_CORNER = b.define("leftCorner", false);
        EXACT = b.comment("Use full numbers instead of K/M abbreviations; fitted without dropping values.")
                .define("exactNumbers", false);
        SCALE = b.defineInRange("scale", 1.0, .75, 1.0);
        CLIENT = b.build();
    }
    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT);
    }
    private StarRailStatusConfig() {}
}
