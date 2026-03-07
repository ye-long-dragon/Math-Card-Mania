package com.example.baraclan.mentalchallengemath_namepending.models

data class cardGame(
    val id: String,
    val name: String,
    val description: String = "",
    val type: cardType,

    // Used by NUMBER cards
    val numberValue: Int? = null,

    // Used by OPERATOR, FUNCTION, CONSTANT, FRACTION, EXPONENT cards
    val operator: Operator? = null,

    // Used by FRACTION and EXPONENT cards to hold their two input slots.
    // These are filled in when the player places number cards into the slots.
    val slotA: Double? = null,   // numerator / base
    val slotB: Double? = null    // denominator / exponent
) {
    init {
        when (type) {
            cardType.NUMBER -> require(numberValue != null && operator == null) {
                "NUMBER cards must have numberValue and no operator."
            }
            cardType.OPERATOR -> require(
                operator in listOf(Operator.ADD, Operator.SUBTRACT, Operator.MULTIPLY, Operator.DIVIDE)
                        && numberValue == null
            ) { "OPERATOR cards must use ADD/SUBTRACT/MULTIPLY/DIVIDE and have no numberValue." }

            cardType.FUNCTION -> require(
                operator in listOf(Operator.SIN, Operator.COS, Operator.LN, Operator.LOG10)
                        && numberValue == null
            ) { "FUNCTION cards must use SIN/COS/LN/LOG10." }

            cardType.CONSTANT -> require(
                operator in listOf(Operator.PI, Operator.EULER)
                        && numberValue == null
            ) { "CONSTANT cards must use PI or EULER." }

            cardType.FRACTION -> require(operator == Operator.FRACTION && numberValue == null) {
                "FRACTION cards must use FRACTION operator."
            }

            cardType.EXPONENT -> require(operator == Operator.POWER && numberValue == null) {
                "EXPONENT cards must use POWER operator."
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    // Returns the numeric value this card resolves to (for CONSTANT, NUMBER).
    // Returns null for cards that need context (OPERATOR, FUNCTION, FRACTION, EXPONENT).
    fun resolvedValue(): Double? = when (type) {
        cardType.NUMBER -> numberValue?.toDouble()
        cardType.CONSTANT -> when (operator) {
            Operator.PI -> Math.PI
            Operator.EULER -> Math.E
            else -> null
        }
        else -> null
    }

    // Whether this card is a "value" (can appear where a number is expected)
    fun isValue(): Boolean = type == cardType.NUMBER || type == cardType.CONSTANT

    // Whether this card is a binary infix operator (+, -, *, /)
    fun isBinaryOperator(): Boolean = type == cardType.OPERATOR

    // Whether this card is a prefix function (sin, cos, ln, log10)
    fun isPrefixFunction(): Boolean = type == cardType.FUNCTION

    // Whether this card needs two number cards after it (fraction/exponent)
    fun isTwoSlot(): Boolean = type == cardType.FRACTION || type == cardType.EXPONENT

    override fun toString(): String = when (type) {
        cardType.NUMBER -> "Number($numberValue)"
        cardType.OPERATOR -> when (operator) {
            Operator.ADD -> "+"
            Operator.SUBTRACT -> "−"
            Operator.MULTIPLY -> "×"
            Operator.DIVIDE -> "÷"
            else -> "?"
        }
        cardType.FUNCTION -> when (operator) {
            Operator.SIN -> "sin"
            Operator.COS -> "cos"
            Operator.LN -> "ln"
            Operator.LOG10 -> "log₁₀"
            else -> "?"
        }
        cardType.CONSTANT -> when (operator) {
            Operator.PI -> "π"
            Operator.EULER -> "e"
            else -> "?"
        }
        cardType.FRACTION -> "Fraction(${slotA ?: "?"}/${slotB ?: "?"})"
        cardType.EXPONENT -> "Exp(${slotA ?: "?"}^${slotB ?: "?"})"
    }
}