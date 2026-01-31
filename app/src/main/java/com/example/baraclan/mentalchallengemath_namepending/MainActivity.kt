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
import com.example.baraclan.mentalchallengemath_namepending.views.*


// Note: Removed unused imports like kotlin.random.Random and kotlin.math.abs,
//       and commented-out game-specific imports, as they are not currently used
//       in this file with the game content removed.
//       Also, androidx.compose.foundation.layout.* and others might be implicitly
//       imported by LoginScreen/SignInScreen's definitions.

// Define your routes
object NavRoutes {
    const val Login = "login"
    const val Menu = "menu"
    const val SignIn = "signin"
    const val ForgotPassword = "forgot_password"
    const val AboutScreen = "about_screen"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            gameTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
public fun AppNavigation(){
    val navController = rememberNavController()

    NavHost(
        navController=navController,
        startDestination = NavRoutes.Login
    ){
        composable(NavRoutes.Login) {
            LoginScreen(
                onLoginSuccess = {
                    // Navigate to the Menu screen after successful login
                    navController.navigate(NavRoutes.Menu) {
                        // Optionally, pop up to the Login screen to prevent going back to it
                        popUpTo(NavRoutes.Login) { inclusive = true }
                    }
                },
                onNavigateToSignUp = { // <--- Added this parameter!
                    navController.navigate(NavRoutes.SignIn) // Navigate to your SignIn screen
                },
                onForgotPassword = {
                    navController.navigate(NavRoutes.ForgotPassword)
                }
            )
        }
        composable(NavRoutes.Menu) {
            menu(
                onLogout = {
                    // Navigate back to the Login screen on logout
                    navController.navigate(NavRoutes.Login) {
                        // Clear the back stack up to the Login screen, ensuring no Menu screens remain
                        popUpTo(NavRoutes.Menu) { inclusive = true }
                    }
                } ,
                onAboutClick = {
                    navController.navigate(NavRoutes.AboutScreen)
                }
            )
        }
        composable(NavRoutes.SignIn){
            SignInScreen(
                onNavigateToLogin = { // <--- Changed from 'onNavigateBackToLogin' to 'onNavigateToLogin'
                    navController.popBackStack() // Go back to the previous screen (Login)
                },
                onSignInSuccess = {
                    navController.navigate(NavRoutes.Menu) {
                        popUpTo(NavRoutes.Login) { inclusive = true } // Clear login/signup from stack
                    }
                }
            )
        }
        composable(NavRoutes.ForgotPassword) { // <--- New composable for ForgotPassword
            ForgotPasswordScreen(
                onNavigateToLogin = {
                    navController.popBackStack() // Go back to Login screen
                    // Or navController.navigate(NavRoutes.Login) { popUpTo(NavRoutes.Login) { inclusive = true } } if you want a cleaner stack
                }
            )

        }
        composable(NavRoutes.AboutScreen){
            AboutScreen(
                onNavigateToMenu = {
                    navController.popBackStack()
                }
            )
        }
    }
}


