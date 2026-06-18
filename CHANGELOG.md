# Changelog — CarryOn

## [3.1.2] — 2026-06-19 — Minecraft 26.2

### Platform
- **Minecraft 26.2** — full port from 26.1.2
- NeoForge 26.2.0.0-beta
- Fabric 0.152.1+26.2
- Build: Gradle 9.5.1, Java 25

### Changed
- Entity rendering fully rewritten for MC 26.2's deferred render pipeline.  
  `MultiBufferSource` is gone in 26.2; all entity/block submission now goes through  
  `SubmitNodeCollector` / `EntityRenderDispatcher.submit()`.
- Block carry rendering updated: uses `ItemStackRenderState.submit()`.
- New render layer `CarryingItemRenderLayer` added to `AvatarRenderer` via mixin,  
  replacing the old immediate-mode player render hooks.
- `CarryOnData.getEntity()` updated for MC 26.2 `EntitySpawnRequest` API.
- `PlacementHandler` updated for MC 26.2 placement/interaction API changes.
- Fabric `ItemInHandRendererMixin` updated for MC 26.2 first-person render changes.
- Fabric `ScreenMixin` updated for MC 26.2 screen API.
- NeoForge `ClientEvents` updated for MC 26.2 client event changes.

### Fixed
- **Entity carry FPS lag (partial)**: The primary symptom — continuous UBO flooding  
  (Dynamic Transforms and Chunk Sections GPU buffers resizing every few seconds while  
  carrying an entity) — is resolved. Root cause: `EntityRenderDispatcher.submit()` was  
  called with the real world-space camera position, causing large coordinate transforms  
  to flood the deferred pipeline's uniform buffers. Fix: pass `(0.0, 0.0, 0.0)` as the  
  camera position, matching the pattern used by vanilla `GuiEntityRenderer`.
- **Per-frame entity NBT reconstruction**: The carried entity is now cached per-player  
  (`CachedRenderEntity` in `CarryRenderHelper`). The entity object is only rebuilt from  
  NBT when the carry data or level reference changes, eliminating the per-frame  
  deserialization overhead.
- **Unnecessary render state overhead**: After `extractEntity()`, the following fields  
  are now explicitly cleared on the carried entity's render state:
  - `shadowPieces` — prevents shadow blob geometry submission
  - `displayFireAnimation` — prevents fire flame geometry (and avoids billboard math  
    on the camera orientation)
  - `nameTag` / `scoreText` — prevents floating name-tag rendering above the player's  
    hands
  - `leashStates` — prevents leash rope geometry submission
- **Entity animation interpolation**: `extractEntity()` now receives the correct  
  `partialTicks` value instead of a hardcoded `0`, giving smooth sub-tick animation.
- **Removed `LevelRendererMixin`**: An earlier attempt captured the frame's  
  `CameraRenderState` via a `LevelRenderer` mixin; this is no longer needed now that  
  `(0, 0, 0)` camera is used.

### Known Issues
- **Entity carry FPS lag — base render cost (unverified)**: The UBO flooding symptom  
  is resolved and several sources of per-frame overhead have been eliminated. Whether  
  the base cost of inserting one entity into the world's deferred render pipeline still  
  causes perceptible lag needs in-game verification. In low-entity-count worlds the  
  relative overhead of one extra entity is larger than in dense worlds.

---

## [3.0.1] — 2026-05-xx — Minecraft 26.1.2

### Fixed
- Crash on world load: deferred `ItemStack` creation in `InventoryMixin` to avoid  
  premature class initialisation.

---

## [3.0.0] — 2026-05-xx — Minecraft 26.1.2

### Changed
- Initial port from Minecraft 1.21.x to MC 26.1.2 / NeoForge 26.1.2 / Fabric.
