# 0.8.0-alpha.2 validation

Observed on Windows, this development laptop, Java 25.0.5, Minecraft 26.1, Loader 0.18.4, Fabric API 0.145.1+26.1, Loom 1.15.5 and Gradle 9.4.0, 2026-10-31 local time. Historical 0.7/0.5 reports are separate evidence.

| Check | Actual result |
|---|---|
| Build and unit tests | PASS, 83 tests, zero failures/errors |
| Native required server tests | PASS, 80 required tests |
| Native graphical client tests | PASS, all seven registered suites |
| Datagen | PASS |
| Two ordinary clients + dedicated server | PASS, 50 checks; offline loopback, not authenticated accounts |
| Temporal load baseline | PASS, 1,200 ticks, 40 mobs, four furnaces; mean server processing 3.4064 ms/tick, max 9.471 ms |
| Dedicated save + separate-process reload | PASS for both seed and verify |

The final server regression adds an invested-input rejection/typed Lerasium alloy test and verifies two medallion powers coexist while independently spending charge. The seven graphical suites and two-client/restart runs preceded that small alloy-input hardening; the changed transaction itself was exercised by the final server suite. No change to those client, temporal or persistence paths followed their passing runs. Final metadata/source packaging is checked separately.

## What the checks exercise

- Existing economy, all standard-metal bounded handlers, five Tin senses, ownership, ordinary/invested fuel separation, physical force order, canonical contact, copied leases, keyed transfers, implants, source-scoped removal, consent/PvP, body/memory/typed Investiture and documented UUID policy remain regressions.
- New pure tests check compounding transaction preflight, exact type/Identity, capacity rollback, spent-source codec, nonduplicating memories, fractional clocks, construct preparations and strict policy types/probabilities.
- New native server tests exercise actual deliberate compounding and refused Intent; protected emotion/compounding; Koloss anatomy; linked Blessing/material/shape/loss; Inquisitor eye/Gold/linchpin consequences; default-off/lower-retention exceptional extraction and cancelled-damage rollback; Gold trauma curing; owned-reference repair; NPC paid attributes and leech; short-pulse retention; inaccessible memory annotation refusal.
- Survival/equipment tests exercise deterministic allocation and independent once-only markers, stolen-source non-restoration, atomic mixed-vial failure/success and invested-preview refusal, medallion leases/copies, independently paid simultaneous powers, explicit Lerasium use, ordinary alloy transaction and separation of public survival commands from operator grant permissions.
- The new graphical suite uses actual P/H controls to compound, prepare Blessings, implant, transform and learn a wolf body, then checks an actual vanilla Wolf render state and collapse presentation. The existing foundation suite opens the new Guide, changes pages and closes it. The Guide screenshot was visually inspected for readable wrapping and visible buttons at 854×480; this is not a full GUI-scale/accessibility matrix.
- Two-client fixture retains private snapshots, duplicate contention, real physical transfers, disconnect/rejoin, death/respawn, competing forces, consent/extraction/implantation, pulse concealment, emotions and mutual contact resource tests. Its new temporal run checks input/output conservation and actual furnace progress with overlapping fields and forty loaded mobs. Timing measures START→END server processing, excluding normal idle time, on one machine; it is not a public-server throughput guarantee.
- Dedicated restart saves real player/world storage, then a new JVM reloads compounded destination charge, spent-source refusal, linked Blessing preparation, construct shape, trauma, merchant relationships, progression/one-time Lerasium markers, original canonical equipment, unique stored memory, typed unsealed ability and partially completed processing/brewing jobs. No fixture reseeds an old user world.

Machine-readable [two-client result](validation/0.8/multiplayer-result.json), [seed result](validation/0.8/restart-seed.result) and [reload result](validation/0.8/restart-verify.result) are retained. Full local reports remain under `build/reports/tests` and client screenshots under `build/run/clientGameTest/screenshots`.

## Practical limits

Not observed: authenticated real-account perk login, a remote public server, packet loss/latency injection, an installed third-party protection/claim mod, Linux/macOS native clients, archived production-world migrations, crash fault injection or hours-long capacity tests. Linux GitHub CI validates build/unit/datagen and publishes the matching alpha assets; it does not run the local native suites. Loader/Mixin emits its existing Java compatibility/remap warnings in the development harness; they did not fail these tests. Development client profile/Realms authentication warnings are not authenticated-account evidence.

Unknown god metals, full Excisor manufacture, arbitrary local time, every NPC metal/construct AI, universal social/language systems and complete translations are not claimed complete. [Ordered remaining work](REMAINING_WORK.md) states what remains. Attribution inventory does not resolve owner licensing/third-party rights.


Alpha.2 retains the tested alpha.1 mechanics and test classes, with corrected bundled provenance, historical guide status and release metadata. No mechanics changed in this documentation follow-up.
