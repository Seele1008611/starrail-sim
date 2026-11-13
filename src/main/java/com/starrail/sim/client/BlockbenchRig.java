package com.starrail.sim.client;

import com.google.gson.*;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.Reader;
import java.util.*;

/** Native Blockbench geometry and sampled local TRS; independent of Minecraft for parity checks. */
public final class BlockbenchRig {
    public record Node(String name, int parent, float[] base) {}
    public record Geometry(int node, float[] vertices, int[] indices) {}
    public record Track(int node, float[] samples) {}
    public record Clip(float length, boolean loop, float[] times, List<Track> tracks) {}
    public final List<Node> nodes;
    public final List<Geometry> geometry;
    public final Map<String, Clip> clips;
    public final float height;

    private BlockbenchRig(JsonObject root) {
        if (root.get("format").getAsInt() != 2) throw new IllegalArgumentException("Unsupported rig format");
        height = root.get("height").getAsFloat();
        if (!(height > 0)) throw new IllegalArgumentException("Invalid model height");
        nodes = new ArrayList<>();
        for (JsonElement e : root.getAsJsonArray("nodes")) {
            JsonObject n = e.getAsJsonObject();
            int parent = n.get("parent").getAsInt();
            if (parent < -1 || parent >= nodes.size()) throw new IllegalArgumentException("Invalid rig parent");
            float[] base = floats(n.getAsJsonArray("base"));
            if (base.length != 10) throw new IllegalArgumentException("Invalid base transform");
            nodes.add(new Node(n.get("name").getAsString(), parent, base));
        }
        geometry = new ArrayList<>();
        for (JsonElement e : root.getAsJsonArray("geometry")) {
            JsonObject g = e.getAsJsonObject();
            float[] v = floats(g.getAsJsonArray("vertices"));
            int[] indices = new int[g.getAsJsonArray("indices").size()];
            for (int i=0; i<indices.length; i++) {
                indices[i] = g.getAsJsonArray("indices").get(i).getAsInt();
                if (indices[i] < 0 || indices[i]*8 >= v.length) throw new IllegalArgumentException("Invalid vertex index");
            }
            if (v.length%8 != 0 || indices.length%3 != 0) throw new IllegalArgumentException("Invalid mesh");
            int node = g.get("node").getAsInt();
            if (node < 0 || node >= nodes.size()) throw new IllegalArgumentException("Invalid mesh node");
            geometry.add(new Geometry(node, v, indices));
        }
        clips = new HashMap<>();
        for (var entry : root.getAsJsonObject("clips").entrySet()) {
            JsonObject c = entry.getValue().getAsJsonObject();
            float[] times = floats(c.getAsJsonArray("times"));
            List<Track> tracks = new ArrayList<>();
            for (JsonElement e : c.getAsJsonArray("tracks")) {
                JsonObject t = e.getAsJsonObject();
                float[] samples = floats(t.getAsJsonArray("samples"));
                int node = t.get("node").getAsInt();
                if (node < 0 || node >= nodes.size() || samples.length != times.length*10)
                    throw new IllegalArgumentException("Invalid animation track");
                tracks.add(new Track(node, samples));
            }
            clips.put(entry.getKey(), new Clip(c.get("length").getAsFloat(), c.get("loop").getAsBoolean(), times, tracks));
        }
    }

    public static BlockbenchRig read(Reader reader) {
        return new BlockbenchRig(JsonParser.parseReader(reader).getAsJsonObject());
    }

    private static float[] floats(JsonArray a) {
        float[] v = new float[a.size()];
        for (int i=0; i<v.length; i++) {
            v[i] = a.get(i).getAsFloat();
            if (!Float.isFinite(v[i])) throw new IllegalArgumentException("Non-finite rig data");
        }
        return v;
    }

    public float length(String clip) { return clips.get(clip).length; }

    /** Produces local transforms; all unanimated descendants retain their native bind pose. */
    public float[][] sample(String name, float seconds, float weight) {
        float[][] result = new float[nodes.size()][];
        for (int i=0; i<result.length; i++) result[i] = nodes.get(i).base.clone();
        Clip clip = clips.get(name);
        if (clip == null) throw new IllegalArgumentException("Missing animation " + name);
        float time = clip.loop ? ((seconds%clip.length)+clip.length)%clip.length
                : Math.max(0, Math.min(clip.length, seconds));
        int upper = Arrays.binarySearch(clip.times, time);
        if (upper < 0) upper = -upper-1;
        upper = Math.min(upper, clip.times.length-1);
        int lower = Math.max(0, upper-1);
        float span = clip.times[upper]-clip.times[lower];
        float fraction = span > 0 ? (time-clip.times[lower])/span : 0;
        weight = Math.max(0, Math.min(1, weight));
        for (Track track : clip.tracks) {
            float[] frame = interpolate(track.samples, lower*10, track.samples, upper*10, fraction);
            result[track.node] = interpolate(result[track.node], 0, frame, 0, weight);
        }
        return result;
    }

    public float[][] blendPoses(float[][] from, float[][] to, float weight) {
        float[][] pose = new float[nodes.size()][];
        for (int i = 0; i < pose.length; i++) {
            pose[i] = interpolate(from[i], 0, to[i], 0, weight);
        }
        return pose;
    }

    private static float[] interpolate(float[] a, int ai, float[] b, int bi, float t) {
        float[] out = new float[10];
        for (int i=0; i<10; i++) out[i] = a[ai+i]+(b[bi+i]-a[ai+i])*t;
        Quaternionf q = new Quaternionf(a[ai+3],a[ai+4],a[ai+5],a[ai+6]).normalize();
        q.slerp(new Quaternionf(b[bi+3],b[bi+4],b[bi+5],b[bi+6]).normalize(),t).normalize();
        out[3]=q.x; out[4]=q.y; out[5]=q.z; out[6]=q.w;
        return out;
    }

    public void aim(float[][] locals, String bone, float pitch, float yaw) {
        for (int i=0; i<nodes.size(); i++) if (nodes.get(i).name.equals(bone)) {
            float[] f = locals[i];
            Quaternionf q = new Quaternionf(f[3],f[4],f[5],f[6]).rotateY(yaw).rotateX(pitch);
            f[3]=q.x; f[4]=q.y; f[5]=q.z; f[6]=q.w;
            return;
        }
    }

    public Matrix4f[] matrices(float[][] locals) {
        Matrix4f[] matrices = new Matrix4f[nodes.size()];
        for (int i=0; i<matrices.length; i++) {
            float[] f = locals[i];
            Matrix4f local = new Matrix4f().translationRotateScale(new Vector3f(f[0],f[1],f[2]),
                    new Quaternionf(f[3],f[4],f[5],f[6]),new Vector3f(f[7],f[8],f[9]));
            int parent = nodes.get(i).parent;
            matrices[i] = parent < 0 ? local : new Matrix4f(matrices[parent]).mul(local);
        }
        return matrices;
    }
}
