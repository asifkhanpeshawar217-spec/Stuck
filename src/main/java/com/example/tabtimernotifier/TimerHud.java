package com.example.tabtimernotifier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class TimerHud {
    private static boolean editMode;
    private static boolean dragging;
    private static boolean resizing;
    private static double dragOffsetX, dragOffsetY;
    private static double resizeStartX, resizeStartY;
    private static int originalWidth, originalHeight;

    private static final int NUKE_COLOR = 0xFFFF6262;
    private static final int CLONE_COLOR = 0xFFB69AFF;
    private static final int TIMER_COLOR = 0xFFFFFFFF;
    private static final int PAD = 6;
    private static final int LINE_HEIGHT = 25;

    private TimerHud() {}

    public static void setEditMode(boolean enabled) {
        editMode = enabled;
        if (!enabled) {
            dragging = false;
            resizing = false;
        }
    }

    public static boolean isEditMode() { return editMode; }

    public static void render(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        if (!NotifierSettings.hudEnabled || client.player == null || client.level == null) return;

        boolean showNuke = NotifierSettings.hudShowNuke
                && (TimerTracker.hasNukeData() || editMode);
        boolean showClone = NotifierSettings.hudShowClone
                && (TimerTracker.hasCloneData() || editMode);
        if (!showNuke && !showClone) return;

        int count = (showNuke ? 1 : 0) + (showClone ? 1 : 0);
        float baseScale = NotifierSettings.hudTextSize;
        int contentWidth = 0;
        if (showNuke) {
            contentWidth = Math.max(contentWidth, Math.max(
                    client.font.width("NUKE"),
                    client.font.width(timerText(TimerTracker.isNukeActive(),
                            TimerTracker.getNukeRemainingSeconds()))));
        }
        if (showClone) {
            contentWidth = Math.max(contentWidth, Math.max(
                    client.font.width("CLONE"),
                    client.font.width(timerText(TimerTracker.isCloneActive(),
                            TimerTracker.getCloneRemainingSeconds()))));
        }

        int autoWidth = (int) Math.ceil(contentWidth * baseScale) + PAD * 2;
        int gap = Math.max(0, NotifierSettings.hudSpacing);
        int autoHeight = (int) Math.ceil((count * LINE_HEIGHT
                + (count > 1 ? gap : 0)) * baseScale) + PAD * 2;

        int width = autoWidth;
        int height = autoHeight;
        float textScale = baseScale;
        if (NotifierSettings.hudCustomSize) {
            width = clamp(NotifierSettings.hudWidth, 60, 500);
            height = clamp(NotifierSettings.hudHeight, 40, 400);
            float widthRatio = (float) width / Math.max(1, autoWidth);
            float heightRatio = (float) height / Math.max(1, autoHeight);
            textScale *= Math.max(0.5f, Math.min(2.0f,
                    (float) Math.sqrt(widthRatio * heightRatio)));
        }

        int x = NotifierSettings.hudX;
        int y = NotifierSettings.hudY;
        int alpha = clamp(Math.round(NotifierSettings.hudOpacity * 255), 0, 255);
        graphics.fill(x, y, x + width, y + height, (alpha << 24) | 0x00101018);

        if (editMode) {
            drawBorder(graphics, x, y, width, height);
            graphics.fill(x + width - 9, y + height - 9, x + width, y + height, 0xFFFFFFFF);
        }

        int textX = x + PAD;
        int textY = y + PAD;
        if (showNuke) {
            drawTimer(graphics, client, "NUKE", NUKE_COLOR,
                    TimerTracker.isNukeActive(), TimerTracker.getNukeRemainingSeconds(),
                    textX, textY, textScale);
            textY += Math.round(LINE_HEIGHT * textScale) + gap;
        }
        if (showClone) {
            drawTimer(graphics, client, "CLONE", CLONE_COLOR,
                    TimerTracker.isCloneActive(), TimerTracker.getCloneRemainingSeconds(),
                    textX, textY, textScale);
        }
    }

    private static String timerText(boolean active, long seconds) {
        return active ? formatTime(seconds) : "READY";
    }

    private static void drawBorder(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + 1, 0xFFFFFFFF);
        graphics.fill(x, y + height - 1, x + width, y + height, 0xFFFFFFFF);
        graphics.fill(x, y, x + 1, y + height, 0xFFFFFFFF);
        graphics.fill(x + width - 1, y, x + width, y + height, 0xFFFFFFFF);
    }

    private static void drawTimer(GuiGraphicsExtractor graphics, Minecraft client,
                                  String name, int color, boolean active, long seconds,
                                  int x, int y, float scale) {
        graphics.pose().pushMatrix();
        graphics.pose().translate((float) x, (float) y);
        graphics.pose().scale(scale, scale);
        graphics.text(client.font, Component.literal(name), 0, 0, color);
        graphics.text(client.font, Component.literal(timerText(active, seconds)), 0, 12, TIMER_COLOR);
        graphics.pose().popMatrix();
    }

    public static boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!editMode || button != 0) return false;
        int x = NotifierSettings.hudX, y = NotifierSettings.hudY;
        int width = currentWidth(), height = currentHeight();
        if (mouseX >= x + width - 14 && mouseX <= x + width
                && mouseY >= y + height - 14 && mouseY <= y + height) {
            resizing = true;
            dragging = false;
            resizeStartX = mouseX;
            resizeStartY = mouseY;
            originalWidth = width;
            originalHeight = height;
            return true;
        }
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            dragging = true;
            resizing = false;
            dragOffsetX = mouseX - x;
            dragOffsetY = mouseY - y;
            return true;
        }
        return false;
    }

    public static boolean mouseDragged(double mouseX, double mouseY, int button,
                                       double deltaX, double deltaY) {
        if (!editMode || button != 0) return false;
        if (dragging) {
            NotifierSettings.hudX = (int) (mouseX - dragOffsetX);
            NotifierSettings.hudY = (int) (mouseY - dragOffsetY);
            return true;
        }
        if (resizing) {
            NotifierSettings.hudCustomSize = true;
            NotifierSettings.hudWidth = clamp((int) (originalWidth + mouseX - resizeStartX), 60, 500);
            NotifierSettings.hudHeight = clamp((int) (originalHeight + mouseY - resizeStartY), 40, 400);
            return true;
        }
        return false;
    }

    public static boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        boolean changed = dragging || resizing;
        if (changed) {
            dragging = false;
            resizing = false;
            NotifierSettings.save();
            return true;
        }
        return false;
    }

    private static int currentWidth() {
        return NotifierSettings.hudCustomSize
                ? clamp(NotifierSettings.hudWidth, 60, 500) : autoWidth();
    }

    private static int currentHeight() {
        return NotifierSettings.hudCustomSize
                ? clamp(NotifierSettings.hudHeight, 40, 400) : autoHeight();
    }

    private static int autoWidth() {
        Minecraft client = Minecraft.getInstance();
        int width = 0;
        if (isNukeVisibleForLayout()) {
            width = Math.max(width, Math.max(client.font.width("NUKE"),
                    client.font.width(timerText(TimerTracker.isNukeActive(),
                            TimerTracker.getNukeRemainingSeconds()))));
        }
        if (isCloneVisibleForLayout()) {
            width = Math.max(width, Math.max(client.font.width("CLONE"),
                    client.font.width(timerText(TimerTracker.isCloneActive(),
                            TimerTracker.getCloneRemainingSeconds()))));
        }
        return (int) Math.ceil(width * NotifierSettings.hudTextSize) + PAD * 2;
    }

    private static int autoHeight() {
        int count = (isNukeVisibleForLayout() ? 1 : 0)
                + (isCloneVisibleForLayout() ? 1 : 0);
        float scale = NotifierSettings.hudTextSize;
        return (int) Math.ceil((count * LINE_HEIGHT
                + (count > 1 ? NotifierSettings.hudSpacing : 0)) * scale) + PAD * 2;
    }

    private static boolean isNukeVisibleForLayout() {
        return NotifierSettings.hudShowNuke && (editMode || TimerTracker.hasNukeData());
    }

    private static boolean isCloneVisibleForLayout() {
        return NotifierSettings.hudShowClone && (editMode || TimerTracker.hasCloneData());
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String formatTime(long seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
}
