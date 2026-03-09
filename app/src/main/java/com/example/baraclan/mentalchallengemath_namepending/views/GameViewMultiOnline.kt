package com.example.baraclan.mentalchallengemath_namepending.views

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baraclan.mentalchallengemath_namepending.data.GameMode
import com.example.baraclan.mentalchallengemath_namepending.data.LobbyPlayer
import com.example.baraclan.mentalchallengemath_namepending.data.LobbyStatus
import com.example.baraclan.mentalchallengemath_namepending.data.OnlineLobbyRepository
import com.example.baraclan.mentalchallengemath_namepending.models.*
import com.example.baraclan.mentalchallengemath_namepending.scripts.RandomHand
import com.example.baraclan.mentalchallengemath_namepending.scripts.transferCards
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
// GameViewMultiOnline
// Mirrors GameView exactly — PemdasEvaluator, margin of error,
// refillHandToCapacity, drag/drop support.
// Adds: live Firestore sync, waiting screen, standings.
// ─────────────────────────────────────────────────────────────
@SuppressLint("UnrememberedMutableState")
@Composable
fun GameViewMultiOnline(
    lobbyId: String,
    playerDeck: deck,
    onReturnToMenu: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val lobby by OnlineLobbyRepository.listenToLobby(lobbyId)
        .collectAsState(initial = null)

    val currentUid = OnlineLobbyRepository.currentUid
    val isHost = lobby?.hostUid == currentUid
    val gameMode = lobby?.settings?.gameMode ?: GameMode.STOPWATCH
    val gameGoals = lobby?.gameGoals ?: emptyList()
    val players = lobby?.players ?: emptyMap()

    // ── Game state (mirrors GameView) ─────────────────────────
    var currentScore by remember { mutableStateOf(0) }
    var currentTurn by remember { mutableStateOf(1) }
    var currentGoalIndex by remember { mutableStateOf(0) }
    val deckSnapshot = remember { playerDeck.getAllCardsWithCounts().toMap() }
    var gameDeckState by remember { mutableStateOf(deck("Online Game Deck")) }
    var playerHandState by remember { mutableStateOf(hand("Online Hand")) }
    val equationCards = remember { mutableStateListOf<cardGame>() }
    var cardBin by remember { mutableStateOf(collection("Card Bin")) }
    var draggedCard by remember { mutableStateOf<cardGame?>(null) }
    var dragSource by remember { mutableStateOf<String?>(null) }
    var iFinished by remember { mutableStateOf(false) }
    var showDeckOverlay by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    // ── Timers ────────────────────────────────────────────────
    var elapsedSeconds by remember { mutableStateOf(0) }
    var roundTimeLeft by remember { mutableStateOf(60) }

    // Stopwatch counts up until finished
    LaunchedEffect(iFinished) {
        if (!iFinished) {
            while (!iFinished) {
                delay(1000)
                elapsedSeconds += 1
            }
        }
    }

    // Timed round countdown
    LaunchedEffect(currentGoalIndex, gameMode) {
        if (gameMode == GameMode.TIMED_ROUND && !iFinished) {
            roundTimeLeft = lobby?.settings?.roundTimerSeconds ?: 60
            while (roundTimeLeft > 0 && !iFinished) {
                delay(1000)
                roundTimeLeft -= 1
            }
            if (roundTimeLeft <= 0 && !iFinished) {
                equationCards.forEach { cardBin.addCard(it, 1) }
                equationCards.clear()
                currentGoalIndex++
                currentTurn = 1
                if (currentGoalIndex >= gameGoals.size) {
                    iFinished = true
                    scope.launch {
                        OnlineLobbyRepository.submitFinish(lobbyId, elapsedSeconds, currentScore)
                        if (isHost) { delay(3000); OnlineLobbyRepository.endGame(lobbyId) }
                    }
                }
            }
        }
    }

    // ── Margin of error (matches GameView exactly) ────────────
    fun getMarginOfError(round: Int): Double = when {
        round >= 9 -> 0.15
        round >= 7 -> 0.10
        round >= 4 -> 0.05
        else -> 0.0
    }

    // ── Safe PEMDAS evaluation ────────────────────────────────
    fun safeEvaluate(cards: List<cardGame>): Double? =
        if (cards.isEmpty()) null
        else try { PemdasEvaluator.evaluate(cards) } catch (e: Exception) { null }

    val equationResult = derivedStateOf { safeEvaluate(equationCards) }

    // ── Refill hand to 8 cards (matches GameView) ─────────────
    fun refillHandToCapacity() {
        while (playerHandState.getTotalCount() < 8 && !gameDeckState.isEmpty()) {
            val drawn = gameDeckState.drawCard()
            if (drawn != null) playerHandState.addCard(drawn, 1)
        }
    }

    fun canDiscard() = equationCards.isNotEmpty()

    // ── Start new round ───────────────────────────────────────
    val startNewRound: () -> Unit = {
        if (currentGoalIndex < gameGoals.size) {
            gameDeckState = deck(playerDeck.name, deckSnapshot)
            playerHandState = RandomHand(gameDeckState)
            equationCards.clear()
            cardBin = collection("Card Bin")
            currentTurn = 1
        } else {
            iFinished = true
            scope.launch {
                OnlineLobbyRepository.submitFinish(lobbyId, elapsedSeconds, currentScore)
                if (isHost) { delay(3000); OnlineLobbyRepository.endGame(lobbyId) }
            }
        }
    }

    LaunchedEffect(gameGoals) {
        if (gameGoals.isNotEmpty()) startNewRound()
    }

    // ── Equation container adapter (matches GameView) ─────────
    val equationCardsContainer = object : cardContainer("Equation Cards") {
        override fun addCard(card: cardGame, count: Int) {
            repeat(count) { equationCards.add(card) }
        }
        override fun removeCard(card: cardGame, count: Int) {
            var removed = 0
            val iter = equationCards.iterator()
            while (iter.hasNext() && removed < count) {
                val c = iter.next()
                if (c === card || (c.id == card.id && c.type == card.type)) {
                    iter.remove(); removed++
                }
            }
        }
        override fun getCardCount(card: cardGame) =
            equationCards.count { c -> c === card || (c.id == card.id && c.type == card.type) }
    }

    // ── Card movement (matches GameView) ─────────────────────
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
    }
    val onHandCardLongPress: (cardGame) -> Unit = { card ->
        if (playerHandState.contains(card)) { draggedCard = card; dragSource = "hand" }
    }
    val onEquationCardClick: (cardGame) -> Unit = { clickedCard ->
        if (draggedCard != null && dragSource == "equation") {
            moveCardToHand(draggedCard!!); draggedCard = null; dragSource = null
        } else moveCardToHand(clickedCard)
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

    // ── Submit logic (matches GameView exactly) ───────────────
    val onSubmit: () -> Unit = {
        try {
            val resultDouble = PemdasEvaluator.evaluate(equationCards)
            equationCards.forEach { cardBin.addCard(it, 1) }
            equationCards.clear()

            val currentGoal = gameGoals.getOrNull(currentGoalIndex)
            if (currentGoal != null) {
                val round = (currentGoalIndex + 1).coerceIn(1, 10)
                val margin = getMarginOfError(round)
                val hit = if (margin == 0.0)
                    kotlin.math.abs(resultDouble - currentGoal) < 0.0001
                else
                    resultDouble in (currentGoal * (1 - margin))..(currentGoal * (1 + margin))

                if (hit) {
                    currentScore += 100
                    currentGoalIndex++
                    startNewRound()
                } else {
                    currentTurn++
                }
            } else {
                startNewRound()
            }
        } catch (e: Exception) {
            equationCards.forEach { cardBin.addCard(it, 1) }
            equationCards.clear()
            currentTurn++
        }
        refillHandToCapacity()
    }

    // ── Standings (everyone done) ─────────────────────────────
    if (lobby?.status == LobbyStatus.FINISHED) {
        OnlineStandingsScreen(
            players = players.values.toList(),
            gameMode = gameMode,
            onReturnToMenu = onReturnToMenu
        )
        return
    }

    // ── Waiting (I'm done, others aren't) ─────────────────────
    if (iFinished && lobby?.status != LobbyStatus.FINISHED) {
        val finishedCount = players.values.count { it.finishTimeSeconds != null }
        OnlineWaitingScreen(
            myScore = currentScore,
            myTimeSeconds = elapsedSeconds,
            finishedCount = finishedCount,
            totalCount = players.size
        )
        return
    }

    // ── Loading goals ─────────────────────────────────────────
    if (gameGoals.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BlackBoardYellow)
        }
        return
    }

    // ── Settings overlay ─────────────────────────────────────
    if (showSettings) {
        SettingsScreen(onNavigateBack = { showSettings = false })
        return
    }

    // ── Deck overlay ──────────────────────────────────────────
    if (showDeckOverlay) {
        ShowDeckOverlay(
            allDeckCards = playerDeck.getAllCardsAsList(),
            handCards = playerHandState.getAllCardsAsList(),
            binCards = cardBin.getAllCardsAsList(),
            onDismiss = { showDeckOverlay = false }
        )
    }

    // ── Main game UI — mirrors GameView layout exactly ────────
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top bar + status
        Column {
            GameTopBar(
                onReturnToMenu = onReturnToMenu,
                onShowDeck = { showDeckOverlay = true },
                onOpenSettings = { showSettings = true }
            )
            statusBar(currentScore, currentTurn)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Goal ${currentGoalIndex + 1}/${gameGoals.size}",
                    fontFamily = Pixel, color = BlackBoardYellow, fontSize = 12.sp
                )
                Text(
                    text = if (gameMode == GameMode.STOPWATCH)
                        "⏱ %02d:%02d".format(minutes, seconds)
                    else "⏰ ${roundTimeLeft}s",
                    fontFamily = Pixel,
                    color = if (gameMode == GameMode.TIMED_ROUND && roundTimeLeft <= 10)
                        Color.Red else BlackBoardYellow,
                    fontSize = 14.sp
                )
                val finishedCount = players.values.count { it.finishTimeSeconds != null }
                Text(
                    text = "✓ $finishedCount/${players.size}",
                    fontFamily = Pixel,
                    color = BlackBoardYellow.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        goal(gameGoals.subList(currentGoalIndex, gameGoals.size))

        Spacer(modifier = Modifier.height(16.dp))

        // Equation display with PEMDAS live result
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
                        text = "= ${com.example.baraclan.mentalchallengemath_namepending.scripts.formatDouble(result)}",
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

        // Submit / Discard — matches GameView exactly
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onSubmit) {
                Text("Submit", fontFamily = Pixel, color = BlackBoardYellow)
            }
            Button(
                onClick = {
                    if (canDiscard()) {
                        equationCards.forEach { cardBin.addCard(it, 1) }
                        equationCards.clear()
                        refillHandToCapacity()
                        currentTurn++
                    }
                },
                enabled = canDiscard()
            ) {
                Text("Discard", fontFamily = Pixel, color = BlackBoardYellow)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Waiting Screen
// ─────────────────────────────────────────────────────────────
@Composable
fun OnlineWaitingScreen(
    myScore: Int,
    myTimeSeconds: Int,
    finishedCount: Int,
    totalCount: Int
) {
    val minutes = myTimeSeconds / 60
    val seconds = myTimeSeconds % 60
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎉", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("You finished!", fontFamily = Pixel, color = BlackBoardYellow, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Your time: %02d:%02d".format(minutes, seconds), fontFamily = Pixel, color = BlackBoardYellow, fontSize = 18.sp)
        Text("Your score: $myScore", fontFamily = Pixel, color = BlackBoardYellow, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(32.dp))
        Text("Waiting for other players...", fontFamily = Pixel, color = BlackBoardYellow.copy(alpha = 0.7f), fontSize = 14.sp)
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { finishedCount.toFloat() / totalCount.toFloat() },
            modifier = Modifier.fillMaxWidth(0.7f).height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = BlackBoardYellow
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("$finishedCount / $totalCount players finished", fontFamily = Pixel, color = BlackBoardYellow.copy(alpha = 0.6f), fontSize = 13.sp)
    }
}

// ─────────────────────────────────────────────────────────────
// Standings Screen
// ─────────────────────────────────────────────────────────────
@Composable
fun OnlineStandingsScreen(
    players: List<LobbyPlayer>,
    gameMode: GameMode,
    onReturnToMenu: () -> Unit
) {
    val sorted = players.sortedWith(
        compareBy<LobbyPlayer> { it.finishTimeSeconds == null }
            .thenBy { it.finishTimeSeconds ?: Int.MAX_VALUE }
            .thenByDescending { it.score }
    )
    val medals = listOf("🥇", "🥈", "🥉")
    val currentUid = OnlineLobbyRepository.currentUid

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Final Standings", fontFamily = Pixel, color = BlackBoardYellow, fontSize = 26.sp,
            modifier = Modifier.padding(bottom = 24.dp))

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(sorted) { index, player ->
                val isMe = player.uid == currentUid
                val medal = medals.getOrElse(index) { "${index + 1}." }
                val timeText = player.finishTimeSeconds?.let {
                    "%02d:%02d".format(it / 60, it % 60)
                } ?: "DNF"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(medal, fontSize = 22.sp, modifier = Modifier.width(40.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            player.username + if (isMe) " (you)" else "",
                            fontFamily = Pixel, color = BlackBoardYellow, fontSize = 15.sp
                        )
                        Text("Score: ${player.score}", fontFamily = Pixel,
                            color = BlackBoardYellow.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                    Text(
                        text = timeText,
                        fontFamily = Pixel,
                        color = if (timeText == "DNF") Color.Red else BlackBoardYellow,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onReturnToMenu, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Return to Menu", fontFamily = Pixel, color = BlackBoardYellow, fontSize = 16.sp)
        }
    }
}