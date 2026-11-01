# Account access validation — 0.7.0-alpha.2

This update adds a public, default-on, server-configurable UUID-based access overlay. Historical Phase7 results remain in PHASE67_VALIDATION.md; they are not substituted for current checks.

Confirmed on 2026-10-31, Minecraft 26.1/Fabric/Java 25:

- 71 unit tests passed, including five new account/config tests.
- All 62 required server game tests passed, including the new integrated profile/account-permission fixture.
- Both official UUIDs were resolved through Mojang's profile service. Native fixtures deliberately use different display labels, verify every standard art/metal type, require real burn fuel, surrender real weight before storage credit, preserve exact charge when disabled, create a correctly typed nicrosilmind, and include eligible base access in owner UI metadata.
- An unrelated UUID using the name RashekKholin receives no perk. Offline name-derived UUIDs likewise fail eligibility. Stronger existing powers and Hemalurgic stacking are preserved.
- Existing server configuration migrated with enableAccountPowers=true and its original saved to metallicarts-server.before-account-powers.json; inspection confirmed existing policy/cooldown fields were retained.
- The perk never adds persistent raw grants, changes Identity, grants operator rights or creates extractable donor sources. Policy-off checks preserve earned grants and already paid assets without unrelated failures.

All six native client suites passed in the same sequential run (account-powers-native.log, BUILD SUCCESSFUL), including ownership display/type selection and existing world-transition, save/reload and Hemalurgy regressions. Native fixtures are synthetic profiles, not a sign-in to either real account. A real authenticated-login smoke check on the user's installation remains manual.

Final runDatagen + build passed (account-powers-package2.log), including a repeat run of all 62 server tests. The initial packaging rerun encountered previously leased equipment from the fixed-UUID fixture; test-only cleanup now releases those fixture contacts before and after the test. No production behavior changed after the full client pass. [Checksums](validation/account-access-sha256.txt).

Reproduce with JDK 25 and the wrapper:

```powershell
.\gradlew.bat build runClientGameTest -PacceptMinecraftEula=true
```

The EULA property requires agreement to https://aka.ms/MinecraftEULA. Run sequentially; do not rebuild resources while a client test loads worlds. Server policy lives in config/metallicarts-server.json; set enableAccountPowers=false and restart to test disabling. Use /metallicarts inspect PLAYER to view the public account entitlement separately from canonical grants. Account UUIDs and exact semantics are in ACCOUNT_ACCESS.md.
