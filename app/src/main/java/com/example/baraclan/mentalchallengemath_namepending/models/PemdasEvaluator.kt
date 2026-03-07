package com.example.baraclan.mentalchallengemath_namepending.models

import java.util.Stack
import kotlin.math.*

// ─────────────────────────────────────────────────────────────
// PemdasEvaluator
//
// Supported card layout in expression:
//
//   NUMBER / CONSTANT          → bare value
//   FUNCTION  NUMBER/CONSTANT  → sin(x), cos(x), ln(x), log10(x)
//   FRACTION  NUMBER NUMBER    → x / y  (exact rational division)
//   EXPONENT  NUMBER NUMBER    → x ^ y
//   OPERATOR                   → +  −  ×  ÷  (infix, PEMDAS)
//
// Returns Double.
// ─────────────────────────────────────────────────────────────
object PemdasEvaluator {

    // ── Token types used internally ───────────────────────────
    private sealed class Token {
        data class Value(val v: Double) : Token()
        data class BinOp(val op: Operator) : Token()
    }

    // ── Operator precedence ───────────────────────────────────
    private fun precedence(op: Operator): Int = when (op) {
        Operator.ADD, Operator.SUBTRACT -> 1
        Operator.MULTIPLY, Operator.DIVIDE -> 2
        else -> 0
    }

    // ── Apply a binary operator to two doubles ────────────────
    private fun applyBinary(op: Operator, a: Double, b: Double): Double = when (op) {
        Operator.ADD -> a + b
        Operator.SUBTRACT -> a - b
        Operator.MULTIPLY -> a * b
        Operator.DIVIDE -> {
            require(b != 0.0) { "Division by zero." }
            a / b
        }
        else -> throw IllegalArgumentException("Not a binary operator: $op")
    }

    // ─────────────────────────────────────────────────────────
    // Main entry point
    // Returns Double result of the expression.
    // Throws IllegalArgumentException for invalid expressions.
    // ─────────────────────────────────────────────────────────
    fun evaluate(expressionCards: List<cardGame>): Double {
        require(expressionCards.isNotEmpty()) { "Expression is empty." }

        // Step 1: Resolve all cards into a flat list of Token
        val tokens = resolveToTokens(expressionCards)

        // Step 2: Shunting-yard → postfix
        val postfix = toPostfix(tokens)

        // Step 3: Evaluate postfix
        return evalPostfix(postfix)
    }

    // ── Step 1: Walk the card list and resolve values ─────────
    // Handles: FUNCTION (prefix), FRACTION (two-slot), EXPONENT (two-slot),
    //          CONSTANT, NUMBER, OPERATOR, unary +/−
    private fun resolveToTokens(cards: List<cardGame>): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0

        while (i < cards.size) {
            val card = cards[i]

            when {
                // ── Value cards (NUMBER, CONSTANT) ────────────
                card.isValue() -> {
                    val v = card.resolvedValue()
                        ?: throw IllegalArgumentException("Card '${card.name}' has no value.")
                    tokens.add(Token.Value(v))
                    i++
                }

                // ── Prefix functions: sin/cos/ln/log10 ────────
                // Layout: FUNCTION  VALUE_CARD
                card.isPrefixFunction() -> {
                    require(i + 1 < cards.size) {
                        "${card.operator} must be followed by a value card."
                    }
                    val next = cards[i + 1]
                    require(next.isValue()) {
                        "${card.operator} must be followed by a NUMBER or CONSTANT, got ${next.type}."
                    }
                    val arg = next.resolvedValue()!!
                    val result = applyFunction(card.operator!!, arg)
                    tokens.add(Token.Value(result))
                    i += 2
                }

                // ── FRACTION card: x / y ──────────────────────
                // Layout: FRACTION  NUMBER  NUMBER
                card.type == cardType.FRACTION -> {
                    require(i + 2 < cards.size) {
                        "FRACTION card needs two number cards after it."
                    }
                    val numeratorCard = cards[i + 1]
                    val denominatorCard = cards[i + 2]
                    require(numeratorCard.isValue() && denominatorCard.isValue()) {
                        "FRACTION card must be followed by two value cards."
                    }
                    val num = numeratorCard.resolvedValue()!!
                    val den = denominatorCard.resolvedValue()!!
                    require(den != 0.0) { "Fraction denominator cannot be zero." }
                    tokens.add(Token.Value(num / den))
                    i += 3
                }

                // ── EXPONENT card: x ^ y ──────────────────────
                // Layout: EXPONENT  NUMBER  NUMBER
                card.type == cardType.EXPONENT -> {
                    require(i + 2 < cards.size) {
                        "EXPONENT card needs two number cards after it."
                    }
                    val baseCard = cards[i + 1]
                    val expCard = cards[i + 2]
                    require(baseCard.isValue() && expCard.isValue()) {
                        "EXPONENT card must be followed by two value cards."
                    }
                    val base = baseCard.resolvedValue()!!
                    val exp = expCard.resolvedValue()!!
                    tokens.add(Token.Value(base.pow(exp)))
                    i += 3
                }

                // ── Binary operators (+, −, ×, ÷) ─────────────
                card.isBinaryOperator() -> {
                    val op = card.operator!!

                    // Handle unary +/− at the start or after another operator
                    val isUnary = (op == Operator.ADD || op == Operator.SUBTRACT) &&
                            (tokens.isEmpty() || tokens.last() is Token.BinOp)

                    if (isUnary) {
                        require(i + 1 < cards.size) { "Unary operator at end of expression." }
                        val next = cards[i + 1]
                        require(next.isValue()) { "Unary operator must be followed by a value." }
                        val v = next.resolvedValue()!!
                        val signed = if (op == Operator.SUBTRACT) -v else v
                        tokens.add(Token.Value(signed))
                        i += 2
                    } else {
                        tokens.add(Token.BinOp(op))
                        i++
                    }
                }

                else -> throw IllegalArgumentException(
                    "Unexpected card type '${card.type}' for card '${card.name}'."
                )
            }
        }

        return tokens
    }

    // ── Step 2: Shunting-yard → postfix ──────────────────────
    private fun toPostfix(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val opStack = Stack<Token.BinOp>()

        for (token in tokens) {
            when (token) {
                is Token.Value -> output.add(token)
                is Token.BinOp -> {
                    while (opStack.isNotEmpty() &&
                        precedence(opStack.peek().op) >= precedence(token.op)
                    ) {
                        output.add(opStack.pop())
                    }
                    opStack.push(token)
                }
            }
        }
        while (opStack.isNotEmpty()) output.add(opStack.pop())
        return output
    }

    // ── Step 3: Evaluate postfix ──────────────────────────────
    private fun evalPostfix(tokens: List<Token>): Double {
        val stack = Stack<Double>()
        for (token in tokens) {
            when (token) {
                is Token.Value -> stack.push(token.v)
                is Token.BinOp -> {
                    require(stack.size >= 2) { "Invalid expression: not enough operands." }
                    val b = stack.pop()
                    val a = stack.pop()
                    stack.push(applyBinary(token.op, a, b))
                }
            }
        }
        require(stack.size == 1) { "Invalid expression: leftover values." }
        return stack.pop()
    }

    // ── Apply prefix function ─────────────────────────────────
    private fun applyFunction(op: Operator, arg: Double): Double = when (op) {
        Operator.SIN -> sin(Math.toRadians(arg))   // degrees input
        Operator.COS -> cos(Math.toRadians(arg))   // degrees input
        Operator.LN -> {
            require(arg > 0) { "ln requires a positive argument." }
            ln(arg)
        }
        Operator.LOG10 -> {
            require(arg > 0) { "log10 requires a positive argument." }
            log10(arg)
        }
        else -> throw IllegalArgumentException("Not a function operator: $op")
    }

    // ── Convenience: evaluate and round to N decimal places ───
    fun evaluateRounded(expressionCards: List<cardGame>, decimals: Int = 4): Double {
        val result = evaluate(expressionCards)
        val factor = 10.0.pow(decimals)
        return round(result * factor) / factor
    }
}