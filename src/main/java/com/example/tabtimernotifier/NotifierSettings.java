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
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("tab-timer-notifier");
    private static final Path FILE = CONFIG_DIR.resolve("settings.json");
    private static final Path SOUNDS_DIR = CONFIG_DIR.resolve("sounds");

    public static String nukeFile = "";
    public static String cloneFile = "";
    public static String accumulateFile = "";
    public static float nukeVolume = 0.85f;
    public static float cloneVolume = 0.85f;
    public static float accumulateVolume = 0.85f;

    private NotifierSettings() {}

    public static void load() {
        createSoundFolders();
        if (!Files.exists(FILE)) return;

        try (Reader reader = Files.newBufferedReader(FILE)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            nukeFile = safeFileName(stringValue(json, "nukeFile", ""));
            cloneFile = safeFileName(stringValue(json, "cloneFile", ""));
            accumulateFile = safeFileName(stringValue(json, "accumulateFile", ""));
            nukeVolume = clamp(floatValue(json, "nukeVolume", 0.85f));
            cloneVolume = clamp(floatValue(json, "cloneVolume", 0.85f));
            accumulateVolume = clamp(floatValue(json, "accumulateVolume", 0.85f));
        } catch (Exception e) {
            System.err.println("[Tab Timer Notifier] Settings load failed: " + e.getMessage());
        }
    }

    private static String stringValue(JsonObject json, String key, String fallback) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : fallback;
    }

    private static float floatValue(JsonObject json, String key, float fallback) {
        return json.has(key) ? json.get(key).getAsFloat() : fallback;
    }

    public static void save() {
        JsonObject json = new JsonObject();
        json.addProperty("nukeFile", nukeFile);
        json.addProperty("cloneFile", cloneFile);
        json.addProperty("accumulateFile", accumulateFile);
        json.addProperty("nukeVolume", nukeVolume);
        json.addProperty("cloneVolume", cloneVolume);
        json.addProperty("accumulateVolume", accumulateVolume);

        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
            }
        } catch (Exception e) {
            System.err.println("[Tab Timer Notifier] Settings save failed: " + e.getMessage());
        }
    }

    public static Path soundFolder(String timer) {
        return SOUNDS_DIR.resolve(timer.toLowerCase());
    }

    public static Path soundPath(String timer, String fileName) {
        if (fileName == null || fileName.isBlank()) return null;
        String safe = safeFileName(fileName);
        if (safe.isBlank()) return null;
        return soundFolder(timer).resolve(safe);
    }

    public static void createSoundFolders() {
        try {
            Files.createDirectories(soundFolder("nuke"));
            Files.createDirectories(soundFolder("clone"));
            Files.createDirectories(soundFolder("accumulate"));
        } catch (Exception e) {
            System.err.println("[Tab Timer Notifier] Could not create sound folders: " + e.getMessage());
        }
    }

    public static String safeFileName(String value) {
        if (value == null || value.isBlank()) return "";
        return Path.of(value.replace('\\', '/')).getFileName().toString();
    }

    public static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    public static void restoreAll() {
        nukeFile = "";
        cloneFile = "";
        accumulateFile = "";
        nukeVolume = cloneVolume = accumulateVolume = 0.85f;
        save();
    }
}
