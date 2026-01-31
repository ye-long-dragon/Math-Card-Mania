package com.example.baraclan.mentalchallengemath_namepending.models

data class cardGame(
    val id: String,
    val name: String,
    val description: String = "",
    val type: cardType,
    val numberValue: Int? = null,
    val operator: Operator? = null
) {
    init {
        when (type) {
            cardType.NUMBER -> require(numberValue != null && operator == null) {
                "Number cards must have a numberValue and no operator."
            }
            cardType.OPERATOR -> require(operator != null && numberValue == null) {
                "Operator cards must have an operator and no numberValue."
            }
        }
    }

    override fun toString(): String {
        return when (type) {
            cardType.NUMBER -> "$name ($numberValue)"
            cardType.OPERATOR -> "$name (${when(operator) {
                Operator.ADD -> "+"
                Operator.SUBTRACT -> "-"
                Operator.MULTIPLY -> "*"
                Operator.DIVIDE -> "/"
                null -> "?"
            }})"
        }
    }
}