package com.quoomy.timecapsules;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TimeCapsuleData
{
    private int id;
    private BufferedImage image;
    private String text;
    private String username;
    private String signature;
    private String gameVersion;
    private String modloader;
    private boolean isValid;

    public TimeCapsuleData()
    {
        this.id = -1;
        this.image = null;
        this.text = "";
        this.username = "";
        this.signature = "";
        this.gameVersion = "";
        this.modloader = "";
        this.isValid = false;
    }

    public int getId() { return this.id; }
    public BufferedImage getImage() { return this.image; }
    public String getText() { return this.text; }
    public String getUsername() { return this.username; }
    public String getSignature() { return this.signature; }
    public String getGameVersion() { return this.gameVersion; }
    public String getModloader() { return this.modloader; }
    public boolean isValid() { return this.isValid; }

    public void setId(int id) { this.id = id; }
    public void setImage(BufferedImage image) { this.image = image; }
    public void setText(String text) { this.text = text; }
    public void setUsername(String username) { this.username = username; }
    public void setSignature(String signature) { this.signature = signature; }
    public void setGameVersion(String gameVersion) { this.gameVersion = gameVersion; }
    public void setModloader(String modloader) { this.modloader = modloader; }
    public void setValid(boolean isValid) { this.isValid = isValid; }

    // TODO: Start fetching data asynchronously once item is in inventory.
    public static TimeCapsuleData fetchTimeCapsuleData()
    {
        TimeCapsuleData data = new TimeCapsuleData();
        Gson gson = new Gson();

        try
        {
            URL url = new URL("timecapsules.quoomy.com/fetch.py");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            String jsonData;

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                TimeCapsulesMod.LOGGER.error("Failed to fetch time capsule data: HTTP error code {}", connection.getResponseCode());
                data.setValid(false);
                return data;
            }

            StringBuilder jsonBuilder = new StringBuilder();
            int bytesRead;
            byte[] buffer = new byte[1024];
            while ((bytesRead = connection.getInputStream().read(buffer)) != -1)
                jsonBuilder.append(new String(buffer, 0, bytesRead));
            jsonData = jsonBuilder.toString();

            JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);

            if (jsonObject.has("id") && jsonObject.get("id").isJsonPrimitive())
                data.setId(jsonObject.get("id").getAsInt());
            if (jsonObject.has("text_data") && jsonObject.get("text_data").isJsonPrimitive())
                data.setText(jsonObject.get("text_data").getAsString());
            if (jsonObject.has("username") && jsonObject.get("username").isJsonPrimitive())
                data.setUsername(jsonObject.get("username").getAsString());
            if (jsonObject.has("signature") && jsonObject.get("signature").isJsonPrimitive())
                data.setSignature(jsonObject.get("signature").getAsString());
            if (jsonObject.has("game_version") && jsonObject.get("game_version").isJsonPrimitive())
                data.setGameVersion(jsonObject.get("game_version").getAsString());
            if (jsonObject.has("modloader") && jsonObject.get("modloader").isJsonPrimitive())
                data.setModloader(jsonObject.get("modloader").getAsString());
            if (jsonObject.has("image_url") && jsonObject.get("image_url").isJsonPrimitive())
            {
                String imageUrl = jsonObject.get("image_url").getAsString();
                URL imageUrlObj = new URL(imageUrl);
                InputStream imageStream = imageUrlObj.openStream();
                data.setImage(ImageIO.read(imageStream));
            }

            data.setValid(true);

            if (data.getUsername().isEmpty() && data.getSignature().isEmpty())
                data.setValid(false);
            if (data.getText().isEmpty())
                data.setValid(false);
        }
        catch (IOException e)
        {
            TimeCapsulesMod.LOGGER.error("Failed to fetch time capsule data: {}", e.getMessage());
            data.setValid(false);
        }

        return data;
    }

    public String getNameOrSignature()
    {
        return this.signature.isEmpty() ? this.username : this.signature;
    }

    public String getDataPrintout()
    {
        return "Time Capsule ID: " + this.id + "\n" +
               "Username: " + this.username + "\n" +
               "Signature: " + this.signature + "\n" +
               "Game Version: " + this.gameVersion + "\n" +
               "Modloader: " + this.modloader + "\n" +
               "Text: " + this.text;
    }
}
