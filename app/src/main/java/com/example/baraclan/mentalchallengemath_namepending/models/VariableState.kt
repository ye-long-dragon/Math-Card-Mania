package com.example.baraclan.mentalchallengemath_namepending.models

import kotlin.random.Random

// ─────────────────────────────────────────────────────────────
// VariableState
//
// Holds current values for all 7 variables:
//   x, y, z — set ONCE per game, range -100..100 including decimals
//   a, b, c, d — reset each GOAL, range -10..10 integers only
//
// Usage:
//   val vars = VariableState.newGame()        // call once when game starts
//   val vars2 = vars.newGoal()               // call each time goal changes
//   vars.resolve(card)                        // returns card with variableValue filled
// ─────────────────────────────────────────────────────────────
data class VariableState(
    val x: Double,
    val y: Double,
    val z: Double,
    val a: Int,
    val b: Int,
    val c: Int,
    val d: Int
) {
    companion object {
        // Called once when the game starts — seeds x/y/z
        fun newGame(): VariableState = VariableState(
            x = randomXYZ(),
            y = randomXYZ(),
            z = randomXYZ(),
            a = randomABCD(),
            b = randomABCD(),
            c = randomABCD(),
            d = randomABCD()
        )

        // -100..100 including small decimals (-0.01 to 0.01 range represented)
        // Strategy: 70% chance whole integer, 30% chance small decimal
        private fun randomXYZ(): Double {
            return if (Random.nextFloat() < 0.70f) {
                Random.nextInt(-100, 101).toDouble()
            } else {
                // small decimal: step of 0.01
                val steps = Random.nextInt(-10000, 10001)
                steps / 100.0
            }
        }

        private fun randomABCD(): Int = Random.nextInt(-10, 11)
    }

    // Called each goal — keeps x/y/z, re-randomizes a/b/c/d
    fun newGoal(): VariableState = copy(
        a = randomABCD(),
        b = randomABCD(),
        c = randomABCD(),
        d = randomABCD()
    )

    // Returns the Double value for a given variable operator
    fun valueOf(op: Operator): Double = when (op) {
        Operator.VAR_X -> x
        Operator.VAR_Y -> y
        Operator.VAR_Z -> z
        Operator.VAR_A -> a.toDouble()
        Operator.VAR_B -> b.toDouble()
        Operator.VAR_C -> c.toDouble()
        Operator.VAR_D -> d.toDouble()
        else -> throw IllegalArgumentException("Not a variable operator: $op")
    }

    // Injects variableValue into a VARIABLE card so PemdasEvaluator can read it
    fun resolve(card: cardGame): cardGame {
        if (card.type != cardType.VARIABLE) return card
        return card.copy(variableValue = valueOf(card.operator!!))
    }

    // Human-readable display string shown under the goal
    fun displayString(): String {
        fun fmt(v: Double) = if (v == kotlin.math.floor(v) && !v.isInfinite()) v.toLong().toString() else "%.2f".format(v)
        return "x=${fmt(x)}  y=${fmt(y)}  z=${fmt(z)}  |  a=$a  b=$b  c=$c  d=$d"
    }
}