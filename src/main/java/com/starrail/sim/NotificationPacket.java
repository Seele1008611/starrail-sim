package com.starrail.sim;

import com.starrail.sim.client.StarRailNotifications;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Sends a styled server notification to the client's HUD queue. */
public record NotificationPacket(Component message, boolean combat, boolean priority,
                                 String mergeKey) {
    public static NotificationPacket queued(Component message) {
        return new NotificationPacket(message, false, false, "");
    }

    public static NotificationPacket combat(Component message, boolean priority) {
        String key = message.getContents() instanceof TranslatableContents translated
                ? translated.getKey() : message.getString();
        return new NotificationPacket(message, true, priority, key);
    }

    public static void encode(NotificationPacket packet, FriendlyByteBuf buffer) {
        buffer.writeComponent(packet.message());
        buffer.writeBoolean(packet.combat());
        buffer.writeBoolean(packet.priority());
        buffer.writeUtf(packet.mergeKey());
    }

    public static NotificationPacket decode(FriendlyByteBuf buffer) {
        return new NotificationPacket(buffer.readComponent(), buffer.readBoolean(),
                buffer.readBoolean(), buffer.readUtf());
    }

    public static void handle(NotificationPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (packet.combat()) {
                StarRailNotifications.addCombat(packet.message(), packet.mergeKey(),
                        packet.priority());
            } else {
                StarRailNotifications.add(packet.message());
            }
        });
        context.setPacketHandled(true);
    }
}
