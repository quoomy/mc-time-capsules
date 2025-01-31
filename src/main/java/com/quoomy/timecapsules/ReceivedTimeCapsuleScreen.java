package com.quoomy.timecapsules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public class ReceivedTimeCapsuleScreen extends Screen implements Closeable
{
    private static final Identifier BOOK_TEXTURE = Identifier.of("minecraft", "textures/gui/book.png");
    private static final int BOOK_WIDTH = 192;
    private static final int BOOK_HEIGHT = 192;

    private static final int TEXT_X = 36;
    private static final int TEXT_Y = 30;
    private static final int TEXT_MAX_WIDTH = 114;
    private static final int TEXT_MAX_HEIGHT = 128;
    private static final float TEXT_SCALE = 1f;

    private int currentPage = 0;
    private static final int MAX_PAGES = 2;

    private final TimeCapsuleData data;

    private NativeImageBackedTexture capsuleTexture;
    private Identifier capsuleTextureId;
    private int scaledImageWidth;
    private int scaledImageHeight;

    private final List<OrderedText> scrollLines = new ArrayList<>();
    private float scrollOffset = 0.0F;
    private float totalContentHeight = 0.0F;

    private ButtonWidget nextPageButton;
    private ButtonWidget prevPageButton;

    // calc positions
    private int BOOK_X;
    private int BOOK_Y;

    public ReceivedTimeCapsuleScreen(TimeCapsuleData data)
    {
        super(Text.of("Time Capsule Book"));
        this.data = data;
    }

    @Override
    protected void init()
    {
        super.init();

        BOOK_X = (this.width - BOOK_WIDTH) / 2;
        BOOK_Y = 2;
        int PAGE_BUTTONS_Y = BOOK_Y + BOOK_HEIGHT + 5;
        int CLOSE_BUTTON_Y = PAGE_BUTTONS_Y + 25;

        int centerX = (this.width - BOOK_WIDTH) / 2;

        BufferedImage image = data.getImage();
        if (image != null)
        {
            try
            {
                NativeImage nativeImage = convertToNativeImage(image);
                this.capsuleTexture = new NativeImageBackedTexture(nativeImage);
                this.capsuleTextureId = Identifier.of(Timecapsules.MOD_ID, "capsule_image_" + data.getId());
                MinecraftClient.getInstance().getTextureManager().registerTexture(capsuleTextureId, capsuleTexture);

                int originalWidth = image.getWidth();
                int originalHeight = image.getHeight();
                float ratio = (float) originalHeight / (float) originalWidth;

                scaledImageWidth = TEXT_MAX_WIDTH;
                scaledImageHeight = Math.round(scaledImageWidth * ratio);

            }
            catch (Exception e)
            {
                Timecapsules.LOGGER.error("Failed to display image for time capsule screen", e);
            }
        }

        buildScrollLines();

        this.prevPageButton = ButtonWidget.builder(Text.literal("<"), (button) -> goToPreviousPage())
                .dimensions(centerX + 43, PAGE_BUTTONS_Y, 20, 20).build();
        this.nextPageButton = ButtonWidget.builder(Text.literal(">"), (button) -> goToNextPage())
                .dimensions(centerX + 116, PAGE_BUTTONS_Y, 20, 20).build();

        this.addDrawableChild(prevPageButton);
        this.addDrawableChild(nextPageButton);

        int closeBtnX = (this.width / 2) - 50;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), (button) -> this.close()).dimensions(closeBtnX, CLOSE_BUTTON_Y, 100, 20).build());

        updatePageButtons();
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta)
    {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawTexture(RenderLayer::getGuiTextured, BOOK_TEXTURE, BOOK_X, BOOK_Y, 0, 0, BOOK_WIDTH, BOOK_HEIGHT, 256, 256);

        if (currentPage == 0)
            renderFrontPage(context);
        else
            renderScrollPage(context);
    }

    private void buildScrollLines()
    {
        scrollLines.clear();

        String rawText = data.getText();
        String[] lines = rawText.split("\\n");
        for (String line : lines)
        {
            List<OrderedText> wrapped = textRenderer.wrapLines(Text.literal(line), TEXT_MAX_WIDTH);
            scrollLines.addAll(wrapped);
        }

        float lineHeight = textRenderer.fontHeight * TEXT_SCALE;
        this.totalContentHeight = scrollLines.size() * lineHeight;
    }

    private void goToPreviousPage()
    {
        if (currentPage > 0)
        {
            currentPage--;
            updatePageButtons();
        }
    }
    private void goToNextPage()
    {
        if (currentPage < MAX_PAGES - 1)
        {
            currentPage++;
            updatePageButtons();
        }
    }

    private void updatePageButtons()
    {
        this.prevPageButton.visible = (currentPage > 0);
        this.nextPageButton.visible = (currentPage < (MAX_PAGES - 1));

        if (currentPage == 0)
            scrollOffset = 0;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount)
    {
        if (currentPage == 1)
        {
            scrollOffset -= (float) (verticalAmount * 10);
            clampScroll();
            return true;
        }
        return false;
    }

    private void clampScroll()
    {
        if (scrollOffset < 0)
            scrollOffset = 0;
        float maxScroll = Math.max(0, this.totalContentHeight - (float) TEXT_MAX_HEIGHT);
        if (scrollOffset > maxScroll)
            scrollOffset = maxScroll;
    }

    private void renderFrontPage(DrawContext context)
    {
        int textX = BOOK_X + TEXT_X;
        int textY = BOOK_Y + TEXT_Y;

        if (capsuleTextureId != null)
        {
            context.drawTexture(RenderLayer::getGuiTexturedOverlay,
                    capsuleTextureId,
                    textX,
                    textY,
                    0, 0,
                    scaledImageWidth,
                    scaledImageHeight,
                    scaledImageWidth,
                    scaledImageHeight);

            textY += scaledImageHeight + 10;
        }

        drawScaledLine(context, "TIME CAPSULE #" + data.getId(), textX, textY);
        textY += (int)(textRenderer.fontHeight * TEXT_SCALE + 2);

        drawScaledLine(context, "Written by " + data.getUserNameOrSignature(), textX, textY);
        textY += (int)(textRenderer.fontHeight * TEXT_SCALE + 2);

        drawScaledLine(context, "In Universe " + data.getModloader() + " " + data.getGameVersion(), textX, textY);
        // textY += (int)(textRenderer.fontHeight * TEXT_SCALE + 2);
    }
    private void renderScrollPage(DrawContext context)
    {
        int textX = BOOK_X + TEXT_X;
        int textY = BOOK_Y + TEXT_Y;

        float lineHeight = textRenderer.fontHeight * TEXT_SCALE;

        int startLine = (int)(scrollOffset / lineHeight);
        float offsetWithinLine = scrollOffset % lineHeight;
        float drawY = textY - offsetWithinLine;

        int maxVisible = (int) Math.ceil(TEXT_MAX_HEIGHT / lineHeight);

        for (int i = startLine; i < scrollLines.size() && i < startLine + maxVisible; i++)
        {
            OrderedText line = scrollLines.get(i);

            context.getMatrices().push();
            context.getMatrices().translate(textX, drawY, 0);
            context.getMatrices().scale(TEXT_SCALE, TEXT_SCALE, 1.0F);
            context.drawText(textRenderer, line, 0, 0, 0x000000, false);
            context.getMatrices().pop();

            drawY += lineHeight;
        }
    }

    private void drawScaledLine(DrawContext context, String text, int x, int y)
    {
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(TEXT_SCALE, TEXT_SCALE, 1.0F);
        context.drawText(textRenderer, text, 0, 0, 0x000000, false);
        context.getMatrices().pop();
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
