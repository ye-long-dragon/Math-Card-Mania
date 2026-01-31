package com.example.baraclan.mentalchallengemath_namepending.models

import java.lang.IllegalArgumentException // For error handling
import com.example.baraclan.mentalchallengemath_namepending.models.card

abstract class CardContainer(val name: String) {
    protected val cards: MutableMap<card, Int> = mutableMapOf()

    /**
     * Adds a specified count of a card to this container.
     */
    open fun addCard(card: card, count: Int = 1) {
        require(count > 0) { "Count must be positive to add cards." }
        cards[card] = (cards[card] ?: 0) + count
        println("$name: Added $count x ${card.name}. Total: ${getCardCount(card)}")
    }

    /**
     * Removes a specified count of a card from this container.
     * Throws an IllegalArgumentException if not enough cards are present.
     */
    open fun removeCard(card: card, count: Int = 1) {
        require(count > 0) { "Count must be positive to remove cards." }
        val currentCount = cards[card] ?: 0
        require(currentCount >= count) { "Not enough ${card.name} in $name to remove $count. Current: $currentCount" }

        if (currentCount == count) {
            cards.remove(card)
        } else {
            cards[card] = currentCount - count
        }
        println("$name: Removed $count x ${card.name}. Remaining: ${getCardCount(card)}")
    }

    /**
     * Gets the current count of a specific card in this container.
     */
    fun getCardCount(card: card): Int {
        return cards[card] ?: 0
    }

    /**
     * Gets the total number of individual cards (including duplicates).
     */
    fun getTotalCount(): Int {
        return cards.values.sum()
    }

    /**
     * Gets the number of unique card types in this container.
     */
    fun getUniqueCardTypesCount(): Int {
        return cards.keys.size
    }

    /**
     * Returns an immutable map of all cards and their counts in this container.
     */
    fun getAllCardsWithCounts(): Map<card, Int> {
        return cards.toMap() // Returns a copy to prevent external modification
    }

    /**
     * Checks if the container contains at least one of the specified card.
     */
    fun contains(card: card): Boolean {
        return cards.containsKey(card)
    }

    /**
     * Checks if the container is empty.
     */
    fun isEmpty(): Boolean {
        return cards.isEmpty()
    }

    override fun toString(): String {
        return "$name (${getTotalCount()} cards total, ${getUniqueCardTypesCount()} unique types)"
    }
}
