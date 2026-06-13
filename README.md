# FPSFlow

> **Real FPS improvements — no placebo, no bloat.**

[![Modrinth](https://img.shields.io/badge/Modrinth-fpsflow-1bd96a?logo=modrinth)](https://modrinth.com/mod/fpsflow)
[![License: MPL-2.0](https://img.shields.io/badge/License-MPL--2.0-blue.svg)](https://opensource.org/licenses/MPL-2.0)
[![Fabric](https://img.shields.io/badge/Mod%20Loader-Fabric-747bff)](https://fabricmc.net/)
[![Java 21](https://img.shields.io/badge/Java-21-orange)](https://adoptium.net/)

Client-side Fabric optimization mod targeting the biggest rendering bottlenecks in vanilla Minecraft. Every feature is individually toggleable and adjusts automatically via four built-in performance profiles.

---

## Features

| Feature | What it does |
|---------|-------------|
| **Entity Culling** | Frustum, occlusion (dual-point raycast), and distance culling. Auto-disables when [EntityCulling](https://modrinth.com/mod/entityculling) is detected. |
| **Entity LOD** | Distant entities render every 2nd or 3rd tick — XOR-distributed to avoid synchronized "freeze frames". Server NPCs and nameplate-range entities are exempt. |
| **Nameplate Culling** | Hides name tags beyond a configurable distance. Server-forced labels are never culled. |
| **Block Entity Culling** | Skips chests, furnaces, signs, banners, and item frames beyond the configured distance. |
| **Particle Optimization** | Count cap + three-zone density LOD (full / ~50 % / none) based on distance. |
| **Painting Back-Face Culling** | Dot-product check skips paintings when the camera is behind them. |
| **Background FPS Limiter** | Caps frame rate when the window is unfocused or minimised. |
| **Singleplayer Boost** | More aggressive adaptive culling in singleplayer, freeing CPU for chunk generation threads. |
| **World Join Optimizer** | Culling distances start at 35 % on join and ease back to 100 % over ~10 s. |
| **GUI & HUD Optimization** | Dirty-flag hotbar caching (including `CustomModelData`); HUD throttling with immediate response to stat changes. Auto-clears on resource-pack reload to prevent stale textures. |

---

## Performance Profiles

| Profile | Entity Dist. | BE Dist. | LOD (med/far) | Nameplate | Particle (mid/max) | BG FPS (unfoc/min) |
|---------|:---:|:---:|:---:|:---:|:---:|:---:|
| **Quality** | 128 b | Off | 48/96 b | Off | Off | 30/10 |
| **Balanced** *(default)* | 64 b | 64 b | 40/80 b | 32 b | 32/64 b | 60/30 |
| **Performance** | 48 b | 48 b | 24/48 b | 24 b | 16/32 b | 10/3 |
| **Ultra Performance** | 32 b | 32 b | 16/32 b | 16 b | 8/16 b | 5/2 |

---

## Compatibility

| Mod | Behaviour |
|-----|-----------|
| **[EntityCulling](https://modrinth.com/mod/entityculling)** (tr7zw) | FPSFlow's built-in entity culling disables automatically — recommended for best occlusion accuracy |
| **Sodium** | Fully compatible; FPSFlow adds entity/GUI layer on top |
| **ImmediatelyFast** | HUD caching deferred to ImmediatelyFast |
| **Lithium / FerriteCore / ModernFix** | No overlap; all run independently |

**Recommended stack:** Sodium + Lithium + FerriteCore + ImmediatelyFast + EntityCulling + ModMenu + **FPSFlow**

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 1.21.11
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Drop the FPSFlow `.jar` into your `mods/` folder
4. *(Optional)* Install [ModMenu](https://modrinth.com/mod/modmenu) for the in-game config screen
5. Launch — the config is written automatically on first run

---

## Configuration

Config file: `.minecraft/config/fpsflow.json`

All settings are accessible via the ModMenu config screen (four tabs: **General · Culling · LOD & Labels · Background FPS**). Every button shows a tooltip describing what the setting does.

**Per-entity-type override** — never cull armor stands:
```json
"entityTypeOverrides": {
  "minecraft:armor_stand": false
}
```

---

## License

[Mozilla Public License 2.0](LICENSE) — you are free to use, modify, and redistribute FPSFlow. Modifications to covered files must remain MPL-2.0.
