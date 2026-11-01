> Historical phase guide: current 1.0.0-rc.2 additions and limits are in [RELEASE_CANDIDATE.md](RELEASE_CANDIDATE.md), with observed results in [RELEASE_VALIDATION.md](RELEASE_VALIDATION.md). Earlier future/deferred statements below describe that phase, not the current release.

**Version 0.7 update:** this physical/foundation guide is extended by [ADVANCED_ARTS.md](ADVANCED_ARTS.md). Read that guide for remaining-metal handlers, native advanced passives, Identity/unsealed access, controls and exact limits. Historical future-scope statements below describe the original foundation release.

# Hemalurgy foundation — 0.5.0-alpha.1

Hemalurgy now has real intentional extraction, canonical charged spikes, anatomical insertion/removal, sourced Allomantic/Feruchemical access, decay and blood preservation. Some stolen types are **typed hooks only**, not finished cognitive/Spiritual effects. Neither a registered power nor an implanted spike implements one of the twelve still-missing Allomantic/Feruchemical effects.

## A controlled first test

Use a fresh cheats-enabled test world. Natural powered-NPC progression is future work; the following command explicitly creates a development donor.

1. Obtain a **Steel Spike** from the creative inventory or `/give @s metallicarts:steel_spike`, then return to survival. Have ordinary Steel fuel available if you want to use the stolen power.
2. Spawn a villager a few blocks away and grant it Steel Allomancy:
   ```mcfunction
   /summon minecraft:villager ~ ~ ~2 {NoAI:1b,Tags:["metallicarts_test_donor"]}
   /metallicarts grantdonor @e[tag=metallicarts_test_donor,sort=nearest,limit=1] allomancy steel 1
   ```
3. Hold exactly one uncharged Steel spike. Aim at the donor's chest within three blocks with line of sight. Press **M → H**.
4. Select **allomancy steel** using the arrows. Read the effect availability label. Turn **Intent: ON**, then click **Extract aimed**. This interaction is lethal to a valid donor. The server checks the actual held material, donor-owned capability, reach, chest aim and attack permission; it never accepts client-provided donor/charge/source IDs.
5. Select **5 Left chest**, then **Insert held**. The actual item leaves your hand and the spike grants its own sourced Steel access. Press **A** to return to Allomancy. Ingest Steel fuel, then use its normal burn/push controls. Hemalurgy grants access, not free fuel.
6. Return with **H**, select the occupied point and click **Remove**. Make inventory space first. You receive the same canonical asset at a new transfer revision and lose only that spike's grants. Other independent powers remain.

For physical Feruchemy, use a **Pewter spike**, grant the donor `feruchemy iron` (or Steel/Tin/Pewter), and select the corresponding Feruchemical capability. A matching implanted stolen Feruchemical source can authorize that donor's keyed metalminds. New minds filled by the recipient key to the recipient; this does not create shared access to other recipients' new minds. Removing/draining the qualifying spike removes the donor-key authorization on the next server tick, even if another source still grants the same art.

Ordinary humanoid innate attributes can be extracted without an administrative power grant. The default donor tag includes villagers, pillagers, vindicators, evokers and witches; this is an explicit Minecraft adaptation. Animals, undead and arbitrary mod entities are refused unless deliberately added to the data-pack tag `metallicarts:hemalurgic_donors`. This is not a universal canon claim about those entities' souls.

## Controls and placement

**M → H** opens twelve numbered anatomical points. The selected point shows its region and occupied capability/potency. Insertion validates both the charged spike's material and the point's compatibility. Points are defined in `metallicarts/binding_points.json`; exact coordinates/allowed combinations are labelled **GAMEPLAY_INTERPRETATION**. The catalog's source supports the importance of placement, not these exact twelve locations. This is not a complete canonical binding-point map.

**Point 12, upper back**, is an explicitly dangerous experimental linchpin hook. It requires the separately labelled **Insert critical** action. Removing it while other spikes remain attempts lethal damage once, through normal damage handling. It does not serialize a repeated death action. Inquisitor/koloss/kandra transformation, their complete anatomy/dependencies and exceptional survivability remain future construct work; this hook does not implement those creatures.

**Toggle consent** controls your own player-extraction consent for this connection/life. It does not enable server policy. Default `config/metallicarts-server.json` has `allowPlayerHemalurgy: false`. Player extraction requires that option **and** current donor consent **and** vanilla PvP/team permission. Creative donors, spectators, out-of-range donors, wrong materials, missing powers and held Totems of Undying are refused. Consent clears on death, successful player extraction, logout and server stop.

## Current material/type compatibility

This is the implementation's working standard-metal compatibility interpretation. The primary author annotation establishes typed transfer and placement-dependent outcomes; specific point layouts, numerical losses and uncertain advanced interactions are documented adaptations.

| Spike metal | Stored/transferred type | Current effect status |
| --- | --- | --- |
| Iron | Innate strength | Melee/knockback and carrying handler |
| Steel | Physical Allomancy | Iron/Steel/Tin/Pewter handlers available |
| Tin | Innate senses | Sensory framework handler |
| Pewter | Physical Feruchemy | Iron/Steel/Tin/Pewter handlers available |
| Zinc | Emotional fortitude | Reduces current instability/vulnerability score; full emotional system later |
| Brass | Cognitive Feruchemy | Typed grants; effect handlers Phase 7 |
| Copper | Cognition/memory | Typed charge/provenance hook; no memory theft/restoration UI yet |
| Bronze | Mental Allomancy | Typed grants; effect handlers Phase 6 |
| Aluminum | Power nullification | Removes donor grants; creates a spent record, no transferable power grant |
| Duralumin | Connection/Identity | Typed hook; no general social/Identity rewrite yet |
| Chromium | Fortune/destiny | Uncertain advanced interpretation; typed hook only |
| Nicrosil | Investiture | Typed hook; does not grant arbitrary powers or implement unsealed equipment |
| Gold | Hybrid Feruchemy | Typed grants; effect handlers Phase 7 |
| Electrum | Enhancement Allomancy | Typed grants; effect handlers Phase 6 |
| Cadmium | Temporal Allomancy | Typed grants; effect handlers Phase 6 |
| Bendalloy | Spiritual Feruchemy | Typed grants; effect handlers Phase 7 |

Each art spike stores **one selected metal capability**, not the whole category. Nullification is deliberately distinct from stealing a usable art: its spent spike cannot implant or confer nullification. Its destructive extraction behavior is an explicit policy for an otherwise under-specified interaction.

## Accounting, decay and source resolution

The world ledger owns spike ID/seal/revision, material, typed capability, donor entity UUID, private donor Spirit Identity, extracted grant/source, original potency, extraction loss, outside exposure, blood preservation, anatomical lease and drained state. Owner-only UI previews exclude the private donor Identity. World-bound tokens imported into another world are refused. Copying an item cannot multiply usable charge; transfers invalidate older revisions and only one anatomical lease may own an asset.

Numerical game policy:

- Extraction retains **90%** of the selected source strength. A weaker donor creates a weaker spike.
- Outside exposure uses server-world game time, including time an item was not individually ticking. It does not charge offline wall-clock time. Outside exposure advances at 20 units/tick, or 1 unit/tick while blood-preserved; implantation stops it.
- Potency is `extractedStrength × (0.1 + 0.9 / (1 + ln(1 + exposure / 480000)))`. It decays monotonically toward a nonzero floor. These exact rates, curve and 10% floor are not claimed as canonical measurements.
- A tooltip is a preview and refreshes on use. Canonical age is resolved before implantation or preservation changes; editing the displayed potency cannot restore charge.
- Distinct Allomantic spike sources add to the strongest non-spike base; duplicate records for one source count once. Total access has a technical cap of 1,000, while existing effect handlers retain their lower effect caps. Feruchemy uses the strongest access grant, with its existing throughput limits. Exact stacking numbers are a documented interpretation; provenance is preserved for later refinement.
- Current vulnerability counts implanted usable spikes, reduced by innate emotional-fortitude support. Above three effective spikes a small separately owned movement penalty models instability. Future emotional/Investiture handlers can use the exposed score; Ruin/Harmony possession or arbitrary mind control are not implemented.

Extraction reserves the donor source before damage callbacks, escrows the real held item, removes the selected grant before lethal consequences, and publishes one charged item only after success. Rejected damage restores the source/held item and cancels the provisional ledger entry. Parent Hemalurgic assets are marked drained when their granted capability is extracted, so recalculation cannot regenerate it. Donor-source tombstones persist across saves. Ordinary protection checks and failure cases are tested; arbitrary third-party death callbacks with irreversible side effects are not promised to be globally transactional.

Implants, source provenance and charge survive death as the same explicit equipment-retention game policy as contact gear. Active Allomancy/Feruchemy still stops. Source recalculation precedes Feruchemy and Allomancy each tick. Duplicate/stale/foreign leases confer no power. Full inventories refuse removal without losing the physical item. Charged items cannot enter ordinary metallurgy/trading or be silently repurposed as metalminds.

## Blood preservation

In survival, hold exactly one **empty glass bottle** with more than three hearts. In M → H click **Draw blood**. Successful collection deals four ordinary magic-damage health points and creates one blood vial; damage protection/spawn grace can refuse it. This draws blood only, not a stolen capability. It is a game adaptation, not exceptional nonlethal Hemalurgy.

Hold a free charged spike in the main hand and a blood vial in the offhand, then click **Blood preserve**. One vial is consumed and future outside decay slows twentyfold. Clicking again removes preservation without refunding the blood. Implantation itself stops outside exposure. No charged-item reshaping/splitting, god-metal exceptions or decay-free advanced preservation is implemented.

## Research and remaining work

Sanderson's [Hero of Ages chapter 39 annotation](https://www.brandonsanderson.com/blogs/blog/annotation-mistborn-3-chapter-thirty-nine) supports typed transfer, placement-dependent outcomes and the distinction between stealing powers and innate attributes. It does not establish this mod's numerical curve, exact placement grid, donor tag, death-retention policy or detailed advanced handlers.

The next work is the README's dependency-ordered roadmap: real two-client contention/privacy/lifecycle checks; complete remaining Allomancy and Feruchemy handlers; cognition/memory and Connection/Identity/Investiture/Fortune systems; researched construct templates and exceptional extractions; natural survival acquisition; migrations, compatibility and release polish. The current typed hooks must not be described as completed advanced powers. See PHASE5_VALIDATION.md for actual evidence and outstanding checks.
