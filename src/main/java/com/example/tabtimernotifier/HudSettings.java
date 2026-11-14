
package com.example.tabtimernotifier;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HudSettings {
    private static final Path FILE = FabricLoader.getInstance()
            .getConfigDir().resolve("tab-timer-notifier")
            .resolve("hud.json");

    public static boolean nukeEnabled = true;
    public static boolean cloneEnabled = true;

    public static int x = 12;
    public static int y = 40;
    public static int width = 150;
    public static int height = 112;
    public static int textScale = 1;
    public static int spacing = 8;
    public static float opacity = 0.78f;

    private HudSettings() {}

    public static void load() {
        if (!Files.exists(FILE)) return;

        try (Reader reader = Files.newBufferedReader(FILE)) {
            JsonObject j = JsonParser.parseReader(reader).getAsJsonObject();

            nukeEnabled = bool(j, "nukeEnabled", true);
            cloneEnabled = bool(j, "cloneEnabled", true);
            x = integer(j, "x", 12);
            y = integer(j, "y", 40);
            width = clamp(integer(j, "width", 150), 100, 500);
            height = clamp(integer(j, "height", 112), 60, 400);
            textScale = clamp(integer(j, "textScale", 1), 1, 3);
            spacing = clamp(integer(j, "spacing", 8), 0, 30);
            opacity = Math.max(0f, Math.min(1f,
                    number(j, "opacity", 0.78f)));

        } catch (Exception e) {
            System.err.println("[Nuke Alert] HUD settings load failed: "
                    + e.getMessage());
        }
    }

    public static void save() {
        JsonObject j = new JsonObject();

        j.addProperty("nukeEnabled", nukeEnabled);
        j.addProperty("cloneEnabled", cloneEnabled);
        j.addProperty("x", x);
        j.addProperty("y", y);
        j.addProperty("width", width);
        j.addProperty("height", height);
        j.addProperty("textScale", textScale);
        j.addProperty("spacing", spacing);
        j.addProperty("opacity", opacity);

        try {
            Files.createDirectories(FILE.getParent());

            try (Writer writer = Files.newBufferedWriter(FILE)) {
                new GsonBuilder().setPrettyPrinting().create()
                        .toJson(j, writer);
            }
        } catch (Exception e) {
            System.err.println("[Nuke Alert] HUD settings save failed: "
                    + e.getMessage());
        }
    }

    private static boolean bool(JsonObject j, String k, boolean d) {
        return j.has(k) ? j.get(k).getAsBoolean() : d;
    }

    private static int integer(JsonObject j, String k, int d) {
        return j.has(k) ? j.get(k).getAsInt() : d;
    }

    private static float number(JsonObject j, String k, float d) {
        return j.has(k) ? j.get(k).getAsFloat() : d;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
