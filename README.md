# Iron's Spells Controllify Compat

NeoForge **1.21.1** bridge so [Controlify](https://github.com/isXander/Controlify) controller input properly triggers [Iron's Spells 'n Spellbooks](https://github.com/iron431/irons-spells-n-spellbooks) (ISS) keybinds **and** stick-aims the spell wheel.

## What it does

Controlify emulates vanilla `KeyMapping` presses via `controlify$setPressed`, which updates `isDown` / `clickCount` **without** calling `KeyMapping#setDown(boolean)`.

ISS uses an `ExtendedKeyMapping` whose one-shot `consume()` API only arms when `setDown(true)` sees a rising edge. Controllers therefore looked “bound” in Controlify but never cast or opened the spell wheel.

The ISS spell wheel selects by **mouse cursor angle** after ungrabbing the mouse — sticks alone never moved that cursor.

This mod:

1. **Mixin bridge** — after Controlify’s `controlify$setPressed`, calls `setDown` so ISS `consume()` / hold detection work like keyboard.
2. **ISS safety mixin** — if emulation still skips `setDown`, syncs ISS rising-edge flags from `isDown()` before `consume()`.
3. **Optional Controlify bindings** — when both mods are present, registers explicit `keyEmulation` bindings for ISS keymaps (SPI entrypoint; reflective so ISS can stay optional).
4. **Spell-wheel stick aim** — while the wheel is open in controller mode, **raw right-stick** axes (left stick fallback) place the free cursor on a ring so ISS’s angle math highlights sectors; look is suppressed while the wheel is open. (Controlify’s `RADIAL_AXIS_*` binds only work inside Controlify’s own radial screen, so they are not used here.)

## Install

1. NeoForge **1.21.1**
2. [Iron's Spells 'n Spellbooks](https://www.curseforge.com/minecraft/mc-mods/irons-spells-n-spellbooks) (1.21.1 builds that use tick-based input / `ExtendedKeyMapping`, roughly **≥ 1.21.1-3.14** after [PR #989](https://github.com/iron431/irons-spells-n-spellbooks/pull/989))
3. [Controlify](https://modrinth.com/mod/controlify) **3.0.x LTS** for 1.21.1 (e.g. `3.0.1+lts`)
4. This mod jar in `mods/`

Deps are **optional** in the mod metadata: without Controlify or ISS the mixins simply no-op (`require = 0`).

## Versions

| Component | Target |
|-----------|--------|
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.x |
| Controlify | 3.0.0+ (LTS 1.21.1) |
| ISS | 1.21.1-3.14+ (tick / `ExtendedKeyMapping` input) |

## ARR / licensing note

Iron's Spells 'n Spellbooks is **All Rights Reserved**. This add-on does **not** include ISS assets, textures, models, sounds, or jars. It only depends on ISS at compile/runtime as a separate mod and calls its public/client APIs. Do not redistribute ISS binaries with this project.

This compat mod is licensed **LGPL-3.0-or-later** (aligned with Controlify’s license for mixin/API integration).

## Related GitHub issues

- [ISS #961 – Prefer `ClientTickEvent` / `KeyMapping` over `InputEvent`](https://github.com/iron431/irons-spells-n-spellbooks/issues/961) (root-cause analysis for controller emulation)
- [ISS PR #989 – Migrate to vanilla client input handling](https://github.com/iron431/irons-spells-n-spellbooks/pull/989) (merged; enables Controlify-style emulation *if* `setDown` is used)
- [ISS #552 – Controllable spell wheel](https://github.com/iron431/irons-spells-n-spellbooks/issues/552) / [Controllable #532](https://github.com/MrCrayfish/Controllable/issues/532) (same class of bug with a different controller mod)
- [EchoEllet/Controlify #5](https://github.com/EchoEllet/Controlify/issues/5) (Forge `InputEvent` vs `consumeClick` guidance; links ISS #961)

Design notes: see [`docs/compat-plan.md`](https://github.com/azrieselman/irons-spells-controllify/blob/main/docs/compat-plan.md) in this repo (also published under the project store).

## Build

```bash
./gradlew build
```

Output: `build/libs/irons_spells_controllify-<version>.jar`

## Project layout

Single NeoForge module for now, with package boundaries ready for multi-loader:

- `...mixin` — shared mixin bridge (portable idea)
- `...controlify` — Controlify API helpers / SPI
- `IronsSpellsControllifyMod` — NeoForge `@Mod` shell only

## Spell wheel UX notes

- Open the wheel with your bound key (e.g. D-pad left), then aim with the **right stick** (or left stick if right is idle).
- Camera look is paused while the spell wheel overlay is open.
- Releasing the stick keeps the last highlighted sector; release the wheel hold/toggle bind to confirm (ISS behavior).
- Fixed in **1.0.2**: earlier builds read Controlify Radial Axis binds, which are inactive outside Controlify’s own radial menu — stick aim appeared broken.

## Remaining gaps

- Spell-bar **scroll-with-modifier** still needs a scroll/stick source; ISS exposes `ClientInputEvents.handleSpellBarScrollModifier` for that.
- Pre-#989 ISS builds that only listen to `InputEvent` are out of scope; update ISS instead.
- Optional full Controlify radial UI (instead of feeding ISS’s mouse-angle overlay) is not implemented.
