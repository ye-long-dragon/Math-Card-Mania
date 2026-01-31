package com.example.baraclan.mentalchallengemath_namepending.models

import com.example.baraclan.mentalchallengemath_namepending.models.card

class deck(name: String) : CardContainer(name) {
    // Deck-specific methods can go here
    fun shuffle() {
        println("$name: Shuffling the deck...")
        // In a real implementation, you'd convert the map to a list of individual cards, shuffle, then rebuild
        // For simplicity, we'll just print for now.
    }

    fun drawCard(): card? {
        if (isEmpty()) {
            println("$name: Deck is empty, cannot draw.")
            return null
        }
        // A simple "draw" would be to pick a random card, remove one, and return it.
        val randomCardEntry = cards.entries.randomOrNull() ?: return null
        val cardToDraw = randomCardEntry.key
        removeCard(cardToDraw, 1) // Remove one instance of the drawn card
        println("$name: Drew 1 x ${cardToDraw.name}.")
        return cardToDraw
    }
}
