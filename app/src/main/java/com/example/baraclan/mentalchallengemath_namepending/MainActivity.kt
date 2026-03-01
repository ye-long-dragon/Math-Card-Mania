package com.example.baraclan.mentalchallengemath_namepending

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.baraclan.mentalchallengemath_namepending.data.DeckRepository
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.gameTheme
import com.example.baraclan.mentalchallengemath_namepending.views.*
import com.example.baraclan.mentalchallengemath_namepending.models.*
import java.util.UUID

// ─────────────────────────────────────────────────────────────
// Nav routes
// ─────────────────────────────────────────────────────────────
object NavRoutes {
    const val Login = "login"
    const val Menu = "menu"
    const val SignIn = "signin"
    const val ForgotPassword = "forgot_password"
    const val AboutScreen = "about_screen"
    const val EditDeck = "edit_Deck"
    const val GameSingle = "game_single"
    const val MultiplayerView = "multiplayer_view"
    const val LocalMultiplayer = "local_multiplayer"
    const val OnlineMultiplayer = "online_multiplayer"
    const val EditDeckSelect = "edit_deck_select"
    const val Tutorial = "tutorial"
    const val Profile = "profile"
}

// ─────────────────────────────────────────────────────────────
// MainActivity
// ─────────────────────────────────────────────────────────────
class MainActivity : ComponentActivity() {
    var playerDeckState by mutableStateOf(initDeck())

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
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.background),
                        contentDescription = "App Background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.6f
                    )
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent
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

// ─────────────────────────────────────────────────────────────
// Deck + Collection initializers
// ─────────────────────────────────────────────────────────────
fun initDeck(): deck {
    val playerDeck = deck("Player Deck")
    val initialCardsConfig = listOf(
        "0" to 2, "1" to 2, "2" to 2, "3" to 2, "4" to 2,
        "5" to 2, "6" to 2, "7" to 2, "8" to 2, "9" to 2,
        "+" to 2, "-" to 2, "*" to 2, "/" to 2
    )
    initialCardsConfig.forEach { (cardString, count) ->
        playerDeck.addCard(createCardFromConfig(cardString), count)
    }
    println("Deck Initialized: ${playerDeck.getTotalCount()} total cards.")
    return playerDeck
}

fun fullCollection(): collection {
    val availableCards = collection("Full Collection")
    val allPossibleCardConfigs = listOf(
        "0" to 1, "1" to 1, "2" to 1, "3" to 1, "4" to 1,
        "5" to 1, "6" to 1, "7" to 1, "8" to 1, "9" to 1,
        "+" to 1, "-" to 1, "*" to 1, "/" to 1
    )
    allPossibleCardConfigs.forEach { (cardString, count) ->
        availableCards.addCard(createCardFromConfig(cardString), count)
    }
    println("Collection Initialized: ${availableCards.getTotalCount()} total card types.")
    return availableCards
}

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
                id = UUID.randomUUID().toString(),
                name = "Operator ($cardString)",
                type = cardType.OPERATOR,
                operator = operatorType
            )
        }
        else -> {
            val numberValue = cardString.toIntOrNull()
                ?: throw IllegalArgumentException("Could not parse '$cardString'")
            cardGame(
                id = UUID.randomUUID().toString(),
                name = "Number ($cardString)",
                type = cardType.NUMBER,
                numberValue = numberValue
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Navigation
// ─────────────────────────────────────────────────────────────
@Composable
fun AppNavigation(
    currentDeck: deck,
    onUpdateDeck: (deck) -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val availableCollection = fullCollection()

    // Load saved decks from local storage — available throughout the nav graph
    val savedDecks by DeckRepository.getDecksFlow(context).collectAsState(initial = emptyList())

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Login
    ) {

        composable(NavRoutes.Login) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(NavRoutes.Menu) {
                        popUpTo(NavRoutes.Login) { inclusive = true }
                    }
                },
                onNavigateToSignUp = { navController.navigate(NavRoutes.SignIn) },
                onForgotPassword = { navController.navigate(NavRoutes.ForgotPassword) }
            )
        }

        composable(NavRoutes.SignIn) {
            SignUpScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onSignUpSuccess = {
                    navController.navigate(NavRoutes.Menu) {
                        popUpTo(NavRoutes.Login) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.ForgotPassword) {
            ForgotPasswordScreen(
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.Menu) {
            menu(
                onLogout = {
                    navController.navigate(NavRoutes.Login) {
                        popUpTo(NavRoutes.Menu) { inclusive = true }
                    }
                },
                onAboutClick = { navController.navigate(NavRoutes.AboutScreen) },
                onEditDeckClick = { navController.navigate(NavRoutes.EditDeckSelect) },
                onStartGameClick = { navController.navigate(NavRoutes.GameSingle) },
                onMultiplayerGameClick = { navController.navigate(NavRoutes.MultiplayerView) },
                onTutorialClick = { navController.navigate(NavRoutes.Tutorial) },
                onProfileClick = { navController.navigate(NavRoutes.Profile) }
            )
        }

        composable(NavRoutes.AboutScreen) {
            AboutScreen(
                onNavigateToMenu = { navController.navigate(NavRoutes.Menu) }
            )
        }

        // EditDeckSelect → DeckManagerScreen (list of all saved decks)
        composable(NavRoutes.EditDeckSelect) {
            DeckManagerScreen(
                defaultDeck = initDeck(),
                availableCollection = availableCollection,
                onReturnMenu = { navController.popBackStack() }
            )
        }



        composable(NavRoutes.GameSingle) {
            GameView(
                initialDeck = currentDeck,
                onGameComplete = {
                    navController.navigate(NavRoutes.Menu) {
                        popUpTo(NavRoutes.GameSingle) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.Tutorial) {
            TutorialView(
                initialDeck = currentDeck,
                onNavigateBack = {
                    navController.navigate(NavRoutes.Menu) {
                        popUpTo(NavRoutes.Tutorial) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.Profile) {
            ProfileView(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.MultiplayerView) {
            MultiplayerSelectScreen(
                onNavigateToMenu = { navController.navigate(NavRoutes.Menu) },
                onNavigateToOnline = { /* TODO */ },
                onNavigateToLocal = { navController.navigate(NavRoutes.LocalMultiplayer) }
            )
        }

        // LocalMultiplayer now receives savedDecks for deck selection
        composable(NavRoutes.LocalMultiplayer) {
            LocalMultiplayer(
                availableDecks = savedDecks,
                onReturnToMultiplayerMenu = {
                    navController.navigate(NavRoutes.MultiplayerView) {
                        popUpTo(NavRoutes.LocalMultiplayer) { inclusive = true }
                    }
                }
            )
        }
    }
}