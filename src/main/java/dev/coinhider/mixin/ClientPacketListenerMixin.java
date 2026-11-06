package dev.coinhider.mixin;

import dev.coinhider.CoinHider;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skips the "item flies into the player" pickup animation for hidden coins.
 * Mirrors vanilla's handling for item entities (pickup sound, shrink, remove) minus the ItemPickupParticle,
 * so the client stays in sync with the server.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Shadow private ClientLevel level;
    @Shadow @Final private RandomSource random;

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

        this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
                0.2F, (this.random.nextFloat() - this.random.nextFloat()) * 1.4F + 2.0F, false);
        ItemStack stack = item.getItem();
        if (!stack.isEmpty()) stack.shrink(packet.getAmount());
        if (stack.isEmpty()) this.level.removeEntity(packet.getItemId(), Entity.RemovalReason.DISCARDED);
        ci.cancel();
    }
}
