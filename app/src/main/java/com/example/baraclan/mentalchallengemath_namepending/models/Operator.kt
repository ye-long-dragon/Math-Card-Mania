package com.example.baraclan.mentalchallengemath_namepending.models

enum class Operator {
    // Basic arithmetic
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,

    // Functions (applied to next card)
    SIN,
    COS,
    LN,
    LOG10,

    // Constants (self-contained values)
    PI,
    EULER,

    // Two-slot special cards
    POWER,      // x ^ y  — base and exponent are separate number cards placed after
    FRACTION    // x / y  — numerator and denominator are separate number cards placed after
}