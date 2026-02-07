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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.baraclan.mentalchallengemath_namepending.R
import com.example.baraclan.mentalchallengemath_namepending.models.cardGame
import com.example.baraclan.mentalchallengemath_namepending.models.deck
import com.example.baraclan.mentalchallengemath_namepending.models.hand
import com.example.baraclan.mentalchallengemath_namepending.scripts.RandomHand
import com.example.baraclan.mentalchallengemath_namepending.scripts.evaluateEquation
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow
import com.example.baraclan.mentalchallengemath_namepending.views.*
import com.example.baraclan.mentalchallengemath_namepending.views.Pixel

// Define the Pixel font family if not already defined globally or in an object


// Your predefined game goals
val gameGoalsPreset: List<Double> = listOf(
    4.0, -0.125, -36.0, 0.00462962962963, 576.0,
    -0.000072337962963, -14400.0, 5.787037037E-7,
    518400.0, -2.6791838134E-9
)

@Composable
fun LocalMultiplayer(
    onReturntoMultiplayerMenu:()->Unit // The new callback from the user
){
    val initialGlobalDeck = remember { deck("Shared Initial Deck") } // A common template deck

    Column(modifier = Modifier.fillMaxSize()) {
        // Player 1's screen (vertically flipped)
        Surface(
            modifier = Modifier
                .weight(1f) // Takes up half the screen vertically
                .fillMaxWidth()
                .rotate(180f) ,
            color = Color.Blue// Apply 180-degree rotation for vertical flip
        ) {
            HalfScreen(
                initialDeck = deck(initialGlobalDeck.name, initialGlobalDeck.getAllCardsWithCounts()),
                gameGoals = gameGoalsPreset,
                onGameCompleted = onReturntoMultiplayerMenu // Pass the callback here
            )
        }

        // You might want a divider or some UI element between the two player screens
        // Divider(modifier = Modifier.fillMaxWidth().height(4.dp).background(Color.DarkGray))

        // Player 2's screen (normal orientation)
        Surface(
            modifier = Modifier
                .weight(1f) // Takes up the other half of the screen vertically
                .fillMaxWidth(),
            color = Color.Red
        ) {
            HalfScreen(
                initialDeck = deck(initialGlobalDeck.name, initialGlobalDeck.getAllCardsWithCounts()),
                gameGoals = gameGoalsPreset,
                onGameCompleted = onReturntoMultiplayerMenu // Pass the callback here as well
            )
        }
    }
}

@Composable
fun HalfScreen(
    initialDeck: deck,
    gameGoals: List<Double> = gameGoalsPreset, // Use the provided list, with a default
    modifier: Modifier = Modifier, // Add modifier for better reusability in LocalMultiplayer
    onGameCompleted: () -> Unit // NEW: Callback for when the game for this player is completed
){
    // Game state variables
    var currentScore by remember { mutableStateOf(0) }
    var currentTurn by remember { mutableStateOf(1) } // Turn within a single goal/round

    // State for managing goals from the list
    var currentGoalIndex by remember { mutableStateOf(0) }
    var gameFinished by remember { mutableStateOf(false) }

    // Active deck for the current game session (a mutable copy)
    var gameDeckState by remember { mutableStateOf(deck("Game Deck Copy")) }

    // Player's current hand of cards
    var playerHandState by remember { mutableStateOf(hand("Player's Current Hand")) }

    // Cards currently forming the equation
    val equationCards = remember { mutableStateListOf<cardGame>() }

    // Function to set up a new round
    val startNewRound: () -> Unit = {
        if (currentGoalIndex < gameGoals.size) {
            val nextGoal = gameGoals[currentGoalIndex] // Get the specific goal for evaluation
            println("New round: Goal is $nextGoal (index $currentGoalIndex)")

            // Copy the initial deck for a fresh start to this round
            gameDeckState = deck(initialDeck.name, initialDeck.getAllCardsWithCounts())
            println("Game deck reset to ${gameDeckState.getTotalCount()} cards.")

            // Draw a new hand from the game deck
            playerHandState = RandomHand(gameDeckState)
            println("New hand drawn. Hand size: ${playerHandState.getTotalCount()}. Deck remaining: ${gameDeckState.getTotalCount()}")

            // Clear previous equation and reset turn count for this goal
            equationCards.clear()
            currentTurn = 1 // Reset turn count for each new goal/round
        } else {
            // All goals completed
            gameFinished = true
            println("All goals completed! Game Over. Final Score: $currentScore")
        }
    }

    // Start the first round when HalfScreen is launched
    LaunchedEffect(Unit) {
        startNewRound()
    }

    // Move card from hand to equation
    val onHandCardClick: (cardGame) -> Unit = { clickedCard ->
        // TODO: Add equation building validation (e.g., cannot add two numbers consecutively)
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

    if (gameFinished) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Game Over!",
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = Pixel,
                fontSize = 48.sp,
                color = BlackBoardYellow
            )
            Text(
                text = "Final Score: $currentScore",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = Pixel,
                modifier = Modifier.padding(top = 16.dp),
                color = BlackBoardYellow
            )
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
            ) {
                Button(onClick = {
                    // Reset game for replay
                    currentScore = 0
                    currentGoalIndex = 0
                    gameFinished = false
                    startNewRound()
                }) {
                    Text("Play Again", fontFamily = Pixel,color = BlackBoardYellow)
                }
                Button(onClick = {
                    onGameCompleted() // Call the passed-in callback to return to multiplayer menu
                }) {
                    Text("Return to Menu", fontFamily = Pixel,color = BlackBoardYellow)
                }
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // These Composables now use the current state variables
            statusBar(currentScore, currentTurn)

            Spacer(modifier = Modifier.height(16.dp))

            // Pass a sublist of goals starting from the current index
            // This ensures your `goal` Composable's `gameGoals.first()` correctly shows the current goal
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
                fontFamily = Pixel,
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
                    // Ensure currentGoal is correctly accessed for comparison
                    val currentTargetGoal = gameGoals[currentGoalIndex]
                    println("Submitted: ${equationCards.joinToString(" ") { it.toString() }} = $equationResult")

                    // Compare with the current active goal
                    if (equationResult == currentTargetGoal) {
                        println("Goal reached! Goal: $currentTargetGoal")
                        currentScore += 100 // Example score
                        currentGoalIndex++ // Move to the next goal
                        startNewRound() // Start new round with next goal
                        println("Round completed, starting new one.")
                    } else {
                        println("Goal NOT reached. Target: $currentTargetGoal, Result: $equationResult")
                        equationCards.clear() // Clear equation for next attempt
                        // Only increment turn if the goal was not met and a new attempt is needed
                        currentTurn++
                    }
                }) {
                    Text("Submit Equation", fontFamily = Pixel,color = BlackBoardYellow)
                }
                Button(onClick = {
                    // Return equation cards to hand
                    equationCards.forEach { card ->
                        playerHandState.addCard(card, 1)
                    }
                    equationCards.clear()
                }) {
                    Text("Clear Equation", fontFamily = Pixel,color = BlackBoardYellow)
                }
            }
        }
    }
}