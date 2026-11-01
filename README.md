# The Metallic Arts

Minecraft Java **26.1** · Fabric · Java **25** · mod ID `metallicarts` · **0.8.0-alpha.1**

Playable fan-mod alpha: all sixteen standard Allomantic metals and sixteen Feruchemical metals (five separate Tin senses), metal economy, source-tracked Hemalurgy, canonical equipment, compounding and prepared player constructs. This update also adds survival discovery, powered hostile populations, mixed vials, multi-core medallions and a researched Lerasium subset. These are bounded Minecraft interpretations, not every canonical interaction.

**Current validation:** 83 unit tests, 80 required native server tests, all seven native client suites, and datagen/build passed. Dedicated restart and two-client results are recorded separately in [current validation](docs/RELEASE_VALIDATION.md); historical reports are not substituted for current evidence.

Read [advanced systems and exact limits](docs/ADVANCED_SYSTEMS.md), [host configuration/recovery](docs/OPERATIONS.md), [advanced per-metal controls](docs/ADVANCED_ARTS.md), [economy](docs/ECONOMY.md), [physical Allomancy](docs/ALLOMANCY.md), [Feruchemy](docs/FERUCHEMY.md) and [Hemalurgy](docs/HEMALURGY.md).

## Download and launch

1. Open [versioned GitHub Releases](https://github.com/RashekKholin/metallic-arts-fabric/releases). Choose **v0.8.0-alpha.1** and download **metallic-arts-fabric-0.8.0-alpha.1.jar**. The `-sources.jar` is source code, not the playable mod. Releases are published after successful main-branch CI; if that release is not present, use a successful [build artifact](https://github.com/RashekKholin/metallic-arts-fabric/actions/workflows/build.yml) or build below.
2. Create a Minecraft **26.1** instance using **Java 25**, **Fabric Loader 0.18.4+**, and **Fabric API 0.145.1+26.1**. Download dependencies from their official projects; this jar does not bundle them.
3. Put the ordinary mod jar and matching Fabric API jar in that instance's `mods` folder. Install the same mod version on the dedicated server and every client. Keep only one Metallic Arts jar.
4. Launch the Fabric profile and create/join a world. Press **M**, then **Guide**. The mod's keys can be rebound in vanilla Controls.
5. Upgrading: stop Minecraft/server, back up the entire world and configs, remove the old mod jar, then test the new build on a copy. Do not downgrade the world in place. Canonical charge belongs to world data; copying charged item previews between worlds does not transfer charge.

GitHub Releases include the installable jar, sources and SHA-256 checksums. CI artifacts require sign-in and can expire. A successful CI run proves build/unit/datagen only; native local validation has its own report. The release automation retains existing versions rather than overwriting them.

## Start playing in survival

Craft a **Metallurgy Table** using three iron ingots, three cobblestone and three planks in horizontal rows. Fuel it with furnace fuel. Refine raw ores, alloy exact compositions, then use **Shape** to make nuggets, portions, flakes and equipment. One nugget makes 100 portions; one portion makes one flake. Use recipe arrows/ghost ingredient requirements and **Start/Stop**; **Batch** repeats while resources/space permit.

Brew flakes with **water bottles and blaze powder in a normal Brewing Stand**. One flake fills one vial. Drinking provides fuel; it does not grant standard-metal powers. Ore appears in newly generated Overworld chunks. Sneak-right-click the table empty-handed to choose one of four metallurgist specialties; unemployed adult villagers can claim it.

Each character receives a once-only latent allocation: default independent 50% chances of one Allomantic and one Feruchemical metal. Survive an unblocked hit of at least six damage with six health or less to awaken latent Allomancy. At XP level five, use `/metallicarts-survival discover` to reveal Feruchemy. Some characters have neither. Death, rejoining and extraction cannot reroll or regrant powers. These probabilities/thresholds are host-configurable gameplay rules.

A default 2% powered hostile population carries real owned abilities and finite fuel. Its active AI supports Iron/Steel/Pewter/Tin/Copper; some also carry extractable Feruchemy. It is not full sixteen-metal NPC combat AI. Other players' real unsealed cores and Hemalurgy provide alternate access; merchants do not create free infinite Investiture.

## Controls and new workflows

| Key/action | Use |
|---|---|
| **M** | Ownership, reserves, Allomancy burn/flare; Guide button |
| **F** | Equip/contact, store/tap, rate, Tin sense and typed Nicrosil; W switches grid/wheel |
| **H** | Source-aware Hemalurgy, implantation/removal, explicit donor consent |
| **K** | Recorded history; selected coppermind memory search/annotation |
| **P** | Compounding, prepared spikes, constructs, body forms, finite compulsion, exceptional extraction |
| **C / B / J** | Toss coins / supported cargo / paid Electrum prediction; see detailed guides |
| **Esc** | Close |

- **Compounding:** own both Arts, hold a charged free mind, wear a compatible destination with capacity, select its slot in P and enable Intent. The whole source burns once; the destination receives the configured amplified attribute. Copies stay spent after restart. Copper restores actual records once, not duplicate facts.
- **Constructs:** prepare real charged spikes in P. Koloss needs four prepared Iron strength staples; Inquisitor needs researched eye/linchpin/Gold dependencies; Kandra needs mistwraith lineage and a genuine paired innate Blessing. Dependency loss has consequences. Wolf/slime Kandra forms have material costs and real collision/rendering changes. Exact anatomy and the optional mistwraith ritual are documented adaptations.
- **Mixed fuel:** hold two filled ordinary vials in opposite hands; `/metallicarts-survival mix` combines them (up to sixteen batches), returning one empty bottle. All reserve checks finish before drinking consumes anything. Invested previews are refused.
- **Medallions:** `/metallicarts-survival bind` joins two free charged unsealed typed Nicrosil cores held in opposite hands; add a third similarly. `/metallicarts-survival medallion 0` equips them in adjacent empty contact slots, starting with UI slot one. Tap each core to borrow its specific ability. Independent canonical charge/leases remain; copies cannot double-equip. This is a carrier adaptation, not a verified Excisor process.
- **Lerasium:** crouch-use a pure bead for standard Allomancy or an alloy bead for one metal. It does not grant Feruchemy. `/metallicarts-survival alloy` consumes a pure bead plus an ordinary standard-metal ingot offhand. The optional boss/material acquisition is disabled by default. Other god metals remain unimplemented rather than guessed.
- **Recovery:** craft bandages to heal bounded trauma while still, or pay Gold charge. Operators can audit/refresh matching owned references without inventing charge. See [operations](docs/OPERATIONS.md).

## Documented account access

**RashekKholin and The17thShard have baseline access to all standard Allomancy/Feruchemy**, matched by verified account UUID. Normal fuel, contact, charge, Identity and suppression rules apply. Hosts can set `enableAccountPowers` to false in `metallicarts-server.json` and restart. Earned grants remain intact. This public perk grants no operator rights, uses no username fallback and has no hidden dependency/tamper penalty. See [exact UUID policy](docs/ACCOUNT_ACCESS.md).

## Configuration and safety

Host files: `metallicarts-server.json`, `metallicarts-allomancy.json`, `metallicarts-advanced.json` and `metallicarts-progression.json`. Restart after edits. Advanced policy exposes detection/emotions/time/body/cure/burst/social/compounding/construct limits. Numeric strings, fractional integer costs and invalid ranges fail visibly. Player extraction and exceptional nonlethal extraction are separately default-off; player consent, PvP and protection checks still apply. No actual third-party protection mod is certified merely by the presence of adapter hooks.

Back up player data and world ledgers together. `/metallicarts assets PLAYER audit|repairrefs` can refresh only an existing matching seal/owner/slot; it cannot restore charge from a preview. Detailed configuration, adapters and rollback steps are in [OPERATIONS.md](docs/OPERATIONS.md).

## Build and test

Use JDK **25**. The wrapper pins Gradle **9.4.0**, Loom **1.15.5**, Minecraft **26.1**, Loader **0.18.4** and API **0.145.1+26.1**.

```powershell
# Build + unit tests, without accepting Minecraft's EULA
.\gradlew.bat clean build -x runGameTest --console=plain
# Data generation
.\gradlew.bat runDatagen
# Native server and seven graphical client suites; only if you accept the EULA
.\gradlew.bat build runClientGameTest -PacceptMinecraftEula=true --console=plain
# Two ordinary loopback clients + dedicated server (port 25570 must be free)
.\scripts\verify-multiplayer.ps1 -AcceptMinecraftEula
# Real dedicated save/reload, fresh fixture directory, two separate JVM runs
.\gradlew.bat runRestartSmoke -PsmokeMode=seed -PsmokeRunDir=build/run/restartSmoke-new -PacceptMinecraftEula=true
.\gradlew.bat runRestartSmoke -PsmokeMode=verify -PsmokeRunDir=build/run/restartSmoke-new -PacceptMinecraftEula=true
# Interactive development
.\gradlew.bat runClient
.\gradlew.bat runServer
# Reproduce generated economy/advanced data and original assets
node scripts/generate-economy.cjs
node scripts/generate-advanced.cjs
```

Build output: `build/libs/metallic-arts-fabric-0.8.0-alpha.1.jar`. Test XML/reports: `build/test-results` and `build/reports/tests`. Client screenshots: `build/run/clientGameTest/screenshots`. Multiplayer writes a timestamped result under `build/run/multiplayer-*`. Never run resource rebuilds concurrently with Minecraft test processes using those resources.

Manual smoke test: open/close M/F/H/P and Guide; reconnect; store/tap a real contacted mind; verify its balance after saving/restarting; test failed compounding preserves its source; test medallion copies cannot equip twice. Use a disposable backed-up test world for extraction/body dependency consequences. [Current validation](docs/RELEASE_VALIDATION.md) records actual automated scope and exclusions.

## Status, attribution and future work

Phases 1–7 retain their playable foundations. Steps 1–8 of the follow-up now include advanced mechanics, survival/equipment and release infrastructure, with the limits below. This remains an **alpha**. [Changelog](docs/CHANGELOG.md), [canon provenance](docs/CANON.md), [attribution/license review](docs/ATTRIBUTION_REVIEW.md) and [detailed new mechanics](docs/ADVANCED_SYSTEMS.md) explain decisions. Source-license selection and fan-use/branding clearance remain unresolved; no open-source or commercial permission is implied.

## 1. External acceptance before a stable release

- Run authenticated accounts on a remote dedicated server, then test latency, loss, extra players and long sessions. Local ordinary clients use offline-mode loopback.
- Install actual claim/PvP/protection mods and test adapters against their APIs. Deny-wins hooks and native cancellation tests do not certify an untested mod.
- Profile temporal fields, entities, projectiles and machines over hours; measure CPU, bandwidth, save growth and interpolation. The 60-second load fixture is a regression baseline, not capacity certification.
- Test archived real 0.5/0.7 world upgrades, crashes during transfers/saves and whole-backup restoration. Serialization fixtures do not certify every historical save or crash boundary.
- Validate Linux/macOS native clients and servers. Linux CI covers build/unit/datagen, not the graphical runtime.

## 2. Presentation and accessibility

- Replace remaining English literals and translate the full UI/tutorial. Current coverage is the English guide and initial Spanish navigation/messages.
- Rebind every contextual shortcut; test keyboard/narration, high GUI scales and narrow windows across all menus. Add per-client sensory controls; Bronze sound is currently a host setting.
- Add per-metal tutorial examples, clearer typed ownership/charge tooltips, source shutdown explanations and combined medallion status. Current carriers unfold into independent slots.
- Replace shared vanilla placeholder item references with distinctive original art, with provenance review.

## 3. Standard-metal depth

- Bronze: broader evidenced Investiture detection and weak/strong concealment tuning beyond paid player/NPC Allomancy pulses.
- Zinc/Brass: richer NPC decisions, competing influences and awareness; add each NPC metal with actual ownership/fuel.
- Enhancement: researched per-metal burst duration/output and actual third-party protection/mutual-contact tests. Current one-frame capped burst is an approximation.
- Gold/Electrum: more actual character alternatives and projectile/latency uncertainty. No fabricated biography or perfect future prediction.
- Temporal: explicit new machine/vehicle adapters and entry/exit tests. General redstone, random/scheduled ticks and player physiology remain global.
- Body Feruchemy: environmental/survival-mod adapters, long balance sessions and healing priorities. Bounded trauma is not amputation/regrowth, ageing or disease simulation.
- Copper: permission-safe sharing, richer organization and knowledge-system adapters. Real human knowledge cannot be erased.
- Identity/Connection/Fortune: explicit faction/language/social adapters and meaningful bounded opportunities. No arbitrary impersonation, universal translation or destiny control.
- Compounding: consider a separately swallowed-metalmind interface and residual-material recovery; current whole-source burn is deliberate and canonical-once.

## 4. Constructs and spiritual consequences

- Add distinct NPC construct species/AI and more researched forms. Current player templates are Koloss/Inquisitor/Kandra, with wolf/slime Kandra bodies.
- Research more placements, unusual spikes, multiple-power extraction, anatomy and psychology. The twelve-point grid and preparation recipes are adaptations.
- Expand exceptional extraction and Invested resistance only with explicit source accounting and consent/protection policy.
- Balance growth, collapse, lineage acquisition and recovery through survival playthroughs. The human-to-mistwraith ritual is a game recipe, not asserted canon.

## 5. Survival depth

- Add custom powered NPC identities, factions, quests, structures and authored discoveries. Current powered population uses vanilla hostile mobs, finite fuel and a bounded active subset.
- Add autonomous NPC storage/tapping and more metal AI. NPC Feruchemy is currently an actual donor source, not active Feruchemical AI.
- Balance latent allocation/snapping/discovery through playtests. Some characters have neither Art; unsealed equipment and Hemalurgy are alternate paths. Death/extraction must not reroll powers.
- Add authored advancements, recipe-book onboarding, rare loot/trade structures and individual-metal tutorials. Existing ore economy/four professions remain the baseline.

## 6. Invested material research

- Verify a published Excisor process before asserting canonical manufacturing. Extend the 2–3 core carrier only with independent charge/lease accounting and evidence.
- Design pure Atium separately from Era-one Atium/electrum; design Malatium and researched prediction interactions.
- Research Harmonium, Trellium and other shard metals/alloys/hazards individually. Unknown effects remain disabled rather than guessed.
- Recheck direct author sources when accessible. Lerasium strength, non-stacking and acquisition costs are balance choices, not canon quantities.

## 7. Release governance

- Obtain the owner's source-license choice and any branding/fan-use review. The technical attribution inventory is not legal clearance.
- Maintain immutable versioned Releases, checksums, matching dependency notes and changelogs; do not overwrite existing assets under the same version.
- Expand CI/native/multiplayer regressions and issue templates as adapters land; distinguish implemented scope, observed tests and open acceptance.

