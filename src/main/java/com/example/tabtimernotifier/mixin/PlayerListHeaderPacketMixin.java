package com.example.tabtimernotifier.mixin;

import com.example.tabtimernotifier.TimerTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerListHeaderS2CPacket.class)
public abstract class PlayerListHeaderPacketMixin {
    @Inject(method = "apply", at = @At("HEAD"))
    private void tabtimernotifier$readFooter(ClientPlayPacketListener listener, CallbackInfo ci) {
        PlayerListHeaderS2CPacket packet = (PlayerListHeaderS2CPacket) (Object) this;
        String footer = packet.footer().getString();
        MinecraftClient.getInstance().execute(() -> TimerTracker.onFooterReceived(footer));
    }
}
