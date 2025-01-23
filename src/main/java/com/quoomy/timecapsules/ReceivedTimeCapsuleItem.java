package com.quoomy.timecapsules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

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

        TimeCapsuleData data = TimeCapsuleData.fetchTimeCapsuleData();
        if (!data.isValid())
        {
            user.sendMessage(Text.of("Unable to decode time capsule right now. Please try again later with a stable connection."), false);
            return ActionResult.FAIL;
        }
        stack.set(ModRegistrations.RECEIVED_TIME_CAPSULE_ID_COMPONENT, data.getId());
        MinecraftClient.getInstance().setScreen(new ReceivedTimeCapsuleScreen(data));
        // user.sendMessage(Text.of("Time Capsule received: " + data.getText()), false);

        return ActionResult.SUCCESS;
    }
}