package com.example.baraclan.mentalchallengemath_namepending.views

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.baraclan.mentalchallengemath_namepending.R
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ─────────────────────────────────────────────────────────────
// Session DataStore — persists login for 30 days
// ─────────────────────────────────────────────────────────────
private val Context.sessionDataStore by preferencesDataStore(name = "session")

private val KEY_UID        = stringPreferencesKey("uid")
private val KEY_LOGIN_TIME = longPreferencesKey("login_time")
private val KEY_IS_OFFLINE = androidx.datastore.preferences.core.booleanPreferencesKey("is_offline")

private const val SESSION_DURATION_MS = 30L * 24 * 60 * 60 * 1000  // 30 days

object SessionManager {
    /** Save a successful online login. */
    suspend fun saveOnlineSession(context: Context, uid: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_UID]        = uid
            prefs[KEY_LOGIN_TIME] = System.currentTimeMillis()
            prefs[KEY_IS_OFFLINE] = false
        }
    }

    /** Save offline mode (no uid needed). */
    suspend fun saveOfflineSession(context: Context) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_UID]        = ""
            prefs[KEY_LOGIN_TIME] = System.currentTimeMillis()
            prefs[KEY_IS_OFFLINE] = true
        }
    }

    /** Returns true if a valid (non-expired) session exists. */
    suspend fun hasValidSession(context: Context): Boolean {
        val prefs     = context.sessionDataStore.data.first()
        val loginTime = prefs[KEY_LOGIN_TIME] ?: return false
        val elapsed   = System.currentTimeMillis() - loginTime
        return elapsed < SESSION_DURATION_MS
    }

    /** Returns true if the saved session is offline mode. */
    suspend fun isOfflineSession(context: Context): Boolean {
        val prefs = context.sessionDataStore.data.first()
        return prefs[KEY_IS_OFFLINE] ?: false
    }

    /** Clear session on logout. */
    suspend fun clearSession(context: Context) {
        context.sessionDataStore.edit { it.clear() }
    }
}

// ─────────────────────────────────────────────────────────────
// Shared helpers
// ─────────────────────────────────────────────────────────────
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = this.clickable(
    interactionSource = MutableInteractionSource(),
    indication = null,
    onClick = onClick
)

fun String.isValidEmail(): Boolean = Patterns.EMAIL_ADDRESS.matcher(this).matches()
fun String.isStrongPassword(): Boolean = this.length >= 8 && this.any { it.isDigit() }

// ─────────────────────────────────────────────────────────────
// Password field with eye icon
// ─────────────────────────────────────────────────────────────
@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: @Composable (() -> Unit)? = null
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = Pixel) },
        supportingText = supportingText,
        modifier = modifier,
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (visible) "Hide password" else "Show password",
                    tint = BlackBoardYellow.copy(alpha = 0.7f)
                )
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────
// Firebase Manager
// ─────────────────────────────────────────────────────────────
object FirebaseManager {
    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

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

    suspend fun signUpUser(username: String, email: String, password: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid
                ?: return Result.failure(Exception("Sign up failed. Please try again."))
            val userDoc = hashMapOf("username" to username, "email" to email, "uid" to uid)
            db.collection("users").document(uid).set(userDoc).await()
            Log.d("FirebaseManager", "Firestore user document created for uid=$uid")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "SignUp error: ${e.message}")
            Result.failure(Exception(e.message ?: "Sign up failed. Please try again."))
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
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
    onPlayOffline: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onForgotPassword: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Auto-login if valid session exists
    LaunchedEffect(Unit) {
        if (SessionManager.hasValidSession(context)) {
            if (SessionManager.isOfflineSession(context)) onPlayOffline()
            else onLoginSuccess()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Math Card Mania Logo",
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Math Card Mania",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp),
                fontSize = 42.sp, fontFamily = Pixel,
                textAlign = TextAlign.Center, color = BlackBoardYellow
            )

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email", fontFamily = Pixel) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            PasswordField(
                value = password, onValueChange = { password = it },
                label = "Password",
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            Text(
                text = "Forgot password?",
                color = BlackBoardYellow,
                modifier = Modifier.padding(bottom = 16.dp).clickableNoRipple { onForgotPassword() },
                fontFamily = Pixel
            )

            errorMessage?.let {
                Text(it, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp), fontFamily = Pixel)
            }

            // ── Login button ──────────────────────────────────
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) { errorMessage = "Please fill all fields"; return@Button }
                    if (!email.isValidEmail()) { errorMessage = "Please enter a valid email address"; return@Button }
                    isLoading = true; errorMessage = null
                    scope.launch {
                        val result = FirebaseManager.loginUser(email, password)
                        isLoading = false
                        result
                            .onSuccess { uid ->
                                SessionManager.saveOnlineSession(context, uid)
                                onLoginSuccess()
                            }
                            .onFailure { e -> errorMessage = e.message }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BlackBoardYellow, strokeWidth = 2.dp)
                else Text("Login", fontFamily = Pixel, color = BlackBoardYellow)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Play Offline button ───────────────────────────
            OutlinedButton(
                onClick = {
                    scope.launch {
                        SessionManager.saveOfflineSession(context)
                        onPlayOffline()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Play Offline", fontFamily = Pixel, color = BlackBoardYellow)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Don't have an account?", fontFamily = Pixel, color = BlackBoardYellow)
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

// ─────────────────────────────────────────────────────────────
// 2. Sign Up Screen
// ─────────────────────────────────────────────────────────────
@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var reEnteredPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Create Your Account",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp),
                color = BlackBoardYellow, fontFamily = Pixel
            )

            OutlinedTextField(
                value = username, onValueChange = { username = it },
                label = { Text("Username", fontFamily = Pixel) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email", fontFamily = Pixel) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            PasswordField(
                value = password, onValueChange = { password = it },
                label = "Password",
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                supportingText = { Text("Min. 8 characters, at least 1 number", fontFamily = Pixel) }
            )

            PasswordField(
                value = reEnteredPassword, onValueChange = { reEnteredPassword = it },
                label = "Re-enter Password",
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            errorMessage?.let {
                Text(it, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp), fontFamily = Pixel)
            }

            Button(
                onClick = {
                    if (username.isBlank() || email.isBlank() || password.isBlank()) { errorMessage = "Please fill all fields"; return@Button }
                    if (!email.isValidEmail()) { errorMessage = "Please enter a valid email address"; return@Button }
                    if (!password.isStrongPassword()) { errorMessage = "Password must be at least 8 characters and include a number"; return@Button }
                    if (password != reEnteredPassword) { errorMessage = "Passwords do not match"; return@Button }
                    isLoading = true; errorMessage = null
                    scope.launch {
                        val result = FirebaseManager.signUpUser(username, email, password)
                        isLoading = false
                        result
                            .onSuccess {
                                // Save session right after sign-up
                                val uid = Firebase.auth.currentUser?.uid ?: ""
                                SessionManager.saveOnlineSession(context, uid)
                                onSignUpSuccess()
                            }
                            .onFailure { e -> errorMessage = e.message }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BlackBoardYellow, strokeWidth = 2.dp)
                else Text("Create Account", fontFamily = Pixel, color = BlackBoardYellow)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Already have an account?", fontFamily = Pixel, color = BlackBoardYellow)
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
fun ForgotPasswordScreen(onNavigateToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Forgot Password?",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp),
                fontFamily = Pixel, color = BlackBoardYellow
            )
            Text(
                text = "Enter your email and we'll send you a reset link.",
                fontFamily = Pixel, color = BlackBoardYellow.copy(alpha = 0.7f),
                textAlign = TextAlign.Center, fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Enter your Email", fontFamily = Pixel) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            errorMessage?.let { Text(it, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp), fontFamily = Pixel) }
            successMessage?.let { Text(it, color = Color.Green, modifier = Modifier.padding(bottom = 8.dp), fontFamily = Pixel) }

            Button(
                onClick = {
                    if (email.isBlank()) { errorMessage = "Please enter your email"; return@Button }
                    if (!email.isValidEmail()) { errorMessage = "Please enter a valid email address"; return@Button }
                    isLoading = true; errorMessage = null
                    scope.launch {
                        val result = FirebaseManager.sendPasswordResetEmail(email)
                        isLoading = false
                        result.onSuccess {
                            successMessage = "Reset link sent! Check your inbox."
                            delay(2500)
                            onNavigateToLogin()
                        }.onFailure { e -> errorMessage = e.message }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BlackBoardYellow, strokeWidth = 2.dp)
                else Text("Send Reset Link", fontFamily = Pixel, color = BlackBoardYellow)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Remembered your password?", fontFamily = Pixel, color = BlackBoardYellow)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Log In", color = BlackBoardYellow,
                modifier = Modifier.clickableNoRipple { onNavigateToLogin() },
                fontFamily = Pixel
            )
        }
    }
}