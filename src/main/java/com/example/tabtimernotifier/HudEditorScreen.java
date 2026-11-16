package com.example.tabtimernotifier;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class HudEditorScreen extends Screen {
    private final Screen parent;

    public HudEditorScreen(Screen parent) {
        super(Component.literal("Nuke Alert HUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        TimerHud.setEditMode(true);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        TimerHud.render(graphics);
        graphics.text(font, Component.literal("HUD EDITOR"), 10, 10, 0xFFFFFFFF);
        graphics.text(font, Component.literal("Drag to move | Bottom-right corner to resize"),
                10, 22, 0xFFCCCCCC);
        graphics.text(font, Component.literal("Right Shift: Finish editing"), 10, 34, 0xFFCCCCCC);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (TimerHud.mouseClicked(event.x(), event.y(), event.button())) return true;
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (TimerHud.mouseDragged(event.x(), event.y(), event.button(), deltaX, deltaY)) return true;
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (TimerHud.mouseReleased(event.x(), event.y(), event.button())) return true;
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            finishEditing();
            return true;
        }

        if (event.key() == GLFW.GLFW_KEY_N
                && (event.modifiers() & GLFW.GLFW_MOD_SHIFT) != 0) {
            if (minecraft != null) minecraft.gui.setScreen(new SoundSettingsScreen(this));
            return true;
        }

        return super.keyPressed(event);
    }

    private void finishEditing() {
        TimerHud.setEditMode(false);
        NotifierSettings.save();
        if (minecraft != null) minecraft.gui.setScreen(parent);
    }

    @Override
    public void onClose() {
        finishEditing();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
