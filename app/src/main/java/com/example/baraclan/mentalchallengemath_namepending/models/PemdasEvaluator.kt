package com.example.baraclan.mentalchallengemath_namepending.models

import java.util.Stack
import kotlin.math.*

// ─────────────────────────────────────────────────────────────────────────────
// PemdasEvaluator
//
// NUMBER + NUMBER adjacent        → concatenate digits  (1,2 → 12)
// NUMBER + VARIABLE/CONSTANT      → multiply            (12,a → 12×a)
// VARIABLE/CONSTANT + anything    → multiply            (a,b → a×b)
// Value + (                       → multiply            (3,(x+1) → 3×(x+1))
// ) + Value                       → multiply
//
// Functions/Exponents/Fractions accept a parenthesised group as their argument:
//   sin > ( > cards > )          → sin( evaluated_group )
//   Exponent > ( > ... > ) > ( > ... > )  → base^exp
// ─────────────────────────────────────────────────────────────────────────────
object PemdasEvaluator {

    private sealed class Token {
        data class Value(val v: Double, val isLiteral: Boolean = false) : Token()
        data class BinOp(val op: Operator) : Token()
        object LParen : Token()
        object RParen : Token()
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
    fun evaluate(expressionCards: List<cardGame>): Double {
        require(expressionCards.isNotEmpty()) { "Expression is empty." }
        val raw      = resolveToTokens(expressionCards)
        val combined = collapseAndImplicit(raw)
        val postfix  = toPostfix(combined)
        return evalPostfix(postfix)
    }

    // ─────────────────────────────────────────────────────────
    // resolveSlot — recursively resolves one value-producing slot.
    //
    // A "slot" can be:
    //   • A simple value card (NUMBER / VARIABLE / CONSTANT)
    //   • A prefix-function card followed by one slot or a parenthesised group
    //   • An EXPONENT card followed by two slots/groups  (base ^ exp)
    //   • A FRACTION card followed by two slots/groups   (num / den)
    //   • A parenthesised group: LEFT_PAREN … RIGHT_PAREN
    //     (the interior is fully evaluated via a recursive evaluate call)
    //
    // Returns (resolvedDouble, nextIndex).
    // ─────────────────────────────────────────────────────────
    private fun resolveSlot(cards: List<cardGame>, i: Int): Pair<Double, Int> {
        require(i < cards.size) { "Expected a value but reached end of expression." }
        val card = cards[i]
        return when {
            // ── Simple value ──────────────────────────────────
            card.isValue() -> {
                val v = card.resolvedValue()
                    ?: throw IllegalArgumentException(
                        "'${card.name}' has no resolved value (inject VariableState first)."
                    )
                Pair(v, i + 1)
            }

            // ── Prefix function (sin, cos, ln, log10) ─────────
            card.isPrefixFunction() -> {
                val (arg, next) = resolveSlot(cards, i + 1)
                Pair(applyFunction(card.operator!!, arg), next)
            }

            // ── Fraction ──────────────────────────────────────
            card.type == cardType.FRACTION -> {
                val (num, a2) = resolveSlot(cards, i + 1)
                val (den, a3) = resolveSlot(cards, a2)
                require(den != 0.0) { "Fraction denominator is zero." }
                Pair(num / den, a3)
            }

            // ── Exponent (base ^ exp) ─────────────────────────
            card.type == cardType.EXPONENT -> {
                val (base, a2) = resolveSlot(cards, i + 1)
                val (exp,  a3) = resolveSlot(cards, a2)
                Pair(base.pow(exp), a3)
            }

            // ── Parenthesised group ───────────────────────────
            // Collect all cards between the matching ( … ) and
            // evaluate them as a sub-expression.
            card.isLeftParen() -> {
                var depth = 1
                var j = i + 1
                while (j < cards.size && depth > 0) {
                    when {
                        cards[j].isLeftParen()  -> depth++
                        cards[j].isRightParen() -> depth--
                    }
                    j++
                }
                require(depth == 0) { "Mismatched parentheses in sub-expression." }
                // cards[i+1 .. j-2] is the interior (j-1 is the closing paren)
                val interior = cards.subList(i + 1, j - 1)
                require(interior.isNotEmpty()) { "Empty parentheses are not allowed." }
                val value = evaluate(interior)   // recursive full evaluation
                Pair(value, j)                   // j already points past the ')'
            }

            else -> throw IllegalArgumentException(
                "Expected value/function/fraction/exponent/( at position $i, got '${card.type}'."
            )
        }
    }

    // ─────────────────────────────────────────────────────────
    // resolveToTokens — walks the top-level card list into a
    // flat token stream, tagging NUMBER-sourced values as literals.
    // Parentheses that appear at the TOP level (not consumed by
    // resolveSlot) are kept as LParen/RParen tokens for the
    // shunting-yard algorithm.
    // ─────────────────────────────────────────────────────────
    private fun resolveToTokens(cards: List<cardGame>): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < cards.size) {
            val card = cards[i]
            when {
                card.isLeftParen()  -> { tokens.add(Token.LParen); i++ }
                card.isRightParen() -> { tokens.add(Token.RParen); i++ }

                // Compound/value slots
                card.isValue() || card.isPrefixFunction() ||
                        card.type == cardType.FRACTION || card.type == cardType.EXPONENT -> {
                    val (v, next) = resolveSlot(cards, i)
                    val isLit = card.type == cardType.NUMBER
                    tokens.add(Token.Value(v, isLiteral = isLit))
                    i = next
                }

                card.isBinaryOperator() -> {
                    val op = card.operator!!
                    val isUnary = (op == Operator.ADD || op == Operator.SUBTRACT) &&
                            (tokens.isEmpty() || tokens.last() is Token.BinOp || tokens.last() is Token.LParen)
                    if (isUnary) {
                        val (v, next) = resolveSlot(cards, i + 1)
                        val nextIsLit = i + 1 < cards.size && cards[i + 1].type == cardType.NUMBER
                        val signed = if (op == Operator.SUBTRACT) -v else v
                        tokens.add(Token.Value(signed, isLiteral = nextIsLit))
                        i = next
                    } else {
                        tokens.add(Token.BinOp(op))
                        i++
                    }
                }

                else -> throw IllegalArgumentException(
                    "Unexpected card type '${card.type}' at position $i."
                )
            }
        }
        return tokens
    }

    // ─────────────────────────────────────────────────────────
    // collapseAndImplicit
    //
    // Pass 1 — collapse runs of adjacent NUMBER literals:
    //   [1,lit] [2,lit] → [12,lit]
    //
    // Pass 2 — insert implicit × where needed:
    //   literal + non-literal value  → × (12a)
    //   non-literal + any value      → × (ab, a2)
    //   value + LParen               → × (3(x+1))
    //   RParen + value               → × ((x+1)3)
    //   RParen + LParen              → × ((a)(b))
    // ─────────────────────────────────────────────────────────
    private fun collapseAndImplicit(tokens: List<Token>): List<Token> {
        // Pass 1 — collapse adjacent literals
        val collapsed = mutableListOf<Token>()
        var i = 0
        while (i < tokens.size) {
            val t = tokens[i]
            if (t is Token.Value && t.isLiteral) {
                var digits = t.v.toLong().toString()
                var j = i + 1
                while (j < tokens.size) {
                    val n = tokens[j]
                    if (n is Token.Value && n.isLiteral) { digits += n.v.toLong().toString(); j++ }
                    else break
                }
                collapsed.add(Token.Value(digits.toDouble(), isLiteral = true))
                i = j
            } else { collapsed.add(t); i++ }
        }

        // Pass 2 — implicit multiply
        val result = mutableListOf<Token>()
        for (k in collapsed.indices) {
            result.add(collapsed[k])
            if (k < collapsed.size - 1) {
                val cur  = collapsed[k]
                val next = collapsed[k + 1]
                val mul = when {
                    cur is Token.Value && cur.isLiteral  && next is Token.Value && !next.isLiteral -> true
                    cur is Token.Value && !cur.isLiteral && next is Token.Value                   -> true
                    cur is Token.Value                   && next is Token.LParen                  -> true
                    cur is Token.RParen                  && next is Token.Value                   -> true
                    cur is Token.RParen                  && next is Token.LParen                  -> true
                    else -> false
                }
                if (mul) result.add(Token.BinOp(Operator.MULTIPLY))
            }
        }
        return result
    }

    // ─────────────────────────────────────────────────────────
    // Shunting-yard → postfix (handles parens)
    // ─────────────────────────────────────────────────────────
    private fun toPostfix(tokens: List<Token>): List<Token> {
        val output  = mutableListOf<Token>()
        val opStack = Stack<Token>()
        for (token in tokens) {
            when (token) {
                is Token.Value  -> output.add(token)
                is Token.LParen -> opStack.push(token)
                is Token.RParen -> {
                    while (opStack.isNotEmpty() && opStack.peek() !is Token.LParen)
                        output.add(opStack.pop())
                    require(opStack.isNotEmpty()) { "Mismatched parentheses." }
                    opStack.pop()
                }
                is Token.BinOp -> {
                    while (opStack.isNotEmpty() &&
                        opStack.peek() is Token.BinOp &&
                        precedence((opStack.peek() as Token.BinOp).op) >= precedence(token.op))
                        output.add(opStack.pop())
                    opStack.push(token)
                }
            }
        }
        while (opStack.isNotEmpty()) {
            val top = opStack.pop()
            require(top !is Token.LParen) { "Mismatched parentheses." }
            output.add(top)
        }
        return output
    }

    // ─────────────────────────────────────────────────────────
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
                else -> {}
            }
        }
        require(stack.size == 1) { "Invalid expression: leftover values." }
        return stack.pop()
    }

    fun evaluateRounded(expressionCards: List<cardGame>, decimals: Int = 4): Double {
        val result = evaluate(expressionCards)
        val factor = 10.0.pow(decimals)
        return kotlin.math.round(result * factor) / factor
    }
}