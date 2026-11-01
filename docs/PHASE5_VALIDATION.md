**Later acceptance update:** the foundation two-client checks and expanded advanced interactions now pass in the 0.7 run; see [PHASE67_VALIDATION.md](PHASE67_VALIDATION.md). The results below remain the original0.5 record.

# Phase 5 validation — 0.5.0-alpha.1

Environment remains Minecraft 26.1 / Fabric Loader 0.18.4 / Fabric API 0.145.1+26.1 / JDK 25.0.5 / Gradle 9.4.0 on Windows. Local Minecraft EULA acceptance was explicitly authorized by the user.

## Recorded results

- Initial Hemalurgy build: passed with 54 unit tests and 41 required server tests.
- Extended build/client rerun: passed with 55 unit tests, all 41 required server tests and all five real-client test suites. The first extended client attempt timed out during blood collection because its fixture changed the server hotbar selection without synchronizing the client's selection. The corrected fixture sends the standard carried-item selection packet and respects spawn damage protection.
- Final full client run: **passed**, 55 unit tests, all 42 then-registered required server tests and all five client suites, including critical removal/normal respawn and the grant-capacity guard. Datagen also passed in the preceding full validation run.
- Final packaging build: **passed**, 55 unit tests and all **44 required server tests**, adding explicit innate-strength and destructive Aluminum/spent-record coverage. No production gameplay code changed between the final client run and this packaging run. All results were observed locally on 2026-10-31.
- Separate-process spike-ledger restart check: **passed**. Seed and verify launched distinct dedicated-server JVMs against a fresh world. Both result files report PASS; canonical spike potency, donor Identity, anatomical lease and source tombstone survived alongside exact metalmind charge/contact gear and the earlier player/economy records.

Local logs: phase5-capacity-final.log, phase5-package-tests.log, phase5-restart-seed.log and phase5-restart-verify.log. GitHub checks independently cover the workflow's build/unit/datagen scope, not the locally run Minecraft clients.

## Coverage

Domain tests verify the sixteen-metal type matrix, single anatomical ownership, stale/copy invalidation, monotone decay, blood/implant preservation, persisted donor-source tombstones, drained assets, private-preview exclusion, per-tick donor-key authorization and deduplicated additive Allomantic spike sources.

Server fixtures exercise lethal extraction from an actual powered NPC, power removal before donor death, one physically charged output, implantation/source-specific removal, independent power preservation, copied implants, wrong materials, missing donor access, attack protection, invulnerable-donor rollback, default player restrictions, malformed intents, full-inventory removal rejection, charged-earring repurposing rejection and drained-source recalculation. Final tests also cover incompatible anatomy, explicit critical insertion, saturated external grant capacity, real innate-strength modifiers/carrying, and Aluminum producing a non-implantable spent record after destroying donor grants.

The fifth real-client suite uses the actual M → H menu and payloads to extract a powered NPC's Steel Allomancy, implant it, burn real fuel, remove it and observe access shutdown. It collects blood with actual damage, spends a blood vial, extracts another NPC's Feruchemy, and taps that donor's keyed metalmind. Save/reload checks preserve the implant ID, sourced access, exact metalmind charge and blood preservation. The final extension also exercises dangerous critical removal and normal client-requested respawn, retaining unrelated implants/charge without a repeated death loop.

The existing four client suites and physical/economy/domain tests remain regressions. Source fixtures and setup commands are development tools, not proof of natural survival progression.

## Reproduce

```powershell
.\gradlew.bat clean build -x runGameTest
.\gradlew.bat runDatagen
.\gradlew.bat build runClientGameTest -PacceptMinecraftEula=true
```

The last property requires accepting https://aka.ms/MinecraftEULA. For manual testing use HEMALURGY.md; for older physical/economy evidence see PHASE4_VALIDATION.md and PHASE2_VALIDATION.md. Screenshots are written under `build/run/clientGameTest/screenshots/`.

For the separate-process fixture, use a **fresh directory for each seed/verify pair**. This is a development fixture, not a production server setup. Only write the EULA file if you accept the linked EULA:

```powershell
New-Item -ItemType Directory -Force build/run/restart-check | Out-Null
'eula=true' | Set-Content build/run/restart-check/eula.txt
"server-ip=127.0.0.1`nserver-port=25569`nlevel-type=minecraft:flat`nview-distance=2`nsimulation-distance=2" | Set-Content build/run/restart-check/server.properties
.\gradlew.bat runRestartSmoke -PsmokeMode=seed -PsmokeRunDir=build/run/restart-check
.\gradlew.bat runRestartSmoke -PsmokeMode=verify -PsmokeRunDir=build/run/restart-check
Get-Content build/run/restart-check/restart-seed.result
Get-Content build/run/restart-check/restart-verify.result
```

Each command launches and stops its own server JVM. Both result files must say PASS. Do not seed the same fixture world twice; choose another directory for a fresh run. Back up ordinary play worlds separately.

## Limits

Two simultaneous authenticated live clients, successful consensual player-to-player extraction, contested multiplayer assets, arbitrary third-party death/protection callbacks, long-session profiling and broad mod compatibility remain unverified. Mock server actors and a single real client do not establish those results.

Cognitive/Spiritual types without handlers, the twelve later Allomantic/Feruchemical effects, full Inquisitor/koloss/kandra templates, exceptional nonlethal extraction, Compounding and unsealed equipment are not completed by these tests. Current anatomy, compatibility details, losses, stacking and vulnerability behavior remain documented interpretations/policies.
