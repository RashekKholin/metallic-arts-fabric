# The Metallic Arts

Minecraft Java **26.1** • Fabric • Java **25** • mod ID `metallicarts` • version **0.5.0-alpha.1**

The Phase 2 metal economy remains available: **ores → concentrates → refined metal → measured flakes → Brewing Stand vials → persistent ingested reserves**. This development build includes server-controlled Steel, Iron, Pewter and Tin Allomancy and physical Feruchemy, with individual contacted metalminds. Drinking supplies fuel; it never grants a power.

**Physical powers are a playable alpha.** Read the [Allomancy guide](docs/ALLOMANCY.md), [Feruchemy guide](docs/FERUCHEMY.md) and [Phase 4 validation record](docs/PHASE4_VALIDATION.md). Selection wheels, a coin pouch, physical carrying and separate Tin sensory minds are available. Two simultaneous live clients and broader compatibility remain acceptance gaps.

**Phase 5 Hemalurgy foundation is now playable:** intentional donor extraction, individual charged spikes, anatomical insertion/removal, source-specific art access, decay and blood preservation. Read [HEMALURGY.md](docs/HEMALURGY.md) for the setup, protections, compatibility table and explicit limits. Cognitive/Spiritual stolen types retain labelled hooks; their advanced effects and full construct templates remain future work.

**Observed local validation:** 55 unit tests, all 44 required server game tests, all five client suites, datagen and separate-process invested-data restart checks passed. [Validation details and limits](docs/PHASE5_VALIDATION.md).

## Install / upgrade

Use `metallic-arts-fabric-0.5.0-alpha.1.jar`, not the sources jar. Install Fabric Loader 0.18.4+ and Fabric API 0.145.1+26.1 for exactly Minecraft 26.1. Put the mod jar in both client and server mods folders. Remove the previous Metallic Arts jar before adding this version. Keep a backup of existing worlds before upgrading.

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
- Experimental independent burning/flaring for Steel, Iron, Pewter and Tin; server-controlled fuel costs, basic reciprocal forces, owner-only particle target lines, Pewter attributes/fatigue, Tin dark vision/hearing/overload and recoverable coin projectiles.
- Operator grant/revoke/fuel/inspect tools, rebindable push/pull/toss keys, M-menu burn controls and a mode HUD.

- Twelve contact slots and canonical world-bound metalmind accounting; Store/Off/Tap rates, Identity checks, duplicate-reference protection and separate Iron/Steel/Pewter/five-sense Tin storage.
- Grid/wheel controls, coin pouch, strength-dependent heavy-block carrying and sensory cues. See the Feruchemy guide for exact controls, policies and limits.
- Canonical Hemalurgic spikes, typed donor provenance, decay, blood preservation, source-tracked anatomical implantation/removal and intentional lethal extraction. Player extraction is disabled by default and additionally requires consent and PvP permission.
- Stolen physical Allomancy/Feruchemy use the existing real handlers; innate strength/senses and a limited fortitude/instability handler exist. Other typed capabilities are explicitly marked as planned effects.

## Not implemented

The remaining twelve Allomantic effects, the remaining twelve Feruchemical attributes, full cognitive/Spiritual stolen-attribute handlers, researched construct transformations, exceptional nonlethal Hemalurgy, Compounding, unsealed metalminds, god metals, natural survival power acquisition and optional mixed-metal vials are not implemented. Equipment crafted in normal play is initially uncharged. Direct testing grants and powered-NPC setup use operator tools; Hemalurgy can transfer a valid donor's actual source, but there is no automatic Mistborn/Feruchemist progression yet.

## Quick physical-power test

In a cheats-enabled test world use `/metallicarts grantferuchemy @s iron 1`; hold one Iron ring/bracelet/bracer/earring, press **M → F**, select a slot and **Equip held**. Select a rate with **+/-**, then **Store**, **Off**, or **Tap**. **W** switches grid/wheel. Repeat with Steel, Pewter or Tin; select a new Tin mind's sense before equipping it. Each item keeps its own charge. **B** lifts/drops supported nearby blocks when strength permits. **C** tosses actual loose/pouched coins. Full instructions, cost formulas and administrative unkeying are in [FERUCHEMY.md](docs/FERUCHEMY.md).

## Quick Hemalurgy test

Read [HEMALURGY.md](docs/HEMALURGY.md) before testing the lethal interaction. In a controlled cheats-enabled world, set up a villager with an actual development donor grant, hold a compatible uncharged spike, aim at its chest within three blocks and press **M → H**. Select the stolen capability, turn **Intent: ON**, then **Extract aimed**. Insert the resulting charged item at a compatible point to gain its source; remove it to lose only that source. Blood collection/preservation, the full material table, player consent/policy and dangerous critical removal are documented in the guide. Natural powered-NPC progression is still planned.

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

See [current Hemalurgy validation](docs/PHASE5_VALIDATION.md), [physical-power validation](docs/PHASE4_VALIDATION.md), [Phase 2 validation](docs/PHASE2_VALIDATION.md), [architecture](docs/ARCHITECTURE.md), [canon policy](docs/CANON.md), and [historical Phase 1 results](docs/VALIDATION.md).

## Sources

The [Fabric 26.1 guidance](https://fabricmc.net/2026/03/14/261.html) informed the unobfuscated Java 25/Loom setup and data-driven trading approach. Implementation was compiled and exercised against Minecraft **26.1**, including its local unobfuscated sources. Alloy quantities and recovery yields are explicitly documented gameplay interpretations, not verified universal canonical measurements.

Unofficial fan project. No book text or official illustrations are bundled.

## Development status and detailed future plan

This is a living implementation plan. A planned entry does not mean its mechanic exists. Future releases will update this section alongside the controls, recipes, configuration, migration notes and test evidence.

### Implementation order and completion rules

Work proceeds in the order below. Shared data and server validation come before dependent powers. A phase is complete only when its usable behavior, documentation and recorded validation agree; registering an item or declaring a data contract does not complete its gameplay system.

1. **Complete multiplayer acceptance for the shared foundation.** Individual contact/charge, storing/tapping, sensory types, carrying, wheels and coin pouches are implemented and locally tested. Next exercise two simultaneous live clients, privacy, interacting forces, contested assets, disconnects during actions and compatibility with real protection mods. Keep the exact lifecycle/save/restart checks as regressions.
2. **Extend the Hemalurgy foundation deliberately.** Core typed extraction, canonical spikes, anatomical ownership, removal, decay and blood preservation exist. Research exact placements and exceptions further, test successful consensual player extraction, and improve balancing/feedback. Keep cognitive/Spiritual types visibly labelled as hooks until their dependent effect systems exist. Build full construct templates after the relevant body/emotional/Investiture systems are available.
3. **Harden transfers and saved-world upgrades.** Expand cross-player asset contention, removal during active use, recovery from invalid references and future schema migrations. Test a new server process loading invested items and provenance after each format change. Keep whole-world backup/recovery instructions current; fix authority, duplication or persistence failures before expanding powers.
4. **Complete remaining Allomancy in dependency order.** Start with Bronze/Copper detection and concealment; add the emotional state model for Zinc/Brass; implement Aluminum/Duralumin and contact Chromium/Nicrosil using explicit per-metal amplification/depletion handlers. Build recorded history before Gold, bounded prediction before Electrum, and a tested local-time model before Cadmium/Bendalloy. Each pair needs its own gameplay, fuel, interruption and multiplayer acceptance tests.
5. **Complete remaining Feruchemy by subsystem.** Introduce fatigue/temperature/respiration/nutrition before their metal effects; implement healing and determination deliberately; then build memory, Identity, Connection, typed Investiture and Fortune. Identity and Investiture support must precede ordinary unkeyed filling and unsealed equipment. Test costs and benefits together so storage cannot manufacture value from a capped or absent disadvantage.
6. **Connect the advanced systems.** Add Compounding only after ordinary fuel, stored attributes and Identity have independent accounting. Implement researched construct templates and their critical dependencies, then survival acquisition and world content. Add migration/recovery tools before changing saved schemas or invested-item formats.
7. **Prepare a public release.** Finish tutorials, accessibility, localization and configuration guidance; run longer sessions, compatibility and performance checks; publish versioned installable jars with checksums, source, changelogs and repeatable test instructions. Keep known approximations and unsupported interactions visible.

Every implemented step should update this README's current-feature list, installation/download instructions, controls and test results. Validation notes must identify the tested version, actual commands and outcomes, remaining gaps and any manual checks a player still needs to perform. A planned test is not a passed test.

| Stage | Current status | Acceptance target |
| --- | --- | --- |
| Phase 1: foundation | Implemented and validated | Build, launch, persistent data, owner-only networking |
| Phase 2: metal economy | Implemented and validated | Mine/trade → refine/alloy/shape → brew → ingest |
| Phase 3: physical Allomancy | Playable alpha; unfinished work below | Usable Steel/Iron/Pewter/Tin with robust controls and multiplayer physics |
| Phase 4: physical Feruchemy | Implemented playable alpha; validation limits documented | Separate contacted metalminds, meaningful storing costs and adjustable tapping |
| Phase 5: Hemalurgy foundation | Playable transactions and ownership; advanced types/constructs remain limited | Obtain, implant and remove source-tracked spikes with real consequences |
| Phase 6: remaining Allomancy | Planned | All sixteen standard Allomantic effects |
| Phase 7: remaining Feruchemy | Planned | All sixteen Feruchemical attributes and their dedicated supporting systems |
| Advanced systems and release | Planned; subject to documented research | Compounding, constructs, progression, compatibility and release-quality validation |

### Physical foundation: implemented scope and remaining polish

- Grid/wheel switching is implemented. Refine target highlighting and accessible line rendering beyond the current particle feedback; preserve rebinding and server validation.
- Coin-pouch storage and component-preserving tossing are implemented. Broaden selection usability and compatibility checks without creating ammunition.
- Distinct bounded touch/smell/taste feedback and five-sense metalminds are implemented. Improve cue presentation and accessibility; this is not a complete sensory simulation.
- Broaden explicit metal profiles and compatibility tags. Document aluminum/alloy/ore and Investiture-resistance policies rather than treating every vaguely metallic block alike.
- Exercise interacting forces from multiple Allomancers, collision/landing behavior, projectile synchronization, sustained play, two simultaneous live clients and mod compatibility. Current active death/respawn and dimension checks must remain regressions.

### Phase 4: implemented physical Feruchemy and its acceptance contract

The equipment, accounting and first-four-metal behaviors below exist in this alpha. Remaining acceptance limits are listed in the validation record; later Identity/unsealed/Compounding systems remain planned.

**Equipment and persistence**

- Support distinct rings, bracelets, bracers, earrings and compatible metalmind forms through the metal economy.
- Give each invested item an individual ID, composition, mass, capacity, stored attribute, charge and Identity key. Preserve separate contents on otherwise identical items.
- Add dedicated wearable/contact slots and validate actual equipped contact before allowing use. Unequipping must stop the associated action.
- Reject unauthorized keyed use; support unkeyed compatibility while still requiring the relevant Feruchemical ability. Reserve explicit contracts for later unsealed constructions without accidentally granting their powers.
- Keep charge authoritative across equip/unequip, death, transfers and reload. Prevent copied/split/stacked item data from multiplying spendable charge.

**Controls and accounting**

- Add a Feruchemy selection wheel and per-metalmind Store / Inactive / Tap controls, adjustable rates, capacity displays and informative tooltips.
- Storing must surrender the relevant attribute before crediting charge. Tapping must spend charge before producing its benefit.
- Implement high-rate tapping inefficiency, capacity/depletion shutdown and simultaneous independent attributes. Prevent contradictory actions or capped effects from producing free stored charge.
- Integrate with Allomantic modifiers without deleting vanilla effects or other mods' modifiers. Document units, limits and every numerical gameplay adaptation.

**First four metals**

- **Iron:** store/tap weight and connect effective body mass to momentum, recoil, anchors and incoming metal forces.
- **Steel:** store/tap physical speed, including movement and appropriate physical actions; retain server authority and attack/exploit limits.
- **Pewter:** store/tap strength, with meaningful weakness while storing and stronger melee/physical capability while tapping. Add carrying/heavy-object and visual support where the Minecraft adaptation exposes those mechanics.
- **Tin:** separate sight, hearing, touch, smell and taste charges. A sensory metalmind must retain which sense it contains; one pool must not masquerade as all five.

**Acceptance:** equip multiple distinct metalminds, store at a measurable personal cost, tap at adjustable rates, combine with Allomancy, reject foreign Identity and duplicated items, and preserve exact data through real save/lifecycle tests.

### Phase 5: Hemalurgy foundation, implemented scope and remaining extensions

The core extraction/implantation/accounting path below is implemented. Specific future-effect hooks and construct gaps are identified here and in the Hemalurgy guide; typed charge does not complete its associated advanced gameplay.

**Typed spikes**

- Extend the sixteen existing spike forms with typed charge: stolen art/attribute, donor, material, original potency, extraction loss and current potency.
- Allomantic/Feruchemical typed grants and innate physical handlers exist. Cognitive and Spiritual types retain explicit contracts; implement memory/cognition, Connection/Identity, Fortune/destiny and Investiture effects as their dependent systems become available. A stored type alone is not a functioning effect.
- Implement outside-body decay, preservation by implantation and a blood-preservation path, with researched behavior and clearly labeled numerical interpretations.
- Prevent invalid stacking, copied charge, duplicate implantation and repeated theft of an already-extracted attribute. Keep unusual Invested/god-metal extensions separate from unverified mechanics.

**Binding and implantation**

- Add unique anatomical binding-point IDs grouped by body region, compatible material/charge rules and a data-driven evidence catalog.
- Clearly distinguish documented placements from inferred gameplay placements. Do not present a guessed complete binding-point map as canon.
- Add an anatomical spike inventory with occupied/empty points, compatibility feedback, charge inspection and ability previews.
- Implement insertion/removal, source-specific ability grants and removal, recalculation after every change, multiple implanted spikes and duplicate-power resolution.
- An explicitly dangerous critical-point removal hook, instability penalty and vulnerability score exist. Build researched Inquisitor/koloss/kandra templates, complete dependencies, psychological/emotional interactions and exceptional survivability; the current human layout is not a completed construct simulation.

**Acquiring charge**

- Add an intentional in-world extraction interaction that validates the donor's actual attribute, spike material, placement and Intent before changing anything.
- Apply donor consequences and extraction loss; create exactly one correctly charged item. Failed or protected interactions must not consume the spike, steal a power or create charge.
- Keep player-to-player extraction under server policy and multiplayer protections; it must not become enabled merely by installing the update.
- Implement exceptional/nonlethal methods only when their rules are deliberately supported and documented.

**Acceptance:** acquire a real charged spike, insert it at a compatible point, gain its sourced ability, remove it and lose only that spike's grants; verify donor effects, protection rules, decay, transfer, survival and persistence. See [Phase 5 validation](docs/PHASE5_VALIDATION.md) for actual results, remaining live-multiplayer checks and the distinction between working effects and typed hooks.

### Phase 6: complete the remaining Allomantic metals

- **Bronze/Copper:** spatial Allomantic pulses, metal-specific signatures, concealment, detection strength comparisons and accessible audio/visual feedback.
- **Zinc/Brass:** emotional profiles, existing emotion intensity, resistance and awareness; connect those states to eligible AI behavior without inventing unrestricted mind control.
- **Aluminum/Duralumin:** self-reserve depletion and short amplified bursts of concurrently active eligible metals, with metal-specific handlers and correct depletion/interruption behavior.
- **Chromium/Nicrosil:** server-validated contact with another Allomancer, reserve depletion and forced amplification, including permissions and simultaneous effects.
- **Gold:** recorded character history and meaningful alternate-self visions, with clear limits on inferred history.
- **Electrum:** bounded movement/combat prediction, uncertainty, branching shadows, performance limits and future-sight interaction hooks.
- **Cadmium/Bendalloy:** anchored temporal bubbles, collapse/entry/exit rules, local update-rate contexts, projectile boundaries and overlapping users. Unsupported block/redstone/mod behavior must have explicit approximations.
- **Acceptance:** all sixteen have real effects, correct fuel/strength interactions and multiplayer tests; registry entries or cosmetic shadows alone do not count.

### Phase 7: complete the remaining Feruchemical attributes

- **Bronze:** wakefulness, fatigue, rest and sleep interactions.
- **Brass:** body warmth, biomes/weather/water/heat exposure and consequences.
- **Zinc:** cognitive processing and useful reaction/information assistance.
- **Copper:** individual persistent memories, discovery/knowledge capture, loss of access while stored and restoration when tapped.
- **Gold:** health storage disadvantages, ordinary/severe injury healing and charged emergency recovery.
- **Bendalloy:** nutrition and saturation accounting, including deliberate limits beyond normal satiety.
- **Cadmium:** breath storage/tapping, underwater endurance and atmosphere compatibility.
- **Electrum:** determination and adversity-related effects, kept distinct from physical strength and health.
- **Aluminum:** Identity suppression and Identity-free filling of eligible metalminds.
- **Duralumin:** Connection to people, factions, places and supported language/social systems.
- **Nicrosil:** typed Investiture and stored ability access, then deliberately constructed unsealed metalminds.
- **Chromium:** Fortune expressed through bounded intuition/opportunities, with documented uncertainty and no claim to simulate arbitrary real causality.
- **Acceptance:** each attribute has its own meaningful cost, storage and benefit, appropriate UI and persistence; all sixteen interact correctly with the shared equipment system.

### Later interconnected systems

- **Compounding:** distinguish ordinary metal, Feruchemical charge and the Allomantic fuel path; preserve Identity, consume the correct invested material, prevent double-counting and implement each supported attribute's output deliberately.
- **Advanced Hemalurgy:** researched construct templates, linchpin dependencies, spiritual vulnerability and interaction with emotional/Investiture systems. Add unusual/god-metal rules only with evidence or clearly marked interpretations.
- **Survival progression:** balanced power acquisition, compatible NPC/world content, recipes, trades, discovery/tutorials and meaningful resource scarcity. Administrative testing tools must remain distinct from progression.
- **Economy expansion:** optional mixed-metal vials, invested equipment workflows and compatibility with additional material/item sources.
- **Presentation/accessibility:** finished wheels, icons, tooltips, sound/visual options, localization and understandable controls for all three Arts.
- **Public release:** migrations and recovery guidance, configuration documentation, regression and two-client tests, long-session/performance checks, compatibility testing and permanent versioned downloads with clear changelogs.
