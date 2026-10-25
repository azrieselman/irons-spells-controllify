package dev.azrieselman.irons_spells_controllify;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NeoForge entry for Iron's Spells Controllify Compat.
 *
 * <p>Loader-specific shell. Shared compatibility logic lives under
 * {@code common} / {@code mixin} / {@code controlify} packages so a future
 * Architectury or MultiLoader split can move those without rewriting the bridge.
 */
@Mod(IronsSpellsControllifyMod.MOD_ID)
public final class IronsSpellsControllifyMod {
    public static final String MOD_ID = "irons_spells_controllify";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public IronsSpellsControllifyMod(IEventBus modEventBus, ModContainer modContainer) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            LOGGER.info("Iron's Spells Controllify Compat loaded (client). "
                    + "KeyMapping.setDown bridge applies when Controlify is present.");
        }
    }
}
