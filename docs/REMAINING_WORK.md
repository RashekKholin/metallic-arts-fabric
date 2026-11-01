# Ordered work after the 0.8 alpha

Steps 1–8 now have concrete implementation or release infrastructure. That does **not** mean every acceptance check or canonical interaction is complete. See [implemented scope](ADVANCED_SYSTEMS.md) and [actual validation](RELEASE_VALIDATION.md).

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
