package com.example.baraclan.mentalchallengemath_namepending.models

import android.content.Context
import android.media.SoundPool
import com.example.baraclan.mentalchallengemath_namepending.R

object SoundManager {
    private var soundPool: SoundPool? = null
    private val placeSounds = mutableListOf<Int>() // card-place-1 to 4
    private val slideSounds = mutableListOf<Int>() // card-slide-1 to 8
    private var loaded = false

    fun init(context: Context) {
        if (loaded) return
        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .build()

        val sp = soundPool!!
        placeSounds.clear()
        slideSounds.clear()

        // Load card-place-1 through card-place-4
        placeSounds.add(sp.load(context, R.raw.card_place_1, 1))
        placeSounds.add(sp.load(context, R.raw.card_place_2, 1))
        placeSounds.add(sp.load(context, R.raw.card_place_3, 1))
        placeSounds.add(sp.load(context, R.raw.card_place_4, 1))

        // Load card-slide-1 through card-slide-8
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

    fun playPlace() {
        val sp = soundPool ?: return
        val sound = placeSounds.randomOrNull() ?: return
        sp.play(sound, 1f, 1f, 1, 0, 1f)
    }

    fun playSlide() {
        val sp = soundPool ?: return
        val sound = slideSounds.randomOrNull() ?: return
        sp.play(sound, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        placeSounds.clear()
        slideSounds.clear()
        loaded = false
    }
}