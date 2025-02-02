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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.CompletableFuture;

public class TimeCapsuleItem extends Item
{
    public static final String TO_UPLOAD_ID = "to_upload_id";

    public TimeCapsuleItem(Settings settings)
    {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand)
    {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient())
            return super.use(world, user, hand);

        boolean isSendingCapsule = stack.getOrDefault(ModRegistrations.TIME_CAPSULE_IS_SENDING_COMPONENT, false);

        if (isSendingCapsule)
        {
            MinecraftClient.getInstance().setScreen(new SendingTimeCapsuleScreen(stack));

            CompletableFuture.runAsync(() -> {
                TimeCapsuleData data = new TimeCapsuleData(TO_UPLOAD_ID);
                if (!data.isValid())
                {
                    Timecapsules.LOGGER.info("Failed to send time capsule. Removing from to_upload folder.");
                    if (user instanceof PlayerEntity player)
                        player.sendMessage(Text.of("Failed to send time capsule. Please try again later with a stable connection."), false);
                    removeToUploadFolder();
                    return;
                }

                String sendingReturn = data.sendCapsule();
                removeToUploadFolder();

                if (sendingReturn.isEmpty())
                {
                    if (user instanceof PlayerEntity player)
                        player.sendMessage(Text.of("Time capsule sent successfully!"), false);
                    stack.decrement(1);
                }
                else
                {
                    if (user instanceof PlayerEntity player)
                        player.sendMessage(Text.of("Failed to send time capsule. Please try again later with a stable connection and verify all data is correct."), false);
                }
            });
        }
        else
        {
            int id = stack.getOrDefault(ModRegistrations.TIME_CAPSULE_ID_COMPONENT, -1);

            TimeCapsuleData data;
            if (id < 0)
                data = new TimeCapsuleData();
            else
                data = new TimeCapsuleData(String.valueOf(id));

            if (!data.isValid())
            {
                user.sendMessage(Text.of("Unable to decode time capsule right now. Please try again later with a stable connection."), false);
                return ActionResult.FAIL;
            }
            stack.set(ModRegistrations.TIME_CAPSULE_ID_COMPONENT, data.getId());
            stack.set(ModRegistrations.TIME_CAPSULE_FETCH_ATTEMPTED_COMPONENT, true);
            MinecraftClient.getInstance().setScreen(new ReceivedTimeCapsuleScreen(data));

        }

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

        boolean isSendingCapsule = stack.getOrDefault(ModRegistrations.TIME_CAPSULE_IS_SENDING_COMPONENT, false);

        if (!isSendingCapsule)
        {
            boolean fetched = stack.getOrDefault(ModRegistrations.TIME_CAPSULE_FETCH_ATTEMPTED_COMPONENT, false);
            if (fetched)
                return;
            stack.set(ModRegistrations.TIME_CAPSULE_FETCH_ATTEMPTED_COMPONENT, true);

            // first inv tick, attempt to fetch data
            CompletableFuture.runAsync(() -> {
                TimeCapsuleData data = new TimeCapsuleData();
                if (!data.isValid())
                    return;

                stack.set(ModRegistrations.TIME_CAPSULE_ID_COMPONENT, data.getId());
            });
        }

        super.inventoryTick(stack, world, entity, slot, selected);
    }

    private void removeToUploadFolder()
    {
        CompletableFuture.runAsync(() -> {
            Path folderPath = MinecraftClient.getInstance().runDirectory
                    .toPath()
                    .resolve("timecapsules")
                    .resolve(TimeCapsuleItem.TO_UPLOAD_ID);

            try
            {
                if (Files.exists(folderPath))
                {
                    Files.walkFileTree(folderPath, new SimpleFileVisitor<Path>()
                    {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                        {
                            // Remove each file 🍆🤏
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
                        {
                            // Then delete directory once empty 🖕
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

}
