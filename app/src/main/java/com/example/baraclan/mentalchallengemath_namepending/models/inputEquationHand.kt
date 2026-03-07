package com.example.baraclan.mentalchallengemath_namepending.models

import com.example.baraclan.mentalchallengemath_namepending.scripts.formatDouble
import com.example.baraclan.mentalchallengemath_namepending.scripts.getEquationString

class inputEquationHand(val name: String) {
    private val equationCards: MutableList<cardGame> = mutableListOf()

    val currentEquation: List<cardGame>
        get() = equationCards.toList()

    // Uses getEquationString from gameLogic which handles all card types
    val currentExpressionString: String
        get() = getEquationString(equationCards)

    fun addCard(card: cardGame) {
        // Relaxed validation — PemdasEvaluator handles structural errors on evaluate.
        // Only enforce: can't start with a binary operator.
        if (equationCards.isEmpty()) {
            require(card.type != cardType.OPERATOR) {
                "Equation must not start with an operator."
            }
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

    // Returns Double now that PemdasEvaluator returns Double
    fun evaluateEquation(): Double {
        require(equationCards.isNotEmpty()) { "No cards in equation to evaluate." }
        require(equationCards.last().type != cardType.OPERATOR) {
            "Equation cannot end with an operator."
        }
        return PemdasEvaluator.evaluate(equationCards)
    }

    // Safe version — returns null instead of throwing
    fun tryEvaluate(): Double? {
        return try { evaluateEquation() } catch (e: Exception) { null }
    }

    // Formatted result string — shows "3.1416" not "3.141592653589793"
    fun evaluateForDisplay(): String? {
        return tryEvaluate()?.let { formatDouble(it) }
    }

    fun isEmpty() = equationCards.isEmpty()
    fun size() = equationCards.size
}