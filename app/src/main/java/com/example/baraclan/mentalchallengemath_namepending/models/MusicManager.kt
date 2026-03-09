package com.example.baraclan.mentalchallengemath_namepending.models

import android.content.Context
import android.media.MediaPlayer
import com.example.baraclan.mentalchallengemath_namepending.R

object MusicManager {
    private var mediaPlayer: MediaPlayer? = null
    private var masterVolume: Float = 1.0f
    private var musicVolume: Float = 0.5f
    private var musicEnabled: Boolean = true

    fun start(context: Context) {
        if (mediaPlayer != null) return
        mediaPlayer = MediaPlayer.create(context, R.raw.background_music).apply {
            isLooping = true
            applyVolume()
            start()
        }
    }

    fun pause() { mediaPlayer?.pause() }

    fun resume() {
        if (musicEnabled && mediaPlayer?.isPlaying == false) mediaPlayer?.start()
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun applySettings(settings: AudioSettings) {
        musicEnabled = settings.musicEnabled
        masterVolume = settings.masterVolume
        musicVolume = settings.musicVolume
        applyVolume()
        if (!musicEnabled) mediaPlayer?.pause()
        else if (mediaPlayer?.isPlaying == false) mediaPlayer?.start()
    }

    private fun applyVolume() {
        val vol = (masterVolume * musicVolume).coerceIn(0f, 1f)
        mediaPlayer?.setVolume(vol, vol)
    }
}