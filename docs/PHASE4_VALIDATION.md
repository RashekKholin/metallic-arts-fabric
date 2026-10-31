# Phase 4 validation — 0.4.0-alpha.1

Local environment: Windows, JDK 25.0.5, Gradle wrapper 9.4.0, Minecraft 26.1, Fabric Loader 0.18.4 and Fabric API 0.145.1+26.1. Tests run with the user's explicit local Minecraft EULA acceptance.

## Recorded results

- Final `build runClientGameTest -PacceptMinecraftEula=true`: **passed**, 47 unit tests, all 35 required server game tests and all four client test suites. Includes actual client-requested death/respawn, active Feruchemy dimension shutdown and exact save reload. Final local log: phase4-respawn-tests.log (2026-10-31).
- `runDatagen`: **passed** in the earlier combined run. The later changes corrected lifecycle test code, documentation and startup logging, without changing generated data.
- Two earlier extended lifecycle runs failed at the final menu reopen. The fixture had called PlayerList.respawn directly, bypassing the connection's replacement-player assignment. Sending Minecraft's normal PERFORM_RESPAWN client request fixed the fixture; the final run above passed. These failed runs are not counted as passes.
- Separate dedicated-process restart fixture: **passed**. Seed and verify launched distinct server JVMs against a fresh test world. The loaded player retained the contact item and exact canonical charge (123,456), capacity (9,000,000), Identity and lease, alongside the existing spiritweb/workstation/brewing checks. Both result files report PASS.

## Meaningful coverage

Domain tests cover canonical leases, copied/stale references, Identity, unkeyed access, capacity, codec persistence, exact storage, high-rate costs across multiple minds, contrary modes, weak/removed access and depletion. Existing reserve/source/economy tests remain regressions.

Server game tests cover contact inventory transactions (including a full inventory), separate Tin attributes and paid cues, Allomancy/Feruchemy modifier independence, weight/reciprocal forces, strength-dependent lifting, block protection, exactly one returned carried block, and component-preserving one-coin pouch withdrawal. There are 485 metallurgy recipes under conservation validation.

Real-client checks exercise M → F, multiple equipped assets, storage/tapping, synchronized mining speed, sight rendering, muted/enhanced food taste, grid/wheel controls, B carrying/drop, visible Pewter bulk, individual charge and exact world-save reload. Separate Allomancy client checks exercise selected targets, force controls and active death/dimension shutdown; economy and initial Spiritweb suites also run.

Screenshots are written to `build/run/clientGameTest/screenshots/`. They are review evidence rather than tests of every possible display size. The wheel, contacted-mind menu and third-person carrying view have been visually inspected.

## Reproduce

Use a JDK 25 and the checked-in wrapper:

```powershell
.\gradlew.bat clean build -x runGameTest
.\gradlew.bat runDatagen
.\gradlew.bat build runClientGameTest -PacceptMinecraftEula=true
```

Only supply the last property if you accept https://aka.ms/MinecraftEULA. For interactive use, run `runClient`/`runServer` and follow FERUCHEMY.md. Automated fixtures live only in the gametest source set, not the installed mod jar.

## Limits

No claim of two simultaneous live clients, authenticated multiplayer, broad third-party protection/mod compatibility, long-session profiling or every resolution/accessibility combination. Current server fixtures are not substitutes for a live two-client test. Future unsupported Feruchemical attributes, Hemalurgy, unsealed equipment and Compounding are not completed by these tests.
