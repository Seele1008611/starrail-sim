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

/** Light Cone: You Will Rise and Sing. */
public final class YouWillRiseAndSingItem extends Item implements ICurioItem {
    private static final String SUPERIMPOSITION_TAG = "Superimposition";
    private static final UUID ATTACK_ID = UUID.fromString("a1150001-0000-4000-8000-000000000001");
    private static final UUID HEALTH_ID = UUID.fromString("a1150001-0000-4000-8000-000000000002");
    private static final UUID ARMOR_ID = UUID.fromString("a1150001-0000-4000-8000-000000000003");

    public YouWillRiseAndSingItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> result = ImmutableMultimap.builder();
        result.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_ID,
                "You Will Rise and Sing base attack", 0.10D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        result.put(Attributes.MAX_HEALTH, new AttributeModifier(HEALTH_ID,
                "You Will Rise and Sing base health", 0.05D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        result.put(Attributes.ARMOR, new AttributeModifier(ARMOR_ID,
                "You Will Rise and Sing base armor", 0.05D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        return result.build();
    }

    public static int getSuperimposition(ItemStack stack) {
        return stack.getItem() instanceof YouWillRiseAndSingItem && stack.hasTag()
                ? Math.max(1, Math.min(5, stack.getTag().getInt(SUPERIMPOSITION_TAG))) : 1;
    }

    public static void setSuperimposition(ItemStack stack, int level) {
        if (stack.getItem() instanceof YouWillRiseAndSingItem) {
            stack.getOrCreateTag().putInt(SUPERIMPOSITION_TAG, Math.max(1, Math.min(5, level)));
        }
    }
}
