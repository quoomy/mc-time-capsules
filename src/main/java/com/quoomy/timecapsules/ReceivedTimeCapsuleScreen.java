package com.quoomy.timecapsules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.util.UUID;

public class ReceivedTimeCapsuleScreen extends Screen implements Closeable
{
    private final TimeCapsuleData data;

    private NativeImageBackedTexture capsuleTexture;
    private Identifier capsuleTextureId;

    public ReceivedTimeCapsuleScreen(TimeCapsuleData data)
    {
        super(Text.of("Time Capsule"));

        this.data = data;
    }

    @Override
    protected void init()
    {
        super.init();

        BufferedImage image = data.getImage();
        if (image != null)
        {
            try
            {
                NativeImage nativeImage = convertToNativeImage(image);
                this.capsuleTexture = new NativeImageBackedTexture(nativeImage);
                this.capsuleTextureId = Identifier.of(Timecapsules.MOD_ID, "capsule" + UUID.randomUUID());
                MinecraftClient.getInstance().getTextureManager().registerTexture(capsuleTextureId, capsuleTexture);
            }
            catch (Exception e)
            {
                Timecapsules.LOGGER.error("Failed to load image for time capsule", e);
            }
        }
        else
        {
            Timecapsules.LOGGER.error("Failed to load image for time capsule");
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);

        int startY = 20;
        int leftX = this.width / 2 - 100;

        context.drawTextWithShadow(textRenderer, "Time Capsule ID: " + data.getId(), leftX, startY, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, "Name/Signature: " + data.getUserNameOrSignature(), leftX, startY + 10, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, "Game Version: " + data.getGameVersion(), leftX, startY + 20, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, "Modloader: " + data.getModloader(), leftX, startY + 30, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, "Text: " + data.getText(), leftX, startY + 40, 0xFFFFFF);

        if (capsuleTextureId != null)
        {
            int imgWidth = data.getImage().getWidth();
            int imgHeight = data.getImage().getHeight();

            int imageX = this.width / 2 - (imgWidth / 2);
            int imageY = startY + 60;

            // RenderSystem.setShaderTexture(0, capsuleTextureId);
            context.drawTexture(RenderLayer::getGuiTexturedOverlay, capsuleTextureId, imageX, imageY, 0, 0, imgWidth, imgHeight, imgWidth, imgHeight);
        }
    }

    private NativeImage convertToNativeImage(BufferedImage bufferedImage)
    {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, width, height, false);

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                int argb = bufferedImage.getRGB(x, y);

                int a = 255;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8)  & 0xFF;
                int b = (argb)       & 0xFF;

                int rgba = ((a & 0xFF) << 24) |
                        ((r & 0xFF) << 16) |
                        ((g & 0xFF) << 8)  |
                        (b & 0xFF);

                nativeImage.setColorArgb(x, y, rgba);
            }
        }
        return nativeImage;
    }

    @Override
    public void close()
    {
        if (capsuleTexture != null)
        {
            try
            {
                capsuleTexture.close();
            }
            catch (Exception e)
            {
                Timecapsules.LOGGER.error("Failed to close texture", e);
            }
        }
        super.close();
    }
}
