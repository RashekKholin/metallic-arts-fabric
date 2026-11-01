# 1.0.0-rc.2 validation

Observed locally on Windows, Java 25.0.5, Minecraft 26.1, Fabric Loader 0.18.4, Fabric API 0.145.1+26.1, Loom 1.15.5 and Gradle 9.4.0 on 2026-11-01. Historical alpha reports remain separate.

| Check | Observed result |
|---|---|
| Complete build / unit tests | PASS; 87 tests, zero failures/errors |
| Native required server suite | PASS; 107 required tests |
| Real claim-mod integration | PASS with OpenPAC 0.33.0 + Forge Config API Port 26.1.3; claim owner/stranger block, temporal and loose-cargo checks |
| Native graphical clients | PASS; nine registered suites, minimum scaled menus, actual rebound controls, all-direction sight and full-sphere rendering |
| Datagen | PASS |
| Separate-process orderly save/reload | PASS in isolated fresh world |
| Intentional partial player-save crash / canonical lease recovery | PASS in a separate isolated world; expected exit 71 and fault marker required |
| Actual old-version save upgrades | PASS; committed 0.5 and 0.7 sources generated worlds; current version upgraded/reloaded each twice |
| Two-client extended load | PASS; 50 checks, 18,000 measured ticks (15 minutes), mean 1.1718 ms / maximum 56.3722 ms; 40 mobs, four furnaces, 32 metal sources and both clients' Steel/Iron sight |
| Cross-platform native CI | PASS on rc.2 commit `a637f66`: [run 41301257587](https://github.com/RashekKholin/metallic-arts-fabric/actions/runs/41301257587); Windows/Linux/macOS native servers, Linux graphical clients, full build, actual OpenPAC, crash recovery and historical upgrades |

## Exercised behavior

The server suite covers canonical mass/charge, source-specific power loss, consent/protection, contact/Identity, spent-copy refusal, compounding and medallions, finite NPC storage/tapping, typed Copper/Nicrosil behavior, NPC construct anatomy/death, permanent-despawn cleanup, faction injury/trade, finite god-metal windows/alloy preflight, camp placement/containers, memory movement and ownerless water-reaction safety. The real optional claim fixture installs its exact third-party jars; the normal suite skips that one test when the optional mod is absent.

Graphical suites use actual native Minecraft clients and integrated/dedicated connections: open menus, consume server-authorized actions, powers/equipment, dimension/death/reconnect, real disk reopen and body rendering. The eighth suite additionally checks controls inside the 320×240 scaled viewport, guide narration, lessons/settings and distinct NPC renderer state. The ninth suite checks live nearby-metal payloads, coin auto-lock, the inventory help tab, a rebound Push key, synchronized player time rates and a complete NPC sphere viewed from outside. Screenshots are captured by Minecraft, not generated promotional art.

The historical migration fixtures generate old saves using archived committed sources (`79639da` for 0.5 and `993d9f8` for 0.7), then load/resave/reload current code. They verify old spiritweb/fuel, canonical mind charge/spike potency and appropriate new-field defaults. They are not arbitrary production-world certification. The crash fixture tests a deliberately missing player contact reference while the world lease remains; recovery cannot repair arbitrary lost/corrupt world ledgers or every multi-file crash boundary.

## Reproduction and evidence

Use the README commands. Run fixtures sequentially: Fabric's test-directory deletion task invalidates a concurrent graphical run, even when starting a different server fixture. One earlier client run was invalidated by that test orchestration mistake and was not counted as passing; the isolated rerun passed. An asynchronous reaction test's original 20-tick deadline matched its scheduled 20-tick assertion and was corrected to a 100-tick deadline before the final successful build.

Final rc.2 loopback fixture: `build/run/multiplayer-20261101-041702` (two ordinary offline clients, not authenticated accounts). Local retained fixtures: `build/run/recovery-20261101-001228` and `build/legacy-20261101-002529`. Final local logs/screenshots are preserved outside disposable build directories in the task's `rc-evidence` output. CI uploads separate unit/server/client/recovery evidence and gates immutable prerelease publication on all required jobs.

## Requested gameplay regression scope

Nine additional native cases verify distance falloff with reciprocal recoil; explicit dual-force cancellation; all standard material immunity; connected/behind-source discovery; auto-selected coin locks and actual impact entity replacement; a full 200-tick unavailable-lock interval without remote force; 64-stack output/legacy quantity conservation; exact visible hunger repayment; vertical caster/visitor sphere boundaries; and the configured player item-use clock inside versus outside. Earlier assertions for 99+1 portions, disconnected iron-floor cells and distance-independent recoil were updated to the requested behavior rather than preserved as obsolete expectations.

The earlier 15-minute rc.1 load run reached 18,000 measured ticks (mean 2.8351 ms, max 52.7347 ms) but FAILED its final active-field assertion; it is not counted as a passing soak. The revised fixture uses a deterministic flat world, stationary invulnerable casters, early active-field diagnostics, both clients' metal sight, 32 separate metal sources, 40 mobs and four furnaces. The final rc.2 run passed all 50 checks and 18,000 measured ticks, including continuously active fields and final furnace conservation. These measured server-tick durations are not client frame times, network latency or hours-long capacity certification.

An initial rc.2 run was stopped before completion to add worn-body detection and close client menus during the visual workload. A later fixture failed before the soak when a moving dropped asset escaped its pickup staging bounds. The fixture now parks the same real client-dropped item on its test platform, preserving its canonical token and normal pickup delay; it does not mint a replacement. Neither incomplete/failed run is counted as passing.

## What remains unverified

Authenticated remote licensed accounts, a real latency/loss deployment, hours-long capacity/bandwidth/save-growth certification, an ordinary full survival balance playthrough, complete Spanish translation, real screen-reader acceptance and native macOS graphical drivers remain open. A pinned OpenPAC result does not certify other claim mods or future versions. Working code and the selected source license do not establish third-party fan-use/branding clearance or Modrinth eligibility. See REMAINING_WORK.md and DISTRIBUTION.md.
