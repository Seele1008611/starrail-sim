package com.starrail.sim.client;

import com.starrail.sim.DoomsdayBeast;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.WeakHashMap;

final class DoomsdayBeastVisuals {
    private static final int GOLD = 0xffa82c;
    private static final int IVORY = 0xffefb4;
    private static final int VIOLET = 0xb25cff;
    private static final int BLUE = 0x70c7ff;
    private static final Map<DoomsdayBeast, Anchors> ANCHORS = new WeakHashMap<>();
    private static ClientLevel level;

    private record Edge(float time, Vec3 inner, Vec3 outer) {}

    private static final class Anchors {
        private final ArrayDeque<Edge> trail = new ArrayDeque<>();
        private final Vec3[] points = new Vec3[3];
        private int action;
        private float time;
    }

    private DoomsdayBeastVisuals() {}

    static void capture(DoomsdayBeast boss, Matrix4f[] pose, float partial) {
        sync(Minecraft.getInstance().level);
        int action = boss.visualAction();
        if (action < 1 || action > 4 || !boss.isAlive()) {
            ANCHORS.remove(boss);
            return;
        }
        Anchors anchors = ANCHORS.computeIfAbsent(boss, ignored -> new Anchors());
        float time = boss.visualTicks(partial);
        if (action != anchors.action || time < anchors.time) anchors.trail.clear();
        anchors.action = action;
        anchors.time = time;
        Matrix4f[] bones = new Matrix4f[3];
        var nodes = DoomsdayBeast.rig().nodes;
        for (int i = 0; i < nodes.size(); i++) {
            int index = switch (nodes.get(i).name()) {
                case "左爪" -> 0;
                case "右爪" -> 1;
                case "反物质引擎" -> 2;
                default -> -1;
            };
            if (index >= 0) bones[index] = pose[i];
        }
        for (int i = 0; i < bones.length; i++) anchors.points[i] = world(boss, bones[i], 0, 0, -6);
        if (action <= 2 && time >= 6 && time <= 22) {
            int claw = action == 1 ? 1 : 0;
            Edge last = anchors.trail.peekLast();
            if (last == null || time - last.time() >= .35F) {
                anchors.trail.addLast(new Edge(time, world(boss, bones[claw], 0, 3, -3),
                        world(boss, bones[claw], claw == 0 ? 8 : -8, -6, -22)));
            }
        }
        while (!anchors.trail.isEmpty() && time - anchors.trail.peekFirst().time() > 6) anchors.trail.removeFirst();
    }

    private static Vec3 world(DoomsdayBeast boss, Matrix4f bone, float x, float y, float z) {
        Vector3f point = bone.transformPosition(new Vector3f(x, y, z));
        return boss.position().add(point.x / 16, point.y / 16, point.z / 16);
    }

    private static void sync(ClientLevel current) {
        if (level != current) {
            ANCHORS.clear();
            level = current;
        }
    }

    static int draw(int detail, Vec3 camera, Minecraft mc, float partial, MonsterCombatEffects.Sink sink) {
        sync(mc.level);
        int count = 0;
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof DoomsdayBeast boss) || boss.isRemoved() || boss.distanceToSqr(camera) > 64 * 64) continue;
            int action = boss.visualAction();
            if (action < 1 || action > 4 && action != 6 && action != 7 && action != 8 && DoomsdayBeast.firstImpact(action) == 0) continue;
            if (++count > 6) return 6;
            float time = boss.visualTicks(partial);
            Vec3 target = boss.visualTarget().add(0, .08, 0);
            if (action == 6 || action == 7 || action == 8) {
                drawPresence(sink, camera, boss, time, detail);
                continue;
            }
            Anchors anchors = ANCHORS.get(boss);
            if (anchors != null && (anchors.action != action || Math.abs(anchors.time - time) > 2)) anchors = null;
            if (action == 3) {
                if (anchors != null && time >= 6 && time < 27) {
                    for (int claw = 0; claw < 2; claw++) {
                        if (boss.visualPartActive(claw)) ray(sink, camera, anchors.points[2], anchors.points[claw], .25, BLUE, .75, time);
                    }
                    ring(sink, anchors.points[2], 1 + .25 * Math.sin(time * .65), .12, BLUE, .8, detail, time);
                    orb(sink, anchors.points[2], .45 + time * .025, BLUE, .45, time, detail);
                }
                continue;
            }
            double radius = DoomsdayBeast.dangerRadius(action);
            int hit = DoomsdayBeast.firstImpact(action);
            if (action == DoomsdayBeast.DOUBLE_SLAM) {
                drawSlam(sink, camera, target, radius, time, hit, detail);
                continue;
            }
            if (action == DoomsdayBeast.WING_GALE) {
                drawGale(sink, camera, boss.position(), target, radius, time, hit, detail);
                continue;
            }
            int color = action == 2 ? VIOLET : GOLD;
            if (time < (action == 4 ? 60 : hit)) {
                ring(sink, target, radius, .12, color, .86, detail * 4, time);
                double charge = Math.min(1, time / hit);
                ring(sink, target, radius * (.15 + .82 * charge), .075, color, .52, detail * 3, -time);
                for (int i = 0; i < (action == 4 ? 8 : 4); i++) {
                    double angle = i * (action == 4 ? Math.PI / 4 : Math.PI / 2);
                    Vec3 p = radial(target, angle, radius - .65);
                    Vec3 tip = radial(target, angle, radius);
                    MonsterCombatEffects.face(sink, color, .8, 0, 0, tip,
                            radial(target, angle + .09, radius - .52), p, radial(target, angle - .09, radius - .52));
                }
            }
            if (anchors != null && action <= 2) {
                Edge previous = null;
                for (Edge edge : anchors.trail) {
                    if (previous != null) {
                        double opacity = Math.max(0, .82 * (1 - (time - previous.time()) / 6));
                        MonsterCombatEffects.face(sink, color, opacity, 10, time * .1,
                                previous.inner(), previous.outer(), edge.outer(), edge.inner());
                        MonsterCombatEffects.stroke(sink, previous.outer(), edge.outer(), .09, IVORY, opacity, camera, time * .1);
                    }
                    previous = edge;
                }
            }
            burst(sink, target, radius, color, time - hit, detail);
            if (action != 4) continue;
            Vec3 engine = anchors == null ? boss.visualEngineCenter() : anchors.points[2];
            if (time >= 18 && time < 60) {
                double charge = Math.min(1, (time - 18) / 28);
                orb(sink, engine, .55 + charge * 1.1, VIOLET, .6, time, detail);
                for (int arc = 0; arc < 6; arc++) {
                    double angle = arc * Math.PI / 3 + time * .045;
                    Vec3 outer = radial(engine, angle, 4.5 * (1 - charge) + 1.6).add(0, Math.sin(angle * 2) * 1.5, 0);
                    MonsterCombatEffects.stroke(sink, outer, engine, .05 + charge * .04, BLUE, .5, camera, time);
                }
                double size = 2.8 + .6 * Math.sin(time * .13);
                ring(sink, engine.add(0, 1.35, .55), size, .13, BLUE, .72, detail * 2, time);
                ring(sink, engine.add(0, .85, 0), size * .68, .09, IVORY, .6, detail * 2, -time);
            }
            burst(sink, target, radius, IVORY, time - 60, detail);
            for (int release : new int[]{46, 60}) {
                float age = time - release;
                if (age >= 0 && age < 14) {
                    orb(sink, target.add(0, .65, 0), .5 + age * .16, GOLD, .65 * (1 - age / 14), time, detail);
                    ring(sink, target.add(0, 1.5 + age * .14, 0), radius * age / 14, .2, IVORY,
                            .55 * (1 - age / 14), detail * 3, time);
                }
            }
            for (int release : new int[]{46, 60}) {
                float age = time - release;
                if (age < 0 || age >= 9) continue;
                for (int branch = -1; branch <= 1; branch++) {
                    double side = branch * 1.7;
                    Vec3 end = target.add(side, 0, release == 46 ? 0 : .9 * branch);
                    Vec3 start = engine.add(side * 2.4, 3 + Math.abs(branch), 1.4);
                    ray(sink, camera, start, end, branch == 0 ? .66 : .35, GOLD, 1 - age / 9, age);
                }
            }
        }
        return count;
    }

    private static void drawPresence(MonsterCombatEffects.Sink sink, Vec3 camera,
                                     DoomsdayBeast boss, float time, int detail) {
        int action = boss.visualAction();
        float duration = action == 7 ? 20 : action == 6 ? 33 : 44;
        double progress = Math.min(1, time / duration);
        double opacity = Math.sin(progress * Math.PI) * .7;
        Vec3 core = boss.visualEngineCenter();
        int color = action == 6 ? VIOLET : action == 7 ? GOLD : BLUE;
        double radius = action == 7 ? 3 * (1 - progress) + .2 : .4 + 2.2 * progress;
        orb(sink, core, radius, color, opacity, time, detail);
        ring(sink, boss.position().add(0, -.88, 0), 2 + progress * 10, .2,
                color, opacity, detail * 4, time);
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4 + time * .025;
            Vec3 foot = radial(boss.position(), angle, 2 + 4 * progress);
            Vec3 top = foot.add(0, .5 + 4 * Math.sin(progress * Math.PI), 0);
            MonsterCombatEffects.stroke(sink, foot, top, .06, color, opacity, camera, time);
        }
    }

    private static void orb(MonsterCombatEffects.Sink sink, Vec3 center, double radius,
                            int color, double opacity, double time, int detail) {
        int segments = detail * 2;
        for (int band = 0; band < 4; band++) {
            double low = -.5 * Math.PI + band * Math.PI / 4;
            double high = low + Math.PI / 4;
            for (int i = 0; i < segments; i++) {
                double a = i * Math.PI * 2 / segments + time * .04;
                double b = (i + 1) * Math.PI * 2 / segments + time * .04;
                MonsterCombatEffects.face(sink, color, opacity, 10, time * .1,
                        sphere(center, radius, low, a), sphere(center, radius, low, b),
                        sphere(center, radius, high, b), sphere(center, radius, high, a));
            }
        }
    }

    private static Vec3 sphere(Vec3 center, double radius, double latitude, double longitude) {
        return center.add(Math.cos(latitude) * Math.cos(longitude) * radius, Math.sin(latitude) * radius,
                Math.cos(latitude) * Math.sin(longitude) * radius);
    }

    private static void drawSlam(MonsterCombatEffects.Sink sink, Vec3 camera, Vec3 center,
                                 double radius, float time, int hit, int detail) {
        double age = time - hit;
        if (age < 0) {
            ring(sink, center, radius, .18, GOLD, .88, detail * 3, time);
        }
        if (age >= 14) return;
        double progress = age < 0 ? Math.min(1, time / hit) : Math.min(1, age / 14);
        double opacity = age < 0 ? .3 + progress * .5 : .86 * (1 - progress);
        for (int spoke = 0; spoke < 9; spoke++) {
            double angle = spoke * Math.PI * 2 / 9;
            Vec3 previous = center;
            for (int segment = 1; segment <= 4; segment++) {
                double twist = segment % 2 == 0 ? .09 : -.11;
                Vec3 tip = radial(center, angle + twist, radius * progress * segment / 4);
                MonsterCombatEffects.stroke(sink, previous, tip, .08, GOLD, opacity, camera, time);
                previous = tip;
            }
            if (age >= 0) {
                Vec3 foot = radial(center, angle, radius * (.25 + .66 * progress));
                Vec3 peak = foot.add(.21, Math.sin(Math.PI * progress) * (1.8 + spoke % 3 * .65), 0);
                MonsterCombatEffects.face(sink, 0x535063, opacity, 0, 0, foot,
                        foot.add(.49, 0, -.22), peak, foot.add(-.32, 0, .12));
            }
        }
        burst(sink, center, radius, GOLD, (float) age, detail);
    }

    private static void drawGale(MonsterCombatEffects.Sink sink, Vec3 camera, Vec3 origin, Vec3 center,
                                 double radius, float time, int hit, int detail) {
        if (time < 31) ring(sink, center, radius, .13, BLUE, .84, detail * 4, time);
        for (int release : new int[]{hit, 31}) {
            float age = time - release;
            if (age < 0 || age >= 12) continue;
            double phase = age / 12;
            double heading = Math.atan2(center.z - origin.z, center.x - origin.x);
            for (int band = 0; band < 3; band++) {
                double distance = radius * (.24 + .66 * phase) + band * .42;
                Vec3 previous = null;
                for (int i = 0; i <= detail * 2; i++) {
                    double u = (double) i / (detail * 2);
                    double angle = heading - 1.55 + u * 3.1;
                    Vec3 point = radial(center, angle, distance).add(0, .4 + band * .33 + .64 * Math.sin(u * Math.PI), 0);
                    if (previous != null) {
                        MonsterCombatEffects.stroke(sink, previous, point, .28 - band * .055, BLUE,
                                .82 * (1 - phase), camera, time * .2 + u);
                        MonsterCombatEffects.stroke(sink, previous, point, .045, IVORY,
                                .78 * (1 - phase), camera, -time * .2 + u);
                    }
                    previous = point;
                }
            }
        }
    }

    private static void burst(MonsterCombatEffects.Sink sink, Vec3 center, double radius, int color, float age, int detail) {
        if (age < 0 || age >= 10) return;
        double phase = age / 10;
        ring(sink, center.add(0, phase * .38, 0), radius * (.18 + .82 * phase), .26 * (1 - phase),
                color, .85 * (1 - phase), detail * 4, age);
        ring(sink, center, radius * (.14 + .76 * phase), .08, IVORY, .76 * (1 - phase), detail * 3, -age);
    }

    private static void ray(MonsterCombatEffects.Sink sink, Vec3 camera, Vec3 from, Vec3 to, double width,
                            int color, double opacity, double time) {
        MonsterCombatEffects.stroke(sink, from, to, width, color, opacity * .75, camera, time * .2);
        MonsterCombatEffects.stroke(sink, from, to, width * .38, IVORY, opacity, camera, -time * .15);
    }

    private static Vec3 radial(Vec3 center, double angle, double radius) {
        return center.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
    }

    private static void ring(MonsterCombatEffects.Sink sink, Vec3 center, double radius, double width, int color,
                             double opacity, int segments, double time) {
        for (int i = 0; i < segments; i++) {
            double a = i * 2 * Math.PI / segments;
            double b = (i + 1) * 2 * Math.PI / segments;
            MonsterCombatEffects.face(sink, color, opacity, 10, time * .08 + a,
                    radial(center, a, radius), radial(center, b, radius),
                    radial(center, b, radius - width), radial(center, a, radius - width));
        }
    }
}
