
package com.example.tabtimernotifier;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class HudEditorScreen extends Screen {

    private final Screen parent;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("Nuke Alert HUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        TimerHud.setEditMode(true);
    }

    @Override
    public void render(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
        // Keep the game visible behind the HUD editor.
        TimerHud.render(context);

        context.drawTextWithShadow(
                textRenderer,
                Text.literal("HUD EDITOR"),
                10,
                10,
                0xFFFFFFFF
        );

        context.drawTextWithShadow(
                textRenderer,
                Text.literal("Drag to move | Bottom-right corner to resize"),
                10,
                22,
                0xFFCCCCCC
        );

        context.drawTextWithShadow(
                textRenderer,
                Text.literal("Right Shift: Finish editing"),
                10,
                34,
                0xFFCCCCCC
        );
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (TimerHud.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double deltaX,
            double deltaY
    ) {
        if (TimerHud.mouseDragged(
                mouseX,
                mouseY,
                button,
                deltaX,
                deltaY
        )) {
            return true;
        }

        return super.mouseDragged(
                mouseX,
                mouseY,
                button,
                deltaX,
                deltaY
        );
    }

    @Override
    public boolean mouseReleased(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (TimerHud.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            finishEditing();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_N
                && (modifiers & GLFW.GLFW_MOD_SHIFT) != 0) {

            client.setScreen(new SoundSettingsScreen(this));
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void finishEditing() {
        TimerHud.setEditMode(false);
        NotifierSettings.save();

        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public void close() {
        finishEditing();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
