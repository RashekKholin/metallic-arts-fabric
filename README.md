# The Metallic Arts

Minecraft Java **26.1** · Fabric · Java **25** · mod ID `metallicarts` · **1.0.0-rc.3**

## Install and play — start here

**You need Minecraft Java Edition 26.1, Fabric Loader, and TWO mod files.** Use the versions below; this release targets **26.1**, not Bedrock Edition or another Minecraft version.

**Download these two files:**

| Required file | Download |
|---|---|
| The Metallic Arts — playable mod | **[metallic-arts-fabric-1.0.0-rc.3.jar](https://github.com/RashekKholin/metallic-arts-fabric/releases/download/v1.0.0-rc.3/metallic-arts-fabric-1.0.0-rc.3.jar)** |
| Fabric API — required dependency | **[fabric-api-0.145.1+26.1.jar](https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/0.145.1%2B26.1/fabric-api-0.145.1%2B26.1.jar)** |

**Download the ordinary mod jar above. Do not install the `-sources.jar`, Source code ZIP, or Source code TAR files. Leave both downloaded jars as they are; do not extract or double-click them.** The [release page](https://github.com/RashekKholin/metallic-arts-fabric/releases/tag/v1.0.0-rc.3) also provides [SHA-256 checksums](https://github.com/RashekKholin/metallic-arts-fabric/releases/download/v1.0.0-rc.3/SHA256SUMS).

### Windows: official Minecraft Launcher

1. **Prepare Minecraft 26.1.** In the Minecraft Launcher, launch vanilla **26.1** once if you have not already. Then close Minecraft and the launcher.
2. **Install Fabric Loader.** Download and run the **Windows installer** from the [official Fabric download page](https://fabricmc.net/use/installer/). Choose **Client**, Minecraft **26.1**, and Loader **0.18.4** (the tested version; the mod requires 0.18.4 or newer). Leave **Create profile** checked and click **Install**. Fabric also provides an [illustrated installation guide](https://docs.fabricmc.net/players/installing-fabric/windows).
3. **Find your game folder.** For the default Windows installation, press **Windows + R**, enter `%APPDATA%\.minecraft`, and press Enter. Open the `mods` folder, or create a folder named exactly `mods` if it is missing. If your launcher/profile uses a custom game folder, use that instance's `mods` folder instead.
4. **Copy BOTH jars into `mods`.** Download them using the table above. Your folder should contain these two files (other compatible mods may also be present):

   ```text
   mods/
     metallic-arts-fabric-1.0.0-rc.3.jar
     fabric-api-0.145.1+26.1.jar
   ```

5. **Launch the Fabric profile.** Reopen the Minecraft Launcher, select the **Fabric 26.1** profile, and click **Play**. The game must use **Java 25**. If your launcher reports an older Java version, install [Eclipse Temurin Java 25](https://adoptium.net/temurin/releases?version=25&os=windows&arch=x64) and select its `bin/javaw.exe` under **Installations → your Fabric profile → Edit → More Options → Java Executable**.
6. **Open the mod in game.** Create or join a world, then press **M** for the metals menu and **Guide**, or **I** for controls. The inventory also has an **Arts help** tab. Read the [in-game acceptance checklist](docs/MANUAL_ACCEPTANCE.md) to check your installation.

### Other launchers, operating systems, and servers

- **Another launcher or Linux/macOS:** create a **Minecraft 26.1 + Fabric Loader 0.18.4+** instance using **Java 25**. Open that instance's game folder and place the same two jars in its `mods` folder. Follow [Fabric's installation guides](https://wiki.fabricmc.net/install) for your launcher/system; the default Windows folder above may not apply.
- **Dedicated server:** use the [official Fabric server setup](https://fabricmc.net/use/server/) for Minecraft **26.1**, Loader **0.18.4+**, and Java **25**. Stop the server, place both jars in the server's `mods` folder, then start it following Fabric's instructions. **Every connecting player must also install the same Metallic Arts version and the required Fabric API.**
- **Updating an existing installation:** stop the game/server and back up the **whole world and configs** first. Remove the previous Metallic Arts jar before adding RC3; keep only one Metallic Arts jar installed. Update clients and server together, test a copy of the world first, and do not downgrade the original world in place. See [backup and recovery instructions](docs/OPERATIONS.md).

A playable unofficial Mistborn fan mod: sixteen standard Allomantic metals, sixteen Feruchemical metals with five separate Tin senses, metallurgy, source-tracked Hemalurgy, compounding, unsealed equipment, player and NPC constructs, survival discovery and researched god-metal interpretations. **Release candidate, pending the acceptance checks below.** This does not implement every unpublished canonical interaction.

**License: All rights reserved.** The [LICENSE](LICENSE) permits official downloads, gameplay, server use, configuration and local backups. It does not grant general modification or redistribution rights. Third-party rights remain separate. Read [attribution](docs/ATTRIBUTION_REVIEW.md) and [contribution policy](CONTRIBUTING.md).

Versioned releases include sources and SHA-256 checksums. CI artifacts may expire and require sign-in. See [actual validation](docs/RELEASE_VALIDATION.md), including precise platform, multiplayer and migration limits, and the [in-game acceptance checklist](docs/MANUAL_ACCEPTANCE.md) to check a downloaded candidate yourself.

## Your first survival session

Craft a **Metallurgy Table**: three iron ingots, three cobblestone and three planks in horizontal rows. Supply furnace fuel, refine ores and alloy exact compositions; use **Shape** for equipment, portions and flakes. A nugget makes 100 portions; a portion makes a flake. The table shows ingredients, remaining work and batch/start/stop controls.

Brew flakes using water bottles and blaze powder in a normal Brewing Stand. Drinking provides fuel, **not standard-metal ownership**. Ore and rare research camps generate in new Overworld chunks. Camps contain a finite loot chest, a workstation and a Terris researcher; they do not overwrite existing containers. Craft a field guide from a book and copper nugget, or use M → Guide. Advancements lead through fuel, equipment, mixed vials, medallions and god-metal discovery.

Each character has a once-only latent allocation: default independent 50% chances of one Allomantic and one Feruchemical metal. Survive an unblocked hit of at least six damage with six health or less to awaken latent Allomancy. At XP level five, `/metallicarts-survival discover` reveals Feruchemy. Some characters have neither Art. Death/rejoining/extraction cannot reroll powers. Hosts can adjust these gameplay choices.

Powered hostile populations use finite actual fuel and owned powers. NPC Feruchemists surrender attributes while resting and spend canonical charge in combat; their physical rings can be recovered on death. Rare Koloss, Inquisitors and Kandra are distinct entities with real implanted dependencies. Their generated initial powers represent authored NPC backstories, never extraction from an unseen real player. Four metallurgist professions trade ordinary materials; merchants cannot mint unlimited Investiture.

## Controls

| Action | Use |
|---|---|
| **M** | Ownership, reserves, burn/flare, Guide |
| **I / inventory Arts help tab** | Live keybindings, targeting, storage/tapping and time-field explanations |
| **F** within an Arts menu | Contact gear, store/tap, rate, Tin sense, typed Nicrosil |
| **H** within an Arts menu | Hemalurgy anatomy, source-aware insert/remove and explicit donor consent |
| **K** | Recorded history and selected coppermind records |
| **P** | Compounding, prepared spikes, constructs/forms, bounded compulsion and extraction |
| **R / V / G / H** in the world | Push / Pull / toggle selection / clear; both force keys oppose and equal strength cancels |
| **N** in the world | Toggle automatic selection of discovered metal sources (H clears/disables) |
| **C / B / J** | Toss and auto-lock coins / supported cargo / paid prediction opening |
| **W**, arrows and other menu shortcuts | Contextual grid/wheel/navigation; separately rebindable |
| **Esc** | Close/back |

M → Guide has eleven pages and 32 per-metal lessons. Press I or use the inventory Arts help tab for three narrated controls pages showing your actual rebound keys. A compact force-key HUD appears while Steel/Iron is burning. **Sensory settings** control Tin brightness/sound, overload indicators, Bronze sound, mod particles, cues and high contrast. These affect presentation, never ownership or payment. Mod-only sound/particle filtering preserves unrelated vanilla effects. The guide wraps, scrolls and narrates its text. English is the complete reference locale; Spanish coverage is explicitly partial pending translation review.

See [RC3 refinements](docs/RC3_REFINEMENTS.md) for stable depth-labelled sight, six-face grouping, 4/8-block particle bubbles, independent Zinc/Brass, five-second Duralumin, the all-metal radial/grid UI, and `/metallicarts grantall PLAYER` / `/metallicarts fuelall PLAYER MICROGRAMS`.

## Main workflows

- **Feruchemy:** equip an actual contacted mind, own its matching ability, then store at an actual attribute cost or tap finite charge. Identity and typed content matter. Preview numbers never create charge.
- **Hemalurgy:** use intentional, material-compatible extraction and prepared placement. Donor sources, decay and ownership remain canonical. Player extraction is default-off and needs host permission/consent/protection checks. Missing Inquisitor linchpins are lethal; test on disposable worlds.
- **Compounding:** own both matching Arts, hold a charged free source, wear a compatible destination with space, select its slot in P and enable Intent. The whole source burns once. Copper restores existing records once. This is an explicit whole-source gameplay interface.
- **Constructs:** P prepares genuine spikes and transformations. Koloss needs four strength staples; Inquisitors need their eye/linchpin dependencies; Kandra needs mistwraith lineage and a linked innate Blessing. Wolf/slime forms cost real materials. Sneak-feed a functioning NPC Kandra bones/leather or slime to teach those bodies; empty-handed interaction restores human form.
- **Mixed vials:** hold two ordinary filled vials in opposite hands; `/metallicarts-survival mix` combines up to sixteen batches and returns an empty bottle. All reserve checks finish before consumption.
- **Medallions:** `/metallicarts-survival bind` joins two free charged unsealed typed Nicrosil cores held in opposite hands; add a third similarly. `/metallicarts-survival medallion 0` equips them from UI slot one. Each core keeps its independent charge/lease. This carrier is an adaptation, not a claim to reproduce the unpublished Excisor process.
- **Memory sharing:** the recipient opts in with `/metallicarts-survival sharememories`. In the memory menu choose sharing and a record while aiming at that nearby recipient. The exact record moves once, preserving provenance; the recipient must be within three blocks with line of sight and protection permission. `/metallicarts-survival pastvision` separately opts into Malatium observations.
- **Factions:** trade, gifts and injury affect bounded persistent Terris/Empire/village/independent standing. `/metallicarts-survival factions` shows your standing. Aim at an eligible member and use `tribute` to pay one Gold/four Steel ingots for a gift; twenty-second cooldown. Standing influences prices and some hostility, never account authentication or claim permissions.
- **God metals:** crouch-use a Lerasium bead for standard Allomancy, or a standard alloy bead for that metal. Pure Atium, Atium/Electrum (Nalatium) and Atium/Gold (Malatium) have distinct finite prepaid windows. Nalatium requires Electrum access; Malatium requires Gold access as documented interpretations. Predictions are uncertain nearby trajectories, not guaranteed futures or fabricated biographies.
- **God-metal crafting:** hold one Atium bead and an ordinary Gold/Electrum ingot offhand; `/metallicarts-survival atiumalloy` makes the supported alloy. `split` uses one Harmonium sample, a retained Trellium catalyst, eight blaze powder, inventory space and a dry permitted Metallurgy Table underfoot. The host's default-off Lerasium acquisition policy must permit it. Wet dropped Harmonium reacts without destroying terrain; ownerless reactions cannot anonymously damage entities.

See [all eleven requested gameplay fixes and exact limits](docs/REQUESTED_GAMEPLAY.md) and [release-candidate mechanics](docs/RELEASE_CANDIDATE.md), [economy](docs/ECONOMY.md), [Allomancy](docs/ALLOMANCY.md), [Feruchemy](docs/FERUCHEMY.md), [Hemalurgy](docs/HEMALURGY.md), [advanced Arts](docs/ADVANCED_ARTS.md) and [advanced systems](docs/ADVANCED_SYSTEMS.md). Earlier phase notes describe their historical scope; the current release notes and validation supersede their old status statements.

## Account access and server policy

**RashekKholin and The17thShard have access to all standard Allomancy/Feruchemy**, matched by verified UUIDs. They still pay normal fuel, charge, contact, Identity and suppression costs. Hosts can set `enableAccountPowers: false` in `metallicarts-server.json` and restart. This public perk grants no operator rights and has no hidden dependency/tamper penalty. [Exact policy](docs/ACCOUNT_ACCESS.md).

Configuration files: `metallicarts-server.json`, `metallicarts-allomancy.json`, `metallicarts-advanced.json`, `metallicarts-progression.json`, **`metallicarts-world.json`** and client-only `metallicarts-client.json`. Restart after host edits. Invalid values fail visibly. World policy controls research camps, Harmonium reactions, construct rarity, NPC session/scan budgets and god-metal duration. Open Parties and Claims integration is optional and fails closed on API errors; only the pinned tested version pair is documented as verified. Other claim mods need their own adapter.

Back up player data and world ledgers together. Operators can use `/metallicarts assets PLAYER audit|repairrefs|recoverleases`. Recovery restores only references for assets already canonically leased to that UUID and empty slot; it never invents charge, free items or foreign ownership. See [operations](docs/OPERATIONS.md).

## Build and test

Use PowerShell 7 (`pwsh`) for the portable recovery/migration scripts. The wrapper pins Gradle **9.4.0**, Loom **1.15.5**, Minecraft **26.1**, Loader **0.18.4** and API **0.145.1+26.1**. Use JDK **25**. Native tests require accepting Minecraft's EULA; this repository does not accept it for ordinary users automatically.

```powershell
.\gradlew.bat clean build -x runGameTest --console=plain
.\gradlew.bat runDatagen
# Native server + ten graphical client suites, if you accept the EULA:
.\gradlew.bat runGameTest runClientGameTest -PacceptMinecraftEula=true --console=plain
# Windows: two ordinary offline-mode loopback clients, port 25570 free:
.\scripts\verify-multiplayer.ps1 -AcceptMinecraftEula -SoakMinutes 15
# Isolated orderly reload, deliberate partial-save crash and canonical recovery:
.\scripts\verify-save-recovery.ps1 -AcceptMinecraftEula
# PowerShell 7; full Git history required; actual older-source-generated worlds:
.\scripts\verify-legacy-migration.ps1 -AcceptMinecraftEula
# Optional exact third-party fixture; no jars are bundled:
.\scripts\setup-claim-fixture.ps1 -Destination build/claims-fixture
.\gradlew.bat runGameTest -PclaimsIntegrationTest=true -PclaimsModsDir=build/claims-fixture -PacceptMinecraftEula=true
# Development:
.\gradlew.bat runClient
.\gradlew.bat runServer
```

On Linux/macOS use `./gradlew` and PowerShell 7 (`pwsh`) for portable fixture scripts. Build output: `build/libs/metallic-arts-fabric-1.0.0-rc.3.jar`. Reports: `build/reports/tests`; graphical screenshots: `build/run/clientGameTest/screenshots`; isolated fixtures retain logs/results in their timestamped directories. Do not rebuild classes/resources while a test JVM is using them.

Asset/data regeneration order: `node scripts/generate-economy.cjs`, `node scripts/generate-advanced.cjs`, `node scripts/generate-release.cjs`, `node scripts/localize-ui.cjs`. All art is original deterministic pixel art; no Minecraft textures are copied. [Canon provenance](docs/CANON.md) separates author evidence from intuitive interpretations and balance policy.

## Acceptance and future plans

See the [ordered remaining work](docs/REMAINING_WORK.md) for concrete acceptance tasks and future expansion, and [current validation](docs/RELEASE_VALIDATION.md) for results. In particular, a local two-client fixture does not prove authenticated remote play, a short profile does not certify hours of load, and CI cannot supply real macOS graphical/user acceptance or third-party intellectual-property clearance. Those gates remain explicit before a stable 1.0 declaration or eventual Modrinth submission. The source-license choice is now resolved: **All rights reserved**.
