package dev.azrieselman.irons_spells_controllify.controlify;

import dev.azrieselman.irons_spells_controllify.IronsSpellsControllifyMod;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.api.entrypoint.InitContext;
import dev.isxander.controlify.api.entrypoint.PreInitContext;
import net.neoforged.fml.ModList;

/**
 * Controlify SPI entrypoint with no hard reference to ISS classes in this file.
 * Loads {@link IssControlifyBindings} reflectively only when ISS is present.
 */
public final class SafeControlifyEntrypoint implements ControlifyEntrypoint {

    @Override
    public void onControlifyPreInit(PreInitContext ctx) {
        if (!ModList.get().isLoaded("irons_spellbooks")) {
            return;
        }
        try {
            Class<?> registrar = Class.forName(
                    "dev.azrieselman.irons_spells_controllify.controlify.IssControlifyBindings");
            registrar.getMethod("tryRegister", PreInitContext.class).invoke(null, ctx);
        } catch (ReflectiveOperationException e) {
            IronsSpellsControllifyMod.LOGGER.warn(
                    "Could not register ISS Controlify bindings; mixin bridge still applies", e);
        }
    }

    @Override
    public void onControlifyInit(InitContext ctx) {
        // no-op
    }

    @Override
    public void onControllersDiscovered(ControlifyApi controlify) {
        // no-op
    }
}
