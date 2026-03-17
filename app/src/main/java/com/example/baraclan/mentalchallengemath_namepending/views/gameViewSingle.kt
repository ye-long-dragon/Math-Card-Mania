package com.example.baraclan.mentalchallengemath_namepending.views

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baraclan.mentalchallengemath_namepending.models.*
import com.example.baraclan.mentalchallengemath_namepending.scripts.RandomHand
import com.example.baraclan.mentalchallengemath_namepending.scripts.formatDouble
import com.example.baraclan.mentalchallengemath_namepending.scripts.transferCards
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow
import androidx.compose.runtime.derivedStateOf


// ─────────────────────────────────────────────────────────────
// DeckPickerScreen — shown before singleplayer game starts
// ─────────────────────────────────────────────────────────────
@Composable
fun DeckPickerScreen(
    availableDecks: List<deck>,
    defaultDeck: deck,
    onDeckSelected: (deck) -> Unit,
    onNavigateBack: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenH = maxHeight
        val titleSp = (screenH.value * 0.04f).coerceIn(18f, 28f)
        val bodysSp = (screenH.value * 0.025f).coerceIn(12f, 18f)
        val btnH    = (screenH * 0.07f).coerceIn(44.dp, 60.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Choose Your Deck",
                fontFamily = Pixel,
                fontSize = titleSp.sp,
                color = BlackBoardYellow,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Use default deck button
            Button(
                onClick = { onDeckSelected(defaultDeck) },
                modifier = Modifier.fillMaxWidth().height(btnH)
            ) {
                Text("Default Deck (${defaultDeck.getTotalCount()} cards)", fontFamily = Pixel, fontSize = bodysSp.sp, color = BlackBoardYellow)
            }

            if (availableDecks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "— or your saved decks —",
                    fontFamily = Pixel,
                    fontSize = (bodysSp * 0.8f).sp,
                    color = BlackBoardYellow.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableDecks) { d ->
                        OutlinedButton(
                            onClick = { onDeckSelected(d) },
                            modifier = Modifier.fillMaxWidth().height(btnH)
                        ) {
                            Text("${d.name}  (${d.getTotalCount()} cards)", fontFamily = Pixel, fontSize = bodysSp.sp, color = BlackBoardYellow)
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onNavigateBack) {
                Text("← Back", fontFamily = Pixel, fontSize = bodysSp.sp, color = BlackBoardYellow.copy(alpha = 0.7f))
            }
        }
    }
} // end BoxWithConstraints

// ─────────────────────────────────────────────────────────────
// GameView — Single Player
// Shows difficulty select first, then runs the game.
// ─────────────────────────────────────────────────────────────
@SuppressLint("UnrememberedMutableState")
@Composable
fun GameView(
    initialDeck: deck,
    availableDecks: List<deck> = emptyList(),
    onGameComplete: (() -> Unit)? = null,
    tutorialMode: Boolean = false,
    maxRounds: Int = 10
) {
    // ── Deck picker gate ──────────────────────────────────────
    var activeDeck by remember { mutableStateOf<deck?>(if (tutorialMode) initialDeck else null) }

    if (!tutorialMode && activeDeck == null) {
        DeckPickerScreen(
            availableDecks = availableDecks,
            defaultDeck = initialDeck,
            onDeckSelected = { activeDeck = it },
            onNavigateBack = { onGameComplete?.invoke() }
        )
        return
    }

    val chosenDeck = activeDeck ?: initialDeck

    // ── Difficulty gate ───────────────────────────────────────
    var selectedDifficulty by remember { mutableStateOf<Difficulty?>(null) }

    if (!tutorialMode && selectedDifficulty == null) {
        DifficultySelectScreen(
            onDifficultySelected = { selectedDifficulty = it },
            onNavigateBack = { activeDeck = null }
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
    val deckSnapshot = remember { chosenDeck.getAllCardsWithCounts().toMap() }
    // Variables: x/y/z set once per game, a/b/c/d reset each goal
    var variableState by remember { mutableStateOf(VariableState.newGame()) }
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
        else try {
            val resolved = cards.map { variableState.resolve(it) }
            PemdasEvaluator.evaluate(resolved)
        } catch (e: Exception) { null }

    val equationResult = derivedStateOf { safeEvaluate(equationCards) }

    val startNewRound: () -> Unit = {
        variableState = variableState.newGoal()
        gameDeckState = deck(chosenDeck.name, deckSnapshot)
        playerHandState = RandomHand(gameDeckState)
        equationCards.clear()
        cardBin = collection("Card Bin")
        currentTurn = 1
    }

    fun refillHandToCapacity() {
        // Draw cards into a temp hand copy, then reassign to trigger recomposition
        val updated = hand("Player's Current Hand")
        playerHandState.getAllCardsWithCounts().forEach { (card, count) ->
            updated.addCard(card, count)
        }
        while (updated.getTotalCount() < 8 && !gameDeckState.isEmpty()) {
            gameDeckState.drawCard()?.let { updated.addCard(it, 1) }
        }
        playerHandState = updated
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
        if (playerHandState.getCardCount(card) >= 1) {
            transferCards(card, 1, playerHandState, equationCardsContainer)
            // Reassign to trigger Compose recomposition (hand is mutable class, not value type)
            playerHandState = hand("Player's Current Hand").also { h ->
                playerHandState.getAllCardsWithCounts().forEach { (c, n) -> h.addCard(c, n) }
            }
        }
    }
    val moveCardToHand: (cardGame) -> Unit = { card ->
        if (equationCardsContainer.getCardCount(card) >= 1) {
            transferCards(card, 1, equationCardsContainer, playerHandState)
            // Reassign to trigger Compose recomposition
            playerHandState = hand("Player's Current Hand").also { h ->
                playerHandState.getAllCardsWithCounts().forEach { (c, n) -> h.addCard(c, n) }
            }
        }
    }
    val onHandCardClick: (cardGame) -> Unit = { clickedCard ->
        if (draggedCard != null && dragSource == "hand") {
            moveCardToEquation(draggedCard!!); draggedCard = null; dragSource = null
        } else {
            moveCardToEquation(clickedCard)
            // Auto-add matching right paren to hand when left paren is placed
            if (clickedCard.isLeftParen()) {
                val rightParen = cardGame(
                    id = java.util.UUID.randomUUID().toString(),
                    name = ")",
                    type = cardType.PARENTHESIS,
                    operator = Operator.RIGHT_PAREN
                )
                playerHandState = hand("Player's Current Hand").also { h ->
                    playerHandState.getAllCardsWithCounts().forEach { (c, n) -> h.addCard(c, n) }
                    h.addCard(rightParen, 1)
                }
            }
        }
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
            allDeckCards = chosenDeck.getAllCardsAsList(),
            handCards = playerHandState.getAllCardsAsList(),
            binCards = cardBin.getAllCardsAsList(),
            onDismiss = { showDeckOverlay = false }
        )
    }

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenH = maxHeight
        val goalSp  = (screenH.value * 0.038f).coerceIn(16f, 28f)
        val varSp   = (screenH.value * 0.018f).coerceIn(9f, 13f)
        val labelSp = (screenH.value * 0.022f).coerceIn(10f, 15f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = statusBarPadding.calculateTopPadding(),
                    bottom = navBarPadding.calculateBottomPadding()
                ),
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
            // Goal with responsive font
            if (goalsToUse.isNotEmpty() && currentGoalIndex < goalsToUse.size) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Current Goal:", fontFamily = Pixel, fontSize = labelSp.sp,
                        color = BlackBoardYellow.copy(alpha = 0.7f))
                    Text(goalsToUse[currentGoalIndex].toString(), fontFamily = Pixel,
                        fontSize = goalSp.sp, color = BlackBoardYellow)
                }
            }

            Text(
                text = variableState.displayString(),
                fontSize = varSp.sp,
                color = BlackBoardYellow.copy(alpha = 0.75f),
                fontFamily = Pixel,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                        val resolved = equationCards.map { variableState.resolve(it) }
                        val resultDouble = PemdasEvaluator.evaluate(resolved)
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



}