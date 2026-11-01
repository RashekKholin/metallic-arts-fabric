# Remaining work after Phase 7

This is the ordered backlog for the 0.7 playable alpha. A standard metal having a bounded playable handler does not mean every canonical edge case or release acceptance check is complete. Compounding and full constructs have not been implemented.

## 1. Hardening and compatibility before more powers

- Exercise the existing multiplayer harness on authenticated accounts and a remote dedicated server; repeat under latency, packet loss, reconnects, different client frame rates and additional players. Local offline loopback evidence is distinct from public-server validation.
- Test actual protection/claim/PvP mods, especially Chromium/Nicrosil contact, emotions, lifting blocks, moving objects, extraction, inventory transfers and merchant price updates. Existing native protection events are not a guarantee of every mod's integration.
- Run sustained sessions with overlapping temporal bubbles, many entities/machines/projectiles and multiple concurrent users. Profile CPU, network traffic, save size, client interpolation and entity removal. Preserve force-order independence and bounded work.
- Expand adversarial tests for exhausted source removal, partial payments, death/respawn/dimension changes during every advanced effect, disconnect mid-memory transfer, capped body resources, duplicate/stale references and cross-dimension copies. Keep whole-world recovery instructions and fixtures current.
- Add explicit schema migration tooling and operator recovery/inspection commands. Never reconstruct charge from a preview or silently repair duplicated ownership. Test upgrades from archived real 0.5 worlds as well as codec fixtures; document irreversible changes/downgrade limits.
- Extract tunable advanced constants into validated server configuration: bubble rates/radii, emotion cadence/caps, body rates, healing/cure/emergency prices, opportunity scans, trade coefficients and unsealing costs. Current explicit defaults are documented policies, not fully exposed configuration.

## 2. Refine standard-metal interpretations

- Allomantic Bronze: short-pulse history, accessible sound signatures, sensory options, stronger/weaker concealment edge cases and broader Investiture detection only after explicit rules. Current ten-tick sampling can miss very brief burns.
- Zinc/Brass: richer eligible NPC reactions, opposing influences, awareness UI, resistances and recovery tuning; no unrestricted mind control. Expand NPC state support deliberately.
- Enhancement: research and test per-metal burst duration/output, protection-mod callbacks and simultaneous mutual contact. Current one-frame capped burst and one-second contact cadence are documented approximations. Add active-burning NPC targets with actual fuel/ownership rather than pretending every mob burns.
- Gold: richer actual character events, useful alternate-self choices and presentation; no fabricated biography. Electrum: projectile-specific physics, uncertainty, latency, more combat possibilities and eventual atium interaction. Predictions currently approximate bounded movement/projectiles.
- Temporal bubbles: preserve native clock accounting under rate changes, collision/entry/exit and nesting; add supported adapters where justified. Vehicles, player physiology, general redstone/random/scheduled ticks and arbitrary block entities currently use global time. An arbitrary local-time simulation is a separate research/performance task.
- Feruchemical Bronze/Brass/Cadmium/Bendalloy: longer balance sessions for fatigue/sleep, temperature/environment, oxygen and food/saturation; adapters for gas, seasons, survival/temperature mods. Body escrow resets on death; evaluate that policy without duplication or death loops.
- Zinc/Electrum: broaden useful cognition and adversity interactions beyond current action/analysis/emotional resistance handlers. Keep these distinct from physical speed and strength.
- Copper: capture more actual discoveries/events, naming/search and memory organization, permission-safe sharing, reliable long-term journal limits. Integrate other knowledge systems only through explicit adapters; real player knowledge cannot be erased.
- Gold: anatomical injury/status adapters, healing priorities, interrupted/cancelled damage tests and emergency-balance review. No current limb regrowth/age/disease simulation or exceptional Hemalurgic survivability.
- Identity/Connection: broader supported social/faction/language systems, per-person relationships and documented access policies. Current Connection is villager trade rapport plus actual regional familiarity; Identity is filling/access/resistance, not arbitrary impersonation.
- Fortune: more meaningful bounded opportunity providers, explicit uncertainty and balance across loot contexts. Vanilla Luck alone does not change every random roll; no arbitrary destiny/causality simulation.
- Nicrosil/unsealed equipment: research fuller medallion/Excisor behavior, multiple ability constructions, production and accessible ownership previews. Current one-type finite-access construction is an explicit game adaptation, not a verified canonical medallion factory.

## 3. Compounding

1. Specify the transaction contract: distinguish ordinary fuel, invested material, stored attribute/ability content, mass, Identity and ownership.
2. Define supported per-attribute outputs and costs. Typed abilities and discrete memories cannot silently become generic energy.
3. Consume the exact authorized physical invested material once; invalidate its previous canonical use and preserve residual mass/charge accurately. Prevent burning and tapping the same charge twice, copying/splitting exploits and invalid cross-world references.
4. Implement amplification through separate Allomantic/Feruchemical accounting, with interruptions, capacities, source shutdown and usable feedback.
5. Add unit, server, client, two-client and dedicated restart regressions before exposing normal gameplay recipes. Compounding is currently absent.

## 4. Advanced Hemalurgy and constructs

- Research canonical evidence for supported placements, exceptions and construct dependencies. Existing twelve-point human grid is an explicitly inferred playable layout.
- Implement distinct researched Inquisitor/koloss/kandra templates, anatomy/body changes, required spikes, removal dependencies and linchpin consequences; do not use one generic stat multiplier as a full construct.
- Expand spiritual vulnerability and emotional control interactions while retaining consent/protection policy for players. Current fortitude/instability/passive handlers do not simulate every construct psychology.
- Implement exceptional/nonlethal extraction only with explicit rules, consequences and rollback/resource accounting. Normal extraction currently requires lethal donor loss and refuses death-prevention equipment/armed Gold recovery.
- Research unusual Invested/god-metal spikes, multiple-power extraction and native-attribute consequences separately; source-scoped theft must preserve unrelated grants and keep extracted-source tombstones.

## 5. Survival acquisition and world progression

- Design natural access to Misting/Mistborn/Ferring/Feruchemist powers, rarity and progression, with configurable server policy. New characters currently start powerless; operator grants and powered-donor setup are development tools.
- Add genuinely powered NPCs with actual spiritwebs, fuel, AI and loot/protection behavior. Hemalurgy currently transfers actual configured donors; it does not seed a world population of powered NPCs.
- Add discovery/tutorial progression, balanced recipes/trades and resource scarcity. Decide how unsealed equipment is acquired and produced in survival.
- Optional mixed-metal vials need composition, purity, capacity, invested-subset handling, brewing/ingestion UI and conservation tests. Expand material/tag interoperability and deliberate Investiture resistance.
- Add god metals/alloys only after explicit researched rules, source provenance and separately scoped effects. No fictional universal compatibility promises.

## 6. Presentation, accessibility and release

- Finish wheel icons/highlighting, readable tooltips, clear typed-charge/access/Identity states, status messages, key-conflict handling and low-resolution/high-GUI-scale layouts. Keep source-specific shutdown understandable.
- Add optional sound/visual accessibility controls, color-independent cues, screen-reader/localization coverage and translations. Current advanced explanatory strings are mostly English.
- Add in-game tutorials and examples for every Art, metal, body cost, privacy, equipment transfer and unsealing. Keep the README and test guide synchronized.
- Publish permanent versioned GitHub Releases with installable jar, sources, checksums, changelog and exact matching dependencies; CI artifacts currently expire and require GitHub sign-in.
- Add platform coverage (Linux/macOS clients/dedicated servers), longer save/recovery and compatibility matrices, automated repeated multiplayer runs and performance baselines. GitHub CI currently validates compile/unit/datagen; local native tests are separately recorded.
- Finish attribution/license review and public issue templates; label experimental approximations and unsupported interactions in release notes. A playable alpha is not a finished public release.
