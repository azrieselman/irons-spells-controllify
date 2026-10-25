package dev.azrieselman.irons_spells_controllify.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controlify emulates vanilla keybinds by calling {@code controlify$setPressed},
 * which sets {@code KeyMapping.isDown} / {@code clickCount} directly and does
 * <strong>not</strong> invoke {@link KeyMapping#setDown(boolean)}.
 *
 * <p>Iron's Spells' {@code ExtendedKeyMapping} records a one-shot rising edge
 * inside {@code setDown} ({@code canBeConsumed}) and exposes that via
 * {@code consume()}. Without {@code setDown}, controller presses increment
 * {@code clickCount} but never set {@code canBeConsumed}, so ISS silently
 * ignores them after draining the click counter.
 *
 * <p>{@code require = 0}: method exists only when Controlify's KeyMapping mixin
 * is applied — soft dependency.
 */
@Mixin(KeyMapping.class)
public abstract class KeyMappingControlifyBridgeMixin {

    @Inject(method = "controlify$setPressed", at = @At("TAIL"), remap = false, require = 0)
    private void ironsSpellsControllify$callSetDown(boolean pressed, CallbackInfo ci) {
        // ToggleKeyMapping overrides controlify$setPressed and already calls setDown;
        // this inject targets the base KeyMapping implementation used by ISS ExtendedKeyMapping.
        ((KeyMapping) (Object) this).setDown(pressed);
    }
}
