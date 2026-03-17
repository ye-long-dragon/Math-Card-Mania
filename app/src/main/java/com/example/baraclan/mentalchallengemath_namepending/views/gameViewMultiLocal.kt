package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
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
import com.example.baraclan.mentalchallengemath_namepending.models.Difficulty
import com.example.baraclan.mentalchallengemath_namepending.models.GoalGenerator
import com.example.baraclan.mentalchallengemath_namepending.models.VariableState
import com.example.baraclan.mentalchallengemath_namepending.models.collection
import com.example.baraclan.mentalchallengemath_namepending.scripts.RandomHand
import com.example.baraclan.mentalchallengemath_namepending.scripts.evaluateEquation
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow
import kotlinx.coroutines.delay

// Who finished first
enum class Winner { PLAYER1, PLAYER2, NONE }

@Composable
fun LocalMultiplayer(
    availableDecks: List<deck>,
    onReturnToMultiplayerMenu: () -> Unit
) {
    var player1Deck by remember { mutableStateOf<deck?>(null) }
    var player2Deck by remember { mutableStateOf<deck?>(null) }
    var decksSelected by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf<Difficulty?>(null) }

    // Shared game state — 5 rounds × 10 goals = 50 goals
    val matchGoals = remember(selectedDifficulty) {
        if (selectedDifficulty != null)
            GoalGenerator.generateMultiplayerGoals(selectedDifficulty!!)
        else emptyList()
    }
    var winner by remember { mutableStateOf(Winner.NONE) }
    var winnerTimeSeconds by remember { mutableStateOf(0) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    var showBackConfirm by remember { mutableStateOf(false) }
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

    // ── Settings overlay ─────────────────────────────────────
    if (showSettings) {
        SettingsScreen(onNavigateBack = { showSettings = false })
        return
    }

    // ── Back confirm dialog ───────────────────────────────────
    if (showBackConfirm) {
        AlertDialog(
            onDismissRequest = { showBackConfirm = false },
            title = { Text("Return to Menu?", fontFamily = Pixel, color = BlackBoardYellow) },
            text = { Text("Your current game will be lost.", fontFamily = Pixel, color = BlackBoardYellow) },
            confirmButton = {
                TextButton(onClick = { showBackConfirm = false; onReturnToMultiplayerMenu() }) {
                    Text("Yes", fontFamily = Pixel, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackConfirm = false }) {
                    Text("Cancel", fontFamily = Pixel, color = BlackBoardYellow)
                }
            }
        )
    }

    when {
        // ── 0. Difficulty selection ────────────────────────────
        selectedDifficulty == null -> {
            DifficultySelectScreen(
                title = "Select Difficulty",
                onDifficultySelected = { selectedDifficulty = it },
                onNavigateBack = onReturnToMultiplayerMenu
            )
        }

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

                // Top bar with back + settings (normal orientation)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showBackConfirm = true }) {
                        Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Back", tint = BlackBoardYellow)
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(androidx.compose.material.icons.Icons.Default.Settings, contentDescription = "Settings", tint = BlackBoardYellow)
                    }
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
    val deckSnapshot = remember { initialDeck.getAllCardsWithCounts().toMap() }
    var variableState by remember { mutableStateOf(VariableState.newGame()) }

    // ── FIX: initialize deck and hand immediately so first frame has cards ──
    val initialGameDeck = remember { deck(initialDeck.name, deckSnapshot) }
    var gameDeckState by remember { mutableStateOf(initialGameDeck) }
    var playerHandState by remember { mutableStateOf(RandomHand(initialGameDeck)) }
    val equationCards = remember { mutableStateListOf<cardGame>() }
    var cardBin by remember { mutableStateOf(collection("Card Bin")) }
    var showDeckOverlay by remember { mutableStateOf(false) }

    val startNewRound: () -> Unit = {
        if (currentGoalIndex < gameGoals.size) {
            variableState = variableState.newGoal()
            val freshDeck = deck(initialDeck.name, deckSnapshot)
            gameDeckState = freshDeck
            playerHandState = RandomHand(freshDeck)
            equationCards.clear()
            cardBin = collection("Card Bin")
            currentTurn = 1
        } else {
            gameFinished = true
            onGameCompleted()
        }
    }

    // No longer needed for initial load — only call on subsequent rounds
    // LaunchedEffect(Unit) { startNewRound() }  ← REMOVED (caused empty first frame)

    val onHandCardClick: (cardGame) -> Unit = { clickedCard ->
        if (playerHandState.contains(clickedCard)) {
            playerHandState.removeCard(clickedCard, 1)
            equationCards.add(clickedCard)
            // Reassign to trigger Compose recomposition (hand is a mutable class, not value type)
            playerHandState = hand("Player's Current Hand").also { h ->
                playerHandState.getAllCardsWithCounts().forEach { (c, n) -> h.addCard(c, n) }
            }
        }
    }

    val onEquationCardClick: (cardGame) -> Unit = { clickedCard ->
        if (equationCards.contains(clickedCard)) {
            equationCards.remove(clickedCard)
            playerHandState = hand("Player's Current Hand").also { h ->
                playerHandState.getAllCardsWithCounts().forEach { (c, n) -> h.addCard(c, n) }
                h.addCard(clickedCard, 1)
            }
        }
    }

    val progressText = if (!gameFinished)
        "Goal ${currentGoalIndex + 1} / ${gameGoals.size}"
    else ""

    if (showDeckOverlay) {
        ShowDeckOverlay(
            allDeckCards = initialDeck.getAllCardsAsList(),
            handCards = playerHandState.getAllCardsAsList(),
            binCards = cardBin.getAllCardsAsList(),
            onDismiss = { showDeckOverlay = false }
        )
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (progressText.isNotEmpty()) {
                Text(
                    text = progressText,
                    fontFamily = Pixel,
                    color = BlackBoardYellow.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            statusBar(currentScore, currentTurn)

            // ── Compact inline goal ───────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Goal: ",
                    fontFamily = Pixel,
                    fontSize = 11.sp,
                    color = BlackBoardYellow.copy(alpha = 0.7f)
                )
                Text(
                    text = if (gameGoals.isNotEmpty()) gameGoals[currentGoalIndex].toString() else "-",
                    fontFamily = Pixel,
                    fontSize = 16.sp,
                    color = BlackBoardYellow
                )
            }

            // ── Variable values ───────────────────────────────
            Text(
                text = variableState.displayString(),
                fontFamily = Pixel,
                fontSize = 9.sp,
                color = BlackBoardYellow.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )

            EquationDisplay(
                equationElements = equationCards,
                onCardClick = onEquationCardClick,
                modifier = Modifier.heightIn(max = 80.dp)
            )

            Text(
                text = "Your hand:",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp),
                fontFamily = Pixel,
                fontSize = 10.sp,
                color = BlackBoardYellow
            )

            InputCardsDisplay(playerHand = playerHandState, onCardClick = onHandCardClick)
        }

        // ── Bottom buttons ────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    val equationResult: Double? = evaluateEquation(equationCards.map { variableState.resolve(it) })
                    val currentTargetGoal = gameGoals[currentGoalIndex]
                    val round = (currentGoalIndex + 1).coerceIn(1, 10)
                    val margin = when {
                        round >= 9 -> 0.15
                        round >= 7 -> 0.10
                        round >= 4 -> 0.05
                        else -> 0.0
                    }
                    val hit = if (equationResult == null) false
                    else if (margin == 0.0) kotlin.math.abs(equationResult - currentTargetGoal) < 0.0001
                    else equationResult in (currentTargetGoal * (1 - margin))..(currentTargetGoal * (1 + margin))

                    if (hit) {
                        equationCards.forEach { cardBin.addCard(it, 1) }
                        equationCards.clear()
                        currentScore += 100
                        currentGoalIndex++
                        startNewRound()
                    } else {
                        equationCards.forEach { cardBin.addCard(it, 1) }
                        equationCards.clear()
                        currentTurn++
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Submit", fontFamily = Pixel, color = BlackBoardYellow, fontSize = 12.sp)
            }

            Button(
                onClick = {
                    val returnCards = equationCards.toList()
                    equationCards.clear()
                    playerHandState = hand("Player's Current Hand").also { h ->
                        playerHandState.getAllCardsWithCounts().forEach { (c, n) -> h.addCard(c, n) }
                        returnCards.forEach { card -> h.addCard(card, 1) }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear", fontFamily = Pixel, color = BlackBoardYellow, fontSize = 12.sp)
            }

            Button(
                onClick = { showDeckOverlay = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Deck", fontFamily = Pixel, color = BlackBoardYellow, fontSize = 12.sp)
            }
        }
    }
}