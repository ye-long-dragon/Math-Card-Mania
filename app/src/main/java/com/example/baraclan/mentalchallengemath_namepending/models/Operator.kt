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
    POWER,      // x ^ y
    FRACTION,   // x / y

    // Variables — a,b,c,d reset each goal; x,y,z set once per game
    VAR_A,
    VAR_B,
    VAR_C,
    VAR_D,
    VAR_X,
    VAR_Y,
    VAR_Z,

    // Parentheses
    LEFT_PAREN,
    RIGHT_PAREN
}