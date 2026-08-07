package com.rei.app.economy

import androidx.compose.ui.graphics.Color
import com.rei.app.data.local.EconomyDao
import com.rei.app.data.local.EconomyEntity
import com.rei.app.data.local.TransactionEntity
import com.rei.app.data.local.UnlockEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

object ReiShop {
    data class ShopItem(
        val id: String,
        val name: String,
        val emoji: String,
        val cost: Int,
        val category: String,
        val rarity: String = "Common",
        val description: String = "",
        val glowHex: Long = 0L  // Non-zero = has animated glow (for Mythic items)
    )

    // THEMES (9)
    val THEME_ITEMS = listOf(
        ShopItem("theme_STRAWBERRY",    "Strawberry",     "◆", 100, "Themes", "Rare"),
        ShopItem("theme_OCEAN",         "Ocean Wave",     "◇", 100, "Themes", "Rare"),
        ShopItem("theme_SAKURA",        "Sakura",         "✦", 150, "Themes", "Rare"),
        ShopItem("theme_LAVENDER_NIGHT","Lavender Night", "◈", 150, "Themes", "Rare"),
        ShopItem("theme_MATRIX",        "Matrix",         "▣", 200, "Themes", "Epic"),
        ShopItem("theme_NUXT",          "Nuxt Dark",      "▢", 200, "Themes", "Epic"),
        ShopItem("theme_ROSE_GOLD",     "Rose Gold",      "♦", 250, "Themes", "Epic"),
        ShopItem("theme_MIDNIGHT_DUSK", "Midnight Dusk",  "◐", 200, "Themes", "Epic"),
        ShopItem("theme_AMOLED",        "AMOLED Black",   "●", 300, "Themes", "Legendary")
    )

    // ACCENTS (16)
    val ACCENT_ITEMS = listOf(
        ShopItem("accent_AURORA",        "Aurora",        "✧", 75, "Accents", "Rare"),
        ShopItem("accent_NEON_GREEN",    "Neon Green",    "▣", 75, "Accents", "Rare"),
        ShopItem("accent_HOT_PINK",      "Hot Pink",      "♥", 50, "Accents", "Common"),
        ShopItem("accent_ELECTRIC_BLUE", "Electric Blue", "✶", 50, "Accents", "Common"),
        ShopItem("accent_GOLD",          "Gold",          "★", 100, "Accents", "Epic"),
        ShopItem("accent_CORAL",         "Coral",         "◠", 60, "Accents", "Common"),
        ShopItem("accent_SUNSET",        "Sunset",        "◒", 60, "Accents", "Common"),
        ShopItem("accent_SAKURA_PINK",   "Sakura Pink",   "✦", 50, "Accents", "Common"),
        ShopItem("accent_OCEAN_TEAL",    "Ocean Teal",    "◇", 50, "Accents", "Common"),
        ShopItem("accent_LAVENDER",      "Lavender",      "◈", 60, "Accents", "Rare"),
        ShopItem("accent_MINT_GREEN",    "Mint",          "⚘", 60, "Accents", "Rare"),
        ShopItem("accent_VIOLET",        "Violet",        "◈", 75, "Accents", "Rare"),
        ShopItem("accent_CYAN",          "Cyan",          "◆", 75, "Accents", "Rare"),
        ShopItem("accent_ROSE",          "Rose",          "✿", 60, "Accents", "Common"),
        ShopItem("accent_CRUNCHYROLL",   "Crunchyroll",   "▸", 100, "Accents", "Epic"),
        ShopItem("accent_REI_BLUE",      "Rei Blue",      "◈", 40, "Accents", "Common")
    )

    // CARDS (6)
    val CARD_ITEMS = listOf(
        ShopItem("card_COMPACT",     "Compact",       "≡", 50, "Cards", "Common"),
        ShopItem("card_CINEMATIC",   "Cinematic",     "▶", 100, "Cards", "Rare"),
        ShopItem("card_GLASS",       "Glassmorphism", "☐", 150, "Cards", "Epic"),
        ShopItem("card_NEON",        "Neon Outline",  "✶", 150, "Cards", "Epic"),
        ShopItem("card_MINIMAL",     "Minimal",       "·", 30, "Cards", "Common"),
        ShopItem("card_MAGAZINE",    "Magazine",      "§", 200, "Cards", "Legendary")
    )

    // EFFECTS (12)
    val FX_ITEMS = listOf(
        ShopItem("fx_PARTICLES",       "Particle Background", "★", 200, "Effects", "Epic"),
        ShopItem("fx_GLOW_SCORE",      "Glow on Score",      "✦", 100, "Effects", "Rare"),
        ShopItem("fx_COLORFUL_GENRES", "Colorful Genres",    "✦", 150, "Effects", "Rare"),
        ShopItem("fx_PARALLAX",        "Parallax Banner",    "▲", 120, "Effects", "Rare"),
        ShopItem("fx_NEON_BORDER",     "Neon Card Borders", "✳", 180, "Effects", "Epic"),
        ShopItem("fx_RAIN",            "Rain Particles",     "▽", 250, "Effects", "Epic"),
        ShopItem("fx_SAKURA_PETALS",   "Sakura Petals",      "✦", 220, "Effects", "Epic"),
        ShopItem("fx_MATRIX_RAIN",     "Matrix Rain",        "▣", 280, "Effects", "Legendary"),
        ShopItem("fx_PULSE_ICONS",     "Pulsing Icons",      "✳", 80, "Effects", "Common"),
        ShopItem("fx_GRADIENT_NAV",    "Gradient Nav Bar",   "◐", 160, "Effects", "Rare"),
        ShopItem("fx_BLUR_OVERLAY",    "Blur Overlay Cards", "○", 140, "Effects", "Rare"),
        ShopItem("fx_GLITCH_HOVER",    "Glitch on Hover",    "✶", 300, "Effects", "Legendary")
    )

    // NAV (5)
    val NAV_ITEMS = listOf(
        ShopItem("nav_PILL",         "Floating Pill Nav", "⬡", 200, "Nav", "Epic"),
        ShopItem("nav_RAIL",        "Navigation Rail",   "≡", 150, "Nav", "Rare"),
        ShopItem("nav_GHOST",        "Ghost Nav",         "◇", 250, "Nav", "Epic"),
        ShopItem("nav_BUBBLE",       "Bubble Nav",        "○", 220, "Nav", "Epic"),
        ShopItem("nav_TRANSPARENT",  "Glass Nav",         "☐", 180, "Nav", "Rare")
    )

    // INDICATORS (4)
    val IND_ITEMS = listOf(
        ShopItem("ind_LINE",   "Line Indicator",  "━",   60, "Indicators", "Common"),
        ShopItem("ind_NUMBER", "Number Counter",  "#",  80, "Indicators", "Rare"),
        ShopItem("ind_DOTS",   "Animated Dots",   "•••", 100, "Indicators", "Rare"),
        ShopItem("ind_GLOW",   "Glow Indicator",  "✦", 150, "Indicators", "Epic")
    )

    // PROFILE (8)
    val PROF_ITEMS = listOf(
        ShopItem("prof_ANIMATED_BORDER", "Animated Border",    "♛", 300, "Profile", "Legendary"),
        ShopItem("prof_BANNER_GLINT",    "Banner Glint",      "★", 150, "Profile", "Epic"),
        ShopItem("prof_AURORA_RING",     "Aurora Avatar Ring","✧", 200, "Profile", "Epic"),
        ShopItem("prof_SAKURA_CROWN",    "Sakura Crown",      "✦", 250, "Profile", "Legendary"),
        ShopItem("prof_FLAME_BADGE",    "Flame Streak Badge","▲", 180, "Profile", "Epic"),
        ShopItem("prof_DIAMOND_FRAME",   "Diamond Frame",     "◈", 350, "Profile", "Legendary"),
        ShopItem("prof_GLOW_USERNAME",   "Glow Username",     "✳", 120, "Profile", "Rare"),
        ShopItem("prof_PARTICLES_BG",    "Profile Particles", "✧", 200, "Profile", "Epic")
    )

    // CAROUSEL (6)
    val CAR_ITEMS = listOf(
        ShopItem("car_3D_TILT",     "3D Tilt Cards",     "▤", 200, "Carousel", "Epic"),
        ShopItem("car_BLUR_BG",     "Blur Background",   "○", 120, "Carousel", "Rare"),
        ShopItem("car_CINEMA_MASK", "Cinema Mask",       "▶", 150, "Carousel", "Rare"),
        ShopItem("car_KEN_BURNS",   "Ken Burns Effect", "▷", 250, "Carousel", "Epic"),
        ShopItem("car_VHS_OVERLAY", "VHS Overlay",       "◧", 300, "Carousel", "Legendary"),
        ShopItem("car_GLITCH",      "Glitch Transition", "✶", 280, "Carousel", "Legendary")
    )

    // SEARCH (5)
    val SRC_ITEMS = listOf(
        ShopItem("src_RECENT_CHIPS", "Recent Search Chips", "⏱",  80, "Search", "Common"),
        ShopItem("src_VOICE_SEARCH", "Voice Search Icon",   "✦", 120, "Search", "Rare"),
        ShopItem("src_ANIMATED_BAR", "Animated Search Bar", "✳", 100, "Search", "Rare"),
        ShopItem("src_GLOW_FOCUS",  "Glow on Focus",       "✦",  60, "Search", "Common"),
        ShopItem("src_FILTER_PANEL", "Filter Side Panel",   "☰", 150, "Search", "Epic")
    )

    // SPLASH (5)
    val SPLASH_ITEMS = listOf(
        ShopItem("splash_REI_GLOW",    "Rei Glow",         "◈", 100, "Splash", "Rare"),
        ShopItem("splash_PARTICLES",   "Ember Particles",  "▲", 150, "Splash", "Epic"),
        ShopItem("splash_SAKURA_FALL", "Sakura Fall",      "✦", 180, "Splash", "Epic"),
        ShopItem("splash_LIGHTNING",   "Lightning Strike", "✶", 250, "Splash", "Legendary"),
        ShopItem("splash_AURORA",      "Aurora Wave",      "✧", 200, "Splash", "Epic")
    )

    // WIDGET (4)
    val WIDGET_ITEMS = listOf(
        ShopItem("widget_COMPACT",     "Compact Widget",    "□", 100, "Widget", "Rare"),
        ShopItem("widget_TRANSPARENT", "Transparent Widget","☐", 150, "Widget", "Epic"),
        ShopItem("widget_NEON",        "Neon Widget",       "✶", 200, "Widget", "Epic"),
        ShopItem("widget_AIRING_GLOW", "Glow Airing Dot",  "✦",  80, "Widget", "Common")
    )

    // PREMIUM (8)
    val PREMIUM_ITEMS = listOf(
        ShopItem("prem_CUSTOM_GENRES",   "Custom Genre Colors","✦", 500, "Premium", "Legendary"),
        ShopItem("prem_EPISODE_GRID",    "Episode Grid Pro",   "▥", 250, "Premium", "Epic"),
        ShopItem("prem_FRANCHISE_VIEW",  "Franchise Tree",     "⌇", 250, "Premium", "Epic"),
        ShopItem("prem_EP_NOTES",        "Unlimited Ep Notes", "✎", 300, "Premium", "Epic"),
        ShopItem("prem_BATCH_TRACK",     "Batch Tracking",     "✓", 400, "Premium", "Legendary"),
        ShopItem("prem_COMPARE_LISTS",   "Compare Lists",      "↻", 350, "Premium", "Legendary"),
        ShopItem("prem_SOCIAL_FEED",     "Social Activity",    "▪", 400, "Premium", "Legendary"),
        ShopItem("prem_WATCH_STATS",     "Advanced Watch Stats","↗",300, "Premium", "Epic")
    )

    // MYTHIC (10) — ultra-premium, highest tier, extremely expensive
    val MYTHIC_ITEMS = listOf(
        ShopItem("mythic_VOID_THEME",      "Void Walker Theme",   "\u2742", 2000, "Mythic", "Mythic", "Tear through dimensions. Animated void particles with crimson-black gradient.", 0xFFFF1744),
        ShopItem("mythic_COSMIC_RING",     "Cosmic Avatar Ring",  "\u2735", 1800, "Mythic", "Mythic", "Orbiting celestial bodies around your avatar. Animated rotation.", 0xFF7C4DFF),
        ShopItem("mythic_PHOENIX_FX",      "Phoenix Resurrection","\u2740", 2500, "Mythic", "Mythic", "Rising phoenix flame animation on completing anime. Legendary effect.", 0xFFFF6D00),
        ShopItem("mythic_DRAGON_BADGE",    "Dragon Sovereign",    "\u2764", 1500, "Mythic", "Mythic", "Writhing dragon badge with animated scale shimmer. Power symbol.", 0xFFFF1744),
        ShopItem("mythic_HOLO_CARD",       "Holographic Card",    "\u25C9", 1200, "Mythic", "Mythic", "Rainbow holographic sheen that shifts with scroll angle. Prismatic.", 0xFF00BCD4),
        ShopItem("mythic_CROWN_INFINITE",  "Infinite Crown",      "\u265B", 3000, "Mythic", "Mythic", "Floating crown with golden particle aura. The ultimate status symbol.", 0xFFFFD700),
        ShopItem("mythic_NEBULA_NAV",      "Nebula Navigation",   "\u2728", 1600, "Mythic", "Mythic", "Living nebula background behind navigation. Stars slowly drift.", 0xFF7C4DFF),
        ShopItem("mythic_CHROMATIC_GLOW",  "Chromatic Glow",      "\u2606", 1400, "Mythic", "Mythic", "Every UI element pulses with a slow rainbow chromatic shift.", 0xFFE91E63),
        ShopItem("mythic_ETERNAL_FLAME",   "Eternal Flame",       "\u2666", 2200, "Mythic", "Mythic", "Persistent flame animation on your profile. Never extinguishes.", 0xFFFF5722),
        ShopItem("mythic_OMNISCIENT",      "Omniscient View",     "\u273B", 3500, "Mythic", "Mythic", "See all. Unlock every visual feature simultaneously. The final tier.", 0xFFFF1744)
    )

    // BUNDLES (5) — discounted sets
    val BUNDLE_ITEMS = listOf(
        ShopItem("bundle_STARTER",    "Starter Pack (500C value)",  "✧", 199, "Bundles", "Epic"),
        ShopItem("bundle_THEMER",     "Theme Master (1200C)",       "✦", 499, "Bundles", "Legendary"),
        ShopItem("bundle_EFFECTS",    "All Effects (1600C)",        "★", 599, "Bundles", "Legendary"),
        ShopItem("bundle_PROFILE",    "Profile Pro (1250C)",        "♛", 499, "Bundles", "Legendary"),
        ShopItem("bundle_LEGENDARY",  "Legendary All (3500C)",      "★", 999, "Bundles", "Legendary"),
        ShopItem("bundle_MYTHIC",     "Mythic Ascension (18000C)",  "\u2756", 4999, "Bundles", "Mythic", "All 10 Mythic items. The ultimate collection.", 0xFFFF1744)
    )

    val ALL_ITEMS = THEME_ITEMS + ACCENT_ITEMS + CARD_ITEMS + FX_ITEMS + NAV_ITEMS + IND_ITEMS +
        PROF_ITEMS + CAR_ITEMS + SRC_ITEMS + SPLASH_ITEMS + WIDGET_ITEMS + PREMIUM_ITEMS + BUNDLE_ITEMS + MYTHIC_ITEMS

    val CATEGORIES = listOf("Themes", "Accents", "Cards", "Effects", "Nav", "Indicators", "Profile",
        "Carousel", "Search", "Splash", "Widget", "Premium", "Bundles", "Mythic")

    fun findById(id: String): ShopItem? = ALL_ITEMS.find { it.id == id }
    fun forTheme(name: String): ShopItem? = THEME_ITEMS.find { it.id == "theme_$name" }
    fun forAccent(name: String): ShopItem? = ACCENT_ITEMS.find { it.id == "accent_$name" }
    fun forCard(name: String): ShopItem? = CARD_ITEMS.find { it.id == "card_$name" }

    val RARITY_ORDER = listOf("Common", "Rare", "Epic", "Legendary", "Mythic")
    val RARITY_COLORS: Map<String, Color> = mapOf(
        "Common" to Color(0xFF9E9E9E),
        "Rare" to Color(0xFF42A5F5),
        "Epic" to Color(0xFFAB47BC),
        "Legendary" to Color(0xFFFF6D00),
        "Mythic" to Color(0xFFFF1744)
    )
    val RARITY_BG: Map<String, Color> = mapOf(
        "Common" to Color(0xFF9E9E9E),
        "Rare" to Color(0xFF42A5F5),
        "Epic" to Color(0xFFAB47BC),
        "Legendary" to Color(0xFFFF6D00),
        "Mythic" to Color(0xFFFF1744)
    )
}

object ReiRewards {
    const val TRACK_ANIME = 10
    const val COMPLETE_ANIME = 50
    const val RATE_ANIME = 5
    const val WATCH_EPISODE = 2
    const val DAILY_STREAK = 20
    const val RANDOM_DISCOVER = 1
    const val FIRST_SEARCH = 5
    const val WRITE_NOTE = 3
    const val FIRST_LOGIN = 100
    const val IMAGE_SEARCH = 2
    const val FAVORITE = 3
    const val SHARE_ANIME = 5
    const val WRITE_REVIEW = 15
    const val REWATCH_ANIME = 30
    const val STREAK_WEEK = 10
    const val STREAK_MONTH = 50
}

@Singleton
class EconomyManager @Inject constructor(private val dao: EconomyDao) {
    val balance: Flow<Int> = flow { emit((dao.get("balance")?.longValue ?: 0L).toInt()) }
    val totalEarned: Flow<Int> = flow { emit((dao.totalEarned() ?: 0L).toInt()) }
    val unlocks: Flow<List<UnlockEntity>> = dao.allUnlocks()
    val recentTransactions: Flow<List<TransactionEntity>> = dao.recentTransactions(50)

    suspend fun getBalance(): Int = (dao.get("balance")?.longValue ?: 0L).toInt()

    suspend fun earn(amount: Int, reason: String, targetId: String? = null) {
        val current = getBalance()
        dao.set(EconomyEntity("balance", (current + amount).toLong()))
        dao.addTransaction(TransactionEntity(type = "EARN", amount = amount, reason = reason, targetId = targetId))
    }

    suspend fun spend(amount: Int, reason: String, targetId: String? = null): Boolean {
        val current = getBalance()
        if (current < amount) return false
        dao.set(EconomyEntity("balance", (current - amount).toLong()))
        dao.addTransaction(TransactionEntity(type = "SPEND", amount = amount, reason = reason, targetId = targetId))
        return true
    }

    suspend fun purchase(item: ReiShop.ShopItem): Boolean {
        if (isUnlocked(item.id)) return true
        if (!spend(item.cost, "unlock_${item.id}", item.id)) return false
        dao.addUnlock(UnlockEntity(itemId = item.id, cost = item.cost))
        return true
    }

    suspend fun isUnlocked(itemId: String): Boolean = dao.getUnlock(itemId) != null

    suspend fun checkDailyStreak(): Int {
        val now = System.currentTimeMillis()
        val lastDaily = dao.get("last_daily")?.longValue ?: 0L
        val oneDay = 86_400_000L
        if (now - lastDaily >= oneDay) {
            val currentStreak = (dao.get("streak")?.longValue ?: 0L).toInt()
            val newStreak = if ((now / oneDay) - (lastDaily / oneDay) <= 1) currentStreak + 1 else 1
            dao.set(EconomyEntity("streak", newStreak.toLong()))
            dao.set(EconomyEntity("last_daily", now))
            val bonus = ReiRewards.DAILY_STREAK + when {
                newStreak >= 30 -> ReiRewards.STREAK_MONTH
                newStreak >= 7 -> ReiRewards.STREAK_WEEK
                else -> 0
            }
            earn(bonus, "daily_streak", "day_$newStreak")
            return bonus
        }
        return 0
    }

    suspend fun getStreak(): Int = (dao.get("streak")?.longValue ?: 0L).toInt()
}
