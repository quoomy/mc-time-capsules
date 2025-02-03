package com.quoomy.timecapsules.screen;

import com.quoomy.timecapsules.item.timecapsule.TimeCapsuleData;
import com.quoomy.timecapsules.Timecapsules;
import com.quoomy.timecapsules.utils.ImageUtils;
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
import net.minecraft.util.Util;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public class ReceivedTimeCapsuleScreen extends Screen implements Closeable
{
    private static final Identifier BOOK_TEXTURE = Identifier.of("minecraft", "textures/gui/book.png");
    private static final int BOOK_WIDTH = 186;
    private static final int BOOK_HEIGHT = 182;

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
        int PAGE_BUTTONS_Y = BOOK_Y + BOOK_HEIGHT;
        int BUTTON_HEIGHT = 20;
        int PAGE_BUTTON_HEIGHT = BUTTON_HEIGHT / 2;
        int MORE_INFO_BUTTON_Y = PAGE_BUTTONS_Y + PAGE_BUTTON_HEIGHT + 5;
        int CLOSE_BUTTON_Y = MORE_INFO_BUTTON_Y + BUTTON_HEIGHT + 1;

        int centerX = this.width / 2;

        BufferedImage image = data.getImage();
        if (image != null)
        {
            try
            {
                NativeImage nativeImage = ImageUtils.convertToNativeImage(image);
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

        int pageRowWidth = 20 + 5 + 20;
        int firstRowStartX = centerX - (pageRowWidth / 2);

        this.prevPageButton = ButtonWidget.builder(Text.literal("<"), (button) -> goToPreviousPage())
                .dimensions(firstRowStartX, PAGE_BUTTONS_Y, 20, PAGE_BUTTON_HEIGHT).build();
        this.nextPageButton = ButtonWidget.builder(Text.literal(">"), (button) -> goToNextPage())
                .dimensions(firstRowStartX + 20 + 3, PAGE_BUTTONS_Y, 20, PAGE_BUTTON_HEIGHT).build();

        this.addDrawableChild(prevPageButton);
        this.addDrawableChild(nextPageButton);

        int doneButtonWidth = 100;
        int moreInfoButtonWidth = 150;

        ButtonWidget moreInfoButton = ButtonWidget.builder(Text.literal("More Info / Report"), (button) -> Util.getOperatingSystem().open("https://timecapsules.quoomy.com/"))
                .dimensions(centerX - (moreInfoButtonWidth / 2), MORE_INFO_BUTTON_Y, moreInfoButtonWidth, BUTTON_HEIGHT).build();

        ButtonWidget closeButton = ButtonWidget.builder(Text.literal("Done"), (button) -> this.close())
                .dimensions(centerX - (doneButtonWidth / 2), CLOSE_BUTTON_Y, doneButtonWidth, BUTTON_HEIGHT).build();

        this.addDrawableChild(closeButton);
        this.addDrawableChild(moreInfoButton);

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

        drawScaledLine(context, "Written by " + data.getUserNameAndSignature(), textX, textY);
        textY += (int)(textRenderer.fontHeight * TEXT_SCALE + 2);

        drawScaledLine(context, "Universe " + data.getModloader() + " " + data.getGameVersion(), textX, textY);
        textY += (int)(textRenderer.fontHeight * TEXT_SCALE + 2);

        if (!data.getFormattedTimestamp().isEmpty())
        {
            drawScaledLine(context, "Sent on " + data.getFormattedTimestamp(), textX, textY);
            // textY += (int)(textRenderer.fontHeight * TEXT_SCALE + 2);
        }
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
