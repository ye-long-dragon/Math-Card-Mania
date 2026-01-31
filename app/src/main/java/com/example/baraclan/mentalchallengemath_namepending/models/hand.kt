package com.example.baraclan.mentalchallengemath_namepending.models

// Requires cardContainer and cardGame
class hand(name: String, private val maxCapacity: Int = 8) : cardContainer(name) {

    init {
        require(maxCapacity > 0) { "Hand capacity must be positive." }
    }

    override fun addCard(card: cardGame, count: Int) {
        require(count > 0) { "Count must be positive to add cards." }

        val currentTotal = getTotalCount()
        if (currentTotal + count > maxCapacity) {
            val canAdd = maxCapacity - currentTotal
            if (canAdd > 0) {
                super.addCard(card, canAdd)
                // Note: If 'count' was more than 'canAdd', some cards are implicitly 'lost'
                // This behavior might need refinement based on exact game rules.
            } else {
                // Hand is full, cannot add any cards
            }
        } else {
            super.addCard(card, count)
        }
    }

    fun playCard(card: cardGame): cardGame? {
        if (getCardCount(card) > 0) {
            removeCard(card, 1)
            return card
        }
        return null
    }

    override fun toString(): String {
        return "$name (${getTotalCount()}/$maxCapacity cards in hand)"
    }
}