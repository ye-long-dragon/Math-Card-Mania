package com.example.baraclan.mentalchallengemath_namepending.scripts

import com.example.baraclan.mentalchallengemath_namepending.models.*
import kotlin.random.Random

// ─────────────────────────────────────────────────────────────
// transferCards
// ─────────────────────────────────────────────────────────────
fun transferCards(card: cardGame, count: Int, from: cardContainer, to: cardContainer) {
    require(count > 0) { "Count must be positive to transfer cards." }
    require(from.getCardCount(card) >= count) {
        "Cannot transfer $count x ${card.name} from ${from.name}. Only ${from.getCardCount(card)} available."
    }
    from.removeCard(card, count)
    to.addCard(card, count)
}

// ─────────────────────────────────────────────────────────────
// RandomHand — draws 8 cards from the deck
// ─────────────────────────────────────────────────────────────
fun RandomHand(Deck: deck): hand {
    val randomHand = hand("Player's Current Hand")
    val handCapacity = 8

    if (Deck.isEmpty()) return randomHand

    val allAvailableCardsInDeck = mutableListOf<cardGame>()
    Deck.cards.forEach { (card, count) ->
        repeat(count) { allAvailableCardsInDeck.add(card) }
    }

    if (allAvailableCardsInDeck.isEmpty()) return randomHand

    repeat(handCapacity) {
        if (allAvailableCardsInDeck.isEmpty()) return@repeat
        val randomIndex = Random.nextInt(allAvailableCardsInDeck.size)
        val drawnCard = allAvailableCardsInDeck[randomIndex]
        randomHand.addCard(drawnCard, 1)
        allAvailableCardsInDeck.removeAt(randomIndex)

        val currentCountInDeck = Deck.cards[drawnCard] ?: 0
        if (currentCountInDeck > 1) Deck.cards[drawnCard] = currentCountInDeck - 1
        else Deck.cards.remove(drawnCard)
    }

    return randomHand
}

// ─────────────────────────────────────────────────────────────
// evaluateEquation — returns Double (was Int)
// Safe wrapper around PemdasEvaluator; returns null on failure
// ─────────────────────────────────────────────────────────────
fun evaluateEquation(equation: List<cardGame>): Double? {
    if (equation.isEmpty()) return null
    return try {
        PemdasEvaluator.evaluate(equation)
    } catch (e: Exception) {
        null
    }
}

// ─────────────────────────────────────────────────────────────
// evaluateEquationRounded — rounds to 4 decimal places
// ─────────────────────────────────────────────────────────────
fun evaluateEquationRounded(equation: List<cardGame>): Double? {
    if (equation.isEmpty()) return null
    return try {
        PemdasEvaluator.evaluateRounded(equation)
    } catch (e: Exception) {
        null
    }
}

// ─────────────────────────────────────────────────────────────
// getEquationString — human-readable equation display
// Handles all card types including FUNCTION, CONSTANT,
// FRACTION, EXPONENT
// ─────────────────────────────────────────────────────────────
fun getEquationString(equation: List<cardGame>): String {
    val sb = StringBuilder()
    var i = 0
    while (i < equation.size) {
        val card = equation[i]
        when (card.type) {
            cardType.NUMBER -> sb.append(card.numberValue)

            cardType.OPERATOR -> sb.append(
                when (card.operator) {
                    Operator.ADD -> " + "
                    Operator.SUBTRACT -> " − "
                    Operator.MULTIPLY -> " × "
                    Operator.DIVIDE -> " ÷ "
                    else -> " ? "
                }
            )

            cardType.CONSTANT -> sb.append(
                when (card.operator) {
                    Operator.PI -> "π"
                    Operator.EULER -> "e"
                    else -> "?"
                }
            )

            cardType.FUNCTION -> {
                val fnName = when (card.operator) {
                    Operator.SIN -> "sin"
                    Operator.COS -> "cos"
                    Operator.LN -> "ln"
                    Operator.LOG10 -> "log₁₀"
                    else -> "?"
                }
                // Peek at next card to show sin(x) style
                val nextCard = equation.getOrNull(i + 1)
                if (nextCard != null && nextCard.isValue()) {
                    sb.append("$fnName(${nextCard.resolvedValue()?.let { formatDouble(it) } ?: "?"})")
                    i += 2
                    continue
                } else {
                    sb.append("$fnName(?)")
                }
            }

            cardType.FRACTION -> {
                val num = equation.getOrNull(i + 1)
                val den = equation.getOrNull(i + 2)
                val numStr = num?.resolvedValue()?.let { formatDouble(it) } ?: "?"
                val denStr = den?.resolvedValue()?.let { formatDouble(it) } ?: "?"
                sb.append("($numStr/$denStr)")
                if (num != null && den != null) { i += 3; continue }
            }

            cardType.EXPONENT -> {
                val base = equation.getOrNull(i + 1)
                val exp = equation.getOrNull(i + 2)
                val baseStr = base?.resolvedValue()?.let { formatDouble(it) } ?: "?"
                val expStr = exp?.resolvedValue()?.let { formatDouble(it) } ?: "?"
                sb.append("$baseStr^$expStr")
                if (base != null && exp != null) { i += 3; continue }
            }
        }
        i++
    }
    return sb.toString()
}

// ─────────────────────────────────────────────────────────────
// formatDouble — show as Int if whole number, else 4dp
// ─────────────────────────────────────────────────────────────
fun formatDouble(v: Double): String {
    return if (v == kotlin.math.floor(v) && !v.isInfinite()) {
        v.toLong().toString()
    } else {
        "%.4f".format(v).trimEnd('0').trimEnd('.')
    }
}