# Candidate in-game acceptance checklist

Use a disposable world or a backup copy with the ordinary 1.0.0-rc.2 jar, Minecraft 26.1, Java 25 and the Fabric dependencies listed in README. Check the version on both server and clients. These are checks for a person to perform; they are not claims that remote/hardware acceptance has already happened.

## Preparation

Press M to inspect ownership and fuel. Press I for the three live controls pages; rebound keys replace the defaults below. Access is separate from fuel. Use normal acquisition or operator tools in the disposable world. For example, `/metallicarts fuel @s steel 1000000` supplies Steel fuel; `/give @s metallicarts:steel_coin 16` supplies actual coins. An account without the required ability can be granted it with `/metallicarts grant @s steel 1` or `/metallicarts grantferuchemy @s bendalloy 1`. Do not repeatedly grant abilities as a substitute for adding fuel. Substitute the desired standard metal ID for Steel.

Prepare separate Iron/Gold/Copper blocks, an attached mixed-metal group, Aluminum and Duralumin equipment, and a clear flat area. A second licensed player is needed for real multiplayer acceptance. F-Bendalloy additionally needs an actual contacted Bendalloy mind with stored charge; use the F menu to equip and fill it rather than assuming an item preview contains charge.

## Requested gameplay checks

1. **Coin lock:** burn Steel or Iron, press C, then apply R (Push) or V (Pull). The thrown coin should already be selected. After impact, its recovered item should retain selection until collected or otherwise unavailable long enough. Check that one throw consumes one real coin.
2. **Both metals:** burn both, select one source with G, and compare R, V, then both together. Push and Pull must remain explicit. Equal paid strengths cancel when both keys are held; unequal strengths apply their difference. H clears selection.
3. **Distance:** compare the same source and burn strength at close and farther distances. The farther impulse should be weaker. Keep equipment, mass and other powers unchanged during comparison.
4. **Discovery and groups:** turn away from sources and check rear/edge blue indicators. Face-connected mixed-metal blocks should produce one source/selection. Separate sources remain independent. Aluminum and its alloys should be immune; worn metal, charged metal and ordinary metal fixtures should remain eligible. Unknown third-party materials require tags as described in REQUESTED_GAMEPLAY.md.
5. **Selection grace:** select a fixed block, leave detection range for fewer than ten seconds, then return: the lock should recover. Remain outside for more than ten seconds and confirm expiry. There must be no out-of-range force. Do not change dimension, disconnect, die or explicitly clear during this check, since those deliberately end the session.
6. **Time fields:** view a Cadmium/Bendalloy field from outside, above and below: its outline must form a sphere. Have the other player enter and leave, including through the upper boundary. Compare movement, jumping, mining, attacks and item use. Their outside rates must return after exit. Repeat for each metal. Moving the caster outside ends that caster's field. Network/chat and general world time remain global.
7. **Stacks:** shape a 100-portion batch with space available: expect 64+36, never 100 in one slot. Test an old oversized-stack save only on a backup copy; total quantity must survive splitting, with surplus dropped if inventory space is exhausted.
8. **Controls:** open E and click Arts help. Rebind Push under vanilla Controls, reopen I and confirm the new key appears. Test the three pages at the smallest GUI viewport you use, scrolling and keyboard focus. Verify the force-key HUD while burning Iron/Steel.
9. **Nutrition:** in Survival with a partly empty hunger bar and a contacted charged Bendalloy mind, tap through F and watch hunger rise. Turn tapping off: the refill must stop. Fill and confirm hunger drains visibly, stopping at the protective minimum. Compare ordinary and higher rates; charge must be finite. Do not use Creative/Peaceful hunger behavior as the balance reference.

## Record acceptance

Record jar checksum, mod/dependency versions, server online-mode, player count, latency, operating system, graphics driver, keybindings and reproduction steps for each failure. Retain logs and screenshots. Repeat transfers, death, dimensions, save/restart and permissions with both remote accounts before declaring stable multiplayer acceptance. Follow REMAINING_WORK.md for hours-long capacity, survival balance, accessibility, localization and distribution gates.
