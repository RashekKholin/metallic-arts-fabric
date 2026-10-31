# Phase 2 player guide

## First iron vial

1. Craft a **Metallurgy Table**: three iron ingots across the top, three cobblestone in the middle, and three planks along the bottom.
2. Open it and put coal or another furnace fuel in the **Fuel** slot.
3. Put an iron ingot into an input slot. Select **Shape**, use the arrow buttons until the ghost recipe shows iron nuggets, then press **Start/Stop**.
4. Turn one iron nugget into **100 Iron Portions**. The two stacks in the outputs are 99 + 1.
5. Move a portion to the inputs. Select the flakes recipe and process it into **Iron Flakes**.
6. Put water bottles, iron flakes, and blaze powder in a normal **Brewing Stand**. It takes the usual 400 ticks. One flake fills one bottle; two fill two; three fill three. Unused flakes and unfilled bottles remain.
7. Drink the resulting **Iron Vial**. The bottle returns. Press **M** to see **0.01g** of ingested iron.

Ingesting metal does not grant powers or activate an effect. The powers themselves begin in Phase 3.

You can also start with raw iron: **Refine** raw iron into iron concentrate, then refine the concentrate into an ingot. Vanilla smelting remains available.

## Using the table

The four slots on the left are inputs; the four on the right are outputs. Hover the ghost item between the arrows to see the selected recipe's requirements. The ghost cannot be taken in survival.

**Refine** prepares concentrates and refined materials. **Alloy** processes the entire measured-portion batch in the input slots. **Shape** converts metal forms. Insert the ingredients before selecting a recipe. The arrows cycle recipes that use those ingredient types.

**Start/Stop** controls processing. **Batch** repeats the selected operation while ingredients, fuel and output space permit. Full outputs pause the job without consuming more ingredients or fuel. Inventory changes are committed together on the server. Changing tab/recipe resets progress. The table keeps its inventory, fuel, selected operation and in-progress job across saving and restart.

One ingot = **9 nuggets = 900 portions**. One nugget = **100 portions**. One portion or flake represents **0.01g** of credited metal. One coin = 10 portions; a ring, bracelet, bracer or spike = one ingot before any invested flaking. These are gameplay accounting units, not measurements of real Minecraft item weights.

## Obtaining metals

New Overworld chunks contain tin, zinc, silver, bauxite, nickel, chromite, lead and bismuth ores, each with a deepslate variant. Use at least a stone pickaxe. Silk Touch and Fortune apply. Existing chunks are not retroactively changed.

Refine raw ores into concentrates, then into ingots. Zinc concentrate yields **8 zinc nuggets and 1 cadmium nugget**. Bauxite supplies aluminum; chromite supplies chromium. Coal/charcoal supplies carbon, and sand supplies silicon through the table. Recovery yields and silicon/carbon processing are gameplay abstractions.

All sixteen standard metals have portions and flakes. Silver, nickel, lead, bismuth, carbon and silicon support the alloy economy but cannot be brewed as Allomantic fuel.

## Alloys

Use measured **portions**, not ingots or nuggets, in the Alloy tab. The default formulas total 100:

| Alloy | Portion ratio |
| --- | --- |
| Steel | 98 iron, 2 carbon |
| Pewter | 91 tin, 9 lead |
| Brass | 65 copper, 35 zinc |
| Bronze | 88 copper, 12 tin |
| Electrum | 45 gold, 55 silver |
| Duralumin | 96 aluminum, 4 copper |
| Nicrosil | 84 nickel, 14 chromium, 2 silicon |
| Bendalloy | 50 bismuth, 27 lead, 13 tin, 10 cadmium |

These ratios are explicit canon-informed gameplay interpretations, not claims that every number has been established by the sources reviewed for this project.

Correctly proportioned batches produce **viable** alloy. Small deviations produce **imperfect** alloy; larger deviations produce **invalid alloy residue**. The composition and credited mass remain recorded. Imperfect/invalid materials cannot be laundered through shaping into viable fuel. Brewing/drinking refuses unsafe material rather than inventing injury values.

## Metallurgists

Sneak-right-click an empty-handed Metallurgy Table to cycle its job-site specialty:

| Specialty | Stock |
| --- | --- |
| Common metals | Iron, tin, copper, zinc, lead and carbon |
| Alloys | The eight alloys |
| Rare metals | Aluminum, chromium, cadmium, gold, silver, nickel, bismuth and silicon |
| Equipment | Flakes, bottles, coins and uncharged equipment |

Let an unemployed adult villager claim the table during working hours. A villager whose trades are already locked keeps its profession; changing a workstation is not intended to reroll a traded villager.

Each specialty progresses from Novice to Master using vanilla villager XP, restocking, finite uses and reputation/demand pricing. Every trade in a level's set is offered, avoiding a reroll lottery for material access. Master trades include bulk offers. Charged/Identity-bearing inputs and nonviable alloys are refused by merchants.

## Invested material support

Phase 2 creates only **uncharged** rings, bracelets, bracers and spikes. Storage, tapping, spike extraction and power grants remain later systems.

If an item already carries invested test/mod data, the table can shave one measured flake at a time. It deducts the flake's mass and proportional charge from the source, keeps its Identity, and gives the resulting invested flake a separate instance. Output-space checks occur before the source changes. A partially shaved item cannot be reshaped into a full-mass ingot.

Invested flakes remain nonstackable, retain charge/Identity in brewing, and retain those records when ingested. These records describe a subset of ingested reserves, not a second quantity of fuel. They do not grant access to a metalmind or implement Compounding. Duplicate invested instance ingestion is refused.

## Data packs and compatibility

The bundled catalog is `data/metallicarts/metallurgy/catalog.json`. Server resource reloads accept additional JSON arrays under any namespace's `metallurgy/` directory. IDs must be unique. Invalid catalogs are rejected as a unit while the previous valid catalog is retained; startup without a valid catalog fails visibly.

Recipe inputs accept an `item` or a `tag`, with a count. Built-in ingot, nugget and raw-material recipes use common `c:` tags for interoperability. Outputs name specific registered items. Tags treat a compatible ingot/nugget as the same credited unit. Server administrators are responsible for the balance of added recipes; conservation tests cover the bundled recipes.

Ore size, frequency, height and stone/deepslate targets live in normal configured/placed-feature JSON files and can be overridden by data packs. Trade sets and villager trades use Minecraft 26.1's data-driven registries.

Optional mixed-metal vials are not implemented.
