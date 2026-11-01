# Documented account access — 0.7.0-alpha.2

When enabled, these two Minecraft accounts have baseline strength 1 access to all sixteen standard Allomantic and all sixteen standard Feruchemical metals, including all five Tin senses:

| Account label | Verified UUID |
|---|---|
| RashekKholin |18a05fe3-2c04-4639-9ded-18bbc18e8ba6|
| The17thShard |e889f7e1-3316-484d-a1e5-cab8b4d39d2b|

UUIDs were resolved on 2026-10-31 through Mojang's official profile API: [RashekKholin](https://api.mojang.com/users/profiles/minecraft/RashekKholin), [The17thShard](https://api.mojang.com/users/profiles/minecraft/The17thShard). No profile lookup is needed during play. UUIDs authorize access; names are labels, so renaming an account does not remove its access. A different account with a matching name receives nothing.

## Server policy and installation

Use the same 0.7.0-alpha.2 mod on the logical server and clients. This works in new and existing worlds when the server supplies those account UUIDs. Normal authenticated multiplayer and the matching integrated-server profile can supply them. The mod does not bypass a server owner's configuration or authentication policy. An offline-mode name alone cannot establish account ownership; there is deliberately no username fallback. The server owner can use ordinary grant commands for an intentionally managed offline setup.

In config/metallicarts-server.json:

~~~json
{
  "schema": 1,
  "allowPlayerHemalurgy": false,
  "snapshotCooldownTicks": 20,
  "enableAccountPowers": true
}
~~~

The setting defaults to true, including existing configs. A legacy config receives this field while retaining its other settings, with its original copied to metallicarts-server.before-account-powers.json once. Set enableAccountPowers to false and restart the server to disable the perk. No client switch can override this policy. Server startup logs its status; eligible players receive an explanatory join message; operator inspect reports account access.

## Exact behavior

- Supplies a normal strength1 baseline, not maximum-strength powers, infinite fuel, automatic burning or automatic metalmind charge.
- Stronger existing grants remain stronger. Distinct Hemalurgic Allomancy still stacks according to existing rules.
- Ordinary burning must consume ingested fuel. Feruchemy still needs a contacted canonical mind, matching Identity/access rules and paid storage/tapping. Nicrosil can temporarily suppress/store the access normally.
- The M ownership display and new Nicrosil type selector read server-provided eligible/effective access. Temporary unsealed additions do not become permanent raw-owned type choices.
- Does not grant operator status, commands, items, protection bypasses, Hemalurgic constructs or unimplemented god-metal powers.
- Is evaluated from the current profile UUID/policy, so it is available after world changes, reconnects, respawn or raw grant removal. It never changes the character's Identity or fills the 128 raw-grant slots.
- Does not mint an extractable canonical donor source. Real earned/admin/stolen grants remain independent and keep their existing provenance rules. Normally paid stored Nicrosil charge remains a real asset under the usual rules.
- Disabling the feature removes only account access; earned/admin/stolen grants, reserves, keys, equipment and already paid charge are untouched. There is no penalty, hidden code, corruption or artificial dependency that breaks unrelated systems when disabled.

Implementation is intentionally easy to inspect: domain/AccountPowers.java, arts/EffectivePower.java, config/ServerConfig.java and the private owner UI metadata. No save migration of grants is needed.

## Validation

See ACCOUNT_ACCESS_VALIDATION.md for observed results. Tests cover both UUIDs, all 32 standard art/metal combinations, unrelated/name-spoofed identities, stronger and Hemalurgic grants, no saved ownership mutation, legacy/disabled configuration, real paid burning and contacted Feruchemy, UI eligibility metadata, typed Nicrosil creation, and clean shutdown on policy removal. Synthetic profile fixtures test game integration; they are not proof of signing in to either real account.
