package com.starrail.sim;

/**
 * 模组代码说明：自定义光锥物品类，定义装备属性或物品交互行为，并把专属效果接入模组系统。
 */

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

/** First real light cone prototype: In the Night. */
public final class InTheNightItem extends Item implements ICurioItem {
    private static final String SUPERIMPOSITION_TAG = "Superimposition";

    private static final UUID ATTACK_DAMAGE_MODIFIER_ID =
            UUID.fromString("f6dba7d8-9fc9-4d31-9a65-9a820c1e2a01");
    private static final UUID MAX_HEALTH_MODIFIER_ID =
            UUID.fromString("f6dba7d8-9fc9-4d31-9a65-9a820c1e2a02");
    private static final UUID ARMOR_MODIFIER_ID =
            UUID.fromString("f6dba7d8-9fc9-4d31-9a65-9a820c1e2a03");

    public InTheNightItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> modifiers =
                ImmutableMultimap.builder();
        modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                ATTACK_DAMAGE_MODIFIER_ID,
                "In the Night attack damage",
                0.10D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        modifiers.put(Attributes.MAX_HEALTH, new AttributeModifier(
                MAX_HEALTH_MODIFIER_ID,
                "In the Night max health",
                0.05D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        modifiers.put(Attributes.ARMOR, new AttributeModifier(
                ARMOR_MODIFIER_ID,
                "In the Night armor",
                0.05D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        return modifiers.build();
    }

    /** Returns the stack's S-level, defaulting to S1 for a normal /give item. */
    public static int getSuperimposition(ItemStack stack) {
        if (!(stack.getItem() instanceof InTheNightItem) || !stack.hasTag()) {
            return 1;
        }
        return Math.max(1, Math.min(5, stack.getTag().getInt(SUPERIMPOSITION_TAG)));
    }

    /** Used by future item-management UI and commands. */
    public static void setSuperimposition(ItemStack stack, int level) {
        if (stack.getItem() instanceof InTheNightItem) {
            stack.getOrCreateTag().putInt(SUPERIMPOSITION_TAG,
                    Math.max(1, Math.min(5, level)));
        }
    }

}
