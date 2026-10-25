package dev.azrieselman.irons_spells_controllify.controlify;

import dev.azrieselman.irons_spells_controllify.IronsSpellsControllifyMod;
import dev.azrieselman.irons_spells_controllify.mixin.MouseHandlerAccessor;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import io.redspace.ironsspellbooks.gui.overlays.SpellWheelOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

/**
 * Drives ISS spell-wheel selection from Controlify radial stick axes.
 *
 * <p>ISS {@link SpellWheelOverlay} selects by <em>mouse cursor angle</em> from screen
 * center after {@code releaseMouse()}. Controlify's built-in radial bindings
 * ({@code RADIAL_AXIS_*}, default: right stick) supply an analogue vector; we place
 * the ungrabbed cursor on that angle so ISS's existing math highlights the sector.
 *
 * <p>Look input is zeroed while the wheel is open so the same stick (defaults share
 * the right stick with look) does not also turn the camera.
 */
public final class SpellWheelStickBridge {
    /** Screen-pixel radius outside ISS's center deadzone ({@code ringOuterEdgeMin = 65}). */
    private static final double CURSOR_RADIUS_PX = 140.0;

    private SpellWheelStickBridge() {}

    public static void register() {
        ControlifyEvents.CONTROLLER_STATE_UPDATE.register(event -> onControllerTick(event.controller()));
        ControlifyEvents.LOOK_INPUT_MODIFIER.register(modifier -> {
            if (isSpellWheelActive()) {
                modifier.lookInput().set(0f, 0f);
            }
        });
        IronsSpellsControllifyMod.LOGGER.info("Registered Controlify stick bridge for ISS spell wheel");
    }

    private static void onControllerTick(ControllerEntity controller) {
        if (!isSpellWheelActive()) {
            return;
        }
        if (!ControlifyApi.get().currentInputMode().isController()) {
            return;
        }

        float x = ControlifyBindings.RADIAL_AXIS_RIGHT.on(controller).analogueNow()
                - ControlifyBindings.RADIAL_AXIS_LEFT.on(controller).analogueNow();
        float y = ControlifyBindings.RADIAL_AXIS_DOWN.on(controller).analogueNow()
                - ControlifyBindings.RADIAL_AXIS_UP.on(controller).analogueNow();

        float threshold = controller.input()
                .map(input -> input.settings().buttonActivationThreshold)
                .orElse(0.5f);

        if (Math.abs(x) < threshold && Math.abs(y) < threshold) {
            // Leave cursor where the stick last pointed so highlight sticks.
            return;
        }

        float length = Mth.sqrt(x * x + y * y);
        if (length < 1.0e-4f) {
            return;
        }
        x /= length;
        y /= length;

        Minecraft minecraft = Minecraft.getInstance();
        double centerX = minecraft.getWindow().getScreenWidth() * 0.5;
        double centerY = minecraft.getWindow().getScreenHeight() * 0.5;
        // Screen Y grows downward; Controlify radial Y is down − up — same convention as ISS mouse math.
        double mouseX = centerX + x * CURSOR_RADIUS_PX;
        double mouseY = centerY + y * CURSOR_RADIUS_PX;

        MouseHandlerAccessor mouse = (MouseHandlerAccessor) minecraft.mouseHandler;
        mouse.ironsSpellsControllify$setXpos(mouseX);
        mouse.ironsSpellsControllify$setYpos(mouseY);
        GLFW.glfwSetCursorPos(minecraft.getWindow().getWindow(), mouseX, mouseY);
    }

    private static boolean isSpellWheelActive() {
        return SpellWheelOverlay.instance != null && SpellWheelOverlay.instance.active;
    }
}
