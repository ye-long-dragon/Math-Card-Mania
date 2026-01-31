package com.example.baraclan.mentalchallengemath_namepending.models


// Requires cardContainer and cardGame
class collection(name: String) : cardContainer(name) {
    fun filterByAttribute(attribute: String): Map<cardGame, Int> {
        // Actual filtering logic
        return getAllCardsWithCounts().filterKeys { it.description.contains(attribute, ignoreCase = true) }
    }
}

