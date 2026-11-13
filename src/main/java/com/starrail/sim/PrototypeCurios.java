package com.starrail.sim;

import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID)
public final class PrototypeCurios {
    public static final String SLOT = "path_curio";
    public static final int SLOT_COUNT = 3;
    private static final TagKey<EntityType<?>> BOSSES = TagKey.create(Registries.ENTITY_TYPE,
            new ResourceLocation("forge", "bosses"));
    static final String LAST_COMBAT = "starrail_curio_last_combat";
    private static final String NEXT_HEAL = "starrail_curio_next_heal";

    enum Effect {
        ROBE(-1), KNOT(-1), MODEL(-1), SCISSORS(-1), WHEEL(90), PLAGUE(90), ENGINE(-1), REUNION(-1), CUSTOM(-1), COLLECTION(-1), HERO(180), TAIL(-1), CALM(-1), CODE(-1), WINDOW(0), SEAL(-1), BEAUTY(-1), COMPASS(120), NEMESIS(-1), DEBT(90), BOX(0), REPAIR(0), STAR(-1), BOOK(120), MACHINE(-1), FRUIT(-1), MARK(-1);

        final int cooldown;
        Effect(int seconds) { cooldown = seconds * 20; }
        boolean active() { return cooldown >= 0; }
    }

    private static final java.util.EnumMap<Effect, RegistryObject<Item>> ITEMS = new java.util.EnumMap<>(Effect.class);
    public static final RegistryObject<Item> ROBE = register("curio_robe", Effect.ROBE);
    public static final RegistryObject<Item> KNOT = register("curio_knot", Effect.KNOT);
    public static final RegistryObject<Item> MODEL = register("curio_galaxy_model", Effect.MODEL);
    public static final RegistryObject<Item> SCISSORS = register("curio_annihilation_scissors", Effect.SCISSORS);
    public static final RegistryObject<Item> WHEEL = register("curio_memory_wheel", Effect.WHEEL);
    public static final RegistryObject<Item> PLAGUE = register("curio_plague_nest", Effect.PLAGUE);
    public static final RegistryObject<Item> ENGINE = register("curio_differential_engine", Effect.ENGINE);
    public static final RegistryObject<Item> REUNION = register("curio_reunion", Effect.REUNION);
    public static final RegistryObject<Item> CUSTOM = register("curio_local_custom", Effect.CUSTOM);
    public static final RegistryObject<Item> COLLECTION = register("curio_collection", Effect.COLLECTION);
    public static final RegistryObject<Item> HERO = register("curio_heroic_triumph", Effect.HERO);
    public static final RegistryObject<Item> TAIL = register("curio_carnival_tail", Effect.TAIL);
    public static final RegistryObject<Item> CALM = register("curio_tranquility", Effect.CALM);
    public static final RegistryObject<Item> CODE = register("curio_elegant_code", Effect.CODE);
    public static final RegistryObject<Item> WINDOW = register("curio_fortune_window", Effect.WINDOW);
    public static final RegistryObject<Item> SEAL = register("curio_trailblaze_seal", Effect.SEAL);
    public static final RegistryObject<Item> BEAUTY = register("curio_natural_beauty", Effect.BEAUTY);
    public static final RegistryObject<Item> COMPASS = register("curio_trailblaze_compass", Effect.COMPASS);
    public static final RegistryObject<Item> NEMESIS = register("curio_nemesis", Effect.NEMESIS);
    public static final RegistryObject<Item> DEBT = register("curio_debt_book", Effect.DEBT);
    public static final RegistryObject<Item> BOX = register("curio_toy_box", Effect.BOX);
    public static final RegistryObject<Item> REPAIR = register("curio_repair_scissors", Effect.REPAIR);
    public static final RegistryObject<Item> STAR = register("curio_wishing_star", Effect.STAR);
    public static final RegistryObject<Item> BOOK = register("curio_water_book", Effect.BOOK);
    public static final RegistryObject<Item> MACHINE = register("curio_fictional_automaton", Effect.MACHINE);
    public static final RegistryObject<Item> FRUIT = register("curio_corrupted_fruit", Effect.FRUIT);
    public static final RegistryObject<Item> MARK = register("curio_law_mark", Effect.MARK);

    private PrototypeCurios() {}

    static void registerContent() {}

    private static RegistryObject<Item> register(String id, Effect effect) {
        RegistryObject<Item> item = StarRailSimMod.ITEMS.register(id, () -> new CurioItem(effect));
        ITEMS.put(effect, item);
        return item;
    }

    public static void addCreativeItems(net.minecraft.world.item.CreativeModeTab.Output output) {
        ITEMS.values().forEach(item -> output.accept(item.get()));
    }

    static final class CurioItem extends Item {
        final Effect effect;

        CurioItem(Effect effect) {
            super(new Properties().stacksTo(1));
            this.effect = effect;
        }

        @Override
        public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
            lines.add(Component.translatable(getDescriptionId() + ".effect").withStyle(ChatFormatting.GRAY));
        }
    }

    public static ItemStack equipped(Player player, int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) return ItemStack.EMPTY;
        return CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inventory -> inventory.getStacksHandler(SLOT))
                .filter(handler -> slot < handler.getSlots())
                .map(handler -> handler.getStacks().getStackInSlot(slot))
                .orElse(ItemStack.EMPTY);
    }

    static ItemStack create(Effect effect) { return new ItemStack(ITEMS.get(effect).get()); }

    static Effect effect(ItemStack stack) {
        return stack.getItem() instanceof CurioItem item ? item.effect : null;
    }

    static ItemStack find(Player player, Effect effect) {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack stack = equipped(player, slot);
            if (effect(stack) == effect) return stack;
        }
        return ItemStack.EMPTY;
    }

    static boolean boss(LivingEntity target) {
        return target instanceof DoomsdayBeast || target.getType().is(BOSSES)
                || target instanceof net.minecraft.world.entity.boss.wither.WitherBoss
                || target instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon;
    }

    public static String bonusKey(ItemStack stack) {
        Effect effect = effect(stack);
        if (effect == null) return "";
        return switch (effect) {
            case ROBE, KNOT, MODEL, ENGINE, REUNION, COLLECTION, CALM, CODE, SEAL,
                    NEMESIS, MACHINE, WHEEL, PLAGUE, DEBT, BOX -> effect.name();
            default -> "";
        };
    }

    public static boolean isActive(ItemStack stack) {
        return stack.getItem() instanceof CurioItem item && item.effect.active();
    }

    static void use(ServerPlayer player, int slot, int expectedItem) {
        if (!player.isAlive() || player.isSpectator()) return;
        ItemStack stack = equipped(player, slot);
        if (!(stack.getItem() instanceof CurioItem item) || !isActive(stack)
                || Item.getId(item) != expectedItem || player.getCooldowns().isOnCooldown(item)) return;
        boolean success = switch (item.effect) {
            case HERO -> strike(player);
            case BOOK -> heal(player);
            default -> CurioEffects.use(player, stack, item.effect);
        };
        if (success && item.effect.cooldown > 0) player.getCooldowns().addCooldown(item, item.effect.cooldown);
        else if (!success) player.displayClientMessage(Component.translatable("message.starrail_sim.curio.no_target"), true);
    }

    static boolean hostile(LivingEntity entity) {
        return entity.getType().getCategory() == MobCategory.MONSTER;
    }

    private static boolean strike(ServerPlayer player) {
        var targets = player.serverLevel().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(10), target -> hostile(target) && target.isAlive()
                && player.distanceToSqr(target) <= 100 && !player.isAlliedTo(target)
                && player.hasLineOfSight(target));
        return targets.stream().max(Comparator.comparingDouble(LivingEntity::getMaxHealth))
                .map(target -> StarRailOwnedDamage.activeHurt(player, target,
                        Math.min(30F, target.getMaxHealth() * (boss(target) ? .08F : .30F))))
                .orElse(false);
    }

    private static boolean heal(ServerPlayer player) {
        boolean healed = false;
        for (ServerPlayer ally : player.serverLevel().players()) {
            if (!ally.isAlive() || ally.isSpectator() || ally.getHealth() >= ally.getMaxHealth()) continue;
            if (ally != player && (player.getTeam() == null || !player.isAlliedTo(ally)
                    || player.distanceToSqr(ally) > 64 || !player.hasLineOfSight(ally))) continue;
            float before = ally.getHealth();
            ally.heal(ally.getMaxHealth() * .20F);
            healed |= ally.getHealth() > before;
        }
        return healed;
    }

    @SubscribeEvent
    public static void recordCombat(LivingDamageEvent event) {
        if (event.getAmount() <= 0) return;
        if (event.getEntity() instanceof ServerPlayer player
                && event.getSource().getEntity() instanceof LivingEntity attacker && hostile(attacker)) {
            touchCombat(player);
        } else if (event.getSource().getEntity() instanceof ServerPlayer player && hostile(event.getEntity())) {
            touchCombat(player);
        }
    }

    private static void touchCombat(ServerPlayer player) {
        var data = player.getPersistentData();
        long now = player.server.overworld().getGameTime();
        if (!data.contains(LAST_COMBAT) || now - data.getLong(LAST_COMBAT) > 200) {
            data.putLong(NEXT_HEAL, now + 160);
        }
        data.putLong(LAST_COMBAT, now);
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || !player.isAlive() || player.isSpectator() || player.tickCount % 10 != 0) return;
        CurioEffects.tick(player);
        var data = player.getPersistentData();
        long now = player.server.overworld().getGameTime();
        if (!data.contains(LAST_COMBAT) || now - data.getLong(LAST_COMBAT) > 200
                || now < data.getLong(NEXT_HEAL)) return;
        data.putLong(NEXT_HEAL, now + 160);
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (equipped(player, slot).is(MACHINE.get())) {
                player.heal(player.getMaxHealth() * .05F);
                return;
            }
        }
    }
}
