package com.starrail.sim;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

/** Modifies existing ranges once, before attribute instances are used by a world. */
public final class StarRailAttributeLimits {
    public static void apply() {
        if (!StarRailStatusConfig.EXTEND_LIMITS.get() || ModList.get().isLoaded("attributefix")) return;
        // Verified 1.20.1 SRG field; the helper also resolves the development mapping.
        var field = ObfuscationReflectionHelper.findField(RangedAttribute.class, "f_22308_");
        for (String entry : StarRailStatusConfig.LIMITS.get()) {
            String[] parts = entry.split("=", 2);
            var attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(parts[0]));
            if (!(attribute instanceof RangedAttribute ranged)) {
                LogUtils.getLogger().warn("Ignoring unknown/range-free attribute limit: {}", entry);
                continue;
            }
            // The mod's six custom caps are fixed separately, not overridden by this vanilla table.
            if (parts[0].startsWith(StarRailSimMod.MOD_ID + ":")) continue;
            double maximum = Double.parseDouble(parts[1]);
            if (!Double.isFinite(maximum) || maximum < ranged.getMinValue()
                    || maximum < ranged.getDefaultValue()) {
                LogUtils.getLogger().warn("Ignoring invalid attribute maximum: {}", entry);
                continue;
            }
            try { field.setDouble(ranged, maximum); }
            catch (IllegalAccessException e) { throw new IllegalStateException("Cannot extend " + parts[0], e); }
        }
    }
    private StarRailAttributeLimits() {}
}
