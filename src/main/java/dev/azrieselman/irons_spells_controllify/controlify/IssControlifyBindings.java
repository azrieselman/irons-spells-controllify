package dev.azrieselman.irons_spells_controllify.controlify;

import dev.azrieselman.irons_spells_controllify.IronsSpellsControllifyMod;
import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.entrypoint.PreInitContext;
import dev.isxander.controlify.bindings.BindContext;
import io.redspace.ironsspellbooks.player.KeyMappings;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;

/**
 * Optional explicit Controlify bindings for ISS keymaps.
 *
 * <p>Not registered via Controlify's SPI by default: ServiceLoader would load this
 * class whenever Controlify is present, even if ISS is absent. Call
 * {@link #tryRegister(PreInitContext)} only from a context that already confirmed
 * both mods are loaded (or use Controlify's automatic KeyMapping conversion).
 *
 * <p>Kept as a portable helper for a future Fabric/Quilt module or an SPI that
 * gates on {@code ModList}.
 */
public final class IssControlifyBindings {
    private IssControlifyBindings() {}

    public static void tryRegister(PreInitContext ctx) {
        if (!ModList.get().isLoaded("irons_spellbooks")) {
            IronsSpellsControllifyMod.LOGGER.debug("Skipping ISS Controlify bindings; irons_spellbooks not loaded");
            return;
        }
        register(ctx.bindings());
    }

    public static void register(ControlifyBindApi bindings) {
        Component category = Component.translatable(KeyMappings.KEY_BIND_GENERAL_CATEGORY);

        bindings.registerBinding(builder -> builder
                .id(IronsSpellsControllifyMod.MOD_ID, "spellbook_cast")
                .name(Component.translatable("key.irons_spellbooks.spellbook_cast"))
                .category(category)
                .allowedContexts(BindContext.IN_GAME)
                .keyEmulation(KeyMappings.SPELLBOOK_CAST_ACTIVE_KEYMAP));

        bindings.registerBinding(builder -> builder
                .id(IronsSpellsControllifyMod.MOD_ID, "spell_wheel")
                .name(Component.translatable("key.irons_spellbooks.spell_wheel"))
                .category(category)
                .allowedContexts(BindContext.IN_GAME)
                .keyEmulation(KeyMappings.SPELL_WHEEL_KEYMAP));

        bindings.registerBinding(builder -> builder
                .id(IronsSpellsControllifyMod.MOD_ID, "spell_wheel_toggle")
                .name(Component.translatable("key.irons_spellbooks.spell_wheel_toggle"))
                .category(category)
                .allowedContexts(BindContext.IN_GAME)
                .keyEmulation(KeyMappings.SPELL_WHEEL_TOGGLE_KEYMAP));

        bindings.registerBinding(builder -> builder
                .id(IronsSpellsControllifyMod.MOD_ID, "spell_bar_modifier")
                .name(Component.translatable("key.irons_spellbooks.spell_bar_modifier"))
                .category(category)
                .allowedContexts(BindContext.IN_GAME)
                .keyEmulation(KeyMappings.SPELLBAR_SCROLL_MODIFIER_KEYMAP));

        Component quickCastCategory = Component.translatable(KeyMappings.KEY_BIND_QUICK_CAST_CATEGORY);
        for (int i = 0; i < KeyMappings.QUICK_CAST_MAPPINGS.size(); i++) {
            final int slot = i;
            bindings.registerBinding(builder -> builder
                    .id(IronsSpellsControllifyMod.MOD_ID, "spell_quick_cast_" + (slot + 1))
                    .name(Component.translatable("key.irons_spellbooks.spell_quick_cast_" + (slot + 1)))
                    .category(quickCastCategory)
                    .allowedContexts(BindContext.IN_GAME)
                    .keyEmulation(KeyMappings.QUICK_CAST_MAPPINGS.get(slot)));
        }

        IronsSpellsControllifyMod.LOGGER.info("Registered Controlify keyEmulation bindings for ISS keymaps");
    }
}
