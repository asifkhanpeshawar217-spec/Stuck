# Nuke Alert

A client-side Fabric mod for Minecraft Java **1.21.11**.

It reads the server's player-list (Tab) footer even when the Tab screen is closed, tracks these cooldowns locally, and plays a notification when one becomes ready:

- Nuke
- Clone
- Accumulate

## Features

- Separate custom sounds for Nuke, Clone, and Accumulate
- MP3, WAV, AIFF, and AIF audio files
- In-game sound selector with previous/next selection
- Preview button for each timer
- Independent volume slider for each timer
- Refresh Sound Folders button
- Settings are saved and retained after restarting Minecraft
- Restore Defaults button (only changes settings when clicked)
- Separate desktop audio output, independent of Minecraft's in-game sound sliders

The operating system's output device and per-application volume controls can still affect the Java process.

## Open the settings screen

Press **Right Shift + N** while no other screen is open.

## Add your own sounds

Run the mod once. It creates these folders inside your Minecraft instance's `config` folder:

```text
.minecraft/
└── config/
    └── nuke-alert/
        ├── settings.json
        └── sounds/
            ├── nuke/
            ├── clone/
            └── accumulate/
```

Put your audio files in the matching folder. For example:

- `sounds/nuke/alert.mp3`
- `sounds/clone/clone-ready.mp3`
- `sounds/accumulate/finished.wav`

Open the settings screen and press **Refresh Sound Folders**. Use the left and right arrow buttons in each section to select a sound, then press **Preview** to test it.

If no custom file is selected, the mod plays its built-in tone.

## Timer format

The footer parser recognizes these examples:

```text
Nuke: 11m 59s
Clone: 3m 27s
Accumulate: Ready!
```

It also recognizes `Ready!` for Nuke and Clone.

**Important:** the server must send player-list footer updates to your client. The mod can estimate a countdown from the last received timer, but it cannot know about server-side timer changes that the server never sends.

## Build using GitHub Actions

1. Create a new, empty GitHub repository.
2. Extract this ZIP file.
3. Upload the **contents inside** the `NukeAlert-Fresh` folder to the root of your new repository. Do not upload the ZIP itself.
4. Make sure `.github/workflows/build.yml` is included. GitHub may hide `.github` in some file pickers; create that folder manually if necessary.
5. Open the **Actions** tab in your repository.
6. Select **Build Fabric mod** and run the workflow, or push a commit to the `main` branch.
7. When the workflow succeeds, open the run and download the `nuke-alert` artifact.
8. Inside the downloaded artifact, use `nuke-alert-1.0.0.jar` in your Minecraft `mods` folder.

## Requirements

- Minecraft Java 1.21.11
- Fabric Loader 0.20.1 or newer
- Fabric API for Minecraft 1.21.11
- Java 21

This project is configured to build with Gradle 9.2.0 through GitHub Actions.
