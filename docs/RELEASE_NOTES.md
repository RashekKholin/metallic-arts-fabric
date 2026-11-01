# The Metallic Arts 1.0.0-rc.3

Minecraft **26.1**, Java **25**, Fabric Loader **0.18.4+**, Fabric API **0.145.1+26.1**. Stop the game/server, back up the whole world/configs, remove the old mod jar and install the ordinary jar on both sides. Sources are for development.

- Stable camera-local blue sight: in-view rays, distance-sized diamonds, distance labels and explicit rear/off-screen markers. No mirrored rear rays into the sky.
- Metal grouping is six face neighbors only; diagonal contacts do not join a source. N / Auto metals toggles automatic discovered-source selection. H clears and disables it. Shared force budgets and scan limits remain.
- Real particle sphere shells replace drawn wireframes. Bendalloy radius is four blocks; Cadmium is eight, subject to the host cap. All supported players/local clocks retain entry/exit behavior; caster movement no longer receives duplicate time multipliers.
- `/metallicarts grantall PLAYER` grants every standard Allomantic/Feruchemical power idempotently. `/metallicarts fuelall PLAYER MICROGRAMS` adds that amount to each Allomantic fuel reserve. Both require operator permission; fuel does not fill metalminds.
- Duralumin amplification lasts 100 ticks. Other active eligible fuel pays once; Duralumin burns normally during that window and switches off automatically. Late burns use the remaining window. Supported NPC burners share the finite rule.
- Independent Zinc/Brass emotion selectors influence protected nearby eligible minds. Soothing anger calms attacks until provoked; soothing trust creates distrust/hostility, including a temporary finite melee response in ordinary passive mobs. Copper, range/line of sight, resistance and permission checks remain. Boss exceptions are explicit.
- All sixteen metals appear together in M: wide radial constellation or complete narrow grid, plus an owned-metal world HUD. I / inventory Arts help explains current bindings and behavior.

Read [exact behavior and limits](https://github.com/RashekKholin/metallic-arts-fabric/blob/main/docs/RC3_REFINEMENTS.md), [current verification](https://github.com/RashekKholin/metallic-arts-fabric/blob/main/docs/RELEASE_VALIDATION.md) and the [README](https://github.com/RashekKholin/metallic-arts-fabric#readme). RC2 evidence is archived separately. This is an immutable release candidate under All rights reserved; remote authenticated acceptance, hours-long survival/capacity balance, complete Spanish/accessibility/hardware review and distribution permissions remain separate gates. No Modrinth submission has been made.
