package com.starrail.sim;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client-to-server actions from the path selection screen. */
public final class PathActionPacket {
    public enum Action {
        REQUEST_STATE,
        START_PATH,
        CONFIRM,
        RESET_PATH
    }

    private final Action action;
    private final StarRailPath path;

    public PathActionPacket(Action action) {
        this(action, StarRailPath.NONE);
    }

    public PathActionPacket(Action action, StarRailPath path) {
        this.action = action;
        this.path = path == null ? StarRailPath.NONE : path;
    }

    public static void encode(PathActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.action);
        if (packet.action == Action.START_PATH) {
            buffer.writeEnum(packet.path);
        }
    }

    public static PathActionPacket decode(FriendlyByteBuf buffer) {
        Action action = buffer.readEnum(Action.class);
        return action == Action.START_PATH
                ? new PathActionPacket(action, buffer.readEnum(StarRailPath.class))
                : new PathActionPacket(action);
    }

    public static void handle(PathActionPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            switch (packet.action) {
                case REQUEST_STATE -> StarRailPathService.sync(player);
                case START_PATH -> StarRailPathService.startTrial(player, packet.path);
                case CONFIRM -> StarRailPathService.confirmTrial(player);
                case RESET_PATH -> StarRailPathService.resetPath(player);
            }
        });
        context.setPacketHandled(true);
    }
}
