package dev.azrieselman.irons_spells_controllify.controlify;

import dev.azrieselman.irons_spells_controllify.IronsSpellsControllifyMod;
import dev.azrieselman.irons_spells_controllify.mixin.MouseHandlerAccessor;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.input.ControllerStateView;
import dev.isxander.controlify.controller.input.GamepadInputs;
import io.redspace.ironsspellbooks.gui.overlays.SpellWheelOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

/**
 * Drives ISS spell-wheel selection from controller sticks.
 *
 * <p>ISS {@link SpellWheelOverlay} selects by <em>mouse cursor angle</em> from screen
 * center after {@code releaseMouse()}. We place the ungrabbed cursor on that angle
 * so ISS's existing math highlights the sector.
 *
 * <p><strong>Important:</strong> Controlify's {@code RADIAL_AXIS_*} bindings are scoped
 * to {@code BindContext.RADIAL_MENU} (only while Controlify's own
 * {@code RadialMenuScreen} is open). The ISS wheel is an in-game overlay with
 * {@code screen == null}, so those bindings always read {@code 0}. This bridge reads
 * <em>raw</em> gamepad stick axes instead.
 *
 * <p>Look input is zeroed while the wheel is open so the right stick does not also
 * turn the camera.
 */
public final class SpellWheelStickBridge {
    /** Screen-pixel radius outside ISS's center deadzone ({@code ringOuterEdgeMin = 65}). */
    private static final double CURSOR_RADIUS_PX = 140.0;

    /** Stick deadzone before we move the cursor (raw axes are 0..1 per direction). */
    private static final float STICK_DEADZONE = 0.25f;

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

        float[] stick = readAimStick(controller);
        if (stick == null) {
            // Leave cursor where the stick last pointed so highlight sticks.
            return;
        }

        float x = stick[0];
        float y = stick[1];
        float length = Mth.sqrt(x * x + y * y);
        if (length < 1.0e-4f) {
            return;
        }
        x /= length;
        y /= length;

        Minecraft minecraft = Minecraft.getInstance();
        double centerX = minecraft.getWindow().getScreenWidth() * 0.5;
        double centerY = minecraft.getWindow().getScreenHeight() * 0.5;
        // Screen Y grows downward; Controlify stick Y is down − up — same convention as ISS mouse math.
        double mouseX = centerX + x * CURSOR_RADIUS_PX;
        double mouseY = centerY + y * CURSOR_RADIUS_PX;

        MouseHandlerAccessor mouse = (MouseHandlerAccessor) minecraft.mouseHandler;
        mouse.ironsSpellsControllify$setXpos(mouseX);
        mouse.ironsSpellsControllify$setYpos(mouseY);
        GLFW.glfwSetCursorPos(minecraft.getWindow().getWindow(), mouseX, mouseY);
    }

    /**
     * Prefer right stick (aim); fall back to left stick if right is in the deadzone.
     *
     * @return {@code {x, y}} in screen-ish space (right/down positive), or {@code null} if idle
     */
    private static float[] readAimStick(ControllerEntity controller) {
        return controller.input().map(input -> {
            ControllerStateView state = input.stateNow();
            float[] right = axisPair(
                    state,
                    GamepadInputs.RIGHT_STICK_AXIS_LEFT,
                    GamepadInputs.RIGHT_STICK_AXIS_RIGHT,
                    GamepadInputs.RIGHT_STICK_AXIS_UP,
                    GamepadInputs.RIGHT_STICK_AXIS_DOWN);
            if (right != null) {
                return right;
            }
            return axisPair(
                    state,
                    GamepadInputs.LEFT_STICK_AXIS_LEFT,
                    GamepadInputs.LEFT_STICK_AXIS_RIGHT,
                    GamepadInputs.LEFT_STICK_AXIS_UP,
                    GamepadInputs.LEFT_STICK_AXIS_DOWN);
        }).orElse(null);
    }

    private static float[] axisPair(
            ControllerStateView state,
            net.minecraft.resources.ResourceLocation left,
            net.minecraft.resources.ResourceLocation right,
            net.minecraft.resources.ResourceLocation up,
            net.minecraft.resources.ResourceLocation down) {
        float x = state.getAxisState(right) - state.getAxisState(left);
        float y = state.getAxisState(down) - state.getAxisState(up);
        if (Math.abs(x) < STICK_DEADZONE && Math.abs(y) < STICK_DEADZONE) {
            return null;
        }
        return new float[]{x, y};
    }

    private static boolean isSpellWheelActive() {
        return SpellWheelOverlay.instance != null && SpellWheelOverlay.instance.active;
    }
}
