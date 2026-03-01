package com.example.baraclan.mentalchallengemath_namepending.views

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


// ─────────────────────────────────────────────────────────────
// Shared helpers
// ─────────────────────────────────────────────────────────────



// Reusable no-ripple click modifier
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = this.clickable(
    interactionSource = MutableInteractionSource(),
    indication = null,
    onClick = onClick
)

// Email validation using Android's built-in pattern
fun String.isValidEmail(): Boolean =
    Patterns.EMAIL_ADDRESS.matcher(this).matches()

// Password strength check (min 8 chars, at least 1 digit)
fun String.isStrongPassword(): Boolean =
    this.length >= 8 && this.any { it.isDigit() }


// ─────────────────────────────────────────────────────────────
// Firebase Manager — now using Firebase Authentication
// ─────────────────────────────────────────────────────────────
object FirebaseManager {
    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

    // 1. LOGIN
    suspend fun loginUser(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid
                ?: return Result.failure(Exception("Login failed. Please try again."))
            Log.d("FirebaseManager", "Login success: uid=$uid")
            Result.success(uid)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Login error: ${e.message}")
            Result.failure(Exception("Invalid email or password."))
        }
    }

    // 2. SIGN UP — creates Auth user + saves username to Firestore
    suspend fun signUpUser(username: String, email: String, password: String): Result<Unit> {
        return try {
            // Step 1: Create the Firebase Auth account
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid
                ?: return Result.failure(Exception("Sign up failed. Please try again."))

            Log.d("FirebaseManager", "Auth user created: uid=$uid")

            // Step 2: Save username + email to Firestore under the user's uid
            val userDoc = hashMapOf(
                "username" to username,
                "email" to email,
                "uid" to uid
            )
            db.collection("users").document(uid).set(userDoc).await()

            Log.d("FirebaseManager", "Firestore user document created for uid=$uid")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "SignUp error: ${e.message}")
            Result.failure(Exception(e.message ?: "Sign up failed. Please try again."))
        }
    }

    // 3. FORGOT PASSWORD — sends a real reset email (no plaintext password needed)
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Log.d("FirebaseManager", "Password reset email sent to $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Reset error: ${e.message}")
            Result.failure(Exception("Could not send reset email. Check the address and try again."))
        }
    }
}


// ─────────────────────────────────────────────────────────────
// 1. Login Screen
// ─────────────────────────────────────────────────────────────
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Math Card Mania",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp),
                fontSize = 42.sp,
                fontFamily = Pixel,
                textAlign = TextAlign.Center,
                color = BlackBoardYellow
            )

            // Email field (login now uses email, not username)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", fontFamily = Pixel) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", fontFamily = Pixel) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            // "Forgot password?" link
            Text(
                text = "Forgot password?",
                color = BlackBoardYellow,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .clickableNoRipple { onForgotPassword() },
                fontFamily = Pixel
            )

            // Error message
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontFamily = Pixel
                )
            }

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill all fields"
                        return@Button
                    }
                    if (!email.isValidEmail()) {
                        errorMessage = "Please enter a valid email address"
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    scope.launch {
                        val result = FirebaseManager.loginUser(email, password)
                        isLoading = false
                        result
                            .onSuccess { onLoginSuccess() }
                            .onFailure { e -> errorMessage = e.message }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = BlackBoardYellow,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Login", fontFamily = Pixel, color = BlackBoardYellow)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Don't have an account?",
                fontFamily = Pixel,
                color = BlackBoardYellow
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sign Up",
                color = BlackBoardYellow,
                modifier = Modifier.clickableNoRipple { onNavigateToSignUp() },
                fontFamily = Pixel
            )
        }
    }
}


@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var reEnteredPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Create Your Account",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp),
                color = BlackBoardYellow,
                fontFamily = Pixel
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", fontFamily = Pixel) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", fontFamily = Pixel) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", fontFamily = Pixel) },
                supportingText = { Text("Min. 8 characters, at least 1 number", fontFamily = Pixel) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            OutlinedTextField(
                value = reEnteredPassword,
                onValueChange = { reEnteredPassword = it },
                label = { Text("Re-enter Password", fontFamily = Pixel) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontFamily = Pixel
                )
            }

            Button(
                onClick = {
                    if (username.isBlank() || email.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill all fields"
                        return@Button
                    }
                    if (!email.isValidEmail()) {
                        errorMessage = "Please enter a valid email address"
                        return@Button
                    }
                    if (!password.isStrongPassword()) {
                        errorMessage = "Password must be at least 8 characters and include a number"
                        return@Button
                    }
                    if (password != reEnteredPassword) {
                        errorMessage = "Passwords do not match"
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    scope.launch {
                        val result = FirebaseManager.signUpUser(username, email, password)
                        isLoading = false
                        result
                            .onSuccess { onSignUpSuccess() }
                            .onFailure { e -> errorMessage = e.message }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = BlackBoardYellow,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Create Account", fontFamily = Pixel, color = BlackBoardYellow)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Already have an account?",
                fontFamily = Pixel,
                color = BlackBoardYellow
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Log In",
                color = BlackBoardYellow,
                modifier = Modifier.clickableNoRipple { onNavigateToLogin() },
                fontFamily = Pixel
            )
        }
    }
}



// ─────────────────────────────────────────────────────────────
// 3. Forgot Password Screen
// ─────────────────────────────────────────────────────────────
@Composable
fun ForgotPasswordScreen(
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Forgot Password?",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp),
                fontFamily = Pixel,
                color = BlackBoardYellow
            )

            Text(
                text = "Enter your email and we'll send you a reset link.",
                fontFamily = Pixel,
                color = BlackBoardYellow.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Only needs email now — no new password entered here (Firebase handles that)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter your Email", fontFamily = Pixel) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontFamily = Pixel
                )
            }

            successMessage?.let {
                Text(
                    text = it,
                    color = Color.Green,
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontFamily = Pixel
                )
            }

            Button(
                onClick = {
                    if (email.isBlank()) {
                        errorMessage = "Please enter your email"
                        return@Button
                    }
                    if (!email.isValidEmail()) {
                        errorMessage = "Please enter a valid email address"
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    scope.launch {
                        val result = FirebaseManager.sendPasswordResetEmail(email)
                        isLoading = false
                        result.onSuccess {
                            successMessage = "Reset link sent! Check your inbox."
                            delay(2500)
                            onNavigateToLogin()
                        }.onFailure { e ->
                            errorMessage = e.message
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = BlackBoardYellow,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Send Reset Link", fontFamily = Pixel, color = BlackBoardYellow)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Remembered your password?",
                fontFamily = Pixel,
                color = BlackBoardYellow
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Log In",
                color = BlackBoardYellow,
                modifier = Modifier.clickableNoRipple { onNavigateToLogin() },
                fontFamily = Pixel
            )
        }
    }
}