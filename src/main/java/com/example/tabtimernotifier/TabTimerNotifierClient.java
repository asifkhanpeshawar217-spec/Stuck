package com.example.tabtimernotifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class TabTimerNotifierClient implements ClientModInitializer {
    private static KeyBinding openSettings;

    @Override
    public void onInitializeClient() {
        NotifierSettings.load();
        ClientTickEvents.END_CLIENT_TICK.register(TimerTracker::onClientTick);

        openSettings = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.tabtimernotifier.settings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openSettings.wasPressed()) {
                boolean rightShift = GLFW.glfwGetKey(
                        client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT
                ) == GLFW.GLFW_PRESS;

                if (rightShift && client.currentScreen == null) {
                    client.setScreen(new SoundSettingsScreen(null));
                }
            }
        });
    }
}
