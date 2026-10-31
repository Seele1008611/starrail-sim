package com.starrail.sim;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Dawn copper tools and armor for the Path material progression. */
public final class PathDawncopperEquipment {
    private static final List<RegistryObject<Item>> ITEMS = new ArrayList<>();

    public static final RegistryObject<Item> SWORD = sword("path_dawncopper_sword");
    public static final RegistryObject<Item> PICKAXE = pickaxe("path_dawncopper_pickaxe");
    public static final RegistryObject<Item> AXE = axe("path_dawncopper_axe");
    public static final RegistryObject<Item> SHOVEL = shovel("path_dawncopper_shovel");
    public static final RegistryObject<Item> HOE = hoe("path_dawncopper_hoe");
    public static final RegistryObject<Item> HELMET = armor("path_dawncopper_helmet", ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> CHESTPLATE = armor("path_dawncopper_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> LEGGINGS = armor("path_dawncopper_leggings", ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> BOOTS = armor("path_dawncopper_boots", ArmorItem.Type.BOOTS);

    private PathDawncopperEquipment() {
    }

    /** Forces the registration fields to initialize while mod registry events are assembled. */
    public static void register() {
    }

    public static void addCreativeItems(CreativeModeTab.Output output) {
        for (RegistryObject<Item> item : ITEMS) output.accept(item.get());
    }

    private static RegistryObject<Item> sword(String id) {
        return register(id, () -> new SwordItem(ToolTier.DAWNCOPPER, 7, -2.2F, new Item.Properties()));
    }

    private static RegistryObject<Item> pickaxe(String id) {
        return register(id, () -> new PickaxeItem(ToolTier.DAWNCOPPER, 2, -2.6F, new Item.Properties()));
    }

    private static RegistryObject<Item> axe(String id) {
        return register(id, () -> new AxeItem(ToolTier.DAWNCOPPER, 6.5F, -2.9F, new Item.Properties()));
    }

    private static RegistryObject<Item> shovel(String id) {
        return register(id, () -> new ShovelItem(ToolTier.DAWNCOPPER, 2.5F, -2.8F, new Item.Properties()));
    }

    private static RegistryObject<Item> hoe(String id) {
        return register(id, () -> new HoeItem(ToolTier.DAWNCOPPER, 0, -0.8F, new Item.Properties()));
    }

    private static RegistryObject<Item> armor(String id, ArmorItem.Type type) {
        return register(id, () -> new ArmorItem(ArmorMaterialTier.DAWNCOPPER, type, new Item.Properties()));
    }

    private static RegistryObject<Item> register(String id, Supplier<? extends Item> factory) {
        RegistryObject<Item> item = StarRailSimMod.ITEMS.register(id, factory::get);
        ITEMS.add(item);
        return item;
    }

    private enum ToolTier implements Tier {
        DAWNCOPPER(1, 400, 5.5F, 1.0F, 14,
                () -> Ingredient.of(PathMineralContent.DAWNCOPPER_INGOT.get()));

        private final int level;
        private final int uses;
        private final float speed;
        private final float attackDamage;
        private final int enchantmentValue;
        private final Supplier<Ingredient> repairIngredient;

        ToolTier(int level, int uses, float speed, float attackDamage, int enchantmentValue,
                 Supplier<Ingredient> repairIngredient) {
            this.level = level;
            this.uses = uses;
            this.speed = speed;
            this.attackDamage = attackDamage;
            this.enchantmentValue = enchantmentValue;
            this.repairIngredient = repairIngredient;
        }

        @Override public int getUses() { return uses; }
        @Override public float getSpeed() { return speed; }
        @Override public float getAttackDamageBonus() { return attackDamage; }
        @Override public int getLevel() { return level; }
        @Override public int getEnchantmentValue() { return enchantmentValue; }
        @Override public Ingredient getRepairIngredient() { return repairIngredient.get(); }
    }

    private enum ArmorMaterialTier implements ArmorMaterial {
        DAWNCOPPER("dawncopper", new int[]{188, 216, 231, 159},
                new int[]{3, 6, 6, 3}, 14,
                SoundEvents.ARMOR_EQUIP_IRON,
                () -> Ingredient.of(PathMineralContent.DAWNCOPPER_INGOT.get()));

        private final String name;
        private final int[] durability;
        private final int[] defense;
        private final int enchantmentValue;
        private final SoundEvent equipSound;
        private final Supplier<Ingredient> repairIngredient;

        ArmorMaterialTier(String name, int[] durability, int[] defense, int enchantmentValue,
                          SoundEvent equipSound, Supplier<Ingredient> repairIngredient) {
            this.name = name;
            this.durability = durability;
            this.defense = defense;
            this.enchantmentValue = enchantmentValue;
            this.equipSound = equipSound;
            this.repairIngredient = repairIngredient;
        }

        @Override
        public int getDurabilityForType(ArmorItem.Type type) {
            return switch (type) {
                case BOOTS -> durability[0];
                case LEGGINGS -> durability[1];
                case CHESTPLATE -> durability[2];
                case HELMET -> durability[3];
            };
        }

        @Override
        public int getDefenseForType(ArmorItem.Type type) {
            return switch (type) {
                case BOOTS -> defense[0];
                case LEGGINGS -> defense[1];
                case CHESTPLATE -> defense[2];
                case HELMET -> defense[3];
            };
        }

        @Override public int getEnchantmentValue() { return enchantmentValue; }
        @Override public SoundEvent getEquipSound() { return equipSound; }
        @Override public Ingredient getRepairIngredient() { return repairIngredient.get(); }
        @Override public String getName() { return StarRailSimMod.MOD_ID + ":" + name; }
        @Override public float getToughness() { return 0.0F; }
        @Override public float getKnockbackResistance() { return 0.0F; }
    }
}
