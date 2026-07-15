# Changelog — CarryOn

## [3.2.3] — 2026-07-15 — Minecraft 26.2

### Fixed
- **Server crash: `NullPointerException` in `CompoundTag.copy()` when a player interacts with a block while carrying** (`CarryOnData.java`):

  Root cause was a thread-safety race in `CarryOnData.getNbt()`. Fabric's
  `fabric:attachment_sync_v1` calls `getNbt()` on a **Netty IO thread** to serialize the
  attachment for client sync. The original implementation wrote directly into `this.nbt`
  (`putString("type", ...)`, `putBoolean("keyPressed", ...)`, `putInt("selected", ...)`)
  while the **server tick thread** simultaneously called `clone()` → `nbt.copy()`, which
  iterates the same `Object2ObjectOpenHashMap`. Concurrent write-from-Netty +
  read-from-server-thread on a non-thread-safe fastutil map corrupts the backing array
  (`this.wrapped == null`) → NPE that propagates from the Netty encoder and kills the server.

  Observed crash sequence (from MegumiHime's session, `crash-2026-07-15_13.32.29`):
  1. `ServerboundUseItemOnPacket` → `clone()` → NPE suppressed on server thread
  2. Same NPE in Netty IO threads #43 and #47 during attachment sync encode (fatal
     `EncoderException`, not catchable)
  3. NPE in `onCarryTick` → uncaught on server thread → server stops

  **Fix (`CarryOnData.getNbt()`):** write into a `nbt.copy()` snapshot instead of mutating
  `this.nbt`. The live backing store is now never written from the Netty IO thread;
  concurrent reads (server clone + Netty encode) are safe on fastutil maps.

  **Safety net (`CarryOnData.clone()`):** NPE guard added — returns an empty `CarryOnData`
  instead of propagating, so any remaining edge-case race drops the carry state (player
  appears to put down what they were carrying) rather than crashing the server.

---

## [3.2.2] — 2026-06-20 — Minecraft 26.2

### Fixed
- **`Pack declares support for version newer than 64, but is missing mandatory fields min_format and max_format`**
  (render-thread WARN on every launch, `pack.mcmeta`):
  MC 26.2 split pack format into independent client-resource and server-data epochs (resource major 88,
  data major 107) and deprecated the bare `pack_format` int for formats above the legacy threshold (64) —
  packs on the modern epoch must also declare `min_format`/`max_format`. Confirmed against vanilla's own
  bundled datapacks (e.g. `trade_rebalance`, which now ships `"max_format": 107, "min_format": [107, 1]`).
  Our `pack_format` was a stale `34` left over from a much older MC version. Updated to `88` (current
  resource-pack epoch, matching the render-thread/client resource loader that emitted the warning) with
  explicit `min_format`/`max_format: [88, 0]`. Warning was cosmetic (engine fell back successfully either
  way) but the declared format was genuinely out of date.

---

## [3.2.0 → 3.2.1] — 2026-06-20 — Minecraft 26.2 — Forge 65.0.0

### Platform
- Added **Forge** subproject targeting Forge 65.0.0 (MC 26.2). First working Forge build.
- Build: ForgeGradle 7 (`[7.0.17,8)`) via MinecraftForge maven in `pluginManagement`.

### Changed (build system — `Forge/build.gradle`, `settings.gradle`, `gradle.properties`)
- `settings.gradle`: added `"Forge"` to `include(...)` and MinecraftForge maven to `pluginManagement`.
- `gradle.properties`: added `forge_version=65.0.0`, `forge_loader_version_range=[65,)`, and
  `net.minecraftforge.gradle.merge-source-sets=true` (required for the multiloader Common pattern).
- `buildSrc/multiloader-common.gradle`: added `forge_loader_version_range` to `processResources` expand props.
- `Forge/build.gradle`: full rewrite for ForgeGradle 7:
  - New repository DSL: `minecraft.mavenizer(it)`, `maven fg.forgeMaven`, `maven fg.minecraftLibsMaven`.
  - New dependency DSL: `minecraft.dependency("net.minecraftforge:forge:...")`.
  - Removed `org.spongepowered.mixin` (MixinGradle 0.7-SNAPSHOT) — incompatible with Gradle 9.5.1
    (`org.gradle.util.VersionNumber` was removed); FG7 handles refmap generation internally.
  - Removed Mixin AP (`org.spongepowered:mixin:0.8.5-SNAPSHOT:processor`) — AP version 0.8.5 tries to
    generate SRG/notch obfuscation refmaps which don't exist for unobfuscated MC 26.2 and causes compile
    errors. FG7 MDK omits it. Mixin annotations are processed at runtime by Forge's loader.
  - Removed `jarJar.enable()` — FG7 dropped the old JarInJar API; switched to `compileOnly` for MixinExtras.
  - MixinExtras AP kept for `@ModifyExpressionValue` / `@WrapOperation` bootstrap.

### Fixed
- **`PlayerInteractEvent.EntityInteract` removed in Forge 65.0.0** (`CommonEvents.java`):
  `PlayerInteractEvent` is now a `sealed abstract class`; `EntityInteract` no longer exists.
  Replaced with `PlayerInteractEvent.EntityInteractSpecific` — fires on right-click with known hit vector.
  `getTarget()` still available on `EntityInteractSpecific`, so handler body is unchanged.
- **`ModList.get()` removed in Forge 65.0.0** (`ForgePlatformHelper.java`):
  `ModList` is now a fully static utility class (no singleton). `ModList.get().isLoaded(id)` →
  `ModList.isLoaded(id)`.
- **`mc.screen = null` removed in MC 26.2** (`ClientEvents.java`):
  Screen ownership moved from `Minecraft` to `Minecraft.gui`. Updated to `mc.gui.setScreen(null)`.

### Bugs / Known limitations
- `ForgePlatformHelper.java`: `PacketDistributor` API usage (`SERVER.noArg()`, `PLAYER.with(player)`)
  may need verification against Forge 65.0.0 network API at runtime.
- Capabilities: `CarryOnDataCapabilityProvider` not yet tested with Forge 65.0.0 caps pipeline.

---

## [3.1.2 → 3.1.7] — 2026-06-19 — Minecraft 26.2

### Platform
- Minecraft 26.2 — NeoForge 26.2.0.0-beta / Fabric 0.152.1+26.2
- Build: Gradle 9.5.1, Java 25

### Changed
- Entity rendering fully rewritten for MC 26.2's deferred render pipeline (`SubmitNodeCollector` replaces `MultiBufferSource`).
- Block carry rendering updated to use `ItemStackRenderState.submit()`.
- New render layer `CarryingItemRenderLayer` added to `AvatarRenderer` via mixin, replacing the old event-based player render hooks (NeoForge `RenderPlayerEvent`, Fabric equivalent).
- `CarryOnData.getEntity()` updated for MC 26.2 `EntitySpawnRequest` API (`EntityType.create()` now takes `EntitySpawnRequest` instead of a raw `EntitySpawnReason`).
- `PlacementHandler` updated for MC 26.2 placement and interaction API changes.
- Fabric `ItemInHandRendererMixin` target updated: `renderArmWithItem` renamed to `submitArmWithItem` in MC 26.2.
- Fabric `ScreenMixin` and NeoForge `ClientEvents` updated: `Minecraft#setScreen` moved to `Minecraft.gui#setScreen` in MC 26.2.

### Fixed
- **Entity carry invisible + GPU 100% / ~7 FPS** (all 26.2 builds before 3.1.7):

  Root cause: `EntityType.create()` builds the carry entity from NBT without world registration, so no entity ID is assigned. `LivingEntityRenderer.extractRenderState()` → `ItemModelResolver.updateForLiving()` → `entity.getId()` threw `IllegalStateException` every render frame, silently preventing any model submission. `IllegalStateException` construction calls `fillInStackTrace()` at ~60 Hz, which starved the render thread and caused the GPU to idle at 100%.

  Fixes (all active in 3.1.7):
  - `entity.setId(player.getId())` before `extractEntity()` — assigns a stable fake ID. Carried entities hold no items, so the exact value is cosmetically irrelevant.
  - Zero `yBodyRot`, `yBodyRotO`, `yHeadRotO` before `extractEntity()`. `LivingEntityRenderer.extractRenderState()` reads these via `solveBodyRot()` to set `state.bodyRot`; a non-zero value (the entity's world-facing when picked up) causes `setupRotations()` to rotate it away from the camera.
  - Entity position for shadow extraction uses `(playerPos.x, 64.0, playerPos.z)` instead of the player's actual Y. `extractShadow()` probes blocks near entity Y; an extreme Y (e.g. Y=6.4M in a test world) causes Sodium's Chunk Sections UBO to resize every frame. `shadowPieces` are cleared immediately after extraction regardless.
  - `renderer.submit()` called directly, bypassing `EntityRenderDispatcher.submit()`. `manager.submit()` translates the poseStack by `(state.x − cameraX)`; with camera at (0,0,0) and `state.x` at the player's world X (~60), this shifts the entity ~60 units from the position set by `setupEntityTransformations()`.

- **Per-frame entity NBT reconstruction** eliminated — carry entity now cached per-player (`CachedRenderEntity`), rebuilt only when carry data or level reference changes.

- **Unnecessary render state overhead** — after `extractEntity()`, `shadowPieces`, `displayFireAnimation`, `nameTag`, `scoreText`, and `leashStates` are explicitly cleared on the carried entity's render state.

- **Removed `LevelRendererMixin`** — earlier attempt to capture `CameraRenderState` via a `LevelRenderer` mixin; no longer needed.

---

## [3.0.1] — 2026-05-xx — Minecraft 26.1.2

### Fixed
- Crash on world load: deferred `ItemStack` creation in `InventoryMixin` to avoid premature class initialisation.

---

## [3.0.0] — 2026-05-xx — Minecraft 26.1.2

### Changed
- Initial port from Minecraft 1.21.x to MC 26.1.2 / NeoForge 26.1.2 / Fabric.
