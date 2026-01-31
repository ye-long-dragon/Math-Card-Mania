package com.example.baraclan.mentalchallengemath_namepending.models

data class card(val id: String, // A unique identifier for the card type (e.g., "fireball_spell")
                val name: String,
                val description: String = "",
                val attack: Int = 0,
                val defense: Int = 0,
                val cost: Int = 0)
{
    override fun toString(): String {
        return "$name (ID: $id)"
    }
}
