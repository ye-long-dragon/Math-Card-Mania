package com.example.baraclan.mentalchallengemath_namepending

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme // Still needed for Surface colorScheme
import androidx.compose.material3.Surface     // Still needed for background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.gameTheme
import com.example.baraclan.mentalchallengemath_namepending.views.LoginScreen
import com.example.baraclan.mentalchallengemath_namepending.views.SignInScreen

// Note: Removed unused imports like kotlin.random.Random and kotlin.math.abs,
//       and commented-out game-specific imports, as they are not currently used
//       in this file with the game content removed.
//       Also, androidx.compose.foundation.layout.* and others might be implicitly
//       imported by LoginScreen/SignInScreen's definitions.

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            gameTheme {
                appScreen()
            }
        }
    }
}

@Composable
fun appScreen() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.LOGIN_ROUTE
    ) {
        composable(AppDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onNavigateToSignUp = { navController.navigate(AppDestinations.SIGN_IN_ROUTE) },
                onLoginSuccess = { println("Login Successful! Game screen not yet integrated.") }
            )
        }

        composable(AppDestinations.SIGN_IN_ROUTE) {
            SignInScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onSignInSuccess = { println("Sign In Successful! Game screen not yet integrated.") }
            )
        }
    }
}

object AppDestinations {
    const val LOGIN_ROUTE = "login"
    const val SIGN_IN_ROUTE = "signin"
    const val GAME_ROUTE = "game" // Still defined for future use, but no composable yet
}

@Preview(showBackground = true)
@Composable
fun AppScreenPreview() {
    gameTheme {
        appScreen()
    }
}
