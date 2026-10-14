package com.starrail.sim;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

/** Server-side effects for the three 1.15.0 light cones. */
public final class StarRail15LightConeService {
    private static final int SONG_DURATION = 20 * 20;
    private static final int INK_DURATION = 10 * 20;
    private static final int WIND_DURATION = 3 * 20;
    private static final int TIDE_DURATION = 10 * 20;

    private static final String SONG_UNTIL = "starrail_15_song_until";
    private static final String SONG_LEVEL = "starrail_15_song_level";
    private static final String INK_UNTIL = "starrail_15_ink_until";
    private static final String INK_LEVEL = "starrail_15_ink_level";
    private static final String INK_SOURCE = "starrail_15_ink_source";
    private static final String WIND_UNTIL = "starrail_15_wind_until";
    private static final String WIND_LEVEL = "starrail_15_wind_level";
    private static final String TIDE_UNTIL = "starrail_15_tide_until";
    private static final String TIDE_LEVEL = "starrail_15_tide_level";

    private static final UUID SONG_HEALTH = id("0001");
    private static final UUID SONG_ATTACK = id("0002");
    private static final UUID SONG_AURA_ATTACK = id("0003");
    private static final UUID COLOR_ARMOR = id("0004");
    private static final UUID SUMMER_CRIT = id("0005");
    private static final UUID SUMMER_ATTACK_SPEED = id("0006");
    private static final UUID SUMMER_TIDE_ATTACK = id("0007");

    private static final double[] SONG_HEALTH_VALUES = {.30D, .375D, .45D, .525D, .60D};
    private static final double[] SONG_ATTACK_VALUES = {.10D, .125D, .15D, .175D, .20D};
    private static final double[] SONG_AURA_VALUES = {.20D, .25D, .30D, .35D, .40D};
    private static final double[] COLOR_ARMOR_VALUES = {.28D, .36D, .48D, .56D, .68D};
    private static final double[] COLOR_DAMAGE_VALUES = {.12D, .14D, .16D, .18D, .20D};
    private static final double[] COLOR_HEAL_VALUES = {.10D, .12D, .15D, .17D, .20D};
    private static final double[] SUMMER_CRIT_VALUES = {.08D, .11D, .14D, .17D, .20D};
    private static final double[] SUMMER_SPEED_VALUES = {.24D, .28D, .32D, .36D, .40D};
    private static final double[] SUMMER_ATTACK_VALUES = {.10D, .20D, .30D, .40D, .50D};

    private StarRail15LightConeService() { }

    public static void refresh(ServerPlayer player) {
        ItemStack song = equipped(player, StarRailSimMod.YOU_WILL_RISE_AND_SING);
        ItemStack colors = equipped(player, StarRailSimMod.COLORS_FOR_TOMORROW);
        ItemStack summer = equipped(player, StarRailSimMod.SUMMERTIME_TO_THE_WAVES);
        boolean remembrance = onPath(player, StarRailPath.REMEMBRANCE);
        boolean elation = onPath(player, StarRailPath.ELATION);
        int songLevel = song == null ? 1 : YouWillRiseAndSingItem.getSuperimposition(song);
        int colorsLevel = colors == null ? 1 : ColorsForTomorrowItem.getSuperimposition(colors);
        int summerLevel = summer == null ? 1 : SummertimeToTheWavesItem.getSuperimposition(summer);
        CompoundTag data = player.getPersistentData();
        long now = player.level().getGameTime();

        if (song == null || !remembrance) {
            clear(data, SONG_UNTIL, SONG_LEVEL);
        }
        if (summer == null || !elation) {
            clear(data, WIND_UNTIL, WIND_LEVEL, TIDE_UNTIL, TIDE_LEVEL);
        }

        boolean songActive = song != null && remembrance;
        boolean colorActive = colors != null && elation;
        boolean summerActive = summer != null && elation;
        boolean songBuff = songActive && data.getLong(SONG_UNTIL) > now;
        boolean windBuff = summerActive && data.getLong(WIND_UNTIL) > now;
        boolean tideBuff = summerActive && data.getLong(TIDE_UNTIL) > now;

        apply(player, Attributes.MAX_HEALTH, SONG_HEALTH, "starrail_sim.15_song.health",
                songActive ? SONG_HEALTH_VALUES[songLevel - 1] : 0,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        apply(player, Attributes.ATTACK_DAMAGE, SONG_ATTACK, "starrail_sim.15_song.attack",
                songBuff ? SONG_ATTACK_VALUES[level(data, SONG_LEVEL) - 1] : 0,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        double aura = strongestNearbySong(player, now);
        apply(player, Attributes.ATTACK_DAMAGE, SONG_AURA_ATTACK,
                "starrail_sim.15_song.aura_attack", aura,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        apply(player, Attributes.ARMOR, COLOR_ARMOR, "starrail_sim.15_colors.armor",
                colorActive ? COLOR_ARMOR_VALUES[colorsLevel - 1] : 0,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        apply(player, StarRailAttributes.CRIT_RATE, SUMMER_CRIT, "starrail_sim.15_summer.crit",
                summerActive ? SUMMER_CRIT_VALUES[summerLevel - 1] : 0,
                AttributeModifier.Operation.ADDITION);
        apply(player, Attributes.ATTACK_SPEED, SUMMER_ATTACK_SPEED,
                "starrail_sim.15_summer.attack_speed",
                windBuff ? SUMMER_SPEED_VALUES[level(data, WIND_LEVEL) - 1] : 0,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        apply(player, Attributes.ATTACK_DAMAGE, SUMMER_TIDE_ATTACK,
                "starrail_sim.15_summer.tide_attack",
                tideBuff ? SUMMER_ATTACK_VALUES[level(data, TIDE_LEVEL) - 1] : 0,
                AttributeModifier.Operation.MULTIPLY_TOTAL);

        if (data.getLong(SONG_UNTIL) <= now) clear(data, SONG_UNTIL, SONG_LEVEL);
        if (data.getLong(WIND_UNTIL) <= now) clear(data, WIND_UNTIL, WIND_LEVEL);
        if (data.getLong(TIDE_UNTIL) <= now) clear(data, TIDE_UNTIL, TIDE_LEVEL);
        activeInkDamage(player);
    }

    public static void onMonsterKilled(ServerPlayer player) {
        long now = player.level().getGameTime();
        ItemStack song = equipped(player, StarRailSimMod.YOU_WILL_RISE_AND_SING);
        if (song != null && onPath(player, StarRailPath.REMEMBRANCE)) {
            player.getPersistentData().putInt(SONG_LEVEL,
                    YouWillRiseAndSingItem.getSuperimposition(song));
            player.getPersistentData().putLong(SONG_UNTIL, now + SONG_DURATION);
        }

        ItemStack colors = equipped(player, StarRailSimMod.COLORS_FOR_TOMORROW);
        if (colors != null && onPath(player, StarRailPath.ELATION)) {
            triggerColors(player, ColorsForTomorrowItem.getSuperimposition(colors), now);
        }

        ItemStack summer = equipped(player, StarRailSimMod.SUMMERTIME_TO_THE_WAVES);
        if (summer != null && onPath(player, StarRailPath.ELATION)) {
            player.getPersistentData().putInt(TIDE_LEVEL,
                    SummertimeToTheWavesItem.getSuperimposition(summer));
            player.getPersistentData().putLong(TIDE_UNTIL, now + TIDE_DURATION);
        }
        refresh(player);
    }

    public static void onAttack(ServerPlayer player) {
        ItemStack summer = equipped(player, StarRailSimMod.SUMMERTIME_TO_THE_WAVES);
        if (summer == null || !onPath(player, StarRailPath.ELATION)) return;
        player.getPersistentData().putInt(WIND_LEVEL,
                SummertimeToTheWavesItem.getSuperimposition(summer));
        player.getPersistentData().putLong(WIND_UNTIL,
                player.level().getGameTime() + WIND_DURATION);
        refresh(player);
    }

    public static double damageMultiplier(ServerPlayer player) {
        int level = activeInkDamage(player);
        return level == 0 ? 1.0D : 1.0D + COLOR_DAMAGE_VALUES[level - 1];
    }

    public static double songHealth(int level) { return SONG_HEALTH_VALUES[bounded(level)]; }
    public static double songAttack(int level) { return SONG_ATTACK_VALUES[bounded(level)]; }
    public static double songAura(int level) { return SONG_AURA_VALUES[bounded(level)]; }
    public static double colorsArmor(int level) { return COLOR_ARMOR_VALUES[bounded(level)]; }
    public static double colorsDamage(int level) { return COLOR_DAMAGE_VALUES[bounded(level)]; }
    public static double colorsHeal(int level) { return COLOR_HEAL_VALUES[bounded(level)]; }
    public static double summerCrit(int level) { return SUMMER_CRIT_VALUES[bounded(level)]; }
    public static double summerSpeed(int level) { return SUMMER_SPEED_VALUES[bounded(level)]; }
    public static double summerAttack(int level) { return SUMMER_ATTACK_VALUES[bounded(level)]; }

    private static void triggerColors(ServerPlayer wearer, int level, long now) {
        double heal = Math.max(0.0D, wearer.getAttributeValue(Attributes.ARMOR)
                * COLOR_HEAL_VALUES[level - 1]);
        AABB area = wearer.getBoundingBox().inflate(10.0D);
        for (ServerPlayer recipient : wearer.serverLevel().getEntitiesOfClass(
                ServerPlayer.class, area,
                target -> target.isAlive() && target.distanceToSqr(wearer) <= 100.0D)) {
            CompoundTag data = recipient.getPersistentData();
            data.putLong(INK_UNTIL, now + INK_DURATION);
            data.putInt(INK_LEVEL, level);
            data.putUUID(INK_SOURCE, wearer.getUUID());
            recipient.heal((float) heal);
        }
    }

    private static double strongestNearbySong(ServerPlayer recipient, long now) {
        double strongest = 0.0D;
        for (ServerPlayer source : recipient.serverLevel().getEntitiesOfClass(
                ServerPlayer.class, recipient.getBoundingBox().inflate(10.0D),
                candidate -> candidate.isAlive() && candidate.distanceToSqr(recipient) <= 100.0D)) {
            if (!isActiveSongSource(source, now)) continue;
            int level = level(source.getPersistentData(), SONG_LEVEL);
            strongest = Math.max(strongest, SONG_AURA_VALUES[level - 1]);
        }
        return strongest;
    }

    private static boolean isActiveSongSource(ServerPlayer source, long now) {
        return equipped(source, StarRailSimMod.YOU_WILL_RISE_AND_SING) != null
                && onPath(source, StarRailPath.REMEMBRANCE)
                && source.getPersistentData().getLong(SONG_UNTIL) > now;
    }

    private static int activeInkDamage(ServerPlayer recipient) {
        CompoundTag data = recipient.getPersistentData();
        long now = recipient.level().getGameTime();
        if (data.getLong(INK_UNTIL) <= now || !data.hasUUID(INK_SOURCE)) {
            clear(data, INK_UNTIL, INK_LEVEL, INK_SOURCE);
            return 0;
        }
        ServerPlayer source = recipient.serverLevel().getServer().getPlayerList()
                .getPlayer(data.getUUID(INK_SOURCE));
        if (source == null || source.serverLevel() != recipient.serverLevel()
                || equipped(source, StarRailSimMod.COLORS_FOR_TOMORROW) == null
                || !onPath(source, StarRailPath.ELATION)) {
            clear(data, INK_UNTIL, INK_LEVEL, INK_SOURCE);
            return 0;
        }
        return level(data, INK_LEVEL);
    }

    private static boolean onPath(ServerPlayer player, StarRailPath path) {
        return player.getCapability(StarRailPathCapability.PATH_DATA)
                .map(data -> data.getCurrentPath() == path).orElse(false);
    }

    private static ItemStack equipped(ServerPlayer player,
                                      RegistryObject<? extends net.minecraft.world.item.Item> item) {
        return CuriosApi.getCuriosHelper().findFirstCurio(player, item.get())
                .map(result -> result.stack()).orElse(null);
    }

    private static void apply(Player player, RegistryObject<Attribute> attribute, UUID id,
                              String name, double amount,
                              AttributeModifier.Operation operation) {
        apply(player, attribute.get(), id, name, amount, operation);
    }

    private static void apply(Player player, Attribute attribute, UUID id, String name,
                              double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        AttributeModifier existing = instance.getModifier(id);
        if (amount == 0.0D) {
            if (existing != null) instance.removeModifier(existing);
            return;
        }
        if (existing != null && Double.compare(existing.getAmount(), amount) == 0
                && existing.getOperation() == operation) return;
        if (existing != null) instance.removeModifier(existing);
        instance.addTransientModifier(new AttributeModifier(id, name, amount, operation));
    }

    private static int level(CompoundTag data, String key) {
        return Math.max(1, Math.min(5, data.getInt(key)));
    }

    private static int bounded(int level) {
        return Math.max(1, Math.min(5, level)) - 1;
    }

    private static void clear(CompoundTag data, String... keys) {
        for (String key : keys) data.remove(key);
    }

    private static UUID id(String suffix) {
        return UUID.fromString("a1151500-0000-4000-8000-00000000" + suffix);
    }
}
