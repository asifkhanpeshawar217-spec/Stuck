
package com.example.tabtimernotifier;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class TimerHud {

    private static boolean editMode = false;
    private static boolean dragging = false;
    private static boolean resizing = false;

    private static double dragOffsetX;
    private static double dragOffsetY;

    private static double resizeStartX;
    private static double resizeStartY;

    private static int originalWidth;
    private static int originalHeight;

    private static boolean wasLeftMouseDown = false;

    private static final int PADDING = 8;

    private static final int NUKE_COLOR = 0xFFFF6262;
    private static final int CLONE_COLOR = 0xFFB69AFF;
    private static final int TIMER_COLOR = 0xFFFFFFFF;

    private TimerHud() {}

    public static void setEditMode(boolean enabled) {
        if (editMode != enabled) {
            editMode = enabled;
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

        int width = NotifierSettings.hudWidth;
        int height = NotifierSettings.hudHeight;

        int alpha = Math.round(NotifierSettings.hudOpacity * 255);
        int background = (alpha << 24) | 0x00101018;

        context.fill(x, y, x + width, y + height, background);

        if (editMode) {
            context.drawBorder(
                    x, y, width, height,
                    0xFFFFFFFF
            );

            context.drawTextWithShadow(
                    client.textRenderer,
                    Text.literal("HUD EDIT MODE"),
                    x + 5,
                    y - 12,
                    0xFFFFFFFF
            );

            context.fill(
                    x + width - 8,
                    y + height - 8,
                    x + width,
                    y + height,
                    0xFFFFFFFF
            );
        }

        int textX = x + PADDING;
        int textY = y + PADDING;

        if (showNuke) {
            drawTimer(
                    context,
                    client,
                    "NUKE",
                    NUKE_COLOR,
                    TimerTracker.isNukeActive(),
                    TimerTracker.getNukeRemainingSeconds(),
                    textX,
                    textY
            );

            textY += 35 + NotifierSettings.hudSpacing;
        }

        if (showClone) {
            drawTimer(
                    context,
                    client,
                    "CLONE",
                    CLONE_COLOR,
                    TimerTracker.isCloneActive(),
                    TimerTracker.getCloneRemainingSeconds(),
                    textX,
                    textY
            );
        }

        if (editMode) {
            handleMouse(client, x, y, width, height);
        }
    }

    private static void drawTimer(
            DrawContext context,
            MinecraftClient client,
            String name,
            int color,
            boolean active,
            long seconds,
            int x,
            int y
    ) {
        context.getMatrices().push();

        float scale = NotifierSettings.hudTextSize;
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scale, scale, 1);

        context.drawTextWithShadow(
                client.textRenderer,
                Text.literal(name),
                0,
                0,
                color
        );

        String timer = active ? formatTime(seconds) : "READY";

        context.drawTextWithShadow(
                client.textRenderer,
                Text.literal(timer),
                0,
                12,
                TIMER_COLOR
        );

        context.getMatrices().pop();
    }

    private static void handleMouse(
            MinecraftClient client,
            int x,
            int y,
            int width,
            int height
    ) {
        long window = client.getWindow().getHandle();

        double mouseX = client.mouse.getX()
                * client.getWindow().getScaledWidth()
                / client.getWindow().getWidth();

        double mouseY = client.mouse.getY()
                * client.getWindow().getScaledHeight()
                / client.getWindow().getHeight();

        boolean mouseDown =
                GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT)
                        == GLFW.GLFW_PRESS;

        boolean justPressed = mouseDown && !wasLeftMouseDown;
        boolean released = !mouseDown && wasLeftMouseDown;

        if (justPressed) {
            if (mouseX >= x + width - 12
                    && mouseX <= x + width
                    && mouseY >= y + height - 12
                    && mouseY <= y + height) {

                resizing = true;
                dragging = false;

                resizeStartX = mouseX;
                resizeStartY = mouseY;

                originalWidth = width;
                originalHeight = height;

            } else if (mouseX >= x && mouseX <= x + width
                    && mouseY >= y && mouseY <= y + height) {

                dragging = true;
                resizing = false;

                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
            }
        }

        if (mouseDown && dragging) {
            NotifierSettings.hudX = (int) (mouseX - dragOffsetX);
            NotifierSettings.hudY = (int) (mouseY - dragOffsetY);
        }

        if (mouseDown && resizing) {
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
        }

        if (released) {
            if (dragging || resizing) {
                NotifierSettings.save();
            }

            dragging = false;
            resizing = false;
        }

        wasLeftMouseDown = mouseDown;
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
