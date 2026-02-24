package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baraclan.mentalchallengemath_namepending.models.cardGame
import com.example.baraclan.mentalchallengemath_namepending.models.deck
import com.example.baraclan.mentalchallengemath_namepending.models.hand
import com.example.baraclan.mentalchallengemath_namepending.scripts.RandomHand
import com.example.baraclan.mentalchallengemath_namepending.scripts.evaluateEquation
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow
import kotlinx.coroutines.delay

@Composable
fun LocalMultiplayer(
    onReturntoMultiplayerMenu: () -> Unit
) {
    var player1Deck by remember { mutableStateOf<deck?>(null) }
    var player2Deck by remember { mutableStateOf<deck?>(null) }
    var decksSelected by remember { mutableStateOf(false) }

    if (!decksSelected) {
        SelectDeckScreen {
            p1Deck, p2Deck ->
            // For now, we'll use a sample deck
            val sampleDeck = deck("Starting Deck") 
            player1Deck = sampleDeck
            player2Deck = sampleDeck
            decksSelected = true
        }
    } else {
        val matchGoals = remember { gameGoal.shuffled().take(5) }
        var elapsedSeconds by remember { mutableStateOf(0) }
        LaunchedEffect(Unit) {
            while (true) {
                delay(1000)
                elapsedSeconds += 1
            }
        }
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60

        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .rotate(180f),
                color = Color.Red
            ) {
                HalfScreen(
                    initialDeck = player1Deck!!,
                    gameGoals = matchGoals,
                    onGameCompleted = onReturntoMultiplayerMenu
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Time: %02d:%02d".format(minutes, seconds),
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    color = BlackBoardYellow
                )
            }

            Surface(
                modifier = Modifier
                    .weight(1f) 
                    .fillMaxWidth(),
                color = Color.Blue
            ) {
                HalfScreen(
                    initialDeck = player2Deck!!,
                    gameGoals = matchGoals,
                    onGameCompleted = onReturntoMultiplayerMenu
                )
            }
        }
    }
}

@Composable
fun HalfScreen(
    initialDeck: deck,
    gameGoals: List<Double>,
    modifier: Modifier = Modifier,
    onGameCompleted: () -> Unit
) {
    var currentScore by remember { mutableStateOf(0) }
    var currentTurn by remember { mutableStateOf(1) }
    var currentGoalIndex by remember { mutableStateOf(0) }
    var gameFinished by remember { mutableStateOf(false) }
    var gameDeckState by remember { mutableStateOf(deck("Game Deck Copy")) }
    var playerHandState by remember { mutableStateOf(hand("Player's Current Hand")) }
    val equationCards = remember { mutableStateListOf<cardGame>() }

    val startNewRound: () -> Unit = {
        if (currentGoalIndex < gameGoals.size) {
            val nextGoal = gameGoals[currentGoalIndex]
            gameDeckState = deck(initialDeck.name, initialDeck.getAllCardsWithCounts())
            playerHandState = RandomHand(gameDeckState)
            equationCards.clear()
            currentTurn = 1
        } else {
            gameFinished = true
        }
    }

    LaunchedEffect(Unit) {
        startNewRound()
    }

    val onHandCardClick: (cardGame) -> Unit = { clickedCard ->
        if (playerHandState.contains(clickedCard)) {
            playerHandState.removeCard(clickedCard, 1)
            equationCards.add(clickedCard)
        }
    }

    val onEquationCardClick: (cardGame) -> Unit = { clickedCard ->
        if (equationCards.contains(clickedCard)) {
            equationCards.remove(clickedCard)
            playerHandState.addCard(clickedCard, 1)
        }
    }

    if (gameFinished) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Game Over!",
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = FontFamily.Monospace,
                fontSize = 48.sp,
                color = BlackBoardYellow
            )
            Text(
                text = "Final Score: $currentScore",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 16.dp),
                color = BlackBoardYellow
            )
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            ) {
                Button(onClick = {
                    currentScore = 0
                    currentGoalIndex = 0
                    gameFinished = false
                    startNewRound()
                }) {
                    Text("Play Again", fontFamily = FontFamily.Monospace, color = BlackBoardYellow)
                }
                Button(onClick = {
                    onGameCompleted()
                }) {
                    Text("Return to Menu", fontFamily = FontFamily.Monospace, color = BlackBoardYellow)
                }
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            statusBar(currentScore, currentTurn)

            Spacer(modifier = Modifier.height(16.dp))

            goal(gameGoals = gameGoals.subList(currentGoalIndex, gameGoals.size))

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
                fontFamily = FontFamily.Monospace,
                color = BlackBoardYellow
            )
            InputCardsDisplay(playerHand = playerHandState, onCardClick = onHandCardClick)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    val equationResult: Double = evaluateEquation(equationCards)
                    val currentTargetGoal = gameGoals[currentGoalIndex]

                    if (equationResult == currentTargetGoal) {
                        currentScore += 100 
                        currentGoalIndex++
                        startNewRound()
                    } else {
                        equationCards.clear()
                        currentTurn++
                    }
                }) {
                    Text("Submit Equation", fontFamily = FontFamily.Monospace, color = BlackBoardYellow)
                }
                Button(onClick = {
                    equationCards.forEach { card ->
                        playerHandState.addCard(card, 1)
                    }
                    equationCards.clear()
                }) {
                    Text("Clear Equation", fontFamily = FontFamily.Monospace, color = BlackBoardYellow)
                }
            }
        }
    }
}