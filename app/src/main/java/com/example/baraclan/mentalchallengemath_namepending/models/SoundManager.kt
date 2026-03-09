package com.example.baraclan.mentalchallengemath_namepending.models

import android.content.Context
import android.media.SoundPool
import com.example.baraclan.mentalchallengemath_namepending.R

object SoundManager {
    private var soundPool: SoundPool? = null
    private val placeSounds = mutableListOf<Int>()
    private val slideSounds = mutableListOf<Int>()
    private var loaded = false
    private var masterVolume: Float = 1.0f
    private var sfxVolume: Float = 1.0f
    private var sfxEnabled: Boolean = true

    fun init(context: Context) {
        if (loaded) return
        soundPool = SoundPool.Builder().setMaxStreams(4).build()
        val sp = soundPool!!
        placeSounds.add(sp.load(context, R.raw.card_place_1, 1))
        placeSounds.add(sp.load(context, R.raw.card_place_2, 1))
        placeSounds.add(sp.load(context, R.raw.card_place_3, 1))
        placeSounds.add(sp.load(context, R.raw.card_place_4, 1))
        slideSounds.add(sp.load(context, R.raw.card_slide_1, 1))
        slideSounds.add(sp.load(context, R.raw.card_slide_2, 1))
        slideSounds.add(sp.load(context, R.raw.card_slide_3, 1))
        slideSounds.add(sp.load(context, R.raw.card_slide_4, 1))
        slideSounds.add(sp.load(context, R.raw.card_slide_5, 1))
        slideSounds.add(sp.load(context, R.raw.card_slide_6, 1))
        slideSounds.add(sp.load(context, R.raw.card_slide_7, 1))
        slideSounds.add(sp.load(context, R.raw.card_slide_8, 1))
        loaded = true
    }

    fun applySettings(settings: AudioSettings) {
        sfxEnabled = settings.sfxEnabled
        masterVolume = settings.masterVolume
        sfxVolume = settings.sfxVolume
    }

    fun playPlace() {
        if (!sfxEnabled) return
        val vol = (masterVolume * sfxVolume).coerceIn(0f, 1f)
        soundPool?.play(placeSounds.randomOrNull() ?: return, vol, vol, 1, 0, 1f)
    }

    fun playSlide() {
        if (!sfxEnabled) return
        val vol = (masterVolume * sfxVolume).coerceIn(0f, 1f)
        soundPool?.play(slideSounds.randomOrNull() ?: return, vol, vol, 1, 0, 1f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        placeSounds.clear()
        slideSounds.clear()
        loaded = false
    }
}