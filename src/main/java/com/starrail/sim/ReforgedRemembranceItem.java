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

/** Nihility light cone: Reforged Remembrance. */
public final class ReforgedRemembranceItem extends Item implements ICurioItem {
    private static final String SUPERIMPOSITION_TAG = "Superimposition";
    private static final UUID ATTACK_MODIFIER_ID =
            UUID.fromString("c36d75b1-7fcb-4f1f-8ab5-7681aabf9501");
    private static final UUID MAX_HEALTH_MODIFIER_ID =
            UUID.fromString("c36d75b1-7fcb-4f1f-8ab5-7681aabf9502");
    private static final UUID ARMOR_MODIFIER_ID =
            UUID.fromString("c36d75b1-7fcb-4f1f-8ab5-7681aabf9503");

    public ReforgedRemembranceItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> modifiers =
                ImmutableMultimap.builder();
        modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_MODIFIER_ID,
                "Reforged Remembrance attack", 0.10D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        modifiers.put(Attributes.MAX_HEALTH, new AttributeModifier(MAX_HEALTH_MODIFIER_ID,
                "Reforged Remembrance max health", 0.05D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        modifiers.put(Attributes.ARMOR, new AttributeModifier(ARMOR_MODIFIER_ID,
                "Reforged Remembrance armor", 0.05D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        return modifiers.build();
    }

    public static int getSuperimposition(ItemStack stack) {
        if (!(stack.getItem() instanceof ReforgedRemembranceItem) || !stack.hasTag()) {
            return 1;
        }
        return Math.max(1, Math.min(5, stack.getTag().getInt(SUPERIMPOSITION_TAG)));
    }

    public static void setSuperimposition(ItemStack stack, int level) {
        if (stack.getItem() instanceof ReforgedRemembranceItem) {
            stack.getOrCreateTag().putInt(SUPERIMPOSITION_TAG,
                    Math.max(1, Math.min(5, level)));
        }
    }
}
