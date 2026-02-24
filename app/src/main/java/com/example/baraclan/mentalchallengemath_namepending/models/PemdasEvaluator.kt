package com.example.baraclan.mentalchallengemath_namepending.models



import java.util.Stack

// Requires cardGame, CardType, Operator
object PemdasEvaluator {

    // Treat leading +/- as unary sign operators and fold them into the following number.
    // Example: [-, 5, +, 3] -> [ -5, +, 3 ]
    private fun normalizeUnary(expressionCards: List<cardGame>): List<cardGame> {
        if (expressionCards.isEmpty()) return expressionCards

        val result = mutableListOf<cardGame>()
        var i = 0
        while (i < expressionCards.size) {
            val card = expressionCards[i]
            val isUnaryPlusOrMinus =
                card.type == cardType.OPERATOR &&
                        (card.operator == Operator.ADD || card.operator == Operator.SUBTRACT) &&
                        (i == 0 || expressionCards[i - 1].type == cardType.OPERATOR)

            if (isUnaryPlusOrMinus) {
                require(i + 1 < expressionCards.size) {
                    "Unary operator at end of expression."
                }
                val next = expressionCards[i + 1]
                require(next.type == cardType.NUMBER) {
                    "Unary operator must be followed by a number."
                }

                val baseValue = next.numberValue
                    ?: throw IllegalArgumentException("Number card missing value.")
                val signedValue = if (card.operator == Operator.SUBTRACT) -baseValue else baseValue

                // Create a transient signed number card just for evaluation.
                val signedCard = cardGame(
                    id = next.id,
                    name = "Number ($signedValue)",
                    type = cardType.NUMBER,
                    numberValue = signedValue,
                    operator = null
                )
                result.add(signedCard)
                i += 2
            } else {
                result.add(card)
                i++
            }
        }

        return result
    }

    private fun getPrecedence(operator: Operator): Int {
        return when (operator) {
            Operator.ADD, Operator.SUBTRACT -> 1
            Operator.MULTIPLY, Operator.DIVIDE -> 2
        }
    }

    private fun applyOperator(operator: Operator, b: Int, a: Int): Int {
        return when (operator) {
            Operator.ADD -> a + b
            Operator.SUBTRACT -> a - b
            Operator.MULTIPLY -> a * b
            Operator.DIVIDE -> {
                require(b != 0) { "Division by zero." }
                a / b
            }
        }
    }

    fun evaluate(expressionCards: List<cardGame>): Int {
        require(expressionCards.isNotEmpty()) { "Expression is empty." }

        // First normalize any unary +/- into signed numbers.
        val tokens = normalizeUnary(expressionCards)

        // After normalization, expression must be NUMBER (op NUMBER)* and end with NUMBER.
        require(tokens.first().type == cardType.NUMBER) { "Expression must start with a number." }
        require(tokens.last().type == cardType.NUMBER) { "Expression cannot end with an operator." }
        require(tokens.size % 2 != 0) { "Invalid expression length." }

        val outputQueue: MutableList<cardGame> = mutableListOf()
        val operatorStack: Stack<cardGame> = Stack()

        for (card in tokens) {
            when (card.type) {
                cardType.NUMBER -> outputQueue.add(card)
                cardType.OPERATOR -> {
                    val currentOp = card.operator!!
                    while (operatorStack.isNotEmpty() && operatorStack.peek().type == cardType.OPERATOR &&
                        getPrecedence(operatorStack.peek().operator!!) >= getPrecedence(currentOp)) {
                        outputQueue.add(operatorStack.pop())
                    }
                    operatorStack.push(card)
                }
            }
        }

        while (operatorStack.isNotEmpty()) {
            outputQueue.add(operatorStack.pop())
        }

        val evaluationStack: Stack<Int> = Stack()
        for (card in outputQueue) {
            when (card.type) {
                cardType.NUMBER -> evaluationStack.push(card.numberValue!!)
                cardType.OPERATOR -> {
                    require(evaluationStack.size >= 2) { "Invalid expression: not enough operands." }
                    val operand2 = evaluationStack.pop()
                    val operand1 = evaluationStack.pop()
                    evaluationStack.push(applyOperator(card.operator!!, operand2, operand1))
                }
            }
        }

        require(evaluationStack.size == 1) { "Invalid expression: mismatch." }
        return evaluationStack.pop()
    }
}