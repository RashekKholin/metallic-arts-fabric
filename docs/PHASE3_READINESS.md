# Phase 3 readiness

Prepared October 31, 2026, against commit `fce5ea1f37c4a46f7a9b03a1703431eec72f574a` (version 0.2.0). Gemini contributed no code. Phase 3 power mechanics have not been implemented.

## Verified starting point

- Local checkout was clean and matched freshly fetched `origin/main` exactly.
- Existing GitHub Actions run 41181341379 was rechecked and is successful. This is evidence for the published baseline, not a new CI run for this document.
- `build runDatagen runClientGameTest -PacceptMinecraftEula=true` passed. The server runner executed all 16 required tests; both existing client test entrypoints completed.
- A separate forced `test --rerun-tasks` run passed all 22 unit tests with zero failures/errors. The initial combined build had reused the unit task, so it was explicitly rerun.
- Separate `runRestartSmoke` seed and verify processes both wrote fresh PASS results. Player powers/reserves/Identity, invested vial data, workstation progress and an interrupted brew survived the JVM boundary.
- The production jar still identifies itself as 0.2.0 and excludes test fixtures.
- No gameplay code, configuration schema, resource quantities or version number was changed during this preparation.

No baseline test failures were found. This is readiness to implement Phase 3, not evidence that its powers already work.

## Known verification limits

The existing client suite uses one real test client, including a local dedicated-server connection. The player-isolation test uses two server-side player instances. Two simultaneous live clients, authenticated online multiplayer, long multiplayer sessions and broad compatibility/balance testing remain unverified.

The respawn test invokes the actual server player-replacement path with KILLED reason; it does not exercise every death cause or a manual death-screen interaction. Existing development-account authentication and upstream compatibility/deprecation warnings remain and are described in PHASE2_VALIDATION.md.

## API checks for implementation

The pinned local Fabric libraries expose:

- `ServerTickEvents.END_SERVER_TICK` for a single server-owned update loop.
- `HudElementRegistry` for HUD registration.
- `HudElement.extractRenderState(GuiGraphicsExtractor, DeltaTracker)` for HUD extraction.

These signatures were inspected in the actual dependency jars. The existing screen also compiles against `GuiGraphicsExtractor` and `extractRenderState`. Do not copy older `HudRenderCallback`/`GuiGraphics` examples.

Official toolchain reference: https://fabricmc.net/2026/03/14/261.html . The exact-version web Javadoc pages were inaccessible during this check; local dependency inspection supplied the API evidence instead.

Keep Minecraft 26.1, Java 25 and the existing unobfuscated Loom setup. Upgrade dependencies only for a demonstrated need.

## Implement in this order

### 1. Reserve-accounting boundary

Start here, before consuming fuel or applying effects. The persistent record lives in `domain/Spiritweb.java`; ingestion is in `economy/Ingestion.java`.

Existing ingestion adds invested material to BOTH total reserve mass and the invested-subset records. Never count that subset as additional fuel. Current constructors/codecs do not enforce every cross-field invariant a burn engine will require, so add a central accounting service and meaningful tests before introducing burn mutations.

Required invariants:

- Per-metal total reserve mass is nonnegative and at most MAX_RESERVE.
- Invested-subset mass never exceeds its corresponding total reserve.
- Invested instance IDs are unique; records retain their charge, material and Identity.
- Ordinary available fuel equals total mass minus reserved invested-subset mass.
- Every mutation replaces the immutable attachment value; related quantities update together.
- Old schema-1 saves remain readable. Invalid/new data must not silently reset a character's Identity or grants.

For the first burn engine, reserve invested fuel from ordinary burning until its interaction with Compounding is explicitly designed. Show this distinction honestly in the UI rather than destroying charge or pretending all displayed mass is ordinary burnable fuel.

Add tests for mixed ordinary/invested reserves, full invested reserves, mismatched totals, duplicate instances, overflow and save round-trips. Do not introduce burn ticks yet until the accounting tests pass.

### 2. Controlled power assignment and configuration

Add operator-only grant/revoke/inspect commands using existing `PowerGrant` records. Preserve grant/source UUIDs and unrelated abilities. Normal players and incoming client packets cannot grant ownership. Drinking remains unrelated to power acquisition.

Preserve strongest-applicable-grant resolution unless new research justifies a documented change. Keep the current server-config file backward compatible: adding required Gson record fields with zero defaults can break existing files. Supply explicit defaults/migration or use a separate versioned Allomancy settings file.

### 3. Server-owned burn state

Introduce a transient runtime service, separate from persistent ownership and reserves. Support OFF/BURN/FLARE independently per enabled metal, simultaneous burns, configurable rates and automatic stop on fuel exhaustion or lost access.

Use integer/fixed-point accounting. Repeatedly stopping, starting or reconnecting must not erase unpaid fractions and produce free effects. Apply no effect unless its fuel cost is covered. Stop and clean up on relevant player lifecycle transitions; never restore an active burn after reconnect/restart.

Enable effects only for supported handlers. The other twelve metal IDs must not consume fuel while doing nothing. Initially no normal character has powers, so integration tests need the controlled assignment path.

### 4. Intent networking and controls

Extend `network/ModNetworking.java` without turning the current read-only snapshot request into an ownership-edit endpoint. New packets express intent only. Check ownership, fuel, player state, supported metal, target validity/range, request limits and stale held inputs on the server.

Private Identity, source grants and invested records remain owner-only. Use appropriately limited updates; do not broadcast full private spiritweb records every tick. Preserve M-key access and add rebindable controls and accurate low-quantity reserve display—the current two-decimal gram display is insufficient for fine burn consumption.

### 5. Shared detection and physics

Add a separate reusable targeting/physics layer. Use explicit metal tags/profiles, bounded searches of loaded data, stable targets, effective mass, anchors, reciprocal forces, combined vectors and finite movement limits. Research aluminum, alloys, ores and invested resistance rather than assigning unverified blanket rules.

Keep effective physical mass separate from the economy's abstract credited mass. Preserve vanilla collisions and validate multiplayer motion; do not substitute creative flight or arbitrary teleportation for Steel/Iron forces.

### 6. First four powers

Implement Steel, Iron, Pewter and Tin in that order. Include metal lines/target feedback and usable coin projectiles; clean Pewter modifiers and exhaustion; useful Tin sensory changes, overload and accessibility options. Label Minecraft-specific approximations and uncertain canon in the existing provenance catalog.

### 7. Acceptance and release

Each layer needs unit/game tests and actual client checks. Before declaring version 0.3.0 complete, demonstrate drinking, activation, normal/flaring consumption, simultaneous powers, depletion shutdown, Steel/Iron forces, Pewter/Tin behavior, lifecycle cleanup, persistence and multiplayer authorization. Retain the economy regressions and inspect the rendered UI.

Perform the outstanding two-client check before claiming full multiplayer verification. Document any unavailable scenario. Finish with a tested jar, updated guides/provenance/validation, a source commit/push and a checked CI result.

## Scope boundary

This preparation does not authorize treating scaffolding as working powers. Feruchemical effects, Hemalurgic extraction, Compounding, god metals, survival power-acquisition progression and the remaining twelve Allomantic effects remain outside the first physical-Allomancy milestone.

The prepared local branch is `phase3-allomancy`. Begin implementation with the reserve-accounting boundary above; do not jump directly to force/rendering code.
