# The Metallic Arts

Minecraft Java **26.1** • Fabric • Java **25** • mod ID `metallicarts`

Phase 1 technical foundation for a canon-informed Mistborn mod. This is not yet a playable magic system.

## Build and run

Install a JDK 25 and set `JAVA_HOME` to it. The checked-in Gradle wrapper downloads Gradle 9.4.0 and verifies its SHA-256.

```powershell
# Windows: compilation, unit tests and jar; no Minecraft EULA acceptance implied
.\gradlew.bat clean build -x runGameTest
# Datagen entrypoint/empty provider pack validation
.\gradlew.bat runDatagen
# Run these only if you agree to https://aka.ms/MinecraftEULA
.\gradlew.bat runGameTest -PacceptMinecraftEula=true
.\gradlew.bat runClientGameTest -PacceptMinecraftEula=true
# Interactive development client / dedicated server
.\gradlew.bat runClient
.\gradlew.bat runServer
```

macOS/Linux: use `./gradlew` instead. `build` also runs server game tests unless `-x runGameTest` is supplied. Game-test EULA consent is opt-in via the property shown above. Interactive dedicated servers have their own `eula.txt` consent step.

The installable mod is `build/libs/metallic-arts-fabric-0.1.0.jar` (not the sources jar). Install it with Fabric Loader 0.18.4+ and Fabric API 0.145.1+26.1 on both the client and server, targeting exactly Minecraft 26.1. Other Minecraft versions are not declared compatible.

Press **M** in a world to open the read-only Spiritweb screen; rebind it in Controls. The screen shows Allomantic/Feruchemical access for all sixteen metals. New characters have no powers. The screen cannot grant or activate powers.

## Implemented

- Pinned unobfuscated Fabric Loom 1.15.5 build, split common/client source sets, Java 25 compilation, versioned metadata and GitHub Actions build/unit-test/datagen workflow.
- Shared immutable catalog of sixteen stable metal IDs, independent of enum ordinal order. This is a closed application catalog, not a datapack-extensible Minecraft registry.
- Registered persistent, copy-on-death player spiritweb attachment with a schema, independent Identity UUID, source-tracked power grants, strengths and ingested reserve quantities.
- Registered persistent/networked `metallicarts:metal_data` item component with unique instance ID, metal, mass, purity, charge and optional Identity. Phase 1 adds no metalmind items or recipes; game tests attach sample data to a vanilla gold ingot.
- Server-host configuration in `config/metallicarts-server.json`, created on first initialization. Invalid or unsupported config fails visibly. Restart required after edits.
- Versioned request/snapshot payload types. The server reads only the requesting player's data, enforces a request cooldown, and sends snapshots only to that owner. Clients cannot upload grants, target another player's UUID, or change reserves.
- Client snapshot cache cleared on disconnect, key mapping, and read-only screen. Server initialization imports no Minecraft client classes.
- Datagen entrypoint/empty provider pack, asset and data folder scaffolding, English labels, rule-provenance catalog, unit and in-game test foundations.

## Configuration

```json
{
  "schema": 1,
  "allowPlayerHemalurgy": false,
  "snapshotCooldownTicks": 20
}
```

`snapshotCooldownTicks` is enforced (1–1200 ticks). `allowPlayerHemalurgy` is a reserved policy field for future extraction mechanics; changing it currently enables no gameplay. The server host owns the configuration, including in singleplayer. It is not a client preference or per-world config.

## Deferred

No ores, metallurgy, economy, vials, ingestion, burning/flaring, metal lines, physics, Feruchemical storage/tapping, charged spikes, anatomy, extraction, Compounding, god metals, HUD/radial controls, or power acquisition are implemented. Item UUIDs are foundations, not a completed duplication-prevention system. An absent item Identity does not automatically authorize tapping. Migration from future or historical schemas is not implemented; unsupported schemas are rejected.

See [architecture](docs/ARCHITECTURE.md), [canon and interpretations](docs/CANON.md), and [actual validation](docs/VALIDATION.md).

## Toolchain sources

The [Fabric 26.1 announcement](https://www.fabricmc.net/2026/03/14/261.html) establishes Java 25, unobfuscated `net.fabricmc.fabric-loom`, ordinary `implementation` dependencies, and the `fabric-api` dependency ID. The [official 26.1 example](https://github.com/FabricMC/fabric-example-mod/tree/26.1) and [versioned Fabric documentation](https://github.com/FabricMC/fabric-docs/tree/main/versions/26.1.2) informed the implementation; compilation and game tests target **26.1**, not 26.1.2.

Unofficial fan project. Mistborn and its setting belong to their respective rights holders. No book text or official illustrations are bundled.
