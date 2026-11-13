package com.starrail.sim;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

/** Completed presentation events; attack rules remain in their original server owners. */
public final class MonsterCombatVfx {
    private MonsterCombatVfx() {}
    public static int color(StarveinConstructGuardian guardian) {
        return switch(guardian.getCombatVisualPath()) {
            case HUNT,DESTRUCTION,ELATION -> 0x80e9c5;
            case PRESERVATION,ABUNDANCE -> 0x8ad9ed;
            case ERUDITION,HARMONY,NIHILITY,REMEMBRANCE -> 0xa8c5ff;
            default -> 0x64e1e5;
        };
    }
    public static void pulse(DistorterVoidranger source, Vec3 mark) {
        emit(source,MonsterCombatPacket.Kind.DISTORTER_PULSE,mark,new Vec3(0,1,0),2.5,0x9857f5);
    }
    public static void hit(LivingEntity source, LivingEntity target) {
        if(source instanceof VoidrangerMob || source instanceof StarveinConstructGuardian)
            emit(source,MonsterCombatPacket.Kind.CONFIRMED_HIT,target.position().add(0,target.getBbHeight()*.55,0),
                    new Vec3(0,1,0),0,source instanceof StarveinConstructGuardian g?color(g):0x9857f5);
    }
    public static void emit(Entity source,MonsterCombatPacket.Kind kind,Vec3 point,Vec3 normal,double radius,int color) {
        if(source.level().isClientSide)return;
        StarRailNetwork.CHANNEL.send(PacketDistributor.NEAR.with(()->new PacketDistributor.TargetPoint(
                point.x,point.y,point.z,48,source.level().dimension())),
                new MonsterCombatPacket(kind,source.level().dimension().location(),source.level().getGameTime(),
                        point,normal,radius,color,source.getId()));
    }
}
