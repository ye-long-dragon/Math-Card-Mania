package com.example.baraclan.mentalchallengemath_namepending.views

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.baraclan.mentalchallengemath_namepending.models.*
import com.example.baraclan.mentalchallengemath_namepending.scripts.RandomHand
import com.example.baraclan.mentalchallengemath_namepending.scripts.formatDouble
import com.example.baraclan.mentalchallengemath_namepending.scripts.transferCards
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow

// ─────────────────────────────────────────────────────────────
// Tutorial steps
// ─────────────────────────────────────────────────────────────
enum class TutorialStep {
    INTRO,                   // 0 — Darken screen, explain top bar (score/turn)
    EXPLAIN_GOAL,            // 1 — Highlight current goal
    EXPLAIN_HAND,            // 2 — Highlight cards in hand
    TASK_PLACE_IN_EQUATION,  // 3 — Player must drag a card to equation, then discard
    TASK_SUBMIT_EQUATION,    // 4 — Player must place a card and submit
    EXPLAIN_SHOW_DECK,       // 5 — Explain Show Deck button + card opacity meanings
    DONE                     // 6 — Tutorial complete → navigate back
}

// ─────────────────────────────────────────────────────────────
// TutorialView
// ─────────────────────────────────────────────────────────────
@SuppressLint("UnrememberedMutableState")
@Composable
fun TutorialView(
    initialDeck: deck,
    onNavigateBack: () -> Unit
) {
    // ── Tutorial goals: easy, rounds 1 / 3 / 5 (indices 0,20,40) ──
    val tutorialGoals = remember {
        val all = GoalGenerator.generateAllGoals(Difficulty.EASY)
        listOf(all[0], all[20], all[40])   // round 1, round 3, round 5
    }

    // ── Game state ────────────────────────────────────────────
    var currentScore by remember { mutableStateOf(0) }
    var currentTurn by remember { mutableStateOf(1) }
    var currentGoalIndex by remember { mutableStateOf(0) }
    val deckSnapshot = remember { initialDeck.getAllCardsWithCounts().toMap() }
    var gameDeckState by remember { mutableStateOf(deck("Tutorial Deck")) }
    var playerHandState by remember { mutableStateOf(hand("Tutorial Hand")) }
    val equationCards = remember { mutableStateListOf<cardGame>() }
    var cardBin by remember { mutableStateOf(collection("Card Bin")) }
    var showDeckOverlay by remember { mutableStateOf(false) }

    // ── Tutorial step ─────────────────────────────────────────
    var tutorialStep by remember { mutableStateOf(TutorialStep.INTRO) }

    // ── Round setup ───────────────────────────────────────────
    val startNewRound: () -> Unit = {
        gameDeckState = deck(initialDeck.name, deckSnapshot)
        playerHandState = RandomHand(gameDeckState)
        equationCards.clear()
        cardBin = collection("Card Bin")
        currentTurn = 1
    }

    fun refillHand() {
        while (playerHandState.getTotalCount() < 8 && !gameDeckState.isEmpty())
            gameDeckState.drawCard()?.let { playerHandState.addCard(it, 1) }
    }

    LaunchedEffect(Unit) { startNewRound() }

    // ── Equation container adapter ────────────────────────────
    val equationContainer = object : cardContainer("Equation Cards") {
        override fun addCard(card: cardGame, count: Int) { repeat(count) { equationCards.add(card) } }
        override fun removeCard(card: cardGame, count: Int) {
            var r = 0; val it = equationCards.iterator()
            while (it.hasNext() && r < count) {
                val c = it.next()
                if (c === card || (c.id == card.id && c.type == card.type)) { it.remove(); r++ }
            }
        }
        override fun getCardCount(card: cardGame) =
            equationCards.count { c -> c === card || (c.id == card.id && c.type == card.type) }
    }

    val safeEval: () -> Double? = {
        try { if (equationCards.isEmpty()) null else PemdasEvaluator.evaluate(equationCards) }
        catch (e: Exception) { null }
    }
    val equationResult = derivedStateOf { safeEval() }

    // ── Card interaction (locked during tutorial overlays) ────
    val overlayActive = tutorialStep != TutorialStep.TASK_PLACE_IN_EQUATION &&
            tutorialStep != TutorialStep.TASK_SUBMIT_EQUATION &&
            tutorialStep != TutorialStep.DONE

    val onHandCardClick: (cardGame) -> Unit = { card ->
        if (!overlayActive) {
            if (playerHandState.getCardCount(card) >= 1) {
                transferCards(card, 1, playerHandState, equationContainer)
                SoundManager.playPlace()
                // Step 3 — once a card is in equation, allow discard prompt
            }
        }
    }
    val onEquationCardClick: (cardGame) -> Unit = { card ->
        if (!overlayActive) {
            if (equationContainer.getCardCount(card) >= 1) {
                transferCards(card, 1, equationContainer, playerHandState)
                SoundManager.playSlide()
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // Deck overlay
    // ─────────────────────────────────────────────────────────
    if (showDeckOverlay) {
        ShowDeckOverlay(
            allDeckCards = initialDeck.getAllCardsAsList(),
            handCards = playerHandState.getAllCardsAsList(),
            binCards = cardBin.getAllCardsAsList(),
            onDismiss = { showDeckOverlay = false }
        )
    }

    // ─────────────────────────────────────────────────────────
    // Main layout — game UI always rendered underneath
    // ─────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {

        // ── Game UI ───────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar
            GameTopBar(
                onReturnToMenu = onNavigateBack,
                onShowDeck = { showDeckOverlay = true },
                onOpenSettings = {}
            )

            // Status bar
            statusBar(currentScore, currentTurn)

            // Round indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tutorial Round ${currentGoalIndex + 1}/3", fontFamily = Pixel, color = BlackBoardYellow, fontSize = 12.sp)
                Text("Easy", fontFamily = Pixel, color = Color(0xFF4CAF50), fontSize = 12.sp)
            }

            // Goal
            goal(tutorialGoals.subList(currentGoalIndex, tutorialGoals.size))

            // Equation area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !overlayActive) {},
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EquationDisplay(
                    equationElements = equationCards,
                    onCardClick = onEquationCardClick,
                    onCardLongPress = {}
                )
                if (equationCards.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    equationResult.value?.let {
                        Text("= ${formatDouble(it)}", style = MaterialTheme.typography.titleLarge,
                            color = BlackBoardYellow, fontFamily = Pixel)
                    } ?: Text("Invalid equation", style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red, fontFamily = Pixel)
                }
            }

            Text("Select cards from your hand:", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp), fontFamily = Pixel, color = BlackBoardYellow)

            InputCardsDisplay(
                playerHand = playerHandState,
                onCardClick = onHandCardClick,
                onCardLongPress = {}
            )

            // Submit / Discard buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (!overlayActive) {
                            try {
                                val result = PemdasEvaluator.evaluate(equationCards)
                                equationCards.forEach { cardBin.addCard(it, 1) }
                                equationCards.clear()
                                val goal = tutorialGoals.getOrNull(currentGoalIndex)
                                if (goal != null && kotlin.math.abs(result - goal) < 0.0001) {
                                    currentScore += 100
                                    currentGoalIndex++
                                    if (currentGoalIndex >= tutorialGoals.size) {
                                        tutorialStep = TutorialStep.DONE
                                    } else {
                                        startNewRound()
                                    }
                                } else {
                                    currentTurn++
                                }
                            } catch (e: Exception) {
                                equationCards.forEach { cardBin.addCard(it, 1) }
                                equationCards.clear()
                                currentTurn++
                            }
                            refillHand()
                            // After first submit, advance tutorial if on step 4
                            if (tutorialStep == TutorialStep.TASK_SUBMIT_EQUATION)
                                tutorialStep = TutorialStep.EXPLAIN_SHOW_DECK
                        }
                    },
                    enabled = !overlayActive || tutorialStep == TutorialStep.TASK_SUBMIT_EQUATION
                ) {
                    Text("Submit", fontFamily = Pixel, color = BlackBoardYellow)
                }
                Button(
                    onClick = {
                        if (!overlayActive && equationCards.isNotEmpty()) {
                            equationCards.forEach { cardBin.addCard(it, 1) }
                            equationCards.clear()
                            refillHand()
                            currentTurn++
                            // After discard in step 3, advance to step 4
                            if (tutorialStep == TutorialStep.TASK_PLACE_IN_EQUATION)
                                tutorialStep = TutorialStep.TASK_SUBMIT_EQUATION
                        }
                    },
                    enabled = (!overlayActive && equationCards.isNotEmpty()) ||
                            (tutorialStep == TutorialStep.TASK_PLACE_IN_EQUATION && equationCards.isNotEmpty())
                ) {
                    Text("Discard", fontFamily = Pixel, color = BlackBoardYellow)
                }
            }
        }

        // ─────────────────────────────────────────────────────
        // Tutorial overlay — darken + spotlight + tooltip
        // ─────────────────────────────────────────────────────
        when (tutorialStep) {
            TutorialStep.INTRO -> TutorialOverlay(
                spotlightArea = SpotlightArea.TOP_BAR,
                title = "Score & Turn",
                message = "This bar shows your current score and how many turns you've used this round. Each wrong answer costs a turn.",
                buttonLabel = "Next",
                onAction = { tutorialStep = TutorialStep.EXPLAIN_GOAL }
            )

            TutorialStep.EXPLAIN_GOAL -> TutorialOverlay(
                spotlightArea = SpotlightArea.GOAL,
                title = "Current Goal",
                message = "This is the number your equation must equal. Try to build an equation that reaches this exact value!",
                buttonLabel = "Next",
                onAction = { tutorialStep = TutorialStep.EXPLAIN_HAND }
            )

            TutorialStep.EXPLAIN_HAND -> TutorialOverlay(
                spotlightArea = SpotlightArea.HAND,
                title = "Your Hand",
                message = "These are your cards! Tap a card to move it into the equation area. Each card is a number, operator, or function.",
                buttonLabel = "Got it — Let me try!",
                onAction = { tutorialStep = TutorialStep.TASK_PLACE_IN_EQUATION }
            )

            TutorialStep.TASK_PLACE_IN_EQUATION -> TutorialTask(
                instruction = "Tap any card from your hand to add it to the equation, then tap Discard to remove it.",
                highlightArea = SpotlightArea.HAND
            )

            TutorialStep.TASK_SUBMIT_EQUATION -> TutorialTask(
                instruction = "Now place one or more cards to form an equation, then tap Submit!",
                highlightArea = SpotlightArea.HAND
            )

            TutorialStep.EXPLAIN_SHOW_DECK -> TutorialOverlay(
                spotlightArea = SpotlightArea.TOP_BAR,
                title = "Show Deck",
                message = "Tap the card icon (🃏) in the top bar to view your full deck.\n\n• Normal card = still in deck\n• Glowing yellow border = card is in your hand\n• 50% faded = card has been used or discarded",
                buttonLabel = "Start Playing!",
                onAction = { tutorialStep = TutorialStep.DONE }
            )

            TutorialStep.DONE -> {
                LaunchedEffect(Unit) { onNavigateBack() }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Spotlight areas — which region of screen to keep bright
// ─────────────────────────────────────────────────────────────
enum class SpotlightArea {
    TOP_BAR,   // top ~15% of screen
    GOAL,      // middle-top area
    HAND,      // bottom ~35% of screen
    EQUATION   // middle section
}

// ─────────────────────────────────────────────────────────────
// TutorialOverlay — darkens screen except spotlighted area
// ─────────────────────────────────────────────────────────────
@Composable
fun TutorialOverlay(
    spotlightArea: SpotlightArea,
    title: String,
    message: String,
    buttonLabel: String,
    onAction: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().zIndex(10f)) {

        // ── Dark regions (everything except spotlight) ────────
        val darkColor = Color.Black.copy(alpha = 0.75f)

        when (spotlightArea) {
            SpotlightArea.TOP_BAR -> {
                // Dark below the top bar
                Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f)
                    .align(Alignment.BottomCenter).background(darkColor))
            }
            SpotlightArea.GOAL -> {
                // Dark above (top 15%) and below (bottom 50%)
                Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.15f)
                    .align(Alignment.TopCenter).background(darkColor))
                Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.50f)
                    .align(Alignment.BottomCenter).background(darkColor))
            }
            SpotlightArea.HAND -> {
                // Dark above (top 65%)
                Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.62f)
                    .align(Alignment.TopCenter).background(darkColor))
            }
            SpotlightArea.EQUATION -> {
                Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.30f)
                    .align(Alignment.TopCenter).background(darkColor))
                Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.35f)
                    .align(Alignment.BottomCenter).background(darkColor))
            }
        }

        // ── Tooltip card ──────────────────────────────────────
        val tooltipAlign = when (spotlightArea) {
            SpotlightArea.TOP_BAR -> Alignment.Center
            SpotlightArea.GOAL -> Alignment.BottomCenter
            SpotlightArea.HAND -> Alignment.TopCenter
            SpotlightArea.EQUATION -> Alignment.BottomCenter
        }
        val tooltipPadding = when (spotlightArea) {
            SpotlightArea.TOP_BAR -> PaddingValues(horizontal = 24.dp)
            SpotlightArea.GOAL -> PaddingValues(bottom = 40.dp, start = 24.dp, end = 24.dp)
            SpotlightArea.HAND -> PaddingValues(top = 24.dp, start = 24.dp, end = 24.dp)
            SpotlightArea.EQUATION -> PaddingValues(bottom = 40.dp, start = 24.dp, end = 24.dp)
        }

        Card(
            modifier = Modifier
                .align(tooltipAlign)
                .padding(tooltipPadding)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontFamily = Pixel,
                    color = BlackBoardYellow,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = message,
                    fontFamily = Pixel,
                    color = BlackBoardYellow.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(buttonLabel, fontFamily = Pixel, color = BlackBoardYellow)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TutorialTask — semi-transparent instruction banner only
// (screen is mostly usable, just shows a floating instruction)
// ─────────────────────────────────────────────────────────────
@Composable
fun TutorialTask(
    instruction: String,
    highlightArea: SpotlightArea
) {
    // Pulsing border animation on the highlighted area
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(modifier = Modifier.fillMaxSize().zIndex(10f)) {
        // Subtle pulsing glow on the hand area to guide player
        if (highlightArea == SpotlightArea.HAND) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.38f)
                    .align(Alignment.BottomCenter)
                    .border(3.dp, BlackBoardYellow.copy(alpha = alpha), RoundedCornerShape(0.dp))
            )
        }

        // Instruction banner at top
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 70.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.93f)
            ),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("👆", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = instruction,
                    fontFamily = Pixel,
                    color = BlackBoardYellow,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}