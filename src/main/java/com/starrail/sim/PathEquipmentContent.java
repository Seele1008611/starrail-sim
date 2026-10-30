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
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** First two equipment tiers for the redesigned Path material progression. */
public final class PathEquipmentContent {
    private static final List<RegistryObject<Item>> ITEMS = new ArrayList<>();

    public static final RegistryObject<Item> SUNIRON_SWORD = sword("path_suniron_sword", ToolTier.SUNIRON);
    public static final RegistryObject<Item> SUNIRON_PICKAXE = pickaxe("path_suniron_pickaxe", ToolTier.SUNIRON);
    public static final RegistryObject<Item> SUNIRON_AXE = axe("path_suniron_axe", ToolTier.SUNIRON);
    public static final RegistryObject<Item> SUNIRON_SHOVEL = shovel("path_suniron_shovel", ToolTier.SUNIRON);
    public static final RegistryObject<Item> SUNIRON_HOE = hoe("path_suniron_hoe", ToolTier.SUNIRON);
    public static final RegistryObject<Item> SUNIRON_HELMET = armor("path_suniron_helmet", ArmorMaterialTier.SUNIRON, Type.HELMET);
    public static final RegistryObject<Item> SUNIRON_CHESTPLATE = armor("path_suniron_chestplate", ArmorMaterialTier.SUNIRON, Type.CHESTPLATE);
    public static final RegistryObject<Item> SUNIRON_LEGGINGS = armor("path_suniron_leggings", ArmorMaterialTier.SUNIRON, Type.LEGGINGS);
    public static final RegistryObject<Item> SUNIRON_BOOTS = armor("path_suniron_boots", ArmorMaterialTier.SUNIRON, Type.BOOTS);

    public static final RegistryObject<Item> STARMARROW_SWORD = sword("path_starmarrow_sword", ToolTier.STARMARROW);
    public static final RegistryObject<Item> STARMARROW_PICKAXE = pickaxe("path_starmarrow_pickaxe", ToolTier.STARMARROW);
    public static final RegistryObject<Item> STARMARROW_AXE = axe("path_starmarrow_axe", ToolTier.STARMARROW);
    public static final RegistryObject<Item> STARMARROW_SHOVEL = shovel("path_starmarrow_shovel", ToolTier.STARMARROW);
    public static final RegistryObject<Item> STARMARROW_HOE = hoe("path_starmarrow_hoe", ToolTier.STARMARROW);
    public static final RegistryObject<Item> STARMARROW_HELMET = armor("path_starmarrow_helmet", ArmorMaterialTier.STARMARROW, Type.HELMET);
    public static final RegistryObject<Item> STARMARROW_CHESTPLATE = armor("path_starmarrow_chestplate", ArmorMaterialTier.STARMARROW, Type.CHESTPLATE);
    public static final RegistryObject<Item> STARMARROW_LEGGINGS = armor("path_starmarrow_leggings", ArmorMaterialTier.STARMARROW, Type.LEGGINGS);
    public static final RegistryObject<Item> STARMARROW_BOOTS = armor("path_starmarrow_boots", ArmorMaterialTier.STARMARROW, Type.BOOTS);

    public static final RegistryObject<Item> STELLARJADE_SWORD = sword("path_stellarjade_sword", ToolTier.STELLARJADE);
    public static final RegistryObject<Item> STELLARJADE_PICKAXE = pickaxe("path_stellarjade_pickaxe", ToolTier.STELLARJADE);
    public static final RegistryObject<Item> STELLARJADE_AXE = axe("path_stellarjade_axe", ToolTier.STELLARJADE);
    public static final RegistryObject<Item> STELLARJADE_SHOVEL = shovel("path_stellarjade_shovel", ToolTier.STELLARJADE);
    public static final RegistryObject<Item> STELLARJADE_HOE = hoe("path_stellarjade_hoe", ToolTier.STELLARJADE);
    public static final RegistryObject<Item> STELLARJADE_HELMET = armor("path_stellarjade_helmet", ArmorMaterialTier.STELLARJADE, Type.HELMET);
    public static final RegistryObject<Item> STELLARJADE_CHESTPLATE = armor("path_stellarjade_chestplate", ArmorMaterialTier.STELLARJADE, Type.CHESTPLATE);
    public static final RegistryObject<Item> STELLARJADE_LEGGINGS = armor("path_stellarjade_leggings", ArmorMaterialTier.STELLARJADE, Type.LEGGINGS);
    public static final RegistryObject<Item> STELLARJADE_BOOTS = armor("path_stellarjade_boots", ArmorMaterialTier.STELLARJADE, Type.BOOTS);

    public static final RegistryObject<Item> ANCIENTDREAM_SWORD = sword("path_ancientdream_sword", ToolTier.ANCIENTDREAM);
    public static final RegistryObject<Item> ANCIENTDREAM_PICKAXE = pickaxe("path_ancientdream_pickaxe", ToolTier.ANCIENTDREAM);
    public static final RegistryObject<Item> ANCIENTDREAM_AXE = axe("path_ancientdream_axe", ToolTier.ANCIENTDREAM);
    public static final RegistryObject<Item> ANCIENTDREAM_SHOVEL = shovel("path_ancientdream_shovel", ToolTier.ANCIENTDREAM);
    public static final RegistryObject<Item> ANCIENTDREAM_HOE = hoe("path_ancientdream_hoe", ToolTier.ANCIENTDREAM);
    public static final RegistryObject<Item> ANCIENTDREAM_HELMET = armor("path_ancientdream_helmet", ArmorMaterialTier.ANCIENTDREAM, Type.HELMET);
    public static final RegistryObject<Item> ANCIENTDREAM_CHESTPLATE = armor("path_ancientdream_chestplate", ArmorMaterialTier.ANCIENTDREAM, Type.CHESTPLATE);
    public static final RegistryObject<Item> ANCIENTDREAM_LEGGINGS = armor("path_ancientdream_leggings", ArmorMaterialTier.ANCIENTDREAM, Type.LEGGINGS);
    public static final RegistryObject<Item> ANCIENTDREAM_BOOTS = armor("path_ancientdream_boots", ArmorMaterialTier.ANCIENTDREAM, Type.BOOTS);

    public static final RegistryObject<Item> VOID_RIFT_BLADE = register("void_rift_blade",
            () -> new SwordItem(ToolTier.VOID_RIFT, 3, -2.4F,
                    new Item.Properties().durability(ToolTier.VOID_RIFT.getUses())));

    public static final RegistryObject<Item> STELLARJADE_UPGRADE_TEMPLATE = template("path_stellarjade_upgrade_smithing_template");
    public static final RegistryObject<Item> ANCIENTDREAM_UPGRADE_TEMPLATE = template("path_ancientdream_upgrade_smithing_template");

    private PathEquipmentContent() {
    }

    static void register() {
        // Accessing this method initializes the equipment registrations before registry events fire.
    }

    public static void addCreativeItems(CreativeModeTab.Output output) {
        for (RegistryObject<Item> item : ITEMS) output.accept(item.get());
    }

    private static RegistryObject<Item> sword(String id, Tier tier) {
        return register(id, () -> new SwordItem(tier, 3, -2.4F, toolProperties(tier)));
    }

    private static RegistryObject<Item> pickaxe(String id, Tier tier) {
        return register(id, () -> new PickaxeItem(tier, 1, -2.8F, toolProperties(tier)));
    }

    private static RegistryObject<Item> axe(String id, Tier tier) {
        float damage = tier == ToolTier.SUNIRON ? 6.0F : 5.0F;
        float speed = tier == ToolTier.SUNIRON ? -3.1F : -3.0F;
        return register(id, () -> new AxeItem(tier, damage, speed, toolProperties(tier)));
    }

    private static RegistryObject<Item> shovel(String id, Tier tier) {
        return register(id, () -> new ShovelItem(tier, 1.5F, -3.0F, toolProperties(tier)));
    }

    private static RegistryObject<Item> hoe(String id, Tier tier) {
        return register(id, () -> new HoeItem(tier, -2, -1.0F, toolProperties(tier)));
    }

    private static RegistryObject<Item> armor(String id, ArmorMaterial material, Type type) {
        return register(id, () -> new ArmorItem(material, type, armorProperties(material)));
    }

    private static RegistryObject<Item> template(String id) {
        return register(id, () -> new Item(new Item.Properties().stacksTo(64)));
    }

    private static Item.Properties toolProperties(Tier tier) {
        return tier == ToolTier.STELLARJADE || tier == ToolTier.ANCIENTDREAM
                ? new Item.Properties().fireResistant() : new Item.Properties();
    }

    private static Item.Properties armorProperties(ArmorMaterial material) {
        return material == ArmorMaterialTier.STELLARJADE || material == ArmorMaterialTier.ANCIENTDREAM
                ? new Item.Properties().fireResistant() : new Item.Properties();
    }

    private static RegistryObject<Item> register(String id, Supplier<? extends Item> factory) {
        RegistryObject<Item> item = StarRailSimMod.ITEMS.register(id, factory::get);
        ITEMS.add(item);
        return item;
    }

    private enum ToolTier implements Tier {
        SUNIRON(2, 250, 6.0F, 2.0F, 14,
                () -> Ingredient.of(PathMineralContent.SUNIRON_INGOT.get())),
        STARMARROW(3, 1561, 8.0F, 3.0F, 10,
                () -> Ingredient.of(PathMineralContent.STARMARROW.get())),
        VOID_RIFT(3, 450, 8.0F, 2.5F, 10,
                () -> Ingredient.of(VoidrangerRewardContent.VOID_ESSENCE_SHARD.get())),
        STELLARJADE(4, 2031, 9.0F, 4.0F, 15,
                () -> Ingredient.of(PathMineralContent.STELLARJADE_INGOT.get())),
        ANCIENTDREAM(5, 2235, 9.5F, 4.5F, 18,
                () -> Ingredient.of(PathMineralContent.ANCIENTDREAM_INGOT.get()));

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
        SUNIRON("suniron", 15, new int[]{2, 6, 5, 2}, 14, SoundEvents.ARMOR_EQUIP_IRON,
                0.0F, 0.0F, () -> Ingredient.of(PathMineralContent.SUNIRON_INGOT.get())),
        STARMARROW("starmarrow", 33, new int[]{3, 8, 6, 3}, 10, SoundEvents.ARMOR_EQUIP_DIAMOND,
                2.0F, 0.0F, () -> Ingredient.of(PathMineralContent.STARMARROW.get())),
        STELLARJADE("stellarjade", 37, new int[]{3, 6, 8, 3}, 15, SoundEvents.ARMOR_EQUIP_NETHERITE,
                3.0F, 0.1F, () -> Ingredient.of(PathMineralContent.STELLARJADE_INGOT.get())),
        ANCIENTDREAM("ancientdream", 41, new int[]{3, 6, 8, 3}, 18, SoundEvents.ARMOR_EQUIP_NETHERITE,
                3.5F, 0.15F, () -> Ingredient.of(PathMineralContent.ANCIENTDREAM_INGOT.get()));

        private final String name;
        private final int durabilityMultiplier;
        private final int[] defense;
        private final int enchantmentValue;
        private final SoundEvent equipSound;
        private final float toughness;
        private final float knockbackResistance;
        private final Supplier<Ingredient> repairIngredient;

        ArmorMaterialTier(String name, int durabilityMultiplier, int[] defense, int enchantmentValue,
                          SoundEvent equipSound, float toughness, float knockbackResistance,
                          Supplier<Ingredient> repairIngredient) {
            this.name = name;
            this.durabilityMultiplier = durabilityMultiplier;
            this.defense = defense;
            this.enchantmentValue = enchantmentValue;
            this.equipSound = equipSound;
            this.toughness = toughness;
            this.knockbackResistance = knockbackResistance;
            this.repairIngredient = repairIngredient;
        }

        @Override
        public int getDurabilityForType(Type type) {
            return switch (type) {
                case BOOTS -> durabilityMultiplier * 13;
                case LEGGINGS -> durabilityMultiplier * 15;
                case CHESTPLATE -> durabilityMultiplier * 16;
                case HELMET -> durabilityMultiplier * 11;
            };
        }

        @Override
        public int getDefenseForType(Type type) {
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
        @Override public float getToughness() { return toughness; }
        @Override public float getKnockbackResistance() { return knockbackResistance; }
    }
}
