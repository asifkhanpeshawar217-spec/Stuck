
package com.example.tabtimernotifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class TabTimerNotifierClient implements ClientModInitializer {

    private static KeyBinding openSettings;

    private static boolean previousRightShift = false;
    private static boolean hudEditMode = false;

    @Override
    public void onInitializeClient() {

        // Load saved settings.
        NotifierSettings.load();

        // Keep the existing timer notification system.
        ClientTickEvents.END_CLIENT_TICK.register(TimerTracker::onClientTick);

        // Existing settings shortcut: Right Shift + N.
        openSettings = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.tabtimernotifier.settings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            long window = client.getWindow().getHandle();

            boolean rightShift =
                    GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT)
                            == GLFW.GLFW_PRESS;

            boolean nPressed =
                    GLFW.glfwGetKey(window, GLFW.GLFW_KEY_N)
                            == GLFW.GLFW_PRESS;

            // Right Shift + N opens settings.
            while (openSettings.wasPressed()) {
                if (rightShift && client.currentScreen == null) {
                    client.setScreen(new SoundSettingsScreen(null));
                }
            }

            // Right Shift alone toggles HUD edit mode.
            if (rightShift && !previousRightShift
                    && !nPressed
                    && client.currentScreen == null) {

                hudEditMode = !hudEditMode;
            }

            previousRightShift = rightShift;

            // Pass editing state to the HUD.
            TimerHud.setEditMode(hudEditMode);
        });
    }
}
