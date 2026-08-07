package com.rei.app.data.remote.jikan

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MAL (MyAnimeList) OAuth2 flow via Custom Tabs.
 *
 * MAL OAuth docs: https://myanimelist.net/apiconfig/references/authorization
 * - Register client at: https://myanimelist.net/apiconfig/create
 * - Uses Authorization Code with PKCE
 * - Scopes: "read" for reading lists, "write" for updating lists
 */
@Singleton
class MALAuth @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        // Register your app at https://myanimelist.net/apiconfig/create
        var CLIENT_ID: String = ""
        var CLIENT_SECRET: String = ""
        const val REDIRECT_URI = "rei://mal-auth-callback"

        private const val AUTH_URL = "https://myanimelist.net/v1/oauth2/authorize"
        private const val TOKEN_URL = "https://myanimelist.net/v1/oauth2/token"

        private val KEY_ACCESS_TOKEN = stringPreferencesKey("mal_access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("mal_refresh_token")
        private val KEY_USER_NAME = stringPreferencesKey("mal_user_name")
        private val KEY_USER_ID = stringPreferencesKey("mal_user_id")
        private val KEY_CODE_VERIFIER = stringPreferencesKey("mal_code_verifier")

        // Temporary storage for PKCE code verifier (set before launching Custom Tab)
        private var pendingCodeVerifier: String? = null
    }

    /** Current access token */
    val accessToken = dataStore.data.map { it[KEY_ACCESS_TOKEN] }

    /** Current user name */
    val userName = dataStore.data.map { it[KEY_USER_NAME] }

    /** Is the user currently authenticated? */
    suspend fun isLoggedIn(): Boolean = dataStore.data.map { it[KEY_ACCESS_TOKEN] != null }.first()

    /** Is MAL configured (client ID set)? */
    fun isConfigured(): Boolean = CLIENT_ID.isNotEmpty()

    /** Launch the OAuth flow via Custom Tabs with PKCE */
    fun launchAuth(context: Context) {
        if (!isConfigured()) return
        val codeVerifier = generateCodeVerifier()
        pendingCodeVerifier = codeVerifier

        val uri = Uri.parse(AUTH_URL).buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("scope", "read,write")
            .appendQueryParameter("code_challenge", codeVerifier)
            .appendQueryParameter("code_challenge_method", "plain")
            .appendQueryParameter("state", "rei_mal_auth")
            .build()

        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setStartAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            .build()
        customTabsIntent.launchUrl(context, uri)
    }

    /** Handle the OAuth callback - exchange authorization code for token */
    suspend fun handleCallback(uri: Uri, okHttpClient: OkHttpClient): Boolean {
        val code = uri.getQueryParameter("code") ?: return false
        val state = uri.getQueryParameter("state")
        if (state != "rei_mal_auth") return false

        val codeVerifier = pendingCodeVerifier ?: return false

        val formBody = "client_id=$CLIENT_ID" +
            "&client_secret=$CLIENT_SECRET" +
            "&grant_type=authorization_code" +
            "&code=$code" +
            "&redirect_uri=${Uri.encode(REDIRECT_URI)}" +
            "&code_verifier=$codeVerifier"

        val request = Request.Builder()
            .url(TOKEN_URL)
            .post(formBody.toRequestBody("application/x-www-form-urlencoded".toMediaType()))
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string() ?: return false
            if (!response.isSuccessful) return false
            val json = Json.parseToJsonElement(body).jsonObject
            val accessTokenValue = json["access_token"]?.jsonPrimitive?.contentOrNull ?: return false
            val refreshTokenValue = json["refresh_token"]?.jsonPrimitive?.contentOrNull
            saveTokens(accessTokenValue, refreshTokenValue)
            // Also persist code verifier
            dataStore.edit { it[KEY_CODE_VERIFIER] = codeVerifier }
            return true
        } catch (_: Exception) { return false }
    }

    /** Save tokens */
    private suspend fun saveTokens(accessToken: String, refreshToken: String?) {
        dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            if (refreshToken != null) prefs[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    /** Save user info */
    suspend fun saveUserInfo(id: Int, name: String) {
        dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = id.toString()
            prefs[KEY_USER_NAME] = name
        }
    }

    /** Logout */
    suspend fun logout() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
            prefs.remove(KEY_USER_NAME)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_CODE_VERIFIER)
        }
    }

    /** Check if a URI is our OAuth callback */
    fun isAuthCallback(uri: Uri): Boolean {
        return uri.scheme == "rei" && uri.host == "mal-auth-callback"
    }

    /** Get access token for API calls */
    suspend fun getToken(): String? = dataStore.data.map { it[KEY_ACCESS_TOKEN] }.first()

    /** Refresh token if expired */
    suspend fun refreshToken(okHttpClient: OkHttpClient): Boolean {
        val refreshTokenValue = dataStore.data.map { it[KEY_REFRESH_TOKEN] }.first() ?: return false
        if (!isConfigured()) return false

        val formBody = "client_id=$CLIENT_ID" +
            "&client_secret=$CLIENT_SECRET" +
            "&grant_type=refresh_token" +
            "&refresh_token=$refreshTokenValue"

        val request = Request.Builder()
            .url(TOKEN_URL)
            .post(formBody.toRequestBody("application/x-www-form-urlencoded".toMediaType()))
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string() ?: return false
            if (!response.isSuccessful) return false
            val json = Json.parseToJsonElement(body).jsonObject
            val accessTokenValue = json["access_token"]?.jsonPrimitive?.contentOrNull ?: return false
            val newRefreshToken = json["refresh_token"]?.jsonPrimitive?.contentOrNull
            saveTokens(accessTokenValue, newRefreshToken)
            return true
        } catch (_: Exception) { return false }
    }

    private fun generateCodeVerifier(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
        return (1..64).map { chars.random() }.joinToString("")
    }
}
