# Rei 零 — Anime Tracker

> A production-quality Android anime tracker built with 100% Kotlin and Jetpack Compose, with deep customization, multi-API integration, and a coin economy.

## 🔮 Rei Coins Economy — 93 Purchasable Items

### Rarity System
Every item has a rarity: **Common** (gray) → **Rare** (blue) → **Epic** (purple) → **Legendary** (orange)

### 13 Categories
| Category | Items | Price Range | Rarities |
|----------|-------|-------------|----------|
| **Themes** | 9 | 100–300🔮 | Rare→Legendary |
| **Accents** | 16 | 40–100🔮 | Common→Epic |
| **Cards** | 6 | 30–200🔮 | Common→Legendary |
| **Effects** | 12 | 80–300🔮 | Common→Legendary |
| **Nav** | 5 | 150–250🔮 | Rare→Epic |
| **Indicators** | 4 | 60–150🔮 | Common→Epic |
| **Profile** | 8 | 120–350🔮 | Rare→Legendary |
| **Carousel** | 6 | 120–300🔮 | Rare→Legendary |
| **Search** | 5 | 60–150🔮 | Common→Epic |
| **Splash** | 5 | 100–250🔮 | Rare→Legendary |
| **Widget** | 4 | 80–200🔮 | Common→Epic |
| **Premium** | 8 | 250–500🔮 | Epic→Legendary |
| **Bundles** | 5 | 199–999🔮 | Epic→Legendary (50-70% off!) |

### Notable Items
- 🖤 AMOLED Black theme (300🔮, Legendary)
- 📖 Magazine card style (200🔮, Legendary)
- ⚡ Glitch on Hover effect (300🔮, Legendary)
- 📼 VHS Overlay carousel (300🔮, Legendary)
- 💎 Diamond Frame profile (350🔮, Legendary)
- 🎨 Custom Genre Colors (500🔮, Legendary)
- 🏆 Legendary All bundle (999🔮 — 70% off buying individually)

### Earning (17 actions)
🔥 Daily Streak (+20, +10 bonus at 7 days, +50 at 30 days) • 📺 Track (+10) • ✅ Complete (+50) • ⭐ Rate (+5) • ▶️ Episode (+2) • 🎲 Random (+1) • 🔍 Search/Day (+5) • 📝 Note (+3) • 📸 Scene Search (+2) • ❤️ Favorite (+3) • 🔐 First Login (+100) • 🔗 Share (+5) • ✍️ Review (+15) • 🔄 Rewatch (+30) • 🎯 50 Anime Milestone (+100) • 🎯 100 Anime Milestone (+250)

## ✨ Features

### Core
- **6 API integrations** — AniList, Jikan/MAL, Kitsu, Shikimori, Trace.moe, LiveChart.me (all zero API keys)
- **14+ screens** — Home, Search, Discover, Streaming, Tracking, Seasonal, Detail, ImageSearch, Calendar, Stats, Random, Profile, Settings, Onboarding, **Episode Grid**, **Franchise Tree**, **Economy Shop**
- **Animated splash screen** — 3-layer vector animation

### New in v2.2.0
- 🔮 **Rei Coins Economy** — Earn & spend virtual currency for premium customization
- 📊 **Per-Episode Tracking Grid** — Tappable grid of episode squares with notes + rewatch tracking
- 🌳 **Franchise/Watch Order Tree** — Visual tree from Shikimori franchise + related data
- 🏷️ **Spring Hero Transitions** — Spring-based page transitions between screens
- 🛒 **Coin Shop** — Browse, preview, and unlock customization items

### Customization (50+ options)
- **12 theme modes** — System, Light, Dark, AMOLED, Midnight Dusk, Strawberry, Ocean Wave, Sakura, Lavender Night, Matrix, Nuxt, Rose Gold
- **16 accent colors** × 16 secondary accents
- **6 card styles** — Standard, Compact, Minimal, Cinematic, Glassmorphism, Neon Outline
- **5 score styles** — Stars, 10pt, 100pt, Percentage, Smiley
- **3 nav styles** — Bottom Bar, Navigation Rail, Floating Pill
- **4 indicator styles** — Dot, Line, Number, None
- **30+ genre colors** — Action→Red, Romance→Pink, Sci-Fi→Cyan, etc.
- **13 layout sliders** + **20 show/hide toggles** + **12 effect controls** + **15 behavior controls**

## 🏗️ Architecture

```
Rei/app/src/main/java/com/rei/app/
├── economy/         — ReiShop catalog, EconomyManager, ReiRewards rates
├── data/
│   ├── local/       — Room DB (Anime, Tracking, Episodes, Economy, Transactions, Unlocks)
│   ├── remote/      — 6 API clients (AniList, Jikan, Kitsu, Shikimori, TraceMoe, LiveChart)
│   └── repository/  — Aggregation + Room CRUD + economy rewards on actions
├── ui/screens/
│   ├── economy/     — 🔮 Coin shop with categories, purchase dialog, balance
│   ├── episodes/    — 📊 Tappable episode grid with notes
│   ├── franchise/   — 🌳 Watch order tree view
│   └── ...          — 14 other screens
└── widget/ + work/  — Glance widget + WorkManager notifications
```

## 🔧 Tech Stack

Kotlin 1.9.24 • Compose BOM 2024.05.00 • Hilt 2.50 (KSP) • Room 2.6.1 (KSP) • OkHttp 5.0.0 • Coil 2.5.0 • DataStore • WorkManager 2.9.0 • Glance 1.0.0 • kotlinx-serialization
