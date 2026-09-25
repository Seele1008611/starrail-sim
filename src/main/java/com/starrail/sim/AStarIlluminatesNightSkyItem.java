package com.starrail.sim;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.UUID;

/** The Erudition light cone: A Star Illuminates the Night Sky. */
public final class AStarIlluminatesNightSkyItem extends Item implements ICurioItem {
    private static final String SUPERIMPOSITION_TAG = "Superimposition";

    private static final UUID ATTACK_DAMAGE_MODIFIER_ID =
            UUID.fromString("f6dba7d8-9fc9-4d31-9a65-9a820c1e2af1");
    private static final UUID MAX_HEALTH_MODIFIER_ID =
            UUID.fromString("f6dba7d8-9fc9-4d31-9a65-9a820c1e2af2");
    private static final UUID ARMOR_MODIFIER_ID =
            UUID.fromString("f6dba7d8-9fc9-4d31-9a65-9a820c1e2af3");

    public AStarIlluminatesNightSkyItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> modifiers =
                ImmutableMultimap.builder();
        modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                ATTACK_DAMAGE_MODIFIER_ID, "A Star Illuminates the Night Sky attack damage",
                0.10D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        modifiers.put(Attributes.MAX_HEALTH, new AttributeModifier(
                MAX_HEALTH_MODIFIER_ID, "A Star Illuminates the Night Sky max health",
                0.05D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        modifiers.put(Attributes.ARMOR, new AttributeModifier(
                ARMOR_MODIFIER_ID, "A Star Illuminates the Night Sky armor", 0.05D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        return modifiers.build();
    }

    public static int getSuperimposition(ItemStack stack) {
        if (!(stack.getItem() instanceof AStarIlluminatesNightSkyItem) || !stack.hasTag()) {
            return 1;
        }
        return Math.max(1, Math.min(5, stack.getTag().getInt(SUPERIMPOSITION_TAG)));
    }

    public static void setSuperimposition(ItemStack stack, int level) {
        if (stack.getItem() instanceof AStarIlluminatesNightSkyItem) {
            stack.getOrCreateTag().putInt(SUPERIMPOSITION_TAG,
                    Math.max(1, Math.min(5, level)));
        }
    }
}
