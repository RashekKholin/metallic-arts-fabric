> Historical phase guide: current 1.0.0-rc.1 additions and limits are in [RELEASE_CANDIDATE.md](RELEASE_CANDIDATE.md), with observed results in [RELEASE_VALIDATION.md](RELEASE_VALIDATION.md). Earlier future/deferred statements below describe that phase, not the current release.

# Phase 6–7 playable alpha guide

Version **0.7.0-alpha.2**, Minecraft 26.1/Fabric/Java 25. These are bounded Minecraft implementations. Numerical values are gameplay policies, recorded in the provenance catalog. Physical Arts and the economy remain available.

## Setup and controls

Survival discovery now follows the once-only allocation in [ADVANCED_SYSTEMS.md](ADVANCED_SYSTEMS.md). Unawakened ordinary characters have no active innate powers; the documented [account-access perk](ACCOUNT_ACCESS.md) is a configurable exception. Use operator development grants, e.g. `/metallicarts grant @s bronze 1` and `/metallicarts grantferuchemy @s gold 1`; use tab completion to confirm command arguments. Drink ordinary corresponding vials for fuel. Drinking never grants powers. Legacy invested fuel remains segregated from ordinary burns; the deliberate compounding path consumes canonical physical metalminds, not these previews.

M opens Allomancy. < / > or [ / ] switch Physical, Mental, Enhancement and Temporal groups. Click a metal: Off → Burn → Flare. W switches grid/wheel. F opens contact equipment; H opens spikes; Esc closes. Key bindings are editable in Minecraft Controls. Screen shortcuts are listed below.

## Allomancy

| Metals | Implemented behavior |
|---|---|
| Zinc / Brass | Select anger/fear/trust/determination on Mental page; aim within12 blocks to riot/soothe intensity. Influences combine and recover naturally, changing supported AI/attributes. High fear flees an existing threat; trust can clear aggression toward the caster. Copper immunity, PvP/Creative protection, fortitude, determination and Identity resistance apply. No unrestricted mind control. |
| Copper / Bronze | Copper conceals nearby paid Allomantic pulses and protects its smoker emotionally. Bronze samples every10 ticks and gives metal signatures/distance/private particles. Stronger Bronze pierces weaker clouds. Bronze base range15 blocks/cap32; Copper radius9/cap18. Brief pulses between samples may be missed; no general Investiture sensing. |
| Aluminum | Clears all internal metal reserves, including invested subsets, and stops burning. Never deletes ownership or Identity. |
| Duralumin | One amplified frame for concurrently paid eligible metals; consumes those metals and Duralumin, preserving inactive reserves. Boost1+sqrt(ordinary micrograms/1000), capped8; resulting power capped32. Physical safety caps remain. |
| Chromium / Nicrosil | Aim at an actual player within2.5 blocks: leech internal fuel / force next eligible paid burst. One contact action per second; PvP, Creative and attack-protection checks apply. Supported paid NPC burners and natural profiles are documented in ADVANCED_SYSTEMS.md. Ownership is preserved. |
| Gold | K opens actual recorded routes. While burning, select a route for a nearby alternate-path echo and its real recorded location. Explore new regions to record entries. No invented biography, parallel inventory or alternate world. |
| Electrum | Bounded movement branches, actual incoming-projectile cues and uncertainty. J follows a collision-checked opening when danger exists, costs1000ordinary micrograms, and has a cooldown. Adds movement, not invulnerability. Cognitive speed supports reaction assistance. No guaranteed futures/atium resolution. |
| Cadmium / Bendalloy | Anchored slow/fast bubbles; walk beyond anchor radius to collapse. Base radius6 blocks/cap8, rates0.25x/3x at strength 1, multiplicative overlaps clamped0.25..4. Changes non-player entity clocks and supported machine clocks; player movement/attack/mining multipliers synchronize. Projectile rate boundaries deflect velocity five degrees. |

Supported machines: furnace, blast furnace, smoker, Brewing Stand, Metallurgy Table. **Global-clock exceptions:** vehicles, player body/hunger/potions, arbitrary block entities, scheduled/random block ticks and general redstone. No time travel. Other mods require compatibility checks/adapters.

## Feruchemy

Hold one ring/bracelet/bracer/earring, M→F, choose an empty contact slot and Equip held. Every item has separate canonical charge. New Tin minds use the sense selector; new nicrosilminds use N to choose a specific raw-owned A/F ability before equipping. Existing minds keep their stored type. K opens the selected coppermind. Store/Off/Tap applies to other attributes; W switches grid/wheel.

Storage25/50/75%; Identity additionally100% at rate4. Tap25/50/100/200/400%.100% is1000 units/tick. Combined tap cost is `ceil(total × max(1000,total)/1000)`, paid incrementally so split items cannot evade inefficiency. Separate Nicrosil types have independent channels. Server ledger owns charge, keys and contact.

| Attribute | Real storing cost / paid tapping benefit |
|---|---|
| Bronze / wakefulness | Fatigue increase and immediate drowsy movement cost; tapping reduces fatigue/rest counter. Awake+1000/tick, sleep−500000/tick, max96000000. No offline accrual. |
| Brass / warmth | Stored body temperature decreases; tapping warms.5microdegreesC/unit. Climate, rain, water and fire influence temperature. Cold/hot extremes impair movement and damage health; sufficient warming reduces freezing. Range20–45°C/default36.5°C. No broad fire immunity. |
| Zinc / mental speed | Slower/faster deliberate attack/mining actions, aimed health analysis and Electrum reaction assistance. Does not accelerate the entire simulation. |
| Copper / memories | K: accessible records left, stored right. Click to move/restore exact entry;1000 units/entry, max256/pool. Stored records leave mod journal/history. Cannot erase player knowledge, recipes, advancements or arbitrary other-mod data. |
| Gold / health | Actual uncancelled health/absorption loss before credit; temporary lower max health is an additional storage disadvantage.20000 units/health point. Paid ordinary tapping heals missing health. Full-health armed Gold wastes no charge. Poison/Wither cure50000 units; ordinary lethal recovery200000 units, up to4 health,1200tick cooldown. Vanilla Totem acts first; bypass-invulnerability damage is not blocked. Ordinary extraction refuses an armed Gold donor before changes. No anatomical injury simulation. |
| Bendalloy / nutrition | Actual food/saturation surrendered before credit,400000 units/food point, floor1 food. Tap restores food/saturation plus bounded8000000 unit extra bank. No benefit/debit above cap or free recycling while storing. |
| Cadmium / breath | Actual air prepaid in whole points with persistent fractional escrow;1000 units/air point, store floor10 air. Tap restores missing air only. Supports vanilla underwater respiration; gas/space mods need adapters. |
| Electrum / determination | Storing causes apathy/action/movement/emotional-resistance costs; tapping improves emotional resistance. Separate from strength/healing. |
| Aluminum / Identity | Full paid suppression fills empty pools unkeyed and lowers emotional resistance. Partial suppression remains keyed. Cannot erase charged foreign keys. Tapping improves resistance, not arbitrary impersonation. |
| Duralumin / Connection | Reduced/increased supported villager trade rapport, affected by real regional familiarity and villager trust. Changes only owned price deltas, preserving vanilla reputation/discounts. Refreshes open offers on shutdown. No arbitrary factions or universal translation. |
| Nicrosil / Investiture | A specific selected raw-owned A/F type is suppressed while storing; paid tapping adds that typed access temporarily. Stopping/removing its physical source revokes access without rewriting grants. Types are not generic energy. |
| Chromium / Fortune | Lower/higher vanilla Luck and bounded directional opportunities while positive. At most512loaded positions, using `metallicarts:fortune_opportunities`. Only loot systems consulting Luck are affected. No guaranteed success/destiny/omniscient scan. |

## Unkeyed filling and deliberate unsealed construction

1. Own F-Aluminum and the desired Feruchemy. Equip empty Aluminummind and Store100% (rate4), keeping it paid/active.
2. Fill another empty mind under full suppression: its pool is unkeyed. A normal unkeyed mind still needs matching ability. Charged foreign keys cannot be replaced.
3. Own F-Nicrosil and the ability to store. On N select that specific type; equip fresh nicrosilmind and Store under full paid Identity suppression. It receives typed unkeyed charge.
4. Keep full Identity suppression and Tap charged Connection. Unequip the typed nicrosilmind, hold it in main hand and an ordinary viable Duralumin ring in offhand. In F press U. The binder is consumed and exactly that canonical item becomes deliberately unsealed.
5. An ordinary recipient contacts and Taps it to spend charge for one temporary ability. An accompanying attribute mind must separately be contacted, charged and key-authorized. This creates no free charge, permanent grant or multi-power medallion.

**Provenance:** construction method/binder/rates are intuitive game adaptations, not a verified canonical Excisor mechanism. Full medallion production/research remains future work.

## Persistence and upgrades

Back up the whole world: player data and SavedData ledgers must stay together. Old assets decode optional content as empty. Old four-metal configuration gains twelve default rates while preserving custom rates and making a one-time backup. Never downgrade a used world.

Body escrow persists through logout/restart without offline accrual, and resets on death with physical physiology. Journal/emotions/region familiarity and canonical equipment persist under existing death policy. No active action resumes automatically on reconnect/death/dimension replacement. Memory previews contain counts/type labels; private text is owner-only and paginated.

Native stolen cognition, Connection/Identity, Fortune and Investiture now support analysis, rapport/donor-key authorization, Luck/opportunity cues and scaling existing access. Full constructs and unrestricted Spiritual effects remain deferred. See [remaining work](REMAINING_WORK.md).
