package com.example.tabtimernotifier;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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
        super(Component.literal("Nuke Alert Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        NotifierSettings.createSoundFolders();
        refreshLists();

        int cardWidth = Math.min(500, width - 28);
        int left = (width - cardWidth) / 2;
        int top = Math.max(34, (height - 416) / 2);

        addRenderableWidget(Button.builder(
                Component.literal(NotifierSettings.lightTheme ? "Light theme" : "Dark theme"),
                button -> {
                    NotifierSettings.lightTheme = !NotifierSettings.lightTheme;
                    NotifierSettings.save();
                    rebuildWidgets();
                }
        ).bounds(left + cardWidth - 112, top - 28, 112, 20).build());

        addSection("NUKE", "nuke", left, top, cardWidth);
        addSection("CLONE", "clone", left, top + 105, cardWidth);
        addSection("ACCUMULATE", "accumulate", left, top + 210, cardWidth);

        addRenderableWidget(Button.builder(flashButtonText(), button -> {
            NotifierSettings.nukeFlashEnabled = !NotifierSettings.nukeFlashEnabled;
            NotifierSettings.save();
            rebuildWidgets();
        }).bounds(left, top + 318, cardWidth, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Refresh sound folders"), button -> {
            refreshLists();
            rebuildWidgets();
        }).bounds(left, top + 344, cardWidth, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Restore defaults"), button -> {
            NotifierSettings.restoreAll();
            rebuildWidgets();
        }).bounds(left, top + 370, cardWidth / 2 - 4, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(left + cardWidth / 2 + 4, top + 370, cardWidth / 2 - 4, 20).build());
    }

    private Component flashButtonText() {
        return Component.literal("Nuke screen flash  ·  "
                + (NotifierSettings.nukeFlashEnabled ? "ON" : "OFF"));
    }

    private void addSection(String title, String timer, int x, int y, int w) {
        addRenderableWidget(Button.builder(Component.literal("‹"), button -> {
            cycle(timer, -1);
            rebuildWidgets();
        }).bounds(x, y + 42, 28, 20).build());

        addRenderableWidget(Button.builder(Component.literal("›"), button -> {
            cycle(timer, 1);
            rebuildWidgets();
        }).bounds(x + 34, y + 42, 28, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Preview"),
                button -> IndependentAudio.preview(timer))
                .bounds(x + w - 88, y + 42, 88, 20).build());

        addRenderableWidget(new VolumeSlider(x, y + 70, w, 20, getVolume(timer), timer));
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
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        boolean light = NotifierSettings.lightTheme;
        int bg = light ? LIGHT_BG : DARK_BG;
        int card = light ? LIGHT_CARD : DARK_CARD;
        int main = light ? LIGHT_TEXT : DARK_TEXT;
        int muted = light ? LIGHT_MUTED : DARK_MUTED;
        int cardWidth = Math.min(500, width - 28);
        int left = (width - cardWidth) / 2;
        int top = Math.max(34, (height - 416) / 2);

        graphics.fill(0, 0, width, height, bg);
        Component heading = Component.literal("NUKE ALERT");
        graphics.centeredText(font, heading, width / 2, top - 25, main);
        Component subtitle = Component.literal("COOLDOWN NOTIFICATIONS");
        graphics.centeredText(font, subtitle, width / 2, top - 12, muted);

        drawCard(graphics, left, top, cardWidth, "NUKE", "nuke", card, main, muted);
        drawCard(graphics, left, top + 105, cardWidth, "CLONE", "clone", card, main, muted);
        drawCard(graphics, left, top + 210, cardWidth, "ACCUMULATE", "accumulate",
                card, main, muted);

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private void drawCard(GuiGraphicsExtractor graphics, int x, int y, int w,
                          String title, String timer, int card, int main, int muted) {
        graphics.fill(x - 8, y, x + w + 8, y + 98, card);
        graphics.fill(x - 8, y, x + w + 8, y + 2, ACCENT);
        graphics.text(font, Component.literal(title), x, y + 10, main);

        String selected = getSelected(timer);
        String shown = selected.isBlank() ? "Built-in tone" : selected;
        graphics.text(font, Component.literal("SOUND"), x, y + 29, muted);
        graphics.text(font, Component.literal(shown), x + 48, y + 29, main);

        List<String> sounds = listFor(timer);
        String count = sounds.size() + (sounds.size() == 1 ? " file available" : " files available");
        graphics.text(font, Component.literal(count), x, y + 50, muted);
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.gui.setScreen(parent);
    }

    private final class VolumeSlider extends AbstractSliderButton {
        private final String timer;

        VolumeSlider(int x, int y, int width, int height, double value, String timer) {
            super(x, y, width, height, Component.literal("Volume"), value);
            this.timer = timer;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal("Volume: " + Math.round(value * 100) + "%"));
        }

        @Override
        protected void applyValue() {
            setVolume(timer, (float) value);
            NotifierSettings.save();
        }
    }
}
