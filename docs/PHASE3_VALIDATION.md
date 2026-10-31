# Phase 3 alpha validation

Date: 2026-10-31. Version: 0.3.0-alpha.1. Baseline: Phase 2 commit fce5ea1f37c4a46f7a9b03a1703431eec72f574a. Work is on phase3-allomancy; main remains the stable Phase 2 branch.

**This is a tested development alpha, not completed Phase 3.** See ALLOMANCY.md for exact behavior and unfinished scope.

## Environment

Windows, Temurin Java 25.0.5, Gradle 9.4.0, Minecraft 26.1, Fabric Loader 0.18.4, Fabric API 0.145.1+26.1 and Loom 1.15.5. Dependencies remain pinned; no Minecraft upgrade was introduced. The user authorized EULA acceptance for local tests.

## Results

- Unit suite: **39 tests, zero failures/errors**. Includes ordinary/invested accounting, codec/save round trips, duplicate/inconsistent subset refusal, exact fractional costs, stop/restart rounding, ownership, revocation, depletion, simultaneous costs and unsupported handlers.
- Server game suite: **24 required tests passed**. Retains the sixteen foundation/economy checks and adds eight command/Allomancy checks.
- Client suite: **all three entrypoints passed**: FoundationClientGameTest, EconomyClientGameTest and AllomancyClientGameTest.
- Full local command: `build runDatagen runClientGameTest -PacceptMinecraftEula=true --console=plain` passed. The final packaging command `build -x runGameTest` also passed with unit tests rerun after the last sound-gain/tag-label adjustment.
- A final `runClientGameTest` rerun after that adjustment passed in 1m 8s, including all three entrypoints.
- Separate dedicated JVM persistence: fresh seed and verify result files both report **PASS**. These exercise real player/item storage plus interrupted metallurgy/brewing state.
- Production jar inspected for common/client power classes, client mixin configuration and version metadata. Game-test fixtures are excluded.
- Git whitespace check passed.

Installable jar SHA-256: `84223c217fccd70664c8584d94188957e1dc7993014d719d63f7dcd37cce7bec`.

## What the new tests actually demonstrate

Server tests execute the registered operator commands through the command dispatcher, deny commands with NO_PERMISSIONS, reject malformed input without mutation, preserve grant provenance and revoke only the selected grant. Pewter pays fuel and cleans its own modifiers while retaining an unrelated modifier. Unsupported requests do not activate. Stopping clears active modes.

Physics tests check opposite motion and equal opposite momentum before caps, player lift from a downward anchor push, finite speed limits and nonfinite-vector refusal. Aluminum, carbon and invested forms are excluded. A real thrown coin hits terrain, returns exactly one item and preserves its component payload.

The real-client test clicks the M-menu burn buttons, receives authoritative mode updates, verifies Tin's lightmap hook, checks simultaneous fuel use and Pewter's modifier, holds the real Steelpush key to lift the player off an iron anchor, cycles flare/off, revokes an active power and reopens the saved world. Remaining fuel and Identity persist; modes and Pewter modifiers do not resume. Malformed unsupported client intent does not activate another metal.

The existing client entrypoints also exercise owner-only snapshots on a local dedicated server, logout/login, economy/worldgen, machine menus, saved inventory and the actual server player-replacement path.

Screenshots of the alpha menu and Steel push were inspected. A small-window overlap found during review was corrected by tightening row spacing; status and controls remain visible at the suite's 854x480 window.

## Failures found and corrected

The first client test exposed Tin's hook missing an early return in lightmap extraction. Injection now covers every return; the same direct lightmap assertion passes.

A later Pewter assertion incorrectly assumed vanilla attack damage was greater than one. It was corrected to inspect the actual mod-owned +3 modifier; server tests independently check additive effect and cleanup behavior.

The first compile also caught a compact-record constructor capturing a reassigned map in a lambda. This was corrected before the successful accounting/build runs.

## Limits and remaining Phase 3 work

- Two simultaneous live clients and authenticated online multiplayer are **not verified**. Two separate server player instances plus one real dedicated-server client are not equivalent to that test.
- Active-power death/respawn and dimension transitions need additional real-client acceptance tests. Cleanup exists, but the current active-power client test covers stop, revocation, disconnect and world restart; the historical respawn check covers data copying.
- Single aimed target only. No multi-target selection, radial interface, comprehensive metal profiles, coin pouch or advanced Investiture resistance.
- Particle lines are an initial target cue, not a finished through-wall line renderer. Particle settings can affect their appearance.
- Tin implements vision and environmental hearing only. Smell, taste, touch, zoom, sophisticated filtering and savantism remain absent. Audio quality/gain and overload comfort have not been quantitatively or perceptually evaluated.
- Long multiplayer sessions, interacting simultaneous users, broad mod compatibility, large entity crowds, balance and performance stress tests remain outstanding.
- Automatic survival acquisition is absent; tests use explicit administrative grants. The other twelve Allomantic effects and advanced Metallic Arts remain disabled.

Development-account Realms/profile-key authentication messages, Fabric's Java-25 compatibility warning and upstream deprecation/Gradle warnings appeared in local runs. They did not fail the successful suites. These results do not establish authenticated online-service compatibility.

## Reproduce

Use JDK 25 and the committed wrapper:

```powershell
.\gradlew.bat build runDatagen runClientGameTest -PacceptMinecraftEula=true --console=plain
.\gradlew.bat runRestartSmoke -PsmokeMode=seed -PacceptMinecraftEula=true --console=plain
.\gradlew.bat runRestartSmoke -PsmokeMode=verify -PacceptMinecraftEula=true --console=plain
```

Delete the two old restart result files before using those files as fresh evidence. The local smoke server binds to loopback. The local complete command includes real game tests; the existing GitHub workflow runs compilation, unit tests and datagen with game tests explicitly excluded. A passing remote build must not be described as remote game/client validation.
