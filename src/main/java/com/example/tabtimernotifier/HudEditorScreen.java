
package com.example.tabtimernotifier;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.MouseInput;
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
    public boolean mouseClicked(MouseInput click, boolean doubleClick) {
        if (TimerHud.mouseClicked(
                click.x(),
                click.y(),
                click.button()
        )) {
            return true;
        }

        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public boolean mouseDragged(
            MouseInput click,
            double deltaX,
            double deltaY
    ) {
        if (TimerHud.mouseDragged(
                click.x(),
                click.y(),
                click.button(),
                deltaX,
                deltaY
        )) {
            return true;
        }

        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseInput click) {
        if (TimerHud.mouseReleased(
                click.x(),
                click.y(),
                click.button()
        )) {
            return true;
        }

        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.key();

        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            finishEditing();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_N
                && (input.modifiers() & GLFW.GLFW_MOD_SHIFT) != 0) {

            if (client != null) {
                client.setScreen(new SoundSettingsScreen(this));
            }

            return true;
        }

        return super.keyPressed(input);
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
