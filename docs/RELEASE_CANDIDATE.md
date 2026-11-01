# Release-candidate mechanics and limits

This document describes **1.0.0-rc.2**, supplementing earlier phase guides. Read RELEASE_VALIDATION.md for observed results; implementation and verification are separate.

## NPC powers and canonical resources

NPC Allomancy accepts all sixteen standard metals through the same paid burn engine. Natural single-metal profiles omit Aluminum/Duralumin because those alone do not form useful attacks. NPCs use actual Tin/Pewter modifiers, reciprocal metal force, competing emotions, Copper concealment, Bronze pursuit, paid enhancement contact, a remembered Gold refuge, bounded projectile evasion and Cadmium/Bendalloy bubbles. Distances, cooldowns, caps and decisions are gameplay policies. Prediction is neither perfect aim avoidance nor omniscience.

NPC Feruchemy creates an empty physical ring once when an eligible profile has no ring or lease. Resting storage surrenders the corresponding attribute; combat tapping spends the canonical balance. Gold costs health, Bendalloy nutrition, Cadmium air, Brass warmth and Bronze fatigue. Copper stores actual observed patrol refuges and consumes them on retreat; Nicrosil stores one owned typed Allomantic ability, suppressing only that type while storing and restoring finite paid access while tapping. Body rest recovery consumes actual nutrition. Not every abstract attribute has a human-equivalent NPC psychology or language simulation.

Death releases owned physical equipment once. Permanent discard destroys only still-leased NPC assets; released items and player assets remain. Chunk unload/dimension travel retain leases. Session and scan budgets are host-bounded. The spike ledger retains donor-source tombstones even after destruction; reaching its finite world limit stops provisioning safely rather than regenerating powers or crashing.

Koloss, Inquisitor and Kandra have distinct registered entity types and original skins. Their initial prepared canonical implants represent generated backstory sources. Koloss strength/growth depends on four actual staples; Inquisitors require eleven implants including a linchpin, whose loss kills the body. Kandra depends on a linked Blessing pair; loss collapses the body. Material-fed wolf/slime forms use actual body/render/collision state. The twelve-point anatomical mapping, exact counts/recipes/growth and AI are documented adaptations, not an exhaustive canon model.

## World progression and factions

Research camps place a 5×5 shelter only in eligible new terrain after checking every occupied block, fluids, containers and solid floor. Rarity is a placement attempt per 96 chunks, not a guaranteed structure every 96 chunks. Finite loot can contain supplies and very rare god-metal samples. The spawned researcher has an actual Copper Feruchemy profile. Seven advancement milestones and a craftable field guide lead through the existing economy. This is an onboarding path, not a large narrative quest campaign.

Terris/Empire/village/independent standing persists at -100..100. Actual trades add two, injury removes five, hostile construct defeats aid Terris/villagers, and paid gifts add five with a cooldown. Mod price adjustments preserve vanilla demand/reputation. Empire standing can suppress ordinary construct aggression until provoked. Standing is not authentication or protection permission.

## God metals

- Lerasium and sixteen standard alloys retain nonstacking, distinct ownership grants and the host-controlled acquisition policy.
- Pure Atium, Atium/Electrum (called Nalatium here) and Atium/Gold (Malatium) are separate items/windows, never standard-metal IDs. One bead prepays a finite default 100-tick window. Active windows cannot stack; death, disconnect and dimension changes stop them.
- Pure Atium draws longer uncertain trajectories for nearby visible loaded living targets; Nalatium is shorter and requires A-Electrum. Malatium requires A-Gold and displays actual recorded NPC/player history only with the separate player opt-in. Associated alloy access and exact effects are conservative interpretations.
- J can spend ten remaining ticks for a collision-checked predicted opening. No perfect future, private-coordinate payload or fabricated biography is generated. Copper concealment and Bronze signatures remain bounded game rules.
- One pure bead plus ordinary Gold/Electrum produces one supported alloy. Harmonium/Trellium controlled separation needs host-enabled Lerasium acquisition, a dry permitted workstation, eight blaze powder and spare inventory space. Catalyst retention, quantities and safe execution are explicit game policy.
- Dropped Harmonium consumes material in water and produces a bounded reaction. Terrain is preserved. Damage requires a real living owner and applicable PvP/protection permission; ownerless reactions are cosmetic.
- Unknown shard-metal and alloy abilities remain disabled. There is no truthful implementation of canonical effects that have not been published. No Excisor manufacturing process is asserted.

## Consent, protection and recovery

Memory sharing is separate from donor and Malatium consent. A recipient opts in, must be nearby and visible, and receives the exact selected record once; sender removal and recipient capacity are preflighted. The action moves ownership rather than duplicating private memories. Consent can be withdrawn.

The optional Open Parties and Claims adapter resolves the public API at runtime, so no dependency is bundled. Denials win and exceptions/API mismatches fail closed. Claimed blocks, entity force anchors/recoil, temporal processing and NPC effects check protection. Tested pair: OpenPAC 0.33.0 (`HDDpeLJz`, metadata supports 26.1) with Forge Config API Port 26.1.3 (`RXbL43Lt`), on the pinned Fabric stack. Later dependency versions are not automatically compatible. Other claim mods are not certified by these hooks.

Operator `recoverleases` restores a missing contact/spike reference only for a canonical asset already leased to that exact UUID and empty slot. It does not mint an asset or charge, steal a foreign lease or revive a spent source. A deliberate partial player-save crash fixture verifies this specific recovery boundary. Whole-world backups are still required for arbitrary file damage or crashes across multiple assets.

## Presentation

Eleven wrapped/narrated guide pages, 32 metal lessons, original item/NPC art, rebindable context keys and seven client sensory preferences are implemented. Mod sound/particles are separately identifiable so they can be muted without muting vanilla equivalents. Narrow menus have dedicated native regression coverage. Full English text is the reference; Spanish is partial. Text keys facilitate further localization; unreviewed fallback text must not be advertised as a completed translation.

## rc.2 requested gameplay changes

The authoritative eleven-request mapping is REQUESTED_GAMEPLAY.md. Physical charged metals now remain force targets; Aluminum/Duralumin/compositions containing Aluminum are immune. Connected loaded metal has one key, impulse weakens at distance, and explicit Push/Pull intents oppose rather than guessing when both metals burn. Locks persist 200 unavailable ticks with no remote force, and thrown coins transfer selection across their actual impact recovery.

Full spherical field outlines use public geometry without owner/private data. Players inside receive transient local movement/combat/mining/use clocks, and boundary exit removes those modifiers. Network/keepalive/global physiology are not skipped. Source rays show nearby metal in every direction, with rear indicators; there is no hidden inventory scan. Native and graphical tests verify these paths.

All item/workstation stacks now cap at 64 or their smaller item limit. Larger batches split; legacy surplus is preserved. Faster F-Bendalloy food exchange is symmetric at twenty body units per charge unit, with visible food consumed before saturation and exact persistent fractional escrow. This changes the nutrition value of existing charge, so preserve backups. Inventory Arts help and I show actual rebound keys and explain these workflows in game.
