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

/** Remembrance light cone: Love Is Eternal. */
public final class LoveIsEternalItem extends Item implements ICurioItem {
    private static final String SUPERIMPOSITION_TAG = "Superimposition";
    private static final UUID ATTACK_MODIFIER_ID =
            UUID.fromString("d4ce7d1a-a899-4c94-9d49-a6c3fca2a101");
    private static final UUID MAX_HEALTH_MODIFIER_ID =
            UUID.fromString("d4ce7d1a-a899-4c94-9d49-a6c3fca2a102");
    private static final UUID ARMOR_MODIFIER_ID =
            UUID.fromString("d4ce7d1a-a899-4c94-9d49-a6c3fca2a103");

    public LoveIsEternalItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> modifiers =
                ImmutableMultimap.builder();
        modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                ATTACK_MODIFIER_ID, "Love Is Eternal base attack", 0.10D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        modifiers.put(Attributes.MAX_HEALTH, new AttributeModifier(
                MAX_HEALTH_MODIFIER_ID, "Love Is Eternal base max health", 0.05D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        modifiers.put(Attributes.ARMOR, new AttributeModifier(
                ARMOR_MODIFIER_ID, "Love Is Eternal base armor", 0.05D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        return modifiers.build();
    }

    public static int getSuperimposition(ItemStack stack) {
        if (!(stack.getItem() instanceof LoveIsEternalItem) || !stack.hasTag()) {
            return 1;
        }
        return Math.max(1, Math.min(5, stack.getTag().getInt(SUPERIMPOSITION_TAG)));
    }

    public static void setSuperimposition(ItemStack stack, int level) {
        if (stack.getItem() instanceof LoveIsEternalItem) {
            stack.getOrCreateTag().putInt(SUPERIMPOSITION_TAG,
                    Math.max(1, Math.min(5, level)));
        }
    }
}
