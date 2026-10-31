# Phase 3 development: first Allomancy

This is an experimental milestone, version 0.3.0-alpha.1. Phase 3 is not complete. The other twelve Allomantic effects, Feruchemy, Hemalurgic extraction, Compounding, god metals and survival power acquisition remain unimplemented.

## Try it

Use a test world with cheats enabled, or a server where you are an operator. Replace PLAYER with your Minecraft player name:

```mcfunction
/metallicarts grant PLAYER steel 1
/metallicarts grant PLAYER iron 1
/metallicarts grant PLAYER pewter 1
/metallicarts grant PLAYER tin 1
/metallicarts fuel PLAYER steel 100000
/metallicarts fuel PLAYER iron 100000
/metallicarts fuel PLAYER pewter 100000
/metallicarts fuel PLAYER tin 100000
```

Fuel quantities are **micrograms**: 100000 is 0.1 gram. You can instead drink the existing metal vials. An ordinary prepared vial contains 10000 micrograms. Drinking alone never grants a power.

Press **M**. Each of the four buttons cycles **off → burn → flare → off**. The HUD shows active modes. Ownership is still read-only in this screen.

While Steel or Iron burns, aim toward an eligible metal target. The blue particle line identifies the current target. Hold **R** to Steelpush or **V** to Ironpull. Push downward on an iron block to lift yourself. Aim at an external dropped metal stack or metal-armored creature to move it with reciprocal recoil. **C** tosses one coin from your inventory; a coin must actually be present. All three controls can be rebound in Minecraft settings.

Pewter increases attack, armor, knockback resistance, movement, jump and safe fall distance. It does not grant invulnerability or erase ordinary fall hazards. It adds food exhaustion; stopping after at least 200 continuous ticks causes 200 ticks of movement fatigue. Only this mod's own attribute modifiers are removed.

Tin improves dark vision and increases environmental, creature and player sound volume. Music/UI categories are untouched. Flaring Tin in bright light or among more than twelve nearby entities produces an overload cue and a small movement penalty. These thresholds are gameplay policies, not canonical measurements. Smell, taste, touch, zoom and advanced filtering are not implemented.

## Operator tools

```text
/metallicarts grant <player> <metal> <strength>
/metallicarts revoke <player> <grant-UUID>
/metallicarts fuel <player> <metal> <micrograms>
/metallicarts inspect <player>
```

These require Minecraft's gamemaster/operator permission. Grants have explicit ADMIN origin and individual grant/source UUIDs. Inspect lists those IDs. Revoking one grant preserves other grants, fuel and Identity. The strongest remaining applicable grant wins; effect strength is capped at 4 before the flare multiplier. Granting another metal records ownership but explicitly reports its effect as unimplemented; unsupported metals cannot burn.

## Costs and persistence

Server settings live in `config/metallicarts-allomancy.json`, separate from the old server configuration. Normal rates are Steel/Iron/Tin 100 micrograms per second and Pewter 200, at twenty game ticks per second. Lag slows game-time consumption. Flare costs four times as much and has a 1.8 strength multiplier. Values are configurable and validated at startup.

Fuel is paid before a power is applied. Whole micrograms prepay fractional tick costs; stopping or reconnecting can waste less than one microgram of prepaid credit but cannot erase unpaid debt. Insufficient fuel or lost ownership stops that power. Independent simultaneous burns pay independently.

Ownership, Identity and remaining reserves persist. Active modes and attribute modifiers are transient and reset on disconnect, server restart, death/player replacement, dimension change and spectator state. A dimension change is detected by the next server update.

Invested records are a **subset of total reserves**, not additional mass. Ordinary available fuel is total minus invested mass. This build reserves invested fuel from burning entirely; it cannot destroy charge/Identity through ordinary burning. The menu displays ordinary fuel only; inspect reports total, invested and ordinary micrograms separately.

The version-1 save shape is unchanged. Decoding refuses duplicate grant IDs, duplicate invested instance IDs and invested mass exceeding total mass. Invalid data is an error, not a migration or an intentional character reset; retain backups for recovery.

## Detection and physics policies

The first implementation aims at one target, with a 24-block default range, at most 64 block probes and a configured maximum of 48 entity candidates examined. Only loaded chunks are probed. It does not load terrain to search for metal. Nonmetal walls do not hide tagged external anchors.

Data packs can extend `metallicarts:metal_anchors` block and `metallicarts:external_metals` item tags. Initial anchors cover basic iron/gold/copper storage blocks, iron fixtures, anvils and rails. Weathered copper variants, every modded block, raw ores and arbitrary machines are not blanket-classified as metal. Registered metal forms are detected directly. Carbon, silicon and pure aluminum forms are excluded.

Invested item forms resist this ordinary force implementation completely. This is a conservative gameplay policy until Investiture resistance is designed, not a claim that all invested matter is canonically immovable. Armor moves its wearer rather than being ripped out. Internal reserves are never external targets.

Forces follow the line from player to target; equal opposite impulses apply to free targets before speed caps. Anchors absorb recoil, and pushing downward on a grounded item can brace it. Small flakes are weaker anchors than coins. Player effective mass is 80; metal form mass is a separate gameplay scale, unrelated to credited ingestible grams. Default maximum speeds are 2.4 blocks/tick for players and 4 for objects. Vanilla collision and gravity remain active.

Opposing simultaneous push/pull strengths combine before application. There is no creative flight switch, teleport movement, wall clipping, target-coordinate packet or client-provided strength. Held input expires after ten server ticks without a heartbeat and is released while a screen is open.

Coins preserve their actual component-bearing stack, inflict speed-dependent impact damage capped at twelve, and return one item on impact or after 1200 ticks. No inventory coin means no toss, including in creative mode. There is no coin pouch yet.

## Accessibility

`config/metallicarts-client.json` has `brighterVision`, `louderSound` and `showOverload` switches. Restart the client after changing them. They alter presentation only; server costs and overload penalties remain authoritative. Overload feedback is static text with no flashing overlay. Tin does not overwrite vanilla potion effects or save changes to Minecraft brightness, sound or FOV settings.

## Before the Phase 3 release

Finish multi-target selection and clearer metal lines, broaden profiles without guessing ore/invested behavior, improve Tin sensory coverage, add coin inventory conveniences, exercise active death/dimension transitions and two simultaneous live clients, and balance sustained multiplayer movement. See PHASE3_VALIDATION.md for observed results and remaining checks.
