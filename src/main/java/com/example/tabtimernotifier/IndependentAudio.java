package com.example.tabtimernotifier;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class IndependentAudio {
    private IndependentAudio() {}

    public static void play(String type) {
        String timer = type == null ? "" : type.toLowerCase();
        String file;
        float volume;
        double pitch;

        switch (timer) {
            case "nuke" -> {
                file = NotifierSettings.nukeFile;
                volume = NotifierSettings.nukeVolume;
                pitch = 1.0;
            }
            case "accumulate" -> {
                file = NotifierSettings.accumulateFile;
                volume = NotifierSettings.accumulateVolume;
                pitch = 1.5;
            }
            default -> {
                file = NotifierSettings.cloneFile;
                volume = NotifierSettings.cloneVolume;
                pitch = 1.25;
            }
        }
        playFile(timer, file, volume, pitch);
    }

    public static void preview(String timer) {
        String key = timer.toLowerCase();
        String file;
        float volume;
        switch (key) {
            case "nuke" -> {
                file = NotifierSettings.nukeFile;
                volume = NotifierSettings.nukeVolume;
            }
            case "accumulate" -> {
                file = NotifierSettings.accumulateFile;
                volume = NotifierSettings.accumulateVolume;
            }
            default -> {
                file = NotifierSettings.cloneFile;
                volume = NotifierSettings.cloneVolume;
            }
        }
        playFile(key, file, volume, 1.0);
    }

    private static void playFile(String timer, String fileName, float volume, double pitch) {
        if (volume <= 0.0f) return;

        Thread thread = new Thread(() -> {
            try {
                Path path = NotifierSettings.soundPath(timer, fileName);
                if (path == null || !Files.isRegularFile(path)) {
                    tone(volume, pitch);
                    return;
                }

                try (AudioInputStream input = AudioSystem.getAudioInputStream(
                        new BufferedInputStream(Files.newInputStream(path)))) {
                    AudioFormat source = input.getFormat();
                    AudioFormat pcm = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            source.getSampleRate(),
                            16,
                            source.getChannels(),
                            source.getChannels() * 2,
                            source.getSampleRate(),
                            false
                    );
                    try (AudioInputStream decoded = AudioSystem.getAudioInputStream(pcm, input)) {
                        stream(decoded, pcm, volume);
                    }
                }
            } catch (Exception e) {
                System.err.println("[Tab Timer Notifier] Audio playback failed: " + e.getMessage());
                tone(volume, pitch);
            }
        }, "TabTimerNotifier-Audio");
        thread.setDaemon(true);
        thread.start();
    }

    private static void stream(AudioInputStream input, AudioFormat format, float volume) throws Exception {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
            line.open(format);
            line.start();
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) {
                for (int i = 0; i + 1 < count; i += 2) {
                    short sample = (short) ((buffer[i] & 0xff) | (buffer[i + 1] << 8));
                    int adjusted = Math.max(-32768, Math.min(32767, (int) (sample * volume)));
                    buffer[i] = (byte) adjusted;
                    buffer[i + 1] = (byte) (adjusted >> 8);
                }
                line.write(buffer, 0, count);
            }
            line.drain();
        }
    }

    private static void tone(float volume, double pitch) {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            byte[] data = new byte[29400];
            for (int i = 0; i < data.length / 2; i++) {
                double envelope = 1.0 - (double) i / (data.length / 2);
                short sample = (short) (Math.sin(2 * Math.PI * 660 * pitch * i / 44100)
                        * 12000 * volume * envelope);
                data[i * 2] = (byte) sample;
                data[i * 2 + 1] = (byte) (sample >> 8);
            }
            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(
                    new DataLine.Info(SourceDataLine.class, format))) {
                line.open(format);
                line.start();
                line.write(data, 0, data.length);
                line.drain();
            }
        } catch (Exception e) {
            System.err.println("[Tab Timer Notifier] No audio output: " + e.getMessage());
        }
    }
}
