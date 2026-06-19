# Changelog — CarryOn

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
