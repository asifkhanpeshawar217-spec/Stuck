
package com.example.tabtimernotifier;

import net.minecraft.client.gui.DrawContext;

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

    public static void render(DrawContext context, int width, int height) {
        if (!NotifierSettings.nukeFlashEnabled || startedAtMillis < 0L) return;

        long elapsed = System.currentTimeMillis() - startedAtMillis;

        if (elapsed >= TOTAL_DURATION_MS) {
            startedAtMillis = -1L;
            return;
        }

        // Three red flashes, each followed by a pause.
        if (elapsed < FLASH_CYCLE_MS * 3
                && elapsed % FLASH_CYCLE_MS < FLASH_ON_MS) {
            context.fill(0, 0, width, height, 0x99FF0000);
        }
    }
}
