package com.example.baraclan.mentalchallengemath_namepending.views

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.baraclan.mentalchallengemath_namepending.models.*
import com.example.baraclan.mentalchallengemath_namepending.scripts.RandomHand
import com.example.baraclan.mentalchallengemath_namepending.scripts.formatDouble
import com.example.baraclan.mentalchallengemath_namepending.scripts.transferCards
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow
import androidx.compose.runtime.derivedStateOf

// ─────────────────────────────────────────────────────────────
// GameView — Single Player
// Shows difficulty select first, then runs the game.
// ─────────────────────────────────────────────────────────────
@SuppressLint("UnrememberedMutableState")
@Composable
fun GameView(
    initialDeck: deck,
    onGameComplete: (() -> Unit)? = null,
    tutorialMode: Boolean = false,
    maxRounds: Int = 10
) {
    // ── Difficulty gate ───────────────────────────────────────
    var selectedDifficulty by remember { mutableStateOf<Difficulty?>(null) }

    if (!tutorialMode && selectedDifficulty == null) {
        DifficultySelectScreen(
            onDifficultySelected = { selectedDifficulty = it },
            onNavigateBack = { onGameComplete?.invoke() }
        )
        return
    }

    // ── Goal list (100 goals, 10 per round) ───────────────────
    val allGoals = remember(selectedDifficulty) {
        if (tutorialMode) GoalGenerator.generateAllGoals(Difficulty.EASY)
        else GoalGenerator.generateAllGoals(selectedDifficulty!!)
    }
    val goalsToUse = if (tutorialMode) allGoals.take(maxRounds * 10) else allGoals

    // ── Game state ────────────────────────────────────────────
    var currentScore by remember { mutableStateOf(0) }
    var currentTurn by remember { mutableStateOf(1) }
    var currentGoalIndex by remember { mutableStateOf(0) }
    var showCongratulations by remember { mutableStateOf(false) }
    var showDeckOverlay by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    // Snapshot initialDeck ONCE before any RandomHand mutates it
    val deckSnapshot = remember { initialDeck.getAllCardsWithCounts().toMap() }
    var gameDeckState by remember { mutableStateOf(deck("Game Deck Copy")) }
    var playerHandState by remember { mutableStateOf(hand("Player's Current Hand")) }
    val equationCards = remember { mutableStateListOf<cardGame>() }
    var cardBin by remember { mutableStateOf(collection("Card Bin")) }
    var draggedCard by remember { mutableStateOf<cardGame?>(null) }
    var dragSource by remember { mutableStateOf<String?>(null) }

    val currentRound = GoalGenerator.getRoundNumber(currentGoalIndex)
    val currentGoalInRound = (currentGoalIndex % 10) + 1

    fun getMarginOfError(round: Int): Double = when {
        round >= 9 -> 0.15
        round >= 7 -> 0.10
        round >= 4 -> 0.05
        else -> 0.0
    }

    fun safeEvaluate(cards: List<cardGame>): Double? =
        if (cards.isEmpty()) null
        else try { PemdasEvaluator.evaluate(cards) } catch (e: Exception) { null }

    val equationResult = derivedStateOf { safeEvaluate(equationCards) }

    val startNewRound: () -> Unit = {
        gameDeckState = deck(initialDeck.name, deckSnapshot)
        playerHandState = RandomHand(gameDeckState)
        equationCards.clear()
        cardBin = collection("Card Bin")
        currentTurn = 1
    }

    fun refillHandToCapacity() {
        while (playerHandState.getTotalCount() < 8 && !gameDeckState.isEmpty()) {
            gameDeckState.drawCard()?.let { playerHandState.addCard(it, 1) }
        }
    }

    LaunchedEffect(Unit) { startNewRound() }

    val equationCardsContainer = object : cardContainer("Equation Cards") {
        override fun addCard(card: cardGame, count: Int) { repeat(count) { equationCards.add(card) } }
        override fun removeCard(card: cardGame, count: Int) {
            var removed = 0
            val iter = equationCards.iterator()
            while (iter.hasNext() && removed < count) {
                val c = iter.next()
                if (c === card || (c.id == card.id && c.type == card.type)) { iter.remove(); removed++ }
            }
        }
        override fun getCardCount(card: cardGame) =
            equationCards.count { c -> c === card || (c.id == card.id && c.type == card.type) }
    }

    val moveCardToEquation: (cardGame) -> Unit = { card ->
        if (playerHandState.getCardCount(card) >= 1)
            transferCards(card, 1, playerHandState, equationCardsContainer)
    }
    val moveCardToHand: (cardGame) -> Unit = { card ->
        if (equationCardsContainer.getCardCount(card) >= 1)
            transferCards(card, 1, equationCardsContainer, playerHandState)
    }
    val onHandCardClick: (cardGame) -> Unit = { clickedCard ->
        if (draggedCard != null && dragSource == "hand") {
            moveCardToEquation(draggedCard!!); draggedCard = null; dragSource = null
        } else moveCardToEquation(clickedCard)
        SoundManager.playPlace()
    }
    val onHandCardLongPress: (cardGame) -> Unit = { card ->
        if (playerHandState.contains(card)) { draggedCard = card; dragSource = "hand" }
    }
    val onEquationCardClick: (cardGame) -> Unit = { clickedCard ->
        if (draggedCard != null && dragSource == "equation") {
            moveCardToHand(draggedCard!!); draggedCard = null; dragSource = null
        } else moveCardToHand(clickedCard)
        SoundManager.playSlide()
    }
    val onEquationCardLongPress: (cardGame) -> Unit = { card ->
        if (equationCards.contains(card)) { draggedCard = card; dragSource = "equation" }
    }
    val onEquationAreaClick: () -> Unit = {
        if (draggedCard != null && dragSource == "hand") {
            moveCardToEquation(draggedCard!!); draggedCard = null; dragSource = null
        }
    }
    val onHandAreaClick: () -> Unit = {
        if (draggedCard != null && dragSource == "equation") {
            moveCardToHand(draggedCard!!); draggedCard = null; dragSource = null
        }
    }

    // ── Settings overlay ─────────────────────────────────────
    if (showSettings) {
        SettingsScreen(onNavigateBack = { showSettings = false })
        return
    }

    // ── Deck overlay ──────────────────────────────────────────
    if (showDeckOverlay) {
        ShowDeckOverlay(
            allDeckCards = initialDeck.getAllCardsAsList(),
            handCards = playerHandState.getAllCardsAsList(),
            binCards = cardBin.getAllCardsAsList(),
            onDismiss = { showDeckOverlay = false }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Top bar (back + deck + settings) ──────────────────
        GameTopBar(
            onReturnToMenu = { onGameComplete?.invoke() },
            onShowDeck = { showDeckOverlay = true },
            onOpenSettings = { showSettings = true }
        )

        // ── Status bar ────────────────────────────────────────
        statusBar(currentScore, currentTurn)

        // ── Round + difficulty indicator ──────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Round $currentRound / ${if (tutorialMode) maxRounds else 10}",
                fontFamily = Pixel, color = BlackBoardYellow
            )
            Text(
                text = "Goal $currentGoalInRound / 10",
                fontFamily = Pixel, color = BlackBoardYellow
            )
            if (!tutorialMode) {
                val diffLabel = when (selectedDifficulty) {
                    Difficulty.EASY -> "Easy"
                    Difficulty.MEDIUM -> "Medium"
                    Difficulty.HARD -> "Hard"
                    null -> ""
                }
                val diffColor = when (selectedDifficulty) {
                    Difficulty.EASY -> Color(0xFF4CAF50)
                    Difficulty.MEDIUM -> Color(0xFFFF9800)
                    Difficulty.HARD -> Color(0xFFF44336)
                    null -> BlackBoardYellow
                }
                Text(text = diffLabel, fontFamily = Pixel, color = diffColor)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Current goal ──────────────────────────────────────
        goal(goalsToUse.subList(currentGoalIndex, goalsToUse.size))

        Spacer(modifier = Modifier.height(16.dp))

        // ── Equation display ──────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onEquationAreaClick),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EquationDisplay(
                equationElements = equationCards,
                onCardClick = onEquationCardClick,
                onCardLongPress = onEquationCardLongPress
            )
            if (equationCards.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                equationResult.value?.let { result ->
                    Text(
                        text = "= ${formatDouble(result)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = BlackBoardYellow,
                        fontFamily = Pixel
                    )
                } ?: Text(
                    text = "Invalid equation",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Red,
                    fontFamily = Pixel
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Select cards from your hand:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp),
            fontFamily = Pixel,
            color = BlackBoardYellow
        )

        InputCardsDisplay(
            playerHand = playerHandState,
            onCardClick = onHandCardClick,
            onCardLongPress = onHandCardLongPress,
            onAreaClick = onHandAreaClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Submit / Discard ──────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                try {
                    val resultDouble = PemdasEvaluator.evaluate(equationCards)
                    equationCards.forEach { cardBin.addCard(it, 1) }
                    equationCards.clear()

                    val currentGoal = goalsToUse.getOrNull(currentGoalIndex)
                    if (currentGoal != null) {
                        val margin = getMarginOfError(currentRound)
                        val hit = if (margin == 0.0)
                            kotlin.math.abs(resultDouble - currentGoal) < 0.0001
                        else
                            resultDouble in (currentGoal * (1 - margin))..(currentGoal * (1 + margin))

                        if (hit) {
                            currentScore += 100
                            currentGoalIndex++
                            if (currentGoalIndex >= goalsToUse.size) {
                                showCongratulations = true
                            } else {
                                // Always reset deck + bin on every new goal
                                startNewRound()
                            }
                        } else {
                            currentTurn++
                        }
                    } else {
                        showCongratulations = true
                    }
                } catch (e: Exception) {
                    equationCards.forEach { cardBin.addCard(it, 1) }
                    equationCards.clear()
                    currentTurn++
                }
                refillHandToCapacity()
            }) {
                Text("Submit", fontFamily = Pixel, color = BlackBoardYellow)
            }

            Button(
                onClick = {
                    if (equationCards.isNotEmpty()) {
                        equationCards.forEach { cardBin.addCard(it, 1) }
                        equationCards.clear()
                        refillHandToCapacity()
                        currentTurn++
                    }
                },
                enabled = equationCards.isNotEmpty()
            ) {
                Text("Discard", fontFamily = Pixel, color = BlackBoardYellow)
            }
        }
    }

    // ── Congratulations overlay ───────────────────────────────
    if (showCongratulations) {
        CongratulationsScreen(
            finalScore = currentScore,
            onDismiss = {
                showCongratulations = false
                onGameComplete?.invoke()
            }
        )
    }
}