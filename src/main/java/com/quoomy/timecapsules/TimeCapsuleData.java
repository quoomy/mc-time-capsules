package com.quoomy.timecapsules;

import com.google.gson.JsonObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FileUtils;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

import static net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil.GSON;

public class TimeCapsuleData
{
    private int id;
    private BufferedImage image;
    private String text;
    private String username;
    private String signature;
    private String gameVersion;
    private String modloader;
    private int timestamp;
    private boolean isValid;

    private void initEmpty()
    {
        this.id = -1;
        this.image = null;
        this.text = "";
        this.username = "";
        this.signature = "";
        this.gameVersion = "";
        this.modloader = "";
        this.timestamp = -1;
        this.isValid = false;
    }

    public TimeCapsuleData() // fetching constructor
    {
        initEmpty();

        try
        {
            URL url = new URL("https://timecapsules.quoomy.com/fetch.py");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // ms
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                Timecapsules.LOGGER.error("Failed to fetch random time capsule data: HTTP code {}", connection.getResponseCode());
                this.isValid = false;
                return;
            }

            JsonObject jsonObject;
            try
            {
                StringBuilder jsonBuilder = new StringBuilder();
                int bytesRead;
                byte[] buffer = new byte[1024];
                while ((bytesRead = connection.getInputStream().read(buffer)) != -1)
                    jsonBuilder.append(new String(buffer, 0, bytesRead));
                jsonObject = GSON.fromJson(jsonBuilder.toString(), JsonObject.class);
            }
            catch (IOException e)
            {
                Timecapsules.LOGGER.error("Failed to read JSON from time capsule server: {}", e.getMessage());
                this.isValid = false;
                return;
            }


            Timecapsules.LOGGER.info("Received JSON: {}", jsonObject);

            if (jsonObject.has("capsule_id") && jsonObject.get("capsule_id").isJsonPrimitive())
                this.id = jsonObject.get("capsule_id").getAsInt();
            if (jsonObject.has("text_data") && jsonObject.get("text_data").isJsonPrimitive())
                this.text = jsonObject.get("text_data").getAsString();
            if (jsonObject.has("username") && jsonObject.get("username").isJsonPrimitive())
                this.username = jsonObject.get("username").getAsString();
            if (jsonObject.has("signature") && jsonObject.get("signature").isJsonPrimitive())
                this.signature = jsonObject.get("signature").getAsString();
            if (jsonObject.has("game_version") && jsonObject.get("game_version").isJsonPrimitive())
                this.gameVersion = jsonObject.get("game_version").getAsString();
            if (jsonObject.has("mod_loader") && jsonObject.get("mod_loader").isJsonPrimitive())
                this.modloader = jsonObject.get("mod_loader").getAsString();
            if (jsonObject.has("timestamp") && jsonObject.get("timestamp").isJsonPrimitive())
                this.timestamp = jsonObject.get("timestamp").getAsInt();

            if (jsonObject.has("image_url") && jsonObject.get("image_url").isJsonPrimitive())
            {
                String imageUrl = jsonObject.get("image_url").getAsString();
                URL imageUrlObj = new URL(imageUrl);
                HttpURLConnection imgConnection = (HttpURLConnection) imageUrlObj.openConnection();
                Timecapsules.LOGGER.info("Attempting to load image from {}", imageUrl);

                if (imgConnection.getResponseCode() != HttpURLConnection.HTTP_OK)
                    Timecapsules.LOGGER.error("Failed to load image for capsule ID {} due to invalid response code.", this.id);
                else
                {
                    try (InputStream imgStream = imgConnection.getInputStream())
                    {
                        BufferedImage img = ImageIO.read(imgStream);
                        if (img == null)
                            Timecapsules.LOGGER.error("Failed to load image for time capsule because it was null.");
                        else
                        {
                            this.image = img;
                            Timecapsules.LOGGER.info("Image loaded successfully");
                        }
                    }
                    catch (IOException e)
                    {
                        Timecapsules.LOGGER.error("Failed to load image for capsule ID {}: {}", this.id, e.getMessage());
                    }
                }
            }

            if (this.id < 0 || (this.username.isEmpty() && this.signature.isEmpty()) || this.text.isEmpty())
                this.isValid = false;
            else
                this.isValid = true;

            if (this.isValid) // if valid, write to disk
            {
                File capsuleFolder = getCapsuleFolder(String.valueOf(this.id));
                if (!capsuleFolder.exists())
                    capsuleFolder.mkdirs();

                if (this.text != null && !this.text.isEmpty())
                    writeToFile(new File(capsuleFolder, "text.txt"), this.text);
                if (this.username != null && !this.username.isEmpty())
                    writeToFile(new File(capsuleFolder, "username.txt"), this.username);
                if (this.signature != null && !this.signature.isEmpty())
                    writeToFile(new File(capsuleFolder, "signature.txt"), this.signature);
                if (this.gameVersion != null && !this.gameVersion.isEmpty())
                    writeToFile(new File(capsuleFolder, "gameversion.txt"), this.gameVersion);
                if (this.modloader != null && !this.modloader.isEmpty())
                    writeToFile(new File(capsuleFolder, "modloader.txt"), this.modloader);
                if (this.timestamp > 0)
                    writeToFile(new File(capsuleFolder, "timestamp.txt"), Integer.toString(this.timestamp));
                if (this.image != null)
                    ImageIO.write(this.image, "png", new File(capsuleFolder, "image.png"));
            }
        }
        catch (IOException e)
        {
            Timecapsules.LOGGER.error("Failed to fetch random time capsule data: {}", e.getMessage());
            this.isValid = false;
        }
    }
    private void writeToFile(File file, String content)
    {
        try
        {
            FileUtils.writeStringToFile(file, content, "UTF-8");
        }
        catch (IOException e)
        {
            Timecapsules.LOGGER.error("Error writing file {}: {}", file.getAbsolutePath(), e.getMessage());
        }
    }

    public TimeCapsuleData(String capsuleId) // file loading constructor
    {
        initEmpty();
        if (!Objects.equals(capsuleId, TimeCapsuleItem.TO_UPLOAD_ID))
            this.id = Integer.parseInt(capsuleId);

        File capsuleFolder = getCapsuleFolder(capsuleId);

        if (!capsuleFolder.exists() || !capsuleFolder.isDirectory())
        {
            this.isValid = false;
            return;
        }

        File textFile = new File(capsuleFolder, "text.txt");
        if (textFile.exists())
            this.text = readFileContents(textFile);
        File usernameFile = new File(capsuleFolder, "username.txt");
        if (usernameFile.exists())
            this.username = readFileContents(usernameFile);
        File signatureFile = new File(capsuleFolder, "signature.txt");
        if (signatureFile.exists())
            this.signature = readFileContents(signatureFile);
        File gvFile = new File(capsuleFolder, "gameversion.txt");
        if (gvFile.exists())
            this.gameVersion = readFileContents(gvFile);
        File mlFile = new File(capsuleFolder, "modloader.txt");
        if (mlFile.exists())
            this.modloader = readFileContents(mlFile);
        File timestampFile = new File(capsuleFolder, "timestamp.txt");
        if (timestampFile.exists())
            this.timestamp = Integer.parseInt(readFileContents(timestampFile));
        File imageFile = new File(capsuleFolder, "image.png");
        if (imageFile.exists())
        {
            try
            {
                this.image = ImageIO.read(imageFile);
            }
            catch (IOException e)
            {
                Timecapsules.LOGGER.error("Couldn't load image for ID {}: {}", id, e.getMessage());
            }
        }

        if ((this.username == null || this.username.isEmpty()) &&
                (this.signature == null || this.signature.isEmpty()) ||
                (this.text == null || this.text.isEmpty()))
            this.isValid = false;
        else
            this.isValid = true;
    }
    private String readFileContents(File file)
    {
        try
        {
            return FileUtils.readFileToString(file, "UTF-8");
        }
        catch (IOException e)
        {
            Timecapsules.LOGGER.error("Error reading file {}: {}", file.getAbsolutePath(), e.getMessage());
        }
        return "";
    }

    private File getCapsuleFolder(String capsuleId)
    {
        Path gameDirPath = FabricLoader.getInstance().getGameDir();
        return new File(gameDirPath.toFile(), "timecapsules/" + capsuleId);
    }

    public void sendCapsule()
    {
        String boundary = "----TimeCapsuleBoundary" + System.currentTimeMillis();
        String LINE_FEED = "\r\n";

        try
        {
            URL url = new URL("https://timecapsules.quoomy.com/upload.py");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setRequestProperty("mod-version", Timecapsules.MOD_VERSION);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            OutputStream outputStream = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);

            addFormField(writer, boundary, "username", this.username);
            addFormField(writer, boundary, "text_data", this.text);
            if (!this.signature.isEmpty()) {
                addFormField(writer, boundary, "signature", this.signature);
            }
            if (!this.gameVersion.isEmpty()) {
                addFormField(writer, boundary, "game_version", this.gameVersion);
            }
            if (!this.modloader.isEmpty()) {
                addFormField(writer, boundary, "modloader", this.modloader);
            }

            if (this.image != null)
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(this.image, "png", baos);
                byte[] imageBytes = baos.toByteArray();

                writer.append("--").append(boundary).append(LINE_FEED);
                writer.append("Content-Disposition: form-data; name=\"png_file\"; filename=\"image.png\"").append(LINE_FEED);
                writer.append("Content-Type: image/png").append(LINE_FEED);
                writer.append(LINE_FEED);
                writer.flush();
                outputStream.write(imageBytes);
                outputStream.flush();
                writer.append(LINE_FEED);
                writer.flush();
            }

            writer.append("--").append(boundary).append("--").append(LINE_FEED);
            writer.flush();
        }
        catch (IOException e)
        {
            Timecapsules.LOGGER.error("Failed to send time capsule: {}", e.getMessage());
        }
    }
    private void addFormField(PrintWriter writer, String boundary, String name, String value) throws IOException
    {
        String LINE_FEED = "\r\n";
        writer.append("--").append(boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"").append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=UTF-8").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }

    public int getId() { return this.id; }
    public BufferedImage getImage() { return this.image; }
    public String getText() { return this.text; }
    public String getUsername() { return this.username; }
    public String getSignature() { return this.signature; }
    public String getUserNameOrSignature() { return this.signature.isEmpty() ? this.username : this.signature; }
    public String getGameVersion() { return this.gameVersion; }
    public String getModloader() { return this.modloader; }
    public int getTimestamp() { return this.timestamp; }
    public boolean isValid() { return this.isValid; }

    public String getDataPrintout()
    {
        return "Time Capsule ID: " + this.id + "\n" +
                "Username: " + this.username + "\n" +
                "Signature: " + this.signature + "\n" +
                "Game Version: " + this.gameVersion + "\n" +
                "Modloader: " + this.modloader + "\n" +
                "Text: " + this.text;
    }

    public String getFormattedTimestamp()
    {
        if (this.timestamp < 1_000_000_000) // September 2001. Mod was released in 2025.
            return "";
        Instant instant = Instant.ofEpochSecond(this.timestamp);
        LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy").withLocale(Locale.GERMAN);
        return date.format(formatter);
    }
}
