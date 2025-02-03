package com.quoomy.timecapsules.item.timecapsulepainting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;

public class TimeCapsulePaintingRecipeSerializer implements RecipeSerializer<TimeCapsulePaintingRecipe>
{
    @Override
    public MapCodec<TimeCapsulePaintingRecipe> codec()
    {
        return MapCodec.unit(new TimeCapsulePaintingRecipe(CraftingRecipeCategory.MISC));
    }

    @Override
    public PacketCodec<RegistryByteBuf, TimeCapsulePaintingRecipe> packetCodec()
    {
        return PacketCodec.unit(new TimeCapsulePaintingRecipe(CraftingRecipeCategory.MISC));
    }
}
