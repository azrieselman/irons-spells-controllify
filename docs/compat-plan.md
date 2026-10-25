# Iron's Spells × Controlify compatibility plan

## Root cause (from public issues)

### Historical: `InputEvent` vs KeyMapping polling

ISS originally handled cast / spell wheel / quick-cast on NeoForge `InputEvent.Key` and `InputEvent.MouseButton`. Those events only fire for physical keyboard/mouse paths.

Controller mods such as **Controlify** (isXander) and **Controllable** (MrCrayfish) emulate keybinds by driving vanilla `KeyMapping` state (`isDown`, `clickCount`). They do **not** synthesize Forge/NeoForge `InputEvent`s. Bound controller buttons therefore never reached ISS.

Documented in:

- [iron431/irons-spells-n-spellbooks#961](https://github.com/iron431/irons-spells-n-spellbooks/issues/961)
- [iron431/irons-spells-n-spellbooks#989](https://github.com/iron431/irons-spells-n-spellbooks/pull/989) (merged: move to `ClientTickEvent.Post` + `KeyMapping` consume / isDown)
- [iron431/irons-spells-n-spellbooks#552](https://github.com/iron431/irons-spells-n-spellbooks/issues/552) / [MrCrayfish/Controllable#532](https://github.com/MrCrayfish/Controllable/issues/532)
- [EchoEllet/Controlify#5](https://github.com/EchoEllet/Controlify/issues/5) (same class of Forge `InputEvent` breakage)

### Remaining gap after #989: `ExtendedKeyMapping` vs `controlify$setPressed`

Post-#989 ISS uses `ExtendedKeyMapping`:

- Overrides `setDown(boolean)` to set a one-shot `canBeConsumed` flag on rising edge
- Exposes `consume()` which drains vanilla `consumeClick()` then returns `canBeConsumed`

Controlify’s `KeyMappingMixin.controlify$setPressed` (see Controlify 3.0.x):

- Sets shadowed `isDown` and increments `clickCount` directly
- Does **not** call `KeyMapping#setDown(boolean)` (unlike Controlify’s `ToggleKeyMapping` path, which does)

Result: controller presses look correct in Controlify’s UI and bump `clickCount`, but ISS `consume()` clears the click counter and still returns `false` because `canBeConsumed` was never set. Hold detection via `isDown()` can work; one-shot cast / wheel open / quick cast do not.

## Approach taken

Minimal NeoForge 1.21.1 add-on mod `irons_spells_controllify`:

1. **Mixin** `KeyMapping.controlify$setPressed` → also call `setDown(pressed)` (`require = 0`, soft Controlify).
2. **Mixin** ISS `ExtendedKeyMapping.consume` HEAD → sync rising-edge flags from `isDown()` if emulation still skipped `setDown`.
3. **Optional** Controlify SPI entrypoint (reflective ISS load) registers explicit `keyEmulation` bindings so ISS keys are first-class controller actions without duplicate auto-bindings.

Soft/optional mod dependencies; no ISS assets or jars redistributed (ARR).

## Version matrix

| Piece | Version |
|-------|---------|
| Minecraft / NeoForge | 1.21.1 / 21.1.x |
| Controlify | 3.0.x LTS (`3.0.1+lts+1.21.1-neoforge`) |
| ISS | 1.21.1 builds with #989-style input (`ExtendedKeyMapping`), e.g. 3.16.3 |

## Multi-loader path

Single NeoForge module today. Packages:

- `mixin` — loader-agnostic mixin idea
- `controlify` — Controlify API / SPI
- Root `@Mod` — NeoForge shell only

Future Architectury/MultiLoader can move `mixin` + `controlify` into `common` and keep a thin NeoForge/Fabric entry.

## Out of scope / follow-ups

- Full spell-wheel stick radial (ISS #961 called this out as needing Controlify-specific UI)
- Driving `ClientInputEvents.handleSpellBarScrollModifier` from a stick/D-pad
- Supporting pre-#989 ISS `InputEvent`-only builds
- Controllable (MrCrayfish) — different API; same conceptual bug class
