
package com.example.tabtimernotifier.mixin;

import com.example.tabtimernotifier.NukeFlash;
import com.example.tabtimernotifier.TimerHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudFlashMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void nukealert$renderHudAndFlash(
            DrawContext context,
            RenderTickCounter tickCounter,
            CallbackInfo ci
    ) {
        // Draw the timer HUD.
        TimerHud.render(context);

        // Preserve the existing Nuke screen flash.
        NukeFlash.render(
                context,
                context.getScaledWindowWidth(),
                context.getScaledWindowHeight()
        );
    }
}
