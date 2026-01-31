package com.example.baraclan.mentalchallengemath_namepending.models

// Requires cardGame definition
interface iCardContainer {
    val name: String

    fun addCard(card: cardGame, count: Int = 1)
    fun removeCard(card: cardGame, count: Int = 1)
    fun getCardCount(card: cardGame): Int
    fun getTotalCount(): Int
    fun getUniqueCardTypesCount(): Int
    fun getAllCardsWithCounts(): Map<cardGame, Int>
    fun contains(card: cardGame): Boolean
    fun isEmpty(): Boolean
}