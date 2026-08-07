package com.rei.app.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rei.app.data.remote.anilist.AniListAuth
import com.rei.app.data.remote.jikan.MALAuth
import com.rei.app.data.repository.AnimeRepository
import com.rei.app.ui.theme.*
import com.rei.app.util.BackupUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val tvm: ThemeViewModel,
    private val repo: AnimeRepository,
    private val backupUtil: BackupUtil,
    private val anilistAuth: AniListAuth,
    private val malAuth: MALAuth
) : ViewModel() {
    val config: StateFlow<ReiConfig> = tvm.themeState.map { it.config }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReiConfig())

    // Auth state flows
    val anilistLoggedIn: StateFlow<Boolean> = anilistAuth.accessToken.map { it != null }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val malUserName: StateFlow<String?> = malAuth.userName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun update(transform: (ReiConfig) -> ReiConfig) = tvm.update(transform)

    // ═══ Auth ═══
    fun isMalConfigured(): Boolean = malAuth.isConfigured()

    fun launchAnilistAuth(context: Context) {
        anilistAuth.launchAuth(context)
    }

    fun launchMalAuth(context: Context) {
        malAuth.launchAuth(context)
    }

    fun logoutAnilist() {
        viewModelScope.launch { anilistAuth.logout() }
    }

    fun logoutMal() {
        viewModelScope.launch { malAuth.logout() }
    }

    // ═══ Sync ═══
    fun fullSync(onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = repo.fullSync()
                onResult("Synced: ${result?.anilistPushed ?: 0} pushed, ${result?.anilistPulled ?: 0} pulled")
            } catch (e: Exception) {
                onResult("Sync failed: ${e.message}")
            }
        }
    }

    fun importFromMal(onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val count = repo.importFromMAL()
                onResult("Imported $count entries from MAL")
            } catch (e: Exception) {
                onResult("Import failed: ${e.message}")
            }
        }
    }

    // ═══ Backup ═══
    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = backupUtil.export()
                onResult(json)
            } catch (e: Exception) {
                onResult("")
            }
        }
    }

    fun importBackup(json: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val count = backupUtil.import(json)
                onResult("Restored $count tracking entries")
            } catch (e: Exception) {
                onResult("Import failed: ${e.message}")
            }
        }
    }

    fun previewBackup(json: String): BackupUtil.ReiBackup? = backupUtil.preview(json)

    // ═══ Reset ═══
    fun resetAllSettings() {
        tvm.update { ReiConfig() }
    }
}
