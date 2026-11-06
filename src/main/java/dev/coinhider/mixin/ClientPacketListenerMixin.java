package dev.coinhider.mixin;

import dev.coinhider.CoinHider;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skips the "item flies into the player" pickup animation (and its pop) for hidden coins.
 * Mirrors vanilla's handling for item entities (shrink, remove) so the client stays in sync with the server.
 * Only this one client-side sound is dropped; every other sound is untouched.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Shadow private ClientLevel level;

    @Inject(method = "handleTakeItemEntity",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V",
                    shift = At.Shift.AFTER),
            cancellable = true)
    private void coinhider$skipCoinPickupAnimation(ClientboundTakeItemEntityPacket packet, CallbackInfo ci) {
        Entity entity = this.level.getEntity(packet.getItemId());
        if (!(entity instanceof ItemEntity item) || !CoinHider.shouldHide(entity)) return;

        // Vanilla does nothing when the collector is unknown - leave that case to vanilla.
        if (this.level.getEntity(packet.getPlayerId()) == null) return;

        // No ItemPickupParticle and no pickup sound for coins - everything else stays vanilla.
        ItemStack stack = item.getItem();
        if (!stack.isEmpty()) stack.shrink(packet.getAmount());
        if (stack.isEmpty()) this.level.removeEntity(packet.getItemId(), Entity.RemovalReason.DISCARDED);
        ci.cancel();
    }
}
