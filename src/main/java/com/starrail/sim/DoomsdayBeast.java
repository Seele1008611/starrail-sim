package com.starrail.sim;

import com.starrail.sim.client.BlockbenchRig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public final class DoomsdayBeast extends Monster {
    private static final String[] CLIPS = {"待机", "右爪横扫", "左爪突击", "引擎增幅", "末日临空", "瘫痪", "重构", "死亡", "登场", "受击", "", "双爪震地", "振翼风压", "向左换位", "向右换位", "待机", "待机"};
    private static final int RECOVERY = 10;
    public static final int DOUBLE_SLAM = 11;
    public static final int WING_GALE = 12;
    private static final int MOVE_LEFT = 13;
    private static final int MOVE_RIGHT = 14;
    private static final int WALK_LEFT = 15;
    private static final int WALK_RIGHT = 16;
    private static final int RECOVERY_TICKS = 16;
    private static final int[] DURATION = {0, 33, 29, 32, 112, 90, 33, 49, 44, 11, RECOVERY_TICKS, 46, 50, 14, 14, 26, 26};
    private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(DoomsdayBeast.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> ACTION_START = SynchedEntityData.defineId(DoomsdayBeast.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> DISABLED = SynchedEntityData.defineId(DoomsdayBeast.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> INTERRUPTED_ACTION = SynchedEntityData.defineId(DoomsdayBeast.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> INTERRUPTED_TICK = SynchedEntityData.defineId(DoomsdayBeast.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Vector3f> STRIKE_OFFSET =
            SynchedEntityData.defineId(DoomsdayBeast.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Integer> INTERRUPTED_MASK = SynchedEntityData.defineId(DoomsdayBeast.class, EntityDataSerializers.INT);
    // World yaw, head yaw relative to the body, and head pitch, in degrees.
    private static final EntityDataAccessor<Vector3f> AIM =
            SynchedEntityData.defineId(DoomsdayBeast.class, EntityDataSerializers.VECTOR3);
    private int dodgeCooldown;
    private int healTicks;
    private static final String[] BONES = {"左爪", "右爪", "反物质引擎", "悬浮躯干"};
    private final Segment[] parts = {new Segment(this, 0), new Segment(this, 1), new Segment(this, 2), new Segment(this, 3)};
    private final ServerBossEvent bossBar = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    private final float[] integrity = {125, 125, 150};
    private boolean secondPhase;
    private boolean empowered;
    private int nextAttack;
    private boolean rightClawNext = true;
    private int quietTicks;
    private int emptyTicks;
    private Vec3 home;
    private Vec3 moveFrom;
    private Vec3 moveTo;
    private int attacksSinceMove;
    private Vec3 strikePoint = Vec3.ZERO;

    public DoomsdayBeast(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setNoGravity(true);
        setPersistenceRequired();
        noPhysics = true;
        noCulling = true;
        setId(ENTITY_COUNTER.getAndAdd(parts.length + 1) + 1);
        begin(8);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 800)
                .add(Attributes.ARMOR, 10).add(Attributes.KNOCKBACK_RESISTANCE, 1);
    }

    public static BlockbenchRig rig() { return RigHolder.RIG; }

    private static final class RigHolder {
        private static final BlockbenchRig RIG = BlockbenchRig.read(new InputStreamReader(
                Objects.requireNonNull(DoomsdayBeast.class.getResourceAsStream(
                        "/assets/starrail_sim/entity_models/doomsday_beast.json")), StandardCharsets.UTF_8));
        private static final List<List<BlockbenchRig.Geometry>> PART_GEOMETRY = findPartGeometry();

        private static List<List<BlockbenchRig.Geometry>> findPartGeometry() {
            return java.util.Arrays.stream(findPartNodes()).mapToObj(node -> RIG.geometry.stream()
                    .filter(mesh -> RIG.nodes.get(mesh.node()).parent() == node).toList()).toList();
        }

        private static int[] findPartNodes() {
            int[] nodes = new int[BONES.length];
            for (int i = 0; i < nodes.length; i++) {
                int index = 0;
                while (!RIG.nodes.get(index).name().equals(BONES[i])) index++;
                nodes[i] = index;
            }
            return nodes;
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ACTION, 0);
        entityData.define(AIM, new Vector3f());
        entityData.define(ACTION_START, 0L);
        entityData.define(DISABLED, 0);
        entityData.define(STRIKE_OFFSET, new Vector3f());
        entityData.define(INTERRUPTED_ACTION, 3);
        entityData.define(INTERRUPTED_TICK, 0);
        entityData.define(INTERRUPTED_MASK, 0);
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        for (int i = 0; i < parts.length; i++) parts[i].setId(id + i + 1);
    }

    @Override public boolean isMultipartEntity() { return true; }
    @Override public PartEntity<?>[] getParts() { return parts; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public boolean isPickable() { return false; }
    @Override public int getExperienceReward() { return 100; }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }

    private int action() { return entityData.get(ACTION); }
    private boolean active(int part) { return (entityData.get(DISABLED) & (1 << part)) == 0; }

    private int actionTicks() {
        return (int) Math.max(0, level().getGameTime() - entityData.get(ACTION_START));
    }

    public Matrix4f[] pose(float partial) {
        int action = isDeadOrDying() ? 7 : action();
        float ticks = action == 7 ? deathTime : action == 0 ? tickCount : actionTicks();
        int disabled = entityData.get(DISABLED);
        float[][] locals;
        if (action == RECOVERY) {
            float progress = Math.min(1, (ticks + partial) / RECOVERY_TICKS);
            float[][] from = samplePose(entityData.get(INTERRUPTED_ACTION), entityData.get(INTERRUPTED_TICK), entityData.get(INTERRUPTED_MASK));
            float[][] to = disabled == 7 ? samplePose(5, 0, disabled) : samplePose(0, tickCount + partial, disabled);
            locals = rig().blendPoses(from, to, progress * progress * (3 - 2 * progress));
        } else {
            locals = samplePose(action, ticks + partial, disabled);
        }
        Vector3f aim = entityData.get(AIM);
        float yaw = aim.x;
        if (action != 7) {
            float headYaw = aim.y * Mth.DEG_TO_RAD;
            float pitch = aim.z * Mth.DEG_TO_RAD;
            rig().aim(locals, "长颈", pitch * .35F, headYaw * .35F);
            rig().aim(locals, "头冠", pitch * .65F, headYaw * .65F);
        }
        Matrix4f[] matrices = rig().matrices(locals);
        Matrix4f rotation = new Matrix4f().rotationY(yaw * Mth.DEG_TO_RAD);
        for (Matrix4f matrix : matrices) matrix.mulLocal(rotation);
        return matrices;
    }

    private float[][] samplePose(int action, float ticks, int disabled) {
        float weight = action >= 1 && action <= 4
                ? Math.max(0, Math.min(1, Math.min(ticks, DURATION[action] - ticks) / 4)) : 1;
        float[][] locals = rig().sample(CLIPS[action], ticks / 20, weight);
        if (action == WALK_LEFT || action == WALK_RIGHT) {
            String step = action == WALK_LEFT ? "向左踏步" : "向右踏步";
            float[][] gait = rig().sample(step, ticks / 20, 1);
            for (var track : rig().clips.get(step).tracks()) locals[track.node()] = gait[track.node()];
        }
        String[] failures = {"左爪失效", "右爪失效", "引擎失效"};
        if (action != 6 && action != 7) {
            for (int i = 0; i < integrity.length; i++) {
                if ((disabled & (1 << i)) == 0) continue;
                var failure = rig().clips.get(failures[i]);
                float[][] failedPose = rig().sample(failures[i], failure.length(), 1);
                for (var track : failure.tracks()) locals[track.node()] = failedPose[track.node()];
            }
        }
        return locals;
    }

    private void begin(int action) {
        entityData.set(ACTION, action);
        entityData.set(ACTION_START, level().getGameTime());
    }

    private void resetParts() {
        integrity[0] = secondPhase ? 155 : 125;
        integrity[1] = integrity[0];
        integrity[2] = secondPhase ? 185 : 150;
        entityData.set(DISABLED, 0);
        empowered = false;
    }

    private boolean hurtPart(int part, DamageSource source, float amount) {
        if (level().isClientSide || !active(part) || action() == 6 || isDeadOrDying()
                || !(source.getEntity() instanceof Player) || amount <= 0) return false;
        integrity[part] = Math.max(0, integrity[part] - amount);
        if (source.getEntity() instanceof ServerPlayer player) {
            player.displayClientMessage(Component.translatable("message.starrail_sim.doomsday_part",
                    Component.translatable("message.starrail_sim.doomsday_part." + part),
                    (int) Math.ceil(integrity[part])), true);
        }
        if (integrity[part] == 0) {
            int previousMask = entityData.get(DISABLED);
            int disabled = previousMask | (1 << part);
            entityData.set(DISABLED, disabled);
            DoomsdayBeastEffects.breakPart((ServerLevel) level(), partCenter(part));
            if (part == 2) empowered = false;
            int interrupted = action();
            boolean stopsEngine = part == 2 && (interrupted == 3 || interrupted == 4 && actionTicks() < 46);
            boolean stopsSlam = part < 2 && interrupted == DOUBLE_SLAM && actionTicks() < firstImpact(DOUBLE_SLAM);
            if (stopsEngine || stopsSlam) {
                entityData.set(INTERRUPTED_MASK, previousMask);
                entityData.set(INTERRUPTED_ACTION, interrupted);
                entityData.set(INTERRUPTED_TICK, actionTicks());
                begin(RECOVERY);
            } else if (disabled == 7 && interrupted != RECOVERY) {
                begin(5);
            } else if ((part == 0 && interrupted == 2) || (part == 1 && interrupted == 1)) {
                begin(0);
                quietTicks = 20;
            }
        }
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!(source.getEntity() instanceof Player)) return source.is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL) && super.hurt(source, amount);
        if (action() != 5) return false;
        if (!secondPhase && getHealth() - amount <= getMaxHealth() * 0.5F) {
            setHealth(getMaxHealth() * 0.5F);
            secondPhase = true;
            resetParts();
            begin(6);
            return true;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(Vec3.ZERO);
        if (!level().isClientSide) {
            bossBar.setProgress(getHealth() / getMaxHealth());
            if (isAlive()) tickBattle((ServerLevel) level());
        }
        if (isAlive()) {
            Matrix4f[] pose = pose(0);
            for (int i = 0; i < parts.length; i++) {
                updatePartBounds(parts[i], RigHolder.PART_GEOMETRY.get(i), pose);
            }
        }
    }

    private void updatePartBounds(Segment part, List<BlockbenchRig.Geometry> geometry, Matrix4f[] pose) {
        Vector3f min = new Vector3f(Float.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY);
        Vector3f point = new Vector3f();
        for (var mesh : geometry) {
            float[] vertices = mesh.vertices();
            for (int i = 0; i < vertices.length; i += 8) {
                pose[mesh.node()].transformPosition(point.set(vertices[i], vertices[i + 1], vertices[i + 2]));
                min.min(point);
                max.max(point);
            }
        }
        AABB bounds = new AABB(getX() + min.x / 16, getY() + min.y / 16, getZ() + min.z / 16,
                getX() + max.x / 16, getY() + max.y / 16, getZ() + max.z / 16).inflate(0.2);
        part.setPos(bounds.getCenter().x, bounds.minY, bounds.getCenter().z);
        part.setBoundingBox(bounds);
    }

    private Vec3 partCenter(int index) {
        return parts[index].getBoundingBox().getCenter();
    }

    public static int firstImpact(int action) {
        return switch (action) {
            case 1 -> 16;
            case 2 -> 14;
            case 4 -> 46;
            case DOUBLE_SLAM -> 25;
            case WING_GALE -> 21;
            default -> 0;
        };
    }

    public static double dangerRadius(int action) {
        return switch (action) {
            case 1 -> 4;
            case 2 -> 2;
            case WING_GALE -> 6;
            default -> 5;
        };
    }

    public boolean wingsPowered() {
        return action() == 4 && actionTicks() >= 18 && actionTicks() < 78;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return VoidrangerSounds.DOOMSDAY_DEATH.get();
    }

    public int visualAction() { return isDeadOrDying() ? 7 : action(); }

    public float visualTicks(float partial) { return (isDeadOrDying() ? deathTime : actionTicks()) + partial; }

    public boolean visualPartActive(int part) { return active(part); }

    public Vec3 visualEngineCenter() { return partCenter(2); }

    public Vec3 visualTarget() {
        Vector3f offset = entityData.get(STRIKE_OFFSET);
        return position().add(offset.x, offset.y, offset.z);
    }

    private int chooseAttack() {
        int turn = Math.floorMod(nextAttack++, secondPhase ? 8 : 6);
        if (turn == 3 && active(0) && active(1)) return DOUBLE_SLAM;
        if (turn == 5) return WING_GALE;
        if (active(2)) {
            if (secondPhase && turn == 4) return 4;
            if (turn == 2) return 3;
        }
        int preferred = rightClawNext ? 1 : 2;
        int alternate = 3 - preferred;
        if (active(preferred == 1 ? 1 : 0)) {
            rightClawNext = !rightClawNext;
            return preferred;
        }
        if (active(alternate == 1 ? 1 : 0)) return alternate;
        return active(2) ? (secondPhase && turn % 2 == 0 ? 4 : 3) : 0;
    }

    private boolean beginReposition(Player target, boolean dodge) {
        Vec3 toward = target.position().subtract(position()).multiply(1, 0, 1).normalize();
        Vec3 side = new Vec3(-toward.z, 0, toward.x);
        if (side.lengthSqr() < .01) return false;
        if (random.nextBoolean()) side = side.scale(-1);
        for (int attempt = 0; attempt < 2; attempt++, side = side.scale(-1)) {
            Vec3 destination = position().add(side.scale(dodge ? 4.2 : 3.2));
            if (destination.distanceToSqr(home) > 36) continue;
            if (level().getBlockCollisions(this, getBoundingBox()
                    .expandTowards(destination.subtract(position()))).iterator().hasNext()) continue;
            moveFrom = position();
            moveTo = destination;
            float yaw = entityData.get(AIM).x * Mth.DEG_TO_RAD;
            boolean left = side.x * Mth.cos(yaw) - side.z * Mth.sin(yaw) < 0;
            begin(dodge ? (left ? MOVE_LEFT : MOVE_RIGHT) : (left ? WALK_LEFT : WALK_RIGHT));
            return true;
        }
        return false;
    }

    private boolean incomingAttack(Player target) {
        if (target.swinging && (parts[0].distanceToSqr(target) < 16 || parts[1].distanceToSqr(target) < 16
                || parts[2].distanceToSqr(target) < 16)) return true;
        AABB threatArea = parts[0].getBoundingBox().minmax(parts[1].getBoundingBox())
                .minmax(parts[2].getBoundingBox()).inflate(12);
        for (Projectile projectile : level().getEntitiesOfClass(Projectile.class, threatArea,
                shot -> shot.getOwner() instanceof Player && shot.getDeltaMovement().lengthSqr() > .09)) {
            Vec3 start = projectile.position();
            Vec3 end = start.add(projectile.getDeltaMovement().scale(8));
            for (int i = 0; i < 3; i++) {
                if (active(i) && parts[i].getBoundingBox().inflate(.4).clip(start, end).isPresent()) return true;
            }
        }
        return false;
    }

    private void trackTarget(Player target, int state) {
        Vector3f aim = entityData.get(AIM);
        Vec3 offset = target.getEyePosition().subtract(position().add(0, 4, 0));
        float targetYaw = (float) Math.toDegrees(Math.atan2(-offset.x, -offset.z));
        float body = state == 0 || state >= MOVE_LEFT ? Mth.approachDegrees(aim.x, targetYaw, 3) : aim.x;
        float head = Mth.clamp(Mth.wrapDegrees(targetYaw - body), -60, 60);
        float pitch = Mth.clamp((float) Math.toDegrees(Math.atan2(offset.y, offset.horizontalDistance())), -30, 30);
        entityData.set(AIM, new Vector3f(Mth.wrapDegrees(body), Mth.approach(aim.y, head, 5),
                Mth.approach(aim.z, pitch, 4)));
    }

    private void tickBattle(ServerLevel server) {
        if (home == null) home = position();
        List<Player> players = level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(26),
                player -> player.isAlive() && !player.isCreative() && !player.isSpectator());
        int state = action();
        int time = actionTicks();
        if (time == 1 && (state == 8 || state == 6)) {
            DoomsdayBeastEffects.sound(server, position().add(0, 3, 0),
                    VoidrangerSounds.DOOMSDAY_ROAR.get(), 2.7F, state == 6 ? .88F : .94F);
        }
        if (state == 4 && time == 18) {
            DoomsdayBeastEffects.sound(server, partCenter(2),
                    VoidrangerSounds.DOOMSDAY_CHARGE.get(), 1.7F, state == 6 ? .8F : .9F);
        }
        Player target = players.stream().min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
        if (dodgeCooldown > 0) dodgeCooldown--;
        if (target != null) {
            if (state != 5 && state != 6 && state != RECOVERY) trackTarget(target, state);
            if (++healTicks >= 20) {
                healTicks = 0;
                if (state != 5 && state != RECOVERY && state != 6) heal(2);
            }
        } else {
            healTicks = 0;
        }
        if (state >= MOVE_LEFT) {
            double t = Math.min(1, time / (double) DURATION[state]);
            Vec3 point = moveFrom.lerp(moveTo, t * t * (3 - 2 * t));
            if (!level().getBlockCollisions(this, getBoundingBox().move(point.subtract(position()))).iterator().hasNext()) {
                setPos(point.x, point.y, point.z);
            }
            if (time >= DURATION[state]) {
                begin(0);
                quietTicks = 8;
            }
            return;
        }
        if (players.isEmpty()) {
            if (++emptyTicks == 200) {
                secondPhase = false;
                setHealth(getMaxHealth());
                resetParts();
                begin(0);
            } else if (state >= 1 && state <= 4 || state == DOUBLE_SLAM || state == WING_GALE) {
                begin(0);
            }
            return;
        }
        emptyTicks = 0;
        if (state == 0) {
            if (dodgeCooldown == 0 && incomingAttack(target) && beginReposition(target, true)) {
                dodgeCooldown = secondPhase ? 65 : 90;
                return;
            }
            if (quietTicks-- > 0) return;
            if (++attacksSinceMove >= (secondPhase ? 3 : 4)) {
                attacksSinceMove = 0;
                if (beginReposition(target, false)) return;
            }
            int next = chooseAttack();
            if (next != 0) {
                strikePoint = target.position();
                Vec3 offset = strikePoint.subtract(position());
                entityData.set(STRIKE_OFFSET, new Vector3f((float) offset.x, (float) offset.y, (float) offset.z));
                begin(next);
                boolean roar = next == DOUBLE_SLAM || next == 4;
                if (roar || next == 3) {
                    DoomsdayBeastEffects.sound(server, partCenter(2), roar ? VoidrangerSounds.DOOMSDAY_ROAR.get()
                            : VoidrangerSounds.DOOMSDAY_CHARGE.get(), roar ? 2F : 1.25F, .94F);
                }
            }
            return;
        }
        int impact = firstImpact(state);
        if (impact > 0 && (time == impact || state == 4 && time == 60 || state == WING_GALE && time == 31)) {
            double radius = dangerRadius(state);
            DoomsdayBeastEffects.impact(server, strikePoint, radius, state);
            for (Player player : players) {
                if (player.position().distanceToSqr(strikePoint) > radius * radius) continue;
                float damage = state == 4 || state == DOUBLE_SLAM ? 32 : state == WING_GALE ? 15 : 19;
                float fury = 1 + .60F * (1 - getHealth() / getMaxHealth());
                boolean hit = player.hurt(damageSources().mobAttack(this), (damage + (empowered ? 6 : 0)) * fury);
                if (hit && state == WING_GALE) {
                    Vec3 away = player.position().subtract(position()).multiply(1, 0, 1).normalize();
                    player.knockback(1.2, -away.x, -away.z);
                }
            }
        }
        if (state == 3 && active(2)) {
            if (time >= 8 && time <= 24 && time % 4 == 0) {
                for (int claw = 0; claw < 2; claw++) {
                    if (active(claw)) DoomsdayBeastEffects.transfer(server, partCenter(claw));
                }
            }
            if (time == 24) empowered = true;
        }
        if (time >= DURATION[state]) {
            if (state == RECOVERY && entityData.get(DISABLED) == 7) {
                begin(5);
            } else if (state == 5) {
                resetParts();
                begin(6);
            } else {
                begin(0);
                quietTicks = secondPhase ? 14 : 26;
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("SecondPhase", secondPhase);
        if (home != null) {
            tag.putDouble("HomeX", home.x);
            tag.putDouble("HomeY", home.y);
            tag.putDouble("HomeZ", home.z);
        }
        for (int i = 0; i < integrity.length; i++) tag.putFloat("Integrity" + i, integrity[i]);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        secondPhase = tag.getBoolean("SecondPhase");
        if (tag.contains("HomeX")) home = new Vec3(tag.getDouble("HomeX"), tag.getDouble("HomeY"), tag.getDouble("HomeZ"));
        resetParts();
        int disabled = 0;
        for (int i = 0; i < integrity.length; i++) {
            if (tag.contains("Integrity" + i)) integrity[i] = tag.getFloat("Integrity" + i);
            if (integrity[i] <= 0) disabled |= 1 << i;
        }
        entityData.set(DISABLED, disabled);
        if (disabled == 7) begin(5);
        quietTicks = 30;
    }

    private static final class Segment extends PartEntity<DoomsdayBeast> {
        private final int index;

        Segment(DoomsdayBeast boss, int index) {
            super(boss);
            this.index = index;
            refreshDimensions();
        }

        @Override protected void defineSynchedData() {}
        @Override protected void readAdditionalSaveData(CompoundTag tag) {}
        @Override protected void addAdditionalSaveData(CompoundTag tag) {}

        @Override public EntityDimensions getDimensions(Pose pose) { return EntityDimensions.fixed(1.9F, 1.9F); }
        @Override public boolean isPickable() { return getParent().isAlive() && (index == 3 ? getParent().action() == 5 : getParent().active(index)); }
        @Override public boolean is(net.minecraft.world.entity.Entity entity) { return entity == this || entity == getParent(); }
        @Override public boolean hurt(DamageSource source, float amount) { return index == 3 ? getParent().hurt(source, amount) : getParent().hurtPart(index, source, amount); }
    }
}
