package com.starrail.sim;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import java.util.*;

/** Seven independent active behaviours. Sessions never survive a path/world/owner change. */
@Mod.EventBusSubscriber(modid=StarRailSimMod.MOD_ID)
public final class ExtraActivePathSkills {
    public static final double AREA_RADIUS=4,GROUND_RANGE=12,SUPPORT_RANGE=8;
    public static final double DESTRUCTION_DAMAGE=2,ERUDITION_DAMAGE=1.6,NIHILITY_DAMAGE=.3;
    public static final double HARMONY_ATTACK=.20,HARMONY_ARMOR=.15,MEMORY_RATIO=.4,MEMORY_CAP=2.5;
    public static final double ELATION_HIT=.35,ELATION_BURST=.8,HEAL_FIRST=.2,HEAL_PULSE=.02;
    private static final int HARMONY_DURATION = 25 * 20;
    private static final UUID ATTACK=UUID.fromString("8354810e-763f-4e3a-9f50-b77c21688701"),
            ARMOR=UUID.fromString("8354810e-763f-4e3a-9f50-b77c21688702");
    private record Bonus(UUID target,Vec3 point,boolean finale) {}
    private static final class Session {
        final StarRailPath path;
        final ResourceLocation dimension;
        final Vec3 point,from;
        final long start;
        final double attack;
        final int length;
        UUID target;
        int age,hits;
        long lastHit=-1;
        float recorded;
        final List<UUID> recipients=new ArrayList<>();
        final Set<UUID> marked=new HashSet<>();
        final List<Bonus> bonuses=new ArrayList<>();
        Session(ServerPlayer player,StarRailPath path,Vec3 point,int length) {
            this.path=path;this.point=point;this.length=length;dimension=player.level().dimension().location();
            from=player.getEyePosition();start=player.level().getGameTime();attack=player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        }
    }
    private record Grant(int ticks,ResourceLocation dimension) {}
    private static final Map<ServerPlayer,Session> SESSIONS=new WeakHashMap<>();
    private static final Map<ServerPlayer,Map<UUID,Grant>> GRANTS=new WeakHashMap<>();
    private ExtraActivePathSkills() {}
    public static Component description(StarRailPath path) {
        int cd=ActivePathSkills.cooldown(path)/20;
        return switch(path) {
            case DESTRUCTION -> Component.translatable("skill.starrail_sim.desc.destruction",8,(int)AREA_RADIUS,DESTRUCTION_DAMAGE,cd);
            case ERUDITION -> Component.translatable("skill.starrail_sim.desc.erudition",(int)GROUND_RANGE,.6,(int)AREA_RADIUS,ERUDITION_DAMAGE,cd);
            case HARMONY -> Component.translatable("skill.starrail_sim.desc.harmony",(int)SUPPORT_RANGE,20,15,HARMONY_DURATION/20,cd);
            case NIHILITY -> Component.translatable("skill.starrail_sim.desc.nihility",(int)GROUND_RANGE,(int)AREA_RADIUS,6,NIHILITY_DAMAGE,cd);
            case ABUNDANCE -> Component.translatable("skill.starrail_sim.desc.abundance",(int)SUPPORT_RANGE,20,2,3,cd);
            case REMEMBRANCE -> Component.translatable("skill.starrail_sim.desc.remembrance",16,5,40,MEMORY_CAP,cd);
            case ELATION -> Component.translatable("skill.starrail_sim.desc.elation",5,3,ELATION_HIT,3,ELATION_BURST,cd);
            default -> Component.translatable("skill.starrail_sim.prototype");
        };
    }
    static int remaining(ServerPlayer p) {Session s=SESSIONS.get(p);return s==null?0:Math.max(0,s.length-s.age);}
    static int charges(ServerPlayer p) {Session s=SESSIONS.get(p);return s!=null && s.path==StarRailPath.ELATION?Math.max(0,3-s.hits):0;}
    static float recorded(ServerPlayer p) {Session s=SESSIONS.get(p);return s==null?0:s.recorded;}
    static String status(ServerPlayer p) {
        Session s=SESSIONS.get(p);
        String window=s==null?"none":s.path.getId()+" ticks="+remaining(p)+" charges="+charges(p)+" damage="+s.recorded;
        return window+", harmony_sources="+GRANTS.getOrDefault(p,Map.of());
    }
    private static boolean valid(ServerPlayer p,Session s) {
        return p.isAlive() && !p.isSpectator() && ActivePathSkills.path(p)==s.path && ActivePathSkills.rank(p)>=5
                && p.level().dimension().location().equals(s.dimension);
    }
    private static Vec3 ground(ServerPlayer p) {
        Vec3 from=p.getEyePosition(),to=from.add(p.getLookAngle().scale(GROUND_RANGE));
        BlockHitResult hit=p.level().clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.ANY,p));
        return hit.getType()!=HitResult.Type.MISS && hit.getDirection()==net.minecraft.core.Direction.UP?hit.getLocation().add(0,.03,0):null;
    }
    private static LivingEntity target(ServerPlayer p,double range) {
        Vec3 from=p.getEyePosition(),end=from.add(p.getLookAngle().scale(range));
        var block=p.level().clip(new ClipContext(from,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        if(block.getType()!=HitResult.Type.MISS)end=block.getLocation();
        var hit=net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(p,from,end,
                p.getBoundingBox().expandTowards(end.subtract(from)).inflate(1),e->ActivePathSkills.eligible(p,e),from.distanceToSqr(end));
        return hit!=null && hit.getEntity() instanceof LivingEntity living && p.hasLineOfSight(living)?living:null;
    }
    private static List<LivingEntity> enemies(ServerPlayer p,Vec3 center,double radius) {
        var list=p.level().getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(radius),
                e->ActivePathSkills.eligible(p,e) && e.position().distanceToSqr(center)<=radius*radius
                        && p.level().clip(new ClipContext(center.add(0,.2,0),e.position().add(0,e.getBbHeight()*.55,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS);
        list.sort(Comparator.comparingDouble(e->e.position().distanceToSqr(center)));
        return list.size()>8?new ArrayList<>(list.subList(0,8)):list;
    }
    private static List<ServerPlayer> allies(ServerPlayer p) {
        List<ServerPlayer> list=new ArrayList<>();list.add(p);
        var others=p.serverLevel().players().stream().filter(q->q!=p && q.isAlive() && !q.isSpectator()
                && p.getTeam()!=null && p.isAlliedTo(q) && p.distanceToSqr(q)<=SUPPORT_RANGE*SUPPORT_RANGE && p.hasLineOfSight(q))
                .sorted(Comparator.comparingDouble(p::distanceToSqr)).limit(4).toList();
        list.addAll(others);return list;
    }
    static boolean cast(ServerPlayer p,StarRailPath path) {
        Vec3 point=p.position();LivingEntity target=null;
        if(path==StarRailPath.ERUDITION || path==StarRailPath.NIHILITY) {
            point=ground(p);if(point==null) {ActivePathSkills.message(p,"ground");return false;}
        } else if(path==StarRailPath.REMEMBRANCE) {
            target=target(p,16);if(target==null) {ActivePathSkills.message(p,"memory_target");return false;}
            point=target.position();
        } else if(path==StarRailPath.DESTRUCTION && enemies(p,point,AREA_RADIUS).isEmpty()) {
            ActivePathSkills.message(p,"nearby");return false;
        }
        int length=switch(path) {case ERUDITION->12;case NIHILITY->120;case ABUNDANCE->60;case HARMONY->HARMONY_DURATION;case REMEMBRANCE,ELATION->100;default->0;};
        Session s=new Session(p,path,point,length);if(target!=null)s.target=target.getUUID();
        ActivePathSkills.startCooldown(p,path);
        if(path==StarRailPath.DESTRUCTION) {
            float health=p.getHealth();p.setHealth(Math.min(health,Math.max(1,health*.92f)));
            for(LivingEntity e:enemies(p,point,AREA_RADIUS))StarRailOwnedDamage.activeExtraHurt(p,e,(float)(s.attack*DESTRUCTION_DAMAGE));
            fx(p,s,point,1,true);return true;
        }
        SESSIONS.put(p,s);
        if(path==StarRailPath.HARMONY || path==StarRailPath.ABUNDANCE) {
            for(ServerPlayer ally:allies(p)) {
                s.recipients.add(ally.getUUID());
                if(path==StarRailPath.HARMONY) {
                    GRANTS.computeIfAbsent(ally,k->new HashMap<>()).put(p.getUUID(),new Grant(HARMONY_DURATION,s.dimension));updateAttributes(ally);
                } else ally.heal((float)(ally.getMaxHealth()*HEAL_FIRST));
                fx(p,s,ally.position().add(0,ally.getBbHeight()*.5,0),2,true);
            }
        }
        fx(p,s,s.point,0,false);return true;
    }
    private static void fx(ServerPlayer p,Session s,Vec3 point,int stage,boolean hit) {
        long tick=stage==0?s.start:p.level().getGameTime();
        StarRailNetwork.CHANNEL.send(PacketDistributor.NEAR.with(()->new PacketDistributor.TargetPoint(
                point.x,point.y,point.z,48,net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,s.dimension))),
                new ActiveSkillFxPacket(s.path,s.dimension,tick,s.from,point,p.getId(),hit,stage,
                        s.target!=null && p.serverLevel().getEntity(s.target)!=null?p.serverLevel().getEntity(s.target).getId():-1));
    }
    static void reconcile(ServerPlayer p) {Session s=SESSIONS.get(p);if(s!=null && !valid(p,s))cancel(p);}
    static void cancel(ServerPlayer p) {
        Session s=SESSIONS.remove(p);if(s!=null)fx(p,s,s.point,3,false);
        for(var entry:GRANTS.entrySet())if(entry.getValue().remove(p.getUUID())!=null)updateAttributes(entry.getKey());
    }
    static void tick(ServerPlayer p) {
        Session s=SESSIONS.get(p);if(s==null)return;
        if(!valid(p,s)) {cancel(p);return;}
        s.age++;
        if(s.path==StarRailPath.ELATION) {
            for(Bonus bonus:new ArrayList<>(s.bonuses)) {
                var e=p.serverLevel().getEntity(bonus.target());
                Vec3 center=bonus.point();
                if(e instanceof LivingEntity living && ActivePathSkills.eligible(p,living)) {
                    center=living.position();StarRailOwnedDamage.activeExtraHurt(p,living,(float)(s.attack*ELATION_HIT));
                }
                if(bonus.finale())for(LivingEntity nearby:enemies(p,center,3))StarRailOwnedDamage.activeExtraHurt(p,nearby,(float)(s.attack*ELATION_BURST));
                fx(p,s,center,5,bonus.finale());
            }
            s.bonuses.clear();if(s.hits>=3) {cancel(p);ActivePathSkills.sync(p);return;}
        }
        if(s.path==StarRailPath.ERUDITION && s.age==12) {
            for(LivingEntity e:enemies(p,s.point,AREA_RADIUS))StarRailOwnedDamage.activeExtraHurt(p,e,(float)(s.attack*ERUDITION_DAMAGE));
            fx(p,s,s.point,1,true);
        } else if(s.path==StarRailPath.NIHILITY && s.age%40==0) {
            for(LivingEntity e:enemies(p,s.point,AREA_RADIUS)) {
                if(StarRailOwnedDamage.activeExtraHurt(p,e,(float)(s.attack*NIHILITY_DAMAGE)) && e.isAlive()) {
                    if(StarRailEffectService.rollEffectHit(p,e))e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,50,0));
                    if(s.marked.add(e.getUUID()))StarRailDebuffService.applyActiveNihilityMark(e,p);
                }
            }
            fx(p,s,s.point,2,true);
        } else if(s.path==StarRailPath.ABUNDANCE && s.age%20==0) {
            for(UUID id:s.recipients) {
                ServerPlayer ally=p.server.getPlayerList().getPlayer(id);
                if(ally!=null && ally.isAlive() && !ally.isSpectator() && ally.level()==p.level()
                        && ally.distanceToSqr(p)<=SUPPORT_RANGE*SUPPORT_RANGE && p.hasLineOfSight(ally)
                        && (ally==p || p.getTeam()!=null && p.isAlliedTo(ally))) {
                    ally.heal((float)(ally.getMaxHealth()*HEAL_PULSE));fx(p,s,ally.position().add(0,ally.getBbHeight()*.5,0),2,true);
                }
            }
        } else if(s.path==StarRailPath.REMEMBRANCE) {
            var e=p.serverLevel().getEntity(s.target);
            if(!(e instanceof LivingEntity living) || !ActivePathSkills.eligible(p,living)) {cancel(p);return;}
            if(s.age==100) {
                float damage=(float)Math.min(s.recorded*MEMORY_RATIO,s.attack*MEMORY_CAP);
                if(damage>0 && p.hasLineOfSight(living) && p.distanceToSqr(living)<=16*16) {
                    boolean hit=StarRailOwnedDamage.activeExtraHurt(p,living,damage);fx(p,s,living.position(),4,hit);
                }
            }
        }
        if(s.age>=s.length) {cancel(p);ActivePathSkills.sync(p);}
        else if(s.age%20==0 && s.path!=StarRailPath.ELATION)fx(p,s,s.point,0,false);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void record(LivingDamageEvent event) {
        if(event.isCanceled() || event.getAmount()<=0 || event.getEntity().level().isClientSide())return;
        ServerPlayer p=StarRailCombatEvents.getPlayerAttacker(event.getSource());
        if(p==null || StarRailOwnedDamage.isSecondary(p))return;
        Session s=SESSIONS.get(p);if(s==null || !valid(p,s) || !ActivePathSkills.eligible(p,event.getEntity()))return;
        if(s.path==StarRailPath.REMEMBRANCE && event.getEntity().getUUID().equals(s.target))
            s.recorded+=Math.min(event.getEntity().getHealth(),Math.max(0,event.getAmount()));
        if(s.path==StarRailPath.ELATION && s.hits<3 && s.lastHit!=p.level().getGameTime()) {
            s.lastHit=p.level().getGameTime();s.hits++;s.bonuses.add(new Bonus(event.getEntity().getUUID(),event.getEntity().position(),s.hits==3));
        }
        ActivePathSkills.sync(p);
    }
    @SubscribeEvent public static void tickGrants(TickEvent.ServerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        var iterator=GRANTS.entrySet().iterator();
        while(iterator.hasNext()) {
            var entry=iterator.next();ServerPlayer recipient=entry.getKey();
            if(!recipient.isAlive() || recipient.isSpectator() || recipient.server.getPlayerList().getPlayer(recipient.getUUID())!=recipient)entry.getValue().clear();
            else entry.getValue().entrySet().removeIf(grant->{
                ServerPlayer source=recipient.server.getPlayerList().getPlayer(grant.getKey());
                return grant.getValue().ticks()<=1 || source==null || !source.isAlive() || source.isSpectator() || ActivePathSkills.path(source)!=StarRailPath.HARMONY
                        || ActivePathSkills.rank(source)<5 || !source.level().dimension().location().equals(grant.getValue().dimension())
                        || !recipient.level().dimension().location().equals(grant.getValue().dimension())
                        || source!=recipient && (source.getTeam()==null || !source.isAlliedTo(recipient));
            });
            entry.getValue().replaceAll((id,g)->new Grant(g.ticks()-1,g.dimension()));updateAttributes(recipient);
            if(entry.getValue().isEmpty())iterator.remove();
        }
    }
    private static void updateAttributes(ServerPlayer p) {
        boolean active=GRANTS.containsKey(p) && !GRANTS.get(p).isEmpty();
        var attack=p.getAttribute(Attributes.ATTACK_DAMAGE);var armor=p.getAttribute(Attributes.ARMOR);
        if(attack!=null) {
            if(active && attack.getModifier(ATTACK)==null)attack.addTransientModifier(new AttributeModifier(ATTACK,"starrail_active_harmony_attack",HARMONY_ATTACK,AttributeModifier.Operation.MULTIPLY_BASE));
            if(!active)attack.removeModifier(ATTACK);
        }
        if(armor!=null) {
            if(active && armor.getModifier(ARMOR)==null)armor.addTransientModifier(new AttributeModifier(ARMOR,"starrail_active_harmony_armor",HARMONY_ARMOR,AttributeModifier.Operation.MULTIPLY_BASE));
            if(!active)armor.removeModifier(ARMOR);
        }
    }
}
