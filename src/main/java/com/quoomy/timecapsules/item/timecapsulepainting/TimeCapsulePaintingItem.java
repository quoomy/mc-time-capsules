package com.quoomy.timecapsules.item.timecapsulepainting;

import com.quoomy.timecapsules.ModRegistrations;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

public class TimeCapsulePaintingItem extends Item
{
    public TimeCapsulePaintingItem(Settings settings)
    {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context)
    {
        BlockPos blockPos = context.getBlockPos();
        Direction direction = context.getSide();
        BlockPos blockPos2 = blockPos.offset(direction);
        PlayerEntity playerEntity = context.getPlayer();
        ItemStack itemStack = context.getStack();

        if (playerEntity != null && !this.canPlaceOn(playerEntity, direction, itemStack, blockPos2))
            return ActionResult.FAIL;

        World world = context.getWorld();
        int id = itemStack.getOrDefault(ModRegistrations.TIME_CAPSULE_ID_COMPONENT, -1);
        Optional<TimeCapsulePaintingEntity> optional = TimeCapsulePaintingEntity.placePainting(world, blockPos2, direction, id);
        if (optional.isEmpty())
            return ActionResult.CONSUME;

        itemStack.decrement(1);

        return ActionResult.SUCCESS;
    }

    protected boolean canPlaceOn(PlayerEntity player, Direction side, ItemStack stack, BlockPos pos)
    {
        return !side.getAxis().isVertical() && player.canPlaceOn(pos, side, stack);
    }
}
