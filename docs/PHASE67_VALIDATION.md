# Phase 6–7 validation — 0.7.0-alpha.1

Environment: Minecraft 26.1, Java 25.0.5, Fabric Loader0.18.4, Fabric API0.145.1+26.1, Loom1.15.5, Gradle9.4.0, Windows. Local Minecraft EULA acceptance was authorized. Results below are updated only after observed completion.

## Confirmed checks

- **66 unit tests passed, zero failures/errors.** Covers all sixteen burn channels, existing economy/ownership/charge contracts, new body bounds and round trips, one canonical copy of each stored memory, old physical asset optional-field decoding, foreign-key preservation, typed unsealed content and provenance classification.
- **61 required server game tests passed.** Includes existing44 Phase5 cases, opposing-caster force-order regression, six remaining-Allomancy cases and ten remaining-Feruchemy cases. Source-aware typed access also exercises an ordinary recipient tapping a separate unkeyed weight mind and immediately losing access when the unsealed source stops.
- **Dedicated restart seed and verify passed in two separate JVMs**, fresh run directory build/run/restartSmoke-phase7. Exact body escrow/cooldown, region familiarity, offloaded unique Copper memory, typed unsealed Gold ability and charge survive alongside canonical mind/contact data, spike provenance/source tombstones, brewing and processing jobs. Stored memory does not silently reappear in the private journal.
- Earlier two-normal-client foundation run **passed35 checks**, including private Identity/reserves/contact, actual drop/pickup transfers, copied lease refusal, foreign-key refusal, owner tapping after return, simultaneous force/recoil, active disconnect/reconnect, consensual player extraction and donor respawn/source removal. This is loopback/offline, not authenticated public multiplayer.

- **All six native client suites passed** in the final sequential run, including existing economy/physical/Hemalurgy lifecycle regressions and the advanced UI/network suite. The native furnace inside a base-strength fast bubble advanced within the expected threefold cooking interval. New memory/type controls were exercised through actual client intent payloads.
- **Expanded two-normal-client run passed all 48 checks.** Includes all foundation regressions plus actual Bronze signatures, equal-strength Copper concealment, actual player fear intensity, Chromium contact depletion, forced Nicrosil exhaustion and owner-only advanced journals. ClientA observed one own Identity over two connections; ClientB one own Identity over one connection. [Machine-readable checks](validation/multiplayer-phase7.json), [clientA](validation/multiplayer-client-A-phase7.json), [clientB](validation/multiplayer-client-B-phase7.json).

Final successful logs: phase67-final-native2.log (build + all 6 client suites), phase67-final-multiplayer.log (48 checks), phase7-restart-seed.log and phase7-restart-verify.log. Validation ran locally on 2026-10-31. **Final runDatagen + build passed** (phase67-package.log). Checksums are in [phase7-sha256.txt](validation/phase7-sha256.txt). The distributable contains production code/resources and excludes test probe classes.

## Reproduce native checks

Set JAVA_HOME to JDK25. Use the checked-in Gradle wrapper. Do not rebuild classes/resources concurrently with a running client test: resource replacement during world/dimension loading invalidates the test environment.

```powershell
.\gradlew.bat clean build -x runGameTest
.\gradlew.bat build runClientGameTest -PacceptMinecraftEula=true
.\gradlew.bat runDatagen
```

Use the EULA property only if you agree to https://aka.ms/MinecraftEULA. Six client entrypoints are registered: foundation, economy, physical Allomancy, physical Feruchemy, Hemalurgy and advanced Arts. Screenshots are under build/run/clientGameTest/screenshots. The advanced suite uses actual menu controls/network transactions for emotional-page activation, Copper memory storage/restoration and Nicrosil type selection, plus a native furnace inside a fast bubble.

For two independent normal clients on Windows:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/verify-multiplayer.ps1 -AcceptMinecraftEula
```

This compiles fixtures, creates a fresh timestamped directory, binds the dedicated server to127.0.0.1:25570 with offline test identities, launches two clients and records JSON/logs. It refuses an occupied port and cleans up only helper processes it started. No production mod exposes the fixture probe payloads. Requires a graphical desktop and enough memory for three game JVMs. The test is not authenticated-account or remote-server acceptance.

For separate-process restart, use a fresh directory and create its eula.txt/server.properties only after accepting the EULA, then:

```powershell
.\gradlew.bat runRestartSmoke -PsmokeRunDir=build/run/restartSmoke-NEW -PsmokeMode=seed
.\gradlew.bat runRestartSmoke -PsmokeRunDir=build/run/restartSmoke-NEW -PsmokeMode=verify
```

Check both restart-*.result files for PASS. Do not seed an already-used fixture world.

## Limits and diagnostic history

- Native mocks always report Creative in26.1. New Survival resource tests therefore use a normal ServerPlayer and complete its load handshake before damage. Villager offers are modified on the actual server list; overrideOffers on the server is a no-op.
- An initial client test still expected four grants after its setup loop expanded to sixteen. That assertion was corrected; physical control behavior is unchanged.
- A concurrent restart compilation replaced processResources output during client world loading and caused a registry resource failure. That run was stopped; subsequent native runs are sequential.
- The new memory test initially clicked immediately after receiving JSON, before the next UI tick rebuilt buttons. It now waits for rendering before clicking. Transactions remain server-authoritative.
- Fabric’s pinned Mixin subsystem warns about JAVA25 compatibility level support. The actual compile/game checks determine observed functionality; this warning is not silently treated as broad future compatibility.
- Offline test-profile/Realms HTTP authorization warnings are expected for test identities, not proof of real account validation.
- GitHub CI covers compilation/unit/datagen only; local server/client/multiplayer/restart evidence is separate. No protection-mod, Linux/macOS client, long-session performance, arbitrary temporal machine, arbitrary gas/knowledge/faction/language or full construct acceptance is claimed.

Read ADVANCED_ARTS.md for exact gameplay approximations and REMAINING_WORK.md for the ordered backlog.
