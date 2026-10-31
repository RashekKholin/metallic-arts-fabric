# Canon and interpretation policy

The executable provenance catalog is `src/main/resources/metallicarts/rules.json`. Common initialization validates that entries have unique IDs and that author-statement entries cite a source. It is bundled evidence, not a datapack loader or finished canonical rules engine.

Use published narrative/Ars Arcanum material first, author statements with their context second, and explicit documented interpretations where evidence is incomplete. Never mark an engineering choice as established canon. Future entries should include specific source locators and separate conflicting-era evidence rather than guessing a precise number or binding point.

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
