package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.LocalIndication // Removed as rememberRipple is preferred
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.material3.TextField // This might be unused, consider removing
import androidx.compose.material3.Button
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.input.PasswordVisualTransformation
// Removed duplicate imports for clickable, indication, Text, remember, dp


@Composable
public fun LoginScreen(
     // Callback for navigating to sign up
    onLoginSuccess: () -> Unit,      // Callback for successful login
    onNavigateToSignUp: () -> Unit,
    onForgotPassword:( ) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome Back!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Username
            var username by remember { mutableStateOf("admin") }
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                singleLine = true
            )

            // Password
            var password by remember { mutableStateOf("admin") }
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            // Save password box
            var savePassword by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = savePassword,
                    onCheckedChange = { savePassword = it }
                )
                Text(text = "Remember me")
            }

            // Forgot password button
            Text(
                text = "Forgot password?",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .clickable(
                        // FIX: Added interactionSource and indication
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        println("Forgot password clicked!")
                        onForgotPassword()
                    }
            )

            // Login button
            Button(
                onClick = {
                    // Handle login logic
                    println("Login attempted with Username: $username, Password: $password")
                    onLoginSuccess() // Trigger the success callback
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Option to go to Sign Up screen
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account?")
                Text(
                    text = " Sign Up",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable(
                            // FIX: Added interactionSource and indication
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onNavigateToSignUp() // Trigger navigation to Sign Up
                        }
                        .padding(start = 4.dp)
                )
            }
        }
    }
}

// Your SignInScreen is already correct regarding the clickable modifier for "Log In"
// It's included below for completeness but no changes are needed there for ripple.
@Composable
public fun SignInScreen(
    onNavigateToLogin: () -> Unit, // Callback for navigating back to login
    onSignInSuccess: () -> Unit    // Callback for successful sign in
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
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
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Email
            var email by remember { mutableStateOf("") }
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                singleLine = true
            )

            // Username
            var username by remember { mutableStateOf("") }
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                singleLine = true
            )

            // Password
            var password by remember { mutableStateOf("") }
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            // Re-enter password
            var reEnteredPassword by remember { mutableStateOf("") }
            OutlinedTextField(
                value = reEnteredPassword,
                onValueChange = { reEnteredPassword = it },
                label = { Text("Re-enter Password") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            // Save account button
            Button(
                onClick = {
                    // Handle account creation logic
                    println("Account creation attempted with Email: $email, Username: $username")
                    // You might want to add validation here (e.g., passwords match)
                    onSignInSuccess() // Trigger the success callback
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Create Account")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Option to go back to Login screen
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account?")
                Text(
                    text = " Log In",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onNavigateToLogin() // Trigger navigation back to Login
                        }
                        .padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
public fun ForgotPasswordScreen(
    onNavigateToLogin: () -> Unit,
){
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
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
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Email input
            var email by remember { mutableStateOf("") }
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter your Email") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true
            )

            // Reset Password button
            Button(
                onClick = {
                    // Handle password reset logic here
                    println("Password reset requested for email: $email")
                    // In a real app, you'd send an API request here
                    // For now, let's assume it's done and navigate back to login
                    onNavigateToLogin() // Navigate back after submission
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = email.isNotBlank() // Enable button only if email is not blank
            ) {
                Text("Reset Password")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Option to go back to Login screen
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Remembered your password?")
                Text(
                    text = " Log In",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            // indication = rememberRipple() // Uncomment for ripple effect
                            indication = null // Or keep it null for no ripple
                        ) {
                            onNavigateToLogin() // Trigger navigation back to Login
                        }
                        .padding(start = 4.dp)
                )
            }
        }
    }
}
