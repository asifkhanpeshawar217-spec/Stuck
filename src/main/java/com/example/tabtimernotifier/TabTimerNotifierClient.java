package com.example.tabtimernotifier;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class TabTimerNotifierClient implements ClientModInitializer {
    private static KeyMapping openSettings;
    private static KeyMapping editHud;

    @Override
    public void onInitializeClient() {
        NotifierSettings.load();

        ClientTickEvents.END_CLIENT_TICK.register(TimerTracker::onClientTick);

        editHud = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.nukealert.edit_hud",
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyMapping.Category.MISC
        ));

        openSettings = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.tabtimernotifier.settings",
                GLFW.GLFW_KEY_N,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() == null) return;

            while (openSettings.consumeClick()) {
                boolean rightShiftDown = InputConstants.isKeyDown(
                        client.getWindow(),
                        GLFW.GLFW_KEY_RIGHT_SHIFT
                );
                if (rightShiftDown && client.gui.screen() == null) {
                    client.gui.setScreen(new SoundSettingsScreen(null));
                    return;
                }
            }

            while (editHud.consumeClick()) {
                if (client.gui.screen() == null && client.player != null) {
                    client.gui.setScreen(new HudEditorScreen(null));
                }
            }
        });

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("nukealert", "timer_hud"),
                (graphics, deltaTracker) -> {
                    TimerHud.render(graphics);
                    NukeFlash.render(graphics, graphics.guiWidth(), graphics.guiHeight());
                }
        );
    }
}
