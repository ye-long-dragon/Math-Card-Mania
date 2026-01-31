package com.example.baraclan.mentalchallengemath_namepending.models

import java.lang.IllegalArgumentException // For error handling
import com.example.baraclan.mentalchallengemath_namepending.models.*

// Requires ICardContainer and cardGame
abstract class cardContainer(override val name: String) : iCardContainer {
    private val cards: MutableMap<cardGame, Int> = mutableMapOf()

    override fun addCard(card: cardGame, count: Int) {
        require(count > 0) { "Count must be positive to add cards." }
        cards[card] = (cards[card] ?: 0) + count
    }

    override fun removeCard(card: cardGame, count: Int) {
        require(count > 0) { "Count must be positive to remove cards." }
        val currentCount = cards[card] ?: 0
        require(currentCount >= count) { "Not enough ${card.name} in $name to remove $count. Current: $currentCount" }

        if (currentCount == count) {
            cards.remove(card)
        } else {
            cards[card] = currentCount - count
        }
    }

    override fun getCardCount(card: cardGame): Int {
        return cards[card] ?: 0
    }

    override fun getTotalCount(): Int {
        return cards.values.sum()
    }

    override fun getUniqueCardTypesCount(): Int {
        return cards.keys.size
    }

    override fun getAllCardsWithCounts(): Map<cardGame, Int> {
        return cards.toMap()
    }

    override fun contains(card: cardGame): Boolean {
        return cards.containsKey(card)
    }

    override fun isEmpty(): Boolean {
        return cards.isEmpty()
    }

    override fun toString(): String {
        return "$name (${getTotalCount()} cards total, ${getUniqueCardTypesCount()} unique types)"
    }
}
