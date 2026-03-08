package com.example.baraclan.mentalchallengemath_namepending.models

import android.content.Context
import android.media.MediaPlayer
import com.example.baraclan.mentalchallengemath_namepending.R

object MusicManager {
    private var mediaPlayer: MediaPlayer? = null

    fun start(context: Context) {
        if (mediaPlayer != null) return  // already playing
        mediaPlayer = MediaPlayer.create(context, R.raw.background_music).apply {
            isLooping = true
            setVolume(0.5f, 0.5f)
            start()
        }
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun resume() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}