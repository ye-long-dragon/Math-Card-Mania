package com.example.baraclan.mentalchallengemath_namepending

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.gameTheme
import com.example.baraclan.mentalchallengemath_namepending.views.*
import com.example.baraclan.mentalchallengemath_namepending.models.*
import java.util.UUID // Import for generating unique IDs

// Define your routes
object NavRoutes {
    const val Login = "login"
    const val Menu = "menu"
    const val SignIn = "signin"
    const val ForgotPassword = "forgot_password"
    const val AboutScreen = "about_screen"
    const val EditDeck = "edit_Deck"
    const val GameSingle = "game_single" // ADDED: New route for the GameView
}

class MainActivity : ComponentActivity() {
    public var playerDeckState by mutableStateOf(initDeck())

    val playerDeck: deck
        get() = playerDeckState

    fun updatePlayerDeck(newDeck: deck) {
        playerDeckState = newDeck
        println("MainActivity: Player deck updated. Total cards: ${newDeck.getTotalCount()}")
        newDeck.getAllCardsWithCounts().forEach { (card, count) ->
            println(" - ${card.name}: $count")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            gameTheme {
                Box(modifier = Modifier.fillMaxSize()) { // <-- The Box
                    Image( // <-- The Image
                        painter = painterResource(id = R.drawable.background), // Make sure this is correct
                        contentDescription = "App Background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.6f // Or adjust as desired
                    )

                    Surface( // <-- The Root Surface, MUST be transparent
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent // <-- THIS IS CRUCIAL
                    ) {
                        AppNavigation(
                            currentDeck = playerDeck,
                            onUpdateDeck = ::updatePlayerDeck
                        )
                    }
                }
            }
        }
    }
}
// Utility function to initialize the player's starting deck
public fun initDeck(): deck {
    val playerDeck = deck("Player Deck") // Initialize an empty deck
    val initialCardsConfig = listOf(
        "0" to 2, "1" to 2, "2" to 2, "3" to 2, "4" to 2,
        "5" to 2, "6" to 2, "7" to 2, "8" to 2, "9" to 2,
        "+" to 2, "-" to 2, "*" to 2, "/" to 2
    )

    initialCardsConfig.forEach { (cardString, count) ->
        val baseCard = createCardFromConfig(cardString)
        playerDeck.addCard(baseCard, count)
    }

    println("Deck Initialized: ${playerDeck.getTotalCount()} total cards.")
    return playerDeck
}

// Utility function to create a collection of all available cards
public fun fullCollection(): collection {
    val availableCards = collection("Full Collection")
    val allPossibleCardConfigs = listOf(
        "0" to 1, "1" to 1, "2" to 1, "3" to 1, "4" to 1,
        "5" to 1, "6" to 1, "7" to 1, "8" to 1, "9" to 1,
        "+" to 1, "-" to 1, "*" to 1, "/" to 1
        // Add more unique cards if needed for the collection
    )

    allPossibleCardConfigs.forEach { (cardString, count) ->
        val baseCard = createCardFromConfig(cardString)
        availableCards.addCard(baseCard, count)
    }
    println("Collection Initialized: ${availableCards.getTotalCount()} total card types.")
    return availableCards
}

// Helper to create cardGame instances consistently for initDeck and fullCollection
private fun createCardFromConfig(cardString: String): cardGame {
    return when (cardString) {
        "+", "-", "*", "/" -> {
            val operatorType = when (cardString) {
                "+" -> Operator.ADD
                "-" -> Operator.SUBTRACT
                "*" -> Operator.MULTIPLY
                "/" -> Operator.DIVIDE
                else -> throw IllegalArgumentException("Unknown operator: $cardString")
            }
            cardGame(
                id = UUID.randomUUID().toString(), // Use UUID for unique instance ID
                name = "Operator ($cardString)",
                type = cardType.OPERATOR,
                operator = operatorType
            )
        }
        else -> {
            val numberValue = cardString.toIntOrNull()
            if (numberValue != null) {
                cardGame(
                    id = UUID.randomUUID().toString(), // Use UUID for unique instance ID
                    name = "Number ($cardString)",
                    type = cardType.NUMBER,
                    numberValue = numberValue
                )
            } else {
                throw IllegalArgumentException("Could not parse '$cardString' as a number or known operator.")
            }
        }
    }
}


@Composable
public fun AppNavigation(
    currentDeck: deck,
    onUpdateDeck: (deck) -> Unit
) {
    val navController = rememberNavController()

    // Create the full collection once
    val availableCollection = fullCollection()

    NavHost(
        navController=navController,
        startDestination = NavRoutes.Login
    ){
        //Routes
        composable(NavRoutes.Login) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(NavRoutes.Menu) {
                        popUpTo(NavRoutes.Login) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(NavRoutes.SignIn)
                },
                onForgotPassword = {
                    navController.navigate(NavRoutes.ForgotPassword)
                }
            )
        }
        composable(NavRoutes.Menu) {
            menu(
                onLogout = {
                    navController.navigate(NavRoutes.Login) {
                        popUpTo(NavRoutes.Menu) { inclusive = true }
                    }
                },
                onAboutClick = {
                    navController.navigate(NavRoutes.AboutScreen)
                },
                onEditDeckClick = {
                    navController.navigate(NavRoutes.EditDeck)
                },
                onStartGameClick = { // ADDED: New callback for starting the game
                    navController.navigate(NavRoutes.GameSingle)
                }
            )
        }
        composable(NavRoutes.SignIn){
            SignInScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onSignInSuccess = {
                    navController.navigate(NavRoutes.Menu) {
                        popUpTo(NavRoutes.Login) { inclusive = true }
                    }
                }
            )
        }
        composable(NavRoutes.ForgotPassword) {
            ForgotPasswordScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        composable(NavRoutes.AboutScreen){
            AboutScreen(
                onNavigateToMenu = {
                    navController.navigate(NavRoutes.Menu)
                }
            )
        }
        composable(NavRoutes.EditDeck){
            EditDeckScreen(
                cardDeck = currentDeck, // Pass the current Deck from MainActivity
                collection = availableCollection, // Pass the full collection of available cards
                onReturnMenu = { // Callback to navigate back to the menu
                    navController.popBackStack()
                },
                onClickDeck = { cardToRemove ->
                    // Create a new deck instance based on the current state
                    val newDeck = deck(currentDeck.name, currentDeck.getAllCardsWithCounts())
                    newDeck.removeCard(cardToRemove, 1) // Remove one instance of the card type
                    onUpdateDeck(newDeck) // Notify MainActivity to update its state
                },
                onClickCollection = { cardToAdd ->
                    // Create a new deck instance based on the current state
                    val newDeck = deck(currentDeck.name, currentDeck.getAllCardsWithCounts())
                    newDeck.addCard(cardToAdd, 1) // Add one instance of the card type
                    onUpdateDeck(newDeck) // Notify MainActivity to update its state
                }
            )
        }
        // ADDED: Composable for the GameView
        composable(NavRoutes.GameSingle) {
            GameView(currentDeck) // Your GameView Composable is displayed here
        }
    }
}
