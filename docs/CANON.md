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
