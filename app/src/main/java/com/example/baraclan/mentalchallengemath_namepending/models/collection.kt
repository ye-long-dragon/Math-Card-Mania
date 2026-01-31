package com.example.baraclan.mentalchallengemath_namepending.models

class collection(name: String) : CardContainer(name) {
    // Collection-specific methods can go here, e.g., filtering by attributes, sorting.
    fun filterByAttribute(attribute: String): Map<card, Int> {
        println("$name: Filtering by attribute: $attribute (Placeholder)")
        return cards.filterKeys { it.description.contains(attribute, ignoreCase = true) }
    }
}
