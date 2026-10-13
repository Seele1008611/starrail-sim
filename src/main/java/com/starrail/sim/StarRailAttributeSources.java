package com.starrail.sim;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.*;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import java.util.*;

/** Reads existing attributes without rebuilding buffs or changing gameplay state. */
public final class StarRailAttributeSources {
    private static final Map<ServerPlayer,Long> REQUESTS=new WeakHashMap<>();
    private StarRailAttributeSources() { }
    public static boolean allowRequest(ServerPlayer player) {
        long now=player.serverLevel().getGameTime();Long last=REQUESTS.get(player);
        if(last!=null && now>=last && now-last<10)return false;
        REQUESTS.put(player,now);return true;
    }
    public static AttributeSourcesPacket snapshot(ServerPlayer player) {
        Attribute[] attributes={Attributes.MAX_HEALTH,Attributes.ATTACK_DAMAGE,Attributes.ARMOR,
                Attributes.ARMOR_TOUGHNESS,Attributes.MOVEMENT_SPEED,Attributes.ATTACK_SPEED,
                StarRailAttributes.CRIT_RATE.get(),StarRailAttributes.CRIT_DAMAGE.get(),StarRailAttributes.BREAK_EFFECT.get(),
                StarRailAttributes.EFFECT_HIT_RATE.get(),StarRailAttributes.EFFECT_RESISTANCE.get(),
                Attributes.KNOCKBACK_RESISTANCE,StarRailAttributes.HEALING_EFFECT.get()};
        Map<UUID,Integer> known=new HashMap<>();
        for(EquipmentSlot slot:EquipmentSlot.values())
            player.getItemBySlot(slot).getAttributeModifiers(slot).values().forEach(m->known.put(m.getId(),3));
        player.getActiveEffects().forEach(effect->effect.getEffect().getAttributeModifiers().values()
                .forEach(m->known.put(m.getId(),5)));
        CuriosApi.getCuriosInventory(player).ifPresent(curios->curios.getCurios().forEach((id,handler)->{
            for(int i=0;i<handler.getSlots();i++) {
                var stack=handler.getStacks().getStackInSlot(i);
                if(stack.getItem() instanceof ICurioItem item) {
                    var context=new SlotContext(id,player,i,false,true);
                    item.getAttributeModifiers(context,UUID.nameUUIDFromBytes((id+i).getBytes(java.nio.charset.StandardCharsets.UTF_8)),stack)
                            .values().forEach(m->known.put(m.getId(),id.equals("light_cone")?2:3));
                }
            }
        }));
        var rows=new ArrayList<AttributeSourcesPacket.Row>();
        for(int index=0;index<attributes.length;index++) {
            Attribute attribute=attributes[index];AttributeInstance instance=player.getAttribute(attribute);
            double base=instance==null?attribute.getDefaultValue():instance.getBaseValue();
            double[][] groups=new double[AttributeSourcesPacket.SOURCES][3];
            for(double[] g:groups)g[2]=1;
            if(instance!=null)for(AttributeModifier modifier:instance.getModifiers()) {
                int source=source(modifier,known);
                switch(modifier.getOperation()) {
                    case ADDITION -> groups[source][0]+=modifier.getAmount();
                    case MULTIPLY_BASE -> groups[source][1]+=modifier.getAmount();
                    case MULTIPLY_TOTAL -> groups[source][2]*=1+modifier.getAmount();
                }
            }
            double additive=base,basePercent=0,totalFactor=1;
            for(double[] g:groups){additive+=g[0];basePercent+=g[1];totalFactor*=g[2];}
            double raw=additive*(1+basePercent)*totalFactor;
            double total=instance==null?base:instance.getValue();
            // Match the existing attack display's fallback for an unsynchronized held-item modifier.
            if(index==1 && instance!=null) {
                total=StarRailAttributes.getAttackDamage(player);
                double missingFactor=1,missingBase=0,missingAdd=0;
                for(AttributeModifier m:player.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND).get(attribute)) {
                    if(instance.getModifier(m.getId())!=null)continue;
                    switch(m.getOperation()) {
                        case ADDITION -> missingAdd+=m.getAmount();
                        case MULTIPLY_BASE -> missingBase+=m.getAmount();
                        case MULTIPLY_TOTAL -> missingFactor*=1+m.getAmount();
                    }
                }
                if(missingAdd!=0||missingBase!=0||missingFactor!=1) {
                    groups[3][0]+=missingAdd;groups[3][1]+=missingBase;groups[3][2]*=missingFactor;
                    raw=(attribute.sanitizeValue(raw)+missingAdd+base*missingBase)*missingFactor;
                }
            }
            var result=new ArrayList<AttributeSourcesPacket.Group>();
            for(double[] g:groups)result.add(new AttributeSourcesPacket.Group(g[0],g[1],g[2]));
            rows.add(new AttributeSourcesPacket.Row(base,total,raw,List.copyOf(result)));
        }
        return new AttributeSourcesPacket(player.getUUID(),List.copyOf(rows));
    }
    private static int source(AttributeModifier modifier,Map<UUID,Integer> known) {
        String name=modifier.getName();
        if(name.startsWith("starrail_sim.relic."))return 4;
        if(name.startsWith("starrail_sim.path."))return 0;
        if(name.endsWith(" traces")||name.startsWith("starrail.trace."))return 1;
        if(StarRailLightConeService.ownsAttributeModifier(modifier.getId()))return 2;
        Integer equipment=known.get(modifier.getId());if(equipment!=null)return equipment;
        if(name.startsWith("starrail_sim."))return 6;
        return 7;
    }
}
