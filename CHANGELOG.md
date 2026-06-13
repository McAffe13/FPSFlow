# Changelog
---

## [1.7.11]

### Fixed
- **Item flickering on texture-pack servers** — When a server sends a resource pack, Minecraft reloads all textures and re-bakes item models asynchronously. The map item-frame throttle (`ItemFrameEntityRendererMixin`) kept cancelling `updateRenderState` calls during this window, leaving frames holding a stale render state that referenced invalidated texture handles — causing visible flickering. A new `ResourcePackReloadTracker` registers as a Fabric resource reload listener and activates a 60-tick cooldown after every reload; the throttle skips itself while the cooldown is active so render states are always refreshed immediately after a pack change. `HUDCache` is also reset on reload, so the hotbar dirty-flag system re-evaluates every slot on the first post-reload frame.
- **Hotbar cache miss for `CustomModelData` items** — `itemStackHash` only covered item type, count, damage, and enchantment presence. Texture-pack servers commonly assign the `custom_model_data` component to items to drive custom model selection; two visually different items sharing the same base data but different `CustomModelData` values produced identical hashes, causing incorrect "nothing changed" cache hits. The hash now includes `CustomModelData`.

---

## [1.7.10]

### Fixed
- **Crash in ParticleManagerMixin on servers with many entities** — Minecraft 1.21.11 changed the value type of the internal `particles` map from `Queue<Particle>` to an internal collection class (`class_11940`) that does not implement `Queue`. The `@Shadow` field declaration and the per-tick particle count loop now use a raw `Map` with an `instanceof Collection<?>` check, which is compatible with both the old and new type.

---

## [1.7.9]

### Fixed
- **Crash with Voxy / extreme camera clipping** — When an entity's eye position coincided exactly with the camera position, `normalize()` produced a NaN direction vector, causing Minecraft's DDA raycast to freeze or crash the render thread. `rayCast()` now returns `false` immediately when the squared distance between endpoints is below 1 × 10⁻⁶, and adds a secondary NaN guard after each passable-block step.

---

## [1.7.8]

### Fixed
- **Update checker never detected new releases** — The Modrinth API URL contained unencoded `[`, `]`, and `"` characters (`loaders=["fabric"]`). Java's `URI.create()` rejects these with an `IllegalArgumentException` that was silently swallowed, so the HTTP request never executed. The URL now uses percent-encoded form (`loaders=%5B%22fabric%22%5D`).
- **Update notification could be missed on slow title screens** — The old implementation slept 10 seconds after the HTTP response, then checked `mc.player`. If the player hadn't joined a world yet, the notification was silently dropped. The notification is now stored and shown 3 seconds after the next world join via `ClientPlayConnectionEvents.JOIN`.

---

## [1.7.7]

### Fixed
- **Crash when occlusion ray and entity eye position coincide** — If a camera and an entity eye position were at the same point (degenerate geometry, e.g. some Voxy LOD entities or extreme camera clipping), `to.subtract(origin).normalize()` produced a NaN direction vector. Minecraft's DDA raycast then diverged into undefined behaviour, freezing or crashing the render thread. `rayCast()` now short-circuits immediately when the squared distance between endpoints is below 1 × 10⁻⁶, and adds a secondary NaN guard after each passable-block step.

---

## [1.7.6]

### Fixed
- **Entity type override cache now resets after a profile switch** — The per-entity-type culling override cache (`entityTypeOverrides`) was computed once and never invalidated. Switching profiles replaced the underlying map reference but left stale cached results for entity types that had already been seen, so the new profile's overrides were silently ignored until the game was restarted. The cache is now cleared automatically whenever the overrides map reference changes.
- **Config null-safety after manual JSON edits** — If a player manually edited `fpsflow.json` and set a section to `null` (e.g. `"entityCulling": null`), all reads from that section would throw a `NullPointerException`. All nested config objects are now replaced with their defaults if null after deserialization.
- **Config screen title and Done button now use the translation system** — The screen title was hardcoded as `"FPSFlow Settings"` and the Done button as `"Done"`, bypassing the translation files. Both now use `Text.translatable` so German (and any future) translations apply correctly.

### Changed
- **Dead code removed** — `WorldRendererMixin.java` was never registered in `fpsflow.mixins.json` and had no effect at runtime. The file has been removed to avoid confusion.

---

## [1.7.5]

### Fixed
- **Update checker now works for all current and future Minecraft versions** — The Modrinth API query previously used `&featured=true`, which caused new releases to be missed if they were not explicitly marked "featured" on the project page. An intermediate fix tried filtering by the exact running MC version, but that would have broken detection for newer MC versions (e.g. 26.1, 26.2) whose version strings do not match the old format. The query now fetches all Fabric releases with no version filter and compares only the FPSFlow version number, so updates are reliably detected regardless of which Minecraft version the mod is built for.

---

## [1.7.4]

### Fixed
- **Entities no longer disappear when moving around corners** — The occlusion cache stored whether an entity was hidden, but did not track where the camera was when that raycast was taken. After the player moved, the old "occluded" result stayed in the cache for up to 10 ticks (0.5 s), making previously-hidden entities invisible even when now in plain sight. The cache now records the camera position at raycast time; if the camera has moved more than 1.5 blocks since then, the result is immediately discarded.
- **Entities within 8 blocks are never occlusion-culled** — The previous safety margin was only 4 blocks. At 5–8 blocks the raycast is susceptible to precision errors (slab edges, floor-level geometry, crawling/swimming camera clips). Raising the threshold to 8 blocks eliminates false-positive culling at close range without any meaningful performance cost.
- **Stale "occluded" result no longer used for nearby entities while async re-check is pending** — When a cache entry expires and the new check is queued asynchronously, entities within 24 blocks now show as visible while waiting for the result. Previously the old "hidden" answer was used unconditionally, so rapidly-moving close entities could wink out for several frames.
- **Entities behind invisible blocks (barrier, light, structure void) are now visible** — The occlusion raycast treated these invisible blocks as solid, hiding any entity behind a barrier wall or inside a light-block setup. The raycast now steps past visually-passable blocks and retries, so only genuinely solid geometry counts as occlusion.

---

## [1.7.3]

### Fixed
- **Particle cap now applies to total live particles, not just new spawns per tick** — The `maxParticles` limit previously reset to zero every tick, allowing up to 4096 new particles to be added each tick regardless of how many were already alive. On high-particle servers (texture-pack servers, firework effects, etc.) this caused thousands of particles to accumulate and be ticked every frame. The cap is now seeded from the real live-particle count at the start of each tick, so the budget is correctly enforced.
- **Per-particle spawn overhead eliminated** — The adaptive distance multiplier (derived from the smoothed FPS chain) was previously recomputed on every individual particle spawn attempt. It is now cached once per tick, reducing overhead proportional to the server's particle spawn rate.

---

## [1.7.2]

### Fixed
- **Crash on `NO_RENDER` particle tick eliminated** — Particles of type `NO_RENDER` (internal server-side particles) can have a null `SpriteProvider`. In MC 1.21.11 the resulting `NullPointerException` escaped the `safeParticleForEach` guard because the restructured particle-tick pipeline (`method_74282` → `class_11938`) caused it to propagate past the narrowly-typed `catch (NullPointerException)`. The catch now covers `Exception` broadly, and `e.toString()` is logged instead of `e.getMessage()` so exceptions with no message text are still reported correctly.

---

## [1.7.1]

### Fixed
- **Reproducible builds** — Gradle no longer embeds per-build timestamps inside the JAR. The same source code now always produces byte-for-byte identical output, so the SHA512 hash is stable across builds. This allows the Modrinth launcher to reliably identify locally-built JARs and track playtime correctly.

---

## [1.7.0]

### Fixed
- **Entity flicker in hubs and lobbies eliminated** — Entity LOD now has a single distance threshold (Balanced: 96 b) instead of two zones. Entities within that distance always render every frame; beyond it they render every other tick. The previous two-zone system started throttling as close as 40 blocks, causing clearly visible flicker on any lobby NPC or minecart. The threshold is now well beyond typical hub visibility range, so flicker is gone in practice.
- **Server-NPC and cosmetic entity flicker eliminated** — Entities whose nametag is set always-visible by the server (`isCustomNameVisible`) are now fully exempt from **both** Entity LOD throttling **and** entity culling (occlusion + distance). This covers plugin NPCs, display minecarts, cosmetic vehicles, and any other entity a server marks as permanently visible.
- **Nameplate LOD exemption widened** — Any entity within the configured nameplate range is now exempt from LOD even when nameplate culling is globally disabled.
- **Small entity occlusion false-positives reduced** — The occlusion raycast now targets the entity's **eye position** instead of the geometric AABB centre, preventing floor-level false positives for small entities (floating mob heads, armor stands, item displays).

### Added
- **Menu/loading FPS cap** — When the Minecraft window is focused but no world is loaded (title screen, loading screen, server selection), the frame rate is now capped (default 120 FPS). This stops the GPU from spinning at thousands of FPS on a static menu, freeing thermal headroom for faster texture and world loading. Configurable via the Background FPS tab in the ModMenu config screen.
- **Singleplayer Boost** — New toggle in the config screen (General tab). When enabled, the adaptive renderer uses more aggressive culling multipliers in singleplayer to free CPU for chunk generation threads.
- **Nameplate distance slider** — The nameplate culling range is now adjustable directly in the ModMenu config screen (LOD & Labels tab, 8–128 blocks).
- **Tooltips on every config button** — Each toggle and slider in the ModMenu config screen now shows a short description on hover.

---

## [1.6.2]

### Changed
- **ModMenu config screen redesigned as tabbed UI** — the settings screen is now split into four clearly labelled tabs so all options are easy to find without scrolling:
  - **General** — profile cycling, save custom profile, update checker, join optimizer, GUI optimizer, particle optimizer
  - **Culling** — entity culling, block entity culling, occlusion culling, async occlusion, painting backface culling
  - **LOD & Labels** — entity LOD toggle, nameplate culling toggle, medium/far LOD distance sliders, map frame throttle
  - **Background FPS** — background FPS toggle, unfocused FPS cap slider, minimized FPS cap slider

### Fixed
- **Nameplate flicker caused by Entity LOD throttling** — The Entity LOD system skips rendering distant entities every 2nd or 3rd tick to reduce GPU load. Because `EntityRenderer.hasLabel()` is only called during an entity's render pass, a throttled frame also skipped the nameplate, causing it to flash on/off at the LOD rate. FPSFlow now exempts any entity that is within the configured nameplate culling distance from LOD throttling, so the label is always rendered when it should be visible.
- **Redundant camera distance recalculation removed** — `EntityLabelMixin` previously re-computed the camera-to-entity squared distance instead of using the value already calculated and passed in by `EntityRenderer.hasLabel`. The mixin now uses the provided parameter directly, eliminating a redundant Vec3d allocation per label per frame.
- **Hysteresis dead-band widened from 15 % to 20 %** — Entities hovering just outside the nameplate range now need to move further away before their label hides, giving an additional buffer against brief visibility changes near the boundary.
- **Dead server-override bookkeeping removed** — `NameplateCullingManager` previously maintained a `SERVER_FORCED_VISIBILITY` map that was never populated (no mixin called `markServerForcedVisibility`). The map was always empty, so its checks were no-ops. The code has been removed; the active server-override protection (`isCustomNameVisible()` bypass) is unchanged.

---

## [1.6.1]

### Added
- **ModMenu config screen updated** — all features introduced in 1.6.0 are now accessible without editing JSON:
  - **Nameplate Culling** toggle (was previously missing from the screen)
  - **Background FPS Limit** toggle
  - **Unfocused FPS cap** slider (0–480 FPS)
  - **Minimized FPS cap** slider (0–480 FPS)
  - **Painting Backface Culling** toggle
- **Unlimited FPS cap** — setting either Background FPS cap to `0` (leftmost slider position, or `"unfocusedFpsCap": 0` / `"minimizedFpsCap": 0` in the config JSON) disables the frame rate limit for that state entirely. The slider label shows `Unlimited` when at `0`.

### Fixed
- `GameRendererMixin` parameter signature corrected for MC 1.21.11 — the `render()` method now takes a `RenderTickCounter` instead of `(float tickDelta, long startTime)`, which previously caused a crash on launch.

---

## [1.6.0]

### Fixed
- **NPC nameplate flickering** — Entities whose nameplate visibility has been set to always-on by the server (`isCustomNameVisible = true`, typical for plugin NPC systems) are now exempted from distance culling entirely. Previously the mod fought against the server metadata update on every view-distance change, causing persistent flickering.
- **Hysteresis reset every 200 ticks removed** — The periodic full cache wipe in `NameplateCullingManager` was resetting the hysteresis dead-band, causing a brief nameplate flicker roughly every 10 seconds for entities near the culling threshold. `WeakHashMap` garbage-collects despawned entity entries automatically, so the manual wipe was never needed.

### Added
- **Background FPS limiter** — When the Minecraft window loses focus the frame rate is capped at a configurable limit (default: 60 FPS unfocused, 30 FPS minimised) via a post-frame sleep on the render thread. Inspired by Dynamic FPS. Caps are profile-aware: Quality 30/10, Balanced 60/30, Performance 10/3, Ultra Performance 5/2. Toggle via `backgroundFps.enabled` in the config.
- **Painting back-face culling** — Paintings are never visible from behind, so the renderer now skips them entirely when the camera is on the back side. Uses a dot-product normal check against the painting's facing direction. Controlled by `entityCulling.paintingBackfaceCulling` (default: `true`).
- **Tiered particle density LOD** — Instead of a hard allow/block cutoff there are now three distance zones: near (< `midDistance`): 100 % of particles; mid (`midDistance` → `maxDistance`): ~50 % via a stable position-based hash — no per-tick flickering; far (> `maxDistance`): 0 %. The new `midDistance` value is configurable in all four built-in profiles and custom profiles.
- **FPS-adaptive LOD tightening** — When the adaptive renderer detects low FPS (< 30 FPS: 0.7× multiplier, < 15 FPS: 0.5×), entity LOD distance thresholds shrink automatically. At 20 FPS the Balanced thresholds (40/80 blocks) behave like 28/56 blocks without requiring a manual profile switch.

---

## [1.5.2]

### Fixed
- **Game crash from stale entity reference in particles** — During rapid world/minigame transitions, `NO_RENDER` particles could crash with a `NullPointerException` (`$$10 is null`) when the entity they referenced was removed before their first tick. FPSFlow now catches this in `ParticleManager.tick()`, preventing the crash and logging a rate-limited warning (at most once every 5 seconds).

---

## [1.5.1]

### Fixed
- **Server-forced nameplate visibility** — nameplate culling now respects when a server forces nametag visibility via game rules or plugins
  - Added detection for `showDeathMessages` and nameplate override packets
  - Nameplate culling gracefully backs off when the server mandates visibility, avoiding visual conflicts
  - Prevents nametag flickering caused by client/server visibility disagreement

### Changed
- Nameplate culling is now more robust in multiplayer scenarios with varied server configurations
- Server-side nameplate settings are now honored alongside client-side distance culling
- Entity LOD distances are now configurable, so medium/far render skip thresholds can be tuned per profile or saved custom profile
- Default Balanced Entity LOD distances widened to 40 / 80 blocks for smoother distant entity rendering

---

## [1.5.0]

### Fixed
- **Preserve player rendering in PvP views** — other players are no longer subject to entity LOD throttling, so F5 and multiplayer combat remain crisp.
- **Avoid duplicate async occlusion checks** — the entity culling queue now deduplicates requests and limits backlog growth, reducing spikey workload when many entities are near the visibility threshold.
- **Faster entity-type override lookups** — entity type override checks are cached to avoid repeated string allocations during render traversal.

### Added
- **Low-FPS adaptive culling** — entity and block entity culling distances are reduced automatically when FPS drops, so busy scenes become smoother.

### Changed
- `mod_version` bumped to `1.5.0`.

### Notes
- These changes target intermittent hangs and render stalls, especially during view changes and busy entity scenes.

## [1.4.0]

### Added
- **Custom profiles** — create your own performance presets and switch between built-in and saved custom profiles using the in-game profile button.
- **Profile persistence** — selected custom profiles now load automatically at startup.
- **Custom profile management** — both built-in and saved profile state are preserved across sessions.

### Changed
- **Config workflow improved** — profile selection and custom preset behavior now work more reliably in the in-game config screen.

## [1.3.1]


### Fixed
- **Nameplate flicker at distance** – name tags no longer rapidly blink on/off when an entity hovers near the culling threshold
  - Visibility decisions are now cached per entity and re-evaluated only every N ticks (`checkIntervalTicks`, default 5)
  - A 15 % hysteresis dead-band is applied around `maxDistance`: a visible tag only disappears when the entity moves clearly *beyond* the threshold, and a hidden tag only reappears when the entity moves clearly *inside* it
  - Combined effect: smooth transitions with no per-frame toggling

### Changed
- `nameplateCulling` config section gains a new field `checkIntervalTicks` (default `5`)
  - Balanced: 6 ticks, Performance: 4 ticks, Ultra Performance: 3 ticks
  - Existing configs without this field default to 5 ticks automatically

---

## [1.3.0]

### Added
- **Entity LOD (Level of Detail)** – distant entities are render-throttled to reduce GPU load without culling them entirely
  - Medium LOD (>32 b on Balanced): entity renders every 2nd tick
  - Far LOD (>64 b on Balanced): entity renders every 3rd tick
  - XOR-based distribution ensures throttling is staggered across entities (no mass-freeze on a single tick)
  - Configurable distances per profile; toggle in the in-game config screen
- **Nameplate Culling** – entity name tags are hidden beyond a configurable distance (default 32 b on Balanced)
  - Applies to all entities, including players, mobs, and armor stands
  - Toggle in the in-game config screen
- **Map Item Frame Throttle** – item frames displaying maps update their render state only every N ticks instead of every frame
  - Default: every 3–5 ticks depending on profile (every frame on Quality)
  - Initial render always runs; throttle kicks in after the first map texture is loaded
  - Toggle in the in-game config screen
- **HUD dirty-flag feedback loop** – the HUD throttle now force-enables itself for one tick whenever a player stat actually changes (health, food, armor, XP, air)
  - Previously the throttle ran purely on tick parity; now it always catches real changes immediately
  - Net effect: no more delayed health/hunger display when hit or eating

### Changed
- Performance profiles updated with values for all three new features
- Quality profile: Entity LOD enabled with generous distances; Nameplate Culling and Map Frame Throttle disabled
- Balanced/Performance/Ultra Performance: all three features enabled with progressively tighter settings

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
