> Historical phase guide: current 1.0.0-rc.1 additions and limits are in [RELEASE_CANDIDATE.md](RELEASE_CANDIDATE.md), with observed results in [RELEASE_VALIDATION.md](RELEASE_VALIDATION.md). Earlier future/deferred statements below describe that phase, not the current release.

# Advanced systems in 0.8.0-alpha.2

These are playable, bounded Minecraft interpretations. A listed feature does not imply every cosmere interaction has been simulated. See the bundled `metallicarts/rules.json` and [canon policy](CANON.md).

## Controls and deliberate actions

- **M**: Allomancy and ownership; **Guide** opens seven short tutorial pages.
- **F**: contact equipment, store/tap, typed Nicrosil choice, unsealing. Twelve individual contact slots remain authoritative.
- **H**: Hemalurgy, actual source selection, implantation/removal and consent.
- **K**: actual character history; memory search and annotation use the server's accessible records.
- **P**: compounding, spike preparation, body transformation, shape selection, compulsion and exceptional extraction. Change pages with arrows. **Intent must be ON** for consuming or body-changing actions.
- Vanilla Controls rebinds the world-opening keys. Standard button focus supports Tab/Shift+Tab/Enter and narration. Some contextual menu shortcuts remain fixed and some labels remain English.

## Compounding

Own both the relevant Allomancy and Feruchemy. Hold one charged registered ring/bracelet/bracer/earring, free of contact leases. Wear a compatible destination with enough capacity. Choose its slot in P, enable Intent, then Compound. The **whole physical source and its stored charge are consumed**; at the default multiplier the contacted destination receives ten times the source's charge. It is then tapped normally. This direct source-burn action replaces a separate swallowed-metalmind interface.

The source's permanent spent record rejects copies after restart. Its Identity must authorize access; target Identity and blanking rules still apply. Attribute and typed Nicrosil ability must match exactly. No arbitrary conversion between metals, powers, invested flakes or legacy preview values. Copper restores the exact stored memories to the journal once; it does not create multiple copies of facts.

## Injury, memory and social refinements

Survived damage of at least six creates a bounded arm/leg/head injury, with actual mining, movement or attack consequences. A craftable bandage (wool/string) heals one level while still and outside hurt cooldown; Gold tapping can spend the configured cure charge to heal a level even at full ordinary health. This is not a limb-amputation, ageing or disease simulation.

Journal capture includes actual ore mining, defeats, respawn and completed trades. Search supports labels, kinds, dimension and recorded coordinates. Renaming an accessible memory preserves its ID, position, time and type. Stored inaccessible memories cannot be renamed. The bounded discovery history evicts older entries rather than freezing permanently. Actual completed merchant interactions add per-NPC familiarity; Connection also observes existing scoreboard team membership without altering permissions or impersonating a player.

Bronze keeps short paid pulse history, including concealment at emission and current concealment. Its sound option is in host policy. Temporal machine clocks carry fractional credit across rate changes and refuse duplicate same-tick payment. Only the supported machine adapters have local processing clocks; general redstone, vehicles, random/scheduled ticks and player physiology remain global.

## Prepared constructs

P prepares **charged, free, canonical** spikes, consuming ordinary bones. Preparation is recorded in world data; editing a preview does not create a valid preparation.

- **Koloss:** four prepared Iron strength staples in implant points 1, 2, 5 and 6 (UI numbers). Human base, real growth, blue tint, collision scale, endurance and force. Missing dependencies debilitate it and remove bonuses. Growth is configurable.
- **Inquisitor:** eleven valid spikes, prepared Steel Allomancy eye spikes at points 3/4, prepared linchpin at point 12, and actual Gold Feruchemy access. Blindness plus private metal cues, endurance and actual dependency checks. Linchpin removal can kill once; no automatic respawn death loop. This slot recipe is a documented anatomical adaptation.
- **Kandra:** mistwraith base plus a linked paired Blessing made from two separate donors' compatible **innate** attributes. Ordinary art spikes are not Blessings. Removing one member causes degradation; losing both collapses the body and blocks powers/memory access. It does not erase the player's real memory journal. Wolf/slime forms learn through actual bones and leather/slime costs and change collision, movement and rendering. Only those body templates are implemented.

An opt-in survival human-to-mistwraith ritual is available with `/metallicarts-survival mistwraith`: **32 slime balls, 32 bones, one Nether star**, empty implants and an ordinary human body. Prepare a Blessing first. The survival lineage change has no survival reversal; an operator can recover it. **This is a gameplay acquisition recipe, not a canonical claim that humans can normally become mistwraiths this way.**

Compulsion requires paid Zinc/Brass, sufficient spike vulnerability, extreme existing fear/trust, resistance checks, PvP and protection permission. It gives brief directional compulsion of supported player constructs. No chat/inventory control or arbitrary mob construct AI.

Exceptional nonlethal extraction is **off by default**. If enabled, ordinary player policy, PvP and explicit consent still apply. It transfers a real innate/art source at lower retention, damages the donor and leaves permanent spiritual wounds/max-health loss. Cancelled/protected injury rolls the transaction back. A transferred source cannot be stolen repeatedly. NPC wounds also persist and reapply on load.

## Survival and powered populations

`metallicarts-progression.json` controls enablement, independent latent chances, XP discovery threshold, hostile powered chance, finite initial NPC fuel, experimental Lerasium acquisition and the mistwraith ritual. Defaults: 50% chance for one latent Allomantic metal, independently 50% for one Feruchemical metal; no guarantee of either. These rates are gameplay policies.

Allocation is deterministic for character UUID/world seed and persisted once. Existing characters allocate on their first join after upgrading. Allomancy awakens after surviving an unblocked hit of at least six damage at six health or less; `/metallicarts-survival discover` reveals Feruchemy at level five. XP is a threshold, not a fee. Awakening markers persist independently of grants, so death/extraction/rejoining cannot restore stolen powers. Changing policy affects later unallocated characters, not previous rolls.

Default 2% of newly encountered hostile mobs receive one supported Allomantic ability and finite fuel; some also have a real Feruchemical donor source. Supported active NPC AI is Iron, Steel, Pewter, Tin and Copper for natural populations; operators can additionally activate Aluminum/Duralumin. Actual combat attributes, copperclouds, reciprocal metal-gear force, leech and burst interactions run through shared costs/protection. NPC Feruchemy is currently a donor capability, not an autonomous storage/tapping AI. No fuel regeneration on reload. Existing vanilla hostile entities allocate on their first load after upgrade. This is not a new custom NPC species or faction system.

Non-Metalborn can obtain real unsealed cores from other players and study Hemalurgy. Four metallurgist specialties already trade materials/equipment. They do **not** sell infinite free Investiture cores. There is no fabricated genealogical or universal training-grant system.

## Mixed vials and medallions

With a filled standard or mixed vial in the main hand and one filled standard vial offhand, `/metallicarts-survival mix` combines ordinary metal fuel into one bottle and returns the second empty bottle. Up to sixteen batches. Drinking preflights every reserve before consuming anything; failure keeps all contents. Invested/Identity-bearing previews are rejected.

Make charged **unsealed typed Nicrosil** cores through the existing F workflow, one power per core. Hold two free individual cores in opposite hands and run `/metallicarts-survival bind`. Hold that carrier and a third core to add a third. `/metallicarts-survival medallion 0` unfolds the carrier into contact slots 1–2/3 (command slot index is zero-based). All slots must be empty; all cores preflight before any claim. Tap each Investiture core in F to borrow its specific power; attribute tapping needs its own charged compatible mind. Unequip the cores normally to trade or rebind. This is a finite multi-core carrier, **not a verified Excisor manufacturing process**. It consumes no canonical charge merely by binding; activation pays each core separately. Copies cannot acquire duplicate leases.

## Researched god-metal subset

Separate items represent pure Lerasium and its sixteen standard-metal alloys. **Crouch-use** explicitly consumes a bead and grants the corresponding Allomancy (pure: all sixteen; alloy: one). Strength two and non-stacking replacement are gameplay balance rules; it does not grant Feruchemy. `/metallicarts-survival alloy` consumes a held pure bead plus one ordinary standard-metal ingot offhand to make one associated alloy bead. It does not multiply bead count.

The optional `/metallicarts-survival lerasium` acquisition is **disabled by default**: when the host enables `lerasiumRecipe`, a character with a recorded Ender Dragon kill can spend one Nether star and eight Netherite ingots once. The once-only marker persists across death and theft. Operators can provide beads through normal creative tools. This acquisition is a Minecraft endgame policy, not a canon origin.

Research: the [author interview index](https://wob.coppermind.net/adv_search/?tags=lerasium) describes Lerasium alloy Misting creation; [event reference](https://wob.coppermind.net/events/9-calamity-philadelphia-signing/#e7624). Search excerpts were available but direct event retrieval was blocked during this audit, so exact quotes/ratios are not asserted. [Author annotation on Hemalurgy](https://www.brandonsanderson.com/blogs/blog/annotation-mistborn-3-chapter-thirty-nine) informed construct interpretation.

**Not implemented:** pure Atium, Era-one Atium/electrum, Malatium, Harmonium, Trellium, other shard metals and their unknown alloys. They do not silently reuse Electrum or invent powers. The standard-metal ordinal/wheel contracts remain stable.
