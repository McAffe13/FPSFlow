# Changelog
---

## [1.2.0]

### Added
- **World Join Optimizer** – detects when the player joins a world (singleplayer or multiplayer) and temporarily tightens entity, block-entity, and particle culling distances for a configurable grace period
  - Starts at 35% of normal cull distances and eases linearly back to 100% over the grace period
  - Async occlusion raycast batch increases 3× during grace period to drain the backlog faster
  - Grace period: 200 ticks (10 s) on Quality/Balanced, 160 t (8 s) on Performance, 120 t (6 s) on Ultra Performance
  - Toggle in the in-game config screen under "Join Optimizer"
- `worldJoinOptimizer` section added to `fpsflow.json` with `enabled` and `gracePeriodTicks` fields

---

## [1.1.0]

### Added
- **Block Entity Culling** – skips rendering distant block entities (chests, furnaces, signs, banners, item frames, armor stands); configurable max distance per profile
- **In-game Config Screen** – accessible via ModMenu; toggle all features and cycle between profiles without editing JSON
- **ModMenu Integration** – when ModMenu is installed, FPSFlow appears in the mod list with a settings button
- **Async Occlusion Culling** – occlusion raycasts are now spread across multiple game ticks (up to 8 per tick) to eliminate frame spikes when many entities need rechecking simultaneously
- **Per-Entity-Type Overrides** – add entries to `entityCulling.entityTypeOverrides` in `fpsflow.json` to always-cull or never-cull specific entity types (e.g. `{"minecraft:armor_stand": false}`)

### Changed
- Performance profiles now also configure block entity culling distance
- `asyncOcclusion` field added to `entityCulling` config section (default: `true`)
- `blockEntityCulling` section added to config with `enabled` and `maxDistance` fields

### Notes
- ModMenu is optional — the mod works without it; the config screen is simply unavailable
- Block entity culling is distance-only (no occlusion raycast); occlusion on block entities is planned for a future release

---

## [1.0.0]

### Added
- **Entity Culling** with frustum, occlusion (raycast-based), and distance culling
- **Particle Optimization** – count cap and distance-based spawn filtering
- **Adaptive Renderer** – FPS-aware dynamic culling level adjustments
- **GUI Optimizer** – hotbar slot dirty-flag tracking, HUD update throttling
- **Smart Render Scheduler** – smoothed FPS monitoring via exponential moving average
- **Performance Profiles** – Quality, Balanced, Performance, Ultra Performance presets
- **Compatibility detection** for EntityCulling, Sodium, ImmediatelyFast, Lithium, FerriteCore, ModernFix
- **Modrinth Update Checker** – async check on startup, in-game chat notification
- **Localisation** – English (en_us) and German (de_de) included
- JSON-based configuration at `config/fpsflow.json`

### Notes
- Entity occlusion culling is disabled automatically when the EntityCulling mod is detected
- HUD caching is disabled automatically when ImmediatelyFast is detected
- The adaptive renderer temporarily tightens particle and entity culling when FPS falls below 30 or 15

---
