# Phase 1 architecture

`MetallicArts` is the physical common initializer for both integrated and dedicated servers. `MetallicArtsClient` lives only in the client source set. A separate dedicated-server initializer is unnecessary: common initialization registers the same data and payloads for both logical server types.

| Package | Responsibility |
| --- | --- |
| domain | Immutable serialized contracts and stable metal IDs |
| registry | Application metal catalog, persistent player attachment, item component registration |
| config | Server-host policy loading and validation |
| network | Versioned read-only request and owner snapshot |
| rules | Bundled canonical evidence and explicit interpretations |
| client | Snapshot presentation, key mapping and screen |
| datagen (client source set) | Empty provider pack for future assets/data |
| test / gametest | Codec contracts, server attachment integration, client/server/save validation |

## Data contracts

All saves use explicit schema `1`. Stable metal strings, not Java enum ordinals, appear in saves. Unknown metal IDs and enum names fail decoding. Add migrations deliberately before changing existing fields or IDs; never silently treat unknown grants as no power.

`Spiritweb.identity` is a gameplay Identity token, separate from a Minecraft account UUID. Each fresh character receives a new token. Grants retain their own ID, art, metal, origin, source UUID and positive finite strength. For innate grants, the source convention is the Identity token; for Hemalurgic grants it is the spike instance ID. Unsealed/admin origins are reserved contracts only. Innate non-metal attributes and anatomical modifications will require additional typed contracts in later phases.

Spiritweb values and collections are immutable. Call `setAttached` with a replacement value for every change so Fabric can save it. `getAttachedOrCreate` is called on join to ensure a persistent default. Copy-on-death is configured. Grants are capped at 128; reserves are limited to 1,000,000,000 micrograms per metal. These are technical bounds, not canon or final balance. Missing map entries mean zero reserve. No active burn/tap state is saved or implemented.

`MetalItemData` keeps a unique item instance UUID and optional Identity independently. Mass is positive micrograms; purity is a finite 0–1 fraction. Charge is an abstract nonnegative integer until the attribute engine defines units. A component is not an entire metalmind system: capacity, attribute subdivisions, spike donor history, contact checks, stack/split/copy protection and access validation are deferred. Future charged equipment should be nonstackable and enforce server-side transfer invariants.

## Network boundary

`request_spiritweb_v1` accepts one protocol integer (1), with no target or desired state. Fabric play handlers execute on the game thread. Server cooldown state is removed on disconnect. Unsupported request versions are ignored. `spiritweb_snapshot_v1` serializes a bounded immutable snapshot, sent only to its owner on join or request. It intentionally does not broadcast source/Identity information to nearby players.

The screen is a cache of server data. Future mutation payloads must carry intent only and check ownership, resources, range, cooldowns and permissions on the logical server. Future state mutations must explicitly call the synchronization service or introduce a revisioned update mechanism. No delta sync, capability negotiation across different schema versions, or prediction is claimed in Phase 1.

## Extensions

Add economy/material items before power effects. Add Allomancy/Feruchemy engines as separate server services over these contracts. Add Hemalurgy source changes through a validated transaction layer; `withoutSource` alone does not simulate insertion, removal, damage or survival. Datagen should add real providers alongside the content they generate.
