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

    // Returns a display string for the "slot" starting at index idx,
    // plus the next index after consuming it.
    fun slotStr(idx: Int): Pair<String, Int> {
        if (idx >= equation.size) return Pair("?", idx)
        val c = equation[idx]
        return when {
            c.isValue() -> Pair(c.displayLabel(), idx + 1)
            c.isPrefixFunction() -> {
                val fnName = when (c.operator) {
                    Operator.SIN -> "sin"; Operator.COS -> "cos"
                    Operator.LN -> "ln";  Operator.LOG10 -> "log₁₀"; else -> "?"
                }
                val (argStr, next) = slotStr(idx + 1)
                Pair("$fnName($argStr)", next)
            }
            c.type == cardType.FRACTION -> {
                val (numStr, a2) = slotStr(idx + 1)
                val (denStr, a3) = slotStr(a2)
                Pair("($numStr/$denStr)", a3)
            }
            c.type == cardType.EXPONENT -> {
                val (baseStr, a2) = slotStr(idx + 1)
                val (expStr,  a3) = slotStr(a2)
                Pair("$baseStr^$expStr", a3)
            }
            c.isLeftParen() -> {
                // Collect interior up to matching ')'
                var depth = 1; var j = idx + 1
                while (j < equation.size && depth > 0) {
                    when { equation[j].isLeftParen() -> depth++; equation[j].isRightParen() -> depth-- }
                    j++
                }
                val interior = equation.subList(idx + 1, j - 1)
                val innerStr = if (interior.isEmpty()) "" else getEquationString(interior)
                Pair("($innerStr)", j)
            }
            else -> Pair(c.displayLabel(), idx + 1)
        }
    }

    while (i < equation.size) {
        val card = equation[i]
        when (card.type) {
            cardType.NUMBER    -> { sb.append(card.numberValue); i++ }
            cardType.VARIABLE  -> { sb.append(card.displayLabel()); i++ }
            cardType.CONSTANT  -> { sb.append(card.displayLabel()); i++ }
            cardType.PARENTHESIS -> { sb.append(card.displayLabel()); i++ }

            cardType.OPERATOR -> {
                sb.append(when (card.operator) {
                    Operator.ADD -> " + "; Operator.SUBTRACT -> " − "
                    Operator.MULTIPLY -> " × "; Operator.DIVIDE -> " ÷ "; else -> " ? "
                }); i++
            }

            cardType.FUNCTION -> {
                val fnName = when (card.operator) {
                    Operator.SIN -> "sin"; Operator.COS -> "cos"
                    Operator.LN -> "ln";  Operator.LOG10 -> "log₁₀"; else -> "?"
                }
                val (argStr, next) = slotStr(i + 1)
                sb.append("$fnName($argStr)")
                i = next
            }

            cardType.FRACTION -> {
                val (numStr, a2) = slotStr(i + 1)
                val (denStr, a3) = slotStr(a2)
                sb.append("($numStr/$denStr)")
                i = a3
            }

            cardType.EXPONENT -> {
                val (baseStr, a2) = slotStr(i + 1)
                val (expStr,  a3) = slotStr(a2)
                sb.append("$baseStr^$expStr")
                i = a3
            }
        }
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