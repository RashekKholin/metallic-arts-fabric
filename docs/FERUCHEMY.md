**Version 0.7 update:** this physical/foundation guide is extended by [ADVANCED_ARTS.md](ADVANCED_ARTS.md). Read that guide for remaining-metal handlers, native advanced passives, Identity/unsealed access, controls and exact limits. Historical future-scope statements below describe the original foundation release.

# Physical Feruchemy (0.5.0-alpha.1)

This build implements Iron weight, Steel speed, Pewter strength and five separate Tin senses. The other twelve Feruchemical metals have no effects yet. Power acquisition remains administrative; crafting jewelry does not grant Feruchemy.

## Try it

1. In a cheats-enabled test world, use `/metallicarts grantferuchemy @s iron 1`. Repeat for `steel`, `pewter` and `tin` as needed.
2. Craft one ring, bracelet, bracer or earring of the matching metal at the Metallurgy Table (or obtain it from the creative tab). Hold **one** item, with an empty contact slot available.
3. Press **M**, then **F**. Choose a numbered slot and **Equip held**. For a new Tin mind, choose the sense before equipping; a registered mind keeps its original sense.
4. Choose a rate with **+ / -**, then click **Store**. You surrender that attribute while charge accumulates. Click **Off** to stop.
5. Select a tapping rate, then click **Tap**. Charge is spent before the benefit is applied. Click **Off** or unequip to stop. Changing the displayed rate alone does not change an already-running action; click Store/Tap again.
6. Press **W** to switch this menu between grid and wheel. **A** returns to Allomancy; **Esc** closes. In the Allomancy menu W also switches its presentation.

There are twelve contact slots. Slot names are a gameplay layout, not a canonical anatomy claim. Current jewelry may occupy any free contact slot. To unequip, make space in your ordinary inventory first; a full inventory refuses the transaction without dropping or duplicating the item.

## Costs and effects

Charge uses integer attribute units: 1,000 units represent one baseline attribute for one server tick. Storage rates are 25/50/75%; tapping rates are 25/50/100/200/400%. Total storage for one attribute is capped at 75%; total tapping at 400%, with additional limits for weak access grants. Opposite Store/Tap modes for the same attribute are refused. Separate attributes may run simultaneously.

High-rate tapping becomes inefficient: for aggregate throughput n, the cost is `ceil(n * max(1000,n) / 1000)`. Thus +100% costs 1,000 units/tick, +200% costs 4,000, and +400% costs 16,000. Splitting tapping among multiple minds does not bypass this cost. The last partially affordable tick grants only what its remaining charge pays for. Full or empty minds stop automatically.

Capacity is floor(mass in micrograms × purity), with technical bounds of 1 to 1,000,000,000,000. These units, rates and numerical effects are game policies. The item keeps its material/mass/purity and individual stored attribute.

- **Iron:** effective body mass starts at 80 kg and changes with stored/tapped weight. Mass affects reciprocal metal forces and incoming knockback; it does not rewrite vanilla gravity or pretend to be a full rigid-body simulation.
- **Steel:** changes movement, attack speed and the synchronized block-breaking speed attribute. Server and client use the same mining multiplier. Action multipliers are bounded.
- **Pewter:** changes melee damage, knockback, visible body scale, effective bulk/mass, mining and carrying capacity. Storing makes these weaker. Base carrying capacity is 40 kg, multiplied by `1 + strength delta`.
- **Tin:** distinct sight, hearing, touch, smell and taste assets. Sight changes visibility; hearing adjusts environmental sounds while respecting sound settings; touch changes hurt tilt and provides bodily/environmental cues; smell gives nearby animal/smoke cues; taste changes actual food/vial feedback. These are bounded Minecraft interpretations. Stored taste is muted; tapped taste includes nutrition/purity information. Strong senses can overload in bright/crowded conditions.

Modifiers have their own IDs. Turning Feruchemy off removes those modifiers without deleting other mods' modifiers or vanilla potion effects. Allomancy remains separately paid and controlled. Sensory cues appear on the HUD when menus are closed; these are initial accessible text cues, not a complete real-world sense simulation.

## Carrying and coins

Aim within three blocks and press **B** to lift/drop an eligible block, subject to strength and protection checks. Current explicit profiles: cobblestone 30 kg, copper block 100 kg, iron block 120 kg, gold block 160 kg, anvils 150 kg. Blocks with inventories/block entities are refused. The real source block is removed and rendered as overhead cargo, adding mass and exhaustion. Other Allomancers can act on eligible metallic cargo through the carrier. Losing capacity or contact with the carrier returns one block item. Cargo is a transient action, not permanent storage; reloaded cargo drops. Its display does not add a second solid collision box to the player.

Craft a **coin pouch** from five leather and one string (`L L / LSL /  L `). Fill/select it using vanilla bundle inventory controls; only coin forms are accepted. **C** tosses a real loose coin first, then the selected/first eligible pouch coin if none is loose. Pouch extraction retains item components. Normal capacity is the vanilla bundle weight budget (up to 64 ordinary coins; component-bearing unstackable coins may use more). Right-clicking the world does not dump the pouch.

## Ownership, saves and limitations

A canonical server-world ledger owns charge. Item components contain references/tooltips, never authority to mint charge. Transfer revisions invalidate stale copies; an asset can have only one contact lease. Copying an item does not duplicate usable charge. Registered assets are world-bound; importing a token into another world is refused.

The first fill keys an ordinary mind to the filler. Foreign keyed use is refused unless a matching implanted stolen Feruchemical source authorizes that donor's key; see HEMALURGY.md for the implemented exception and its limits. An unkeyed mind still requires the relevant Feruchemical ability. For testing, unequip and hold a registered mind, then use `/metallicarts unkeymetalmind @s`; this administrative operation retains charge. Normal Identity-free filling and unsealed power-granting equipment remain future Aluminum/Nicrosil systems.

Contact gear and charge survive death as an explicit game policy. Active modes do not survive death, respawn, dimension changes, logout or server/world reload. Legacy abstract invested charge is not silently converted into canonical spendable Feruchemical charge. Current crafting/trading guards protect registered metalminds and spikes; Compounding and charged-item reshaping are not implemented.

See the README roadmap for later attributes, Hemalurgy, Compounding and release work. See PHASE4_VALIDATION.md for recorded checks and remaining acceptance gaps.
