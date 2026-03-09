package com.example.baraclan.mentalchallengemath_namepending.models

import java.util.Stack
import kotlin.math.*

// ─────────────────────────────────────────────────────────────
// PemdasEvaluator
//
// Supported card layout:
//
//   NUMBER / CONSTANT                    → bare value
//   FUNCTION  <slot>                     → sin/cos/ln/log10 of slot
//   FRACTION  <slot>  <slot>             → slot / slot
//   EXPONENT  <slot>  <slot>             → slot ^ slot
//   OPERATOR                             → +  −  ×  ÷  (infix PEMDAS)
//
// A <slot> can itself be a nested FUNCTION, FRACTION, or EXPONENT,
// so FRACTION → 5 → EXPONENT → 2 → 3  correctly yields 5 / 2^3 = 0.625
//
// Returns Double.
// ─────────────────────────────────────────────────────────────
object PemdasEvaluator {

    // ── Token types used internally ───────────────────────────
    private sealed class Token {
        data class Value(val v: Double) : Token()
        data class BinOp(val op: Operator) : Token()
    }

    private fun precedence(op: Operator): Int = when (op) {
        Operator.ADD, Operator.SUBTRACT -> 1
        Operator.MULTIPLY, Operator.DIVIDE -> 2
        else -> 0
    }

    private fun applyBinary(op: Operator, a: Double, b: Double): Double = when (op) {
        Operator.ADD      -> a + b
        Operator.SUBTRACT -> a - b
        Operator.MULTIPLY -> a * b
        Operator.DIVIDE   -> { require(b != 0.0) { "Division by zero." }; a / b }
        else -> throw IllegalArgumentException("Not a binary operator: $op")
    }

    private fun applyFunction(op: Operator, arg: Double): Double = when (op) {
        Operator.SIN   -> sin(Math.toRadians(arg))
        Operator.COS   -> cos(Math.toRadians(arg))
        Operator.LN    -> { require(arg > 0) { "ln requires a positive argument." }; ln(arg) }
        Operator.LOG10 -> { require(arg > 0) { "log10 requires a positive argument." }; log10(arg) }
        else -> throw IllegalArgumentException("Not a function operator: $op")
    }

    // ─────────────────────────────────────────────────────────
    // Main entry point
    // ─────────────────────────────────────────────────────────
    fun evaluate(expressionCards: List<cardGame>): Double {
        require(expressionCards.isNotEmpty()) { "Expression is empty." }
        val tokens = resolveToTokens(expressionCards)
        val postfix = toPostfix(tokens)
        return evalPostfix(postfix)
    }

    // ─────────────────────────────────────────────────────────
    // resolveSlot — consumes one "value slot" starting at index i.
    // A slot is:
    //   • a plain value card (NUMBER or CONSTANT)
    //   • a FUNCTION card followed by one slot
    //   • a FRACTION or EXPONENT card followed by two slots
    // Returns Pair(resolvedDouble, nextIndex).
    // ─────────────────────────────────────────────────────────
    private fun resolveSlot(cards: List<cardGame>, i: Int): Pair<Double, Int> {
        require(i < cards.size) { "Expected a value or compound card but reached end of expression." }
        val card = cards[i]

        return when {
            // Plain value
            card.isValue() -> {
                val v = card.resolvedValue()
                    ?: throw IllegalArgumentException("Card '${card.name}' has no value.")
                Pair(v, i + 1)
            }

            // FUNCTION <slot>
            card.isPrefixFunction() -> {
                val (arg, next) = resolveSlot(cards, i + 1)
                val result = applyFunction(card.operator!!, arg)
                Pair(result, next)
            }

            // FRACTION <slot> <slot>  →  slot1 / slot2
            card.type == cardType.FRACTION -> {
                val (num, afterNum) = resolveSlot(cards, i + 1)
                val (den, afterDen) = resolveSlot(cards, afterNum)
                require(den != 0.0) { "Fraction denominator cannot be zero." }
                Pair(num / den, afterDen)
            }

            // EXPONENT <slot> <slot>  →  slot1 ^ slot2
            card.type == cardType.EXPONENT -> {
                val (base, afterBase) = resolveSlot(cards, i + 1)
                val (exp, afterExp)   = resolveSlot(cards, afterBase)
                Pair(base.pow(exp), afterExp)
            }

            else -> throw IllegalArgumentException(
                "Expected a value/function/fraction/exponent card at position $i, got '${card.type}'."
            )
        }
    }

    // ─────────────────────────────────────────────────────────
    // resolveToTokens — walk top-level cards into flat Token list.
    // Compound cards (FUNCTION/FRACTION/EXPONENT) are fully consumed
    // by resolveSlot and emitted as a single Token.Value.
    // Binary operators become Token.BinOp.
    // ─────────────────────────────────────────────────────────
    private fun resolveToTokens(cards: List<cardGame>): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0

        while (i < cards.size) {
            val card = cards[i]

            when {
                // Value / compound → resolve the full slot
                card.isValue() || card.isPrefixFunction() ||
                        card.type == cardType.FRACTION || card.type == cardType.EXPONENT -> {
                    val (v, next) = resolveSlot(cards, i)
                    tokens.add(Token.Value(v))
                    i = next
                }

                // Binary operators
                card.isBinaryOperator() -> {
                    val op = card.operator!!

                    // Unary +/− at start or after another operator
                    val isUnary = (op == Operator.ADD || op == Operator.SUBTRACT) &&
                            (tokens.isEmpty() || tokens.last() is Token.BinOp)

                    if (isUnary) {
                        require(i + 1 < cards.size) { "Unary operator at end of expression." }
                        // The operand of a unary op may itself be a compound slot
                        val (v, next) = resolveSlot(cards, i + 1)
                        val signed = if (op == Operator.SUBTRACT) -v else v
                        tokens.add(Token.Value(signed))
                        i = next
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

    // ── Shunting-yard → postfix ───────────────────────────────
    private fun toPostfix(tokens: List<Token>): List<Token> {
        val output  = mutableListOf<Token>()
        val opStack = Stack<Token.BinOp>()
        for (token in tokens) {
            when (token) {
                is Token.Value -> output.add(token)
                is Token.BinOp -> {
                    while (opStack.isNotEmpty() &&
                        precedence(opStack.peek().op) >= precedence(token.op))
                        output.add(opStack.pop())
                    opStack.push(token)
                }
            }
        }
        while (opStack.isNotEmpty()) output.add(opStack.pop())
        return output
    }

    // ── Evaluate postfix ──────────────────────────────────────
    private fun evalPostfix(tokens: List<Token>): Double {
        val stack = Stack<Double>()
        for (token in tokens) {
            when (token) {
                is Token.Value -> stack.push(token.v)
                is Token.BinOp -> {
                    require(stack.size >= 2) { "Invalid expression: not enough operands." }
                    val b = stack.pop(); val a = stack.pop()
                    stack.push(applyBinary(token.op, a, b))
                }
            }
        }
        require(stack.size == 1) { "Invalid expression: leftover values." }
        return stack.pop()
    }

    fun evaluateRounded(expressionCards: List<cardGame>, decimals: Int = 4): Double {
        val result = evaluate(expressionCards)
        val factor = 10.0.pow(decimals)
        return round(result * factor) / factor
    }
}