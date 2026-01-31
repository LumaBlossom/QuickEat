package luma.blossom.mixin.client;

import luma.blossom.QuickEatHandler;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class MovementMixin {
    
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(boolean slowDown, float f, CallbackInfo ci) {
        if (QuickEatHandler.getInstance().shouldBlockMovement()) {
            ClientInput input = (ClientInput) (Object) this;
            input.forwardImpulse = 0.0f;
            input.leftImpulse = 0.0f;

        }
    }
}
