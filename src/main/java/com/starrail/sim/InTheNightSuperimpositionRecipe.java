package com.starrail.sim;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.level.Level;

/** Combines two identical light cones into the next superimposition. */
public final class InTheNightSuperimpositionRecipe extends CustomRecipe {
    public InTheNightSuperimpositionRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        int firstLevel = -1;
        int secondLevel = -1;
        ItemStack firstStack = ItemStack.EMPTY;
        int occupied = 0;

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (!(stack.getItem() instanceof InTheNightItem)
                    && !(stack.getItem() instanceof IWillHuntItem)
                    && !(stack.getItem() instanceof WorrisomeBlissfulItem)
                    && !(stack.getItem() instanceof SleepLikeTheDeadItem)
                    && !(stack.getItem() instanceof PureThoughtBaptismItem)
                    && !(stack.getItem() instanceof IdealBurningHellItem)
                    && !(stack.getItem() instanceof EmbarkOnSecondLifeItem)
                    && !(stack.getItem() instanceof FinaleOfALieItem)
                    && !(stack.getItem() instanceof SomethingIrreplaceableItem)
                    && !(stack.getItem() instanceof BrighterThanTheSunItem)
                    && !(stack.getItem() instanceof DanceAtSunsetItem)
                    && !(stack.getItem() instanceof TheUnreachableSideItem)
                    && !(stack.getItem() instanceof ThisBodyAsSwordItem)
                    && !(stack.getItem() instanceof NoRewardCrowningItem)
                    && !(stack.getItem() instanceof BloodFireBurningPathItem)
                    && !(stack.getItem() instanceof WhereDreamsBelongItem)
                    && !(stack.getItem() instanceof DawnBurnsJustSoItem)
                    && !(stack.getItem() instanceof WhatYouSeeIsMeItem)
                    && !(stack.getItem() instanceof GalaxyRailwayItem)
                    && !(stack.getItem() instanceof MomentOfGloryItem)
                    && !(stack.getItem() instanceof BeforeDawnItem)
                    && !(stack.getItem() instanceof PriceOfPeaceItem)
                    && !(stack.getItem() instanceof TowardsUnanswerableItem)
                    && !(stack.getItem() instanceof NinjaScrollItem)
                    && !(stack.getItem() instanceof LifeAsALightItem)
                    && !(stack.getItem() instanceof AStarIlluminatesNightSkyItem)
                    && !(stack.getItem() instanceof SparkleQuietlyShinesItem)
                    && !(stack.getItem() instanceof BattleIsntOverItem)
                    && !(stack.getItem() instanceof MemoryOfMeItem)
                    && !(stack.getItem() instanceof NightFlowingColorsItem)
                    && !(stack.getItem() instanceof GameOfCosmicWorldsItem)
                    && !(stack.getItem() instanceof ReturningToEarthItem)
                    && !(stack.getItem() instanceof IfTimeWereAFlowerItem)
                    && !(stack.getItem() instanceof AnAgeEtchedInGoldenBloodItem)
                    && !(stack.getItem() instanceof InTheNameOfTheWorldItem)
                    && !(stack.getItem() instanceof OnTheShoreInTheFlowOfTimeItem)
                    && !(stack.getItem() instanceof AThousandFoldSpringItem)
                    && !(stack.getItem() instanceof OnlyWaitItem)
                    && !(stack.getItem() instanceof RainNeverStopsItem)
                    && !(stack.getItem() instanceof LiesInTheWindItem)
                    && !(stack.getItem() instanceof ReturnToLongRoadItem)
                    && !(stack.getItem() instanceof ReforgedRemembranceItem)
                    && !(stack.getItem() instanceof OceanWhySingsItem)
                    && !(stack.getItem() instanceof DoNotForgetHerFlameItem)
                    && !(stack.getItem() instanceof NewFleshOfInfernoItem)
                    && !(stack.getItem() instanceof MomentOfVictoryItem)
                && !(stack.getItem() instanceof SheHasClosedHerEyesItem)
                    && !(stack.getItem() instanceof ThoughRiversAndMountainsItem)
                    && !(stack.getItem() instanceof FateNeverFairItem)
                    && !(stack.getItem() instanceof EchoesOfTheCoffinItem)
                    && !(stack.getItem() instanceof TimeWaitsForNoOneItem)
                    && !(stack.getItem() instanceof NightOfFrightItem)
                    && !(stack.getItem() instanceof OnlyTheScentRemainsItem)
                    && !(stack.getItem() instanceof WeaveTimeIntoGoldItem)
                    && !(stack.getItem() instanceof MakeFarewellMoreBeautifulItem)
                    && !(stack.getItem() instanceof LoveIsEternalItem)
                    && !(stack.getItem() instanceof StarlightForLongNightsItem)
                    && !(stack.getItem() instanceof WelcomeToGalacticCityItem)
                    && !(stack.getItem() instanceof MeetInTheNextFlowerSeasonItem)
                    && !(stack.getItem() instanceof WhenSheDecidesToSeeItem)
                    && !(stack.getItem() instanceof FlowerWorldMesmerizingEyesItem)
                    && !(stack.getItem() instanceof MayRainbowStayInTheSkyItem)) {
                    return false;
            }
            if (occupied == 0) {
                firstStack = stack;
            } else if (stack.getItem() != firstStack.getItem()) {
                return false;
            }
            int levelValue = getSuperimposition(stack);
            if (++occupied == 1) {
                firstLevel = levelValue;
            } else if (occupied == 2) {
                secondLevel = levelValue;
            } else {
                return false;
            }
        }

        return occupied == 2 && canAdvance(firstLevel, secondLevel);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registries) {
        ItemStack highest = ItemStack.EMPTY;
        int highestLevel = 0;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty()) {
                int stackLevel = getSuperimposition(stack);
                if (stackLevel > highestLevel) {
                    highest = stack;
                    highestLevel = stackLevel;
                }
            }
        }
        if (highest.isEmpty() || highestLevel >= 5) {
            return ItemStack.EMPTY;
        }
        ItemStack result = highest.copy();
        if (result.getItem() instanceof MakeFarewellMoreBeautifulItem) {
            MakeFarewellMoreBeautifulItem.setSuperimposition(result, highestLevel + 1);
        } else if (result.getItem() instanceof LoveIsEternalItem) {
            LoveIsEternalItem.setSuperimposition(result, highestLevel + 1);
        } else if (result.getItem() instanceof StarlightForLongNightsItem) {
            StarlightForLongNightsItem.setSuperimposition(result, highestLevel + 1);
        } else if (result.getItem() instanceof WelcomeToGalacticCityItem) {
            WelcomeToGalacticCityItem.setSuperimposition(result, highestLevel + 1);
        } else if (result.getItem() instanceof MeetInTheNextFlowerSeasonItem) {
            MeetInTheNextFlowerSeasonItem.setSuperimposition(result, highestLevel + 1);
        } else if (result.getItem() instanceof WhenSheDecidesToSeeItem) {
            WhenSheDecidesToSeeItem.setSuperimposition(result, highestLevel + 1);
        } else if (result.getItem() instanceof FlowerWorldMesmerizingEyesItem) {
            FlowerWorldMesmerizingEyesItem.setSuperimposition(result, highestLevel + 1);
        } else if (result.getItem() instanceof MayRainbowStayInTheSkyItem) {
            MayRainbowStayInTheSkyItem.setSuperimposition(result, highestLevel + 1);
        } else if (result.getItem() instanceof WeaveTimeIntoGoldItem) {
            WeaveTimeIntoGoldItem.setSuperimposition(result, highestLevel + 1);
        } else if (result.getItem() instanceof InTheNightItem) {
            InTheNightItem.setSuperimposition(result, highestLevel + 1);
        } else if (result.getItem() instanceof IWillHuntItem) {
            IWillHuntItem.setSuperimposition(result, highestLevel + 1);
        } else {
            if (result.getItem() instanceof WorrisomeBlissfulItem) {
                WorrisomeBlissfulItem.setSuperimposition(result, highestLevel + 1);
            } else if (result.getItem() instanceof SleepLikeTheDeadItem) {
                SleepLikeTheDeadItem.setSuperimposition(result, highestLevel + 1);
            } else if (result.getItem() instanceof PureThoughtBaptismItem) {
                PureThoughtBaptismItem.setSuperimposition(result, highestLevel + 1);
            } else if (result.getItem() instanceof IdealBurningHellItem) {
                IdealBurningHellItem.setSuperimposition(result, highestLevel + 1);
            } else if (result.getItem() instanceof EmbarkOnSecondLifeItem) {
                EmbarkOnSecondLifeItem.setSuperimposition(result, highestLevel + 1);
            } else if (result.getItem() instanceof FinaleOfALieItem) {
                FinaleOfALieItem.setSuperimposition(result, highestLevel + 1);
            } else if (result.getItem() instanceof SomethingIrreplaceableItem) {
                SomethingIrreplaceableItem.setSuperimposition(result, highestLevel + 1);
            } else {
                if (result.getItem() instanceof BrighterThanTheSunItem) {
                    BrighterThanTheSunItem.setSuperimposition(result, highestLevel + 1);
                } else if (result.getItem() instanceof DanceAtSunsetItem) {
                    DanceAtSunsetItem.setSuperimposition(result, highestLevel + 1);
                } else if (result.getItem() instanceof TheUnreachableSideItem) {
                    TheUnreachableSideItem.setSuperimposition(result, highestLevel + 1);
                } else if (result.getItem() instanceof ThisBodyAsSwordItem) {
                    ThisBodyAsSwordItem.setSuperimposition(result, highestLevel + 1);
                } else if (result.getItem() instanceof NoRewardCrowningItem) {
                    NoRewardCrowningItem.setSuperimposition(result, highestLevel + 1);
                } else if (result.getItem() instanceof BloodFireBurningPathItem) {
                    BloodFireBurningPathItem.setSuperimposition(result, highestLevel + 1);
                } else if (result.getItem() instanceof WhereDreamsBelongItem) {
                    WhereDreamsBelongItem.setSuperimposition(result, highestLevel + 1);
                } else if (result.getItem() instanceof DawnBurnsJustSoItem) {
                    DawnBurnsJustSoItem.setSuperimposition(result, highestLevel + 1);
                } else {
                    if (result.getItem() instanceof WhatYouSeeIsMeItem) {
                        WhatYouSeeIsMeItem.setSuperimposition(result, highestLevel + 1);
                    } else {
                        if (result.getItem() instanceof GalaxyRailwayItem) {
                            GalaxyRailwayItem.setSuperimposition(result, highestLevel + 1);
                        } else {
                            if (result.getItem() instanceof MomentOfGloryItem) {
                                MomentOfGloryItem.setSuperimposition(result, highestLevel + 1);
                            } else if (result.getItem() instanceof BeforeDawnItem) {
                                BeforeDawnItem.setSuperimposition(result, highestLevel + 1);
                            } else if (result.getItem() instanceof PriceOfPeaceItem) {
                                PriceOfPeaceItem.setSuperimposition(result, highestLevel + 1);
                            } else {
                                if (result.getItem() instanceof TowardsUnanswerableItem) {
                                    TowardsUnanswerableItem.setSuperimposition(result,
                                            highestLevel + 1);
                                } else {
                                    if (result.getItem() instanceof NinjaScrollItem) {
                                        NinjaScrollItem.setSuperimposition(result,
                                                highestLevel + 1);
                                    } else if (result.getItem() instanceof LifeAsALightItem) {
                                        LifeAsALightItem.setSuperimposition(result,
                                                highestLevel + 1);
                                    } else if (result.getItem()
                                            instanceof AStarIlluminatesNightSkyItem) {
                                        AStarIlluminatesNightSkyItem.setSuperimposition(result,
                                                highestLevel + 1);
                                    } else if (result.getItem()
                                            instanceof SparkleQuietlyShinesItem) {
                                        SparkleQuietlyShinesItem.setSuperimposition(result,
                                                highestLevel + 1);
                                    } else {
                                        if (result.getItem() instanceof BattleIsntOverItem) {
                                            BattleIsntOverItem.setSuperimposition(result,
                                                    highestLevel + 1);
                                        } else {
                                            if (result.getItem() instanceof MemoryOfMeItem) {
                                                MemoryOfMeItem.setSuperimposition(result,
                                                        highestLevel + 1);
                                            } else if (result.getItem()
                                                    instanceof NightFlowingColorsItem) {
                                                NightFlowingColorsItem.setSuperimposition(result,
                                                        highestLevel + 1);
                                            } else if (result.getItem()
                                                    instanceof GameOfCosmicWorldsItem) {
                                                GameOfCosmicWorldsItem.setSuperimposition(result,
                                                        highestLevel + 1);
                                            } else if (result.getItem()
                                                    instanceof ReturningToEarthItem) {
                                                ReturningToEarthItem.setSuperimposition(result,
                                                        highestLevel + 1);
                                            } else if (result.getItem()
                                                    instanceof IfTimeWereAFlowerItem) {
                                                IfTimeWereAFlowerItem.setSuperimposition(result,
                                                        highestLevel + 1);
                                            } else if (result.getItem()
                                                    instanceof InTheNameOfTheWorldItem) {
                                                InTheNameOfTheWorldItem.setSuperimposition(result,
                                                        highestLevel + 1);
                                            } else if (result.getItem()
                                                    instanceof OnTheShoreInTheFlowOfTimeItem) {
                                                OnTheShoreInTheFlowOfTimeItem
                                                        .setSuperimposition(result,
                                                                highestLevel + 1);
                                            } else if (result.getItem()
                                                    instanceof AThousandFoldSpringItem) {
                                                AThousandFoldSpringItem.setSuperimposition(
                                                        result, highestLevel + 1);
                                            } else if (result.getItem()
                                                    instanceof OnlyWaitItem) {
                                                OnlyWaitItem.setSuperimposition(result,
                                                        highestLevel + 1);
                                            } else if (result.getItem()
                                                    instanceof RainNeverStopsItem) {
                                                RainNeverStopsItem.setSuperimposition(result,
                                                        highestLevel + 1);
                                            } else if (result.getItem()
                                                    instanceof LiesInTheWindItem) {
                                                LiesInTheWindItem.setSuperimposition(result,
                                                        highestLevel + 1);
                                            } else if (result.getItem()
                                                    instanceof ReturnToLongRoadItem) {
                                                ReturnToLongRoadItem.setSuperimposition(result,
                                                        highestLevel + 1);
                                                } else if (result.getItem()
                                                        instanceof ReforgedRemembranceItem) {
                                                    ReforgedRemembranceItem.setSuperimposition(result,
                                                            highestLevel + 1);
                                                } else if (result.getItem()
                                                        instanceof OceanWhySingsItem) {
                                                    OceanWhySingsItem.setSuperimposition(result,
                                                            highestLevel + 1);
                                                } else if (result.getItem()
                                                        instanceof DoNotForgetHerFlameItem) {
                                                    DoNotForgetHerFlameItem.setSuperimposition(
                                                            result, highestLevel + 1);
                                                } else if (result.getItem()
                                                        instanceof NewFleshOfInfernoItem) {
                                                    NewFleshOfInfernoItem.setSuperimposition(
                                                            result, highestLevel + 1);
                                                } else if (result.getItem()
                                                        instanceof MomentOfVictoryItem) {
                                                    MomentOfVictoryItem.setSuperimposition(
                                                            result, highestLevel + 1);
                                                } else if (result.getItem()
                                                        instanceof SheHasClosedHerEyesItem) {
                                                    SheHasClosedHerEyesItem.setSuperimposition(
                                                            result, highestLevel + 1);
                                                } else if (result.getItem()
                                                        instanceof ThoughRiversAndMountainsItem) {
                                                    ThoughRiversAndMountainsItem.setSuperimposition(
                                                            result, highestLevel + 1);
                                                } else if (result.getItem()
                                                        instanceof FateNeverFairItem) {
                                                    FateNeverFairItem.setSuperimposition(
                                                            result, highestLevel + 1);
                                                } else if (result.getItem()
                                                        instanceof EchoesOfTheCoffinItem) {
                                                    EchoesOfTheCoffinItem.setSuperimposition(
                                                            result, highestLevel + 1);
                                                } else if (result.getItem()
                                                        instanceof TimeWaitsForNoOneItem) {
                                                    TimeWaitsForNoOneItem.setSuperimposition(
                                                            result, highestLevel + 1);
                                                } else if (result.getItem()
                                                        instanceof NightOfFrightItem) {
                                                    NightOfFrightItem.setSuperimposition(
                                                            result, highestLevel + 1);
                                                } else if (result.getItem()
                                                        instanceof OnlyTheScentRemainsItem) {
                                                    OnlyTheScentRemainsItem.setSuperimposition(
                                                            result, highestLevel + 1);
                                                } else if (result.getItem()
                                                        instanceof AnAgeEtchedInGoldenBloodItem) {
                                                    AnAgeEtchedInGoldenBloodItem
                                                            .setSuperimposition(result,
                                                                    highestLevel + 1);
                                                }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        result.setCount(1);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return StarRailSimMod.IN_THE_NIGHT_SUPERIMPOSITION_SERIALIZER.get();
    }

    private static boolean canAdvance(int firstLevel, int secondLevel) {
        if (firstLevel < 1 || secondLevel < 1 || Math.max(firstLevel, secondLevel) >= 5) {
            return false;
        }
        int lower = Math.min(firstLevel, secondLevel);
        return lower == 1;
    }

    private static int getSuperimposition(ItemStack stack) {
        if (stack.getItem() instanceof BattleIsntOverItem) {
            return BattleIsntOverItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof MemoryOfMeItem) {
            return MemoryOfMeItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof NightFlowingColorsItem) {
            return NightFlowingColorsItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof GameOfCosmicWorldsItem) {
            return GameOfCosmicWorldsItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof ReturningToEarthItem) {
            return ReturningToEarthItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof IfTimeWereAFlowerItem) {
            return IfTimeWereAFlowerItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof AnAgeEtchedInGoldenBloodItem) {
            return AnAgeEtchedInGoldenBloodItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof WeaveTimeIntoGoldItem) {
            return WeaveTimeIntoGoldItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof MakeFarewellMoreBeautifulItem) {
            return MakeFarewellMoreBeautifulItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof LoveIsEternalItem) {
            return LoveIsEternalItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof StarlightForLongNightsItem) {
            return StarlightForLongNightsItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof WelcomeToGalacticCityItem) {
            return WelcomeToGalacticCityItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof MeetInTheNextFlowerSeasonItem) {
            return MeetInTheNextFlowerSeasonItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof WhenSheDecidesToSeeItem) {
            return WhenSheDecidesToSeeItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof FlowerWorldMesmerizingEyesItem) {
            return FlowerWorldMesmerizingEyesItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof MayRainbowStayInTheSkyItem) {
            return MayRainbowStayInTheSkyItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof InTheNameOfTheWorldItem) {
            return InTheNameOfTheWorldItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof OnTheShoreInTheFlowOfTimeItem) {
            return OnTheShoreInTheFlowOfTimeItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof AThousandFoldSpringItem) {
            return AThousandFoldSpringItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof OnlyWaitItem) {
            return OnlyWaitItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof RainNeverStopsItem) {
            return RainNeverStopsItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof LiesInTheWindItem) {
            return LiesInTheWindItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof ReturnToLongRoadItem) {
            return ReturnToLongRoadItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof ReforgedRemembranceItem) {
            return ReforgedRemembranceItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof OceanWhySingsItem) {
            return OceanWhySingsItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof DoNotForgetHerFlameItem) {
            return DoNotForgetHerFlameItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof NewFleshOfInfernoItem) {
            return NewFleshOfInfernoItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof MomentOfVictoryItem) {
            return MomentOfVictoryItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof SheHasClosedHerEyesItem) {
            return SheHasClosedHerEyesItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof ThoughRiversAndMountainsItem) {
            return ThoughRiversAndMountainsItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof FateNeverFairItem) {
            return FateNeverFairItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof EchoesOfTheCoffinItem) {
            return EchoesOfTheCoffinItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof TimeWaitsForNoOneItem) {
            return TimeWaitsForNoOneItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof NightOfFrightItem) {
            return NightOfFrightItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof OnlyTheScentRemainsItem) {
            return OnlyTheScentRemainsItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof SparkleQuietlyShinesItem) {
            return SparkleQuietlyShinesItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof InTheNightItem) {
            return InTheNightItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof IWillHuntItem) {
            return IWillHuntItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof WorrisomeBlissfulItem) {
            return WorrisomeBlissfulItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof SleepLikeTheDeadItem) {
            return SleepLikeTheDeadItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof PureThoughtBaptismItem) {
            return PureThoughtBaptismItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof IdealBurningHellItem) {
            return IdealBurningHellItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof EmbarkOnSecondLifeItem) {
            return EmbarkOnSecondLifeItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof FinaleOfALieItem) {
            return FinaleOfALieItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof SomethingIrreplaceableItem) {
            return SomethingIrreplaceableItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof BrighterThanTheSunItem) {
            return BrighterThanTheSunItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof DanceAtSunsetItem) {
            return DanceAtSunsetItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof TheUnreachableSideItem) {
            return TheUnreachableSideItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof ThisBodyAsSwordItem) {
            return ThisBodyAsSwordItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof NoRewardCrowningItem) {
            return NoRewardCrowningItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof BloodFireBurningPathItem) {
            return BloodFireBurningPathItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof WhereDreamsBelongItem) {
            return WhereDreamsBelongItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof DawnBurnsJustSoItem) {
            return DawnBurnsJustSoItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof WhatYouSeeIsMeItem) {
            return WhatYouSeeIsMeItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof GalaxyRailwayItem) {
            return GalaxyRailwayItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof TowardsUnanswerableItem) {
            return TowardsUnanswerableItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof LifeAsALightItem) {
            return LifeAsALightItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof PriceOfPeaceItem) {
            return PriceOfPeaceItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof AStarIlluminatesNightSkyItem) {
            return AStarIlluminatesNightSkyItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof NinjaScrollItem) {
            return NinjaScrollItem.getSuperimposition(stack);
        }
        if (stack.getItem() instanceof MomentOfGloryItem) {
            return MomentOfGloryItem.getSuperimposition(stack);
        }
        return BeforeDawnItem.getSuperimposition(stack);
    }
}
