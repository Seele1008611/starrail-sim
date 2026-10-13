package com.starrail.sim;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;
import java.util.*;

/** Four server-owned loadouts store relic identity, never item copies. */
@Mod.EventBusSubscriber(modid=StarRailSimMod.MOD_ID)
public final class StarRailRelicPresetService {
    private static final String ROOT="starrail_sim_relic_presets";
    private static final Map<ServerPlayer,Long> REQUESTS=new WeakHashMap<>();
    public enum Operation { SAVE, APPLY, CLEAR }
    public record Result(boolean accepted,String key) { }
    private StarRailRelicPresetService() { }

    @SubscribeEvent public static void clone(PlayerEvent.Clone event) {
        CompoundTag old=event.getOriginal().getPersistentData();
        if(old.contains(ROOT,Tag.TAG_COMPOUND))event.getEntity().getPersistentData().put(ROOT,old.getCompound(ROOT).copy());
    }
    public static boolean allow(ServerPlayer player) {
        long now=player.serverLevel().getGameTime();Long last=REQUESTS.get(player);
        if(last!=null&&now>=last&&now-last<5)return false;
        REQUESTS.put(player,now);return true;
    }
    public static CompoundTag snapshots(ServerPlayer player) {
        CompoundTag result=new CompoundTag();CompoundTag stored=player.getPersistentData().getCompound(ROOT);
        for(int slot=0;slot<4;slot++)if(stored.contains("slot_"+slot,Tag.TAG_COMPOUND))
            result.putInt("slot_"+slot,stored.getCompound("slot_"+slot).getList("relics",Tag.TAG_COMPOUND).size());
        return result;
    }
    public static ListTag entries(ServerPlayer player,int slot) {
        CompoundTag root=player.getPersistentData().getCompound(ROOT);
        return root.contains("slot_"+slot,Tag.TAG_COMPOUND)?root.getCompound("slot_"+slot).getList("relics",Tag.TAG_COMPOUND):new ListTag();
    }
    public static boolean exists(ServerPlayer player,int slot) {return player.getPersistentData().getCompound(ROOT).contains("slot_"+slot,Tag.TAG_COMPOUND);}
    public static Result perform(ServerPlayer player,int slot,Operation operation) {
        if(player==null||player.isRemoved()||player.level().isClientSide()||operation==null||slot<0||slot>=4)return new Result(false,"preset_invalid");
        return switch(operation){case SAVE->save(player,slot);case APPLY->apply(player,slot);case CLEAR->clear(player,slot);};
    }
    private static Result save(ServerPlayer player,int slot) {
        CompoundTag preset=new CompoundTag();ListTag relics=new ListTag();Set<UUID> seen=new HashSet<>();
        final boolean[] duplicate={false};
        CuriosApi.getCuriosInventory(player).ifPresent(curios->{for(RelicStackData.Part part:RelicStackData.Part.values())
            curios.getStacksHandler(part.curiosSlot()).ifPresent(handler->{
                for(int i=0;i<handler.getSlots();i++)RelicStackData.read(handler.getStacks().getStackInSlot(i)).ifPresent(data->{
                    if(data.part()!=part)return;
                    if(!seen.add(data.instanceId())){duplicate[0]=true;return;}
                    CompoundTag entry=new CompoundTag();entry.putString("part",part.name());entry.putUUID("id",data.instanceId());relics.add(entry);
                });
            });});
        if(duplicate[0])return new Result(false,"preset_duplicate");
        preset.put("relics",relics);CompoundTag root=player.getPersistentData().getCompound(ROOT);root.put("slot_"+slot,preset);player.getPersistentData().put(ROOT,root);
        return new Result(true,"preset_saved");
    }
    private static Result clear(ServerPlayer player,int slot) {
        CompoundTag root=player.getPersistentData().getCompound(ROOT);root.remove("slot_"+slot);player.getPersistentData().put(ROOT,root);return new Result(true,"preset_cleared");
    }
    private static Result apply(ServerPlayer player,int slot) {
        if(!exists(player,slot))return new Result(false,"preset_empty");
        EnumMap<RelicStackData.Part,UUID> desired=new EnumMap<>(RelicStackData.Part.class);Set<UUID> unique=new HashSet<>();
        ListTag saved=entries(player,slot);
        for(int i=0;i<saved.size();i++) {
            CompoundTag entry=saved.getCompound(i);RelicStackData.Part part;
            try{part=RelicStackData.Part.valueOf(entry.getString("part"));}catch(IllegalArgumentException error){return new Result(false,"preset_invalid");}
            if(!entry.hasUUID("id")||desired.put(part,entry.getUUID("id"))!=null||!unique.add(entry.getUUID("id")))
                return new Result(false,"preset_invalid");
        }
        StarRailRelicOperationService.LocatedRelic[] targets=new StarRailRelicOperationService.LocatedRelic[RelicStackData.Part.values().length];
        for(var part:RelicStackData.Part.values()) {
            UUID id=desired.get(part);if(id==null)continue;
            StarRailRelicOperationService.LocatedRelic found=StarRailRelicOperationService.locateForPreset(player,id);
            if(found==null)return new Result(false,"preset_missing_relic");
            RelicStackData.Data data=RelicStackData.read(found.stack()).orElse(null);
            if(data==null||data.part()!=part||data.hasPendingReroll())return new Result(false,"preset_invalid");
            targets[part.ordinal()]=found;
        }
        var curiosOpt=CuriosApi.getCuriosInventory(player);if(!curiosOpt.isPresent())return new Result(false,"preset_invalid");
        var curios=curiosOpt.orElse(null);if(curios==null)return new Result(false,"preset_invalid");IItemHandlerModifiable[] handlers=new IItemHandlerModifiable[targets.length];int[] destination=new int[targets.length];
        List<ItemStack> outgoing=new ArrayList<>();Set<UUID> targetIds=desired.isEmpty()?Set.of():desired.values().stream().collect(java.util.stream.Collectors.toSet());
        for(var part:RelicStackData.Part.values()) {
            var optional=curios.getStacksHandler(part.curiosSlot());if(optional.isEmpty())return new Result(false,"preset_invalid");
            IItemHandlerModifiable stacks=optional.orElseThrow().getStacks();handlers[part.ordinal()]=stacks;
            int current=-1,empty=-1;List<ItemStack> worn=new ArrayList<>();
            for(int i=0;i<stacks.getSlots();i++) {
                ItemStack stack=stacks.getStackInSlot(i);if(stack.isEmpty()){if(empty<0)empty=i;continue;}
                RelicStackData.Data data=RelicStackData.read(stack).orElse(null);
                if(data!=null&&data.part()==part){worn.add(stack);current=i;}
            }
            if(worn.size()>1)return new Result(false,"preset_invalid");
            destination[part.ordinal()]=current>=0?current:empty;
            if(destination[part.ordinal()]<0&&targets[part.ordinal()]!=null&&!targets[part.ordinal()].equipped())return new Result(false,"preset_invalid");
            for(ItemStack stack:worn) {
                UUID id=RelicStackData.read(stack).orElseThrow().instanceId();
                if(!targetIds.contains(id))outgoing.add(stack.copy());
            }
        }
        Inventory inventory=player.getInventory();List<ItemStack> simulated=new ArrayList<>();
        for(int i=0;i<inventory.items.size();i++)simulated.add(inventory.items.get(i).copy());
        for(StarRailRelicOperationService.LocatedRelic target:targets)if(target!=null&&!target.equipped()) {
            if(target.inventorySlot()<0||target.inventorySlot()>=inventory.getContainerSize())return new Result(false,"preset_missing_relic");
            if(target.inventorySlot()<simulated.size()){if(!containsId(simulated.get(target.inventorySlot()),RelicStackData.read(target.stack()).orElseThrow().instanceId()))return new Result(false,"preset_missing_relic");simulated.set(target.inventorySlot(),ItemStack.EMPTY);}
        }
        for(ItemStack stack:outgoing)if(!simulateInsert(simulated,stack.copy()))return new Result(false,"preset_inventory_full");
        // All checks are complete. Commit the six slots in one server tick.
        for(StarRailRelicOperationService.LocatedRelic target:targets)if(target!=null&&!target.equipped())inventory.setItem(target.inventorySlot(),ItemStack.EMPTY);
        for(var part:RelicStackData.Part.values()) {
            IItemHandlerModifiable stacks=handlers[part.ordinal()];
            for(int i=0;i<stacks.getSlots();i++)if(RelicStackData.read(stacks.getStackInSlot(i)).map(d->d.part()==part).orElse(false))stacks.setStackInSlot(i,ItemStack.EMPTY);
        }
        for(ItemStack stack:outgoing)inventory.add(stack);
        for(var part:RelicStackData.Part.values()) {
            StarRailRelicOperationService.LocatedRelic target=targets[part.ordinal()];if(target==null)continue;
            ItemStack equipped=target.stack().copy();RelicStackData.read(equipped).ifPresent(d->RelicStackData.write(equipped,RelicStackData.touch(d)));
            handlers[part.ordinal()].setStackInSlot(destination[part.ordinal()],equipped);
        }
        inventory.setChanged();StarRailRelicService.refresh(player);return new Result(true,"preset_applied");
    }
    private static boolean containsId(ItemStack stack,UUID id){return RelicStackData.read(stack).map(d->d.instanceId().equals(id)).orElse(false);}
    private static boolean simulateInsert(List<ItemStack> slots,ItemStack stack) {
        if(stack.isEmpty())return true;
        for(ItemStack current:slots)if(!current.isEmpty()&&ItemStack.isSameItemSameTags(current,stack)) {
            int move=Math.min(stack.getCount(),current.getMaxStackSize()-current.getCount());current.grow(move);stack.shrink(move);if(stack.isEmpty())return true;
        }
        for(int i=0;i<slots.size();i++)if(slots.get(i).isEmpty()) {
            int move=Math.min(stack.getCount(),stack.getMaxStackSize());ItemStack placed=stack.copy();placed.setCount(move);slots.set(i,placed);stack.shrink(move);if(stack.isEmpty())return true;
        }
        return false;
    }
}
