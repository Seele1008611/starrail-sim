package com.starrail.sim.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import com.starrail.sim.SunnyHarvestDeer;
import com.starrail.sim.StarvelvetRam;
import com.starrail.sim.StarScreePackbeast;
import com.starrail.sim.WoodlandForager;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;

/**
 * Runtime renderer geometry converted from the accepted 1.24.0 Blockbench cuboid models.
 * Per-face UV rectangles and the source cuboid rotations are emitted directly, avoiding a
 * lossy conversion to vanilla's single-origin cuboid UV layout.
 */
public final class PathAnimalModel<T extends Entity> extends EntityModel<T> {
    public enum Species {
        SUN_GRAIN_DEER("sun_grain_deer"), STARFLEECE_RAM("starfleece_ram"),
        WOODLAND_FORAGER("woodland_forager"), MARSH_FOWL("marsh_fowl"),
        STARGRAVEL_PACKBEAST("stargravel_packbeast");
        private final String id;
        Species(String id) { this.id = id; }
        public String id() { return id; }
    }

    /** Named animation pivots shared with the entity renderer/animation controller. */
    public enum Joint {
        BODY, ABDOMEN, CHEST, NECK, HEAD, EARS, ANTLERS, BEAK, CROWN, WINGS,
        BACK_RIDGE, TAIL, LEFT_FRONT_LEG, RIGHT_FRONT_LEG, LEFT_HIND_LEG,
        RIGHT_HIND_LEG
    }

    private static final Map<Species, SpeciesLayer> LAYERS = Map.ofEntries(
            Map.entry(Species.SUN_GRAIN_DEER, new SpeciesLayer(Species.SUN_GRAIN_DEER,
                new Bone[]{new Bone(Joint.BODY, 0.00000f, 34.00000f, 2.00000f),
                new Bone(Joint.NECK, 0.00000f, 33.00000f, -11.50000f),
                new Bone(Joint.HEAD, 0.00000f, 50.00000f, -20.50000f),
                new Bone(Joint.EARS, 0.00000f, 50.00000f, -16.50000f),
                new Bone(Joint.ANTLERS, 0.00000f, 62.50000f, -17.00000f),
                new Bone(Joint.TAIL, 0.00000f, 29.50000f, 20.50000f),
                new Bone(Joint.LEFT_FRONT_LEG, -4.58333f, 29.00000f, -10.66667f),
                new Bone(Joint.RIGHT_FRONT_LEG, 4.58333f, 29.00000f, -10.66667f),
                new Bone(Joint.LEFT_HIND_LEG, -4.58333f, 29.00000f, 14.83333f),
                new Bone(Joint.RIGHT_HIND_LEG, 4.58333f, 29.00000f, 14.83333f)},
                new Cube[]{new Cube("body_core", Joint.BODY, -7f, 27f, -8f, 7f, 40f, 13f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 12f, 5f, 26f, 18f, 0), new Face("east", 9f, 16f, 30f, 29f, 0), new Face("south", 0f, 10f, 14f, 23f, 0), new Face("west", 1f, 2f, 22f, 15f, 0), new Face("up", 5f, 7f, 19f, 28f, 0), new Face("down", 7f, 3f, 21f, 24f, 0)}),
                new Cube("shoulder_mass", Joint.BODY, -8f, 27f, -16f, 8f, 41f, -5f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 66f, 106f, 82f, 120f, 0), new Face("east", 6f, 2f, 17f, 16f, 0), new Face("south", 6f, 12f, 22f, 26f, 0), new Face("west", 12f, 5f, 23f, 19f, 0), new Face("up", 11f, 18f, 27f, 29f, 0), new Face("down", 13f, 9f, 29f, 20f, 0)}),
                new Cube("hindquarter_mass", Joint.BODY, -8f, 27f, 8f, 8f, 40f, 20f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 0f, 2f, 16f, 15f, 0), new Face("east", 2f, 13f, 14f, 26f, 0), new Face("south", 4f, 5f, 20f, 18f, 0), new Face("west", 8f, 16f, 20f, 29f, 0), new Face("up", 9f, 8f, 25f, 20f, 0), new Face("down", 11f, 0f, 27f, 12f, 0)}),
                new Cube("neck_block", Joint.NECK, -4f, 33f, -15f, 4f, 50f, -8f, 0f, 35f, -11f, -24f, 0f, 0f, new Face[]{new Face("north", 84f, 96f, 92f, 113f, 0), new Face("east", 0f, 10f, 7f, 27f, 0), new Face("south", 3f, 4f, 11f, 21f, 0), new Face("west", 7f, 12f, 14f, 29f, 0), new Face("up", 10f, 10f, 18f, 17f, 0), new Face("down", 13f, 0f, 21f, 7f, 0)}),
                new Cube("head_block", Joint.HEAD, -5f, 45f, -22f, 5f, 55f, -14f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 32f, 32f, 42f, 42f, 0), new Face("east", 20f, 6f, 28f, 16f, 0), new Face("south", 0f, 0f, 10f, 10f, 0), new Face("west", 3f, 12f, 11f, 22f, 0), new Face("up", 6f, 3f, 16f, 11f, 0), new Face("down", 9f, 17f, 19f, 25f, 0)}),
                new Cube("muzzle_block", Joint.HEAD, -3f, 46f, -27f, 3f, 51f, -21f, 0f, 48.5f, -24f, -6f, 0f, 0f, new Face[]{new Face("north", 64f, 32f, 70f, 37f, 0), new Face("east", 107f, 0f, 113f, 5f, 0), new Face("south", 110f, 15f, 116f, 20f, 0), new Face("west", 114f, 3f, 120f, 8f, 0), new Face("up", 118f, 18f, 124f, 24f, 0), new Face("down", 96f, 11f, 102f, 17f, 0)}),
                new Cube("ear_left", Joint.EARS, -9f, 48f, -19f, -5f, 52f, -14f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 96f, 32f, 100f, 36f, 0), new Face("east", 7f, 16f, 12f, 20f, 0), new Face("south", 12f, 4f, 16f, 8f, 0), new Face("west", 15f, 20f, 20f, 24f, 0), new Face("up", 20f, 7f, 24f, 12f, 0), new Face("down", 24f, 23f, 28f, 28f, 0)}),
                new Cube("ear_right", Joint.EARS, 5f, 48f, -19f, 9f, 52f, -14f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 96f, 32f, 100f, 36f, 0), new Face("east", 19f, 24f, 24f, 28f, 0), new Face("south", 24f, 12f, 28f, 16f, 0), new Face("west", 0f, 4f, 5f, 8f, 0), new Face("up", 4f, 19f, 8f, 24f, 0), new Face("down", 8f, 7f, 12f, 12f, 0)}),
                new Cube("antler_root_left", Joint.ANTLERS, -5.5f, 53f, -19f, -2.5f, 58f, -15f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 0f, 64f, 3f, 69f, 0), new Face("east", 4f, 79f, 8f, 84f, 0), new Face("south", 8f, 67f, 11f, 72f, 0), new Face("west", 12f, 83f, 16f, 88f, 0), new Face("up", 16f, 72f, 19f, 76f, 0), new Face("down", 20f, 88f, 23f, 92f, 0)}),
                new Cube("antler_stem_left", Joint.ANTLERS, -8f, 56f, -19f, -5f, 64f, -15f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 12f, 81f, 15f, 89f, 0), new Face("east", 16f, 70f, 20f, 78f, 0), new Face("south", 20f, 84f, 23f, 92f, 0), new Face("west", 24f, 74f, 28f, 82f, 0), new Face("up", 0f, 68f, 3f, 72f, 0), new Face("down", 4f, 84f, 7f, 88f, 0)}),
                new Cube("antler_outer_left", Joint.ANTLERS, -11.5f, 62f, -19f, -7.5f, 67f, -15f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 8f, 67f, 12f, 72f, 0), new Face("east", 12f, 83f, 16f, 88f, 0), new Face("south", 16f, 71f, 20f, 76f, 0), new Face("west", 20f, 87f, 24f, 92f, 0), new Face("up", 24f, 76f, 28f, 80f, 0), new Face("down", 0f, 68f, 4f, 72f, 0)}),
                new Cube("antler_inner_tine_left", Joint.ANTLERS, -7.5f, 60f, -19f, -4.5f, 67f, -15f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 24f, 74f, 27f, 81f, 0), new Face("east", 0f, 67f, 4f, 74f, 0), new Face("south", 4f, 81f, 7f, 88f, 0), new Face("west", 8f, 71f, 12f, 78f, 0), new Face("up", 12f, 88f, 15f, 92f, 0), new Face("down", 16f, 76f, 19f, 80f, 0)}),
                new Cube("antler_tip_left", Joint.ANTLERS, -12.5f, 66f, -19f, -9f, 72f, -15f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 0f, 78f, 3.5f, 84f, 0), new Face("east", 4f, 67f, 8f, 73f, 0), new Face("south", 8f, 82f, 11.5f, 88f, 0), new Face("west", 12f, 71f, 16f, 77f, 0), new Face("up", 16f, 88f, 19.5f, 92f, 0), new Face("down", 20f, 76f, 23.5f, 80f, 0)}),
                new Cube("antler_root_right", Joint.ANTLERS, 2.5f, 53f, -19f, 5.5f, 58f, -15f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 12f, 71f, 15f, 76f, 0), new Face("east", 16f, 87f, 20f, 92f, 0), new Face("south", 20f, 75f, 23f, 80f, 0), new Face("west", 24f, 64f, 28f, 69f, 0), new Face("up", 0f, 84f, 3f, 88f, 0), new Face("down", 4f, 72f, 7f, 76f, 0)}),
                new Cube("antler_stem_right", Joint.ANTLERS, 5f, 56f, -19f, 8f, 64f, -15f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 24f, 64f, 27f, 72f, 0), new Face("east", 0f, 81f, 4f, 89f, 0), new Face("south", 4f, 70f, 7f, 78f, 0), new Face("west", 8f, 84f, 12f, 92f, 0), new Face("up", 12f, 76f, 15f, 80f, 0), new Face("down", 16f, 64f, 19f, 68f, 0)}),
                new Cube("antler_outer_right", Joint.ANTLERS, 7.5f, 62f, -19f, 11.5f, 67f, -15f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 20f, 75f, 24f, 80f, 0), new Face("east", 24f, 64f, 28f, 69f, 0), new Face("south", 0f, 83f, 4f, 88f, 0), new Face("west", 4f, 71f, 8f, 76f, 0), new Face("up", 8f, 88f, 12f, 92f, 0), new Face("down", 12f, 76f, 16f, 80f, 0)}),
                new Cube("antler_inner_tine_right", Joint.ANTLERS, 4.5f, 60f, -19f, 7.5f, 67f, -15f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 8f, 85f, 11f, 92f, 0), new Face("east", 12f, 74f, 16f, 81f, 0), new Face("south", 16f, 64f, 19f, 71f, 0), new Face("west", 20f, 78f, 24f, 85f, 0), new Face("up", 24f, 68f, 27f, 72f, 0), new Face("down", 0f, 88f, 3f, 92f, 0)}),
                new Cube("antler_tip_right", Joint.ANTLERS, 9f, 66f, -19f, 12.5f, 72f, -15f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 12f, 86f, 15.5f, 92f, 0), new Face("east", 16f, 75f, 20f, 81f, 0), new Face("south", 20f, 64f, 23.5f, 70f, 0), new Face("west", 24f, 78f, 28f, 84f, 0), new Face("up", 0f, 72f, 3.5f, 76f, 0), new Face("down", 4f, 88f, 7.5f, 92f, 0)}),
                new Cube("tail_block", Joint.TAIL, -1.5f, 28f, 18f, 1.5f, 31f, 23f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 36f, 8f, 39f, 11f, 0), new Face("east", 39f, 24f, 44f, 27f, 0), new Face("south", 44f, 12f, 47f, 15f, 0), new Face("west", 47f, 0f, 52f, 3f, 0), new Face("up", 52f, 15f, 55f, 20f, 0), new Face("down", 56f, 3f, 59f, 8f, 0)}),
                new Cube("front_thigh_left", Joint.LEFT_FRONT_LEG, -7f, 10f, -14f, -2.5f, 29f, -7f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 3f, 9f, 7.5f, 28f, 0), new Face("east", 7f, 3f, 14f, 22f, 0), new Face("south", 11f, 11f, 15.5f, 30f, 0), new Face("west", 14f, 5f, 21f, 24f, 0), new Face("up", 19f, 0f, 23.5f, 7f, 0), new Face("down", 23f, 14f, 27.5f, 21f, 0)}),
                new Cube("front_shin_left", Joint.LEFT_FRONT_LEG, -6f, 2f, -13f, -3f, 13f, -8f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 68f, 15f, 71f, 26f, 0), new Face("east", 71f, 6f, 76f, 17f, 0), new Face("south", 76f, 18f, 79f, 29f, 0), new Face("west", 79f, 9f, 84f, 20f, 0), new Face("up", 84f, 0f, 87f, 5f, 0), new Face("down", 88f, 15f, 91f, 20f, 0)}),
                new Cube("front_hoof_left", Joint.LEFT_FRONT_LEG, -7f, 0f, -14f, -2f, 3f, -8f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 39f, 112f, 44f, 115f, 0), new Face("east", 43f, 100f, 49f, 103f, 0), new Face("south", 47f, 116f, 52f, 119f, 0), new Face("west", 50f, 104f, 56f, 107f, 0), new Face("up", 55f, 118f, 60f, 124f, 0), new Face("down", 32f, 110f, 37f, 116f, 0)}),
                new Cube("front_thigh_right", Joint.RIGHT_FRONT_LEG, 2.5f, 10f, -14f, 7f, 29f, -7f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 15f, 0f, 19.5f, 19f, 0), new Face("east", 17f, 7f, 24f, 26f, 0), new Face("south", 23f, 1f, 27.5f, 20f, 0), new Face("west", 0f, 11f, 7f, 30f, 0), new Face("up", 3f, 10f, 7.5f, 17f, 0), new Face("down", 7f, 0f, 11.5f, 7f, 0)}),
                new Cube("front_shin_right", Joint.RIGHT_FRONT_LEG, 3f, 2f, -13f, 6f, 13f, -8f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 80f, 0f, 83f, 11f, 0), new Face("east", 83f, 12f, 88f, 23f, 0), new Face("south", 88f, 3f, 91f, 14f, 0), new Face("west", 64f, 18f, 69f, 29f, 0), new Face("up", 68f, 11f, 71f, 16f, 0), new Face("down", 72f, 0f, 75f, 5f, 0)}),
                new Cube("front_hoof_right", Joint.RIGHT_FRONT_LEG, 2f, 0f, -14f, 7f, 3f, -8f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 51f, 120f, 56f, 123f, 0), new Face("east", 54f, 108f, 60f, 111f, 0), new Face("south", 32f, 100f, 37f, 103f, 0), new Face("west", 35f, 116f, 41f, 119f, 0), new Face("up", 39f, 103f, 44f, 109f, 0), new Face("down", 43f, 118f, 48f, 124f, 0)}),
                new Cube("hind_thigh_left", Joint.LEFT_HIND_LEG, -7f, 10f, 9f, -2.5f, 29f, 17f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 0f, 0f, 4.5f, 19f, 0), new Face("east", 3f, 7f, 11f, 26f, 0), new Face("south", 7f, 1f, 11.5f, 20f, 0), new Face("west", 10f, 9f, 18f, 28f, 0), new Face("up", 15f, 6f, 19.5f, 14f, 0), new Face("down", 19f, 20f, 23.5f, 28f, 0)}),
                new Cube("hind_shin_left", Joint.LEFT_HIND_LEG, -6f, 2f, 13f, -3f, 13f, 18f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 64f, 0f, 67f, 11f, 0), new Face("east", 67f, 12f, 72f, 23f, 0), new Face("south", 72f, 3f, 75f, 14f, 0), new Face("west", 75f, 15f, 80f, 26f, 0), new Face("up", 80f, 7f, 83f, 12f, 0), new Face("down", 84f, 23f, 87f, 28f, 0)}),
                new Cube("hind_hoof_left", Joint.LEFT_HIND_LEG, -7f, 0f, 13f, -2f, 3f, 19f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 35f, 120f, 40f, 123f, 0), new Face("east", 39f, 108f, 45f, 111f, 0), new Face("south", 43f, 96f, 48f, 99f, 0), new Face("west", 46f, 112f, 52f, 115f, 0), new Face("up", 51f, 99f, 56f, 105f, 0), new Face("down", 55f, 114f, 60f, 120f, 0)}),
                new Cube("hind_thigh_right", Joint.RIGHT_HIND_LEG, 2.5f, 10f, 9f, 7f, 29f, 17f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 11f, 3f, 15.5f, 22f, 0), new Face("east", 13f, 11f, 21f, 30f, 0), new Face("south", 19f, 5f, 23.5f, 24f, 0), new Face("west", 20f, 0f, 28f, 19f, 0), new Face("up", 0f, 17f, 4.5f, 25f, 0), new Face("down", 3f, 6f, 7.5f, 14f, 0)}),
                new Cube("hind_shin_right", Joint.RIGHT_HIND_LEG, 3f, 2f, 13f, 6f, 13f, 18f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 76f, 6f, 79f, 17f, 0), new Face("east", 79f, 18f, 84f, 29f, 0), new Face("south", 84f, 9f, 87f, 20f, 0), new Face("west", 87f, 0f, 92f, 11f, 0), new Face("up", 64f, 19f, 67f, 24f, 0), new Face("down", 68f, 7f, 71f, 12f, 0)}),
                new Cube("hind_hoof_right", Joint.RIGHT_HIND_LEG, 2f, 0f, 13f, 7f, 3f, 19f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 47f, 100f, 52f, 103f, 0), new Face("east", 50f, 116f, 56f, 119f, 0), new Face("south", 55f, 104f, 60f, 107f, 0), new Face("west", 32f, 96f, 38f, 99f, 0), new Face("up", 35f, 110f, 40f, 116f, 0), new Face("down", 39f, 99f, 44f, 105f, 0)})})),
            Map.entry(Species.STARFLEECE_RAM, new SpeciesLayer(Species.STARFLEECE_RAM,
                new Bone[]{new Bone(Joint.BODY, 0.00000f, 26.00000f, 0.00000f),
                new Bone(Joint.CHEST, 0.00000f, 22.00000f, -10.00000f),
                new Bone(Joint.ABDOMEN, 0.00000f, 18.00000f, 3.00000f),
                new Bone(Joint.HEAD, 0.00000f, 34.00000f, -20.00000f),
                new Bone(Joint.EARS, 0.00000f, 35.00000f, -16.00000f),
                new Bone(Joint.ANTLERS, 0.00000f, 40.00000f, -18.00000f),
                new Bone(Joint.TAIL, 0.00000f, 26.00000f, 20.00000f),
                new Bone(Joint.LEFT_FRONT_LEG, -4.75000f, 22.00000f, -10.50000f),
                new Bone(Joint.RIGHT_FRONT_LEG, 4.75000f, 22.00000f, -10.50000f),
                new Bone(Joint.LEFT_HIND_LEG, -4.75000f, 22.00000f, 13.00000f),
                new Bone(Joint.RIGHT_HIND_LEG, 4.75000f, 22.00000f, 13.00000f)},
                new Cube[]{new Cube("body_core", Joint.BODY, -8f, 18f, -8f, 8f, 34f, 12f, 0f, 26f, 2f, 0f, 0f, 0f, new Face[]{new Face("north", 0f, 0f, 16f, 16f, 0), new Face("east", 0f, 0f, 20f, 16f, 0), new Face("south", 0f, 0f, 16f, 16f, 0), new Face("west", 0f, 0f, 20f, 16f, 0), new Face("up", 0f, 0f, 16f, 20f, 0), new Face("down", 0f, 0f, 16f, 20f, 0)}),
                new Cube("shoulder_mass", Joint.BODY, -9f, 20f, -15f, 9f, 35f, -5f, 0f, 27.5f, -10f, 0f, 0f, 0f, new Face[]{new Face("north", 0f, 32f, 18f, 47f, 0), new Face("east", 0f, 32f, 10f, 47f, 0), new Face("south", 0f, 32f, 18f, 47f, 0), new Face("west", 0f, 32f, 10f, 47f, 0), new Face("up", 0f, 32f, 18f, 42f, 0), new Face("down", 0f, 32f, 18f, 42f, 0)}),
                new Cube("hindquarter_mass", Joint.BODY, -9f, 19f, 9f, 9f, 34f, 19f, 0f, 26.5f, 14f, 0f, 0f, 0f, new Face[]{new Face("north", 0f, 0f, 18f, 15f, 0), new Face("east", 0f, 0f, 10f, 15f, 0), new Face("south", 0f, 0f, 18f, 15f, 0), new Face("west", 0f, 0f, 10f, 15f, 0), new Face("up", 0f, 0f, 18f, 10f, 0), new Face("down", 0f, 0f, 18f, 10f, 0)}),
                new Cube("chest_patch", Joint.CHEST, -5f, 12f, -18f, 5f, 29f, -4f, 0f, 20.5f, -11f, 0f, 0f, 0f, new Face[]{new Face("north", 32f, 0f, 42f, 17f, 0), new Face("east", 32f, 0f, 46f, 17f, 0), new Face("south", 32f, 0f, 42f, 17f, 0), new Face("west", 32f, 0f, 46f, 17f, 0), new Face("up", 32f, 0f, 42f, 14f, 0), new Face("down", 32f, 0f, 42f, 14f, 0)}),
                new Cube("belly_patch", Joint.ABDOMEN, -7f, 14f, -5f, 7f, 22f, 14f, 0f, 18f, 4.5f, 0f, 0f, 0f, new Face[]{new Face("north", 32f, 0f, 46f, 8f, 0), new Face("east", 32f, 0f, 51f, 8f, 0), new Face("south", 32f, 0f, 46f, 8f, 0), new Face("west", 32f, 0f, 51f, 8f, 0), new Face("up", 32f, 0f, 46f, 19f, 0), new Face("down", 32f, 0f, 46f, 19f, 0)}),
                new Cube("head_block", Joint.HEAD, -5.5f, 30f, -22f, 5.5f, 40f, -13f, 0f, 35f, -17.5f, 0f, 0f, 0f, new Face[]{new Face("north", 0f, 32f, 11f, 42f, 0), new Face("east", 0f, 32f, 9f, 42f, 0), new Face("south", 0f, 32f, 11f, 42f, 0), new Face("west", 0f, 32f, 9f, 42f, 0), new Face("up", 0f, 32f, 11f, 41f, 0), new Face("down", 0f, 32f, 11f, 41f, 0)}),
                new Cube("face_block", Joint.HEAD, -4.3f, 22f, -25f, 4.3f, 34f, -17f, 0f, 28f, -21f, 0f, 0f, 0f, new Face[]{new Face("north", 43f, 43f, 52f, 55f, 0), new Face("east", 32f, 0f, 40f, 12f, 0), new Face("south", 32f, 0f, 40.6f, 12f, 0), new Face("west", 32f, 0f, 40f, 12f, 0), new Face("up", 32f, 0f, 40.6f, 8f, 0), new Face("down", 32f, 0f, 40.6f, 8f, 0)}),
                new Cube("muzzle_block", Joint.HEAD, -3.5f, 22f, -28f, 3.5f, 27f, -23f, 0f, 24.5f, -25.5f, 0f, 0f, 0f, new Face[]{new Face("north", 32f, 57f, 39f, 62f, 0), new Face("east", 32f, 0f, 37f, 5f, 0), new Face("south", 32f, 0f, 39f, 5f, 0), new Face("west", 32f, 0f, 37f, 5f, 0), new Face("up", 32f, 0f, 39f, 5f, 0), new Face("down", 32f, 0f, 39f, 5f, 0)}),
                new Cube("ear_left", Joint.EARS, -9.5f, 33f, -18f, -5f, 37f, -13f, -7.25f, 35f, -15.5f, 0f, 0f, 0f, new Face[]{new Face("north", 96f, 0f, 100.5f, 4f, 0), new Face("east", 0f, 0f, 5f, 4f, 0), new Face("south", 0f, 0f, 4.5f, 4f, 0), new Face("west", 0f, 0f, 5f, 4f, 0), new Face("up", 0f, 0f, 4.5f, 5f, 0), new Face("down", 0f, 0f, 4.5f, 5f, 0)}),
                new Cube("ear_right", Joint.EARS, 5f, 33f, -18f, 9.5f, 37f, -13f, 7.25f, 35f, -15.5f, 0f, 0f, 0f, new Face[]{new Face("north", 96f, 0f, 100.5f, 4f, 0), new Face("east", 0f, 32f, 5f, 36f, 0), new Face("south", 0f, 32f, 4.5f, 36f, 0), new Face("west", 0f, 32f, 5f, 36f, 0), new Face("up", 0f, 32f, 4.5f, 37f, 0), new Face("down", 0f, 32f, 4.5f, 37f, 0)}),
                new Cube("horn_root_left", Joint.ANTLERS, -7f, 35f, -22f, -3.5f, 42f, -15f, -5.25f, 38.5f, -18.5f, 0f, 0f, 0f, new Face[]{new Face("north", 64f, 0f, 67.5f, 7f, 0), new Face("east", 64f, 0f, 71f, 7f, 0), new Face("south", 64f, 0f, 67.5f, 7f, 0), new Face("west", 64f, 0f, 71f, 7f, 0), new Face("up", 64f, 0f, 67.5f, 7f, 0), new Face("down", 64f, 32f, 67.5f, 39f, 0)}),
                new Cube("horn_rise_left", Joint.ANTLERS, -9f, 40f, -23f, -5f, 47f, -16f, -7f, 43.5f, -19.5f, 0f, 0f, 0f, new Face[]{new Face("north", 64f, 0f, 68f, 7f, 0), new Face("east", 64f, 0f, 71f, 7f, 0), new Face("south", 64f, 0f, 68f, 7f, 0), new Face("west", 64f, 0f, 71f, 7f, 0), new Face("up", 64f, 0f, 68f, 7f, 0), new Face("down", 64f, 32f, 68f, 39f, 0)}),
                new Cube("horn_outer_left", Joint.ANTLERS, -12.5f, 39f, -20f, -8.5f, 47f, -11f, -10.5f, 43f, -15.5f, 0f, 0f, 0f, new Face[]{new Face("north", 64f, 0f, 68f, 8f, 0), new Face("east", 64f, 0f, 73f, 8f, 0), new Face("south", 64f, 0f, 68f, 8f, 0), new Face("west", 64f, 0f, 73f, 8f, 0), new Face("up", 64f, 0f, 68f, 9f, 0), new Face("down", 64f, 32f, 68f, 41f, 0)}),
                new Cube("horn_lower_left", Joint.ANTLERS, -10.5f, 26f, -10f, -7f, 34f, -3f, -8.75f, 30f, -6.5f, 0f, 0f, 0f, new Face[]{new Face("north", 64f, 0f, 67.5f, 8f, 0), new Face("east", 64f, 0f, 71f, 8f, 0), new Face("south", 64f, 0f, 67.5f, 8f, 0), new Face("west", 64f, 0f, 71f, 8f, 0), new Face("up", 64f, 0f, 67.5f, 7f, 0), new Face("down", 64f, 32f, 67.5f, 39f, 0)}),
                new Cube("horn_tip_left", Joint.ANTLERS, -8.5f, 28f, -17f, -4.5f, 33f, -8f, -6.5f, 30.5f, -12.5f, 0f, 0f, 0f, new Face[]{new Face("north", 64f, 0f, 68f, 5f, 0), new Face("east", 64f, 0f, 73f, 5f, 0), new Face("south", 64f, 0f, 68f, 5f, 0), new Face("west", 64f, 0f, 73f, 5f, 0), new Face("up", 64f, 0f, 68f, 9f, 0), new Face("down", 64f, 32f, 68f, 41f, 0)}),
                new Cube("horn_back_left", Joint.ANTLERS, -12f, 32f, -12f, -8.5f, 42f, -5f, -10.25f, 37f, -8.5f, 0f, 0f, 0f, new Face[]{new Face("north", 64f, 0f, 67.5f, 10f, 0), new Face("east", 64f, 0f, 71f, 10f, 0), new Face("south", 64f, 0f, 67.5f, 10f, 0), new Face("west", 64f, 0f, 71f, 10f, 0), new Face("up", 64f, 0f, 67.5f, 7f, 0), new Face("down", 64f, 32f, 67.5f, 39f, 0)}),
                new Cube("horn_inner_left", Joint.ANTLERS, -7.5f, 32f, -22f, -4f, 38f, -16f, -5.75f, 35f, -19f, 0f, 0f, 0f, new Face[]{new Face("north", 64f, 0f, 67.5f, 6f, 0), new Face("east", 64f, 0f, 70f, 6f, 0), new Face("south", 64f, 0f, 67.5f, 6f, 0), new Face("west", 64f, 0f, 70f, 6f, 0), new Face("up", 64f, 0f, 67.5f, 6f, 0), new Face("down", 64f, 32f, 67.5f, 38f, 0)}),
                new Cube("horn_root_right", Joint.ANTLERS, 3.5f, 35f, -22f, 7f, 42f, -15f, 5.25f, 38.5f, -18.5f, 0f, 0f, 0f, new Face[]{new Face("north", 64f, 0f, 67.5f, 7f, 0), new Face("east", 64f, 0f, 71f, 7f, 0), new Face("south", 64f, 0f, 67.5f, 7f, 0), new Face("west", 64f, 0f, 71f, 7f, 0), new Face("up", 64f, 0f, 67.5f, 7f, 0), new Face("down", 64f, 32f, 67.5f, 39f, 0)}),
                new Cube("horn_rise_right", Joint.ANTLERS, 5f, 40f, -23f, 9f, 47f, -16f, 7f, 43.5f, -19.5f, 0f, 0f, 0f, new Face[]{new Face("north", 64f, 0f, 68f, 7f, 0), new Face("east", 64f, 0f, 71f, 7f, 0), new Face("south", 64f, 0f, 68f, 7f, 0), new Face("west", 64f, 0f, 71f, 7f, 0), new Face("up", 64f, 0f, 68f, 7f, 0), new Face("down", 64f, 32f, 68f, 39f, 0)}),
                new Cube("horn_outer_right", Joint.ANTLERS, 8.5f, 39f, -20f, 12.5f, 47f, -11f, 10.5f, 43f, -15.5f, 0f, 0f, 0f, new Face[]{new Face("north", 64f, 0f, 68f, 8f, 0), new Face("east", 64f, 0f, 73f, 8f, 0), new Face("south", 64f, 0f, 68f, 8f, 0), new Face("west", 64f, 0f, 73f, 8f, 0), new Face("up", 64f, 0f, 68f, 9f, 0), new Face("down", 64f, 32f, 68f, 41f, 0)}),
                new Cube("horn_lower_right", Joint.ANTLERS, 7f, 26f, -10f, 10.5f, 34f, -3f, 8.75f, 30f, -6.5f, 0f, 0f, 0f, new Face[]{new Face("north", 64f, 0f, 67.5f, 8f, 0), new Face("east", 64f, 0f, 71f, 8f, 0), new Face("south", 64f, 0f, 67.5f, 8f, 0), new Face("west", 64f, 0f, 71f, 8f, 0), new Face("up", 64f, 0f, 67.5f, 7f, 0), new Face("down", 64f, 32f, 67.5f, 39f, 0)}),
                new Cube("horn_tip_right", Joint.ANTLERS, 4.5f, 28f, -17f, 8.5f, 33f, -8f, 6.5f, 30.5f, -12.5f, 0f, 0f, 0f, new Face[]{new Face("north", 64f, 0f, 68f, 5f, 0), new Face("east", 64f, 0f, 73f, 5f, 0), new Face("south", 64f, 0f, 68f, 5f, 0), new Face("west", 64f, 0f, 73f, 5f, 0), new Face("up", 64f, 0f, 68f, 9f, 0), new Face("down", 64f, 32f, 68f, 41f, 0)}),
                new Cube("horn_back_right", Joint.ANTLERS, 8.5f, 32f, -12f, 12f, 42f, -5f, 10.25f, 37f, -8.5f, 0f, 0f, 0f, new Face[]{new Face("north", 64f, 0f, 67.5f, 10f, 0), new Face("east", 64f, 0f, 71f, 10f, 0), new Face("south", 64f, 0f, 67.5f, 10f, 0), new Face("west", 64f, 0f, 71f, 10f, 0), new Face("up", 64f, 0f, 67.5f, 7f, 0), new Face("down", 64f, 32f, 67.5f, 39f, 0)}),
                new Cube("horn_inner_right", Joint.ANTLERS, 4f, 32f, -22f, 7.5f, 38f, -16f, 5.75f, 35f, -19f, 0f, 0f, 0f, new Face[]{new Face("north", 64f, 0f, 67.5f, 6f, 0), new Face("east", 64f, 0f, 70f, 6f, 0), new Face("south", 64f, 0f, 67.5f, 6f, 0), new Face("west", 64f, 0f, 70f, 6f, 0), new Face("up", 64f, 0f, 67.5f, 6f, 0), new Face("down", 64f, 32f, 67.5f, 38f, 0)}),
                new Cube("tail_block", Joint.TAIL, -1.5f, 25f, 18f, 1.5f, 28f, 22f, 0f, 26.5f, 20f, 0f, 0f, 0f, new Face[]{new Face("north", 0f, 0f, 3f, 3f, 0), new Face("east", 0f, 0f, 4f, 3f, 0), new Face("south", 0f, 0f, 3f, 3f, 0), new Face("west", 0f, 0f, 4f, 3f, 0), new Face("up", 0f, 0f, 3f, 4f, 0), new Face("down", 0f, 0f, 3f, 4f, 0)}),
                new Cube("front_thigh_left", Joint.LEFT_FRONT_LEG, -7f, 10f, -14f, -2.5f, 23f, -7f, -4.75f, 16.5f, -10.5f, 0f, 0f, 0f, new Face[]{new Face("north", 0f, 32f, 4.5f, 45f, 0), new Face("east", 0f, 32f, 7f, 45f, 0), new Face("south", 0f, 32f, 4.5f, 45f, 0), new Face("west", 0f, 32f, 7f, 45f, 0), new Face("up", 0f, 32f, 4.5f, 39f, 0), new Face("down", 0f, 32f, 4.5f, 39f, 0)}),
                new Cube("front_shin_left", Joint.LEFT_FRONT_LEG, -6f, 2f, -13f, -3f, 12f, -8f, -4.5f, 7f, -10.5f, 0f, 0f, 0f, new Face[]{new Face("north", 0f, 0f, 3f, 10f, 0), new Face("east", 0f, 0f, 5f, 10f, 0), new Face("south", 0f, 0f, 3f, 10f, 0), new Face("west", 0f, 0f, 5f, 10f, 0), new Face("up", 0f, 0f, 3f, 5f, 0), new Face("down", 0f, 0f, 3f, 5f, 0)}),
                new Cube("front_hoof_left", Joint.LEFT_FRONT_LEG, -7f, 0f, -14f, -2f, 3f, -8f, -4.5f, 1.5f, -11f, 0f, 0f, 0f, new Face[]{new Face("north", 96f, 32f, 101f, 35f, 0), new Face("east", 96f, 32f, 102f, 35f, 0), new Face("south", 96f, 32f, 101f, 35f, 0), new Face("west", 96f, 32f, 102f, 35f, 0), new Face("up", 96f, 32f, 101f, 38f, 0), new Face("down", 96f, 32f, 101f, 38f, 0)}),
                new Cube("front_thigh_right", Joint.RIGHT_FRONT_LEG, 2.5f, 10f, -14f, 7f, 23f, -7f, 4.75f, 16.5f, -10.5f, 0f, 0f, 0f, new Face[]{new Face("north", 0f, 0f, 4.5f, 13f, 0), new Face("east", 0f, 0f, 7f, 13f, 0), new Face("south", 0f, 0f, 4.5f, 13f, 0), new Face("west", 0f, 0f, 7f, 13f, 0), new Face("up", 0f, 0f, 4.5f, 7f, 0), new Face("down", 0f, 0f, 4.5f, 7f, 0)}),
                new Cube("front_shin_right", Joint.RIGHT_FRONT_LEG, 3f, 2f, -13f, 6f, 12f, -8f, 4.5f, 7f, -10.5f, 0f, 0f, 0f, new Face[]{new Face("north", 0f, 32f, 3f, 42f, 0), new Face("east", 0f, 32f, 5f, 42f, 0), new Face("south", 0f, 32f, 3f, 42f, 0), new Face("west", 0f, 32f, 5f, 42f, 0), new Face("up", 0f, 32f, 3f, 37f, 0), new Face("down", 0f, 32f, 3f, 37f, 0)}),
                new Cube("front_hoof_right", Joint.RIGHT_FRONT_LEG, 2f, 0f, -14f, 7f, 3f, -8f, 4.5f, 1.5f, -11f, 0f, 0f, 0f, new Face[]{new Face("north", 96f, 32f, 101f, 35f, 0), new Face("east", 96f, 32f, 102f, 35f, 0), new Face("south", 96f, 32f, 101f, 35f, 0), new Face("west", 96f, 32f, 102f, 35f, 0), new Face("up", 96f, 32f, 101f, 38f, 0), new Face("down", 96f, 32f, 101f, 38f, 0)}),
                new Cube("hind_thigh_left", Joint.LEFT_HIND_LEG, -7f, 10f, 9f, -2.5f, 23f, 17f, -4.75f, 16.5f, 13f, 0f, 0f, 0f, new Face[]{new Face("north", 0f, 32f, 4.5f, 45f, 0), new Face("east", 0f, 32f, 8f, 45f, 0), new Face("south", 0f, 32f, 4.5f, 45f, 0), new Face("west", 0f, 32f, 8f, 45f, 0), new Face("up", 0f, 32f, 4.5f, 40f, 0), new Face("down", 0f, 32f, 4.5f, 40f, 0)}),
                new Cube("hind_shin_left", Joint.LEFT_HIND_LEG, -6f, 2f, 13f, -3f, 12f, 18f, -4.5f, 7f, 15.5f, 0f, 0f, 0f, new Face[]{new Face("north", 0f, 0f, 3f, 10f, 0), new Face("east", 0f, 0f, 5f, 10f, 0), new Face("south", 0f, 0f, 3f, 10f, 0), new Face("west", 0f, 0f, 5f, 10f, 0), new Face("up", 0f, 0f, 3f, 5f, 0), new Face("down", 0f, 0f, 3f, 5f, 0)}),
                new Cube("hind_hoof_left", Joint.LEFT_HIND_LEG, -7f, 0f, 13f, -2f, 3f, 19f, -4.5f, 1.5f, 16f, 0f, 0f, 0f, new Face[]{new Face("north", 96f, 32f, 101f, 35f, 0), new Face("east", 96f, 32f, 102f, 35f, 0), new Face("south", 96f, 32f, 101f, 35f, 0), new Face("west", 96f, 32f, 102f, 35f, 0), new Face("up", 96f, 32f, 101f, 38f, 0), new Face("down", 96f, 32f, 101f, 38f, 0)}),
                new Cube("hind_thigh_right", Joint.RIGHT_HIND_LEG, 2.5f, 10f, 9f, 7f, 23f, 17f, 4.75f, 16.5f, 13f, 0f, 0f, 0f, new Face[]{new Face("north", 0f, 0f, 4.5f, 13f, 0), new Face("east", 0f, 0f, 8f, 13f, 0), new Face("south", 0f, 0f, 4.5f, 13f, 0), new Face("west", 0f, 0f, 8f, 13f, 0), new Face("up", 0f, 0f, 4.5f, 8f, 0), new Face("down", 0f, 0f, 4.5f, 8f, 0)}),
                new Cube("hind_shin_right", Joint.RIGHT_HIND_LEG, 3f, 2f, 13f, 6f, 12f, 18f, 4.5f, 7f, 15.5f, 0f, 0f, 0f, new Face[]{new Face("north", 0f, 32f, 3f, 42f, 0), new Face("east", 0f, 32f, 5f, 42f, 0), new Face("south", 0f, 32f, 3f, 42f, 0), new Face("west", 0f, 32f, 5f, 42f, 0), new Face("up", 0f, 32f, 3f, 37f, 0), new Face("down", 0f, 32f, 3f, 37f, 0)}),
                new Cube("hind_hoof_right", Joint.RIGHT_HIND_LEG, 2f, 0f, 13f, 7f, 3f, 19f, 4.5f, 1.5f, 16f, 0f, 0f, 0f, new Face[]{new Face("north", 96f, 32f, 101f, 35f, 0), new Face("east", 96f, 32f, 102f, 35f, 0), new Face("south", 96f, 32f, 101f, 35f, 0), new Face("west", 96f, 32f, 102f, 35f, 0), new Face("up", 96f, 32f, 101f, 38f, 0), new Face("down", 96f, 32f, 101f, 38f, 0)})})),
            Map.entry(Species.WOODLAND_FORAGER, new SpeciesLayer(Species.WOODLAND_FORAGER,
                new Bone[]{new Bone(Joint.BODY, 0.00000f, 21.00000f, 1.00000f),
                new Bone(Joint.ABDOMEN, 0.00000f, 13.50000f, 2.00000f),
                new Bone(Joint.HEAD, 0.00000f, 23.00000f, -24.00000f),
                new Bone(Joint.EARS, 0.00000f, 32.00000f, -18.00000f),
                new Bone(Joint.BACK_RIDGE, 0.00000f, 30.50000f, 2.50000f),
                new Bone(Joint.TAIL, 0.00000f, 22.50000f, 24.00000f),
                new Bone(Joint.LEFT_FRONT_LEG, -8.50000f, 20.00000f, -13.00000f),
                new Bone(Joint.RIGHT_FRONT_LEG, 8.50000f, 20.00000f, -13.00000f),
                new Bone(Joint.LEFT_HIND_LEG, -8.50000f, 20.00000f, 14.66667f),
                new Bone(Joint.RIGHT_HIND_LEG, 8.50000f, 20.00000f, 14.66667f)},
                new Cube[]{new Cube("body_core", Joint.BODY, -12f, 12f, -13f, 12f, 30f, 15f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 5f, 4f, 29f, 22f, 0), new Face("east", 3f, 12f, 31f, 30f, 0), new Face("south", 0f, 8f, 24f, 26f, 0), new Face("west", 0f, 2f, 28f, 20f, 0), new Face("up", 2f, 2f, 26f, 30f, 0), new Face("down", 3f, 1f, 27f, 29f, 0)}),
                new Cube("shoulder_mass", Joint.BODY, -12f, 14f, -20f, 12f, 29f, -7f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 1f, 9f, 25f, 24f, 0), new Face("east", 5f, 2f, 18f, 17f, 0), new Face("south", 3f, 12f, 27f, 27f, 0), new Face("west", 10f, 4f, 23f, 19f, 0), new Face("up", 5f, 16f, 29f, 29f, 0), new Face("down", 6f, 8f, 30f, 21f, 0)}),
                new Cube("hindquarter_mass", Joint.BODY, -12f, 13f, 10f, 12f, 29f, 22f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 0f, 2f, 24f, 18f, 0), new Face("east", 2f, 11f, 14f, 27f, 0), new Face("south", 2f, 4f, 26f, 20f, 0), new Face("west", 8f, 13f, 20f, 29f, 0), new Face("up", 4f, 8f, 28f, 20f, 0), new Face("down", 5f, 0f, 29f, 12f, 0)}),
                new Cube("belly_mass", Joint.ABDOMEN, -10f, 8f, -10f, 10f, 19f, 14f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 101f, 15f, 121f, 26f, 0), new Face("east", 100f, 6f, 124f, 17f, 0), new Face("south", 104f, 18f, 124f, 29f, 0), new Face("west", 102f, 9f, 126f, 20f, 0), new Face("up", 96f, 1f, 116f, 25f, 0), new Face("down", 97f, 5f, 117f, 29f, 0)}),
                new Cube("head_block", Joint.HEAD, -8f, 20f, -26f, 8f, 32f, -14f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 32f, 32f, 48f, 44f, 0), new Face("east", 17f, 5f, 29f, 17f, 0), new Face("south", 0f, 0f, 16f, 12f, 0), new Face("west", 2f, 11f, 14f, 23f, 0), new Face("up", 4f, 2f, 20f, 14f, 0), new Face("down", 6f, 14f, 22f, 26f, 0)}),
                new Cube("snout_block", Joint.HEAD, -5.5f, 17f, -34f, 5.5f, 25f, -23f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 64f, 32f, 75f, 40f, 0), new Face("east", 111f, 17f, 122f, 25f, 0), new Face("south", 114f, 6f, 125f, 14f, 0), new Face("west", 96f, 0f, 107f, 8f, 0), new Face("up", 99f, 12f, 110f, 23f, 0), new Face("down", 102f, 3f, 113f, 14f, 0)}),
                new Cube("jaw_block", Joint.HEAD, -5.5f, 14f, -30f, 5.5f, 19f, -22f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 64f, 32f, 75f, 37f, 0), new Face("east", 106f, 19f, 114f, 24f, 0), new Face("south", 108f, 7f, 119f, 12f, 0), new Face("west", 113f, 23f, 121f, 28f, 0), new Face("up", 114f, 10f, 125f, 18f, 0), new Face("down", 96f, 3f, 107f, 11f, 0)}),
                new Cube("ear_left", Joint.EARS, -12f, 29f, -21f, -7f, 35f, -15f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 96f, 32f, 101f, 38f, 0), new Face("east", 7f, 14f, 13f, 20f, 0), new Face("south", 11f, 3f, 16f, 9f, 0), new Face("west", 14f, 18f, 20f, 24f, 0), new Face("up", 19f, 7f, 24f, 13f, 0), new Face("down", 23f, 22f, 28f, 28f, 0)}),
                new Cube("ear_right", Joint.EARS, 7f, 29f, -21f, 12f, 35f, -15f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 96f, 32f, 101f, 38f, 0), new Face("east", 18f, 22f, 24f, 28f, 0), new Face("south", 23f, 11f, 28f, 17f, 0), new Face("west", 0f, 3f, 6f, 9f, 0), new Face("up", 3f, 18f, 8f, 24f, 0), new Face("down", 7f, 7f, 12f, 13f, 0)}),
                new Cube("ridge_front", Joint.BACK_RIDGE, -4f, 29f, -7f, 4f, 32f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 38f, 88f, 46f, 91f, 0), new Face("east", 42f, 76f, 50f, 79f, 0), new Face("south", 45f, 64f, 53f, 67f, 0), new Face("west", 49f, 80f, 57f, 83f, 0), new Face("up", 52f, 67f, 60f, 75f, 0), new Face("down", 32f, 84f, 40f, 92f, 0)}),
                new Cube("ridge_back", Joint.BACK_RIDGE, -3f, 29f, 1f, 3f, 32f, 12f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 46f, 84f, 52f, 87f, 0), new Face("east", 47f, 72f, 58f, 75f, 0), new Face("south", 54f, 88f, 60f, 91f, 0), new Face("west", 32f, 80f, 43f, 83f, 0), new Face("up", 35f, 67f, 41f, 78f, 0), new Face("down", 39f, 79f, 45f, 90f, 0)}),
                new Cube("tail_block", Joint.TAIL, -1.5f, 21f, 21f, 1.5f, 24f, 27f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 4f, 8f, 7f, 11f, 0), new Face("east", 7f, 24f, 13f, 27f, 0), new Face("south", 12f, 12f, 15f, 15f, 0), new Face("west", 14f, 0f, 20f, 3f, 0), new Face("up", 20f, 14f, 23f, 20f, 0), new Face("down", 24f, 3f, 27f, 9f, 0)}),
                new Cube("front_thigh_left", Joint.LEFT_FRONT_LEG, -11f, 6f, -17f, -6f, 20f, -9f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 3f, 12f, 8f, 26f, 0), new Face("east", 6f, 5f, 14f, 19f, 0), new Face("south", 11f, 15f, 16f, 29f, 0), new Face("west", 13f, 7f, 21f, 21f, 0), new Face("up", 19f, 0f, 24f, 8f, 0), new Face("down", 23f, 13f, 28f, 21f, 0)}),
                new Cube("front_shin_left", Joint.LEFT_FRONT_LEG, -11f, 2f, -16f, -6f, 10f, -10f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 67f, 17f, 72f, 25f, 0), new Face("east", 71f, 6f, 77f, 14f, 0), new Face("south", 75f, 20f, 80f, 28f, 0), new Face("west", 78f, 10f, 84f, 18f, 0), new Face("up", 83f, 0f, 88f, 6f, 0), new Face("down", 87f, 14f, 92f, 20f, 0)}),
                new Cube("front_hoof_left", Joint.LEFT_FRONT_LEG, -11f, 0f, -17f, -6f, 3f, -9f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 39f, 112f, 44f, 115f, 0), new Face("east", 42f, 100f, 50f, 103f, 0), new Face("south", 47f, 116f, 52f, 119f, 0), new Face("west", 49f, 104f, 57f, 107f, 0), new Face("up", 55f, 116f, 60f, 124f, 0), new Face("down", 32f, 109f, 37f, 117f, 0)}),
                new Cube("front_thigh_right", Joint.RIGHT_FRONT_LEG, 6f, 6f, -17f, 11f, 20f, -9f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 15f, 0f, 20f, 14f, 0), new Face("east", 17f, 10f, 25f, 24f, 0), new Face("south", 23f, 2f, 28f, 16f, 0), new Face("west", 0f, 15f, 8f, 29f, 0), new Face("up", 3f, 10f, 8f, 18f, 0), new Face("down", 7f, 0f, 12f, 8f, 0)}),
                new Cube("front_shin_right", Joint.RIGHT_FRONT_LEG, 6f, 2f, -16f, 11f, 10f, -10f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 79f, 0f, 84f, 8f, 0), new Face("east", 82f, 13f, 88f, 21f, 0), new Face("south", 87f, 3f, 92f, 11f, 0), new Face("west", 64f, 20f, 70f, 28f, 0), new Face("up", 67f, 11f, 72f, 17f, 0), new Face("down", 71f, 0f, 76f, 6f, 0)}),
                new Cube("front_hoof_right", Joint.RIGHT_FRONT_LEG, 6f, 0f, -17f, 11f, 3f, -9f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 51f, 120f, 56f, 123f, 0), new Face("east", 52f, 108f, 60f, 111f, 0), new Face("south", 32f, 100f, 37f, 103f, 0), new Face("west", 35f, 116f, 43f, 119f, 0), new Face("up", 39f, 102f, 44f, 110f, 0), new Face("down", 43f, 116f, 48f, 124f, 0)}),
                new Cube("hind_thigh_left", Joint.LEFT_HIND_LEG, -11f, 6f, 9f, -6f, 20f, 18f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 0f, 0f, 5f, 14f, 0), new Face("east", 3f, 10f, 12f, 24f, 0), new Face("south", 7f, 2f, 12f, 16f, 0), new Face("west", 9f, 12f, 18f, 26f, 0), new Face("up", 15f, 6f, 20f, 15f, 0), new Face("down", 19f, 19f, 24f, 28f, 0)}),
                new Cube("hind_shin_left", Joint.LEFT_HIND_LEG, -11f, 2f, 12f, -6f, 10f, 18f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 64f, 0f, 69f, 8f, 0), new Face("east", 67f, 13f, 73f, 21f, 0), new Face("south", 71f, 3f, 76f, 11f, 0), new Face("west", 75f, 17f, 81f, 25f, 0), new Face("up", 79f, 7f, 84f, 13f, 0), new Face("down", 83f, 22f, 88f, 28f, 0)}),
                new Cube("hind_hoof_left", Joint.LEFT_HIND_LEG, -11f, 0f, 12f, -6f, 3f, 19f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 35f, 120f, 40f, 123f, 0), new Face("east", 39f, 108f, 46f, 111f, 0), new Face("south", 43f, 96f, 48f, 99f, 0), new Face("west", 46f, 112f, 53f, 115f, 0), new Face("up", 51f, 99f, 56f, 106f, 0), new Face("down", 55f, 113f, 60f, 120f, 0)}),
                new Cube("hind_thigh_right", Joint.RIGHT_HIND_LEG, 6f, 6f, 9f, 11f, 20f, 18f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 11f, 5f, 16f, 19f, 0), new Face("east", 13f, 15f, 22f, 29f, 0), new Face("south", 19f, 7f, 24f, 21f, 0), new Face("west", 19f, 0f, 28f, 14f, 0), new Face("up", 0f, 16f, 5f, 25f, 0), new Face("down", 3f, 6f, 8f, 15f, 0)}),
                new Cube("hind_shin_right", Joint.RIGHT_HIND_LEG, 6f, 2f, 12f, 11f, 10f, 18f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 75f, 6f, 80f, 14f, 0), new Face("east", 78f, 20f, 84f, 28f, 0), new Face("south", 83f, 10f, 88f, 18f, 0), new Face("west", 86f, 0f, 92f, 8f, 0), new Face("up", 64f, 18f, 69f, 24f, 0), new Face("down", 67f, 7f, 72f, 13f, 0)}),
                new Cube("hind_hoof_right", Joint.RIGHT_HIND_LEG, 6f, 0f, 12f, 11f, 3f, 19f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 47f, 100f, 52f, 103f, 0), new Face("east", 49f, 116f, 56f, 119f, 0), new Face("south", 55f, 104f, 60f, 107f, 0), new Face("west", 32f, 96f, 39f, 99f, 0), new Face("up", 35f, 110f, 40f, 117f, 0), new Face("down", 39f, 99f, 44f, 106f, 0)})})),
            Map.entry(Species.MARSH_FOWL, new SpeciesLayer(Species.MARSH_FOWL,
                new Bone[]{new Bone(Joint.BODY, 0.00000f, 29.50000f, 0.00000f),
                new Bone(Joint.NECK, 0.00000f, 32.00000f, -4.00000f),
                new Bone(Joint.HEAD, 0.00000f, 57.50000f, -7.50000f),
                new Bone(Joint.BEAK, 0.00000f, 56.00000f, -14.50000f),
                new Bone(Joint.CROWN, 0.00000f, 67.50000f, -6.50000f),
                new Bone(Joint.WINGS, 0.00000f, 28.50000f, 2.00000f),
                new Bone(Joint.TAIL, 0.00000f, 21.50000f, 11.00000f),
                new Bone(Joint.LEFT_FRONT_LEG, -3.25000f, 23.00000f, 0.41667f),
                new Bone(Joint.RIGHT_FRONT_LEG, 3.25000f, 23.00000f, 0.41667f)},
                new Cube[]{new Cube("body_core", Joint.BODY, -7f, 20f, -5f, 7f, 39f, 9f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 108f, 3f, 122f, 22f, 0), new Face("east", 111f, 11f, 125f, 30f, 0), new Face("south", 96f, 7f, 110f, 26f, 0), new Face("west", 98f, 1f, 112f, 20f, 0), new Face("up", 101f, 12f, 115f, 26f, 0), new Face("down", 103f, 5f, 117f, 19f, 0)}),
                new Cube("chest_mass", Joint.BODY, -6f, 22f, -9f, 6f, 35f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 69f, 109f, 81f, 122f, 0), new Face("east", 73f, 101f, 83f, 114f, 0), new Face("south", 75f, 112f, 87f, 125f, 0), new Face("west", 79f, 104f, 89f, 117f, 0), new Face("up", 81f, 96f, 93f, 106f, 0), new Face("down", 64f, 111f, 76f, 121f, 0)}),
                new Cube("neck_block", Joint.NECK, -4f, 32f, -7f, 4f, 54f, -1f, 0f, 32f, -4f, -5f, 0f, 0f, new Face[]{new Face("north", 116f, 0f, 124f, 22f, 0), new Face("east", 96f, 7f, 102f, 29f, 0), new Face("south", 99f, 2f, 107f, 24f, 0), new Face("west", 103f, 8f, 109f, 30f, 0), new Face("up", 106f, 11f, 114f, 17f, 0), new Face("down", 109f, 0f, 117f, 6f, 0)}),
                new Cube("head_block", Joint.HEAD, -5.5f, 52f, -12f, 5.5f, 63f, -3f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 32f, 32f, 43f, 43f, 0), new Face("east", 115f, 6f, 124f, 17f, 0), new Face("south", 96f, 0f, 107f, 11f, 0), new Face("west", 99f, 12f, 108f, 23f, 0), new Face("up", 102f, 3f, 113f, 12f, 0), new Face("down", 105f, 16f, 116f, 25f, 0)}),
                new Cube("beak_block", Joint.BEAK, -2.5f, 54f, -18f, 2.5f, 58f, -11f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 23f, 84f, 28f, 88f, 0), new Face("east", 0f, 76f, 7f, 80f, 0), new Face("south", 3f, 64f, 8f, 68f, 0), new Face("west", 7f, 80f, 14f, 84f, 0), new Face("up", 11f, 67f, 16f, 74f, 0), new Face("down", 15f, 81f, 20f, 88f, 0)}),
                new Cube("crown_center", Joint.CROWN, -1.5f, 62f, -8f, 1.5f, 75f, -5f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 36f, 69f, 39f, 82f, 0), new Face("east", 40f, 80f, 43f, 93f, 0), new Face("south", 44f, 72f, 47f, 85f, 0), new Face("west", 48f, 64f, 51f, 77f, 0), new Face("up", 52f, 80f, 55f, 83f, 0), new Face("down", 56f, 68f, 59f, 71f, 0)}),
                new Cube("crown_left", Joint.CROWN, -4.5f, 60f, -8f, -2f, 70f, -5f, -3.25f, 60f, -6.5f, 0f, 0f, 12f, new Face[]{new Face("north", 48f, 82f, 50.5f, 92f, 0), new Face("east", 52f, 73f, 55f, 83f, 0), new Face("south", 57f, 64f, 59.5f, 74f, 0), new Face("west", 32f, 79f, 35f, 89f, 0), new Face("up", 36f, 72f, 38.5f, 75f, 0), new Face("down", 40f, 88f, 42.5f, 91f, 0)}),
                new Cube("crown_right", Joint.CROWN, 2f, 60f, -8f, 4.5f, 70f, -5f, 3.25f, 60f, -6.5f, 0f, 0f, -12f, new Face[]{new Face("north", 32f, 70f, 34.5f, 80f, 0), new Face("east", 36f, 82f, 39f, 92f, 0), new Face("south", 40f, 73f, 42.5f, 83f, 0), new Face("west", 44f, 64f, 47f, 74f, 0), new Face("up", 48f, 80f, 50.5f, 83f, 0), new Face("down", 53f, 68f, 55.5f, 71f, 0)}),
                new Cube("wing_left", Joint.WINGS, -11f, 20f, -4f, -6f, 37f, 8f, -6f, 34f, 0f, 12f, 0f, 0f, new Face[]{new Face("north", 0f, 96f, 5f, 113f, 0), new Face("east", 0f, 96f, 12f, 113f, 0), new Face("south", 0f, 96f, 5f, 113f, 0), new Face("west", 0f, 96f, 12f, 113f, 0), new Face("up", 0f, 96f, 5f, 108f, 0), new Face("down", 0f, 96f, 5f, 108f, 0)}),
                new Cube("wing_right", Joint.WINGS, 6f, 20f, -4f, 11f, 37f, 8f, 6f, 34f, 0f, 12f, 0f, 0f, new Face[]{new Face("north", 0f, 96f, 5f, 113f, 0), new Face("east", 0f, 96f, 12f, 113f, 0), new Face("south", 0f, 96f, 5f, 113f, 0), new Face("west", 0f, 96f, 12f, 113f, 0), new Face("up", 0f, 96f, 5f, 108f, 0), new Face("down", 0f, 96f, 5f, 108f, 0)}),
                new Cube("tail_block", Joint.TAIL, -5f, 18f, 8f, 5f, 25f, 14f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 67f, 103f, 77f, 110f, 0), new Face("east", 71f, 117f, 77f, 124f, 0), new Face("south", 73f, 106f, 83f, 113f, 0), new Face("west", 78f, 96f, 84f, 103f, 0), new Face("up", 79f, 110f, 89f, 116f, 0), new Face("down", 82f, 99f, 92f, 105f, 0)}),
                new Cube("leg_upper_left", Joint.LEFT_FRONT_LEG, -4.5f, 15f, -1f, -2f, 23f, 2f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 4f, 70f, 6.5f, 78f, 0), new Face("east", 8f, 84f, 11f, 92f, 0), new Face("south", 12f, 74f, 14.5f, 82f, 0), new Face("west", 16f, 64f, 19f, 72f, 0), new Face("up", 21f, 80f, 23.5f, 83f, 0), new Face("down", 25f, 68f, 27.5f, 71f, 0)}),
                new Cube("leg_lower_left", Joint.LEFT_FRONT_LEG, -4f, 3f, -0.5f, -2.5f, 17f, 2f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 21f, 66f, 22.5f, 80f, 0), new Face("east", 25f, 76f, 27.5f, 90f, 0), new Face("south", 0f, 71f, 1.5f, 85f, 0), new Face("west", 4f, 64f, 6.5f, 78f, 0), new Face("up", 8f, 80f, 9.5f, 82.5f, 0), new Face("down", 13f, 68f, 14.5f, 70.5f, 0)}),
                new Cube("foot_left", Joint.LEFT_FRONT_LEG, -5f, 0f, -4f, -1.5f, 4f, 4f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 12f, 80f, 15.5f, 84f, 0), new Face("east", 13f, 68f, 21f, 72f, 0), new Face("south", 20f, 84f, 23.5f, 88f, 0), new Face("west", 20f, 72f, 28f, 76f, 0), new Face("up", 0f, 64f, 3.5f, 72f, 0), new Face("down", 4f, 77f, 7.5f, 85f, 0)}),
                new Cube("leg_upper_right", Joint.RIGHT_FRONT_LEG, 2f, 15f, -1f, 4.5f, 23f, 2f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 16f, 77f, 18.5f, 85f, 0), new Face("east", 20f, 67f, 23f, 75f, 0), new Face("south", 25f, 81f, 27.5f, 89f, 0), new Face("west", 0f, 74f, 3f, 82f, 0), new Face("up", 4f, 64f, 6.5f, 67f, 0), new Face("down", 8f, 80f, 10.5f, 83f, 0)}),
                new Cube("leg_lower_right", Joint.RIGHT_FRONT_LEG, 2.5f, 3f, -0.5f, 4f, 17f, 2f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 4f, 74f, 5.5f, 88f, 0), new Face("east", 8f, 66f, 10.5f, 80f, 0), new Face("south", 13f, 76f, 14.5f, 90f, 0), new Face("west", 16f, 69f, 18.5f, 83f, 0), new Face("up", 21f, 89f, 22.5f, 91.5f, 0), new Face("down", 26f, 76f, 27.5f, 78.5f, 0)}),
                new Cube("foot_right", Joint.RIGHT_FRONT_LEG, 1.5f, 0f, -4f, 5f, 4f, 4f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 24f, 88f, 27.5f, 92f, 0), new Face("east", 0f, 80f, 8f, 84f, 0), new Face("south", 4f, 68f, 7.5f, 72f, 0), new Face("west", 6f, 84f, 14f, 88f, 0), new Face("up", 12f, 70f, 15.5f, 78f, 0), new Face("down", 16f, 84f, 19.5f, 92f, 0)})})),
            Map.entry(Species.STARGRAVEL_PACKBEAST, new SpeciesLayer(Species.STARGRAVEL_PACKBEAST,
                new Bone[]{new Bone(Joint.BODY, 0.00000f, 19.00000f, 2.00000f),
                new Bone(Joint.CHEST, 0.00000f, 16.00000f, -2.00000f),
                new Bone(Joint.NECK, 0.00000f, 16.00000f, -12.50000f),
                new Bone(Joint.HEAD, 0.00000f, 28.00000f, -19.75000f),
                new Bone(Joint.EARS, 0.00000f, 34.00000f, -17.50000f),
                new Bone(Joint.TAIL, 0.00000f, 18.25000f, 21.25000f),
                new Bone(Joint.LEFT_FRONT_LEG, -4.10000f, 16.00000f, -9.75000f),
                new Bone(Joint.RIGHT_FRONT_LEG, 4.10000f, 16.00000f, -9.75000f),
                new Bone(Joint.LEFT_HIND_LEG, -3.95000f, 16.00000f, 15.00000f),
                new Bone(Joint.RIGHT_HIND_LEG, 3.95000f, 16.00000f, 15.00000f)},
                new Cube[]{new Cube("body_core", Joint.BODY, -5.5f, 13f, -8f, 5.5f, 25f, 13f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 15f, 5f, 26f, 17f, 0), new Face("east", 0f, 96f, 21f, 108f, 0), new Face("south", 0f, 11f, 11f, 23f, 0), new Face("west", 0f, 96f, 21f, 108f, 0), new Face("up", 6f, 7f, 17f, 28f, 0), new Face("down", 9f, 3f, 20f, 24f, 0)}),
                new Cube("shoulder_mass", Joint.BODY, -6.2f, 14f, -14f, 6.2f, 25f, -6f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 2f, 12f, 14.4f, 23f, 0), new Face("east", 6f, 3f, 14f, 14f, 0), new Face("south", 8f, 15f, 20.4f, 26f, 0), new Face("west", 13f, 6f, 21f, 17f, 0), new Face("up", 14f, 20f, 26.4f, 28f, 0), new Face("down", 16f, 10f, 28.4f, 18f, 0)}),
                new Cube("hindquarter_mass", Joint.BODY, -6.2f, 14f, 9f, 6.2f, 25f, 20f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 0f, 3f, 12.4f, 14f, 0), new Face("east", 3f, 15f, 14f, 26f, 0), new Face("south", 5f, 6f, 17.4f, 17f, 0), new Face("west", 9f, 18f, 20f, 29f, 0), new Face("up", 11f, 9f, 23.4f, 20f, 0), new Face("down", 14f, 0f, 26.4f, 11f, 0)}),
                new Cube("belly_support", Joint.CHEST, -4.5f, 12f, -7f, 4.5f, 17f, 12f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 96f, 3f, 105f, 8f, 0), new Face("east", 97f, 19f, 116f, 24f, 0), new Face("south", 102f, 7f, 111f, 12f, 0), new Face("west", 101f, 23f, 120f, 28f, 0), new Face("up", 109f, 5f, 118f, 24f, 0), new Face("down", 112f, 0f, 121f, 19f, 0)}),
                new Cube("chest_support", Joint.CHEST, -4.5f, 12f, -16f, 4.5f, 20f, -8f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 115f, 0f, 124f, 8f, 0), new Face("east", 96f, 17f, 104f, 25f, 0), new Face("south", 99f, 6f, 108f, 14f, 0), new Face("west", 102f, 20f, 110f, 28f, 0), new Face("up", 105f, 10f, 114f, 18f, 0), new Face("down", 109f, 0f, 118f, 8f, 0)}),
                new Cube("neck_shoulder_bridge", Joint.BODY, -4.5f, 16f, -16f, 4.5f, 24f, -10f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 112f, 17f, 121f, 25f, 0), new Face("east", 118f, 6f, 124f, 14f, 0), new Face("south", 96f, 0f, 105f, 8f, 0), new Face("west", 99f, 13f, 105f, 21f, 0), new Face("up", 102f, 3f, 111f, 9f, 0), new Face("down", 105f, 18f, 114f, 24f, 0)}),
                new Cube("neck_block", Joint.NECK, -3.5f, 16f, -15f, 3.5f, 28.5f, -10f, 0f, 22f, -12.5f, -35f, 0f, 0f, new Face[]{new Face("north", 117f, 0f, 124f, 12.5f, 0), new Face("east", 96f, 13f, 101f, 25.5f, 0), new Face("south", 99f, 5f, 106f, 17.5f, 0), new Face("west", 103f, 16f, 108f, 28.5f, 0), new Face("up", 106f, 11f, 113f, 16f, 0), new Face("down", 110f, 0f, 117f, 5f, 0)}),
                new Cube("head_block", Joint.HEAD, -3.6f, 24f, -21f, 3.6f, 32f, -14.5f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 32f, 32f, 39.2f, 40f, 0), new Face("east", 117f, 6f, 123.5f, 14f, 0), new Face("south", 96f, 0f, 103.2f, 8f, 0), new Face("west", 99f, 13f, 105.5f, 21f, 0), new Face("up", 103f, 3f, 110.2f, 9.5f, 0), new Face("down", 106f, 18f, 113.2f, 24.5f, 0)}),
                new Cube("muzzle_block", Joint.HEAD, -1.7f, 24.5f, -25f, 1.7f, 27.5f, -20.5f, 0f, 26f, -22.75f, -6f, 0f, 0f, new Face[]{new Face("north", 64f, 32f, 67.4f, 35f, 0), new Face("east", 107f, 0f, 111.5f, 3f, 0), new Face("south", 112f, 16f, 115.4f, 19f, 0), new Face("west", 115f, 4f, 119.5f, 7f, 0), new Face("up", 120f, 19f, 123.4f, 23.5f, 0), new Face("down", 96f, 11f, 99.4f, 15.5f, 0)}),
                new Cube("ear_left", Joint.EARS, -4.7f, 30f, -19f, -2.2f, 38f, -16f, -3.45f, 34f, -17.5f, 0f, 0f, 16f, new Face[]{new Face("north", 4f, 64f, 6.5f, 72f, 0), new Face("east", 8f, 77f, 11f, 85f, 0), new Face("south", 12f, 67f, 14.5f, 75f, 0), new Face("west", 16f, 81f, 19f, 89f, 0), new Face("up", 21f, 72f, 23.5f, 75f, 0), new Face("down", 25f, 88f, 27.5f, 91f, 0)}),
                new Cube("ear_right", Joint.EARS, 2.2f, 30f, -19f, 4.7f, 38f, -16f, 3.45f, 34f, -17.5f, 0f, 0f, -16f, new Face[]{new Face("north", 16f, 70f, 18.5f, 78f, 0), new Face("east", 20f, 84f, 23f, 92f, 0), new Face("south", 25f, 74f, 27.5f, 82f, 0), new Face("west", 0f, 67f, 3f, 75f, 0), new Face("up", 4f, 84f, 6.5f, 87f, 0), new Face("down", 8f, 72f, 10.5f, 75f, 0)}),
                new Cube("tail_block", Joint.TAIL, -1.1f, 17f, 19.5f, 1.1f, 19.5f, 23f, 0f, 18.25f, 21.25f, 8f, 0f, 0f, new Face[]{new Face("north", 36f, 8f, 38.2f, 10.5f, 0), new Face("east", 40f, 25f, 43.5f, 27.5f, 0), new Face("south", 44f, 12f, 46.2f, 14.5f, 0), new Face("west", 48f, 0f, 51.5f, 2.5f, 0), new Face("up", 53f, 16f, 55.2f, 19.5f, 0), new Face("down", 57f, 4f, 59.2f, 7.5f, 0)}),
                new Cube("front_thigh_left", Joint.LEFT_FRONT_LEG, -6f, 7f, -12f, -2.2f, 16f, -6f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 4f, 16f, 7.8f, 25f, 0), new Face("east", 7f, 6f, 13f, 15f, 0), new Face("south", 12f, 19f, 15.8f, 28f, 0), new Face("west", 14f, 9f, 20f, 18f, 0), new Face("up", 20f, 0f, 23.8f, 6f, 0), new Face("down", 24f, 14f, 27.8f, 20f, 0)}),
                new Cube("front_shin_left", Joint.LEFT_FRONT_LEG, -5.5f, 0f, -13f, -2.7f, 8f, -8f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 4f, 81f, 6.8f, 89f, 0), new Face("east", 7f, 70f, 12f, 78f, 0), new Face("south", 12f, 84f, 14.8f, 92f, 0), new Face("west", 15f, 74f, 20f, 82f, 0), new Face("up", 20f, 64f, 22.8f, 69f, 0), new Face("down", 25f, 79f, 27.8f, 84f, 0)}),
                new Cube("front_thigh_right", Joint.RIGHT_FRONT_LEG, 2.2f, 7f, -12f, 6f, 16f, -6f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 16f, 0f, 19.8f, 9f, 0), new Face("east", 18f, 13f, 24f, 22f, 0), new Face("south", 24f, 3f, 27.8f, 12f, 0), new Face("west", 0f, 19f, 6f, 28f, 0), new Face("up", 4f, 11f, 7.8f, 17f, 0), new Face("down", 8f, 0f, 11.8f, 6f, 0)}),
                new Cube("front_shin_right", Joint.RIGHT_FRONT_LEG, 2.7f, 0f, -13f, 5.5f, 8f, -8f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 16f, 64f, 18.8f, 72f, 0), new Face("east", 19f, 77f, 24f, 85f, 0), new Face("south", 25f, 67f, 27.8f, 75f, 0), new Face("west", 0f, 84f, 5f, 92f, 0), new Face("up", 4f, 75f, 6.8f, 80f, 0), new Face("down", 8f, 64f, 10.8f, 69f, 0)}),
                new Cube("hind_thigh_left", Joint.LEFT_HIND_LEG, -6f, 7f, 10f, -2.2f, 16f, 17f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 0f, 0f, 3.8f, 9f, 0), new Face("east", 3f, 13f, 10f, 22f, 0), new Face("south", 8f, 3f, 11.8f, 12f, 0), new Face("west", 10f, 16f, 17f, 25f, 0), new Face("up", 16f, 7f, 19.8f, 14f, 0), new Face("down", 20f, 21f, 23.8f, 28f, 0)}),
                new Cube("hind_shin_left", Joint.LEFT_HIND_LEG, -5.2f, 0f, 14f, -2.4f, 8f, 19f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 0f, 64f, 2.8f, 72f, 0), new Face("east", 3f, 77f, 8f, 85f, 0), new Face("south", 8f, 67f, 10.8f, 75f, 0), new Face("west", 11f, 81f, 16f, 89f, 0), new Face("up", 16f, 71f, 18.8f, 76f, 0), new Face("down", 20f, 87f, 22.8f, 92f, 0)}),
                new Cube("hind_thigh_right", Joint.RIGHT_HIND_LEG, 2.2f, 7f, 10f, 6f, 16f, 17f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 12f, 6f, 15.8f, 15f, 0), new Face("east", 14f, 19f, 21f, 28f, 0), new Face("south", 20f, 9f, 23.8f, 18f, 0), new Face("west", 21f, 0f, 28f, 9f, 0), new Face("up", 0f, 17f, 3.8f, 24f, 0), new Face("down", 4f, 7f, 7.8f, 14f, 0)}),
                new Cube("hind_shin_right", Joint.RIGHT_HIND_LEG, 2.4f, 0f, 14f, 5.2f, 8f, 19f, 0f, 0f, 0f, 0f, 0f, 0f, new Face[]{new Face("north", 12f, 70f, 14.8f, 78f, 0), new Face("east", 15f, 84f, 20f, 92f, 0), new Face("south", 20f, 74f, 22.8f, 82f, 0), new Face("west", 23f, 64f, 28f, 72f, 0), new Face("up", 0f, 83f, 2.8f, 88f, 0), new Face("down", 4f, 71f, 6.8f, 76f, 0)})}))
    );

    // Kept in the packbeast atlas' unused UV space so the saddle follows the same
    // texture and renderer scale as the animal without re-rendering its body.
    private static final Cube[] PACKBEAST_SADDLE_CUBES = {
            solidCube("saddle_pad", -5.9f, 24.7f, -3.5f, 5.9f, 26.2f, 7.5f, 59f, 0f),
            solidCube("saddle_seat", -4.3f, 25.9f, -2.8f, 4.3f, 28.0f, 5.8f, 60f, 0f),
            solidCube("saddle_pommel", -2.2f, 27.4f, -3.2f, 2.2f, 29.0f, -1.0f, 61f, 0f),
            solidCube("saddle_flap_left", -6.5f, 21.0f, -1.5f, -5.6f, 25.5f, 6.0f, 60f, 0f),
            solidCube("saddle_flap_right", 5.6f, 21.0f, -1.5f, 6.5f, 25.5f, 6.0f, 60f, 0f),
            solidCube("saddle_girth_left", -6.4f, 14.0f, 1.5f, -5.6f, 21.2f, 3.0f, 59f, 0f),
            solidCube("saddle_girth_right", 5.6f, 14.0f, 1.5f, 6.4f, 21.2f, 3.0f, 59f, 0f),
            solidCube("saddle_trim_left", -6.6f, 21.0f, -1.5f, -6.45f, 25.5f, 6.0f, 62f, 0f),
            solidCube("saddle_trim_right", 6.45f, 21.0f, -1.5f, 6.6f, 25.5f, 6.0f, 62f, 0f)
    };

    private final Species species;
    private final SpeciesLayer layer;
    private final EnumMap<Joint, Rotation> jointRotations = new EnumMap<>(Joint.class);
    private final Map<String, Rotation> elementRotations = new HashMap<>();
    private boolean ramSheared;
    private boolean ramShearReacting;
    private boolean packbeastSaddled;
    private static final ModelPart EMPTY_ROOT = new ModelPart(List.of(), Map.of());

    /** Builds a species-specific geometry layer without opening or changing a Blockbench project. */
    public static SpeciesLayer createLayer(Species species) {
        SpeciesLayer layer = LAYERS.get(species);
        if (layer == null) throw new IllegalArgumentException("Unsupported path animal: " + species);
        return layer;
    }

    /** Convenience overload for callers that keep species IDs as registry strings. */
    public static SpeciesLayer createLayer(String species) {
        return createLayer(Species.valueOf(species.trim().toUpperCase(Locale.ROOT)));
    }

    public PathAnimalModel(Species species) {
        super(RenderType::entityCutoutNoCull);
        this.species = species;
        this.layer = createLayer(species);
    }

    public PathAnimalModel(SpeciesLayer layer) {
        super(RenderType::entityCutoutNoCull);
        this.layer = layer;
        this.species = layer.species;
    }

    public Species species() { return species; }
    public ModelPart root() { return EMPTY_ROOT; }
    public ResourceLocation texture() {
        return new ResourceLocation("starrail_sim", "textures/entity/path_animals/" + species.id + ".png");
    }

    /** Set a part-level pose in radians; call from the entity model's animation adapter. */
    public void setJointRotation(Joint joint, float x, float y, float z) {
        jointRotations.put(joint, new Rotation(x, y, z));
    }

    /** Add an element-level pose (useful for the two wings in their shared source group). */
    public void setElementRotation(String elementName, float x, float y, float z) {
        elementRotations.put(elementName, new Rotation(x, y, z));
    }

    public void resetAnimationPose() {
        jointRotations.clear();
        elementRotations.clear();
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        resetAnimationPose();
        ramSheared = entity instanceof StarvelvetRam ram && ram.isSheared();
        ramShearReacting = entity instanceof StarvelvetRam ram && ram.isShearReacting();
        packbeastSaddled = entity instanceof StarScreePackbeast packbeast && packbeast.isSaddled();
        if (!(entity instanceof Mob mob)) return;

        float walk = Math.min(1.0F, limbSwingAmount);
        float cycle = limbSwing * 0.6662F;
        float leftLeg = (float) Math.cos(cycle) * 0.8F * walk;
        float rightLeg = (float) Math.cos(cycle + Math.PI) * 0.8F * walk;
        float headYaw = (float) Math.toRadians(netHeadYaw);
        float headPitchRad = (float) Math.toRadians(headPitch);
        setJointRotation(Joint.HEAD, headPitchRad, headYaw, 0.0F);

        switch (species) {
            case SUN_GRAIN_DEER -> {
                setJointRotation(Joint.LEFT_FRONT_LEG, leftLeg, 0, 0);
                setJointRotation(Joint.RIGHT_HIND_LEG, leftLeg, 0, 0);
                setJointRotation(Joint.RIGHT_FRONT_LEG, rightLeg, 0, 0);
                setJointRotation(Joint.LEFT_HIND_LEG, rightLeg, 0, 0);
                if (entity instanceof SunnyHarvestDeer deer && deer.isAlerted()) {
                    setJointRotation(Joint.NECK, -0.28F, 0, 0);
                    setJointRotation(Joint.HEAD, -0.34F, headYaw, 0);
                } else if (entity instanceof SunnyHarvestDeer deer && deer.isGrazing()) {
                    float graze = (float) Math.sin(ageInTicks * 0.18F) * 0.12F;
                    setJointRotation(Joint.NECK, 0.38F + graze, 0, 0);
                    setJointRotation(Joint.HEAD, 0.48F + graze, headYaw, 0);
                }
            }
            case STARFLEECE_RAM -> {
                setJointRotation(Joint.LEFT_FRONT_LEG, leftLeg, 0, 0);
                setJointRotation(Joint.RIGHT_HIND_LEG, leftLeg, 0, 0);
                setJointRotation(Joint.RIGHT_FRONT_LEG, rightLeg, 0, 0);
                setJointRotation(Joint.LEFT_HIND_LEG, rightLeg, 0, 0);
                if (ramShearReacting) {
                    float shake = (float) Math.sin(ageInTicks * 2.2F) * 0.22F;
                    setJointRotation(Joint.BODY, 0, 0, shake);
                    setJointRotation(Joint.HEAD, 0, 0, -shake * 0.45F);
                    setJointRotation(Joint.TAIL, 0, 0, shake * 1.4F);
                } else if (!mob.onGround()) {
                    setJointRotation(Joint.LEFT_FRONT_LEG, -0.8F, 0, 0);
                    setJointRotation(Joint.RIGHT_FRONT_LEG, -0.8F, 0, 0);
                    setJointRotation(Joint.LEFT_HIND_LEG, 0.9F, 0, 0);
                    setJointRotation(Joint.RIGHT_HIND_LEG, 0.9F, 0, 0);
                    setJointRotation(Joint.BODY, -0.08F, 0, 0);
                }
                if (walk < 0.1F && mob.tickCount % 113 < 14) {
                    setJointRotation(Joint.HEAD, 0.2F, headYaw, 0);
                }
            }
            case WOODLAND_FORAGER -> {
                setJointRotation(Joint.LEFT_FRONT_LEG, leftLeg, 0, 0);
                setJointRotation(Joint.RIGHT_HIND_LEG, leftLeg, 0, 0);
                setJointRotation(Joint.RIGHT_FRONT_LEG, rightLeg, 0, 0);
                setJointRotation(Joint.LEFT_HIND_LEG, rightLeg, 0, 0);
                if (entity instanceof WoodlandForager forager && forager.isForaging()) {
                    float root = (float) Math.sin(ageInTicks * 0.22F) * 0.08F;
                    setJointRotation(Joint.NECK, 0.3F + root, 0, 0);
                    setJointRotation(Joint.HEAD, 0.42F + root, headYaw, 0);
                    setJointRotation(Joint.BACK_RIDGE, 0.08F, 0, 0);
                }
            }
            case MARSH_FOWL -> {
                float flap = mob.onGround() ? 0.0F : (float) Math.sin(ageInTicks * 1.6F) * 0.85F;
                setElementRotation("wing_left", 0, 0, flap);
                setElementRotation("wing_right", 0, 0, -flap);
                setJointRotation(Joint.LEFT_FRONT_LEG, leftLeg * 0.7F, 0, 0);
                setJointRotation(Joint.RIGHT_FRONT_LEG, rightLeg * 0.7F, 0, 0);
                if (walk < 0.1F && mob.onGround() && ageInTicks % 90.0F < 18.0F) {
                    float peck = (float) Math.sin(ageInTicks * 0.35F) * 0.12F;
                    setJointRotation(Joint.NECK, 0.28F + peck, 0, 0);
                    setJointRotation(Joint.HEAD, 0.45F + peck, headYaw, 0);
                }
            }
            case STARGRAVEL_PACKBEAST -> {
                float stride = walk > 0.55F ? 1.0F : 0.68F;
                setJointRotation(Joint.LEFT_FRONT_LEG, leftLeg * stride, 0, 0);
                setJointRotation(Joint.RIGHT_HIND_LEG, leftLeg * stride, 0, 0);
                setJointRotation(Joint.RIGHT_FRONT_LEG, rightLeg * stride, 0, 0);
                setJointRotation(Joint.LEFT_HIND_LEG, rightLeg * stride, 0, 0);
                if (!mob.onGround()) {
                    setJointRotation(Joint.LEFT_FRONT_LEG, -0.72F, 0, 0);
                    setJointRotation(Joint.RIGHT_FRONT_LEG, -0.72F, 0, 0);
                    setJointRotation(Joint.LEFT_HIND_LEG, 0.82F, 0, 0);
                    setJointRotation(Joint.RIGHT_HIND_LEG, 0.82F, 0, 0);
                    setJointRotation(Joint.BODY, -0.05F, 0, 0);
                }
                setJointRotation(Joint.NECK, headPitchRad * 0.35F, 0, 0);
            }
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        // LivingEntityRenderer prepares vanilla models in top-down model space by applying
        // scale(-1, -1, 1) and then translating the model origin to the feet. These cuboids
        // were authored as Y-up Blockbench coordinates, so undo that convention here before
        // emitting their vertices. Without this inverse, the animals render below the ground
        // and appear inverted despite their source geometry being upright.
        poseStack.pushPose();
        poseStack.translate(0.0F, 1.501F, 0.0F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        for (Cube cube : layer.cubes) {
            if (species == Species.STARFLEECE_RAM && ramSheared
                    && (cube.name.equals("shoulder_mass") || cube.name.equals("hindquarter_mass"))) {
                continue;
            }
            Bone bone = layer.bones.get(cube.joint);
            Rotation jointRotation = jointRotations.get(cube.joint);
            Rotation elementRotation = elementRotations.get(cube.name);
            Bone headBone = isHeadAttachment(cube.joint) ? layer.bones.get(Joint.HEAD) : null;
            Rotation headRotation = headBone == null ? Rotation.ZERO
                    : jointRotations.getOrDefault(Joint.HEAD, Rotation.ZERO);
            renderCube(poseStack, consumer, cube, bone, headBone, headRotation,
                    jointRotation == null ? Rotation.ZERO : jointRotation,
                    elementRotation == null ? Rotation.ZERO : elementRotation,
                    packedLight, packedOverlay, red, green, blue, alpha);
        }
        if (species == Species.STARGRAVEL_PACKBEAST && packbeastSaddled) {
            Bone body = layer.bones.get(Joint.BODY);
            Rotation bodyRotation = jointRotations.getOrDefault(Joint.BODY, Rotation.ZERO);
            for (Cube saddleCube : PACKBEAST_SADDLE_CUBES) {
                renderCube(poseStack, consumer, saddleCube, body, null, Rotation.ZERO,
                        bodyRotation, Rotation.ZERO, packedLight, packedOverlay,
                        red, green, blue, alpha);
            }
        }
        poseStack.popPose();
    }

    private static void renderCube(PoseStack stack, VertexConsumer out, Cube c, Bone bone,
                                   Bone parentBone, Rotation parentRotation,
                                   Rotation joint, Rotation element, int light, int overlay,
                                   float red, float green, float blue, float alpha) {
        stack.pushPose();
        if (parentBone != null) {
            stack.translate(parentBone.x / 16.0f, parentBone.y / 16.0f, parentBone.z / 16.0f);
            rotate(stack, parentRotation.x, parentRotation.y, parentRotation.z);
            stack.translate(-parentBone.x / 16.0f, -parentBone.y / 16.0f, -parentBone.z / 16.0f);
        }
        stack.translate(bone.x / 16.0f, bone.y / 16.0f, bone.z / 16.0f);
        rotate(stack, joint.x, joint.y, joint.z);
        stack.translate((c.pivotX - bone.x) / 16.0f,
                (c.pivotY - bone.y) / 16.0f, (c.pivotZ - bone.z) / 16.0f);
        float rx = (float) Math.toRadians(c.rotX) + element.x;
        float ry = (float) Math.toRadians(c.rotY) + element.y;
        float rz = (float) Math.toRadians(c.rotZ) + element.z;
        rotate(stack, rx, ry, rz);

        float x0 = (c.x0 - c.pivotX) / 16.0f;
        float x1 = (c.x1 - c.pivotX) / 16.0f;
        float y0 = (c.y0 - c.pivotY) / 16.0f;
        float y1 = (c.y1 - c.pivotY) / 16.0f;
        float z0 = (c.z0 - c.pivotZ) / 16.0f;
        float z1 = (c.z1 - c.pivotZ) / 16.0f;
        Matrix4f pose = stack.last().pose();
        Matrix3f normal = stack.last().normal();
        Face[] faces = c.faces;
        for (Face face : faces) {
            emitFace(out, pose, normal, face, x0, y0, z0, x1, y1, z1,
                    light, overlay, red, green, blue, alpha);
        }
        stack.popPose();
    }

    private static boolean isHeadAttachment(Joint joint) {
        return joint == Joint.EARS || joint == Joint.ANTLERS
                || joint == Joint.BEAK || joint == Joint.CROWN;
    }

    private static Cube solidCube(String name, float x0, float y0, float z0,
                                  float x1, float y1, float z1, float u, float v) {
        Face[] faces = new Face[]{
                new Face("north", u, v, u + 1f, v + 1f, 0),
                new Face("east", u, v, u + 1f, v + 1f, 0),
                new Face("south", u, v, u + 1f, v + 1f, 0),
                new Face("west", u, v, u + 1f, v + 1f, 0),
                new Face("up", u, v, u + 1f, v + 1f, 0),
                new Face("down", u, v, u + 1f, v + 1f, 0)
        };
        return new Cube(name, Joint.BODY, x0, y0, z0, x1, y1, z1,
                0f, 0f, 0f, 0f, 0f, 0f, faces);
    }

    private static void rotate(PoseStack stack, float x, float y, float z) {
        if (x != 0 || y != 0 || z != 0) {
            // Source Blockbench coordinates already use the world model's X/Y/Z axes.
            stack.mulPose(new Quaternionf().rotationXYZ(x, y, z));
        }
    }

    private static void emitFace(VertexConsumer out, Matrix4f pose, Matrix3f normal, Face face,
                                 float x0, float y0, float z0, float x1, float y1, float z1,
                                 int light, int overlay, float red, float green, float blue, float alpha) {
        float[][] p;
        float nx, ny, nz;
        switch (face.direction) {
            case "north" -> { p = new float[][]{{x0,y0,z0},{x1,y0,z0},{x1,y1,z0},{x0,y1,z0}}; nx=0; ny=0; nz=-1; }
            case "south" -> { p = new float[][]{{x1,y0,z1},{x0,y0,z1},{x0,y1,z1},{x1,y1,z1}}; nx=0; ny=0; nz=1; }
            case "east" -> { p = new float[][]{{x1,y0,z0},{x1,y0,z1},{x1,y1,z1},{x1,y1,z0}}; nx=1; ny=0; nz=0; }
            case "west" -> { p = new float[][]{{x0,y0,z1},{x0,y0,z0},{x0,y1,z0},{x0,y1,z1}}; nx=-1; ny=0; nz=0; }
            case "up" -> { p = new float[][]{{x0,y1,z1},{x1,y1,z1},{x1,y1,z0},{x0,y1,z0}}; nx=0; ny=1; nz=0; }
            case "down" -> { p = new float[][]{{x0,y0,z0},{x1,y0,z0},{x1,y0,z1},{x0,y0,z1}}; nx=0; ny=-1; nz=0; }
            default -> throw new IllegalStateException("Unknown cuboid face " + face.direction);
        }
        float u0 = face.u0 / 128.0f, v0 = face.v0 / 128.0f;
        float u1 = face.u1 / 128.0f, v1 = face.v1 / 128.0f;
        float[][] uv = {{u0,v0},{u1,v0},{u1,v1},{u0,v1}};
        int turns = Math.floorMod(face.rotation / 90, 4);
        for (int turn = 0; turn < turns; turn++) {
            float[] first = uv[0]; uv[0] = uv[1]; uv[1] = uv[2]; uv[2] = uv[3]; uv[3] = first;
        }
        for (int i = 0; i < 4; i++) {
            out.vertex(pose, p[i][0], p[i][1], p[i][2])
                    .color(red, green, blue, alpha)
                    .uv(uv[i][0], uv[i][1])
                    .overlayCoords(overlay)
                    .uv2(light)
                    .normal(normal, nx, ny, nz)
                    .endVertex();
        }
    }

    public static final class SpeciesLayer {
        private final Species species;
        private final EnumMap<Joint, Bone> bones = new EnumMap<>(Joint.class);
        private final Cube[] cubes;
        private SpeciesLayer(Species species, Bone[] bones, Cube[] cubes) {
            this.species = species;
            for (Bone bone : bones) this.bones.put(bone.joint, bone);
            this.cubes = cubes;
        }
        public Species species() { return species; }
        public int cuboidCount() { return cubes.length; }
    }

    private record Bone(Joint joint, float x, float y, float z) {}
    private record Rotation(float x, float y, float z) {
        private static final Rotation ZERO = new Rotation(0, 0, 0);
    }
    private record Face(String direction, float u0, float v0, float u1, float v1, int rotation) {}
    private record Cube(String name, Joint joint, float x0, float y0, float z0,
                        float x1, float y1, float z1, float pivotX, float pivotY, float pivotZ,
                        float rotX, float rotY, float rotZ, Face[] faces) {}

}
