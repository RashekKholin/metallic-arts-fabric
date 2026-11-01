# RC3 targeting, bubbles, emotions and interface

## Metal sight and selection

Metal rays use camera-local coordinates and the camera's current field of view. A source in view has a blue ray and a diamond endpoint. Near endpoints are larger/brighter; far endpoints are smaller/dimmer. Selected sources are cyan. Distance labels identify the closest sources and selections. Sources behind the camera use explicitly labelled bottom compass markers rather than mirrored rays into the sky; other off-screen sources use edge markers. These are sensory indicators through terrain, not a claim of ordinary unobstructed visibility.

Grouping is strictly **six face neighbors**. Edge-only/corner-only diagonal contacts never connect. A chain of face-connected blocks can still join two otherwise diagonal blocks through its intermediate blocks. Discovery remains loaded-only, with the documented bounded network/group budgets.

Press **N in the world**, or **Auto metals** in M, to toggle automatic selection of discovered sources. It refreshes as you move, up to 64 retained locks; ordinary discovery defaults to 48 sources in 24 blocks and is host-configurable. Sources share the same finite force budget, so selecting more does not multiply strength. Manual G selection retains its eight-lock limit outside automatic mode. H clears selection and disables automatic mode. N inside equipment menus still selects typed Nicrosil powers. No client-supplied source list is trusted.

## Particle time bubbles

**Bendalloy has a four-block radius; Cadmium has an eight-block radius**, bounded by the host's existing maximumRadius cap. Strength changes the clock rate, not this radius. The old baseRadius field remains readable for configuration compatibility but no longer sets either radius.

Actual world-space dust particles form a distributed spherical shell: warm gold for Bendalloy, blue for Cadmium. There are no HUD wireframe latitude/longitude lines. Clients generate the shell from public server geometry, keeping particle positions depth-correct and avoiding hundreds of individual particle network packets. Local particle preferences and vanilla particle settings still apply. Emission has a total client budget rather than unbounded cost per overlapping bubble.

Supported local clocks still affect every eligible player inside, including the caster, and restore outside rates on exit. The caster and visitors use the same transient modifiers; the old duplicate caster movement multipliers have been removed. Global chat/network/world time, general redstone and unsupported systems remain outside this contract.

## Bulk operator commands

`/metallicarts grantall <player>` grants all sixteen standard Allomantic and all sixteen standard Feruchemical powers at strength one where needed. Existing sources/stronger abilities remain intact. Repeating it does not accumulate duplicate grants.

`/metallicarts fuelall <player> <micrograms>` adds that amount of ordinary fuel **to each of the sixteen Allomantic reserves**. For example, `/metallicarts fuelall @s 1000000` adds one gram per metal. It does not fill metalminds or mint canonical charge. Both commands require operator permission. Bulk fuel changes are committed only if the complete immutable update succeeds.

## Five-second amplification

Duralumin starts a **100-server-tick window**. Eligible active metals surrender their remaining fuel once; their paid amplified effect lasts until that window ends. Duralumin continues consuming its normal fuel during the window and turns off automatically at its end. Starting another eligible burn during that window adds its paid amplification to the remaining window, rather than resetting the deadline. Duralumin alone also times out. Inactive reserves remain untouched. Nicrosil-forced amplification uses a finite paid window too; it does not invent Duralumin fuel.

Death, disconnect, dimension replacement, Aluminum/Chromium depletion, manual cancellation, lost ownership and suppression cannot preserve free cached powers. Ending Duralumin early cancels its remaining burst without a refund. The same finite policy is applied to supported NPC burners. Five seconds means 100 server ticks; a lagging server may take longer in wall time.

## Independent Zinc and Brass

M has separate **Zinc** and **Brass** emotion selectors, both visible on the same page. Zinc raises its chosen emotion; Brass lowers its own chosen emotion. Selecting different emotions no longer cancels them. Choosing the same emotion still combines opposing paid strengths. Influence reaches eligible nearby minds within the configured range (default twelve blocks), with line of sight, resistance, protection, Copper and player permission checks. Discovery processes the nearest 64 candidates per caster.

Ordinary hostile and passive mobs are eligible; the emotion_immune entity tag excludes bosses by default and supports host extensions. At sufficient influence:

- Soothing **anger** calms attacks against the influencer until that player attacks the mob. A real provocation breaks that protection.
- Soothing **trust** creates distrust and hostility toward the influencer, even for a normally passive creature. A temporary AI goal approaches and attacks with an ordinary cooldown; passive creatures use a bounded two-damage melee fallback.
- Rioting anger encourages hostility; rioting trust discourages attacking the influencer. Calm takes precedence while anger remains sufficiently suppressed.
- Fear retains threat avoidance and determination retains its bounded resistance behavior. Traits accumulate and recover gradually rather than pretending a single tick rewrites a mind.

Players receive the existing bounded trait effects and awareness feedback, not forced client input. Boss brains, every third-party custom AI and universal mind control are not certified.

## One-page interface

M shows all sixteen metals together, their A/F access, fuel and burn/flare state. Wide screens use the radial constellation; W or the layout button switches to a grid. Narrow screens use a complete four-by-four grid automatically. Full names and narration remain available. The world HUD shows the owned-metal constellation, active/flare colors and automatic-selection state; M supplies full labels and controls. I/inventory Arts help remains the live keybinding reference.

These exact timings, sizes, UI choices and mob responses are requested gameplay interpretations, not new assertions about unpublished canon. RC2 validation is archived in RC2_VALIDATION.md; current results belong in RELEASE_VALIDATION.md.
