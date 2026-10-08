package com.starrail.sim;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

/** All unlock/refund transactions execute on the server thread. */
public final class StarRailTraceService {
    private StarRailTraceService() { }
    public static int mask(Player player) {
        return player.getCapability(StarRailPathCapability.PATH_DATA).map(data ->
                data.getCurrentPath() == StarRailPath.HUNT ? data.getTraceMask(StarRailPath.HUNT) : 0).orElse(0);
    }
    public static int mask(Player player, StarRailPath path) {
        return player.getCapability(StarRailPathCapability.PATH_DATA).map(data ->
                data.getTraceMask(path)).orElse(0);
    }
    public static boolean hasPassive(Player player, StarRailPath path, int node) {
        if (player == null || path == null) return false;
        return player.getCapability(StarRailPathCapability.PATH_DATA).map(data ->
                data.getCurrentPath() == path
                        && StarRailTraces.has(data.getTraceMask(path), node)).orElse(false);
    }
    public static int count(Player player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) count += stack.getCount();
        }
        return count;
    }
    public static void act(ServerPlayer player, TraceActionPacket.Action action, StarRailPath path, int node) {
        IStarRailPathData data = player.getCapability(StarRailPathCapability.PATH_DATA).resolve().orElse(null);
        if (data == null) return;
        if (action == TraceActionPacket.Action.REQUEST) { sync(player, data); return; }
        if (!path.isRealPath() || data.getCurrentPath() != path || !player.isAlive() || player.isSpectator()) {
            reply(player, data, "请先踏上" + path.getDisplayName() + "命途。"); return;
        }
        int old = data.getTraceMask(path), next;
        Item material = StarRailTraceMaterials.get(path);
        if (action == TraceActionPacket.Action.UNLOCK) {
            if (!StarRailTraces.canUnlock(path, old, data.getPathRank(path).getLevel(), node)) {
                reply(player, data, "节点已解锁，或尚未满足阶位及前置条件。"); return;
            }
            int cost = StarRailTraces.nodes(path)[node].cost();
            if (count(player, material) < cost) { reply(player, data, StarRailTraceMaterials.displayName(path) + "不足。"); return; }
            int remaining = cost;
            for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(material)) { int amount = Math.min(stack.getCount(), remaining); stack.shrink(amount); remaining -= amount; }
            }
            next = old | (1 << node);
        } else {
            next = action == TraceActionPacket.Action.RESET ? 0 : StarRailTraces.rollback(path, old, node);
            if (next == old) { sync(player, data); return; }
            // Inventory overflow follows vanilla item drops; refunds never vanish.
            data.setTraceMask(path, next);
            ItemStack refund = new ItemStack(material, StarRailTraces.cost(path, old) - StarRailTraces.cost(path, next));
            if (!player.getInventory().add(refund)) player.drop(refund, false);
        }
        data.setTraceMask(path, next);
        if (path == StarRailPath.HUNT) player.getPersistentData().remove("trace_hunt_echo");
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        refresh(player, path, next);
        reply(player, data, action == TraceActionPacket.Action.UNLOCK ? "行迹已解锁。" : "行迹已回退，材料已返还。");
    }
    private static void reply(ServerPlayer player, IStarRailPathData data, String text) {
        player.displayClientMessage(Component.literal(text), true);
        sync(player, data);
    }
    public static void sync(ServerPlayer player, IStarRailPathData data) {
        StarRailNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new TraceStatePacket(data));
    }
    public static void refresh(Player player, StarRailPath path, int mask) {
        for (StarRailPath tracePath : StarRailPath.values()) {
            if (!tracePath.isRealPath()) continue;
            int active = tracePath == path ? mask : 0;
            for (int stat = 0; stat <= StarRailTraces.HEALING_EFFECT; stat++) {
                AttributeInstance instance = player.getAttribute(attribute(stat));
                if (instance == null) continue;
                UUID id = UUID.nameUUIDFromBytes(("starrail.trace." + tracePath.getId() + "." + stat)
                        .getBytes(StandardCharsets.UTF_8));
                double amount = StarRailTraces.bonus(tracePath, active, stat);
                AttributeModifier old = instance.getModifier(id);
                if (old != null && old.getAmount() == amount) continue;
                instance.removeModifier(id);
                if (amount != 0) instance.addPermanentModifier(new AttributeModifier(id,
                        tracePath.getDisplayName() + " traces", amount, operation(stat)));
            }
        }
        if (path != StarRailPath.HUNT || !StarRailTraces.has(mask, 7)) {
            player.getPersistentData().remove("trace_hunt_echo");
        }
        if (path != StarRailPath.PRESERVATION || !StarRailTraces.has(mask, 7)) {
            player.getPersistentData().remove("trace_preservation_countershock");
        }
        if (path != StarRailPath.PRESERVATION || !StarRailTraces.has(mask, 6)) {
            player.getPersistentData().remove("trace_preservation_shield_barrier");
        }
        if (path != StarRailPath.DESTRUCTION || !StarRailTraces.has(mask, 7)) {
            player.getPersistentData().remove("trace_destruction_wrath_crit");
        }
        if (path != StarRailPath.DESTRUCTION || !StarRailTraces.has(mask, 8)) {
            player.getPersistentData().remove("trace_destruction_desperation_blast");
        }
        if (path != StarRailPath.ERUDITION || !StarRailTraces.has(mask, 8)) {
            player.getPersistentData().remove("trace_erudition_expanded_echo");
        }
    }
    private static Attribute attribute(int stat) {
        return switch (stat) {
            case StarRailTraces.CRIT_RATE -> StarRailAttributes.CRIT_RATE.get();
            case StarRailTraces.ATTACK -> Attributes.ATTACK_DAMAGE;
            case StarRailTraces.CRIT_DAMAGE -> StarRailAttributes.CRIT_DAMAGE.get();
            case StarRailTraces.MAX_HEALTH -> Attributes.MAX_HEALTH;
            case StarRailTraces.ARMOR -> Attributes.ARMOR;
            case StarRailTraces.ARMOR_TOUGHNESS -> Attributes.ARMOR_TOUGHNESS;
            case StarRailTraces.EFFECT_HIT_RATE -> StarRailAttributes.EFFECT_HIT_RATE.get();
            case StarRailTraces.BREAK_EFFECT -> StarRailAttributes.BREAK_EFFECT.get();
            case StarRailTraces.HEALING_EFFECT -> StarRailAttributes.HEALING_EFFECT.get();
            default -> Attributes.ATTACK_DAMAGE;
        };
    }
    private static AttributeModifier.Operation operation(int stat) {
        return switch (stat) {
            case StarRailTraces.ATTACK, StarRailTraces.MAX_HEALTH -> AttributeModifier.Operation.MULTIPLY_BASE;
            case StarRailTraces.ARMOR, StarRailTraces.ARMOR_TOUGHNESS -> AttributeModifier.Operation.MULTIPLY_TOTAL;
            default -> AttributeModifier.Operation.ADDITION;
        };
    }
    public static double consumeEcho(ServerPlayer player, LivingEntity target) {
        var tag = player.getPersistentData().getCompound("trace_hunt_echo");
        if (!StarRailTraces.has(mask(player), 7) || !tag.hasUUID("target")) return 0;
        if (tag.getLong("expires") < player.level().getGameTime()
                || !tag.getString("dimension").equals(player.level().dimension().location().toString())) {
            player.getPersistentData().remove("trace_hunt_echo"); return 0;
        }
        if (!tag.getUUID("target").equals(target.getUUID())) return 0;
        player.getPersistentData().remove("trace_hunt_echo");
        return .08;
    }
    public static void armEcho(ServerPlayer player, LivingEntity target) {
        if (!StarRailTraces.has(mask(player), 7)) return;
        var tag = new net.minecraft.nbt.CompoundTag();
        tag.putUUID("target", target.getUUID());
        tag.putLong("expires", player.level().getGameTime() + 80);
        tag.putString("dimension", player.level().dimension().location().toString());
        player.getPersistentData().put("trace_hunt_echo", tag);
    }
}
