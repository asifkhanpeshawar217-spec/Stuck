package com.example.tabtimernotifier;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class NukeFlash {
    private static long startedAtMillis = -1L;
    private static final long TOTAL_DURATION_MS = 2000L;
    private static final long FLASH_CYCLE_MS = 600L;
    private static final long FLASH_ON_MS = 300L;

    private NukeFlash() {}

    public static void trigger() {
        if (!NotifierSettings.nukeFlashEnabled) return;
        startedAtMillis = System.currentTimeMillis();
    }

    public static void render(GuiGraphicsExtractor graphics, int width, int height) {
        if (!NotifierSettings.nukeFlashEnabled || startedAtMillis < 0L) return;
        long elapsed = System.currentTimeMillis() - startedAtMillis;
        if (elapsed >= TOTAL_DURATION_MS) {
            startedAtMillis = -1L;
            return;
        }
        if (elapsed < FLASH_CYCLE_MS * 3 && elapsed % FLASH_CYCLE_MS < FLASH_ON_MS) {
            graphics.fill(0, 0, width, height, 0x99FF0000);
        }
    }
}
