package com.example.baraclan.mentalchallengemath_namepending

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize // Added this import
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue // Added for 'by' delegate
import androidx.compose.runtime.mutableStateOf // Added for observable state
import androidx.compose.runtime.setValue // Added for 'by' delegate
import androidx.compose.ui.Modifier // Added this import
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.gameTheme
import com.example.baraclan.mentalchallengemath_namepending.views.*
import com.example.baraclan.mentalchallengemath_namepending.models.*

// Define your routes (unchanged)
object NavRoutes {
    const val Login = "login"
    const val Menu = "menu"
    const val SignIn = "signin"
    const val ForgotPassword = "forgot_password"
    const val AboutScreen = "about_screen"
    const val EditDeck = "edit_Deck"
}

class MainActivity : ComponentActivity() {
    // CRUCIAL CHANGE: Make basicdeck observable using mutableStateOf
    private var _playerDeckState by mutableStateOf(initDeck())

    // Expose a read-only version if other parts only need to read
    val playerDeck: deck
        get() = _playerDeckState

    // A function for composables to call to update the deck
    fun updatePlayerDeck(newDeck: deck) {
        _playerDeckState = newDeck
        // In a real app, you would also save this newDeck to persistent storage (e.g., DataStore, database)
        println("MainActivity: Player deck updated and saved (simulated). Total cards: ${newDeck.getTotalCount()}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            gameTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), // Set fillMaxSize here
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass the current deck state and the update function down to AppNavigation
                    AppNavigation(
                        currentDeck = playerDeck, // Pass the current Deck object
                        onUpdateDeck = ::updatePlayerDeck // Pass a reference to the update function
                    )
                }
            }
        }
    }
}

// initDeck function (should ideally be in a separate utility file or object)
public fun initDeck(): deck { // <--- Changed return type to Deck
    val initCards: List<String> = listOf(
        "0","0",
        "1","1",
        "2","2",
        "3","3",
        "4","4",
        "5","5",
        "6","6",
        "7","7",
        "8","8",
        "9","9",
        "+","+",
        "-","-",
        "*","*",
        "/","/",
    )

    val playerDeck = deck("Initial Deck") // <--- Changed instantiation to Deck
    initCards.forEachIndexed { index, cardString ->
        when (cardString) {
            "+", "-", "*", "/" -> {
                // It's an operator
                val operatorType = when (cardString) {
                    "+" -> Operator.ADD
                    "-" -> Operator.SUBTRACT
                    "*" -> Operator.MULTIPLY
                    "/" -> Operator.DIVIDE
                    else -> throw IllegalArgumentException("Unknown operator: $cardString")
                }
                val operatorCard = cardGame(
                    id = "OP_${cardString}_$index", // Unique ID for each instance
                    name = "Operator ($cardString)",
                    type = cardType.OPERATOR, // Corrected to CardType.OPERATOR
                    operator = operatorType
                )
                playerDeck.addCard(operatorCard) // <--- Add to playerDeck
            }
            else -> {
                // It's a number (try to parse it)
                val numberValue = cardString.toIntOrNull()
                if (numberValue != null) {
                    val numberCard = cardGame(
                        id = "NUM_${cardString}_$index", // Unique ID for each instance
                        name = "Number ($cardString)",
                        type = cardType.NUMBER, // Corrected to CardType.NUMBER
                        numberValue = numberValue
                    )
                    playerDeck.addCard(numberCard) // <--- Add to playerDeck
                } else {
                    println("Warning: Could not parse '$cardString' as a number or known operator.")
                }
            }
        }
    }

    println("Deck Initialized: ${playerDeck.getTotalCount()} total cards.") // <--- Updated message
    playerDeck.getAllCardsWithCounts().forEach { (card, count) ->
        println(" - ${card.name} (ID: ${card.id}): $count")
    }

    return playerDeck
}


@Composable
public fun AppNavigation(
    currentDeck: deck, // New: Receive the current deck from MainActivity
    onUpdateDeck: (deck) -> Unit // New: Receive the updater function from MainActivity
) {
    val navController = rememberNavController()

    NavHost(
        navController=navController,
        startDestination = NavRoutes.Login
    ){
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
                onForgotPassword = { // New parameter, passing the navigation callback
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
                onEditDeckClick = { // New parameter, passing the navigation callback
                    navController.navigate(NavRoutes.EditDeck)
                }
                // other menu callbacks if any
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
        /*composable(NavRoutes.EditDeck){
            EditDeckScreen(
                initialDeck = currentDeck, // Pass the current Deck from MainActivity
                onSaveDeck = onUpdateDeck, // Pass the update function from MainActivity
                onReturnMenu = { // This callback simply navigates back
                    navController.popBackStack()
                }
            )
        }*/
    }
}

