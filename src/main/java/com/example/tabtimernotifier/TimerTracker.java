
package com.example.tabtimernotifier;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reads server-sent player-list footer text and tracks cooldowns locally. */
public final class TimerTracker {

    private static final Pattern NUKE_LINE =
            Pattern.compile("(?i)\\bNuke\\s*:\\s*(.*)");

    private static final Pattern CLONE_LINE =
            Pattern.compile("(?i)\\bClone\\s*:\\s*(.*)");

    private static final Pattern ACCUMULATE_LINE =
            Pattern.compile("(?i)\\bAccumulate\\s*:\\s*(.*)");

    private static final Pattern TIME =
            Pattern.compile("(?i)(\\d+)\\s*m\\s*(\\d+)\\s*s");

    private static final Pattern READY =
            Pattern.compile("(?i)\\bready\\s*!?");

    private static final Cooldown NUKE = new Cooldown("Nuke");
    private static final Cooldown CLONE = new Cooldown("Clone");
    private static final Cooldown ACCUMULATE = new Cooldown("Accumulate");

    private TimerTracker() {}

    public static void onFooterReceived(String footer) {
        if (footer == null || footer.isBlank()) return;

        String normalized = footer.replace('\r', '\n');

        updateFromLine(normalized, NUKE_LINE, NUKE);
        updateFromLine(normalized, CLONE_LINE, CLONE);
        updateFromLine(normalized, ACCUMULATE_LINE, ACCUMULATE);
    }

    private static void updateFromLine(
            String footer, Pattern linePattern, Cooldown cooldown) {

        Matcher lineMatcher = linePattern.matcher(footer);

        if (!lineMatcher.find()) return;

        String value = lineMatcher.group(1)
                .trim()
                .toLowerCase(Locale.ROOT);

        Matcher timeMatcher = TIME.matcher(value);

        if (timeMatcher.find()) {
            long seconds =
                    Long.parseLong(timeMatcher.group(1)) * 60L
                    + Long.parseLong(timeMatcher.group(2));

            cooldown.updateTimer(seconds);

        } else if (READY.matcher(value).find()) {
            cooldown.updateReady();
        }
    }

    public static void onClientTick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        long now = System.nanoTime();

        NUKE.tick(now, client);
        CLONE.tick(now, client);
        ACCUMULATE.tick(now, client);
    }

    private static final class Cooldown {

        private final String name;

        private boolean initialized;
        private boolean active;
        private boolean notified;

        private long readyAtNanos;

        private Cooldown(String name) {
            this.name = name;
        }

        private void updateTimer(long seconds) {
            long now = System.nanoTime();

            if (seconds > 0) {
                readyAtNanos =
                        now + seconds * 1_000_000_000L;

                active = true;
                notified = false;
                initialized = true;

            } else {
                updateReady();
            }
        }

        private void updateReady() {
            if (!initialized) {
                initialized = true;
                active = false;
                notified = true;
                return;
            }

            if (active && !notified) {
                playNotification(MinecraftClient.getInstance());
                notified = true;
            }

            active = false;
        }

        private void tick(long now, MinecraftClient client) {
            if (active && !notified && now >= readyAtNanos) {
                playNotification(client);

                notified = true;
                active = false;
            }
        }

        private void playNotification(MinecraftClient client) {
            if (client.player != null) {

                // Only Nuke triggers the screen flash.
                if ("Nuke".equals(name)) {
                    NukeFlash.trigger();
                }

                // Existing sound notification.
                IndependentAudio.play(name);

                client.player.sendMessage(
                        Text.literal(name + " is ready!"),
                        true
                );
            }
        }
    }
}
