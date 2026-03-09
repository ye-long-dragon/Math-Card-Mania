package com.example.baraclan.mentalchallengemath_namepending.models

import kotlin.math.roundToInt

enum class Difficulty { EASY, MEDIUM, HARD }

// ─────────────────────────────────────────────────────────────
// Goal ranges per difficulty:
//   Easy:   -100  to 100
//   Medium: -1000 to 1000
//   Hard:   -10000 to 10000
//
// Goal type per round band:
//   Rounds 1–4:  whole integers only          (e.g. 7, -42, 100)
//   Rounds 5–7:  whole integers OR .5 steps   (e.g. 3, -7, 4.5, -12.5)
//   Rounds 8–10: full decimals per difficulty
//               Easy → 2 dp, Medium → 3 dp, Hard → 5 dp
// ─────────────────────────────────────────────────────────────
object GoalGenerator {

    // ── Range max per difficulty ──────────────────────────────
    private fun maxVal(difficulty: Difficulty) = when (difficulty) {
        Difficulty.EASY   -> 100
        Difficulty.MEDIUM -> 1000
        Difficulty.HARD   -> 10000
    }

    // ── Single goal for a given round (1-based) ───────────────
    private fun randomGoal(round: Int, difficulty: Difficulty): Double {
        val max = maxVal(difficulty)
        return when {
            // Rounds 1–4: whole integers
            round <= 4 -> (-max..max).random().toDouble()

            // Rounds 5–7: whole integer OR x.5
            round <= 7 -> {
                val base = (-max..max).random()
                if ((0..1).random() == 0) base.toDouble()
                else base.toDouble() + 0.5
            }

            // Rounds 8–10: full decimals
            else -> when (difficulty) {
                Difficulty.EASY -> {
                    val raw = (-max * 100..max * 100).random() / 100.0
                    (raw * 100).roundToInt() / 100.0
                }
                Difficulty.MEDIUM -> {
                    val raw = (-max * 1000..max * 1000).random() / 1000.0
                    (raw * 1000).roundToInt() / 1000.0
                }
                Difficulty.HARD -> {
                    val raw = (-max * 100000L..max * 100000L).random() / 100000.0
                    (raw * 100000).roundToInt() / 100000.0
                }
            }
        }
    }

    // ── Generate all 100 goals (10 rounds × 10 goals) ─────────
    fun generateAllGoals(difficulty: Difficulty): List<Double> =
        (1..10).flatMap { round -> (1..10).map { randomGoal(round, difficulty) } }

    // ── Generate 50 goals for multiplayer (5 rounds × 10) ─────
    fun generateMultiplayerGoals(difficulty: Difficulty): List<Double> =
        (1..5).flatMap { round -> (1..10).map { randomGoal(round, difficulty) } }

    // ── Helpers used by game views ────────────────────────────
    fun getGoalsForRound(allGoals: List<Double>, roundIndex: Int): List<Double> {
        val start = roundIndex * 10
        return allGoals.subList(start.coerceAtMost(allGoals.size), (start + 10).coerceAtMost(allGoals.size))
    }

    fun getCurrentGoal(allGoals: List<Double>, goalIndex: Int): Double? =
        allGoals.getOrNull(goalIndex)

    fun getRoundNumber(goalIndex: Int): Int = (goalIndex / 10) + 1
}