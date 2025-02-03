package com.quoomy.timecapsules.item.timecapsulepainting;

import com.quoomy.timecapsules.ModRegistrations;
import com.quoomy.timecapsules.Timecapsules;
import com.quoomy.timecapsules.item.timecapsule.TimeCapsuleItem;
import com.quoomy.timecapsules.mixin.DecorationItemAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.item.DecorationItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class TimeCapsulePaintingRecipe implements CraftingRecipe
{
    public TimeCapsulePaintingRecipe(CraftingRecipeCategory category)
    {
        super();
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world)
    {
        int timeCapsuleCount = 0;
        int paintingCount = 0;

        for (int i = 0; i < input.getWidth(); i++)
        {
            for (int j = 0; j < input.getHeight(); j++)
            {
                ItemStack stack = input.getStackInSlot(i, j);
                if (stack.getItem() instanceof TimeCapsuleItem)
                {
                    int id = stack.getOrDefault(ModRegistrations.TIME_CAPSULE_ID_COMPONENT, -1);
                    timeCapsuleCount += id >= 0 ? 1 : 0;
                }
                else if (stack.getItem() instanceof DecorationItem)
                {
                    DecorationItem decorationItem = (DecorationItem) stack.getItem();
                    EntityType<?> entityType = ((DecorationItemAccessor) decorationItem).getEntityType();
                    paintingCount += entityType == EntityType.PAINTING ? 1 : 0;
                }
            }
        }

        Timecapsules.LOGGER.info("TimeCapsulePaintingRecipe: timeCapsuleCount = " + timeCapsuleCount + ", paintingCount = " + paintingCount);

        return timeCapsuleCount == 1 && paintingCount == 1;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries)
    {
        ItemStack timeCapsuleStack = ItemStack.EMPTY;
        for (int i = 0; i < input.getWidth(); i++)
        {
            for (int j = 0; j < input.getHeight(); j++)
            {
                ItemStack stack = input.getStackInSlot(i, j);
                if (stack.getItem() instanceof TimeCapsuleItem)
                {
                    timeCapsuleStack = stack;
                    break;
                }
            }
            if (!timeCapsuleStack.isEmpty())
                break;
        }

        if (timeCapsuleStack.isEmpty())
            return ItemStack.EMPTY; // no id, no result. cant convert from a sending capsule

        int id = timeCapsuleStack.getOrDefault(ModRegistrations.TIME_CAPSULE_ID_COMPONENT, -1);
        ItemStack result = new ItemStack(ModRegistrations.TIME_CAPSULE_PAINTING_ITEM);
        result.set(ModRegistrations.TIME_CAPSULE_ID_COMPONENT, id);

        return result;
    }

    @Override
    public DefaultedList<ItemStack> getRecipeRemainders(CraftingRecipeInput input)
    {
        DefaultedList<ItemStack> remaining = DefaultedList.ofSize(input.getWidth() * input.getHeight(), ItemStack.EMPTY);

        for (int i = 0; i < input.getWidth(); i++)
        {
            for (int j = 0; j < input.getHeight(); j++)
            {
                ItemStack stack = input.getStackInSlot(i, j);
                if (stack.getItem() instanceof TimeCapsuleItem)
                {
                    remaining.set(i + j * input.getWidth(), stack.copy());
                }
            }
        }

        return remaining;
    }

    @Override
    public RecipeSerializer<? extends CraftingRecipe> getSerializer()
    {
        return ModRegistrations.TIME_CAPSULE_PAINTING_RECIPE_SERIALIZER;
    }

    @Override
    public IngredientPlacement getIngredientPlacement()
    {
        return null;
    }

    @Override
    public CraftingRecipeCategory getCategory()
    {
        return CraftingRecipeCategory.MISC;
    }
}
