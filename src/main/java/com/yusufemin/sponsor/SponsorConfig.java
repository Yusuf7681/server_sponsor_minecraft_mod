package com.yusufemin.sponsor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SponsorConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("server_sponsor.json");
    private static final int CURRENT_VERSION = 3;

    public int configVersion = 0;

    public String buttonText = "Server Hosting -25%";
    public int buttonWidth = 120;
    public int xOffset = 0;
    public int yOffset = 0;

    public String popupTitle = "Shockbyte Server Hosting";
    public String popupDescription = "Get 25% OFF using code: {code}";
    public String copyAndOpenText = "Copy Code & Open Site";
    public String openOnlyText = "Open Site Only";
    public String cancelText = "Cancel";

    public String discountCode = "YUSUFEMIN";
    public String targetUrl = "https://shockbyte.com/partner/yusufemin";

    private static SponsorConfig instance = newDefault();

    private static SponsorConfig newDefault() {
        SponsorConfig c = new SponsorConfig();
        c.configVersion = CURRENT_VERSION;
        return c;
    }

    public static SponsorConfig get() {
        return instance;
    }

    public static void load() {
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
                SponsorConfig loaded = GSON.fromJson(reader, SponsorConfig.class);
                if (loaded != null) {
                    instance = loaded;
                }
            } catch (Exception e) {
                SponsorModClient.LOGGER.error("Could not read server_sponsor.json, using defaults", e);
                instance = newDefault();
            }
            instance.migrate();
            instance.sanitize();
            save();
        } else {
            instance = newDefault();
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(instance, writer);
            }
        } catch (IOException e) {
            SponsorModClient.LOGGER.error("Could not write server_sponsor.json", e);
        }
    }

    private void migrate() {
        if (configVersion < CURRENT_VERSION) {
            SponsorConfig d = new SponsorConfig();
            buttonText = d.buttonText;
            buttonWidth = d.buttonWidth;
            xOffset = d.xOffset;
            yOffset = d.yOffset;
            popupDescription = d.popupDescription;
            configVersion = CURRENT_VERSION;
        }
    }

    private void sanitize() {
        SponsorConfig d = new SponsorConfig();
        if (isBlank(buttonText)) buttonText = d.buttonText;
        if (isBlank(popupTitle)) popupTitle = d.popupTitle;
        if (isBlank(popupDescription)) popupDescription = d.popupDescription;
        if (isBlank(copyAndOpenText)) copyAndOpenText = d.copyAndOpenText;
        if (isBlank(openOnlyText)) openOnlyText = d.openOnlyText;
        if (isBlank(cancelText)) cancelText = d.cancelText;
        if (isBlank(discountCode)) discountCode = d.discountCode;
        if (isBlank(targetUrl)) targetUrl = d.targetUrl;
        if (buttonWidth < 20) buttonWidth = d.buttonWidth;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
