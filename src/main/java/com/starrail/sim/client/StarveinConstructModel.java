package com.starrail.sim.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starrail.sim.StarRailSimMod;
import com.starrail.sim.StarveinConstructGuardian;
import net.minecraft.client.model.EntityModel;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Runtime cuboid model read from the approved Blockbench guardian prototype. */
public final class StarveinConstructModel extends EntityModel<StarveinConstructGuardian> {
    private static final String MODEL_RESOURCE =
            "/assets/starrail_sim/entity_models/starvein_construct_guard.json";
    private static final float ATLAS_WIDTH = 48.0F;
    private static final float ATLAS_HEIGHT = 16.0F;

    private final ModelData modelData = ModelData.load();
    private final Map<String, Rotation> groupRotations = new HashMap<>();

    public static ResourceLocation texture() {
        return new ResourceLocation(StarRailSimMod.MOD_ID,
                "textures/entity/starvein_construct_guard.png");
    }

    @Override
    public void setupAnim(StarveinConstructGuardian guardian, float limbSwing,
                          float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        this.groupRotations.clear();
        float walk = Math.min(1.0F, limbSwingAmount);
        float stride = (float) Math.sin(limbSwing * 0.42F) * 0.42F * walk;
        set("左腿", stride, 0.0F, 0.0F);
        set("右腿", -stride, 0.0F, 0.0F);
        set("左肩与手臂", -stride * 0.45F, 0.0F, 0.0F);
        set("右肩与手臂", stride * 0.45F, 0.0F, 0.0F);
        set("头部", (float) Math.toRadians(headPitch),
                (float) Math.toRadians(netHeadYaw), 0.0F);
        float idle = (float) Math.sin(ageInTicks * 0.045F);
        set("躯干", 0.0F, idle * 0.025F, 0.0F);
        set("星晶", 0.0F, 0.0F, idle * 0.025F);

        if (guardian.getStaggerTicks() > 0) {
            set("躯干", 0.24F, 0.0F, 0.16F);
            set("头部", -0.28F, (float) Math.toRadians(netHeadYaw), -0.12F);
            set("左肩与手臂", 0.50F, 0.0F, -0.24F);
            set("右肩与手臂", 0.50F, 0.0F, 0.24F);
            return;
        }

        int attack = guardian.getAttackState();
        int tick = guardian.getAttackTicks();
        if (attack == StarveinConstructGuardian.ATTACK_STAR_TRAIL_SWEEP) {
            animateSweep(tick);
        } else if (attack == StarveinConstructGuardian.ATTACK_CORE_WAVE) {
            animateCoreWave(tick);
        }

        if (guardian.hurtTime > 0) {
            set("躯干", 0.08F, 0.0F, 0.0F);
        }
        if (guardian.deathTime > 0) {
            float fall = Math.min(1.0F, guardian.deathTime / 20.0F);
            set("躯干", 0.0F, 0.0F, fall * 0.75F);
            set("左肩与手臂", 0.4F * fall, 0.0F, -0.2F);
            set("右肩与手臂", 0.4F * fall, 0.0F, 0.2F);
        }
    }

    private void animateSweep(int tick) {
        float armX;
        float bodyYaw;
        if (tick < 14) {
            float pull = smooth(tick / 14.0F);
            armX = -0.15F - 1.15F * pull;
            bodyYaw = -0.12F * pull;
        } else if (tick < 20) {
            float swing = smooth((tick - 14.0F) / 6.0F);
            armX = -1.30F + 2.25F * swing;
            bodyYaw = -0.12F + 0.28F * swing;
        } else {
            float recover = 1.0F - smooth((tick - 20.0F) / 18.0F);
            armX = 0.95F * recover;
            bodyYaw = 0.16F * recover;
        }
        set("左肩与手臂", armX, 0.0F, -0.2F);
        set("右肩与手臂", 0.0F, 0.0F, 0.0F);
        set("躯干", 0.0F, bodyYaw, 0.0F);
        set("腰部", 0.0F, bodyYaw * 0.35F, 0.0F);
    }

    private void animateCoreWave(int tick) {
        float raise;
        float slam;
        if (tick < 30) {
            raise = smooth(tick / 30.0F);
            slam = 0.0F;
        } else if (tick < 35) {
            raise = 1.0F - smooth((tick - 30.0F) / 5.0F);
            slam = smooth((tick - 30.0F) / 5.0F);
        } else {
            raise = 0.0F;
            slam = 1.0F - smooth((tick - 35.0F) / 17.0F);
        }
        float pulse = (float) Math.sin(tick * 1.1F) * (tick < 30 ? raise : slam) * 0.22F;
        set("左肩与手臂", -0.95F * raise + 1.10F * slam,
                0.0F, -0.48F * raise);
        set("右肩与手臂", -0.95F * raise + 1.10F * slam,
                0.0F, 0.48F * raise);
        set("躯干", 0.10F * slam, 0.0F, 0.0F);
        set("胸口菱形框", 0.0F, 0.0F, pulse);
        set("星晶", 0.0F, 0.0F, -pulse * 1.5F);
        set("头部", -0.12F * raise, 0.0F, 0.0F);
    }

    private static float smooth(float value) {
        float t = Math.max(0.0F, Math.min(1.0F, value));
        return t * t * (3.0F - 2.0F * t);
    }

    private void set(String group, float x, float y, float z) {
        this.groupRotations.put(group, new Rotation(x, y, z));
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        poseStack.pushPose();
        // These cuboids are authored in Y-up Blockbench coordinates; invert LivingEntityModel space.
        poseStack.translate(0.0F, 1.501F, 0.0F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        for (Part part : this.modelData.parts) {
            poseStack.pushPose();
            translatePivot(poseStack, part.pivot, 1.0F);
            Rotation rotation = this.groupRotations.getOrDefault(part.name, Rotation.ZERO);
            rotate(poseStack, rotation.x, rotation.y, rotation.z);
            translatePivot(poseStack, part.pivot, -1.0F);
            for (Cube cube : part.cubes) {
                renderCube(poseStack, consumer, cube, packedLight, packedOverlay,
                        red, green, blue, alpha);
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void renderCube(PoseStack stack, VertexConsumer out, Cube cube,
                                   int light, int overlay, float red, float green,
                                   float blue, float alpha) {
        stack.pushPose();
        translatePivot(stack, cube.pivot, 1.0F);
        rotateDegrees(stack, cube.rotation[0], cube.rotation[1], cube.rotation[2]);
        translatePivot(stack, cube.pivot, -1.0F);

        Matrix4f pose = stack.last().pose();
        Matrix3f normal = stack.last().normal();
        float x0 = cube.from[0] / 16.0F;
        float y0 = cube.from[1] / 16.0F;
        float z0 = cube.from[2] / 16.0F;
        float x1 = cube.to[0] / 16.0F;
        float y1 = cube.to[1] / 16.0F;
        float z1 = cube.to[2] / 16.0F;
        for (Face face : cube.faces) {
            if (face != null) {
                emitFace(out, pose, normal, face, x0, y0, z0, x1, y1, z1,
                        light, overlay, red, green, blue, alpha);
            }
        }
        stack.popPose();
    }

    private static void translatePivot(PoseStack stack, float[] pivot, float sign) {
        stack.translate(sign * pivot[0] / 16.0F,
                sign * pivot[1] / 16.0F, sign * pivot[2] / 16.0F);
    }

    private static void rotateDegrees(PoseStack stack, float x, float y, float z) {
        rotate(stack, (float) Math.toRadians(x), (float) Math.toRadians(y),
                (float) Math.toRadians(z));
    }

    private static void rotate(PoseStack stack, float x, float y, float z) {
        if (x != 0.0F || y != 0.0F || z != 0.0F) {
            stack.mulPose(new Quaternionf().rotationXYZ(x, y, z));
        }
    }

    private static void emitFace(VertexConsumer out, Matrix4f pose, Matrix3f normal, Face face,
                                 float x0, float y0, float z0, float x1, float y1, float z1,
                                 int light, int overlay, float red, float green, float blue,
                                 float alpha) {
        float[][] points;
        float nx;
        float ny;
        float nz;
        switch (face.direction) {
            case "north" -> { points = new float[][]{{x0,y0,z0},{x1,y0,z0},{x1,y1,z0},{x0,y1,z0}}; nx=0; ny=0; nz=-1; }
            case "south" -> { points = new float[][]{{x1,y0,z1},{x0,y0,z1},{x0,y1,z1},{x1,y1,z1}}; nx=0; ny=0; nz=1; }
            case "east" -> { points = new float[][]{{x1,y0,z0},{x1,y0,z1},{x1,y1,z1},{x1,y1,z0}}; nx=1; ny=0; nz=0; }
            case "west" -> { points = new float[][]{{x0,y0,z1},{x0,y0,z0},{x0,y1,z0},{x0,y1,z1}}; nx=-1; ny=0; nz=0; }
            case "up" -> { points = new float[][]{{x0,y1,z1},{x1,y1,z1},{x1,y1,z0},{x0,y1,z0}}; nx=0; ny=1; nz=0; }
            case "down" -> { points = new float[][]{{x0,y0,z0},{x1,y0,z0},{x1,y0,z1},{x0,y0,z1}}; nx=0; ny=-1; nz=0; }
            default -> throw new IllegalStateException("Unknown cuboid face " + face.direction);
        }
        float u0 = (face.u0 + face.texture * 16.0F) / ATLAS_WIDTH;
        float v0 = face.v0 / ATLAS_HEIGHT;
        float u1 = (face.u1 + face.texture * 16.0F) / ATLAS_WIDTH;
        float v1 = face.v1 / ATLAS_HEIGHT;
        float[][] uv = {{u0,v0},{u1,v0},{u1,v1},{u0,v1}};
        int turns = Math.floorMod(face.rotation / 90, 4);
        for (int turn = 0; turn < turns; turn++) {
            float[] first = uv[0]; uv[0] = uv[1]; uv[1] = uv[2]; uv[2] = uv[3]; uv[3] = first;
        }
        for (int i = 0; i < 4; i++) {
            out.vertex(pose, points[i][0], points[i][1], points[i][2])
                    .color(red, green, blue, alpha)
                    .uv(uv[i][0], uv[i][1])
                    .overlayCoords(overlay)
                    .uv2(light)
                    .normal(normal, nx, ny, nz)
                    .endVertex();
        }
    }

    private record Rotation(float x, float y, float z) {
        private static final Rotation ZERO = new Rotation(0.0F, 0.0F, 0.0F);
    }

    private record Part(String name, float[] pivot, List<Cube> cubes) {}
    private record Cube(float[] from, float[] to, float[] pivot, float[] rotation, Face[] faces) {}
    private record Face(String direction, float u0, float v0, float u1, float v1,
                        int texture, int rotation) {}

    private static final class ModelData {
        private final List<Part> parts;

        private ModelData(List<Part> parts) {
            this.parts = parts;
        }

        private static ModelData load() {
            try (InputStream stream = StarveinConstructModel.class.getResourceAsStream(MODEL_RESOURCE)) {
                if (stream == null) throw new IllegalStateException("Missing guardian model resource " + MODEL_RESOURCE);
                JsonObject root = JsonParser.parseReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                Map<String, JsonObject> groupById = new HashMap<>();
                for (JsonElement element : arrayOrEmpty(root, "groups")) {
                    if (!element.isJsonObject()) continue;
                    JsonObject group = element.getAsJsonObject();
                    String uuid = stringOrEmpty(group, "uuid");
                    if (!uuid.isEmpty()) groupById.put(uuid, group);
                }
                Map<String, JsonObject> elementById = new HashMap<>();
                for (JsonElement element : arrayOrEmpty(root, "elements")) {
                    if (!element.isJsonObject()) continue;
                    JsonObject cube = element.getAsJsonObject();
                    String uuid = stringOrEmpty(cube, "uuid");
                    if (!uuid.isEmpty()) elementById.put(uuid, cube);
                }
                List<Part> parts = new ArrayList<>();
                for (JsonElement nodeElement : arrayOrEmpty(root, "outliner")) {
                    if (!nodeElement.isJsonObject()) continue;
                    JsonObject node = nodeElement.getAsJsonObject();
                    JsonObject group = groupById.get(stringOrEmpty(node, "uuid"));
                    if (group == null) continue;
                    List<Cube> cubes = new ArrayList<>();
                    collectCubes(arrayOrEmpty(node, "children"), groupById,
                            elementById, cubes);
                    parts.add(new Part(stringOrEmpty(group, "name"),
                            vectorOrZero(group, "origin"), cubes));
                }
                if (parts.isEmpty()) throw new IllegalStateException("Guardian model has no groups");
                return new ModelData(parts);
            } catch (IOException | RuntimeException exception) {
                throw new IllegalStateException("Could not load the accepted Starvein Construct Blockbench model", exception);
            }
        }

        private static void collectCubes(JsonArray children, Map<String, JsonObject> groups,
                                         Map<String, JsonObject> elements, List<Cube> output) {
            for (JsonElement child : children) {
                if (!child.isJsonPrimitive()) continue;
                String id = child.getAsString();
                JsonObject subgroup = groups.get(id);
                if (subgroup != null) {
                    collectCubes(arrayOrEmpty(subgroup, "children"), groups, elements, output);
                    continue;
                }
                JsonObject element = elements.get(id);
                if (element == null || !"cube".equals(stringOrEmpty(element, "type"))) continue;
                JsonArray from = arrayOrEmpty(element, "from");
                JsonArray to = arrayOrEmpty(element, "to");
                if (from.size() < 3 || to.size() < 3) continue;
                float[] pivot = vectorOrZero(element, "origin");
                float[] rotation = vectorOrZero(element, "rotation");
                JsonObject faceData = objectOrEmpty(element, "faces");
                Face[] faces = new Face[6];
                String[] directions = {"north", "east", "south", "west", "up", "down"};
                for (int i = 0; i < directions.length; i++) {
                    JsonElement faceElement = faceData.get(directions[i]);
                    if (faceElement == null || !faceElement.isJsonObject()) continue;
                    JsonObject face = faceElement.getAsJsonObject();
                    int texture = integerOr(face, "texture", -1);
                    JsonArray uv = arrayOrEmpty(face, "uv");
                    if (texture < 0 || uv.size() < 4) continue;
                    int faceRotation = integerOr(face, "rotation", 0);
                    faces[i] = new Face(directions[i], uv.get(0).getAsFloat(), uv.get(1).getAsFloat(),
                            uv.get(2).getAsFloat(), uv.get(3).getAsFloat(),
                            texture, faceRotation);
                }
                output.add(new Cube(vector(from), vector(to), pivot, rotation, faces));
            }
        }

        private static JsonArray arrayOrEmpty(JsonObject object, String key) {
            JsonElement value = object == null ? null : object.get(key);
            return value != null && value.isJsonArray() ? value.getAsJsonArray() : new JsonArray();
        }

        private static JsonObject objectOrEmpty(JsonObject object, String key) {
            JsonElement value = object == null ? null : object.get(key);
            return value != null && value.isJsonObject() ? value.getAsJsonObject() : new JsonObject();
        }

        private static String stringOrEmpty(JsonObject object, String key) {
            JsonElement value = object == null ? null : object.get(key);
            return value != null && value.isJsonPrimitive() ? value.getAsString() : "";
        }

        private static int integerOr(JsonObject object, String key, int fallback) {
            JsonElement value = object == null ? null : object.get(key);
            return value != null && value.isJsonPrimitive() ? value.getAsInt() : fallback;
        }

        private static float[] vectorOrZero(JsonObject object, String key) {
            JsonArray values = arrayOrEmpty(object, key);
            return values.size() >= 3 ? vector(values) : new float[]{0.0F, 0.0F, 0.0F};
        }

        private static float[] vector(JsonArray values) {
            return new float[]{values.get(0).getAsFloat(), values.get(1).getAsFloat(),
                    values.get(2).getAsFloat()};
        }
    }
}
