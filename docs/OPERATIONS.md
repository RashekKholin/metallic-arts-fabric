# Host configuration, adapters and recovery

## Configuration

All policies are generated under the instance's `config/` directory. Edit while the server is stopped, then restart. Invalid values stop loading visibly; unknown saved charge is never silently reset. No live policy reload command is claimed.

| File | Purpose |
|---|---|
| `metallicarts-server.json` | Player Hemalurgy default off, snapshot cooldown, documented UUID account-access switch |
| `metallicarts-allomancy.json` | Existing burn costs, strength/physics/speed caps |
| `metallicarts-advanced.json` | Detection, emotions, local time, body resource/cure costs, contact/burst caps, social limits, compounding and constructs |
| `metallicarts-progression.json` | Once-only survival allocation, discovery, powered populations and experimental recipes |

Use the generated files for exact field names. Advanced sections: `detection`, `emotions`, `time`, `body`, `enhancement`, `social`, `compounding`, `constructs`. Examples: `compounding.multiplier=10`, `constructs.exceptionalExtraction=false`, `constructs.nonlethalRetention=0.25`, `time.minimum=0.25`, `time.maximum=4`. Schema and stored units are fixed. Numeric strings, fractional integer costs, non-finite values, invalid probabilities and invalid ranges are rejected. New advanced defaults merge with a one-time `metallicarts-advanced.before-0.8.json` backup. The progression file fills missing new fields in memory; unknown fields are retained on disk.

Progression fields: `schema:1`, `enabled:true`, `allomancerChance:0.5`, `feruchemistChance:0.5`, `npcChance:0.02`, `discoveryLevels:5`, `npcFuel:100000`, `lerasiumRecipe:false`, `mistwraithRitual:true`. Disabling prevents further discovery/population assignment; it does not revoke already earned grants or remove existing NPC capabilities. Operator NPC burning remains explicit. Published UUID account access remains separately controlled by `enableAccountPowers`.

## Protection API

`dev.metallicarts.api.ArtsProtection.EVENT`, `NPC_EVENT` and `BLOCK_EVENT` are server adapter hooks. A registered false result denies the action; exceptions fail closed. Actions distinguish emotions, contact depletion/amplification, extraction, compulsion, constructs, compounding and block movement. Existing PvP/consent and native attack protections also apply. Register an adapter during common initialization, identify actor, target/position and action, and consult your actual claim API. Do not approve solely because the mod exposes a hook.

No particular third-party claim/protection mod has been installed and certified by this release. Translation between protection APIs, arbitrary local-time machines and foreign knowledge systems requires explicit adapters and actual integration tests.

## Operator tools

- `/metallicarts assets PLAYER audit`: reports invalid references; never creates assets.
- `/metallicarts assets PLAYER repairrefs`: refreshes only a matching existing canonical seal/owner/slot. Cannot recover foreign, spent or missing charge from a displayed preview.
- `/metallicarts constructbase PLAYER human|mistwraith|reset`: explicit body recovery/provisioning; does not mint spikes or grants.
- `/metallicarts npcfuel ENTITY METAL MICROGRAMS` and `/metallicarts npcburn ENTITY METAL off|burn|flare`: testing/provisioning of supported owned NPC abilities. Existing donor grant tools remain operator-only.

Public `/metallicarts-survival` actions have a separate command root and no arbitrary player target, grant or fuel edit. Giving a player creative/operator access inherently lets them create ordinary physical items; canonical invested copies still cannot double-spend world charge.

## Upgrade and backup

1. Stop the server cleanly and back up the **entire world**, config directory and old mod build. Include `playerdata`, `data`, dimensions and Fabric attachment data together.
2. Install exactly one new mod jar on server and every client, with matching Minecraft/Fabric API/Java. Start a copy of the world first.
3. Review generated defaults and new survival allocation policy before admitting players. Existing characters and hostile mobs first allocate on join/load after upgrade.
4. Audit valuable contacted items, charged free items, paired Blessings, memories and implants. Confirm spent source copies stay invalid. Reconnect and save/restart once before replacing the production world.
5. If validation fails, stop and restore the **whole backup plus the old mod/config**. Never merge an older charge ledger with newer player equipment, regenerate charge from previews, or downgrade a world in place.

Old Spiritweb/Metalmind records default new optional fields; spent defaults false for legitimate old assets, new canonical spends persist tombstones. Progression, trauma, relationships, construct and preparation data have independent persistent records. Corrupt/unknown data requires a backup or explicit diagnosis, not automatic invented replacement.

The two-process restart fixture covers real dedicated world/player serialization and spent-source refusal. It is not an archived production-world migration, crash-consistency/fault-injection certification or proof for every prior alpha save. Real archived save upgrades and all operating systems remain acceptance tasks.



## Release-candidate additions

`metallicarts-world.json` schema 1 defaults: `researchCamps: true`, `harmoniumReactions: true`, `constructChance: 0.002`, `maximumNpcSessions: 256`, `scanBudget: 2048`, `godmetalBurnTicks: 100`. Valid ranges are enforced; chances must be finite 0..1, sessions 16..1024, scans 64..8192, duration 20..1200. World generation applies only to new terrain. Client preferences live in `metallicarts-client.json` and are also editable through Guide → Sensory settings.

`/metallicarts assets PLAYER recoverleases` reconstructs only missing references in empty slots for assets already leased canonically to that exact UUID. It never creates charge or reclaims a foreign/free/spent asset. Run `audit` before/after, keep all world/player files together and preserve a whole backup before repairs. Canonical ownership prevents duplication but is not a multi-file transactional database; arbitrary disk damage still requires backup restoration.

The checked-in recovery fixture uses distinct orderly and crash worlds: seed/verify in one, seed/deliberate player-only save/crash/recover in the other. The intentional exit code 71 and exact fault marker are required, so ordinary crashes do not count as a passing fault test. Legacy fixtures archive committed 0.5/0.7 sources, let each version create its own save, then upgrade and reload twice with current code. This tests real historical-format generation, not every archived production world or every crash boundary.

**Run native fixtures sequentially.** Fabric's `deleteGameTestRunDir` dependency can clear graphical-test storage even when starting another server fixture. Do not start server/restart/legacy/multiplayer Gradle runs while a graphical suite is using its run directory, or rebuild shared classes/resources under running JVMs.

Optional OpenPAC fixture versions and precise supported behavior are documented in RELEASE_CANDIDATE.md. `scripts/setup-claim-fixture.ps1` fetches official pinned Modrinth version IDs and verifies SHA-512 metadata before use. These jars are never bundled in the normal release.
