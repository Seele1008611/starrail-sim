package com.starrail.sim;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Persistent cooldowns are separate from temporary combat state and survive cloning. */
public final class StarRailRankRuntime {
    private static final String KEY = "starrail_rank_runtime";
    private StarRailRankRuntime() {}

    public static long now(ServerPlayer player) {
        return player.server.overworld().getGameTime();
    }

    public static CompoundTag persistent(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG)) root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        CompoundTag saved = root.getCompound(Player.PERSISTED_NBT_TAG);
        if (!saved.contains(KEY)) saved.put(KEY, new CompoundTag());
        return saved.getCompound(KEY);
    }

    public static boolean ready(ServerPlayer player, String key) {
        return now(player) >= persistent(player).getLong(key);
    }

    public static void cooldown(ServerPlayer player, String key, long ticks) {
        persistent(player).putLong(key, now(player) + ticks);
    }

    public static void grantEffect(ServerPlayer player, net.minecraft.world.effect.MobEffectInstance granted) {
        var previous = player.getEffect(granted.getEffect());
        boolean keepsExisting = previous != null && previous.getAmplifier() >= granted.getAmplifier()
                && previous.getDuration() >= granted.getDuration();
        if (!player.addEffect(granted) || keepsExisting) return;
        var current = player.getEffect(granted.getEffect());
        if (current == null || current.getAmplifier() != granted.getAmplifier()
                || current.getDuration() != granted.getDuration()) return;
        CompoundTag root = player.getPersistentData().getCompound("starrail_owned_effects");
        CompoundTag entry = new CompoundTag();
        entry.putInt("amplifier", granted.getAmplifier());
        entry.putLong("until", now(player) + granted.getDuration());
        root.put(net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getKey(granted.getEffect()).toString(), entry);
        player.getPersistentData().put("starrail_owned_effects", root);
    }

    public static void clearEffects(ServerPlayer player) {
        CompoundTag root = player.getPersistentData().getCompound("starrail_owned_effects");
        for (String key : root.getAllKeys()) {
            var effect = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getValue(new net.minecraft.resources.ResourceLocation(key));
            var current = effect == null ? null : player.getEffect(effect);
            CompoundTag entry = root.getCompound(key);
            long remaining = entry.getLong("until") - now(player);
            if (current != null && current.getAmplifier() == entry.getInt("amplifier")
                    && Math.abs(current.getDuration() - remaining) <= 2) player.removeEffect(effect);
        }
        player.getPersistentData().remove("starrail_owned_effects");
    }

    public static void copy(Player original, Player replacement) {
        replacement.getPersistentData().put(Player.PERSISTED_NBT_TAG,
                original.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).copy());
        replacement.getPersistentData().put("starrail_trial_runtime",
                original.getPersistentData().getCompound("starrail_trial_runtime").copy());
        replacement.getPersistentData().putString("starrail_runtime_path",
                original.getPersistentData().getString("starrail_runtime_path"));
    }
}
