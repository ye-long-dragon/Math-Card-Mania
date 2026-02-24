package com.example.baraclan.mentalchallengemath_namepending.views

import android.annotation.SuppressLint
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
import androidx.compose.ui.graphics.Color
import com.example.baraclan.mentalchallengemath_namepending.views.*
import com.example.baraclan.mentalchallengemath_namepending.models.*
import com.example.baraclan.mentalchallengemath_namepending.scripts.RandomHand
import com.example.baraclan.mentalchallengemath_namepending.scripts.transferCards
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow
import com.example.baraclan.mentalchallengemath_namepending.models.PemdasEvaluator
import androidx.compose.foundation.clickable
import androidx.compose.runtime.derivedStateOf

val gameGoal: List<Double> = listOf(
    4.0,        // Round 1
    -0.12,       // Round 2
    -3.6,        // Round 3
    0.046,      // Round 4 (±5%)
    5.76,       // Round 5 (±5%)
    -0.072,      // Round 6 (±5%)
    -14.4,        // Round 7 (±10%)
    0.58,       // Round 8 (±10%)
    51.84,       // Round 9 (±15%)
    -2.68        // Round 10 (±15%)
)

@SuppressLint("UnrememberedMutableState")
@Composable
public fun GameView(
    initialDeck: deck, // Player's persistent deck from MainActivity
    onGameComplete: (() -> Unit)? = null, // Callback when game is completed
    tutorialMode: Boolean = false, // If true, only show first 3 rounds
    maxRounds: Int = 10 // Maximum rounds (3 for tutorial)
){
    // Game state variables
    var currentScore by remember { mutableStateOf(0) }
    var currentTurn by remember { mutableStateOf(1) }
    var currentGoalIndex by remember { mutableStateOf(0) }
    var showCongratulations by remember { mutableStateOf(false) }

    // Active deck for the current game session (a mutable copy)
    var gameDeckState by remember { mutableStateOf(deck("Game Deck Copy")) }

    // Player's current hand of cards
    var playerHandState by remember { mutableStateOf(hand("Player's Current Hand")) }

    // Cards currently forming the equation (fixed: should be List<cardGame>, not List<hand>)
    val equationCards = remember { mutableStateListOf<cardGame>() }
    
    // Temporary card bin for discarded/submitted cards (collection type)
    var cardBin by remember { mutableStateOf(collection("Card Bin")) }
    
    // Drag and drop state
    var draggedCard by remember { mutableStateOf<cardGame?>(null) }
    var dragSource by remember { mutableStateOf<String?>(null) } // "hand" or "equation"
    
    // Calculate current round (1-10)
    val currentRound = (currentGoalIndex + 1).coerceIn(1, 10)
    
    // Calculate margin of error based on round
    fun getMarginOfError(round: Int): Double {
        return when {
            round >= 9 -> 0.15 // ±15% for rounds 9-10
            round >= 7 -> 0.10 // ±10% for rounds 7-8
            round >= 4 -> 0.05 // ±5% for rounds 4-6
            else -> 0.0 // No margin for rounds 1-3
        }
    }
    
    // Helper function to safely evaluate equation
    fun safeEvaluateEquation(cards: List<cardGame>): Int? {
        return if (cards.isEmpty()) {
            null
        } else {
            try {
                PemdasEvaluator.evaluate(cards)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    // Compute equation result safely using derivedStateOf to track list changes
    val equationResult = derivedStateOf {
        safeEvaluateEquation(equationCards)
    }


    // Function to set up a new round
    val startNewRound: () -> Unit = {
        // Copy the initial deck for a fresh start to this round
        gameDeckState = deck(initialDeck.name, initialDeck.getAllCardsWithCounts())
        println("New round: Game deck reset to ${gameDeckState.getTotalCount()} cards.")

        // Draw a new hand from the game deck
        playerHandState = RandomHand(gameDeckState)
        println("New hand drawn. Hand size: ${playerHandState.getTotalCount()}. Deck remaining: ${gameDeckState.getTotalCount()}")

        // Clear previous equation, card bin, and reset turn
        equationCards.clear()
        cardBin = collection("Card Bin")
        currentTurn = 1
    }
    
    // Refill hand from deck until hand has 8 cards
    fun refillHandToCapacity() {
        val handCapacity = 8
        while (playerHandState.getTotalCount() < handCapacity && !gameDeckState.isEmpty()) {
            val drawnCard = gameDeckState.drawCard()
            if (drawnCard != null) {
                playerHandState.addCard(drawnCard, 1)
            }
        }
    }
    
    // Discard is allowed any time there is at least one card in the equation
    fun canDiscardEquation(): Boolean {
        return equationCards.isNotEmpty()
    }

    // Start the first round when GameView is launched
    LaunchedEffect(Unit) {
        startNewRound()
    }

    // Adapter: equationCards (MutableList<cardGame>) as cardContainer for transferCards
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
                    iter.remove()
                    removed++
                }
            }
        }
        override fun getCardCount(card: cardGame): Int {
            return equationCards.count { c -> c === card || (c.id == card.id && c.type == card.type) }
        }
    }
    
    // Handle card movement with drag and drop support
    // Player can place any cards in any order; validity is only checked on Submit.
    val moveCardToEquation: (cardGame) -> Unit = { card ->
        if (playerHandState.getCardCount(card) >= 1) {
            // Always allow the card to be added to the equation.
            // Structural / mathematical validity is enforced only during evaluation on Submit.
            transferCards(card, 1, playerHandState, equationCardsContainer)
            // No cards added to hand here; hand is only refilled when Discard is pressed.
        }
    }
    
    val moveCardToHand: (cardGame) -> Unit = { card ->
        if (equationCardsContainer.getCardCount(card) >= 1) {
            transferCards(card, 1, equationCardsContainer, playerHandState)
        }
    }
    
    // Move card from hand to equation (click handler)
    val onHandCardClick: (cardGame) -> Unit = { clickedCard ->
        if (draggedCard != null && dragSource == "hand") {
            // If dragging, try to drop on equation
            moveCardToEquation(draggedCard!!)
            draggedCard = null
            dragSource = null
        } else {
            // Normal click: move to equation
            moveCardToEquation(clickedCard)
        }
    }
    
    // Long press handler for hand cards
    val onHandCardLongPress: (cardGame) -> Unit = { card ->
        if (playerHandState.contains(card)) {
            draggedCard = card
            dragSource = "hand"
        }
    }
    
    // Move card from equation to hand (click handler)
    val onEquationCardClick: (cardGame) -> Unit = { clickedCard ->
        if (draggedCard != null && dragSource == "equation") {
            // If dragging, try to drop on hand
            moveCardToHand(draggedCard!!)
            draggedCard = null
            dragSource = null
        } else {
            // Normal click: move to hand
            moveCardToHand(clickedCard)
        }
    }
    
    // Long press handler for equation cards
    val onEquationCardLongPress: (cardGame) -> Unit = { card ->
        if (equationCards.contains(card)) {
            draggedCard = card
            dragSource = "equation"
        }
    }
    
    // Handle dropping on equation area
    val onEquationAreaClick: () -> Unit = {
        if (draggedCard != null && dragSource == "hand") {
            moveCardToEquation(draggedCard!!)
            draggedCard = null
            dragSource = null
        }
    }
    
    // Handle dropping on hand area
    val onHandAreaClick: () -> Unit = {
        if (draggedCard != null && dragSource == "equation") {
            moveCardToHand(draggedCard!!)
            draggedCard = null
            dragSource = null
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween

    ) {
        statusBar(currentScore, currentTurn)

        Spacer(modifier = Modifier.height(16.dp))

        val goalsToUse = if (tutorialMode) gameGoal.take(maxRounds) else gameGoal
        goal(goalsToUse.subList(currentGoalIndex, goalsToUse.size))

        Spacer(modifier = Modifier.height(16.dp))

        // Display equation with PEMDAS evaluation
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
                        text = "= $result",
                        style = MaterialTheme.typography.titleLarge,
                        color = BlackBoardYellow,
                        fontFamily = Pixel
                    )
                } ?: run {
                    Text(
                        text = "Invalid equation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red,
                        fontFamily = Pixel
                    )
                }
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                try {
                    // Use PEMDAS evaluator for actual equation evaluation
                    val equationResult: Int = PemdasEvaluator.evaluate(equationCards)
                    val equationResultDouble = equationResult.toDouble()
                    println("Submitted: ${equationCards.joinToString(" ")} = $equationResultDouble")

                    // Move submitted cards to card bin
                    equationCards.forEach { card ->
                        cardBin.addCard(card, 1)
                    }
                    equationCards.clear()

                    val goalsToUse = if (tutorialMode) gameGoal.take(maxRounds) else gameGoal
                    val currentGoal = goalsToUse.getOrNull(currentGoalIndex)
                    if (currentGoal != null) {
                        val margin = getMarginOfError(currentRound)
                        val lowerBound = currentGoal * (1 - margin)
                        val upperBound = currentGoal * (1 + margin)
                        val isWithinMargin = equationResultDouble >= lowerBound && equationResultDouble <= upperBound
                        
                        if (isWithinMargin || (margin == 0.0 && kotlin.math.abs(equationResultDouble - currentGoal) < 0.0001)) {
                            println("Goal reached! Round ${currentRound + 1}")
                            currentScore += 100
                            currentGoalIndex++
                            
                            // Check if all goals are completed
                            if (currentGoalIndex >= goalsToUse.size) {
                                println("All goals completed! Congratulations!")
                                showCongratulations = true
                            } else {
                                startNewRound() // Reset for next round
                                println("Round completed, starting new one.")
                            }
                        } else {
                            println("Goal NOT reached. Expected: $currentGoal (±${margin * 100}%), Got: $equationResultDouble")
                            currentTurn++
                        }
                    } else {
                        println("No more goals!")
                        showCongratulations = true
                    }
                } catch (e: Exception) {
                    println("Invalid equation: ${e.message}")
                    // Still move cards to bin even if invalid
                    equationCards.forEach { card ->
                        cardBin.addCard(card, 1)
                    }
                    equationCards.clear()
                    currentTurn++
                }

                // After any submit (success or failure), refill hand up to 8 cards
                // If a new round was started, hand is already full and this is a no-op.
                refillHandToCapacity()
            }) {
                Text("Submit", fontFamily = Pixel,color = BlackBoardYellow)
            }
            Button(
                onClick = {
                    if (canDiscardEquation()) {
                        // Only equation-display cards go to card bin (using collection)
                        equationCards.forEach { card ->
                            cardBin.addCard(card, 1)
                        }
                        equationCards.clear()
                        // Refill hand from deck until 8 cards
                        refillHandToCapacity()
                        currentTurn++
                    }
                },
                enabled = canDiscardEquation()
            ) {
                Text("Discard", fontFamily = Pixel,color = BlackBoardYellow)
            }
        }
    }
    
    // Congratulations screen
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