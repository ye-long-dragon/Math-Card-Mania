package com.example.baraclan.mentalchallengemath_namepending.models

data class cardGame(
    val id: String,
    val name: String,
    val description: String = "",
    val type: cardType,

    // Used by NUMBER cards
    val numberValue: Int? = null,

    // Used by OPERATOR, FUNCTION, CONSTANT, FRACTION, EXPONENT, VARIABLE, PARENTHESIS cards
    val operator: Operator? = null,

    // Runtime-resolved value for VARIABLE cards (set by VariableState each goal/game)
    val variableValue: Double? = null,

    // Used by FRACTION and EXPONENT cards
    val slotA: Double? = null,
    val slotB: Double? = null
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

            cardType.VARIABLE -> require(
                operator in listOf(
                    Operator.VAR_A, Operator.VAR_B, Operator.VAR_C, Operator.VAR_D,
                    Operator.VAR_X, Operator.VAR_Y, Operator.VAR_Z
                ) && numberValue == null
            ) { "VARIABLE cards must use VAR_A..VAR_Z operators." }

            cardType.PARENTHESIS -> require(
                operator in listOf(Operator.LEFT_PAREN, Operator.RIGHT_PAREN)
                        && numberValue == null
            ) { "PARENTHESIS cards must use LEFT_PAREN or RIGHT_PAREN." }
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    // Returns the numeric value this card resolves to.
    // VARIABLE resolves via variableValue (injected at game start/goal start).
    fun resolvedValue(): Double? = when (type) {
        cardType.NUMBER   -> numberValue?.toDouble()
        cardType.CONSTANT -> when (operator) {
            Operator.PI    -> Math.PI
            Operator.EULER -> Math.E
            else -> null
        }
        cardType.VARIABLE -> variableValue
        else -> null
    }

    fun isValue(): Boolean =
        type == cardType.NUMBER || type == cardType.CONSTANT || type == cardType.VARIABLE

    fun isBinaryOperator(): Boolean = type == cardType.OPERATOR

    fun isPrefixFunction(): Boolean = type == cardType.FUNCTION

    fun isTwoSlot(): Boolean = type == cardType.FRACTION || type == cardType.EXPONENT

    fun isLeftParen(): Boolean  = type == cardType.PARENTHESIS && operator == Operator.LEFT_PAREN
    fun isRightParen(): Boolean = type == cardType.PARENTHESIS && operator == Operator.RIGHT_PAREN

    // Display label used in equation string and card UI
    fun displayLabel(): String = when (type) {
        cardType.NUMBER   -> numberValue.toString()
        cardType.OPERATOR -> when (operator) {
            Operator.ADD      -> "+"
            Operator.SUBTRACT -> "−"
            Operator.MULTIPLY -> "×"
            Operator.DIVIDE   -> "÷"
            else -> "?"
        }
        cardType.FUNCTION -> when (operator) {
            Operator.SIN   -> "sin"
            Operator.COS   -> "cos"
            Operator.LN    -> "ln"
            Operator.LOG10 -> "log₁₀"
            else -> "?"
        }
        cardType.CONSTANT -> when (operator) {
            Operator.PI    -> "π"
            Operator.EULER -> "e"
            else -> "?"
        }
        cardType.FRACTION    -> "Frac"
        cardType.EXPONENT    -> "Exp"
        cardType.VARIABLE    -> when (operator) {
            Operator.VAR_A -> "a"
            Operator.VAR_B -> "b"
            Operator.VAR_C -> "c"
            Operator.VAR_D -> "d"
            Operator.VAR_X -> "x"
            Operator.VAR_Y -> "y"
            Operator.VAR_Z -> "z"
            else -> "?"
        }
        cardType.PARENTHESIS -> when (operator) {
            Operator.LEFT_PAREN  -> "("
            Operator.RIGHT_PAREN -> ")"
            else -> "?"
        }
    }

    override fun toString(): String = displayLabel()
}