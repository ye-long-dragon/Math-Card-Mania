package com.example.baraclan.mentalchallengemath_namepending.models

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.audioDataStore by preferencesDataStore(name = "audio_settings")

data class AudioSettings(
    val musicEnabled: Boolean = true,
    val sfxEnabled: Boolean = true,
    val masterVolume: Float = 1.0f,
    val musicVolume: Float = 0.5f,
    val sfxVolume: Float = 1.0f
)

object AudioSettingsManager {
    private val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
    private val SFX_ENABLED = booleanPreferencesKey("sfx_enabled")
    private val MASTER_VOLUME = floatPreferencesKey("master_volume")
    private val MUSIC_VOLUME = floatPreferencesKey("music_volume")
    private val SFX_VOLUME = floatPreferencesKey("sfx_volume")

    fun getSettingsFlow(context: Context): Flow<AudioSettings> =
        context.audioDataStore.data.map { prefs ->
            AudioSettings(
                musicEnabled = prefs[MUSIC_ENABLED] ?: true,
                sfxEnabled = prefs[SFX_ENABLED] ?: true,
                masterVolume = prefs[MASTER_VOLUME] ?: 1.0f,
                musicVolume = prefs[MUSIC_VOLUME] ?: 0.5f,
                sfxVolume = prefs[SFX_VOLUME] ?: 1.0f
            )
        }

    suspend fun save(context: Context, settings: AudioSettings) {
        context.audioDataStore.edit { prefs ->
            prefs[MUSIC_ENABLED] = settings.musicEnabled
            prefs[SFX_ENABLED] = settings.sfxEnabled
            prefs[MASTER_VOLUME] = settings.masterVolume
            prefs[MUSIC_VOLUME] = settings.musicVolume
            prefs[SFX_VOLUME] = settings.sfxVolume
        }
    }
}