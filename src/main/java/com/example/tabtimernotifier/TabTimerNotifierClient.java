
package com.example.tabtimernotifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class TabTimerNotifierClient implements ClientModInitializer {

    private static KeyBinding openSettings;
    private static KeyBinding editHud;

    @Override
    public void onInitializeClient() {

        NotifierSettings.load();

        ClientTickEvents.END_CLIENT_TICK.register(
                TimerTracker::onClientTick
        );

        editHud = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.nukealert.edit_hud",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_RIGHT_SHIFT,
                        KeyBinding.Category.MISC
                )
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

            boolean rightShiftDown =
                    GLFW.glfwGetKey(
                            window,
                            GLFW.GLFW_KEY_RIGHT_SHIFT
                    ) == GLFW.GLFW_PRESS;

            while (openSettings.wasPressed()) {
                if (rightShiftDown && client.currentScreen == null) {
                    client.setScreen(
                            new SoundSettingsScreen(null)
                    );
                    return;
                }
            }

            while (editHud.wasPressed()) {
                if (client.currentScreen == null
                        && client.player != null) {

                    client.setScreen(
                            new HudEditorScreen(null)
                    );
                }
            }
        });
    }
}
