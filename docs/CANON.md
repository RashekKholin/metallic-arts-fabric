> Historical phase guide: current 1.0.0-rc.1 additions and limits are in [RELEASE_CANDIDATE.md](RELEASE_CANDIDATE.md), with observed results in [RELEASE_VALIDATION.md](RELEASE_VALIDATION.md). Earlier future/deferred statements below describe that phase, not the current release.

# Canon and interpretation policy

The executable provenance catalog is `src/main/resources/metallicarts/rules.json`. Common initialization validates that entries have unique IDs and that author-statement entries cite a source. It is bundled evidence, not a datapack loader or finished canonical rules engine.

Use published narrative/Ars Arcanum material first, author statements with their context second, and explicit documented interpretations where evidence is incomplete. Never mark an engineering choice as established canon. Future entries should include specific source locators and separate conflicting-era evidence rather than guessing a precise number or binding point.

**Current 0.8 scope:** [ADVANCED_SYSTEMS.md](ADVANCED_SYSTEMS.md) documents compounding, prepared player constructs, once-only progression, mixed vials, multi-core carriers and the researched Lerasium subset. Sections below record historical implementation decisions; their original future-work notes do not describe the current feature inventory.

## Evidence reviewed for this foundation

Brandon Sanderson's [Hero of Ages chapter 39 annotation](https://www.brandonsanderson.com/blogs/blog/annotation-mistborn-3-chapter-thirty-nine), under “The Mechanism of Hemalurgy,” describes transferring Allomantic/Feruchemical abilities and the relevance of spike placement and use. This supports separate typed powers and source provenance. It does not establish this mod's numeric strength scale, duplicate-grant stacking rule, UUID representation or any complete binding-point table.

## Explicit Phase 1 choices

- Preserve independent grants instead of storing a single `isMistborn` flag. The same power can retain innate and external sources without losing origin information.
- For the access display, use the strongest grant. Do not multiply or add duplicates yet. Preserve the original grants so a later researched rule can change resolution without rewriting history.
- Start new characters without powers; acquisition is a later system. Copy the spiritweb attachment on death as a game policy, with no active effects serialized.
- Keep sixteen standard metal IDs as the initial closed catalog. God metals, alloys of god metals and supporting mundane materials are future additions rather than guessed capabilities.
- Item Identity and charge fields are data foundations. Their presence does not implement unkeyed/unsealed access, nicrosil storage, or Compounding.

No canonical claim is made for the technical quantity limits, cooldowns, charge units or schema version. Exact alloy compositions, Hemalurgic anatomy, duplicate-power interaction, Fortune, Connection and time behavior must be researched at their implementation phase. Intuitive canon-consistent interpretations should be labeled and reviewable when primary evidence is incomplete.

## Phase 2 choices

The [economy guide](ECONOMY.md) lists the eight current alloy ratios. They are documented interpretations for this implementation, not newly verified primary-source claims. Correct proportions produce viable material; small errors produce imperfect material; large errors produce invalid residue. This makes the distinction visible without presenting a guessed toxicity model as canon.

Credited ore yields, mass units, fuel cost, processing time, cadmium byproduct quantity and villager prices are gameplay policies. In particular, sand-to-silicon and coal-to-carbon are simplified material acquisition paths, not realistic chemical processes.

Unsafe metals are refused by brewing/ingestion. Drinking stores fuel without granting or activating access. Invested flaking preserves material, proportional charge and Identity on the source/remainder/output; it does not establish that a character may tap that charge or Compound it.

The Phase 2 catalog contained nine entries. Advanced canonical anatomy, god metals, Fortune and Connection remain unresolved and unimplemented.

## Phase 3 development choices

The catalog now contains twelve entries. Sanderson's [Well of Ascension chapter eight annotation](https://www.brandonsanderson.com/blogs/blog/annotation-mistborn-2-chapter-eight) describes larger metal sources as stronger Push/Pull anchors and small flakes as poor anchors. The force implementation preserves that qualitative distinction; effective masses, grounded bracing, speed limits and exact impulses are explicit game adaptations.

His [Hero of Ages chapter twenty-six annotation](https://www.brandonsanderson.com/blogs/blog/annotation-mistborn-3-chapter-twenty-six) discusses Tin's sensory filtering and the alertness associated with Tin and Pewter. This does not establish our night-vision brightness, sound gain, overload thresholds, movement penalties or fatigue duration. Those remain gameplay policies.

The first four effects use the strongest ownership grant, with capped effective strength. Pure aluminum forms are excluded; invested item forms conservatively resist ordinary forces entirely pending advanced resistance rules. Tagged fixtures and external armor are supported; internal reserves are not external targets. This is not a complete treatment of aluminum alloys, ores, living Investiture, anchored structures or in-body metal.

Prepaid microgram rounding prevents free effects through toggling. Reserving the invested subset from ordinary burning protects charge and Identity; it does not implement Compounding. See [ALLOMANCY.md](ALLOMANCY.md) for every current numeric policy and the remaining Phase 3 scope.

## Phase 4 choices

The physical Feruchemy ledger enforces separate assets, contact, relevant access and Identity. It does not reinterpret ordinary micrograms of ingested fuel as stored attributes. First filling binds an ordinary empty mind; unkeyed access still requires the ability. Administrative unkeying exists for validation; normal Aluminum/Nicrosil interactions are future work.

Every capacity, rate, unit, high-rate cost, mass, carrying profile, cue radius, body-scale change and sensory overload threshold is a gameplay policy, documented in FERUCHEMY.md. Separate Tin senses are represented by separately typed minds. Contact gear survives death as an explicit game policy; modes and effects stop on lifecycle changes. Copied previews cannot mint canonical charge.

Human equipment slot names and the heavy-block whitelist are Minecraft adaptations, not a canonical anatomical map or exhaustive material physics. Full anatomy and extraction research belongs to Phase 5; the mod does not claim those systems exist merely because spike items exist.

## Phase 5 choices

Hemalurgy has canonical charged assets, typed donor/source provenance, intentional lethal extraction, material/binding checks, anatomical leases, blood preservation, decay and source-specific art grants. The [Hemalurgy guide](HEMALURGY.md) documents all numeric adaptations and the working material matrix. The primary author annotation supports typed transfer and placement-dependent outcomes, not an exhaustive exact anatomy or these gameplay numbers.

This phase revises the historical strongest-only Allomantic policy: distinct spike sources add to the strongest non-spike base, deduplicated by source and bounded by access/effect caps. Feruchemy retains strongest access with its throughput limits. Raw grants and their provenance remain preserved. A matching stolen Feruchemical source can authorize its donor's keyed minds as an explicit interpretation; new recipient-filled minds retain the recipient's Identity.

The twelve anatomical point layouts and critical upper-back hook are labelled interpretations. Critical removal attempts lethal damage once, respecting ordinary damage handling. Full construct transformations, complete canonical binding maps, cognitive/memory theft, Connection/Identity rewrites, Fortune/destiny and arbitrary Investiture effects remain deferred. Their typed records do not count as working powers.

Player extraction is off by default and further requires current donor consent plus PvP permission. Humanoid NPC eligibility uses an explicit Minecraft data-pack tag. Blood collection is an ordinary damaging collection action, not a claim to implement exceptional nonlethal extraction. Aluminum's destructive spent-record behavior, 90-percent extraction retention, the nonzero-floor decay curve, twentyfold blood preservation and equipment retained on death are game policies.

## Phase 6–7 interpretations

ADVANCED_ARTS.md and bundled rules.json explicitly distinguish observed Minecraft behavior from canonical claims. Local temporal clocks, finite body escrow, recorded-path Gold, collision-bounded Electrum, villager Connection, Luck/opportunity Fortune and single-type unsealed construction are documented game adaptations. The consumed Duralumin binder is not a verified canonical Excisor recipe. The original Phase 7 baseline deferred constructs, god metals and Compounding; bounded player constructs, compounding and Lerasium now exist as documented in ADVANCED_SYSTEMS.md. Unresolved god metals and complete canonical metaphysics remain deferred.


## Current release-candidate provenance

The 1.0.0-rc.1 generator reconciles old deferred status and adds separate rules for authored NPC provenance, consent-based memory movement, camp/faction progression, distinct Atium/alloy prediction and Harmonium separation. Initial NPC implants are authored generated-world sources, not invented theft from real accounts. Exact numerical costs, body slots, predictions, finite bead windows and faction scores are explicit interpretations/game policy.

The official publisher's [Lost Metal chapters sixteen–eighteen excerpt](https://reactormag.com/read-the-lost-metal-by-brandon-sanderson-chapters-sixteen-through-eighteen/) and adjacent official excerpts were consulted for Harmonium/Trellium separation context. They do not establish our crafting recipe, safe workstation, water damage or quantities. The [author Q&A event distinguishing Atium/Electrum](https://wob.coppermind.net/events/486-general-reddit-2022/#e15955) was discoverable in the search index but direct event retrieval was blocked during research; this access limitation remains recorded. No precise new ability or quantity is presented as a directly verified quote from that blocked page.

Unknown god-metal alloys and the unpublished Excisor process remain unresolved. The implemented subset is listed explicitly in RELEASE_CANDIDATE.md; its existence does not certify complete canon.
