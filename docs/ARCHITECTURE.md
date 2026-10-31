# Phase 1 architecture

This section records the original foundation. The Phase 2 additions are described below and in [the economy guide](ECONOMY.md).

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

## Phase 2 economy

`economy` owns registered forms/items/ores, brewing conversion, server-validated ingestion, composition metadata and villager/worldgen registration. `metallurgy` owns the persistent container, menu and atomic processing plans. The client renders server-synchronized slots/data and ghost previews rather than interpreting an independently loaded recipe catalog.

The table preflights output copies before changing any input. A completion replaces inputs and outputs together on the server thread. Its saved data includes inventory, recipe choice, fuel, progress and run/batch flags. Vanilla block-entity removal drops inventory; changing specialty retains the same block entity.

Metallurgy recipe arrays reload on the server. Common material tags allow compatible ingots/nuggets/raw materials. The trusted server/data-pack author defines recipes; clients can only click current-menu buttons, with range and button-ID validation.

Two small brewing mixins recognize metal flakes and perform the conversion at the normal Brewing Stand completion point. Vanilla owns the fuel, timer, cancellation, container syncing and persistence. The conversion consumes exactly one flake per converted water bottle and preserves unused input. A merchant-offer guard refuses invested or nonviable alloy inputs.

Spiritweb schema 1 gained an optional bounded `invested_reserves` field. Old saves decode it as an empty list. Those records describe invested subsets of the total reserve mass, not additional fuel. The server rejects unsafe/full reserves and duplicate invested instance ingestion. Do not downgrade after using new content.

Invested slicing produces one nonstackable flake and replaces the residual source item atomically. Proportional charge uses integer arithmetic with an exact remainder retained on the source. Partial-mass equipment cannot pass a normal full-mass reshaping recipe. Non-invested commodity stack metadata can represent a batch; global creative copying and arbitrary third-party item duplication are not claimed to be prevented.

Resources and original pixel art are reproducibly generated by `scripts/generate-economy.cjs`. Text/data resources are committed individually; original PNG textures are versioned together in `src/main/texture-assets.zip`. Gradle expands that archive into the mod, so normal builds need only Java. The ignored per-texture working copies can be regenerated for inspection. The Fabric datagen provider pack remains an empty extension point; the Node generator is the current resource-authoring tool.
# Phase 3 development additions

The historical foundation below is extended by `allomancy/ReserveLedger`, `BurnBudget` and `BurnEngine`. The pure engine returns paid powers alongside the updated immutable spiritweb; the server runtime commits fuel before applying handlers. Ordinary fuel excludes the invested subset. The existing schema-1 save shape is retained with stricter cross-field decode validation.

`AllomancyRuntime` owns transient sessions and a single server update loop. Player object/world replacement and disconnect clear its own attribute modifiers and burn modes. Mode requests and held-force inputs carry only bounded intent; ownership, fuel, aim, strength, targets and recoil are server decisions. Status updates contain only mode/feedback bits and go to their owner; full private snapshots remain owner-only and at most twice per second during burning.

`MetalPhysics` uses loaded block probes and a range-limited entity query. The first implementation supports one aimed external target and explicit block/item tags. `CoinProjectile` carries one real component-bearing stack. Effective physics mass and ingestible credited mass are deliberately different scales.

The client uses Fabric 26.1 HUD extraction, normal screen buttons and rebindable held keys. Tin hooks lightmap extraction at all returns and environmental sound volume calculation; it never writes vanilla settings or potion effects. Client-only mixins are listed in a separate client mixin configuration.

See [ALLOMANCY.md](ALLOMANCY.md) for exact implemented behavior and unfinished scope.
