package com.rei.app.data.remote.anilist

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AniList OAuth2 flow using Custom Tabs.
 *
 * AniList OAuth docs: https://anilist.git1.co/doc/Auth
 * - Authorization URL: https://anilist.co/api/v2/oauth/authorize
 * - Token URL: https://anilist.co/api/v2/oauth/token
 * - No client secret required for mobile/SPA flows
 * - Scopes: none needed for read; use empty string
 */
@Singleton
class AniListAuth @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        const val CLIENT_ID = 0  // Set to your AniList client ID (register at anilist.co/settings/developer)
        const val REDIRECT_URI = "rei://anilist-auth-callback"
        const val AUTH_URL = "https://anilist.co/api/v2/oauth/authorize"
        const val TOKEN_URL = "https://anilist.co/api/v2/oauth/token"

        private val KEY_ACCESS_TOKEN = stringPreferencesKey("anilist_access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("anilist_refresh_token")
        private val KEY_USER_NAME = stringPreferencesKey("anilist_user_name")
        private val KEY_USER_ID = stringPreferencesKey("anilist_user_id")
    }

    /** Current access token, null if not logged in */
    val accessToken = dataStore.data.map { it[KEY_ACCESS_TOKEN] }

    /** Current user name */
    val userName = dataStore.data.map { it[KEY_USER_NAME] }

    /** Is the user currently authenticated? */
    suspend fun isLoggedIn(): Boolean = dataStore.data.map { it[KEY_ACCESS_TOKEN] != null }.first()

    /** Launch the OAuth flow via Custom Tabs */
    fun launchAuth(context: Context) {
        if (CLIENT_ID == 0) return  // Not configured
        val uri = Uri.parse(AUTH_URL).buildUpon()
            .appendQueryParameter("client_id", CLIENT_ID.toString())
            .appendQueryParameter("response_type", "token")
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .build()
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setStartAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            .build()
        customTabsIntent.launchUrl(context, uri)
    }

    /** Handle the OAuth callback — extract token from fragment */
    suspend fun handleCallback(uri: Uri) {
        // AniList returns token in fragment: rei://callback#access_token=xxx&token_type=Bearer
        val fragment = uri.fragment ?: return
        val params = fragment.split("&").associate {
            val (key, value) = it.split("=", limit = 2)
            key to value
        }
        val token = params["access_token"] ?: return
        saveToken(token)
    }

    /** Save the access token */
    private suspend fun saveToken(token: String) {
        dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = token
        }
    }

    /** Save user info after fetching viewer */
    suspend fun saveUserInfo(id: Int, name: String) {
        dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = id.toString()
            prefs[KEY_USER_NAME] = name
        }
    }

    /** Logout — clear all auth data */
    suspend fun logout() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
            prefs.remove(KEY_USER_NAME)
            prefs.remove(KEY_USER_ID)
        }
    }

    /** Check if a URI is our OAuth callback */
    fun isAuthCallback(uri: Uri): Boolean {
        return uri.scheme == "rei" && uri.host == "anilist-auth-callback"
    }
}
