package com.example.baraclan.mentalchallengemath_namepending.models

import com.example.baraclan.mentalchallengemath_namepending.models.*

class deck(name: String) : cardContainer(name) {
    fun shuffle() {
        // Actual shuffling logic would go here
    }

    fun drawCard(): cardGame? {
        if (isEmpty()) {
            return null
        }
        val randomCardEntry = getAllCardsWithCounts().entries.randomOrNull() ?: return null
        val cardToDraw = randomCardEntry.key
        removeCard(cardToDraw, 1)
        return cardToDraw
    }
}