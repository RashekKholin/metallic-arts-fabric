# The Metallic Arts

Minecraft Java **26.1** • Fabric • Java **25** • mod ID `metallicarts` • version **0.3.0-alpha.2**

The Phase 2 metal economy remains available: **ores → concentrates → refined metal → measured flakes → Brewing Stand vials → persistent ingested reserves**. This development build adds the first server-controlled Steel, Iron, Pewter and Tin effects. Drinking supplies fuel; it never grants a power.

**Phase 3 is in progress.** Read the [Allomancy testing guide](docs/ALLOMANCY.md) and [validation record](docs/PHASE3_VALIDATION.md). Aim-based control now supports up to eight selected targets with shared force and stable locks. Particle metal lines and two Tin senses remain initial implementations; a radial interface, broader sensory behavior and two simultaneous live clients remain unfinished.

## Install / upgrade

Use `metallic-arts-fabric-0.3.0-alpha.2.jar`, not the sources jar. Install Fabric Loader 0.18.4+ and Fabric API 0.145.1+26.1 for exactly Minecraft 26.1. Put the mod jar in both client and server mods folders. Remove the previous Metallic Arts jar before adding this version. Keep a backup of existing worlds before upgrading.

Existing Phase 1 spiritweb saves load with an empty invested-reserve collection. Do not downgrade a world after adding Phase 2 blocks/items. Ores appear in newly generated chunks; existing terrain is not rewritten.

## Start playing

Craft a **Metallurgy Table** from three iron ingots, three cobblestone and three planks in three horizontal rows. Fuel it with coal or another furnace fuel.

In **Shape**, turn an ingot into nuggets, one nugget into 100 portions, and a portion into flakes. Use the arrows to select an operation; hover the ghost item for requirements. Press **Start/Stop**. **Batch** repeats work while ingredients and output space permit.

Brew flakes with **water bottles and blaze powder in a normal Brewing Stand**. One flake fills one bottle. Drink the vial, then press **M** to see your ingested reserve. Every standard metal has this path.

Sneak-right-click the table with an empty hand to choose a Metallurgist specialty. An unemployed adult villager can claim it during working hours.

Read the [complete economy guide](docs/ECONOMY.md) for ore recovery, alloy ratios, specialties, invested material handling and data-pack customization.

## Included

- Sixteen standard metal IDs; ingots/nuggets, portions/flakes, coins, rings, bracelets, bracers, spikes and vials. Vanilla iron/copper/gold ingots and nuggets are reused.
- Six supporting materials: silver, nickel, lead, bismuth, carbon and silicon.
- Eight new ore families with stone/deepslate variants, mining tags, Fortune/Silk Touch loot, experience and data-driven Overworld distribution.
- A persistent server-owned Metallurgy Table with Refine/Alloy/Shape tabs, recipe selection, fuel, progress, batch processing and output preflight.
- **437** processing definitions: **325** ordinary refining/shaping/alloy operations and **112** invested-flaking definitions.
- Eight alloy formulas with preserved composition and viable/imperfect/invalid outcomes. Incorrect batches cannot become viable through reshaping.
- Four Metallurgist specialties, Novice–Master trade sets, deterministic category access, finite stock and bulk offers using vanilla trading behavior.
- Sixteen water-bottle brewing paths with ordinary fuel/timing and one-flake-per-bottle accounting.
- Server-validated ingestion, bottle return, reserve synchronization, Identity/charge retention and a read-only Spiritweb screen.
- Phase 1 persistent source-tracked power contracts, server configuration, provenance catalog and owner-only networking.
- Experimental independent burning/flaring for Steel, Iron, Pewter and Tin; server-controlled fuel costs, basic reciprocal forces, owner-only particle target lines, Pewter attributes/fatigue, Tin dark vision/hearing/overload and recoverable coin projectiles.
- Operator grant/revoke/fuel/inspect tools, rebindable push/pull/toss keys, M-menu burn controls and a mode HUD.

## Not implemented

The remaining twelve Allomantic effects, Feruchemical storage/tapping, anatomical spike placement, Hemalurgic extraction, Compounding, god metals, survival power acquisition and optional mixed-metal vials are not implemented. A radial interface, a coin pouch and full Tin sensory coverage remain Phase 3 work. Equipment created in normal play is uncharged. Invested-material support preserves data for later systems and test/mod-provided data.

## Build and validate

Set `JAVA_HOME` to a JDK 25. The checked-in wrapper downloads and checksum-verifies Gradle 9.4.0.

```powershell
# Build the installable jar and run unit tests without accepting the Minecraft EULA
.\gradlew.bat clean build -x runGameTest
# Existing datagen entrypoint
.\gradlew.bat runDatagen
# Run only if you agree to https://aka.ms/MinecraftEULA
.\gradlew.bat build runClientGameTest -PacceptMinecraftEula=true
# Interactive development
.\gradlew.bat runClient
.\gradlew.bat runServer
```

macOS/Linux: use `./gradlew`. Normal builds need Java, not Node. Original pixel textures are versioned in `src/main/texture-assets.zip` and expanded by Gradle. The committed data and texture archive can optionally be regenerated with `node scripts/generate-economy.cjs`; the generator uses no external packages.

Server policy loads from `config/metallicarts-server.json`. The snapshot cooldown is enforced. `allowPlayerHemalurgy` remains reserved and enables no mechanics yet. Ore distribution, processing recipes and trades are configured through data packs.

See [Phase 2 validation](docs/PHASE2_VALIDATION.md), [architecture](docs/ARCHITECTURE.md), [canon policy](docs/CANON.md), and [historical Phase 1 results](docs/VALIDATION.md).

## Sources

The [Fabric 26.1 guidance](https://fabricmc.net/2026/03/14/261.html) informed the unobfuscated Java 25/Loom setup and data-driven trading approach. Implementation was compiled and exercised against Minecraft **26.1**, including its local unobfuscated sources. Alloy quantities and recovery yields are explicitly documented gameplay interpretations, not verified universal canonical measurements.

Unofficial fan project. No book text or official illustrations are bundled.
