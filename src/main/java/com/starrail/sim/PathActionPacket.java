package com.starrail.sim;

/**
 * 模组代码说明：网络数据包类，定义需要在客户端与服务端之间传递的数据及其处理入口。
 */

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client-to-server actions from the path selection screen. */
public final class PathActionPacket {
    public enum Action {
        REQUEST_STATE,
        SEEK_PATH,
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
        if (packet.action == Action.SEEK_PATH) {
            buffer.writeEnum(packet.path);
        }
    }

    public static PathActionPacket decode(FriendlyByteBuf buffer) {
        Action action = buffer.readEnum(Action.class);
        return action == Action.SEEK_PATH
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
                case SEEK_PATH -> StarRailPathSeekService.seek(player, packet.path);
                case CONFIRM -> StarRailPathService.confirmTrial(player);
                case RESET_PATH -> StarRailPathService.resetPath(player);
            }
        });
        context.setPacketHandled(true);
    }
}
