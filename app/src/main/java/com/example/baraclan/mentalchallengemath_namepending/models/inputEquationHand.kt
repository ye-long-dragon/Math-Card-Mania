package com.example.baraclan.mentalchallengemath_namepending.models

// Requires cardGame, CardType, Operator, PemdasEvaluator
class inputEquationHand(val name: String) {
    private val equationCards: MutableList<cardGame> = mutableListOf()

    val currentEquation: List<cardGame>
        get() = equationCards.toList()

    val currentExpressionString: String
        get() = equationCards.joinToString(" ") {
            when (it.type) {
                cardType.NUMBER -> it.numberValue.toString()
                cardType.OPERATOR -> when (it.operator) {
                    Operator.ADD -> "+"
                    Operator.SUBTRACT -> "-"
                    Operator.MULTIPLY -> "*"
                    Operator.DIVIDE -> "/"
                    null -> "?"
                }
            }
        }

    fun addCard(card: cardGame) {
        if (equationCards.isEmpty()) {
            require(card.type == cardType.NUMBER) { "Equation must start with a number card." }
        } else {
            val lastCard = equationCards.last()
            require(lastCard.type != card.type) { "Cannot add two ${card.type.name}s consecutively." }
        }
        equationCards.add(card)
    }

    fun removeLastCard(): cardGame? {
        if (equationCards.isNotEmpty()) {
            return equationCards.removeAt(equationCards.lastIndex)
        }
        return null
    }

    fun clearEquation() {
        equationCards.clear()
    }

    fun evaluateEquation(): Int {
        require(equationCards.isNotEmpty()) { "No cards in equation to evaluate." }
        require(equationCards.last().type != cardType.OPERATOR) { "Equation cannot end with an operator." }
        return PemdasEvaluator.evaluate(equationCards)
    }
}