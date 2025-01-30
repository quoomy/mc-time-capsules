package com.quoomy.timecapsules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.concurrent.CompletableFuture;

public class ReceivedTimeCapsuleItem extends Item
{
    public ReceivedTimeCapsuleItem(Settings settings)
    {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand)
    {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient())
            return super.use(world, user, hand);

        int id = stack.getOrDefault(ModRegistrations.RECEIVED_TIME_CAPSULE_ID_COMPONENT, -1);

        TimeCapsuleData data;
        if (id < 0)
            data = new TimeCapsuleData();
        else
            data = new TimeCapsuleData(id);

        if (!data.isValid())
        {
            user.sendMessage(Text.of("Unable to decode time capsule right now. Please try again later with a stable connection."), false);
            return ActionResult.FAIL;
        }
        stack.set(ModRegistrations.RECEIVED_TIME_CAPSULE_ID_COMPONENT, data.getId());
        stack.set(ModRegistrations.RECEIVED_TIME_CAPSULE_FETCH_ATTEMPTED_COMPONENT, true);
        MinecraftClient.getInstance().setScreen(new ReceivedTimeCapsuleScreen(data));

        return ActionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected)
    {
        if (!world.isClient)
        {
            super.inventoryTick(stack, world, entity, slot, selected);
            return;
        }

        boolean fetched = stack.getOrDefault(ModRegistrations.RECEIVED_TIME_CAPSULE_FETCH_ATTEMPTED_COMPONENT, false);
        if (fetched)
            return;
        stack.set(ModRegistrations.RECEIVED_TIME_CAPSULE_FETCH_ATTEMPTED_COMPONENT, true);

        // first inv tick, attempt to fetch data
        CompletableFuture.runAsync(() -> {
            TimeCapsuleData data = new TimeCapsuleData();
            if (!data.isValid())
                return;

            stack.set(ModRegistrations.RECEIVED_TIME_CAPSULE_ID_COMPONENT, data.getId());
        });

        super.inventoryTick(stack, world, entity, slot, selected);
    }
}
