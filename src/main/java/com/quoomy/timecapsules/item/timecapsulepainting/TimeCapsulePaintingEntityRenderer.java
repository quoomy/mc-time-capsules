package com.quoomy.timecapsules.item.timecapsulepainting;

import com.quoomy.timecapsules.Timecapsules;
import com.quoomy.timecapsules.utils.ImageUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TimeCapsulePaintingEntityRenderer extends EntityRenderer<TimeCapsulePaintingEntity, TimeCapsulePaintingEntityRenderState>
{
    private static final Identifier MISSING_TEXTURE = Identifier.of(Timecapsules.MOD_ID, "textures/painting/unknown.png");
    private static final Map<Integer, CapsuleTexture> textureCache = new HashMap<>();
    private static final double DESIRED_ASPECT = 3.0 / 2.0;

    public TimeCapsulePaintingEntityRenderer(EntityRendererFactory.Context context)
    {
        super(context);
    }

    @Override
    public TimeCapsulePaintingEntityRenderState createRenderState()
    {
        return new TimeCapsulePaintingEntityRenderState();
    }
    @Override
    public void updateRenderState(TimeCapsulePaintingEntity entity, TimeCapsulePaintingEntityRenderState state, float tickDelta)
    {
        state.id = entity.id;
        state.facing = entity.getHorizontalFacing();
    }

    @Override
    public void render(TimeCapsulePaintingEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
    {
        matrices.push();
        matrices.translate(0.5, 0.5, 0.0);

        int capsuleId = state.id;
        CapsuleTexture capsuleTexture = getCapsuleTexture(capsuleId);
        if (capsuleTexture == null)
            capsuleTexture = new CapsuleTexture(MISSING_TEXTURE, 48, 32);
        int texWidth = capsuleTexture.width;
        int texHeight = capsuleTexture.height;

        float u0 = 0.0f, v0 = 0.0f, u1 = 1.0f, v1 = 1.0f;
        double actualAspect = (double) capsuleTexture.width / (double) capsuleTexture.height;
        if (actualAspect > DESIRED_ASPECT)
        {
            // image is too wide: crop left and right.
            double desiredWidth = DESIRED_ASPECT * texHeight;
            double crop = (texWidth - desiredWidth) / 2.0;
            u0 = (float)(crop / texWidth);
            u1 = (float)((crop + desiredWidth) / texWidth);
        }
        else if (actualAspect < DESIRED_ASPECT)
        {
            // image is too tall: crop top and bottom.
            double desiredHeight = texWidth / DESIRED_ASPECT;
            double crop = (texHeight - desiredHeight) / 2.0;
            v0 = (float)(crop / texHeight);
            v1 = (float)((crop + desiredHeight) / texHeight);
        }

        float paintingWidth = 3.0f;
        float paintingHeight = 2.0f;
        float halfWidth = paintingWidth / 2.0f;
        float halfHeight = paintingHeight / 2.0f;

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntitySolid(capsuleTexture.id));
        vertexConsumer.vertex(matrices.peek().getPositionMatrix(), -halfWidth, -halfHeight, 0.0f)
                .color(255, 255, 255, 255)
                .texture(u0, v1)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(matrices.peek(), 0.0f, 0.0f, -1.0f);
        vertexConsumer.vertex(matrices.peek().getPositionMatrix(), halfWidth, -halfHeight, 0.0f)
                .color(255, 255, 255, 255)
                .texture(u1, v1)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(matrices.peek(), 0.0f, 0.0f, -1.0f);
        vertexConsumer.vertex(matrices.peek().getPositionMatrix(), halfWidth, halfHeight, 0.0f)
                .color(255, 255, 255, 255)
                .texture(u1, v0)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(matrices.peek(), 0.0f, 0.0f, -1.0f);
        vertexConsumer.vertex(matrices.peek().getPositionMatrix(), -halfWidth, halfHeight, 0.0f)
                .color(255, 255, 255, 255)
                .texture(u0, v0)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(matrices.peek(), 0.0f, 0.0f, -1.0f);

        matrices.pop();

        super.render(state, matrices, vertexConsumers, light);
    }
    private CapsuleTexture getCapsuleTexture(int capsuleId)
    {
        if (textureCache.containsKey(capsuleId))
            return textureCache.get(capsuleId);

        try
        {
            File gameDir = MinecraftClient.getInstance().runDirectory;
            File imageFile = new File(gameDir, "timecapsules" + File.separator + capsuleId + File.separator + "image.png");
            if (!imageFile.exists())
            {
                Timecapsules.LOGGER.warn("Capsule image file not found for ID {}. Using missing texture.", capsuleId);
                return null;
            }

            BufferedImage img = ImageIO.read(imageFile);
            NativeImage nativeImage = ImageUtils.convertToNativeImage(img);
            NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
            Identifier textureId = Identifier.of(Timecapsules.MOD_ID, "timecapsules_painting_tex/" + capsuleId);
            MinecraftClient.getInstance().getTextureManager().registerTexture(textureId, texture);
            CapsuleTexture capsuleTexture = new CapsuleTexture(textureId, img.getWidth(), img.getHeight());
            textureCache.put(capsuleId, capsuleTexture);
            return capsuleTexture;
        }
        catch (IOException e)
        {
            Timecapsules.LOGGER.error("Failed to load capsule image for ID {}: {}", capsuleId, e.getMessage());
            return null;
        }
    }
}
