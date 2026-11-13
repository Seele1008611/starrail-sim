package com.starrail.sim;

import static com.starrail.sim.PrototypeCurios.Effect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.starrail.sim.PrototypeCurios.Effect;

@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID)
public final class CurioEffects {
    private static final UUID ATTACK = UUID.fromString("65692026-8a46-44ae-9796-f7e111f63e01");
    private static final UUID ARMOR = UUID.fromString("65692026-8a46-44ae-9796-f7e111f63e02");
    private static final UUID SPEED = UUID.fromString("65692026-8a46-44ae-9796-f7e111f63e03");
    static final String LOOT = "StarRailCurioLoot";
    private static final int MAX_DRY_OPENINGS = 8;
    private static final String MISSES = "CurioLootMisses";
    private static final String COUNT = "CurioCount";
    
    private CurioEffects() {}

    private static CompoundTag state(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG)) {
            root.put(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return root.getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG);
    }

    private static long time(ServerPlayer p) { return p.server.overworld().getGameTime(); }
    private static boolean active(ServerPlayer p, String key) { return state(p).getLong(key) > time(p); }
    private static void duration(ServerPlayer p, String key, int seconds) { state(p).putLong(key, time(p) + seconds * 20L); }
    private static boolean worn(ServerPlayer p, Effect e) { return !PrototypeCurios.find(p,e).isEmpty(); }
    static int count(ItemStack stack) { return stack.getTag() == null ? 0 : stack.getTag().getInt(COUNT); }
    private static void count(ItemStack stack, int count) { stack.getOrCreateTag().putInt(COUNT,count); }

    public static int remaining(ItemStack stack) {
        Effect effect = PrototypeCurios.effect(stack);
        if (effect == BOX || effect == STAR) return Math.max(0, 2-count(stack));
        if (effect == MARK) return Math.max(0, 8-count(stack));
        return -1;
    }

    private static void consume(ServerPlayer p, ItemStack stack, int limit, boolean refund) {
        if (refund && worn(p, TAIL) && p.getRandom().nextFloat() < .20F) return;
        count(stack, count(stack)+1);
        if (count(stack) >= limit) {
            var knot=PrototypeCurios.find(p,KNOT);
            if (!knot.isEmpty()) count(knot,Math.min(4,count(knot)+1));
        }
    }

    static boolean use(ServerPlayer p, ItemStack stack, Effect effect) {
        switch (effect) {
            case WHEEL -> {
                state(p).putInt("CurioWheelBuff", p.getRandom().nextInt(3));
                duration(p,"CurioWheelUntil",45);
                tick(p);
            }
            case PLAGUE -> duration(p,"CurioPlague",30);
            case DEBT -> {
                if (p.getHealth() * .8F < 1) return false;
                p.setHealth(p.getHealth()*.8F);
                duration(p,"CurioDebt",30);
                tick(p);
            }
            case BOX -> {
                if (remaining(stack) == 0 || active(p,"CurioBox")) return false;
                consume(p,stack,2,true);
                duration(p,"CurioBox",30);
            }
            case REPAIR -> {
                ItemStack target=p.getOffhandItem();
                Effect other=PrototypeCurios.effect(target);
                if ((other!=BOX && other!=STAR && other!=MARK) || remaining(target)!=0) return false;
                count(target,count(target)-1);
                consume(p,stack,1,false);
                stack.shrink(1);
            }
            case WINDOW -> {
                ItemStack target=p.getOffhandItem();
                Effect other=PrototypeCurios.effect(target);
                if (other==null || other==WINDOW) return false;
                var options=new ArrayList<Effect>();
                for (Effect e:Effect.values()) if (e!=other && e!=WINDOW && tier(e)==tier(other)) options.add(e);
                ItemStack replacement=PrototypeCurios.create(options.get(p.getRandom().nextInt(options.size())));
                p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,replacement);
                discover(p,replacement);
                consume(p,stack,1,true);
                if (count(stack)>0) stack.shrink(1);
            }
            case COMPASS -> {
                if (!p.serverLevel().dimension().equals(StarRailDimensionIds.THE_PATH)) return false;
                var registry=p.serverLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);
                var targets=new ArrayList<net.minecraft.core.Holder<net.minecraft.world.level.levelgen.structure.Structure>>();
                for (String id:List.of("doomsday_arena","dayless_archive","tideward_dock","broken_ring_observatory")) {
                    registry.getHolder(net.minecraft.resources.ResourceKey.create(Registries.STRUCTURE,
                            new ResourceLocation(StarRailSimMod.MOD_ID,id))).ifPresent(targets::add);
                }
                if (targets.isEmpty()) return false;
                var found=p.serverLevel().getChunkSource().getGenerator().findNearestMapStructure(
                        p.serverLevel(),HolderSet.direct(targets),p.blockPosition(),48,false);
                if (found==null) return false;
                BlockPos pos=found.getFirst();
                p.sendSystemMessage(Component.translatable("message.starrail_sim.curio.compass",pos.getX(),pos.getY(),pos.getZ()));
            }
            default -> { return false; }
        }
        return true;
    }

    static void tick(ServerPlayer p) {
        double attack=worn(p,CODE)?.08:0, armor=attack, speed=0;
        if (worn(p,COLLECTION)) attack+=collectionBonus(p);
        if (active(p,"CurioDebt")) armor+=.25;
        if (active(p,"CurioWheelUntil")) {
            switch (state(p).getInt("CurioWheelBuff")) {
                case 0 -> attack+=.15;
                case 1 -> armor+=.15;
                case 2 -> speed=.15;
            }
        }
        modifier(p,Attributes.ATTACK_DAMAGE,ATTACK,attack);
        modifier(p,Attributes.ARMOR,ARMOR,armor);
        modifier(p,Attributes.MOVEMENT_SPEED,SPEED,speed);
        for (int slot=0;slot<PrototypeCurios.SLOT_COUNT;slot++) discover(p,PrototypeCurios.equipped(p,slot));
        if (p.tickCount % 20 == 0) {
            for (ItemStack stack : p.getInventory().items) discover(p,stack);
            discover(p,p.getOffhandItem());
        }
        syncBonuses(p);
    }

    private static void modifier(ServerPlayer p, Attribute attribute, UUID id, double value) {
        var instance=p.getAttribute(attribute);
        if (instance==null) return;
        var current=instance.getModifier(id);
        if (current!=null && current.getAmount()==value) return;
        if (current!=null) instance.removeModifier(id);
        if (value!=0) instance.addTransientModifier(new AttributeModifier(id,"Star Rail curio",value,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    static int skillCooldown(ServerPlayer p, int ticks) { return worn(p,SEAL)?Math.round(ticks*.9F):ticks; }
    static double critRate(ServerPlayer p) { return active(p,"CurioEngine")?.12:0; }
    static double critDamage(ServerPlayer p,LivingEntity target) {
        return (active(p,"CurioGalaxy")?.20:0)+(worn(p,NEMESIS)&&elite(target)?.20:0);
    }

    private static boolean elite(LivingEntity target) {
        return PrototypeCurios.boss(target) || target instanceof StarveinConstructGuardian
                || target.getTags().contains("StarRailRelicArenaElite");
    }

    static float attack(ServerPlayer p, LivingEntity target, float amount) {
        if (!PrototypeCurios.hostile(target)) return amount;
        double bonus=0;
        var robe=PrototypeCurios.find(p,ROBE);
        var knot=PrototypeCurios.find(p,KNOT);
        if (!robe.isEmpty()) bonus+=robeBonus(robe);
        if (!knot.isEmpty()) bonus+=knotBonus(knot);
        if (worn(p,REUNION) && p.getHealth()<p.getMaxHealth()*.30F) bonus+=.20;
        if (active(p,"CurioPlague")) bonus+=.20;
        if (active(p,"CurioBox")) bonus+=.15;
        var data=state(p);
        if (worn(p,CALM) && (!data.contains("CurioCombat") || time(p)-data.getLong("CurioCombat")>=200)) bonus+=.25;
        data.putLong("CurioCombat",time(p));
        return (float)(amount*(1+bonus));
    }

    @SubscribeEvent
    public static void hurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && active(p,"CurioPlague")) event.setAmount(event.getAmount()*1.10F);
    }

    @SubscribeEvent
    public static void damaged(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || event.getAmount()<=0
                || !(event.getSource().getEntity() instanceof LivingEntity mob) || !PrototypeCurios.hostile(mob)) return;
        state(p).putLong("CurioCombat",time(p));
        if (worn(p,MODEL) && !active(p,"CurioGalaxyCooldown")) {
            duration(p,"CurioGalaxy",8);
            duration(p,"CurioGalaxyCooldown",20);
        }
    }

    @SubscribeEvent
    public static void kill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer p) || !elite(event.getEntity())) return;
        var box=PrototypeCurios.find(p,BOX);
        if (!box.isEmpty() && count(box)>0) {
            count(box,count(box)-1);
        }
    }

    @SubscribeEvent
    public static void control(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || !worn(p,FRUIT) || active(p,"CurioFruit")) return;
        var effect=event.getEffectInstance();
        if (effect.getEffect()!=MobEffects.MOVEMENT_SLOWDOWN || effect.getAmplifier()<3) return;
        float cost=p.getMaxHealth()*.20F;
        if (p.getHealth()<=cost) return;
        p.setHealth(p.getHealth()-cost);
        duration(p,"CurioFruit",20);
        event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
    }

    private static void discover(ServerPlayer p, ItemStack stack) {
        var effect=PrototypeCurios.effect(stack);
        if (effect==null) return;
        var data=state(p);
        if (!data.contains("CurioCollection")) data.put("CurioCollection",new CompoundTag());
        data.getCompound("CurioCollection").putBoolean(effect.name(),true);
    }

    private static int tier(Effect effect) { return effect.ordinal()<REPAIR.ordinal()?3:2; }

    private static Effect roll(ServerPlayer p, Effect excluded) {
        int tier=p.getRandom().nextInt(3)==0?3:2;
        var candidates=new ArrayList<Effect>();
        for (Effect e:Effect.values()) if (e!=excluded && tier(e)==tier) candidates.add(e);
        return candidates.get(p.getRandom().nextInt(candidates.size()));
    }

    private static void give(ServerPlayer p, ItemStack stack) {
        discover(p,stack);
        if (!p.getInventory().add(stack)) p.drop(stack,false);
    }

    static void supplies(ServerPlayer p) {
        var table=p.server.getLootData().getLootTable(new ResourceLocation(StarRailSimMod.MOD_ID,"chests/archive_supplies"));
        var params=new net.minecraft.world.level.storage.loot.LootParams.Builder(p.serverLevel())
                .withParameter(LootContextParams.ORIGIN,p.position()).create(LootContextParamSets.CHEST);
        table.getRandomItems(params).forEach(stack->give(p,stack));
    }

    static void opened(ServerPlayer p, RandomizableContainerBlockEntity container) {
        if (!p.serverLevel().dimension().equals(StarRailDimensionIds.THE_PATH) || p.isSpectator()
                || !identifyCurioLoot(container) || !claimLoot(p, container)) return;
        var robe=PrototypeCurios.find(p,ROBE);
        if (!robe.isEmpty()) count(robe,Math.min(6,count(robe)+1));
        if (worn(p,ENGINE)) duration(p,"CurioEngine",60);
        if (worn(p,CUSTOM) && p.getRandom().nextFloat()<.20F) supplies(p);
        var star=PrototypeCurios.find(p,STAR);
        if (!star.isEmpty() && remaining(star)>0) {
            consume(p,star,2,false);
            supplies(p);
        }
        var mark=PrototypeCurios.find(p,MARK);
        if (!mark.isEmpty() && remaining(mark)>0) {
            consume(p,mark,8,false);
            if (remaining(mark)==0) award(p);
        }
        CompoundTag data = state(p);
        if (shouldAward(data.getInt(MISSES), p.getRandom().nextFloat())) {
            data.remove(MISSES);
            award(p);
        } else {
            data.putInt(MISSES, data.getInt(MISSES) + 1);
        }
        syncBonuses(p);
    }

    static boolean shouldAward(int misses, float roll) {
        return misses >= MAX_DRY_OPENINGS - 1 || roll < .15F;
    }

    private static double robeBonus(ItemStack stack) { return .04 * count(stack); }
    private static double knotBonus(ItemStack stack) { return .05 * count(stack); }
    private static double collectionBonus(ServerPlayer p) {
        return Math.min(.18, state(p).getCompound("CurioCollection").getAllKeys().size() * .02);
    }

    private static void syncBonuses(ServerPlayer p) {
        CompoundTag bonuses = new CompoundTag();
        for (int slot = 0; slot < PrototypeCurios.SLOT_COUNT; slot++) {
            ItemStack stack = PrototypeCurios.equipped(p, slot);
            Effect effect = PrototypeCurios.effect(stack);
            if (effect == null || bonuses.contains(effect.name())) continue;
            Component line = switch (effect) {
                case ROBE -> bonus("damage", robeBonus(stack));
                case KNOT -> bonus("damage", knotBonus(stack));
                case MODEL -> bonus("crit_damage", active(p,"CurioGalaxy") ? .20 : 0);
                case ENGINE -> bonus("crit_rate", critRate(p));
                case REUNION -> bonus("damage", p.getHealth() < p.getMaxHealth() * .30F ? .20 : 0);
                case COLLECTION -> bonus("attack", collectionBonus(p));
                case CALM -> bonus("first_hit", !state(p).contains("CurioCombat")
                        || time(p)-state(p).getLong("CurioCombat") >= 200 ? .25 : 0);
                case CODE -> bonus("attack",.08).copy().append(" · ").append(bonus("armor",.08));
                case SEAL -> bonus("skill_cooldown",-.10);
                case NEMESIS -> bonus("elite_crit",.20);
                case MACHINE -> bonus("combat_heal",p.getPersistentData().contains(PrototypeCurios.LAST_COMBAT)
                        && time(p)-p.getPersistentData().getLong(PrototypeCurios.LAST_COMBAT) <= 200 ? .05 : 0);
                default -> null;
            };
            if (line != null) bonuses.putString(effect.name(),Component.Serializer.toJson(line));
        }
        putTimedBonus(bonuses,WHEEL,active(p,"CurioWheelUntil"),bonus(switch(state(p).getInt("CurioWheelBuff")) {
            case 0 -> "attack";
            case 1 -> "armor";
            default -> "speed";
        },.15));
        putTimedBonus(bonuses,PLAGUE,active(p,"CurioPlague"),bonus("damage",.20).copy()
                .append(" · ").append(bonus("damage_taken",.10)));
        putTimedBonus(bonuses,DEBT,active(p,"CurioDebt"),bonus("armor",.25));
        putTimedBonus(bonuses,BOX,active(p,"CurioBox"),bonus("damage",.15));
        CompoundTag old = p.getPersistentData().getCompound("StarRailCurioLastTooltip");
        if (!old.equals(bonuses) || p.tickCount % 100 == 0) {
            p.getPersistentData().put("StarRailCurioLastTooltip",bonuses);
            StarRailNetwork.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> p),
                    new CurioBonusPacket(bonuses));
        }
    }

    private static void putTimedBonus(CompoundTag bonuses, Effect effect, boolean active, Component line) {
        if (active) bonuses.putString(effect.name(),Component.Serializer.toJson(line));
    }

    private static Component bonus(String key, double value) {
        long percent = Math.round(value * 100);
        return Component.translatable("tooltip.starrail_sim.curio.bonus." + key,
                (percent > 0 ? "+" : "") + percent + "%");
    }

    private static void award(ServerPlayer p) {
        Effect first=roll(p,null);
        if (!worn(p,BEAUTY)) {
            give(p,PrototypeCurios.create(first));
            return;
        }
        var data=state(p);
        var queue=data.getList("CurioChoices",10);
        CompoundTag pair=new CompoundTag();
        pair.putUUID("Id",UUID.randomUUID());
        pair.putString("First",first.name());
        pair.putString("Second",roll(p,first).name());
        queue.add(pair);
        data.put("CurioChoices",queue);
        p.displayClientMessage(Component.translatable("message.starrail_sim.curio.choice_ready"),true);
    }

    static void offer(ServerPlayer p) {
        var queue=state(p).getList("CurioChoices",10);
        if (queue.isEmpty()) return;
        var pair=queue.getCompound(0);
        StarRailNetwork.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(()->p),
                new CurioChoicePacket(pair.getUUID("Id"),PrototypeCurios.create(Effect.valueOf(pair.getString("First"))),
                        PrototypeCurios.create(Effect.valueOf(pair.getString("Second")))));
    }

    static void claim(ServerPlayer p, UUID id,int index) {
        if (index<0 || index>1 || !p.isAlive() || p.isSpectator()) return;
        var queue=state(p).getList("CurioChoices",10);
        if (queue.isEmpty()) return;
        var pair=queue.getCompound(0);
        if (!pair.getUUID("Id").equals(id)) return;
        var item=PrototypeCurios.create(Effect.valueOf(pair.getString(index==0?"First":"Second")));
        queue.remove(0);
        give(p,item);
    }

    // Existing unopened containers from test80/81 can be identified before vanilla unpacks their loot.
    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST)
    public static void identifyLegacyLoot(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || p.isSpectator()
                || !p.serverLevel().dimension().equals(StarRailDimensionIds.THE_PATH)
                || !(p.serverLevel().getBlockEntity(event.getPos()) instanceof RandomizableContainerBlockEntity container)) return;
        identifyCurioLoot(container);
    }

    private static boolean identifyCurioLoot(RandomizableContainerBlockEntity container) {
        var data = container.getPersistentData();
        if (data.getBoolean(LOOT)) return true;
        ResourceLocation table = RuinLootContainers.lootTable(container);
        if (table == null || !table.getNamespace().equals(StarRailSimMod.MOD_ID)) return false;
        boolean eligible = switch (table.getPath()) {
            case "chests/archive_supplies", "chests/archive_materials", "chests/archive_hidden",
                    "chests/terminal_supplies", "chests/terminal_salvage",
                    "chests/dock_supplies", "chests/dock_wreck" -> true;
            default -> false;
        };
        if (eligible) {
            data.putBoolean(LOOT, true);
            container.setChanged();
        }
        return eligible;
    }

    private static boolean claimLoot(ServerPlayer player, RandomizableContainerBlockEntity container) {
        var data = container.getPersistentData();
        var personalId = RuinLootContainers.personalLootId(container);
        if (personalId == null) {
            if (data.getBoolean("StarRailCurioClaimed")) return false;
            data.putBoolean("StarRailCurioClaimed", true);
            data.putUUID("StarRailCurioClaimedBy", player.getUUID());
            container.setChanged();
            return true;
        }

        if (data.hasUUID("StarRailCurioClaimedBy")
                && player.getUUID().equals(data.getUUID("StarRailCurioClaimedBy"))) return false;
        var playerData = state(player);
        String claimsKey = "CurioLootrClaims";
        var claims = playerData.getCompound(claimsKey);
        String chestId = personalId.toString();
        if (claims.getBoolean(chestId)) return false;
        claims.putBoolean(chestId, true);
        playerData.put(claimsKey, claims);
        return true;
    }

    @SubscribeEvent
    public static void opened(PlayerContainerEvent.Open event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()
                || !player.serverLevel().dimension().equals(StarRailDimensionIds.THE_PATH)) return;
        RuinLootContainers.forEachOpened(player, event.getContainer(), chest -> opened(player, chest));
    }

    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST)
    public static void broken(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer p) || p.isCreative()
                || !event.getState().is(Blocks.SPAWNER) || !worn(p,SCISSORS)
                || !p.serverLevel().dimension().equals(StarRailDimensionIds.THE_PATH)) return;
        var entity=p.serverLevel().getBlockEntity(event.getPos());
        if (entity!=null && entity.getPersistentData().getBoolean(LOOT)) supplies(p);
    }
}
