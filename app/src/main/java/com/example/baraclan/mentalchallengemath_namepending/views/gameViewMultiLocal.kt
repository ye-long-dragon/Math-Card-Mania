package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baraclan.mentalchallengemath_namepending.models.cardGame
import com.example.baraclan.mentalchallengemath_namepending.models.deck
import com.example.baraclan.mentalchallengemath_namepending.models.hand
import com.example.baraclan.mentalchallengemath_namepending.scripts.RandomHand
import com.example.baraclan.mentalchallengemath_namepending.scripts.evaluateEquation
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow
import kotlinx.coroutines.delay

// Who finished first
enum class Winner { PLAYER1, PLAYER2, NONE }

@Composable
fun LocalMultiplayer(
    availableDecks: List<deck>,          // pass in decks loaded from local storage
    onReturnToMultiplayerMenu: () -> Unit
) {
    var player1Deck by remember { mutableStateOf<deck?>(null) }
    var player2Deck by remember { mutableStateOf<deck?>(null) }
    var decksSelected by remember { mutableStateOf(false) }

    // Shared game state
    val matchGoals = remember { gameGoal.shuffled().take(5) }
    var winner by remember { mutableStateOf(Winner.NONE) }
    var winnerTimeSeconds by remember { mutableStateOf(0) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    val timerRunning = decksSelected && winner == Winner.NONE

    // Stopwatch — stops when someone wins
    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            while (winner == Winner.NONE) {
                delay(1000)
                elapsedSeconds += 1
            }
        }
    }

    when {
        // ── 1. Deck selection ──────────────────────────────────
        !decksSelected -> {
            SelectDeckScreen(
                availableDecks = availableDecks,
                onDecksSelected = { p1, p2 ->
                    player1Deck = p1
                    player2Deck = p2
                    decksSelected = true
                }
            )
        }

        // ── 2. Someone won ─────────────────────────────────────
        winner != Winner.NONE -> {
            WinnerScreen(
                winner = winner,
                timeSeconds = winnerTimeSeconds,
                onPlayAgain = {
                    // Reset everything
                    player1Deck = null
                    player2Deck = null
                    decksSelected = false
                    winner = Winner.NONE
                    winnerTimeSeconds = 0
                    elapsedSeconds = 0
                },
                onReturnToMenu = onReturnToMultiplayerMenu
            )
        }

        // ── 3. Game in progress ────────────────────────────────
        else -> {
            val minutes = elapsedSeconds / 60
            val seconds = elapsedSeconds % 60

            Column(modifier = Modifier.fillMaxSize()) {

                // Player 2 half (rotated — faces away from you)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .rotate(180f),
                    color = Color(0xFF1A237E) // dark blue
                ) {
                    HalfScreen(
                        initialDeck = player2Deck!!,
                        gameGoals = matchGoals,
                        onGameCompleted = {
                            winnerTimeSeconds = elapsedSeconds
                            winner = Winner.PLAYER2
                        }
                    )
                }

                // Shared timer bar in the middle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏱  %02d:%02d".format(minutes, seconds),
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        color = BlackBoardYellow,
                        fontSize = 18.sp
                    )
                }

                // Player 1 half (normal orientation)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    color = Color(0xFF4A0000) // dark red
                ) {
                    HalfScreen(
                        initialDeck = player1Deck!!,
                        gameGoals = matchGoals,
                        onGameCompleted = {
                            winnerTimeSeconds = elapsedSeconds
                            winner = Winner.PLAYER1
                        }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Winner Screen
// ─────────────────────────────────────────────────────────────
@Composable
fun WinnerScreen(
    winner: Winner,
    timeSeconds: Int,
    onPlayAgain: () -> Unit,
    onReturnToMenu: () -> Unit
) {
    val winnerName = if (winner == Winner.PLAYER1) "Player 1" else "Player 2"
    val minutes = timeSeconds / 60
    val seconds = timeSeconds % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🏆",
            fontSize = 72.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "$winnerName Wins!",
            style = MaterialTheme.typography.displaySmall.copy(
                fontFamily = FontFamily.Monospace,
                color = BlackBoardYellow,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Finished in %02d:%02d".format(minutes, seconds),
            fontFamily = FontFamily.Monospace,
            color = BlackBoardYellow.copy(alpha = 0.8f),
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onPlayAgain,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(52.dp)
        ) {
            Text(
                "Play Again",
                fontFamily = FontFamily.Monospace,
                color = BlackBoardYellow,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onReturnToMenu,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(
                "Return to Menu",
                fontFamily = FontFamily.Monospace,
                color = BlackBoardYellow
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// HalfScreen — one player's gameplay area
// ─────────────────────────────────────────────────────────────
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
            gameDeckState = deck(initialDeck.name, initialDeck.getAllCardsWithCounts())
            playerHandState = RandomHand(gameDeckState)
            equationCards.clear()
            currentTurn = 1
        } else {
            gameFinished = true
            onGameCompleted() // ← notifies LocalMultiplayer who won
        }
    }

    LaunchedEffect(Unit) { startNewRound() }

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

    // Progress indicator (e.g. "Goal 2 / 5")
    val progressText = if (!gameFinished)
        "Goal ${currentGoalIndex + 1} / ${gameGoals.size}"
    else ""

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // ── Top section: scrollable content ──────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress
            if (progressText.isNotEmpty()) {
                Text(
                    text = progressText,
                    fontFamily = FontFamily.Monospace,
                    color = BlackBoardYellow.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            statusBar(currentScore, currentTurn)

            goal(gameGoals = gameGoals.subList(currentGoalIndex, gameGoals.size))

            EquationDisplay(
                equationElements = equationCards,
                onCardClick = onEquationCardClick
            )

            Text(
                text = "Select cards from your hand:",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp),
                fontFamily = FontFamily.Monospace,
                color = BlackBoardYellow
            )

            InputCardsDisplay(playerHand = playerHandState, onCardClick = onHandCardClick)
        }

        // ── Bottom section: buttons always visible ────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
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
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Submit", fontFamily = FontFamily.Monospace, color = BlackBoardYellow, fontSize = 12.sp)
            }

            Button(
                onClick = {
                    equationCards.forEach { card -> playerHandState.addCard(card, 1) }
                    equationCards.clear()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear", fontFamily = FontFamily.Monospace, color = BlackBoardYellow, fontSize = 12.sp)
            }
        }
    }
}