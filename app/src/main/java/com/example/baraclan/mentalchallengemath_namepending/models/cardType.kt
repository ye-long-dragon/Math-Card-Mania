package com.example.baraclan.mentalchallengemath_namepending.models

enum class cardType {
    NUMBER,     // Integer number cards (1–20 etc.)
    OPERATOR,   // +  −  ×  ÷
    FUNCTION,   // sin  cos  ln  log10
    CONSTANT,   // π  e
    FRACTION,   // x/y  (two-slot card)
    EXPONENT    // x^y (two-slot card)
}