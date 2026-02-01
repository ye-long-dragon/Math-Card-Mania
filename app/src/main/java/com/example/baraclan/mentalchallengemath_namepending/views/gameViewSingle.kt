package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.material3.Button
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.baraclan.mentalchallengemath_namepending.R
import com.example.baraclan.mentalchallengemath_namepending.views.*
import com.example.baraclan.mentalchallengemath_namepending.models.*

val gameGoal: List<Double> = listOf(
    4.0, // Explicitly make integers doubles
    -0.125,
    -36.0,
    0.00462962962963,
    576.0,
    -0.000072337962963,
    -14400.0,
    5.787037037E-7,  // Corrected scientific notation for Kotlin
    518400.0,
    -2.6791838134E-9   // Corrected scientific notation for Kotlin
)

@Composable
public fun GameView(){
    // Game State
    var currentScore by remember { mutableStateOf(0) }
    var currentTurn by remember { mutableStateOf(1) } // Start with turn 1

    // Example hand initialization for demonstration
    val initialHand = remember {
        hand("Player's Hand").apply {
            addCard(cardGame("c1", "Two", type = cardType.NUMBER, numberValue = 2))
            addCard(cardGame("c2", "Three", type = cardType.NUMBER, numberValue = 3))
            addCard(cardGame("c3", "Plus", type = cardType.OPERATOR, operator = Operator.ADD))
            addCard(cardGame("c4", "Multiply", type = cardType.OPERATOR, operator = Operator.MULTIPLY))
            addCard(cardGame("c5", "Four", type = cardType.NUMBER, numberValue = 4))
            addCard(cardGame("c6", "One", type = cardType.NUMBER, numberValue = 1))
        }
    }
    // MutableState for the hand to allow changes
    var playerHandState by remember { mutableStateOf(initialHand) }

    // State for the equation being built
    val equationCards = remember { mutableStateListOf<cardGame>() }

    // Function to handle card clicks from hand
    val onHandCardClick: (cardGame) -> Unit = { clickedCard ->
        // Basic logic to add card to equation and remove from hand
        if (playerHandState.contains(clickedCard)) {
            // TODO: Add more complex validation here:
            // e.g., if equationCards.last() is an operator, don't allow another operator
            // e.g., if equationCards is empty, only allow number cards
            equationCards.add(clickedCard)
            playerHandState = playerHandState.apply { removeCard(clickedCard, 1) } // Update hand state by creating a new hand instance or explicitly modifying and triggering recompose
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween // Distribute content vertically
    ) {
        statusBar(currentScore, currentTurn)

        Spacer(modifier = Modifier.height(16.dp))

        goal(gameGoal) // Assuming gameGoal doesn't change during a game from this list

        Spacer(modifier = Modifier.height(16.dp))

        // EquationDisplay
        EquationDisplay(equationElements = equationCards)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Select cards from your hand:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        // InputCardsDisplay is used for the interactive hand where cards are selected
        InputCardsDisplay(playerHand = playerHandState, onCardClick = onHandCardClick)

        Spacer(modifier = Modifier.height(16.dp))

        // Placeholder for action buttons (e.g., "Submit", "Clear Equation")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                // TODO: Logic to evaluate equation, check against goal, update score, clear equation
                println("Equation submitted: ${equationCards.joinToString(" ")}")
                // For demonstration, just clear the equation and advance turn
                equationCards.clear()
                // In a real game, you would calculate the result and compare it to the goal
                currentTurn++ // Advance turn
            }) {
                Text("Submit Equation")
            }
            Button(onClick = {
                // Logic to clear the current equation and return cards to hand
                equationCards.forEach { card ->
                    // Make sure to add back to a NEW instance of hand or force recompose if using MutableState
                    playerHandState = playerHandState.apply { addCard(card, 1) }
                }
                equationCards.clear()
            }) {
                Text("Clear Equation")
            }
        }
    }
}