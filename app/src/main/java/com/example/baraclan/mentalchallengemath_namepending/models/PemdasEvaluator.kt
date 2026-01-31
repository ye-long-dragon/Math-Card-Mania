package com.example.baraclan.mentalchallengemath_namepending.models



import java.util.Stack

// Requires cardGame, CardType, Operator
object PemdasEvaluator {

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
        require(expressionCards.size % 2 != 0) { "Invalid expression length." }

        val outputQueue: MutableList<cardGame> = mutableListOf()
        val operatorStack: Stack<cardGame> = Stack()

        for (card in expressionCards) {
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