package dev.coinhider.mixin;

import dev.coinhider.CoinHider;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 26.1 - 26.2: shouldRender(entity, frustum, camX, camY, camZ). */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void coinhider$hideCoins(Entity entity, Frustum frustum, double camX, double camY, double camZ,
                                     CallbackInfoReturnable<Boolean> cir) {
        if (CoinHider.shouldHide(entity)) cir.setReturnValue(false);
    }
}
