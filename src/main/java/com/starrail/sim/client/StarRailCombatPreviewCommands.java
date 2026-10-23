package com.starrail.sim.client;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.starrail.sim.CombatVfxPacket;
import com.starrail.sim.StarRailSimMod;
import com.starrail.sim.StarRailStatusConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Locale;

/** Client-only visual and audio inspection; previews do not reach the server. */
@Mod.EventBusSubscriber(modid=StarRailSimMod.MOD_ID, value=Dist.CLIENT)
public final class StarRailCombatPreviewCommands {
    private static final List<String> EFFECTS=List.of("hunt_strike","hunt_reverse","preservation_wall",
            "preservation_counter","destruction_rage","destruction_burn","destruction_desperate",
            "erudition_echo","erudition_final","nihility_mark","nihility_spread","nihility_end",
            "harmony_resonance","harmony_afterglow","harmony_concert","harmony_unison",
            "abundance_heal","abundance_manna","abundance_mercy","remembrance_record",
            "remembrance_echo","remembrance_eternal","elation_burst","elation_grand",
            "toughness_hit","toughness_break");

    private StarRailCombatPreviewCommands() { }

    @SubscribeEvent public static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("starrailvfx")
            .then(Commands.literal("preview")
                .then(Commands.argument("effect",StringArgumentType.word())
                    .suggests((context,builder)-> {
                        EFFECTS.stream().filter(id->id.startsWith(builder.getRemainingLowerCase())).forEach(builder::suggest);
                        return builder.buildFuture();
                    })
                    .executes(context->preview(StringArgumentType.getString(context,"effect"),false))
                    .then(Commands.argument("ranged",BoolArgumentType.bool())
                        .executes(context->preview(StringArgumentType.getString(context,"effect"),BoolArgumentType.getBool(context,"ranged"))))))
            .then(Commands.literal("mode")
                .then(Commands.literal("enhanced").executes(context->mode(true)))
                .then(Commands.literal("basic").executes(context->mode(false))))
            .then(Commands.literal("strength").then(Commands.argument("value",DoubleArgumentType.doubleArg(.25,2))
                .executes(context->{StarRailStatusConfig.COMBAT_STRENGTH.set(DoubleArgumentType.getDouble(context,"value"));StarRailStatusConfig.CLIENT.save();return status();})))
            .then(Commands.literal("shake").then(Commands.argument("enabled",BoolArgumentType.bool())
                .executes(context->{StarRailStatusConfig.COMBAT_CAMERA_SHAKE.set(BoolArgumentType.getBool(context,"enabled"));StarRailStatusConfig.CLIENT.save();return status();})))
            .then(Commands.literal("status").executes(context->status())));
    }

    private static int preview(String id,boolean ranged) {
        var player=Minecraft.getInstance().player;
        if(player==null)return 0;
        id=id.toLowerCase(Locale.ROOT);
        if(!EFFECTS.contains(id)) { message(Component.translatable("command.starrail_sim.vfx.invalid"));return 0; }
        var kind=CombatVfxPacket.Kind.valueOf(id.toUpperCase(Locale.ROOT));
        boolean self=switch(kind) {
            case PRESERVATION_WALL,DESTRUCTION_RAGE,DESTRUCTION_DESPERATE,HARMONY_RESONANCE,HARMONY_CONCERT,
                    HARMONY_UNISON,ABUNDANCE_HEAL,ABUNDANCE_MANNA,ABUNDANCE_MERCY,TOUGHNESS_HIT,TOUGHNESS_BREAK -> true;
            default -> false;
        };
        Vec3 look=player.getLookAngle(),flat=new Vec3(look.x,0,look.z).normalize();
        if(flat.lengthSqr()<.01)flat=new Vec3(0,0,1);
        Vec3 origin=player.position().add(0,player.getBbHeight()*.55,0);
        Vec3 impact=self?origin:origin.add(flat.scale(4));
        Vec3 source=kind==CombatVfxPacket.Kind.HARMONY_CONCERT?origin.add(flat.scale(4)):origin;
        if(kind==CombatVfxPacket.Kind.HARMONY_CONCERT)impact=origin;
        StarRailCombatClientEffects.accept(new CombatVfxPacket(kind,kind.path(),ranged,source,impact,
                player.getY(),self?player.getId():-1,kind==CombatVfxPacket.Kind.HARMONY_CONCERT?-1:player.getId()));
        message(Component.translatable("command.starrail_sim.vfx.preview",id));
        return 1;
    }

    private static int mode(boolean enhanced) {
        StarRailStatusConfig.COMBAT_SHADERS.set(enhanced);
        StarRailStatusConfig.CLIENT.save();
        message(Component.translatable("command.starrail_sim.vfx.mode",Component.translatable(
                "ui.starrail_sim.settings."+(enhanced?"enhanced_effects":"base_effects"))));
        return 1;
    }

    private static int status() {
        message(Component.translatable("command.starrail_sim.vfx.status",StarRailCombatShaders.available(),
                StarRailCombatShaders.enhanced(),StarRailCombatMesh.activeEffects(),StarRailCombatMesh.lastFaceCount(),
                String.format(Locale.ROOT,"%.3f",StarRailCombatMesh.lastBuildMillis())));
        message(Component.translatable("command.starrail_sim.vfx.post_status",StarRailCombatPost.available(),
                StarRailCombatPost.lastPasses(),String.format(Locale.ROOT,"%.1f",StarRailCombatPost.allocatedMiB()),
                StarRailStatusConfig.COMBAT_STRENGTH.get(),StarRailStatusConfig.COMBAT_CAMERA_SHAKE.get()));
        return 1;
    }

    private static void message(Component message) {
        var player=Minecraft.getInstance().player;
        if(player!=null)player.displayClientMessage(message,false);
    }
}
