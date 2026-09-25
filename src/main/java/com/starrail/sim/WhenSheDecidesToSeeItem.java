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

/** Elation light cone: When She Decides to See. */
public final class WhenSheDecidesToSeeItem extends Item implements ICurioItem {
    private static final String SUPERIMPOSITION_TAG = "Superimposition";
    private static final UUID ATTACK_MODIFIER_ID =
            UUID.fromString("6bb9a60d-4088-4da7-a1f1-5a67ac520001");
    private static final UUID MAX_HEALTH_MODIFIER_ID =
            UUID.fromString("6bb9a60d-4088-4da7-a1f1-5a67ac520002");
    private static final UUID ARMOR_MODIFIER_ID =
            UUID.fromString("6bb9a60d-4088-4da7-a1f1-5a67ac520003");

    public WhenSheDecidesToSeeItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> modifiers =
                ImmutableMultimap.builder();
        modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                ATTACK_MODIFIER_ID, "When She Decides to See base attack", 0.10D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        modifiers.put(Attributes.MAX_HEALTH, new AttributeModifier(
                MAX_HEALTH_MODIFIER_ID, "When She Decides to See base max health",
                0.05D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        modifiers.put(Attributes.ARMOR, new AttributeModifier(
                ARMOR_MODIFIER_ID, "When She Decides to See base armor", 0.05D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        return modifiers.build();
    }

    public static int getSuperimposition(ItemStack stack) {
        if (!(stack.getItem() instanceof WhenSheDecidesToSeeItem) || !stack.hasTag()) {
            return 1;
        }
        return Math.max(1, Math.min(5, stack.getTag().getInt(SUPERIMPOSITION_TAG)));
    }

    public static void setSuperimposition(ItemStack stack, int level) {
        if (stack.getItem() instanceof WhenSheDecidesToSeeItem) {
            stack.getOrCreateTag().putInt(SUPERIMPOSITION_TAG,
                    Math.max(1, Math.min(5, level)));
        }
    }
}
