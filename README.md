# The Metallic Arts

Minecraft Java **26.1** • Fabric • Java **25** • mod ID `metallicarts` • version **0.7.0-alpha.2**

Playable alpha through **Phase 7**: metal economy, all sixteen standard Allomantic metals, all sixteen Feruchemical metals (five separate Tin senses), typed Hemalurgy and canonical contacted equipment. Drinking supplies fuel; it never grants powers.

Read [advanced Arts controls and exact limits](docs/ADVANCED_ARTS.md), [physical Allomancy](docs/ALLOMANCY.md), [Feruchemy](docs/FERUCHEMY.md) and [Hemalurgy](docs/HEMALURGY.md). Temporal clocks, prediction, memories, Connection, Fortune and unsealed construction are explicit bounded Minecraft adaptations. Compounding, full constructs and natural power progression remain future work.

**Phase 7 baseline validation (alpha.1):** 66 unit tests,61 required server tests, all 6 client suites,48 two-client checks and a two-process restart check passed. Details and limits are recorded in [Phase 6–7 evidence](docs/PHASE67_VALIDATION.md). The historical Phase 5 results remain in their own record. Two real independent local clients now have a repeatable test harness; authenticated remote multiplayer and third-party compatibility remain separate acceptance work.

**Current alpha.2 checks:** 71 unit tests, 62 required server tests and all six native client suites passed. [Account-access validation](docs/ACCOUNT_ACCESS_VALIDATION.md) distinguishes current results from earlier multiplayer/restart evidence.

## Documented account access

**RashekKholin and The17thShard have automatic baseline access to all standard Allomancy and Feruchemy**, matched by verified account UUID. Normal fuel, storage/tapping costs, contact and Identity rules still apply. Server owners can disable this with `"enableAccountPowers": false` and restart; earned powers and saved assets are preserved. This does not grant operator permissions. Offline-mode usernames alone are not trusted. See [exact policy and UUIDs](docs/ACCOUNT_ACCESS.md) and [validation](docs/ACCOUNT_ACCESS_VALIDATION.md).

## Install / upgrade

Use `metallic-arts-fabric-0.7.0-alpha.2.jar`, not the sources jar. Install Fabric Loader 0.18.4+ and Fabric API 0.145.1+26.1 for exactly Minecraft 26.1. Put the mod jar in both client and server mods folders. Remove the previous Metallic Arts jar before adding this version. Keep a backup of existing worlds before upgrading.

Existing Phase 1 spiritweb saves load with an empty invested-reserve collection. Do not downgrade a world after adding newer blocks/items or canonical invested assets. Back up the whole world folder so player equipment and the world's charge/provenance ledgers stay together; copying an invested token into another world does not transfer its charge. Ores appear in newly generated chunks; existing terrain is not rewritten.

### Download a prebuilt development jar

1. Open [Build and test runs for this development branch](https://github.com/RashekKholin/metallic-arts-fabric/actions/workflows/build.yml?query=branch%3Aphase3-allomancy).
2. Choose the latest **successful** run for the version you want. Check its commit against the README/version in that branch.
3. Under **Artifacts**, download **metallicarts-build**. GitHub requires signing in to download workflow artifacts; artifacts can expire.
4. Extract the ZIP and find `metallic-arts-fabric-VERSION.jar` under the build/libs path. Use the ordinary jar, **not** the `-sources.jar`.
5. Put that jar and the matching Fabric API jar in your Minecraft 26.1 instance's `mods` folder. Use the same mod build on the client and server, with only one Metallic Arts jar installed.

There is no permanent GitHub Release download yet. If an artifact has expired, build from source using the instructions below. A green GitHub run covers compilation/unit tests/datagen; local game/client results are recorded separately in the validation guide.

## Start playing

Craft a **Metallurgy Table** from three iron ingots, three cobblestone and three planks in three horizontal rows. Fuel it with coal or another furnace fuel.

In **Shape**, turn an ingot into nuggets, one nugget into 100 portions, and a portion into flakes. Use the arrows to select an operation; hover the ghost item for requirements. Press **Start/Stop**. **Batch** repeats work while ingredients and output space permit.

Brew flakes with **water bottles and blaze powder in a normal Brewing Stand**. One flake fills one bottle. Drink the vial, then press **M** to see your ingested reserve. Every standard metal has this path.

Sneak-right-click the table with an empty hand to choose a Metallurgist specialty. An unemployed adult villager can claim it during working hours.

Read the [complete economy guide](docs/ECONOMY.md) for ore recovery, alloy ratios, specialties, invested material handling and data-pack customization.

## Included

- Sixteen standard metal IDs; ingots/nuggets, portions/flakes, coins, rings, bracelets, bracers, earrings, spikes and vials. Vanilla iron/copper/gold ingots and nuggets are reused.
- Six supporting materials: silver, nickel, lead, bismuth, carbon and silicon.
- Eight new ore families with stone/deepslate variants, mining tags, Fortune/Silk Touch loot, experience and data-driven Overworld distribution.
- A persistent server-owned Metallurgy Table with Refine/Alloy/Shape tabs, recipe selection, fuel, progress, batch processing and output preflight.
- **485** processing definitions: **357** ordinary refining/shaping/alloy operations and **128** invested-flaking definitions.
- Eight alloy formulas with preserved composition and viable/imperfect/invalid outcomes. Incorrect batches cannot become viable through reshaping.
- Four Metallurgist specialties, Novice–Master trade sets, deterministic category access, finite stock and bulk offers using vanilla trading behavior.
- Sixteen water-bottle brewing paths with ordinary fuel/timing and one-flake-per-bottle accounting.
- Server-validated ingestion, bottle return, reserve synchronization, Identity/charge retention and a read-only Spiritweb screen.
- Phase 1 persistent source-tracked power contracts, server configuration, provenance catalog and owner-only networking.
- Independent burning/flaring for all sixteen metals, including Steel, Iron, Pewter and Tin; server-controlled fuel costs, basic reciprocal forces, owner-only particle target lines, Pewter attributes/fatigue, Tin dark vision/hearing/overload and recoverable coin projectiles.
- Operator grant/revoke/fuel/inspect tools, rebindable push/pull/toss keys, M-menu burn controls and a mode HUD.

- Twelve contact slots and canonical world-bound metalmind accounting; Store/Off/Tap rates, Identity checks, duplicate-reference protection and all sixteen Feruchemical metals, separate five-sense Tin storage, finite body resources, individual Copper memories and typed Nicrosil access.
- Grid/wheel controls, coin pouch, strength-dependent heavy-block carrying and sensory cues. See the Feruchemy guide for exact controls, policies and limits.
- Canonical Hemalurgic spikes, typed donor provenance, decay, blood preservation, source-tracked anatomical implantation/removal and intentional lethal extraction. Player extraction is disabled by default and additionally requires consent and PvP permission.
- Stolen physical Allomancy/Feruchemy use the existing real handlers; innate strength/senses and a limited fortitude/instability handler exist. Native cognition, Connection/Identity, Fortune and Investiture now support bounded analysis, rapport/donor keys, Luck/opportunities and effective access scaling.

## Not implemented

Compounding, researched construct transformations, exceptional nonlethal Hemalurgy, god metals, natural survival power acquisition and mixed-metal vials are not implemented. Full causality, arbitrary language/faction systems, universal time simulation and cross-mod knowledge erasure are outside the bounded current handlers. Equipment crafted in normal play is initially uncharged. Direct testing grants and powered-NPC setup use operator tools; Hemalurgy can transfer a valid donor's actual source, but there is no automatic Mistborn/Feruchemist progression yet.

## Quick advanced-power test

Use operator grants plus ingested ordinary fuel, then M and [ / ] to choose an Allomantic group. Mental page selects emotions; K opens recorded history; J requests a paid predicted opening while burning Electrum. In F, K manages the selected coppermind, N chooses a new nicrosilmind’s owned ability type and U performs deliberately costed unsealing. Store Identity at rate4 for full suppression; normal storage caps at rate3. Follow [ADVANCED_ARTS.md](docs/ADVANCED_ARTS.md) for per-metal behavior, costs, unkeyed/unsealed steps and limitations.

## Quick physical-power test

In a cheats-enabled test world use `/metallicarts grantferuchemy @s iron 1`; hold one Iron ring/bracelet/bracer/earring, press **M → F**, select a slot and **Equip held**. Select a rate with **+/-**, then **Store**, **Off**, or **Tap**. **W** switches grid/wheel. Repeat with Steel, Pewter or Tin; select a new Tin mind's sense before equipping it. Each item keeps its own charge. **B** lifts/drops supported nearby blocks when strength permits. **C** tosses actual loose/pouched coins. Full instructions, cost formulas and administrative unkeying are in [FERUCHEMY.md](docs/FERUCHEMY.md).

## Quick Hemalurgy test

Read [HEMALURGY.md](docs/HEMALURGY.md) before testing the lethal interaction. In a controlled cheats-enabled world, set up a villager with an actual development donor grant, hold a compatible uncharged spike, aim at its chest within three blocks and press **M → H**. Select the stolen capability, turn **Intent: ON**, then **Extract aimed**. Insert the resulting charged item at a compatible point to gain its source; remove it to lose only that source. Blood collection/preservation, the full material table, player consent/policy and dangerous critical removal are documented in the guide. Natural powered-NPC progression is still planned.

See the [0.7 changelog](docs/CHANGELOG.md) for upgrade notes.

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

Server policy loads from `config/metallicarts-server.json`. The snapshot cooldown is enforced. `allowPlayerHemalurgy` defaults to false; enabling it alone is insufficient for player extraction, which also needs donor consent, valid reach/material and PvP permission. Ore distribution, processing recipes and trades are configured through data packs.

See [current Phase 6–7 validation](docs/PHASE67_VALIDATION.md), [historical Hemalurgy validation](docs/PHASE5_VALIDATION.md), [physical-power validation](docs/PHASE4_VALIDATION.md), [Phase 2 validation](docs/PHASE2_VALIDATION.md), [architecture](docs/ARCHITECTURE.md), [canon policy](docs/CANON.md), and [historical Phase 1 results](docs/VALIDATION.md).

## Sources

The [Fabric 26.1 guidance](https://fabricmc.net/2026/03/14/261.html) informed the unobfuscated Java 25/Loom setup and data-driven trading approach. Implementation was compiled and exercised against Minecraft **26.1**, including its local unobfuscated sources. Alloy quantities and recovery yields are explicitly documented gameplay interpretations, not verified universal canonical measurements.

Unofficial fan project. No book text or official illustrations are bundled.

## Development status and detailed future plan

| Phase | Status |
|---|---|
|1 Foundation|Implemented; persistent contracts, private networking, build scaffolding|
|2 Economy|Implemented; ore/trade → refine/alloy/shape → brew/ingest|
|3 Physical Allomancy|Playable alpha; multiplayer force accumulation fixed and exercised|
|4 Physical Feruchemy|Playable alpha; canonical contact/charge, five senses, carrying|
|5 Hemalurgy foundation|Playable alpha; extraction/implants/source tracking/decay/protections|
|6 Remaining Allomancy|Playable bounded handlers for all standard metals; approximation limits documented|
|7 Remaining Feruchemy|Playable body/memory/Spiritual handlers, typed Investiture and deliberate unsealed construction|
|Advanced systems/release|Still planned; details below|

## 1. Hardening and compatibility before more powers

- Exercise the existing multiplayer harness on authenticated accounts and a remote dedicated server; repeat under latency, packet loss, reconnects, different client frame rates and additional players. Local offline loopback evidence is distinct from public-server validation.
- Test actual protection/claim/PvP mods, especially Chromium/Nicrosil contact, emotions, lifting blocks, moving objects, extraction, inventory transfers and merchant price updates. Existing native protection events are not a guarantee of every mod's integration.
- Run sustained sessions with overlapping temporal bubbles, many entities/machines/projectiles and multiple concurrent users. Profile CPU, network traffic, save size, client interpolation and entity removal. Preserve force-order independence and bounded work.
- Expand adversarial tests for exhausted source removal, partial payments, death/respawn/dimension changes during every advanced effect, disconnect mid-memory transfer, capped body resources, duplicate/stale references and cross-dimension copies. Keep whole-world recovery instructions and fixtures current.
- Add explicit schema migration tooling and operator recovery/inspection commands. Never reconstruct charge from a preview or silently repair duplicated ownership. Test upgrades from archived real 0.5 worlds as well as codec fixtures; document irreversible changes/downgrade limits.
- Extract tunable advanced constants into validated server configuration: bubble rates/radii, emotion cadence/caps, body rates, healing/cure/emergency prices, opportunity scans, trade coefficients and unsealing costs. Current explicit defaults are documented policies, not fully exposed configuration.

## 2. Refine standard-metal interpretations

- Allomantic Bronze: short-pulse history, accessible sound signatures, sensory options, stronger/weaker concealment edge cases and broader Investiture detection only after explicit rules. Current ten-tick sampling can miss very brief burns.
- Zinc/Brass: richer eligible NPC reactions, opposing influences, awareness UI, resistances and recovery tuning; no unrestricted mind control. Expand NPC state support deliberately.
- Enhancement: research and test per-metal burst duration/output, protection-mod callbacks and simultaneous mutual contact. Current one-frame capped burst and one-second contact cadence are documented approximations. Add active-burning NPC targets with actual fuel/ownership rather than pretending every mob burns.
- Gold: richer actual character events, useful alternate-self choices and presentation; no fabricated biography. Electrum: projectile-specific physics, uncertainty, latency, more combat possibilities and eventual atium interaction. Predictions currently approximate bounded movement/projectiles.
- Temporal bubbles: preserve native clock accounting under rate changes, collision/entry/exit and nesting; add supported adapters where justified. Vehicles, player physiology, general redstone/random/scheduled ticks and arbitrary block entities currently use global time. An arbitrary local-time simulation is a separate research/performance task.
- Feruchemical Bronze/Brass/Cadmium/Bendalloy: longer balance sessions for fatigue/sleep, temperature/environment, oxygen and food/saturation; adapters for gas, seasons, survival/temperature mods. Body escrow resets on death; evaluate that policy without duplication or death loops.
- Zinc/Electrum: broaden useful cognition and adversity interactions beyond current action/analysis/emotional resistance handlers. Keep these distinct from physical speed and strength.
- Copper: capture more actual discoveries/events, naming/search and memory organization, permission-safe sharing, reliable long-term journal limits. Integrate other knowledge systems only through explicit adapters; real player knowledge cannot be erased.
- Gold: anatomical injury/status adapters, healing priorities, interrupted/cancelled damage tests and emergency-balance review. No current limb regrowth/age/disease simulation or exceptional Hemalurgic survivability.
- Identity/Connection: broader supported social/faction/language systems, per-person relationships and documented access policies. Current Connection is villager trade rapport plus actual regional familiarity; Identity is filling/access/resistance, not arbitrary impersonation.
- Fortune: more meaningful bounded opportunity providers, explicit uncertainty and balance across loot contexts. Vanilla Luck alone does not change every random roll; no arbitrary destiny/causality simulation.
- Nicrosil/unsealed equipment: research fuller medallion/Excisor behavior, multiple ability constructions, production and accessible ownership previews. Current one-type finite-access construction is an explicit game adaptation, not a verified canonical medallion factory.

## 3. Compounding

1. Specify the transaction contract: distinguish ordinary fuel, invested material, stored attribute/ability content, mass, Identity and ownership.
2. Define supported per-attribute outputs and costs. Typed abilities and discrete memories cannot silently become generic energy.
3. Consume the exact authorized physical invested material once; invalidate its previous canonical use and preserve residual mass/charge accurately. Prevent burning and tapping the same charge twice, copying/splitting exploits and invalid cross-world references.
4. Implement amplification through separate Allomantic/Feruchemical accounting, with interruptions, capacities, source shutdown and usable feedback.
5. Add unit, server, client, two-client and dedicated restart regressions before exposing normal gameplay recipes. Compounding is currently absent.

## 4. Advanced Hemalurgy and constructs

- Research canonical evidence for supported placements, exceptions and construct dependencies. Existing twelve-point human grid is an explicitly inferred playable layout.
- Implement distinct researched Inquisitor/koloss/kandra templates, anatomy/body changes, required spikes, removal dependencies and linchpin consequences; do not use one generic stat multiplier as a full construct.
- Expand spiritual vulnerability and emotional control interactions while retaining consent/protection policy for players. Current fortitude/instability/passive handlers do not simulate every construct psychology.
- Implement exceptional/nonlethal extraction only with explicit rules, consequences and rollback/resource accounting. Normal extraction currently requires lethal donor loss and refuses death-prevention equipment/armed Gold recovery.
- Research unusual Invested/god-metal spikes, multiple-power extraction and native-attribute consequences separately; source-scoped theft must preserve unrelated grants and keep extracted-source tombstones.

## 5. Survival acquisition and world progression

- Design natural access to Misting/Mistborn/Ferring/Feruchemist powers, rarity and progression, with configurable server policy. Ordinary new characters start powerless; the documented account perk is a configurable exception. operator grants and powered-donor setup are development tools.
- Add genuinely powered NPCs with actual spiritwebs, fuel, AI and loot/protection behavior. Hemalurgy currently transfers actual configured donors; it does not seed a world population of powered NPCs.
- Add discovery/tutorial progression, balanced recipes/trades and resource scarcity. Decide how unsealed equipment is acquired and produced in survival.
- Optional mixed-metal vials need composition, purity, capacity, invested-subset handling, brewing/ingestion UI and conservation tests. Expand material/tag interoperability and deliberate Investiture resistance.
- Add god metals/alloys only after explicit researched rules, source provenance and separately scoped effects. No fictional universal compatibility promises.

## 6. Presentation, accessibility and release

- Finish wheel icons/highlighting, readable tooltips, clear typed-charge/access/Identity states, status messages, key-conflict handling and low-resolution/high-GUI-scale layouts. Keep source-specific shutdown understandable.
- Add optional sound/visual accessibility controls, color-independent cues, screen-reader/localization coverage and translations. Current advanced explanatory strings are mostly English.
- Add in-game tutorials and examples for every Art, metal, body cost, privacy, equipment transfer and unsealing. Keep the README and test guide synchronized.
- Publish permanent versioned GitHub Releases with installable jar, sources, checksums, changelog and exact matching dependencies; CI artifacts currently expire and require GitHub sign-in.
- Add platform coverage (Linux/macOS clients/dedicated servers), longer save/recovery and compatibility matrices, automated repeated multiplayer runs and performance baselines. GitHub CI currently validates compile/unit/datagen; local native tests are separately recorded.
- Finish attribution/license review and public issue templates; label experimental approximations and unsupported interactions in release notes. A playable alpha is not a finished public release.
