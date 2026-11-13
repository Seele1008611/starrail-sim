package com.starrail.sim.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starrail.sim.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.WeakHashMap;

/** Approved 1.27 animal geometry, parent bones, face UVs and native animation tracks. */
public final class PathAnimalModel<T extends Mob> extends EntityModel<T> {
    public enum Species {
        SUN_GRAIN_DEER("sunnyharvest_deer"), STARFLEECE_RAM("starvelvet_ram"),
        WOODLAND_FORAGER("woodland_forager"), MARSH_FOWL("stilltide_fowl"),
        STARGRAVEL_PACKBEAST("star_scree_packbeast"), GEM_LIZARD("gem_lizard"),
        LOTUS_OTTER("lotus_otter"),DUSKRED_FISH("duskred_fish"),ROCK_CRAB("rockdwelling_crab"),ORANGE_FINCH("duskorange_finch");
        private final String id;
        Species(String id) { this.id=id; }
        public String id() { return id; }
    }
    private record Action(String name, float started) {}
    private final Species species;
    private final BlockbenchRig rig;
    private final Map<T, Action> actions = new WeakHashMap<>();
    private Matrix4f[] pose;
    private final boolean[] visible;
    private float unitsToBlocks;
    private final Vector3f transformedNormal = new Vector3f();

    public PathAnimalModel(Species species) {
        super(RenderType::entityCutoutNoCull);
        this.species=species;
        String resource="/assets/starrail_sim/entity_models/"+species.id()+".json";
        try(InputStream in=PathAnimalModel.class.getResourceAsStream(resource)) {
            if(in==null) throw new IllegalStateException("Missing animal rig "+resource);
            rig=BlockbenchRig.read(new InputStreamReader(in,StandardCharsets.UTF_8));
        } catch(Exception e) { throw new IllegalStateException("Cannot load animal rig "+species,e); }
        pose=rig.matrices(rig.sample("idle",0,1));
        visible=new boolean[rig.nodes.size()];
        unitsToBlocks=1/16f;
    }
    public ResourceLocation texture() {
        return new ResourceLocation("starrail_sim","textures/entity/path_animals/"+species.id()+".png");
    }
    @Override
    public void setupAnim(T entity,float limbSwing,float limbSwingAmount,float ageInTicks,float netHeadYaw,float headPitch) {
        float partial=Mth.clamp(ageInTicks-entity.tickCount,0,1);
        // The renderer applies the juvenile half scale once. Never scale from the baby's smaller AABB here.
        unitsToBlocks=entity.getType().getHeight()/rig.height;
        String clip="idle";
        float time=ageInTicks/20f,weight=1;
        boolean action=false;
        if(entity.deathTime>0) { clip="death";time=(entity.deathTime+partial)/20f*rig.length(clip);action=true; }
        else if(entity.hurtTime>0) { clip=species==Species.DUSKRED_FISH?"panic":"hurt";time=(10-entity.hurtTime+partial)/10f*rig.length(clip);action=true; }
        else {
            if(entity instanceof SunnyHarvestDeer deer) {
                if(deer.isAlerted()) clip="alert";
                else if(deer.isGrazing()) clip="graze";
            } else if(entity instanceof StarvelvetRam ram) {
                if(ram.isShearReacting()) clip="shear";
                else if(ram.getHeadEatPositionScale(partial)>0) clip="graze";
                else if(!ram.onGround() && !ram.isInWaterOrBubble()) clip="hop";
            } else if(entity instanceof WoodlandForager forager && forager.isForaging()) clip="forage";
            else if(entity instanceof StarScreePackbeast horse) {
                if(!horse.onGround() && !horse.isInWaterOrBubble()) clip="jump";
                else if(horse.getStandAnim(partial)>.05f) clip="rear";
                else if(horse.isEating()) clip="graze";
            }
            action=!clip.equals("idle");
            if(action) {
                Action previous=actions.get(entity);
                if(previous==null || !previous.name().equals(clip)) {
                    previous=new Action(clip,ageInTicks);actions.put(entity,previous);
                }
                time=(ageInTicks-previous.started())/20f;
            } else {
                actions.remove(entity);
                if(species==Species.LOTUS_OTTER && entity.isInWater() && entity.getDeltaMovement().y<-.04)clip="dive";
                else if(entity.isInWaterOrBubble() && rig.clips.containsKey("swim")) clip="swim";
                else if(species==Species.ORANGE_FINCH && !entity.onGround())clip="flap";
                else if(species==Species.MARSH_FOWL && !entity.onGround()) clip="flap";
                else if(limbSwingAmount>.025f) {
                    clip=limbSwingAmount>.55f && rig.clips.containsKey("run")?"run":"walk";
                    time=limbSwing/9.424778f*rig.length(clip);
                    weight=Mth.clamp(limbSwingAmount*3,0,1);
                } else if(species==Species.LOTUS_OTTER && ageInTicks%240<40) {
                    clip="groom";time=(ageInTicks%240)/20f;action=true;
                } else if(species==Species.ROCK_CRAB && ageInTicks%200<36) {
                    clip="claw";time=(ageInTicks%200)/20f;action=true;
                } else if(species==Species.ORANGE_FINCH && ageInTicks%160<24) {
                    clip="peck";time=(ageInTicks%160)/20f;action=true;
                } else if(species==Species.GEM_LIZARD && entity.level().isDay() && entity.onGround()) {
                    clip="bask";
                } else if(species==Species.MARSH_FOWL && ageInTicks%160<24) {
                    clip="peck";time=(ageInTicks%160)/20f;action=true;
                } else if(rig.clips.containsKey("look") && ageInTicks%240<24) {
                    clip="look";time=(ageInTicks%240)/20f;action=true;
                }
            }
        }
        if(clip.equals("graze") && entity instanceof StarvelvetRam) time*=rig.length(clip)/2.0f;
        // Horse rearing is already synchronized; sample its peak pose with that weight.
        // Passenger placement uses the same peak-body transform on both physical sides.
        if(clip.equals("rear") && entity instanceof StarScreePackbeast horse) {
            time=.5f;weight=horse.getStandAnim(partial);
        }
        float[][] locals=rig.sample(clip,time,weight);
        if(!action) rig.aim(locals,"头部",-Mth.clamp(headPitch,-30,35)*Mth.DEG_TO_RAD,
                -Mth.clamp(netHeadYaw,-65,65)*Mth.DEG_TO_RAD);
        pose=rig.matrices(locals);
        boolean sheared=entity instanceof StarvelvetRam ram && ram.isSheared();
        boolean saddled=entity instanceof StarScreePackbeast horse && horse.isSaddled();
        for(int i=0;i<visible.length;i++) {
            var node=rig.nodes.get(i);String name=node.name();
            boolean fleece=name.contains("绒毛") || name.contains("绒块") || name.contains("围毛");
            visible[i]=(node.parent()<0 || visible[node.parent()])
                    && !(sheared && fleece) && !(name.equals("鞍具") && !saddled);
        }
    }

    @Override
    public void renderToBuffer(PoseStack stack, VertexConsumer out, int light, int overlay,
                               float red, float green, float blue, float alpha) {
        stack.pushPose();
        // Cancel LivingEntityRenderer's Y-down model basis and put the native ground at entity feet.
        stack.translate(0,1.501,0);
        stack.scale(-unitsToBlocks,-unitsToBlocks,unitsToBlocks);
        for (BlockbenchRig.Geometry mesh : rig.geometry) {
            if (!visible[mesh.node()]) continue;
            Matrix4f matrix = new Matrix4f(stack.last().pose()).mul(pose[mesh.node()]);
            Matrix3f normal = matrix.normal(new Matrix3f());
            float[] vertices = mesh.vertices();
            int[] indices = mesh.indices();
            for (int i=0; i<indices.length; i+=3) {
                // The entity RenderType consumes quads; repeat the last triangle vertex.
                for (int corner=0; corner<4; corner++) {
                    int v = indices[i+Math.min(corner,2)]*8;
                    normal.transform(vertices[v+5],vertices[v+6],vertices[v+7],transformedNormal).normalize();
                    out.vertex(matrix,vertices[v],vertices[v+1],vertices[v+2]).color(red,green,blue,alpha)
                            .uv(vertices[v+3],vertices[v+4]).overlayCoords(overlay).uv2(light)
                            .normal(transformedNormal.x,transformedNormal.y,transformedNormal.z).endVertex();
                }
            }
        }
        stack.popPose();
    }
}
