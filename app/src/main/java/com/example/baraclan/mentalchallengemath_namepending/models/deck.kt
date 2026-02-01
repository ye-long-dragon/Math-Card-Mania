package com.example.baraclan.mentalchallengemath_namepending.models

import com.example.baraclan.mentalchallengemath_namepending.models.*

class deck(name: String) : cardContainer(name) {
    // Secondary constructor to create a deck from a map of existing cards
    constructor(name: String, initialCards: Map<cardGame, Int>) : this(name) {
        initialCards.forEach { (card, count) ->
            addCard(card, count)
        }
    }

    fun shuffle() {
        // Actual shuffling logic would go here
    }

    fun drawCard(): cardGame? {
        if (isEmpty()) {
            return null
        }
        // To draw, we need to convert the map to a list to pick one randomly
        val allCards = getAllCardsAsList()
        if (allCards.isEmpty()) return null

        val randomCard = allCards.random()
        removeCard(randomCard, 1) // Remove one instance of that specific card type
        return randomCard
    }
}
