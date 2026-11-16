# Nuke Alert — Minecraft 26.2

A client-side Fabric mod for Minecraft Java **26.2**.

It reads the server's player-list footer, tracks cooldowns locally, and plays notifications when timers become ready:

- Nuke
- Clone
- Accumulate

## Features

- Nuke and Clone countdown HUD, visible without holding Tab
- Text-only HUD with no progress bars
- Automatic background sizing with small padding
- Right Shift HUD editor: drag to move and resize from the bottom-right corner
- Saved HUD position and custom dimensions
- Premium Minimal settings screen with dark and light themes
- Separate custom sounds for Nuke, Clone, and Accumulate
- MP3, WAV, AIFF, and AIF audio files
- In-game sound selector, preview buttons, and independent volume sliders
- Refresh Sound Folders button
- Optional Nuke screen flash
- Persistent settings and Restore Defaults

## Controls

- **Right Shift** — open the HUD editor
- **Drag the HUD** — move it
- **Drag the bottom-right handle** — resize it
- **Right Shift** — finish editing
- **Right Shift + N** — open settings when no other screen is open

## Custom sounds

Run the mod once. It creates these folders inside your Minecraft instance's `config/tab-timer-notifier/sounds` directory:

```text
sounds/
├── nuke/
├── clone/
└── accumulate/
```

Place audio files in the matching folder, open settings, and press **Refresh sound folders**. Use the arrow buttons to select a sound and **Preview** to test it. If no custom file is selected, the built-in tone is used.

## Timer format

The footer parser recognizes examples such as:

```text
Nuke: 11m 59s
Clone: 3m 27s
Accumulate: Ready!
```

It also recognizes `Ready!` for Nuke and Clone.

The server must send player-list footer updates to your client. The mod estimates a countdown from the last received timer, but cannot know about server-side changes that the server never sends.

## Requirements

- Minecraft Java 26.2
- Fabric Loader 0.20.1 or newer
- Fabric API for Minecraft 26.2
- Java 25 or newer

## Build

The GitHub Actions workflow builds the mod automatically for this branch. Open the **Actions** tab, select the latest successful run, and download the `nuke-alert-minecraft-26.2` artifact. Place the JAR in your Minecraft `mods` folder.

The 1.21.11 version remains separate and unchanged on its own development branch.
