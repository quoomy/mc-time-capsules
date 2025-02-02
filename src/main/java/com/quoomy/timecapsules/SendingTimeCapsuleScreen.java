package com.quoomy.timecapsules;

import com.quoomy.timecapsules.utils.ImageUtils;
import com.quoomy.timecapsules.utils.MultiLineTextFieldWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SendingTimeCapsuleScreen extends Screen
{
    private static final int MAX_TEXT_SIZE = 1000;
    private static final int MAX_SIGNATURE_SIZE = 100;
    private static final int MAX_PNG_SIZE = 500 * 1024;

    private static ItemStack item = null;

    // general
    private ButtonWidget submitButton;
    private ButtonWidget cancelButton;
    private ButtonWidget nextPageButton;
    private ButtonWidget prevPageButton;
    // page1
    private ButtonWidget prevImageButton;
    private ButtonWidget nextImageButton;
    private static final int previewWidth = 100;
    private static final int previewHeight = 56;
    // page2
    private MultiLineTextFieldWidget textField;
    private TextFieldWidget signatureField;

    private final List<File> screenshotFiles = new ArrayList<>();
    private int selectedScreenshotIndex = 0;

    private BufferedImage selectedImage;
    private NativeImageBackedTexture previewTexture;
    private Identifier previewTextureId;

    private final String username;
    private final String modloader;
    private final String gameVersion;

    private String infoMessage = ""; // Status / info message displayed at the bottom
    private int currentPage = 0;

    /*

        Page 1 will contain image and display of predefined data
        Page 2 will contain the text field and signature field

     */

    private final int width;
    private final int height;
    private final int centerX;
    private final int centerY;
    private final int y;

    private int ticks = 0;

    protected SendingTimeCapsuleScreen(ItemStack stack)
    {
        super(Text.of("Sending Time Capsule"));

        item = stack;

        MinecraftClient client = MinecraftClient.getInstance();
        this.username = client.getSession().getUsername();
        this.modloader = client.getGameVersion();
        this.gameVersion = client.getVersionType();

        width = client.getWindow().getScaledWidth();
        height = client.getWindow().getScaledHeight();
        centerX = width / 2;
        centerY = height / 2;
        y = 20;

        loadScreenshotFiles();
    }

    @Override
    protected void init()
    {
        super.init();

        // prev page | submit | cancel | next page
        int allButtonsWidth = 20 + 10 + 70 + 10 + 70 + 10 + 20;
        int button1X = centerX - (allButtonsWidth / 2);
        int button2X = button1X + 20 + 10;
        int button3X = button2X + 70 + 10;
        int button4X = button3X + 70 + 10;

        prevPageButton = ButtonWidget.builder(Text.literal("<"), (button) -> {
            if (currentPage == 1)
                currentPage = 0;
        }).dimensions(button1X, this.height - 35, 20, 20).build();
        this.addDrawableChild(prevPageButton);

        submitButton = ButtonWidget.builder(Text.literal("Submit"), (button) -> attemptSubmit())
                .dimensions(button2X, this.height - 35, 70, 20).build();
        this.addDrawableChild(submitButton);

        cancelButton = ButtonWidget.builder(Text.literal("Cancel"), (button) -> this.close())
                .dimensions(button3X, this.height - 35, 70, 20).build();
        this.addDrawableChild(cancelButton);

        nextPageButton = ButtonWidget.builder(Text.literal(">"), (button) -> {
            if (currentPage == 0)
                currentPage = 1;
        }).dimensions(button4X, this.height - 35, 20, 20).build();
        this.addDrawableChild(nextPageButton);

        initPage1();
        initPage2();
    }
    private void initPage1()
    {
        prevImageButton = ButtonWidget.builder(Text.literal("<"), (button) -> {
            if (!screenshotFiles.isEmpty()) {
                selectedScreenshotIndex = (selectedScreenshotIndex - 1 + screenshotFiles.size()) % screenshotFiles.size();
                updatePreviewTexture();
            }
        }).dimensions(centerX - (previewWidth / 2) - 10 - 20, y + 115, 20, 20).build();
        this.addDrawableChild(prevImageButton);

        nextImageButton = ButtonWidget.builder(Text.literal(">"), (button) -> {
            if (!screenshotFiles.isEmpty()) {
                selectedScreenshotIndex = (selectedScreenshotIndex + 1) % screenshotFiles.size();
                updatePreviewTexture();
            }
        }).dimensions(centerX + (previewWidth / 2) + 10, y + 115, 20, 20).build();
        this.addDrawableChild(nextImageButton);

        updatePreviewTexture();
    }
    private void initPage2()
    {
        textField = new MultiLineTextFieldWidget(this.textRenderer, centerX - 150, y, 300, 100, Text.of("Message(max " + MAX_TEXT_SIZE + " characters)"), MAX_TEXT_SIZE);
        textField.setText(item.getOrDefault(ModRegistrations.TIME_CAPSULE_SEND_DATA_TEXT, ""));
        this.addDrawableChild(textField);

        signatureField = new TextFieldWidget(this.textRenderer, centerX - 50, y + 100 + 30, 100, 20, Text.of("Signature(optional, max " + MAX_SIGNATURE_SIZE + " characters)"));
        signatureField.setMaxLength(MAX_SIGNATURE_SIZE);
        signatureField.setText(item.getOrDefault(ModRegistrations.TIME_CAPSULE_SEND_DATA_SIGNATURE, ""));
        this.addDrawableChild(signatureField);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta)
    {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        ticks++;
        if (ticks % 20 == 0)
        {
            item.set(ModRegistrations.TIME_CAPSULE_SEND_DATA_TEXT, textField.getText());
            item.set(ModRegistrations.TIME_CAPSULE_SEND_DATA_SIGNATURE, signatureField.getText());
        }

        if (!infoMessage.isEmpty())
            context.drawCenteredTextWithShadow(textRenderer, infoMessage, centerX, this.height - 10, 0xFFFFFF);

        if (currentPage == 0)
            renderPage1(context, mouseX, mouseY, delta);
        else if (currentPage == 1)
            renderPage2(context, mouseX, mouseY, delta);
    }
    private void renderPage1(DrawContext context, int mouseX, int mouseY, float delta)
    {
        context.drawCenteredTextWithShadow(textRenderer, "Time Capsule Submission", centerX, y, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, "Username: " + username, centerX, y + 35, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, "Modloader: " + modloader, centerX, y + 50, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, "Game Version: " + gameVersion, centerX, y + 65, 0xFFFFFF);

        String imageName = selectedImage == null ? "No image" : screenshotFiles.get(selectedScreenshotIndex).getName();
        context.drawCenteredTextWithShadow(textRenderer, "Selected Image: " + imageName, centerX, y + 100, 0xFFFFFF);

        if (previewTextureId != null)
            context.drawTexture(RenderLayer::getGuiTexturedOverlay, previewTextureId, centerX - (previewWidth / 2), y + 115, 0, 0, previewWidth, previewHeight, previewWidth, previewHeight);
    }
    private void renderPage2(DrawContext context, int mouseX, int mouseY, float delta)
    {
        int textLength = textField.getText().length();
        String textText = "Time Capsule Content";
        if (textLength > 0)
        {
            textText += " (" + textLength + "/" + MAX_TEXT_SIZE + ")";
        }
        context.drawText(textRenderer, textText, centerX - (textRenderer.getWidth(textText) / 2), y - 10, 0xFFFFFF, false);

        String signatureText = "Signature / Nickname (optional)";
        context.drawText(textRenderer, signatureText, centerX - (textRenderer.getWidth(signatureText) / 2), y + 100 + 20, 0xFFFFFF, true);
    }

    @Override
    public void tick()
    {
        super.tick();
        if (currentPage == 1)
        {
            textField.visible = true;
            signatureField.visible = true;
            prevImageButton.visible = false;
            nextImageButton.visible = false;
            prevPageButton.active = true;
            nextPageButton.active = false;
        }
        else
        {
            textField.visible = false;
            signatureField.visible = false;
            prevImageButton.visible = true;
            nextImageButton.visible = true;
            prevPageButton.active = false;
            nextPageButton.active = true;
        }
    }

    private void loadScreenshotFiles()
    {
        screenshotFiles.clear();

        File screenshotsDir = new File(MinecraftClient.getInstance().runDirectory, "screenshots");
        if (!screenshotsDir.exists() || !screenshotsDir.isDirectory())
            return;

        File[] files = screenshotsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (files != null)
        {
            screenshotFiles.addAll(Arrays.asList(files));
            screenshotFiles.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified())); // Sort by last modified date, newest first
        }
    }

    private void updatePreviewTexture()
    {
        if (previewTexture != null)
        {
            previewTexture.close();
            previewTexture = null;
        }
        if (screenshotFiles.isEmpty())
        {
            previewTextureId = null;
            return;
        }
        File selectedFile = screenshotFiles.get(selectedScreenshotIndex);
        try
        {
            selectedImage = ImageIO.read(selectedFile);
            NativeImage nativeImage = ImageUtils.convertToNativeImage(selectedImage);
            previewTexture = new NativeImageBackedTexture(nativeImage);
            previewTextureId = Identifier.of(Timecapsules.MOD_ID, "screenshot_preview");
            MinecraftClient.getInstance().getTextureManager().registerTexture(previewTextureId, previewTexture);
        }
        catch (IOException e)
        {
            infoMessage = "Failed to load image preview!";
            Timecapsules.LOGGER.error("Error loading screenshot preview: {}", e.getMessage());
        }
    }

    private void attemptSubmit()
    {
        infoMessage = "";

        String messageText = textField.getText().trim();
        String signatureText = signatureField.getText().trim();

        if (messageText.isEmpty())
        {
            infoMessage = "Message cannot be empty!";
            return;
        }
        if (messageText.length() > MAX_TEXT_SIZE)
        {
            infoMessage = "Message is too long! (max " + MAX_TEXT_SIZE + " characters)";
            return;
        }
        if (signatureText.length() > MAX_SIGNATURE_SIZE)
        {
            infoMessage = "Signature is too long! (max " + MAX_SIGNATURE_SIZE + " characters)";
            return;
        }
        if (screenshotFiles.isEmpty())
        {
            infoMessage = "No screenshots available for selection! Please take one to send a time capsule.";
            return;
        }

        File imageFile = screenshotFiles.get(selectedScreenshotIndex);
        if (!imageFile.getName().toLowerCase().endsWith(".png"))
        {
            infoMessage = "Selected image is not a PNG file!";
            return;
        }
        try
        {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                infoMessage = "Failed to read the image file!";
                return;
            }
            BufferedImage processedImage = scaleImageToMaxSize(image, MAX_PNG_SIZE);

            MinecraftClient client = MinecraftClient.getInstance();
            Path gameDir = client.runDirectory.toPath();
            Path uploadFolder = gameDir.resolve("timecapsules").resolve(TimeCapsuleItem.TO_UPLOAD_ID);
            if (uploadFolder.toFile().exists())
                FileUtils.deleteDirectory(uploadFolder.toFile());
            Files.createDirectories(uploadFolder);

            Files.writeString(uploadFolder.resolve("text.txt"), messageText);
            Files.writeString(uploadFolder.resolve("username.txt"), username);
            if (!signatureText.isEmpty()) {
                Files.writeString(uploadFolder.resolve("signature.txt"), signatureText);
            }
            Files.writeString(uploadFolder.resolve("gameversion.txt"), gameVersion);
            Files.writeString(uploadFolder.resolve("modloader.txt"), modloader);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(processedImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            Files.write(uploadFolder.resolve("image.png"), imageBytes);

            item.set(ModRegistrations.TIME_CAPSULE_DATA_DONE, true);

            infoMessage = "Upload data saved successfully!";
            submitButton.active = false;
            this.close();
        }
        catch (Exception e)
        {
            infoMessage = "Failed to save upload data!";
            Timecapsules.LOGGER.error("Error saving upload data: {}", e.getMessage());
            try
            {
                Path gameDir = MinecraftClient.getInstance().runDirectory.toPath();
                Path uploadFolder = gameDir.resolve("timecapsules").resolve(TimeCapsuleItem.TO_UPLOAD_ID);
                FileUtils.deleteDirectory(uploadFolder.toFile());
            }
            catch (Exception ex)
            {
                Timecapsules.LOGGER.error("Failed to clean upload folder: {}", ex.getMessage());
            }
        }
    }

    private BufferedImage scaleImageToMaxSize(BufferedImage image, int maxBytes) throws IOException
    {
        double scale = 1.0;
        BufferedImage currentImage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(currentImage, "png", baos);
        byte[] bytes = baos.toByteArray();
        if (bytes.length <= maxBytes)
            return currentImage;

        // Iteratively scale down the image (reduce by 10% each loop)
        while (bytes.length > maxBytes && scale > 0.1)
        {
            scale *= 0.9;
            int newWidth = (int) (image.getWidth() * scale);
            int newHeight = (int) (image.getHeight() * scale);
            BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, image.getType());
            Graphics2D g2d = scaledImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
            g2d.dispose();
            baos.reset();
            ImageIO.write(scaledImage, "png", baos);
            bytes = baos.toByteArray();
            currentImage = scaledImage;
        }
        if (bytes.length > maxBytes)
            throw new IOException("Unable to scale image to acceptable size. Final size: " + bytes.length + " bytes.");
        return currentImage;
    }

    @Override
    public void close()
    {
        if (previewTexture != null)
            previewTexture.close();
        super.close();
    }
}
