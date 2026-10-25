package dev.azrieselman.irons_spells_controllify.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Belt-and-suspenders for ISS {@code ExtendedKeyMapping#consume()}.
 *
 * <p>If another Controlify path (or an older Controlify build) sets
 * {@code isDown}/{@code clickCount} without {@code setDown}, sync the ISS
 * rising-edge flags from the live {@link KeyMapping#isDown()} state before
 * consume runs. Harmless when {@link KeyMappingControlifyBridgeMixin} already
 * called {@code setDown}.
 *
 * <p>{@code require = 0}: class only exists when Iron's Spells is installed.
 */
@Mixin(targets = "io.redspace.ironsspellbooks.player.ExtendedKeyMapping", remap = false)
public abstract class ExtendedKeyMappingCompatMixin {

    @Shadow
    private boolean canBeConsumed;

    @Shadow
    private boolean wasDown;

    @Inject(method = "consume", at = @At("HEAD"), require = 0)
    private void ironsSpellsControllify$syncEmulatedRisingEdge(CallbackInfoReturnable<Boolean> cir) {
        boolean down = ((KeyMapping) (Object) this).isDown();
        if (down && !this.wasDown) {
            this.canBeConsumed = true;
            this.wasDown = true;
        } else if (!down && this.wasDown) {
            this.wasDown = false;
        }
    }
}
