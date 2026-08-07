package com.rei.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.rei.app.data.local.AppDatabase
import com.rei.app.data.local.MIGRATION_3_4
import com.rei.app.data.remote.anilist.AniListApi
import com.rei.app.data.remote.jikan.JikanApi
import com.rei.app.data.remote.kitsu.KitsuApi
import com.rei.app.data.remote.livechart.LiveChartApi
import com.rei.app.data.remote.shikimori.ShikimoriApi
import com.rei.app.data.remote.tracemoe.TraceMoeApi
import com.rei.app.data.remote.animeschedule.AnimeScheduleApi
import com.rei.app.data.remote.waifu.WaifuApi
import com.rei.app.data.remote.mangadex.MangaDexApi
import com.rei.app.data.remote.simkl.SimklApi
import com.rei.app.data.repository.AnimeRepository
import com.rei.app.data.sync.SyncService
import com.rei.app.economy.EconomyManager
import com.rei.app.recommend.RecommendationEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rei_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton fun json() = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }

    @Provides @Singleton fun okHttp(@ApplicationContext ctx: Context): OkHttpClient {
        val cacheDir = File(ctx.cacheDir, "http_cache")
        val cache = Cache(cacheDir, 50L * 1024 * 1024) // 50MB HTTP cache
        return OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }) // BASIC in prod, BODY in debug
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .build()
                val response = chain.proceed(request)
                // Cache successful GET responses for 5 min if no Cache-Control
                if (response.isSuccessful && request.method == "GET" && response.header("Cache-Control") == null) {
                    response.newBuilder()
                        .header("Cache-Control", "public, max-age=300")
                        .build()
                } else response
            }
            .build()
    }

    @Provides @Singleton fun anilist(c: OkHttpClient, j: Json) = AniListApi(c, j)
    @Provides @Singleton fun jikan(c: OkHttpClient, j: Json) = JikanApi(c, j)
    @Provides @Singleton fun kitsu(c: OkHttpClient, j: Json) = KitsuApi(c, j)
    @Provides @Singleton fun shiki(c: OkHttpClient, j: Json) = ShikimoriApi(c, j)
    @Provides @Singleton fun traceMoe(c: OkHttpClient, j: Json) = TraceMoeApi(c, j)
    @Provides @Singleton fun livechart(c: OkHttpClient, j: Json) = LiveChartApi(c, j)
    @Provides @Singleton fun animesched(c: OkHttpClient, j: Json) = AnimeScheduleApi(c, j)
    @Provides @Singleton fun waifu(c: OkHttpClient, j: Json) = WaifuApi(c, j)
    @Provides @Singleton fun mangadex(c: OkHttpClient, j: Json) = MangaDexApi(c, j)
    @Provides @Singleton fun simkl(c: OkHttpClient, j: Json) = SimklApi(c, j)

    @Provides @Singleton fun db(@ApplicationContext ctx: Context) = Room.databaseBuilder(ctx, AppDatabase::class.java, "rei.db")
        .addMigrations(MIGRATION_3_4)
        .fallbackToDestructiveMigration()
        .build()

    @Provides fun animeDao(db: AppDatabase) = db.animeDao()
    @Provides fun trackingDao(db: AppDatabase) = db.trackingDao()
    @Provides fun episodeDao(db: AppDatabase) = db.episodeDao()
    @Provides fun economyDao(db: AppDatabase) = db.economyDao()

    @Provides @Singleton fun repo(
        a: AniListApi, j: JikanApi, k: KitsuApi, s: ShikimoriApi,
        tr: TraceMoeApi, lc: LiveChartApi, asched: AnimeScheduleApi, wf: WaifuApi,
        mdx: MangaDexApi, sk: SimklApi,
        d: AppDatabase, eco: EconomyManager,
        sync: SyncService, malAuth: com.rei.app.data.remote.jikan.MALAuth,
        malList: com.rei.app.data.remote.jikan.MALListApi,
        anilistAuth: com.rei.app.data.remote.anilist.AniListAuth
    ) = AnimeRepository(a, j, k, s, tr, lc, asched, wf, mdx, sk, d, eco, sync, malAuth, malList, anilistAuth)

    @Provides @Singleton fun dataStore(@ApplicationContext ctx: Context): DataStore<Preferences> = ctx.dataStore
}
