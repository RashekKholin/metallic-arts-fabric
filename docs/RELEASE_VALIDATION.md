# 1.0.0-rc.3 verification

Pinned stack: Minecraft 26.1, Java 25.0.5, Fabric Loader 0.18.4, Fabric API 0.145.1+26.1, Loom 1.15.5, Gradle 9.4.0. Observed locally on Windows on 2026-11-01. RC2 results are archived in RC2_VALIDATION.md and are not substituted for changed RC3 behavior.

| Check | Result |
|---|---|
| Unit suite | PASS; 90 tests, zero failures/errors |
| Required native server suite | PASS; all 116 required tests, including real registered passive-mob AI and the NPC/late-entry burst cases |
| Native graphical suites | PASS; ten suites, including final brighter particle shells and instruction text |
| Full build and datagen | PASS; full build and datagen; final release build is gated by CI |
| Ordinary two-client loopback | PASS; 50 checks, 1,200 measured ticks (one minute), mean 3.4378 ms / maximum 20.4458 ms; two ordinary clients, 40 mobs, four furnaces and both fields/sight. RC2's separate 15-minute result remains historical |
| Cross-platform and compatibility CI | Publication requires all Windows/Linux/macOS server, Linux graphical, build/datagen, actual pinned OpenPAC and save/crash/migration jobs to pass; see [the exact release workflow](https://github.com/RashekKholin/metallic-arts-fabric/actions) |

## Changed-behavior evidence

Three new pure camera-projection tests cover perspective/depth, rear compass signs, finite horizon transitions and off-screen limits. Native cases cover idempotent all-power grants and all-metal fuel, six-face grouping excluding diagonal edge/corner contacts, automatic selection beyond the manual eight-source limit, actual Zombie target/attack suppression and provocation, a Cow's real temporary melee response, simultaneous independent Zinc/Brass traits, full spherical particle geometry and default four/eight-block radii. Amplification regressions cover 100 paid ticks, normal Duralumin debits, automatic off, late entry without deadline extension and the same finite NPC policy.

The tenth graphical suite opens all sixteen controls on one page, captures wide wheel/grid and minimum 320-by-240 GUI layouts, sends distinct Zinc/Brass choices through the network, enables automatic selection, checks the actual camera quaternion/forward projection at four yaw angles and records screenshots. Other nine suites retain their existing controls, economy, equipment, source/Identity, dimensions/reload and sensory behavior coverage.

Manual screenshot inspection found the first outside shell too faint, so particle size/color and real spherical silhouette particles were improved before final validation. There are no drawn HUD shell lines. The first unit attempt also exposed an overly strict signed-zero assertion (0.0 versus -0.0); its numeric tolerance was corrected. A new rule ID initially contained a digit prohibited by the provenance schema, causing datagen to fail; it was corrected and datagen rerun successfully. These failed/intermediate attempts are not counted as final passes.

## Reproduce and interpret

Use README build/test commands and MANUAL_ACCEPTANCE.md, together with the current controls and limits in RC3_REFINEMENTS.md. Run local fixtures sequentially: the graphical fixture's run-directory deletion invalidates other concurrent local Gradle fixtures. Do not rebuild resources/classes beneath running Minecraft test processes. Test logs/screenshots and the loopback result are preserved outside disposable build directories in rc3-evidence.

The automated multiplayer fixture uses two ordinary offline-mode clients confined to loopback, not real licensed accounts. Server-tick timings are not client FPS, measured WAN latency, full memory/bandwidth percentiles or hours-long capacity certification. The actual pinned OpenPAC/version pair in CI does not certify every claim mod or custom mob brain. Previously verified old-save/crash recovery is rerun in CI rather than assumed from a local gameplay pass.

Remote authenticated accounts, hours-long survival/balance/load, real hardware/screen-reader acceptance, complete Spanish translation, third-party fan-use/branding rights and platform eligibility remain open. See REMAINING_WORK.md and DISTRIBUTION.md. A passing RC is not a complete-canon or stable-1.0 declaration.
