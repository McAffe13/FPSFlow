# FPSFlow

**Real FPS improvements — no placebo, no bloat.**

FPSFlow is a client-side Fabric optimization mod that targets the biggest rendering bottlenecks in vanilla Minecraft: entities, block entities, particles, and the HUD. Every feature is measurably effective, individually toggleable, and automatically adjusts to your hardware via four built-in performance profiles.

---

## ✦ Features

### Entity Culling
The single biggest FPS win in entity-heavy scenes.

- **Frustum culling** — entities outside the camera frustum are never submitted to the renderer
- **Occlusion culling** — a cached raycast check skips entities hidden behind solid blocks; the result is reused for several ticks so the raycast cost is amortized
- **Async occlusion** — raycasts are spread across multiple game ticks (up to 8/tick) to eliminate frame spikes when many entities need rechecking simultaneously
- **Distance culling** — a configurable maximum render distance cuts entities beyond that range entirely
- **Per-entity-type overrides** — configure specific entity types to always or never be culled (e.g. exempt armor stands)

### Block Entity Culling *(new in 1.1.0)*
Chests, furnaces, signs, banners, item frames, and armor stands are common sources of unexpected draw calls.

- **Distance culling** — block entities beyond a configurable distance are not rendered
- Fully profile-aware — distance adjusts automatically with each performance profile

### Particle Optimization
Uncontrolled particle explosions can tank FPS instantly.

- **Count cap** — once the configured particle limit is reached, no new particles are allocated
- **Distance filter** — particles spawning beyond a configurable radius from the player are discarded *before* they are created, saving both CPU and memory
- **FPS-aware radius** — when FPS is critically low, the allowed radius tightens further automatically

### GUI & HUD Optimization
The HUD renders every single frame. Small savings compound fast.

- **Hotbar slot hashing** — unchanged slots skip redundant processing
- **HUD update throttling** — non-critical stat elements (health, food, XP, armor) are gated to every other tick
- **ImmediatelyFast awareness** — FPSFlow's HUD layer steps aside completely when ImmediatelyFast is installed

### World Join Optimizer *(new in 1.2.0)*
That FPS drop right after loading into a world — gone.

- **Grace period** — on joining, culling distances start at 35% of normal and ease back smoothly over ~10 seconds
- **Async burst** — occlusion raycast processing triples during the grace period to drain the backlog that builds up while chunks flood in
- **Particle suppression** — particle cap and spawn radius are also tightened during the grace period
- Works for both singleplayer worlds and multiplayer servers
- Configurable grace period per profile (120–200 ticks); toggle in the in-game config screen

### In-game Config Screen *(new in 1.1.0)*
Install [ModMenu](https://modrinth.com/mod/modmenu) to get a settings button directly in the mod list. Toggle any feature on or off and switch performance profiles with a single click — no JSON editing required. ModMenu is optional; FPSFlow works fine without it.

---

## ✦ Performance Profiles

One setting, four presets. Changing the profile rewrites all other settings automatically.

| Profile | Entity Occlusion | Entity Dist. | BE Dist. | Particle Cap | Particle Dist. | HUD Throttle |
|---------|:---:|:---:|:---:|:---:|:---:|:---:|
| **Quality** | ✗ | 128 b | Off | Off | 128 b | ✗ |
| **Balanced** *(default)* | ✓ | 64 b | 64 b | 4 096 | 64 b | ✓ |
| **Performance** | ✓ | 48 b | 48 b | 1 024 | 32 b | ✓ |
| **Ultra Performance** | ✓ | 32 b | 32 b | 256 | 16 b | ✓ |

> Set `"profile": null` in the config to use fully custom values.

---

## ✦ Compatibility

FPSFlow detects installed mods at startup and automatically disables any overlapping features.

| Mod | Status |
|-----|--------|
| 🟢 **Sodium** | Fully compatible — FPSFlow adds entity/GUI optimizations on top |
| 🟢 **Lithium** | No overlap — both run independently |
| 🟢 **FerriteCore** | No overlap — both run independently |
| 🟢 **ModernFix** | No overlap — both run independently |
| 🟢 **ModMenu** | Optional — enables in-game config screen |
| ⚡ **EntityCulling** *(tr7zw)* | FPSFlow's built-in entity culling disables automatically |
| ⚡ **ImmediatelyFast** | FPSFlow's HUD caching defers automatically |

**Recommended stack:** Sodium + Lithium + FerriteCore + ImmediatelyFast + ModMenu + **FPSFlow**

---

## ✦ Configuration

Config file: `.minecraft/config/fpsflow.json`

```json
{
  "profile": "BALANCED",
  "updateChecker": { "enabled": true },
  "entityCulling": {
    "enabled": true,
    "occlusionCulling": true,
    "asyncOcclusion": true,
    "maxDistance": 64,
    "cacheUpdateIntervalTicks": 10,
    "entityTypeOverrides": {}
  },
  "blockEntityCulling": {
    "enabled": true,
    "maxDistance": 64
  },
  "particleOptimization": {
    "enabled": true,
    "maxParticles": 4096,
    "maxDistance": 64
  },
  "guiOptimization": {
    "enabled": true,
    "hotbarCaching": true,
    "hudUpdateThrottling": true
  },
  "renderCaching": { "enabled": true }
}
```

**Per-entity-type override** — never cull armor stands:
```json
"entityTypeOverrides": {
  "minecraft:armor_stand": false
}
```

---

## ✦ Installation

1. Install [**Fabric Loader**](https://fabricmc.net/use/installer/) for Minecraft 1.21.11
2. Download and install [**Fabric API**](https://modrinth.com/mod/fabric-api)
3. Drop the FPSFlow `.jar` into your `mods/` folder
4. *(Optional)* Install [**ModMenu**](https://modrinth.com/mod/modmenu) for the in-game settings screen
5. Launch — the config file is written automatically on first run

---

## ✦ FAQ

**Does FPSFlow conflict with Sodium, Lithium, or FerriteCore?**
No. FPSFlow is designed to complement these mods. Install all of them together for best results.

**Can entities "pop in" with occlusion culling?**
Rarely. The occlusion check is cached per entity and refreshed every 10 ticks by default. Lower `cacheUpdateIntervalTicks` for more accuracy at a small CPU cost. Async occlusion means the refresh is deferred rather than causing a frame spike.

**Can I make specific entities never get culled?**
Yes. Add them to `entityTypeOverrides` in `fpsflow.json`, e.g. `{"minecraft:item_frame": false}`.

**Is there an in-game config screen?**
Yes — install ModMenu and a settings button appears on the FPSFlow entry in the mod list.

**Does this work on servers?**
FPSFlow is purely client-side. It works with any server and requires no server-side installation.

**Is it compatible with Forge / NeoForge?**
No — Fabric only.

---

## ✦ Source & License

[**GitHub — fpsflow/fpsflow**](https://github.com/fpsflow/fpsflow)

Licensed under the **Mozilla Public License 2.0**.
You are free to use, modify, and redistribute FPSFlow. Modifications to covered files must remain MPL-2.0.
