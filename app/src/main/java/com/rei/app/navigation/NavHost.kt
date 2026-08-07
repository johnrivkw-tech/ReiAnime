package com.rei.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.*
import com.rei.app.ui.screens.calendar.CalendarScreen
import com.rei.app.ui.screens.detail.AnimeDetailScreen
import com.rei.app.ui.screens.discover.DiscoverScreen
import com.rei.app.ui.screens.economy.EconomyScreen
import com.rei.app.ui.screens.episodes.EpisodeGridScreen
import com.rei.app.ui.screens.franchise.FranchiseScreen
import com.rei.app.ui.screens.home.HomeScreen
import com.rei.app.ui.screens.imagesearch.ImageSearchScreen
import com.rei.app.ui.screens.onboarding.OnboardingScreen
import com.rei.app.ui.screens.profile.ProfileScreen
import com.rei.app.ui.screens.random.RandomAnimeScreen
import com.rei.app.ui.screens.search.SearchScreen
import com.rei.app.ui.screens.seasonal.SeasonalScreen
import com.rei.app.ui.screens.settings.SettingsScreen
import com.rei.app.ui.screens.stats.StatsScreen
import com.rei.app.ui.screens.streaming.StreamingScreen
import com.rei.app.ui.screens.tracking.TrackingScreen
import com.rei.app.ui.screens.waifu.WaifuGalleryScreen
import com.rei.app.ui.screens.reviews.ReviewsScreen
import com.rei.app.ui.screens.news.NewsScreen
import com.rei.app.ui.screens.quiz.QuizScreen
import com.rei.app.ui.screens.collections.CollectionsScreen
import com.rei.app.ui.screens.compare.CompareScreen
import com.rei.app.ui.screens.recommend.RecommendationScreen
import com.rei.app.ui.screens.manga.MangaScreen

sealed class Route(val route: String) {
    data object Home : Route("home")
    data object Search : Route("search")
    data object SearchGenre : Route("search/{genre}") { fun create(genre: String) = "search/$genre" }
    data object Seasonal : Route("seasonal")
    data object Tracking : Route("tracking")
    data object Profile : Route("profile")
    data object Settings : Route("settings")
    data object Calendar : Route("calendar")
    data object Stats : Route("stats")
    data object Random : Route("random")
    data object Discover : Route("discover")
    data object Onboarding : Route("onboarding")
    data object ImageSearch : Route("imagesearch")
    data object Streaming : Route("streaming")
    data object AnimeDetail : Route("anime/{id}") { fun create(id: Int) = "anime/$id" }
    data object EpisodeGrid : Route("episodes/{id}/{title}/{total}") {
        fun create(id: Int, title: String, total: Int) = "episodes/$id/$title/$total"
    }
    data object Franchise : Route("franchise/{id}/{title}") {
        fun create(id: Int, title: String) = "franchise/$id/$title"
    }
    data object Economy : Route("economy")
    data object WaifuGallery : Route("waifu")
    data object News : Route("news")
    data object Quiz : Route("quiz")
    data object Reviews : Route("reviews/{id}/{title}") {
        fun create(id: Int, title: String) = "reviews/$id/$title"
    }
    data object Collections : Route("collections")
    data object Compare : Route("compare/{username}") { fun create(username: String) = "compare/$username" }
    data object Recommendations : Route("recommendations")
    data object Manga : Route("manga")
}

@Composable
fun ReiNavHost(nav: NavHostController = rememberNavController(), m: Modifier = Modifier) {
    NavHost(
        nav, Route.Home.route, modifier = m,
        enterTransition = { fadeIn(tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, spring(stiffness = Spring.StiffnessMedium)) },
        exitTransition = { fadeOut(tween(200)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, spring(stiffness = Spring.StiffnessMedium)) },
        popEnterTransition = { fadeIn(tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, spring(stiffness = Spring.StiffnessMedium)) },
        popExitTransition = { fadeOut(tween(200)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, spring(stiffness = Spring.StiffnessMedium)) }
    ) {
        composable(Route.Home.route) { HomeScreen({ nav.navigate(Route.AnimeDetail.create(it)) }, nav) }
        composable(Route.Search.route) { SearchScreen { nav.navigate(Route.AnimeDetail.create(it)) } }
        composable(Route.SearchGenre.route, arguments = listOf(navArgument("genre") { type = NavType.StringType })) {
            val genre = it.arguments?.getString("genre") ?: ""
            SearchScreen(initialGenre = genre, onAnimeClick = { nav.navigate(Route.AnimeDetail.create(it)) })
        }
        composable(Route.Seasonal.route) { SeasonalScreen { nav.navigate(Route.AnimeDetail.create(it)) } }
        composable(Route.Tracking.route) { TrackingScreen { nav.navigate(Route.AnimeDetail.create(it)) } }
        composable(Route.Profile.route) { ProfileScreen({ nav.navigate(Route.Settings.route) }, { nav.navigate(Route.AnimeDetail.create(it)) }, { nav.navigate(Route.Economy.route) }, { nav.navigate(Route.Collections.route) }, { nav.navigate(Route.Compare.create("user")) }) }
        composable(Route.Settings.route) { SettingsScreen { nav.popBackStack() } }
        composable(Route.Calendar.route) { CalendarScreen { nav.navigate(Route.AnimeDetail.create(it)) } }
        composable(Route.Stats.route) { StatsScreen() }
        composable(Route.Random.route) { RandomAnimeScreen { nav.navigate(Route.AnimeDetail.create(it)) } }
        composable(Route.Discover.route) { DiscoverScreen({ nav.navigate(Route.AnimeDetail.create(it)) }, { genre -> nav.navigate(Route.SearchGenre.create(genre)) }) }
        composable(Route.Onboarding.route) { OnboardingScreen { nav.popBackStack() } }
        composable(Route.ImageSearch.route) { ImageSearchScreen { nav.navigate(Route.AnimeDetail.create(it)) } }
        composable(Route.Streaming.route) { StreamingScreen { nav.navigate(Route.AnimeDetail.create(it)) } }
        composable(Route.Economy.route) { EconomyScreen() }
        composable(Route.WaifuGallery.route) { WaifuGalleryScreen { nav.popBackStack() } }
        composable(Route.News.route) { NewsScreen { nav.navigate(Route.AnimeDetail.create(it)) } }
        composable(Route.Quiz.route) { QuizScreen() }
        composable(Route.Collections.route) { CollectionsScreen(emptyList(), onBack = { nav.popBackStack() }) }
        composable(Route.Compare.route, arguments = listOf(navArgument("username") { type = NavType.StringType })) {
            val username = it.arguments?.getString("username") ?: ""
            CompareScreen(username, emptyList(), onBack = { nav.popBackStack() })
        }
        composable(Route.Recommendations.route) { RecommendationScreen({ nav.navigate(Route.AnimeDetail.create(it)) }, { nav.popBackStack() }) }
        composable(Route.Manga.route) { MangaScreen({ nav.popBackStack() }) }

        // Reviews
        composable(Route.Reviews.route, arguments = listOf(
            navArgument("id") { type = NavType.IntType },
            navArgument("title") { type = NavType.StringType }
        )) {
            val id = it.arguments?.getInt("id") ?: return@composable
            val title = it.arguments?.getString("title") ?: ""
            ReviewsScreen(id, title) { nav.popBackStack() }
        }

        // Anime Detail — pass functions for episode grid + franchise
        composable(Route.AnimeDetail.route, arguments = listOf(navArgument("id") { type = NavType.IntType })) {
            val id = it.arguments?.getInt("id") ?: return@composable
            AnimeDetailScreen(
                animeId = id,
                onBack = { nav.popBackStack() },
                onEpisodeGrid = { title, total -> nav.navigate(Route.EpisodeGrid.create(id, title, total)) },
                onFranchise = { title -> nav.navigate(Route.Franchise.create(id, title)) }
            )
        }

        // Episode Grid
        composable(Route.EpisodeGrid.route, arguments = listOf(
            navArgument("id") { type = NavType.IntType },
            navArgument("title") { type = NavType.StringType },
            navArgument("total") { type = NavType.IntType }
        )) {
            val id = it.arguments?.getInt("id") ?: return@composable
            val title = it.arguments?.getString("title") ?: ""
            val total = it.arguments?.getInt("total") ?: 12
            EpisodeGridScreen(id, title, total) { nav.popBackStack() }
        }

        // Franchise / Watch Order
        composable(Route.Franchise.route, arguments = listOf(
            navArgument("id") { type = NavType.IntType },
            navArgument("title") { type = NavType.StringType }
        )) {
            val id = it.arguments?.getInt("id") ?: return@composable
            val title = it.arguments?.getString("title") ?: ""
            FranchiseScreen(id, title, { nav.popBackStack() }) { nav.navigate(Route.AnimeDetail.create(it)) }
        }
    }
}
