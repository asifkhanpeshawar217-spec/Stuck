package com.example.tabtimernotifier.mixin;

import com.example.tabtimernotifier.TimerTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundTabListPacket.class)
public abstract class PlayerListHeaderPacketMixin {
    @Inject(method = "handle", at = @At("HEAD"))
    private void nukealert$readFooter(ClientGamePacketListener listener, CallbackInfo ci) {
        ClientboundTabListPacket packet = (ClientboundTabListPacket) (Object) this;
        String footer = packet.footer().getString();
        Minecraft.getInstance().execute(() -> TimerTracker.onFooterReceived(footer));
    }
}
