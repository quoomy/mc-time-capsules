package com.quoomy.timecapsules.item.timecapsulepainting;

import com.quoomy.timecapsules.ModRegistrations;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/*

    block aspect ratios with least cutoff between 1 and 5 would be:
    5:3
    2:1
    4:2
    3:2

    i think ill pick 3:2 because then the crop will be on the sides, hotbar and center will still be visible

 */

public class TimeCapsulePaintingEntity extends AbstractDecorationEntity
{
    protected int id;

    public TimeCapsulePaintingEntity(World world, BlockPos blockPos, int id)
    {
        super(ModRegistrations.TIME_CAPSULE_PAINTING_ENTITY_ENTITY_TYPE, world, blockPos);
        this.id = id;
    }
    public TimeCapsulePaintingEntity(EntityType<TimeCapsulePaintingEntity> timeCapsulePaintingEntityEntityType, World world)
    {
        super(timeCapsulePaintingEntityEntityType, world);
        this.id = -1;
    }

    @Override
    protected Box calculateBoundingBox(BlockPos pos, Direction side)
    {
        // 3:2 aspect ratio is best because
        // - not too large
        // - close to 16:9 aspect ratio
        // - crop will be on the left and right side (less important stuff)
        return new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 3, pos.getY() + 2, pos.getZ());
    }

    @Override
    public void onPlace()
    {
        this.playSound(SoundEvents.ENTITY_PAINTING_PLACE, 1.0F, 1.0F);
    }
    @Override
    public void onBreak(ServerWorld world, @Nullable Entity breaker)
    {
        if (world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS))
        {
            this.playSound(SoundEvents.ENTITY_PAINTING_BREAK, 1.0F, 1.0F);
            if (breaker instanceof PlayerEntity pntt && pntt.isInCreativeMode())
                return;
            ItemStack stack = new ItemStack(ModRegistrations.TIME_CAPSULE_PAINTING_ITEM);
            stack.set(ModRegistrations.TIME_CAPSULE_ID_COMPONENT, this.id);
            this.dropStack(world, stack);
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {}

    public static Optional<TimeCapsulePaintingEntity> placePainting(World world, BlockPos blockPos, Direction facing, int id)
    {
        TimeCapsulePaintingEntity entity = new TimeCapsulePaintingEntity(world, blockPos, id);
        entity.setFacing(facing);
        if (world.canPlace(world.getBlockState(blockPos), blockPos, ShapeContext.of(entity)))
        {
            world.spawnEntity(entity);
            entity.onPlace();
            return Optional.of(entity);
        }
        else
        {
            entity.remove(RemovalReason.DISCARDED);
            return Optional.empty();
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt)
    {
        nbt.putByte("id", (byte)this.id);
        super.writeCustomDataToNbt(nbt);
    }
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt)
    {
        this.id = nbt.getByte("id");
        super.readCustomDataFromNbt(nbt);
    }
}
