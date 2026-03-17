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
import com.example.baraclan.mentalchallengemath_namepending.models.MusicManager
import com.example.baraclan.mentalchallengemath_namepending.models.SoundManager
import kotlinx.coroutines.launch

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
    const val OnlineLobby = "online_lobby"
    const val OnlineGame = "online_game"
    const val EditDeckSelect = "edit_deck_select"
    const val Tutorial = "tutorial"
    const val Profile = "profile"
    const val Settings = "settings"
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

    override fun onResume() {
        super.onResume()
        MusicManager.resume()
    }

    override fun onPause() {
        super.onPause()
        MusicManager.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicManager.stop()
        SoundManager.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MusicManager.start(this)
        SoundManager.init(this)
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
        // Numbers 0-9
        "0" to 2, "1" to 2, "2" to 2, "3" to 2, "4" to 2,
        "5" to 2, "6" to 2, "7" to 2, "8" to 2, "9" to 2,
        // Basic operators
        "+" to 2, "-" to 2, "*" to 2, "/" to 2,
        // Functions
        "sin" to 1, "cos" to 1, "ln" to 1, "log10" to 1,
        // Constants
        "pi" to 1, "euler" to 1,
        // Special two-slot cards
        "fraction" to 1, "exponent" to 1,
        // Variables
        "var_a" to 1, "var_b" to 1, "var_c" to 1, "var_d" to 1,
        "var_x" to 1, "var_y" to 1, "var_z" to 1,
        // Parentheses
        "left_paren" to 1, "right_paren" to 1
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
        // Numbers 0-9
        "0" to 1, "1" to 1, "2" to 1, "3" to 1, "4" to 1,
        "5" to 1, "6" to 1, "7" to 1, "8" to 1, "9" to 1,
        // Basic operators
        "+" to 1, "-" to 1, "*" to 1, "/" to 1,
        // Functions
        "sin" to 1, "cos" to 1, "ln" to 1, "log10" to 1,
        // Constants
        "pi" to 1, "euler" to 1,
        // Special two-slot cards
        "fraction" to 1, "exponent" to 1,
        // Variables
        "var_a" to 1, "var_b" to 1, "var_c" to 1, "var_d" to 1,
        "var_x" to 1, "var_y" to 1, "var_z" to 1,
        // Parentheses
        "left_paren" to 1, "right_paren" to 1
    )
    allPossibleCardConfigs.forEach { (cardString, count) ->
        availableCards.addCard(createCardFromConfig(cardString), count)
    }
    println("Collection Initialized: ${availableCards.getTotalCount()} total card types.")
    return availableCards
}

private fun createCardFromConfig(cardString: String): cardGame {
    return when (cardString) {
        // Basic operators
        "+", "-", "*", "/" -> cardGame(
            id = UUID.randomUUID().toString(),
            name = "Operator ($cardString)",
            type = cardType.OPERATOR,
            operator = when (cardString) {
                "+" -> Operator.ADD
                "-" -> Operator.SUBTRACT
                "*" -> Operator.MULTIPLY
                "/" -> Operator.DIVIDE
                else -> throw IllegalArgumentException("Unknown operator: $cardString")
            }
        )
        // Functions
        "sin"   -> cardGame(id = UUID.randomUUID().toString(), name = "sin",   type = cardType.FUNCTION,  operator = Operator.SIN)
        "cos"   -> cardGame(id = UUID.randomUUID().toString(), name = "cos",   type = cardType.FUNCTION,  operator = Operator.COS)
        "ln"    -> cardGame(id = UUID.randomUUID().toString(), name = "ln",    type = cardType.FUNCTION,  operator = Operator.LN)
        "log10" -> cardGame(id = UUID.randomUUID().toString(), name = "log10", type = cardType.FUNCTION,  operator = Operator.LOG10)
        // Constants
        "pi"    -> cardGame(id = UUID.randomUUID().toString(), name = "pi",    type = cardType.CONSTANT,  operator = Operator.PI)
        "euler" -> cardGame(id = UUID.randomUUID().toString(), name = "euler", type = cardType.CONSTANT,  operator = Operator.EULER)
        // Special two-slot cards
        "fraction"   -> cardGame(id = UUID.randomUUID().toString(), name = "Fraction",    type = cardType.FRACTION,    operator = Operator.FRACTION)
        "exponent"   -> cardGame(id = UUID.randomUUID().toString(), name = "Exponent",    type = cardType.EXPONENT,    operator = Operator.POWER)
        // Variables
        "var_a"      -> cardGame(id = UUID.randomUUID().toString(), name = "a",           type = cardType.VARIABLE,    operator = Operator.VAR_A)
        "var_b"      -> cardGame(id = UUID.randomUUID().toString(), name = "b",           type = cardType.VARIABLE,    operator = Operator.VAR_B)
        "var_c"      -> cardGame(id = UUID.randomUUID().toString(), name = "c",           type = cardType.VARIABLE,    operator = Operator.VAR_C)
        "var_d"      -> cardGame(id = UUID.randomUUID().toString(), name = "d",           type = cardType.VARIABLE,    operator = Operator.VAR_D)
        "var_x"      -> cardGame(id = UUID.randomUUID().toString(), name = "x",           type = cardType.VARIABLE,    operator = Operator.VAR_X)
        "var_y"      -> cardGame(id = UUID.randomUUID().toString(), name = "y",           type = cardType.VARIABLE,    operator = Operator.VAR_Y)
        "var_z"      -> cardGame(id = UUID.randomUUID().toString(), name = "z",           type = cardType.VARIABLE,    operator = Operator.VAR_Z)
        // Parentheses
        "left_paren"  -> cardGame(id = UUID.randomUUID().toString(), name = "(",          type = cardType.PARENTHESIS, operator = Operator.LEFT_PAREN)
        "right_paren" -> cardGame(id = UUID.randomUUID().toString(), name = ")",          type = cardType.PARENTHESIS, operator = Operator.RIGHT_PAREN)
        // Numbers (catch-all)
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
    var isOfflineMode by remember { mutableStateOf(false) }

    // Load saved decks from local storage — available throughout the nav graph
    val savedDecks by DeckRepository.getDecksFlow(context).collectAsState(initial = emptyList())

    // Seed UserStatsManager with locally-stored username on app start
    val savedUsername by DeckRepository.getUsernameFlow(context).collectAsState(initial = "")
    LaunchedEffect(savedUsername) {
        if (savedUsername.isNotBlank() &&
            savedUsername != com.example.baraclan.mentalchallengemath_namepending.models.UserStatsManager.getStats()?.username) {
            com.example.baraclan.mentalchallengemath_namepending.models.UserStatsManager.updateUsername(savedUsername)
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Login
    ) {

        composable(NavRoutes.Login) {
            LoginScreen(
                onLoginSuccess = {
                    isOfflineMode = false
                    navController.navigate(NavRoutes.Menu) {
                        popUpTo(NavRoutes.Login) { inclusive = true }
                    }
                },
                onPlayOffline = {
                    isOfflineMode = true
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
            val menuScope = rememberCoroutineScope()
            menu(
                onLogout = {
                    menuScope.launch { SessionManager.clearSession(context) }
                    navController.navigate(NavRoutes.Login) {
                        popUpTo(NavRoutes.Menu) { inclusive = true }
                    }
                },
                onAboutClick = { navController.navigate(NavRoutes.AboutScreen) },
                onEditDeckClick = { navController.navigate(NavRoutes.EditDeckSelect) },
                onStartGameClick = { navController.navigate(NavRoutes.GameSingle) },
                onMultiplayerGameClick = { navController.navigate(NavRoutes.MultiplayerView) },
                onTutorialClick = { navController.navigate(NavRoutes.Tutorial) },
                onProfileClick = { navController.navigate(NavRoutes.Profile) },
                onSettingsClick = { navController.navigate(NavRoutes.Settings) }
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
                availableDecks = savedDecks,
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

        composable(NavRoutes.Settings) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.MultiplayerView) {
            MultiplayerSelectScreen(
                onNavigateToMenu = { navController.navigate(NavRoutes.Menu) },
                onNavigateToOnline = { navController.navigate(NavRoutes.OnlineMultiplayer) },
                onNavigateToLocal = { navController.navigate(NavRoutes.LocalMultiplayer) },
                isOfflineMode = isOfflineMode
            )
        }

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

        composable(NavRoutes.OnlineMultiplayer) {
            OnlineMultiplayerEntryScreen(
                onNavigateToLobby = { lobbyId ->
                    navController.navigate("${NavRoutes.OnlineLobby}/$lobbyId")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("${NavRoutes.OnlineLobby}/{lobbyId}") { backStackEntry ->
            val lobbyId = backStackEntry.arguments?.getString("lobbyId") ?: return@composable
            OnlineLobbyScreen(
                lobbyIdArg = lobbyId,
                onGameStart = { id ->
                    navController.navigate("${NavRoutes.OnlineGame}/$id") {
                        popUpTo("${NavRoutes.OnlineLobby}/$id") { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.navigate(NavRoutes.OnlineMultiplayer) {
                        popUpTo("${NavRoutes.OnlineLobby}/$lobbyId") { inclusive = true }
                    }
                }
            )
        }

        composable("${NavRoutes.OnlineGame}/{lobbyId}") { backStackEntry ->
            val lobbyId = backStackEntry.arguments?.getString("lobbyId") ?: return@composable
            GameViewMultiOnline(
                lobbyId = lobbyId,
                playerDeck = currentDeck,
                onReturnToMenu = {
                    navController.navigate(NavRoutes.Menu) {
                        popUpTo(NavRoutes.Menu) { inclusive = false }
                    }
                }
            )
        }
    }
}