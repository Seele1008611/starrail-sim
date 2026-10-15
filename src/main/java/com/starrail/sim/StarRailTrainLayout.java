package com.starrail.sim;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Shared server/client footprint. Only occupied cells become linked collision blocks. */
public final class StarRailTrainLayout {
    public static final int WIDTH = 5;
    public static final int LENGTH = 13;
    public static final int HEIGHT = 5;
    // Keep the original cell encoding so already placed prototype trains remain removable.
    private static final int STRIDE_WIDTH = 6;
    private static final int STRIDE_HEIGHT = 8;
    public static final int CELL_COUNT = 6 * 20 * 8;
    private static final VoxelShape[][] SHAPES = new VoxelShape[4][CELL_COUNT];
    public static final List<Integer> OCCUPIED_CELLS;

    static {
        List<Integer> cells = new ArrayList<>();
        try (var stream = StarRailTrainLayout.class.getResourceAsStream(
                "/data/starrail_sim/train/star_rail_train_collision.json")) {
            if (stream == null) throw new IllegalStateException("Missing train collision resource");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            if (root.get("width").getAsInt() != WIDTH || root.get("length").getAsInt() != LENGTH
                    || root.get("height").getAsInt() != HEIGHT) {
                throw new IllegalStateException("Train mesh footprint and block dimensions disagree");
            }
            for (JsonElement value : root.getAsJsonArray("cells")) {
                JsonObject cell = value.getAsJsonObject();
                int id = cell.get("id").getAsInt();
                if (id < 0 || id >= CELL_COUNT) throw new IllegalStateException("Invalid train cell " + id);
                cells.add(id);
                for (int turn = 0; turn < 4; turn++) {
                    VoxelShape shape = Shapes.empty();
                    for (JsonElement entry : cell.getAsJsonArray("boxes")) {
                        JsonArray a = entry.getAsJsonArray();
                        double x0 = a.get(0).getAsDouble(), y0 = a.get(1).getAsDouble();
                        double z0 = a.get(2).getAsDouble(), x1 = a.get(3).getAsDouble();
                        double y1 = a.get(4).getAsDouble(), z1 = a.get(5).getAsDouble();
                        for (int t = 0; t < turn; t++) {
                            double oldX0 = x0, oldX1 = x1;
                            x0 = 1 - z1; x1 = 1 - z0;
                            z0 = oldX0; z1 = oldX1;
                        }
                        shape = Shapes.or(shape, Shapes.box(x0, y0, z0, x1, y1, z1));
                    }
                    SHAPES[turn][id] = shape.optimize();
                }
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
        OCCUPIED_CELLS = List.copyOf(cells);
    }

    private StarRailTrainLayout() {}

    public static int x(int id) { return id % STRIDE_WIDTH; }
    public static int y(int id) { return (id / STRIDE_WIDTH) % STRIDE_HEIGHT; }
    public static int z(int id) { return id / (STRIDE_WIDTH * STRIDE_HEIGHT); }
    public static int id(int x, int y, int z) { return x + STRIDE_WIDTH * (y + STRIDE_HEIGHT * z); }

    public static BlockPos position(BlockPos anchor, Direction facing, int id) {
        return anchor.relative(facing.getClockWise(), x(id))
                .relative(facing.getOpposite(), z(id)).above(y(id));
    }

    public static BlockPos anchor(BlockPos pos, Direction facing, int id) {
        return pos.relative(facing.getCounterClockWise(), x(id)).relative(facing, z(id)).below(y(id));
    }

    public static VoxelShape shape(int id, Direction facing) {
        int turn = switch (facing) { case EAST -> 1; case SOUTH -> 2; case WEST -> 3; default -> 0; };
        VoxelShape result = SHAPES[turn][id];
        return result == null ? Shapes.empty() : result;
    }
}
