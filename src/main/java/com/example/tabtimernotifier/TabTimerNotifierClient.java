
package com.example.tabtimernotifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class TabTimerNotifierClient implements ClientModInitializer {

    private static KeyBinding openSettings;

    private static boolean previousRightShift;
    private static boolean hudEditMode;

    @Override
    public void onInitializeClient() {

        NotifierSettings.load();

        ClientTickEvents.END_CLIENT_TICK.register(
                TimerTracker::onClientTick
        );

        openSettings = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.tabtimernotifier.settings",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_N,
                        KeyBinding.Category.MISC
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.getWindow() == null) return;

            long window = client.getWindow().getHandle();

            boolean rightShift =
                    GLFW.glfwGetKey(
                            window,
                            GLFW.GLFW_KEY_RIGHT_SHIFT
                    ) == GLFW.GLFW_PRESS;

            boolean nPressed =
                    GLFW.glfwGetKey(
                            window,
                            GLFW.GLFW_KEY_N
                    ) == GLFW.GLFW_PRESS;

            // Right Shift + N opens the existing settings screen.
            while (openSettings.wasPressed()) {

                if (rightShift && client.currentScreen == null) {

                    hudEditMode = false;
                    TimerHud.setEditMode(false);

                    client.setScreen(
                            new SoundSettingsScreen(null)
                    );
                }
            }

            // Right Shift alone toggles HUD editing.
            if (rightShift
                    && !previousRightShift
                    && !nPressed
                    && client.currentScreen == null
                    && client.player != null) {

                hudEditMode = !hudEditMode;

                TimerHud.setEditMode(hudEditMode);

                if (hudEditMode) {
                    // Release Minecraft's captured mouse.
                    client.mouse.unlockCursor();
                } else {
                    // Return mouse control to Minecraft.
                    client.mouse.lockCursor();
                }
            }

            // If another screen opens, stop editing.
            if (client.currentScreen != null && hudEditMode) {
                hudEditMode = false;
                TimerHud.setEditMode(false);
            }

            previousRightShift = rightShift;
        });
    }
}
