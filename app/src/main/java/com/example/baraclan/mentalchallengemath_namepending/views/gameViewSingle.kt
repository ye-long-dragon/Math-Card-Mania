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
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.remember
import androidx.compose.material3.Button
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import com.example.baraclan.mentalchallengemath_namepending.views.*
import com.example.baraclan.mentalchallengemath_namepending.models.*
import com.example.baraclan.mentalchallengemath_namepending.scripts.RandomHand
import com.example.baraclan.mentalchallengemath_namepending.scripts.evaluateEquation

val gameGoal: List<Double> = listOf(
    4.0, -0.125, -36.0, 0.00462962962963, 576.0,
    -0.000072337962963, -14400.0, 5.787037037E-7,
    518400.0, -2.6791838134E-9
)

@Composable
public fun GameView(
    initialDeck: deck // Player's persistent deck from MainActivity
){
    // Game state variables
    var currentScore by remember { mutableStateOf(0) }
    var currentTurn by remember { mutableStateOf(1) }

    // Active deck for the current game session (a mutable copy)
    var gameDeckState by remember { mutableStateOf(deck("Game Deck Copy")) }

    // Player's current hand of cards
    var playerHandState by remember { mutableStateOf(hand("Player's Current Hand")) }

    // Cards currently forming the equation
    val equationCards = remember { mutableStateListOf<cardGame>() }


    // Function to set up a new round
    val startNewRound: () -> Unit = {
        // Copy the initial deck for a fresh start to this round
        gameDeckState = deck(initialDeck.name, initialDeck.getAllCardsWithCounts())
        println("New round: Game deck reset to ${gameDeckState.getTotalCount()} cards.")

        // Draw a new hand from the game deck
        playerHandState = RandomHand(gameDeckState)
        println("New hand drawn. Hand size: ${playerHandState.getTotalCount()}. Deck remaining: ${gameDeckState.getTotalCount()}")

        // Clear previous equation and reset turn
        equationCards.clear()
        currentTurn = 1
    }

    // Start the first round when GameView is launched
    LaunchedEffect(Unit) {
        startNewRound()
    }

    // Move card from hand to equation
    val onHandCardClick: (cardGame) -> Unit = { clickedCard ->
        // TODO: Add equation building validation
        if (playerHandState.contains(clickedCard)) {
            playerHandState.removeCard(clickedCard, 1)
            equationCards.add(clickedCard)
        }
    }

    // Move card from equation to hand
    val onEquationCardClick: (cardGame) -> Unit = { clickedCard ->
        if (equationCards.contains(clickedCard)) {
            equationCards.remove(clickedCard)
            playerHandState.addCard(clickedCard, 1)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        statusBar(currentScore, currentTurn)

        Spacer(modifier = Modifier.height(16.dp))

        goal(gameGoal)

        Spacer(modifier = Modifier.height(16.dp))

        EquationDisplay(
            equationElements = equationCards,
            onCardClick = onEquationCardClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Select cards from your hand:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp),
            fontFamily = Pixel
        )
        InputCardsDisplay(playerHand = playerHandState, onCardClick = onHandCardClick)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                // TODO: Implement actual equation evaluation
                val equationResult: Double = evaluateEquation(equationCards)
                println("Submitted: ${equationCards.joinToString(" ")} = $equationResult")

                val currentGoal = gameGoal.firstOrNull()
                if (currentGoal != null && equationResult == currentGoal) {
                    println("Goal reached!")
                    currentScore += 100
                    startNewRound() // Reset for next round
                    println("Round completed, starting new one.")
                } else {
                    println("Goal NOT reached.")
                    equationCards.clear() // Clear for next attempt
                    currentTurn++
                }
            }) {
                Text("Submit Equation", fontFamily = Pixel)
            }
            Button(onClick = {
                // Return equation cards to hand
                equationCards.forEach { card ->
                    playerHandState.addCard(card, 1)
                }
                equationCards.clear()
            }) {
                Text("Clear Equation", fontFamily = Pixel)
            }
        }
    }
}