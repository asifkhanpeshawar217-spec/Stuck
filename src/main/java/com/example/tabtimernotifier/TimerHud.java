
package com.example.tabtimernotifier;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class TimerHud {

    private static boolean editMode;
    private static boolean dragging;
    private static boolean resizing;

    private static double dragOffsetX;
    private static double dragOffsetY;

    private static double resizeStartX;
    private static double resizeStartY;

    private static int originalWidth;
    private static int originalHeight;

    private static final int BASE_WIDTH = 130;
    private static final int BASE_HEIGHT = 100;

    private static final int NUKE_COLOR = 0xFFFF6262;
    private static final int CLONE_COLOR = 0xFFB69AFF;
    private static final int TIMER_COLOR = 0xFFFFFFFF;

    private TimerHud() {}

    public static void setEditMode(boolean enabled) {
        editMode = enabled;

        if (!enabled) {
            dragging = false;
            resizing = false;
        }
    }

    public static boolean isEditMode() {
        return editMode;
    }

    public static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (!NotifierSettings.hudEnabled) return;
        if (client.player == null || client.world == null) return;

        boolean showNuke = NotifierSettings.hudShowNuke
                && TimerTracker.hasNukeData();

        boolean showClone = NotifierSettings.hudShowClone
                && TimerTracker.hasCloneData();

        if (!showNuke && !showClone && !editMode) return;

        int x = NotifierSettings.hudX;
        int y = NotifierSettings.hudY;

        int width = clamp(NotifierSettings.hudWidth, 80, 500);
        int height = clamp(NotifierSettings.hudHeight, 55, 400);

        float widthRatio = (float) width / BASE_WIDTH;
        float heightRatio = (float) height / BASE_HEIGHT;

        // Uniform scaling keeps the text proportions natural.
        float resizeScale = (float) Math.sqrt(
                widthRatio * heightRatio
        );

        float textScale = resizeScale
                * NotifierSettings.hudTextSize;

        int padding = Math.max(
                3,
                Math.round(8 * resizeScale)
        );

        int alpha = clamp(
                Math.round(NotifierSettings.hudOpacity * 255),
                0,
                255
        );

        int background = (alpha << 24) | 0x00101018;

        context.fill(
                x,
                y,
                x + width,
                y + height,
                background
        );

        if (editMode) {
            drawBorder(context, x, y, width, height);

            context.fill(
                    x + width - 10,
                    y + height - 10,
                    x + width,
                    y + height,
                    0xFFFFFFFF
            );
        }

        int textX = x + padding;
        int textY = y + padding;

        if (showNuke || editMode && NotifierSettings.hudShowNuke) {
            drawTimer(
                    context,
                    client,
                    "NUKE",
                    NUKE_COLOR,
                    TimerTracker.isNukeActive(),
                    TimerTracker.getNukeRemainingSeconds(),
                    textX,
                    textY,
                    textScale
            );

            textY += Math.round(
                    (35 + NotifierSettings.hudSpacing) * resizeScale
            );
        }

        if (showClone || editMode && NotifierSettings.hudShowClone) {
            drawTimer(
                    context,
                    client,
                    "CLONE",
                    CLONE_COLOR,
                    TimerTracker.isCloneActive(),
                    TimerTracker.getCloneRemainingSeconds(),
                    textX,
                    textY,
                    textScale
            );
        }
    }

    private static void drawBorder(
            DrawContext context,
            int x,
            int y,
            int width,
            int height
    ) {
        int color = 0xFFFFFFFF;

        context.fill(x, y, x + width, y + 1, color);
        context.fill(
                x,
                y + height - 1,
                x + width,
                y + height,
                color
        );
        context.fill(x, y, x + 1, y + height, color);
        context.fill(
                x + width - 1,
                y,
                x + width,
                y + height,
                color
        );
    }

    private static void drawTimer(
            DrawContext context,
            MinecraftClient client,
            String name,
            int color,
            boolean active,
            long seconds,
            int x,
            int y,
            float scale
    ) {
        context.getMatrices().pushMatrix();

        context.getMatrices().translate((float) x, (float) y);
        context.getMatrices().scale(scale, scale);

        context.drawTextWithShadow(
                client.textRenderer,
                Text.literal(name),
                0,
                0,
                color
        );

        String timer = active
                ? formatTime(seconds)
                : "READY";

        context.drawTextWithShadow(
                client.textRenderer,
                Text.literal(timer),
                0,
                12,
                TIMER_COLOR
        );

        context.getMatrices().popMatrix();
    }

    public static boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (!editMode || button != 0) return false;

        int x = NotifierSettings.hudX;
        int y = NotifierSettings.hudY;

        int width = clamp(NotifierSettings.hudWidth, 80, 500);
        int height = clamp(NotifierSettings.hudHeight, 55, 400);

        // Bottom-right handle starts resizing.
        if (mouseX >= x + width - 14
                && mouseX <= x + width
                && mouseY >= y + height - 14
                && mouseY <= y + height) {

            resizing = true;
            dragging = false;

            resizeStartX = mouseX;
            resizeStartY = mouseY;

            originalWidth = width;
            originalHeight = height;

            return true;
        }

        // Clicking anywhere else inside the HUD starts dragging.
        if (mouseX >= x && mouseX <= x + width
                && mouseY >= y && mouseY <= y + height) {

            dragging = true;
            resizing = false;

            dragOffsetX = mouseX - x;
            dragOffsetY = mouseY - y;

            return true;
        }

        return false;
    }

    public static boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double deltaX,
            double deltaY
    ) {
        if (!editMode || button != 0) return false;

        if (dragging) {
            NotifierSettings.hudX =
                    (int) (mouseX - dragOffsetX);

            NotifierSettings.hudY =
                    (int) (mouseY - dragOffsetY);

            return true;
        }

        if (resizing) {
            NotifierSettings.hudWidth = clamp(
                    (int) (originalWidth + mouseX - resizeStartX),
                    80,
                    500
            );

            NotifierSettings.hudHeight = clamp(
                    (int) (originalHeight + mouseY - resizeStartY),
                    55,
                    400
            );

            return true;
        }

        return false;
    }

    public static boolean mouseReleased(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button != 0) return false;

        boolean wasEditing = dragging || resizing;

        if (wasEditing) {
            dragging = false;
            resizing = false;

            NotifierSettings.save();
            return true;
        }

        return false;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String formatTime(long seconds) {
        long minutes = seconds / 60;
        long remaining = seconds % 60;

        return String.format("%02d:%02d", minutes, remaining);
    }
}
