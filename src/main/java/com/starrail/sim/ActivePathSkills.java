package com.starrail.sim;

import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

/** Version content 4.2/4.3: one server-owned entry point; Hunt and Preservation first. */
@Mod.EventBusSubscriber(modid=StarRailSimMod.MOD_ID)
public final class ActivePathSkills {
    private static final String ROOT="starrail_active_skills",CD="cooldowns";
    public static final double HUNT_RANGE=18,HUNT_MULTIPLIER=2.2;
    public static final int UNLOCK_LEVEL=5,SHIELD_DURATION=160;
    private static final Map<ServerPlayer,Integer> REQUESTS=new WeakHashMap<>();
    private ActivePathSkills() {}
    public static boolean supported(StarRailPath path) {return path.isRealPath();}
    public static int cooldown(StarRailPath path) {return switch(path) {
        case HUNT -> 360;case PRESERVATION -> 500;case DESTRUCTION,ELATION -> 440;
        case ERUDITION -> 480;case HARMONY,ABUNDANCE -> 600;case NIHILITY -> 520;case REMEMBRANCE -> 500;default -> 0;
    };}
    public static Component name(StarRailPath path) {return Component.translatable("skill.starrail_sim."+path.getId());}
    public static Component description(StarRailPath path) {
        return switch(path) {
            case HUNT -> Component.translatable("skill.starrail_sim.desc.hunt",UNLOCK_LEVEL,(int)HUNT_RANGE,HUNT_MULTIPLIER,cooldown(path)/20);
            case PRESERVATION -> Component.translatable("skill.starrail_sim.desc.preservation",UNLOCK_LEVEL,20,.5,SHIELD_DURATION/20,cooldown(path)/20);
            default -> ExtraActivePathSkills.description(path);
        };
    }
    private static CompoundTag state(ServerPlayer player) {
        CompoundTag persistent=player.getPersistentData();
        if(!persistent.contains(ROOT))persistent.put(ROOT,new CompoundTag());
        return persistent.getCompound(ROOT);
    }
    private static CompoundTag cooldowns(CompoundTag state) {
        if(!state.contains(CD))state.put(CD,new CompoundTag());return state.getCompound(CD);
    }
    static StarRailPath path(ServerPlayer player) {return player.getCapability(StarRailPathCapability.PATH_DATA).map(IStarRailPathData::getCurrentPath).orElse(StarRailPath.NONE);}
    static int rank(ServerPlayer player) {return player.getCapability(StarRailPathCapability.PATH_DATA).map(d->d.getCurrentPathRank().getLevel()).orElse(0);}
    static void message(ServerPlayer player,String key,Object... args) {player.displayClientMessage(Component.translatable("skill.starrail_sim."+key,args),true);}
    public static void cast(ServerPlayer player) {
        Integer prior=REQUESTS.put(player,player.tickCount);
        if(prior!=null && player.tickCount-prior<5)return;
        if(!player.isAlive() || player.isSpectator() || player.isSleeping())return;
        StarRailPath path=path(player);CompoundTag state=state(player),cd=cooldowns(state);
        reconcile(player,state);
        if(!path.isRealPath() || rank(player)<UNLOCK_LEVEL) {message(player,"locked");sync(player);return;}
        if(!supported(path)) {message(player,"prototype");sync(player);return;}
        int left=cd.getInt(path.getId());
        if(left>0) {message(player,"cooldown",String.format(Locale.ROOT,"%.1f",left/20f));sync(player);return;}
        Vec3 from=player.getEyePosition(),to=player.position();boolean hit=false;
        if(path==StarRailPath.HUNT) {
            Vec3 end=from.add(player.getLookAngle().scale(HUNT_RANGE));
            HitResult block=player.level().clip(new ClipContext(from,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
            if(block.getType()!=HitResult.Type.MISS)end=block.getLocation();
            var target=ProjectileUtil.getEntityHitResult(player,from,end,
                    player.getBoundingBox().expandTowards(end.subtract(from)).inflate(1),
                    e->e instanceof LivingEntity living && living.isAlive() && e instanceof Enemy
                            && !player.isAlliedTo(e) && !e.isSpectator()
                            && (!(living instanceof net.minecraft.world.entity.NeutralMob neutral) || neutral.isAngryAt(player)),from.distanceToSqr(end));
            if(target==null || !(target.getEntity() instanceof LivingEntity living) || !player.hasLineOfSight(living)) {message(player,"target");sync(player);return;}
            to=target.getLocation();
            float damage=(float)(player.getAttributeValue(Attributes.ATTACK_DAMAGE)*HUNT_MULTIPLIER);
            cd.putInt(path.getId(),CurioEffects.skillCooldown(player,cooldown(path))); // Commit before hurt callbacks can re-enter.
            hit=StarRailOwnedDamage.activeHurt(player,living,damage);
        } else if(path==StarRailPath.PRESERVATION) {
            cd.putInt(path.getId(),CurioEffects.skillCooldown(player,cooldown(path)));
            float amount=StarRailRelicService.boostShieldAmount(player,(float)(player.getMaxHealth()*.2+player.getAttributeValue(Attributes.ARMOR)*.5));
            state.putFloat("shield",Math.max(0,amount));state.putInt("shield_ticks",SHIELD_DURATION);
            state.putString("shield_dimension",player.level().dimension().location().toString());
            to=player.position().add(0,player.getBbHeight()*.5,0);
        } else {
            if(ExtraActivePathSkills.cast(player,path)) {message(player,"cast",name(path));sync(player);}
            return;
        }
        StarRailNetwork.CHANNEL.send(PacketDistributor.NEAR.with(()->new PacketDistributor.TargetPoint(
                player.getX(),player.getY(),player.getZ(),48,player.level().dimension())),
                new ActiveSkillFxPacket(path,player.level().dimension().location(),player.level().getGameTime(),from,to,player.getId(),hit));
        message(player,"cast",name(path));sync(player);
    }
    static void startCooldown(ServerPlayer player,StarRailPath path) {cooldowns(state(player)).putInt(path.getId(),CurioEffects.skillCooldown(player,cooldown(path)));}
    static boolean eligible(ServerPlayer player,net.minecraft.world.entity.Entity target) {
        return target instanceof LivingEntity living && living.isAlive() && target instanceof Enemy
                && !target.isSpectator() && !player.isAlliedTo(target)
                && (!(living instanceof net.minecraft.world.entity.NeutralMob neutral) || neutral.isAngryAt(player));
    }
    private static void clearShield(CompoundTag state) {state.remove("shield");state.remove("shield_ticks");state.remove("shield_dimension");}
    private static void reconcile(ServerPlayer player,CompoundTag state) {
        String current=path(player).getId();
        if(!current.equals(state.getString("path")) || rank(player)<UNLOCK_LEVEL
                || !player.level().dimension().location().toString().equals(state.getString("shield_dimension")))clearShield(state);
        state.putString("path",current);
        ExtraActivePathSkills.reconcile(player);
    }
    public static void sync(ServerPlayer player) {
        CompoundTag s=state(player);StarRailPath path=path(player);
        StarRailNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(()->player),new ActiveSkillStatePacket(
                player.getUUID(),player.level().dimension().location(),path,rank(player),cooldowns(s).getInt(path.getId()),s.getFloat("shield"),s.getInt("shield_ticks"),ExtraActivePathSkills.remaining(player),ExtraActivePathSkills.charges(player),ExtraActivePathSkills.recorded(player)));
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END || !(event.player instanceof ServerPlayer player))return;
        CompoundTag s=state(player),cd=cooldowns(s);String old=s.getString("path");reconcile(player,s);
        for(StarRailPath path:StarRailPath.values())if(cd.getInt(path.getId())>0)cd.putInt(path.getId(),cd.getInt(path.getId())-1);
        if(s.getInt("shield_ticks")>0) {
            s.putInt("shield_ticks",s.getInt("shield_ticks")-1);
            if(s.getInt("shield_ticks")==0) {clearShield(s);sync(player);}
        }
        ExtraActivePathSkills.tick(player);
        if(player.tickCount%20==0 || !old.equals(s.getString("path")))sync(player);
    }
    /** Independent pool after armour and existing yellow hearts; never edits someone else's absorption. */
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void absorb(LivingDamageEvent event) {
        if(event.isCanceled() || event.getAmount()<=0 || !(event.getEntity() instanceof ServerPlayer player)
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY))return;
        CompoundTag s=state(player);reconcile(player,s);
        if(s.getInt("shield_ticks")<=0 || s.getFloat("shield")<=0)return;
        float used=Math.min(event.getAmount(),s.getFloat("shield"));
        s.putFloat("shield",Math.max(0,s.getFloat("shield")-used));event.setAmount(Math.max(0,event.getAmount()-used));sync(player);
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if(event.getEntity() instanceof ServerPlayer p) {clearShield(state(p));ExtraActivePathSkills.cancel(p);REQUESTS.remove(p);sync(p);}
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if(event.getEntity() instanceof ServerPlayer p) {clearShield(state(p));ExtraActivePathSkills.cancel(p);REQUESTS.remove(p);}
    }
    @SubscribeEvent public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if(event.getEntity() instanceof ServerPlayer p) {clearShield(state(p));ExtraActivePathSkills.cancel(p);sync(p);}
    }
    @SubscribeEvent public static void death(LivingDeathEvent event) {
        if(!event.isCanceled() && event.getEntity() instanceof ServerPlayer p) {clearShield(state(p));ExtraActivePathSkills.cancel(p);}
    }
    @SubscribeEvent public static void clone(PlayerEvent.Clone event) {
        if(event.getOriginal() instanceof ServerPlayer old && event.getEntity() instanceof ServerPlayer fresh) {
            CompoundTag s=new CompoundTag();s.put(CD,cooldowns(state(old)).copy());fresh.getPersistentData().put(ROOT,s);
        }
    }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event) {if(event.getEntity() instanceof ServerPlayer p)sync(p);}
    @SubscribeEvent public static void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("starrailskill").then(Commands.literal("status").executes(c->{
            ServerPlayer p=c.getSource().getPlayerOrException();CompoundTag s=state(p);reconcile(p,s);sync(p);
            c.getSource().sendSuccess(()->Component.literal("path="+path(p).getId()+", level="+rank(p)+", cooldown_ticks="+cooldowns(s).getInt(path(p).getId())+", shield="+s.getFloat("shield")+", shield_ticks="+s.getInt("shield_ticks")+", window="+ExtraActivePathSkills.status(p)+", all_cooldowns="+cooldowns(s)),false);return 1;
        })));
    }
}
