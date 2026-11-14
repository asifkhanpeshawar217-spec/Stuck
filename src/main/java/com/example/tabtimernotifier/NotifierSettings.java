
package com.example.tabtimernotifier;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class NotifierSettings {

    private static final Path CONFIG_DIR =
            FabricLoader.getInstance().getConfigDir()
                    .resolve("tab-timer-notifier");

    private static final Path FILE =
            CONFIG_DIR.resolve("settings.json");

    private static final Path SOUNDS_DIR =
            CONFIG_DIR.resolve("sounds");

    // Sound settings
    public static String nukeFile = "";
    public static String cloneFile = "";
    public static String accumulateFile = "";

    public static float nukeVolume = 0.85f;
    public static float cloneVolume = 0.85f;
    public static float accumulateVolume = 0.85f;

    // Nuke flash
    public static boolean nukeFlashEnabled = true;

    // HUD visibility
    public static boolean hudEnabled = true;
    public static boolean hudShowNuke = true;
    public static boolean hudShowClone = true;

    // HUD position
    public static int hudX = 20;
    public static int hudY = 20;

    // HUD dimensions
    public static int hudWidth = 130;
    public static int hudHeight = 100;

    // HUD appearance
    public static float hudTextSize = 1.0f;
    public static float hudOpacity = 0.75f;
    public static int hudSpacing = 8;

    private NotifierSettings() {}

    public static void load() {
        createSoundFolders();

        if (!Files.exists(FILE)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(FILE)) {

            JsonObject json =
                    JsonParser.parseReader(reader).getAsJsonObject();

            // Sound settings
            nukeFile = safeFileName(
                    stringValue(json, "nukeFile", "")
            );

            cloneFile = safeFileName(
                    stringValue(json, "cloneFile", "")
            );

            accumulateFile = safeFileName(
                    stringValue(json, "accumulateFile", "")
            );

            nukeVolume = clamp(
                    floatValue(json, "nukeVolume", 0.85f)
            );

            cloneVolume = clamp(
                    floatValue(json, "cloneVolume", 0.85f)
            );

            accumulateVolume = clamp(
                    floatValue(json, "accumulateVolume", 0.85f)
            );

            // Flash
            nukeFlashEnabled = booleanValue(
                    json, "nukeFlashEnabled", true
            );

            // HUD visibility
            hudEnabled = booleanValue(
                    json, "hudEnabled", true
            );

            hudShowNuke = booleanValue(
                    json, "hudShowNuke", true
            );

            hudShowClone = booleanValue(
                    json, "hudShowClone", true
            );

            // HUD position
            hudX = intValue(json, "hudX", 20);
            hudY = intValue(json, "hudY", 20);

            // HUD dimensions
            hudWidth = clampInt(
                    intValue(json, "hudWidth", 130), 80, 500
            );

            hudHeight = clampInt(
                    intValue(json, "hudHeight", 100), 55, 400
            );

            // HUD appearance
            hudTextSize = clampRange(
                    floatValue(json, "hudTextSize", 1.0f),
                    0.5f,
                    2.0f
            );

            hudOpacity = clamp(
                    floatValue(json, "hudOpacity", 0.75f)
            );

            hudSpacing = clampInt(
                    intValue(json, "hudSpacing", 8), 0, 40
            );

        } catch (Exception e) {
            System.err.println(
                    "[Nuke Alert] Settings load failed: "
                            + e.getMessage()
            );
        }
    }

    public static void save() {

        JsonObject json = new JsonObject();

        // Sound settings
        json.addProperty("nukeFile", nukeFile);
        json.addProperty("cloneFile", cloneFile);
        json.addProperty("accumulateFile", accumulateFile);

        json.addProperty("nukeVolume", nukeVolume);
        json.addProperty("cloneVolume", cloneVolume);
        json.addProperty("accumulateVolume", accumulateVolume);

        // Flash
        json.addProperty(
                "nukeFlashEnabled",
                nukeFlashEnabled
        );

        // HUD visibility
        json.addProperty("hudEnabled", hudEnabled);
        json.addProperty("hudShowNuke", hudShowNuke);
        json.addProperty("hudShowClone", hudShowClone);

        // HUD position
        json.addProperty("hudX", hudX);
        json.addProperty("hudY", hudY);

        // HUD dimensions
        json.addProperty("hudWidth", hudWidth);
        json.addProperty("hudHeight", hudHeight);

        // HUD appearance
        json.addProperty("hudTextSize", hudTextSize);
        json.addProperty("hudOpacity", hudOpacity);
        json.addProperty("hudSpacing", hudSpacing);

        try {
            Files.createDirectories(CONFIG_DIR);

            try (Writer writer = Files.newBufferedWriter(FILE)) {
                new GsonBuilder()
                        .setPrettyPrinting()
                        .create()
                        .toJson(json, writer);
            }

        } catch (Exception e) {
            System.err.println(
                    "[Nuke Alert] Settings save failed: "
                            + e.getMessage()
            );
        }
    }

    // Sound folder helpers

    public static Path soundFolder(String timer) {
        return SOUNDS_DIR.resolve(timer.toLowerCase());
    }

    public static Path soundPath(
            String timer,
            String fileName
    ) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }

        String safe = safeFileName(fileName);

        if (safe.isBlank()) {
            return null;
        }

        return soundFolder(timer).resolve(safe);
    }

    public static void createSoundFolders() {
        try {
            Files.createDirectories(soundFolder("nuke"));
            Files.createDirectories(soundFolder("clone"));
            Files.createDirectories(soundFolder("accumulate"));

        } catch (Exception e) {
            System.err.println(
                    "[Nuke Alert] Could not create sound folders: "
                            + e.getMessage()
            );
        }
    }

    public static String safeFileName(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return Path.of(value.replace('\\', '/'))
                .getFileName()
                .toString();
    }

    // Value helpers

    public static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static float clampRange(
            float value,
            float min,
            float max
    ) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clampInt(
            int value,
            int min,
            int max
    ) {
        return Math.max(min, Math.min(max, value));
    }

    private static String stringValue(
            JsonObject json,
            String key,
            String fallback
    ) {
        return json.has(key) && !json.get(key).isJsonNull()
                ? json.get(key).getAsString()
                : fallback;
    }

    private static float floatValue(
            JsonObject json,
            String key,
            float fallback
    ) {
        return json.has(key)
                ? json.get(key).getAsFloat()
                : fallback;
    }

    private static int intValue(
            JsonObject json,
            String key,
            int fallback
    ) {
        return json.has(key)
                ? json.get(key).getAsInt()
                : fallback;
    }

    private static boolean booleanValue(
            JsonObject json,
            String key,
            boolean fallback
    ) {
        return json.has(key) && !json.get(key).isJsonNull()
                ? json.get(key).getAsBoolean()
                : fallback;
    }

    // Restore all settings

    public static void restoreAll() {

        nukeFile = "";
        cloneFile = "";
        accumulateFile = "";

        nukeVolume = 0.85f;
        cloneVolume = 0.85f;
        accumulateVolume = 0.85f;

        nukeFlashEnabled = true;

        hudEnabled = true;
        hudShowNuke = true;
        hudShowClone = true;

        hudX = 20;
        hudY = 20;

        hudWidth = 130;
        hudHeight = 100;

        hudTextSize = 1.0f;
        hudOpacity = 0.75f;
        hudSpacing = 8;

        save();
    }
}
