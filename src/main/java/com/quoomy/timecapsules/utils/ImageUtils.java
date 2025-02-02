package com.quoomy.timecapsules.utils;

import net.minecraft.client.texture.NativeImage;

import java.awt.image.BufferedImage;

public class ImageUtils
{
    public static NativeImage convertToNativeImage(BufferedImage bufferedImage)
    {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, width, height, false);

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                int argb = bufferedImage.getRGB(x, y);

                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                int rgba = ((a & 0xFF) << 24) |
                        ((r & 0xFF) << 16) |
                        ((g & 0xFF) << 8)  |
                        (b & 0xFF);

                nativeImage.setColorArgb(x, y, rgba);
            }
        }
        return nativeImage;
    }
}
