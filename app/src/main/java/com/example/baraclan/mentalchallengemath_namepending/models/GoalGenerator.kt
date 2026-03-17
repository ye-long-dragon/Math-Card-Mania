package com.example.baraclan.mentalchallengemath_namepending.models

import kotlin.random.Random

enum class Difficulty { EASY, MEDIUM, HARD }

// ─────────────────────────────────────────────────────────────
// Goal Classes
//   A: integer  -100..100
//   B: integer  -100..100  OR  decimal -0.1..+0.9
//   C: integer  -500..500  OR  decimal -0.01..+0.99
//   D: integer  -1000..1000 OR decimal -0.001..+0.999
//   E: integer  -10000..10000 OR decimal -0.0001..+0.9999
//
// Singleplayer (10 rounds):
//   Easy:   R1-4=A  R5-7=B  R8-10=C
//   Medium: R1-4=B  R5-7=C  R8-10=D
//   Hard:   R1-4=C  R5-7=D  R8-10=E
//
// Multiplayer (5 rounds):
//   Easy:   R1-2=A  R3-4=B  R5=C
//   Medium: R1-2=B  R3-4=C  R5=D
//   Hard:   R1-2=C  R3-4=D  R5=E
// ─────────────────────────────────────────────────────────────
object GoalGenerator {

    private enum class GoalClass { A, B, C, D, E }

    private fun randomGoal(cls: GoalClass): Double {
        return when (cls) {
            GoalClass.A -> Random.nextInt(-100, 101).toDouble()
            GoalClass.B -> if (Random.nextBoolean())
                Random.nextInt(-100, 101).toDouble()
            else
                (-100 + Random.nextDouble()) * 1.0 // -0.1..+0.9 band
                    .let { (-10 + Random.nextInt(10)).toDouble() + Random.nextDouble(-0.1, 0.9) }
            GoalClass.C -> if (Random.nextBoolean())
                Random.nextInt(-500, 501).toDouble()
            else
                Random.nextInt(-500, 501).toDouble() + Random.nextDouble(-0.01, 0.99)
            GoalClass.D -> if (Random.nextBoolean())
                Random.nextInt(-1000, 1001).toDouble()
            else
                Random.nextInt(-1000, 1001).toDouble() + Random.nextDouble(-0.001, 0.999)
            GoalClass.E -> if (Random.nextBoolean())
                Random.nextInt(-10000, 10001).toDouble()
            else
                Random.nextInt(-10000, 10001).toDouble() + Random.nextDouble(-0.0001, 0.9999)
        }
    }

    // Round (1-based) → GoalClass for singleplayer
    private fun singleClass(round: Int, difficulty: Difficulty): GoalClass = when (difficulty) {
        Difficulty.EASY -> when {
            round <= 4 -> GoalClass.A
            round <= 7 -> GoalClass.B
            else       -> GoalClass.C
        }
        Difficulty.MEDIUM -> when {
            round <= 4 -> GoalClass.B
            round <= 7 -> GoalClass.C
            else       -> GoalClass.D
        }
        Difficulty.HARD -> when {
            round <= 4 -> GoalClass.C
            round <= 7 -> GoalClass.D
            else       -> GoalClass.E
        }
    }

    // Round (1-based) → GoalClass for multiplayer (5 rounds)
    private fun multiClass(round: Int, difficulty: Difficulty): GoalClass = when (difficulty) {
        Difficulty.EASY -> when {
            round <= 2 -> GoalClass.A
            round <= 4 -> GoalClass.B
            else       -> GoalClass.C
        }
        Difficulty.MEDIUM -> when {
            round <= 2 -> GoalClass.B
            round <= 4 -> GoalClass.C
            else       -> GoalClass.D
        }
        Difficulty.HARD -> when {
            round <= 2 -> GoalClass.C
            round <= 4 -> GoalClass.D
            else       -> GoalClass.E
        }
    }

    // 100 goals — 10 rounds × 10 goals each
    fun generateAllGoals(difficulty: Difficulty): List<Double> =
        (1..10).flatMap { round ->
            val cls = singleClass(round, difficulty)
            (1..10).map { randomGoal(cls) }
        }

    // 50 goals — 5 rounds × 10 goals each
    fun generateMultiplayerGoals(difficulty: Difficulty): List<Double> =
        (1..5).flatMap { round ->
            val cls = multiClass(round, difficulty)
            (1..10).map { randomGoal(cls) }
        }

    fun getRoundNumber(goalIndex: Int): Int = (goalIndex / 10) + 1
}