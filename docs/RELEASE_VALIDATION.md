# 1.0.0-rc.1 validation

Observed locally on Windows, Java 25.0.5, Minecraft 26.1, Fabric Loader 0.18.4, Fabric API 0.145.1+26.1, Loom 1.15.5 and Gradle 9.4.0 on 2026-11-01. Historical alpha reports remain separate.

| Check | Observed result |
|---|---|
| Complete build / unit tests | PASS; 87 tests, zero failures/errors |
| Native required server suite | PASS; 98 required tests |
| Real claim-mod integration | PASS with OpenPAC 0.33.0 + Forge Config API Port 26.1.3; claim owner/stranger block, temporal and loose-cargo checks |
| Native graphical clients | PASS; eight registered suites, minimum scaled menus and real construct renderer checks |
| Datagen | PASS |
| Separate-process orderly save/reload | PASS in isolated fresh world |
| Intentional partial player-save crash / canonical lease recovery | PASS in a separate isolated world; expected exit 71 and fault marker required |
| Actual old-version save upgrades | PASS; committed 0.5 and 0.7 sources generated worlds; current version upgraded/reloaded each twice |
| Two-client extended load | Pending current-candidate run; prior alpha's 50-check/1,200-tick baseline is not substituted |
| Cross-platform native CI | Pending workflow execution; do not infer native platform success from a local Windows build |

## Exercised behavior

The server suite covers canonical mass/charge, source-specific power loss, consent/protection, contact/Identity, spent-copy refusal, compounding and medallions, finite NPC storage/tapping, typed Copper/Nicrosil behavior, NPC construct anatomy/death, permanent-despawn cleanup, faction injury/trade, finite god-metal windows/alloy preflight, camp placement/containers, memory movement and ownerless water-reaction safety. The real optional claim fixture installs its exact third-party jars; the normal suite skips that one test when the optional mod is absent.

Graphical suites use actual native Minecraft clients and integrated/dedicated connections: open menus, consume server-authorized actions, powers/equipment, dimension/death/reconnect, real disk reopen and body rendering. The eighth suite additionally checks controls inside the 320×240 scaled viewport, guide narration, lessons/settings and distinct NPC renderer state. Screenshots are captured by Minecraft, not generated promotional art.

The historical migration fixtures generate old saves using archived committed sources (`79639da` for 0.5 and `993d9f8` for 0.7), then load/resave/reload current code. They verify old spiritweb/fuel, canonical mind charge/spike potency and appropriate new-field defaults. They are not arbitrary production-world certification. The crash fixture tests a deliberately missing player contact reference while the world lease remains; recovery cannot repair arbitrary lost/corrupt world ledgers or every multi-file crash boundary.

## Reproduction and evidence

Use the README commands. Run fixtures sequentially: Fabric's test-directory deletion task invalidates a concurrent graphical run, even when starting a different server fixture. One earlier client run was invalidated by that test orchestration mistake and was not counted as passing; the isolated rerun passed. An asynchronous reaction test's original 20-tick deadline matched its scheduled 20-tick assertion and was corrected to a 100-tick deadline before the final successful build.

Local retained fixtures: `build/run/recovery-20261101-001228` and `build/legacy-20261101-002529`. Final local logs/screenshots are preserved outside disposable build directories in the task's `rc-evidence` output. CI uploads separate unit/server/client/recovery evidence and gates immutable prerelease publication on all required jobs.

## What remains unverified

Authenticated remote licensed accounts, a real latency/loss deployment, hours-long capacity/bandwidth/save-growth certification, an ordinary full survival balance playthrough, complete Spanish translation, real screen-reader acceptance and native macOS graphical drivers remain open. A pinned OpenPAC result does not certify other claim mods or future versions. Working code and the selected source license do not establish third-party fan-use/branding clearance or Modrinth eligibility. See REMAINING_WORK.md and DISTRIBUTION.md.
