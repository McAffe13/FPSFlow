# FPSFlow

**Real FPS improvements — no placebo, no bloat.**

FPSFlow is a client-side Fabric optimization mod that targets the biggest rendering bottlenecks in vanilla Minecraft. Every feature is measurably effective, individually toggleable, and automatically adjusts to your hardware via four built-in performance profiles.

---

## ✦ Features at a Glance

| | Feature | What it does |
|---|---------|-------------|
| 🔍 | **Entity Culling** | Frustum, occlusion, and distance culling. Dual-point raycasting prevents false-positives on small entities. Auto-disables when [EntityCulling](https://modrinth.com/mod/entityculling) is detected. |
| 📐 | **Entity LOD** | Entities beyond a configurable distance render every 2nd tick. Server NPCs and entities within nameplate range are always fully exempt — no flicker, even in busy lobbies. |
| 🏷️ | **Nameplate Culling** | Hides name tags beyond a configurable distance. Server-forced labels are never hidden. |
| 🧱 | **Block Entity Culling** | Skips chests, furnaces, signs, banners, and item frames beyond the configured range. |
| ✨ | **Particle Optimization** | Count cap + three-zone density LOD (full / ~50 % / none) based on distance from player. |
| 🖼️ | **Painting Back-Face Culling** | Instantly skips paintings when the camera is behind them — they are never visible from the back. |
| 🌙 | **Background FPS Limiter** | Caps frame rate when the window loses focus, is minimised, or on any loading/menu screen (reduces GPU spinning during startup). |
| 🌍 | **Singleplayer Boost** | More aggressive adaptive culling in singleplayer — frees CPU for chunk generation threads so exploration stays smooth. |
| ⚡ | **World Join Optimizer** | Culling distances ramp up from 35 % on join and return to 100 % over ~10 seconds — eliminates the initial FPS spike. |
| 🎮 | **GUI & HUD Optimizer** | Dirty-flag hotbar caching (including `CustomModelData`); HUD throttling with immediate response to actual stat changes. Caches auto-clear on resource-pack reload to prevent stale item textures. |

---

## ✦ In-game Config Screen

Install [ModMenu](https://modrinth.com/mod/modmenu) to get a settings screen directly in the mod list.

- **Four tabs**: General · Culling · LOD & Labels · Background FPS
- Every button has a **tooltip** explaining exactly what the setting does
- Switch profiles, toggle features, adjust LOD distances and FPS caps — no JSON editing required
- New in 1.7.0: **Nameplate distance slider**, **Singleplayer Boost toggle**, and **Menu/Load FPS cap slider** added
- New in 1.7.11: item render throttles automatically pause during server resource-pack reloads — no more flickering items when joining texture-pack servers

---

## ✦ Performance Profiles

One setting, four presets — changing the profile rewrites all other settings automatically.

| Profile | Entity Dist. | BE Dist. | LOD threshold | Nameplate | Particle (mid/max) | BG FPS (unfoc/min/menu) |
|---------|:---:|:---:|:---:|:---:|:---:|:---:|
| **Quality** | 128 b | Off | 128 b | Off | Off | 30/10/120 |
| **Balanced** *(default)* | 64 b | 64 b | 96 b | 32 b | 32/64 b | 60/30/120 |
| **Performance** | 48 b | 48 b | 64 b | 24 b | 16/32 b | 10/3/120 |
| **Ultra Performance** | 32 b | 32 b | 40 b | 16 b | 8/16 b | 5/2/60 |

> Set `"profile": null` in the config to use fully custom values.

---

## ✦ Compatibility

FPSFlow detects installed mods at startup and automatically disables overlapping features.

| Mod | Status |
|-----|--------|
| 🟢 **[Sodium](https://modrinth.com/mod/sodium)** | Fully compatible — FPSFlow adds entity/GUI optimizations on top |
| 🟢 **[Lithium](https://modrinth.com/mod/lithium)** | No overlap — both run independently |
| 🟢 **[FerriteCore](https://modrinth.com/mod/ferrite-core)** | No overlap — both run independently |
| 🟢 **[ModernFix](https://modrinth.com/mod/modernfix)** | No overlap — both run independently |
| 🟢 **[ModMenu](https://modrinth.com/mod/modmenu)** | Optional — enables in-game config screen |
| ⚡ **[EntityCulling](https://modrinth.com/mod/entityculling)** *(tr7zw)* | FPSFlow's built-in culling disables automatically — **recommended companion** for superior occlusion accuracy |
| ⚡ **[ImmediatelyFast](https://modrinth.com/mod/immediatelyfast)** | FPSFlow's HUD caching defers automatically |

**Recommended stack:** Sodium + Lithium + FerriteCore + ImmediatelyFast + **EntityCulling** + ModMenu + **FPSFlow**

---

## ✦ Installation

1. Install [**Fabric Loader**](https://fabricmc.net/use/installer/) for Minecraft 1.21.11
2. Download and install [**Fabric API**](https://modrinth.com/mod/fabric-api)
3. Drop the FPSFlow `.jar` into your `mods/` folder
4. *(Optional)* Install [**ModMenu**](https://modrinth.com/mod/modmenu) for the in-game settings screen
5. Launch — the config is written automatically on first run

---

## ✦ FAQ

**Does FPSFlow conflict with Sodium, Lithium, or FerriteCore?**
No. FPSFlow is designed to complement these mods — install all of them together.

**Should I use FPSFlow's built-in entity culling or the EntityCulling mod?**
Both work. When EntityCulling (tr7zw) is installed, FPSFlow automatically disables its own culling to avoid duplicate raycasts. EntityCulling uses more sophisticated occlusion; FPSFlow adds Entity LOD, nameplate culling, and all other features on top.

**Can entities "pop in" with occlusion culling?**
Rarely. The cache refreshes every 10 ticks (configurable). FPSFlow samples the entity's eye position for the raycast — naturally above the geometric centre, which avoids false-positives for small entities like floating heads and armor stands.

**Does this work on servers?**
FPSFlow is purely client-side — works with any server, no server installation required. Servers that send a custom resource pack are fully supported: FPSFlow detects the pack reload and pauses render throttles for ~3 seconds so item-frame maps and hotbar items always show the correct textures after the pack loads.

---

## ✦ Source & License

[**GitHub — McAffe13/FPSFlow**](https://github.com/McAffe13/FPSFlow) · Licensed under the **Mozilla Public License 2.0**
