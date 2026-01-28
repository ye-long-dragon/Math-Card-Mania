package com.example.baraclan.mentalchallengemath_namepending // Corrected package name

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.gameTheme // Corrected theme name

// Import all your Composable functions from gameViews.kt
// Make sure this import path matches your project structure
import com.example.baraclan.mentalchallengemath_namepending.views.EquationDisplay
import com.example.baraclan.mentalchallengemath_namepending.views.cardDeck
import com.example.baraclan.mentalchallengemath_namepending.views.goal
import com.example.baraclan.mentalchallengemath_namepending.views.inputCards
import com.example.baraclan.mentalchallengemath_namepending.views.statusBar
import kotlin.random.Random
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            gameTheme { // Using your new GameTheme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameScreen()
                }
            }
        }
    }
}

// Data class to hold the values assigned to variables x, y, z
data class VariableAssignments(
    val x: Int,
    val y: Int,
    val z: Int
)

// Helper function to evaluate a mathematical expression string
// This is a basic infix evaluator for demonstration, it handles +, -, *, /
// and respects operator precedence. It doesn't handle parentheses or complex error checking.
fun evaluateExpression(
    expressionTokens: List<String>,
    variableValues: VariableAssignments
): Int? {
    if (expressionTokens.isEmpty()) return null

    // Replace variables with their assigned values
    val processedTokens = expressionTokens.map { token ->
        when (token) {
            "X" -> variableValues.x.toString()
            "Y" -> variableValues.y.toString()
            "Z" -> variableValues.z.toString()
            else -> token
        }
    }

    // Basic shunting-yard like evaluation for simple expressions
    // This assumes a well-formed infix expression (e.g., number operator number operator ...)
    val numbers = mutableListOf<Int>()
    val operators = mutableListOf<String>()

    // Corrected: Added "*" to the precedence map
    val precedence = mapOf("+" to 1, "-" to 1, "*" to 2, "/" to 2)

    fun applyOp(op: String, b: Int, a: Int): Int? {
        return when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b // Corrected: Added multiplication operator
            "/" -> if (b != 0) a / b else null // Handle division by zero
            else -> null
        }
    }

    try {
        var i = 0
        while (i < processedTokens.size) {
            val token = processedTokens[i]
            if (token.matches(Regex("-?\\d+"))) { // It's a number
                numbers.add(token.toInt())
            } else if (precedence.containsKey(token)) { // It's an operator
                while (operators.isNotEmpty() &&
                    precedence.getOrDefault(operators.last(), 0) >= precedence.getOrDefault(token, 0)) {
                    val b = numbers.removeAt(numbers.lastIndex)
                    val a = numbers.removeAt(numbers.lastIndex)
                    val op = operators.removeAt(operators.lastIndex)
                    val res = applyOp(op, b, a)
                    if (res == null) return null // Division by zero or invalid operation
                    numbers.add(res)
                }
                operators.add(token)
            } else {
                // Invalid token (e.g., unexpected character)
                return null
            }
            i++
        }

        while (operators.isNotEmpty()) {
            val b = numbers.removeAt(numbers.lastIndex)
            val a = numbers.removeAt(numbers.lastIndex)
            val op = operators.removeAt(operators.lastIndex)
            val res = applyOp(op, b, a)
            if (res == null) return null // Division by zero or invalid operation
            numbers.add(res)
        }

        return numbers.firstOrNull() // The final result
    } catch (e: Exception) {
        // Catch exceptions like NumberFormatException, NoSuchElementException (if stack is empty unexpectedly)
        return null
    }
}


// Main Game Screen Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen() {
    // --- Game State Variables ---
    var level by remember { mutableStateOf(1) }
    var coins by remember { mutableStateOf(0) }
    var goalNumber by remember { mutableStateOf(0) }
    var variableValues by remember { mutableStateOf(VariableAssignments(0, 0, 0)) }
    val cardDeckContents = remember { mutableStateListOf<String>() } // The 10 cards available
    val userInputExpression = remember { mutableStateListOf<String>() } // Cards player has selected

    // --- Helper Functions for Game Logic ---

    // Function to generate random values for X, Y, Z
    fun assignRandomVariableValues(): VariableAssignments {
        return VariableAssignments(
            x = Random.nextInt(0, 10), // 0-9
            y = Random.nextInt(0, 10),
            z = Random.nextInt(0, 10)
        )
    }

    // Function to generate a random goal number
    fun generateRandomGoal(): Int {
        return Random.nextInt(10, 101) // Goal between 10 and 100
    }

    // Function to generate the 10 random cards for the deck
    fun generateRandomDeck(): List<String> {
        val deck = mutableListOf<String>()
        val possibleElements = listOf(
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", // Numbers
            "+", "-", "*", "/", // Corrected: Added "*" operator
            "X", "Y", "Z" // Variables
        )
        repeat(10) {
            deck.add(possibleElements.random())
        }
        return deck
    }

    // Function to start a new round
    fun startNewRound() {
        goalNumber = generateRandomGoal()
        variableValues = assignRandomVariableValues()
        cardDeckContents.clear()
        cardDeckContents.addAll(generateRandomDeck())
        userInputExpression.clear()
        level++ // Increment level for a new round
    }

    // Initialize game on first composition
    LaunchedEffect(Unit) {
        startNewRound()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Status Bar (Level, Coins)
        statusBar(level = level, coins = coins, modifier = Modifier.fillMaxWidth())

        // 2. Goal Display
        goal(goalNumber = goalNumber, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Input Cards (shows the current constructed equation)
        inputCards(currentExpression = userInputExpression)

        Spacer(modifier = Modifier.weight(1f)) // Pushes deck and buttons to bottom

        // 4. Card Deck
        cardDeck(
            cardContents = cardDeckContents,
            onCardClick = { clickedContent ->
                // Add card to user input expression
                userInputExpression.add(clickedContent)
            },
            modifier = Modifier.fillMaxWidth()
        )

        // 5. Confirm and Discard Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Discard Button
            Button(
                onClick = {
                    userInputExpression.clear() // Clear current input
                },
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            ) {
                Text("Discard")
            }

            // Confirm Button
            Button(
                onClick = {
                    val result = evaluateExpression(userInputExpression, variableValues)
                    var coinsAwarded = 0
                    var roundMessage = ""

                    if (result == null) {
                        roundMessage = "Invalid expression!"
                    } else {
                        val difference = abs(result - goalNumber)

                        when {
                            difference == 0 -> {
                                coinsAwarded = 10
                                roundMessage = "Exact match! +$coinsAwarded coins!"
                            }
                            difference <= 5 -> {
                                coinsAwarded = 8
                                roundMessage = "Close! ($result) - +$coinsAwarded coins!"
                            }
                            difference <= 15 -> {
                                coinsAwarded = 5
                                roundMessage = "Not bad! ($result) - +$coinsAwarded coins!"
                            }
                            difference <= 25 -> {
                                coinsAwarded = 0
                                roundMessage = "Keep trying! ($result) - No coins this round."
                            }
                            else -> {
                                coinsAwarded = 0
                                roundMessage = "Failed round! ($result) - The difference was too large."
                                // You might add game over logic here
                            }
                        }
                        coins += coinsAwarded
                    }

                    println("Result: $result, Goal: $goalNumber, Difference: ${result?.let { abs(it - goalNumber) }}")
                    println(roundMessage)

                    // Start a new round after confirmation
                    startNewRound()
                },
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            ) {
                Text("Confirm")
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun DefaultPreview() {
    gameTheme {
        GameScreen()
    }
}
