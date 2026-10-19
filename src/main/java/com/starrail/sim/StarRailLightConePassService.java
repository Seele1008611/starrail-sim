package com.starrail.sim;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/** Server-authoritative use and reward handling for Star Rail Passes. */
public final class StarRailLightConePassService {
    private StarRailLightConePassService() {
    }

    public static void drawRandom(ServerPlayer player, InteractionHand hand) {
        ItemStack ticket = player.getItemInHand(hand);
        if (ticket.getItem() != StarRailSimMod.STAR_RAIL_PASS.get()) {
            return;
        }

        StarRailPath path = currentPath(player);
        if (!path.isRealPath()) {
            player.sendSystemMessage(Component.translatable("message.starrail_sim.pass.no_path"));
            return;
        }

        List<Item> pool = StarRailLightConePools.forPath(path);
        if (pool.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.starrail_sim.pass.empty_pool"));
            return;
        }

        Item rewardItem = pool.get(player.getRandom().nextInt(pool.size()));
        giveReward(player, rewardItem);
        consumeTicket(player, ticket);
        player.sendSystemMessage(Component.translatable("message.starrail_sim.pass.random_received",
                Component.translatable(rewardItem.getDescriptionId())));
    }

    public static void openPicker(ServerPlayer player, InteractionHand hand) {
        if (player.getItemInHand(hand).getItem() != StarRailSimMod.STAR_RAIL_SPECIAL_PASS.get()) {
            return;
        }

        StarRailPath path = currentPath(player);
        if (!path.isRealPath()) {
            player.sendSystemMessage(Component.translatable("message.starrail_sim.pass.no_path"));
            return;
        }
        if (StarRailLightConePools.forPath(path).isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.starrail_sim.pass.empty_pool"));
            return;
        }

        StarRailNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new OpenLightConePassPickerPacket(path, hand));
    }

    public static void select(ServerPlayer player, StarRailPath requestedPath,
                              InteractionHand hand, ResourceLocation itemId) {
        ItemStack ticket = player.getItemInHand(hand);
        if (ticket.getItem() != StarRailSimMod.STAR_RAIL_SPECIAL_PASS.get()) {
            player.sendSystemMessage(Component.translatable("message.starrail_sim.pass.invalid_choice"));
            return;
        }

        StarRailPath currentPath = currentPath(player);
        if (!currentPath.isRealPath()) {
            player.sendSystemMessage(Component.translatable("message.starrail_sim.pass.no_path"));
            return;
        }
        if (requestedPath != currentPath) {
            player.sendSystemMessage(Component.translatable("message.starrail_sim.pass.path_changed"));
            return;
        }

        Item selected = ForgeRegistries.ITEMS.getValue(itemId);
        if (selected == null || selected == Items.AIR
                || !StarRailLightConePools.forPath(currentPath).contains(selected)) {
            player.sendSystemMessage(Component.translatable("message.starrail_sim.pass.invalid_choice"));
            return;
        }

        giveReward(player, selected);
        consumeTicket(player, ticket);
        player.sendSystemMessage(Component.translatable("message.starrail_sim.pass.selected_received",
                Component.translatable(selected.getDescriptionId())));
    }

    private static StarRailPath currentPath(ServerPlayer player) {
        return player.getCapability(StarRailPathCapability.PATH_DATA)
                .map(IStarRailPathData::getCurrentPath)
                .orElse(StarRailPath.NONE);
    }

    private static void giveReward(ServerPlayer player, Item item) {
        ItemStack reward = new ItemStack(item);
        if (!player.getInventory().add(reward) && !reward.isEmpty()) {
            player.drop(reward, false);
        }
    }

    private static void consumeTicket(ServerPlayer player, ItemStack ticket) {
        if (!player.getAbilities().instabuild) {
            ticket.shrink(1);
        }
    }
}
