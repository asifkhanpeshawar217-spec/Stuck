package com.example.tabtimernotifier;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class SoundSettingsScreen extends Screen {
    private final Screen parent;
    private List<String> nukeSounds = List.of();
    private List<String> cloneSounds = List.of();
    private List<String> accumulateSounds = List.of();

    private static final int DARK_BG = 0xFF10131A;
    private static final int DARK_CARD = 0xFF1B202A;
    private static final int DARK_TEXT = 0xFFF2F4F8;
    private static final int DARK_MUTED = 0xFF9AA5B5;
    private static final int LIGHT_BG = 0xFFF2F4F7;
    private static final int LIGHT_CARD = 0xFFFFFFFF;
    private static final int LIGHT_TEXT = 0xFF202633;
    private static final int LIGHT_MUTED = 0xFF687386;
    private static final int ACCENT = 0xFF8C9EFF;

    public SoundSettingsScreen(Screen parent) {
        super(Text.literal("Nuke Alert Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        NotifierSettings.createSoundFolders();
        refreshLists();

        int cardWidth = Math.min(500, width - 28);
        int left = (width - cardWidth) / 2;
        int top = Math.max(34, (height - 416) / 2);

        addDrawableChild(ButtonWidget.builder(
                Text.literal(NotifierSettings.lightTheme ? "Light theme" : "Dark theme"),
                button -> {
                    NotifierSettings.lightTheme = !NotifierSettings.lightTheme;
                    NotifierSettings.save();
                    clearAndInit();
                }
        ).dimensions(left + cardWidth - 112, top - 28, 112, 20).build());

        addSection("NUKE", "nuke", left, top, cardWidth);
        addSection("CLONE", "clone", left, top + 105, cardWidth);
        addSection("ACCUMULATE", "accumulate", left, top + 210, cardWidth);

        addDrawableChild(ButtonWidget.builder(flashButtonText(), button -> {
            NotifierSettings.nukeFlashEnabled = !NotifierSettings.nukeFlashEnabled;
            NotifierSettings.save();
            clearAndInit();
        }).dimensions(left, top + 318, cardWidth, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Refresh sound folders"), button -> {
            refreshLists();
            clearAndInit();
        }).dimensions(left, top + 344, cardWidth, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Restore defaults"), button -> {
            NotifierSettings.restoreAll();
            clearAndInit();
        }).dimensions(left, top + 370, cardWidth / 2 - 4, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close())
                .dimensions(left + cardWidth / 2 + 4, top + 370,
                        cardWidth / 2 - 4, 20).build());
    }

    private Text flashButtonText() {
        return Text.literal("Nuke screen flash  ·  "
                + (NotifierSettings.nukeFlashEnabled ? "ON" : "OFF"));
    }

    private void addSection(String title, String timer, int x, int y, int w) {
        addDrawableChild(ButtonWidget.builder(Text.literal("‹"), button -> {
            cycle(timer, -1);
            clearAndInit();
        }).dimensions(x, y + 42, 28, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("›"), button -> {
            cycle(timer, 1);
            clearAndInit();
        }).dimensions(x + 34, y + 42, 28, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Preview"),
                button -> IndependentAudio.preview(timer))
                .dimensions(x + w - 88, y + 42, 88, 20).build());

        addDrawableChild(new VolumeSlider(
                x, y + 70, w, 20, getVolume(timer), timer
        ));
    }

    private void cycle(String timer, int direction) {
        List<String> sounds = listFor(timer);
        if (sounds.isEmpty()) {
            setSelected(timer, "");
            NotifierSettings.save();
            return;
        }
        String current = getSelected(timer);
        int index = sounds.indexOf(current);
        if (index < 0) index = direction > 0 ? -1 : 0;
        index = Math.floorMod(index + direction, sounds.size());
        setSelected(timer, sounds.get(index));
        NotifierSettings.save();
    }

    private void refreshLists() {
        nukeSounds = scan("nuke");
        cloneSounds = scan("clone");
        accumulateSounds = scan("accumulate");
        ensureSelection("nuke", nukeSounds);
        ensureSelection("clone", cloneSounds);
        ensureSelection("accumulate", accumulateSounds);
    }

    private List<String> scan(String timer) {
        Path folder = NotifierSettings.soundFolder(timer);
        List<String> result = new ArrayList<>();
        try {
            Files.createDirectories(folder);
            try (var paths = Files.list(folder)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> isAudioFile(path.getFileName().toString()))
                        .map(path -> path.getFileName().toString())
                        .sorted(Comparator.comparing(String::toLowerCase))
                        .forEach(result::add);
            }
        } catch (IOException e) {
            System.err.println("[Nuke Alert] Could not scan " + folder + ": " + e.getMessage());
        }
        return result;
    }

    private boolean isAudioFile(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".mp3") || lower.endsWith(".wav")
                || lower.endsWith(".aiff") || lower.endsWith(".aif");
    }

    private void ensureSelection(String timer, List<String> sounds) {
        String selected = getSelected(timer);
        if (!selected.isBlank() && !sounds.contains(selected)) {
            setSelected(timer, "");
            NotifierSettings.save();
        }
    }

    private List<String> listFor(String timer) {
        return switch (timer) {
            case "nuke" -> nukeSounds;
            case "accumulate" -> accumulateSounds;
            default -> cloneSounds;
        };
    }

    private String getSelected(String timer) {
        return switch (timer) {
            case "nuke" -> NotifierSettings.nukeFile;
            case "accumulate" -> NotifierSettings.accumulateFile;
            default -> NotifierSettings.cloneFile;
        };
    }

    private void setSelected(String timer, String value) {
        switch (timer) {
            case "nuke" -> NotifierSettings.nukeFile = value;
            case "accumulate" -> NotifierSettings.accumulateFile = value;
            default -> NotifierSettings.cloneFile = value;
        }
    }

    private float getVolume(String timer) {
        return switch (timer) {
            case "nuke" -> NotifierSettings.nukeVolume;
            case "accumulate" -> NotifierSettings.accumulateVolume;
            default -> NotifierSettings.cloneVolume;
        };
    }

    private void setVolume(String timer, float value) {
        switch (timer) {
            case "nuke" -> NotifierSettings.nukeVolume = value;
            case "accumulate" -> NotifierSettings.accumulateVolume = value;
            default -> NotifierSettings.cloneVolume = value;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean light = NotifierSettings.lightTheme;
        int bg = light ? LIGHT_BG : DARK_BG;
        int card = light ? LIGHT_CARD : DARK_CARD;
        int main = light ? LIGHT_TEXT : DARK_TEXT;
        int muted = light ? LIGHT_MUTED : DARK_MUTED;
        int cardWidth = Math.min(500, width - 28);
        int left = (width - cardWidth) / 2;
        int top = Math.max(34, (height - 416) / 2);

        context.fill(0, 0, width, height, bg);
        Text heading = Text.literal("NUKE ALERT");
        context.drawText(textRenderer, heading,
                width / 2 - textRenderer.getWidth(heading) / 2,
                top - 25, main, false);
        Text subtitle = Text.literal("COOLDOWN NOTIFICATIONS");
        context.drawText(textRenderer, subtitle,
                width / 2 - textRenderer.getWidth(subtitle) / 2,
                top - 12, muted, false);

        drawCard(context, left, top, cardWidth, "NUKE", "nuke", card, main, muted);
        drawCard(context, left, top + 105, cardWidth, "CLONE", "clone", card, main, muted);
        drawCard(context, left, top + 210, cardWidth, "ACCUMULATE", "accumulate",
                card, main, muted);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawCard(DrawContext context, int x, int y, int w,
                          String title, String timer, int card, int main, int muted) {
        context.fill(x - 8, y, x + w + 8, y + 98, card);
        context.fill(x - 8, y, x + w + 8, y + 2, ACCENT);
        context.drawText(textRenderer, title, x, y + 10, main, false);

        String selected = getSelected(timer);
        String shown = selected.isBlank() ? "Built-in tone" : selected;
        context.drawText(textRenderer, "SOUND", x, y + 29, muted, false);
        context.drawText(textRenderer, shown, x + 48, y + 29, main, false);

        List<String> sounds = listFor(timer);
        String count = sounds.size() + (sounds.size() == 1 ? " file available" : " files available");
        context.drawText(textRenderer, count, x, y + 50, muted, false);
        context.drawText(textRenderer, "VOLUME", x, y + 76, muted, false);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }

    private final class VolumeSlider extends SliderWidget {
        private final String timer;

        VolumeSlider(int x, int y, int width, int height, double value, String timer) {
            super(x, y, width, height, Text.literal("Volume"), value);
            this.timer = timer;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal("Volume: " + Math.round(value * 100) + "%"));
        }

        @Override
        protected void applyValue() {
            setVolume(timer, (float) value);
            NotifierSettings.save();
        }
    }
}