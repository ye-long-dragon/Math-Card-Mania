package com.example.baraclan.mentalchallengemath_namepending.views

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.baraclan.mentalchallengemath_namepending.R
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow


@Composable
public fun LoginScreen(
    onLoginSuccess: () -> Unit,      // Callback for successful login
    onNavigateToSignUp: () -> Unit,  // Callback for navigating to sign up
    onForgotPassword:( ) -> Unit     // Callback for navigating to forgot password
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent // Ensure transparency to show background image
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
                textAlign = TextAlign.Center,color = BlackBoardYellow
            )

            // Username
            var username by remember { mutableStateOf("admin") } // Default value for testing
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", fontFamily = Pixel) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true,
            )

            // Password
            var password by remember { mutableStateOf("admin") } // Default value for testing
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

            //Error Message
            var errorMessage by remember { mutableStateOf<String?>(null) }

            // Save password box
            var savePassword by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = savePassword,
                    onCheckedChange = { savePassword = it }
                )
                Text(
                    text = "Remember me",
                    fontFamily = Pixel,
                    color = BlackBoardYellow
                )
            }

            // Forgot password button
            Text(
                text = "Forgot password?",
                color = BlackBoardYellow,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // Or use rememberRipple() if a ripple effect is desired
                    ) {
                        onForgotPassword() // Trigger the callback here
                    },
                fontFamily = Pixel,

            )

            // Login button
            Button(
                onClick = {
                    // Clear any previous error message
                    errorMessage = "Please Input Admin in username and password"

                    println("Login attempted with Username: $username, Password: $password")

                    // --- Corrected Login Validation Logic ---
                    if (username == "admin" && password == "admin") { // Correct comparison
                        onLoginSuccess() // Trigger the success callback
                    } else { // 'else' block must contain code
                        errorMessage = "Invalid username or password." // Set error message
                        println("Login failed for user: $username")
                    }
                    // --- End Corrected Login Validation Logic ---
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Login",
                    fontFamily = Pixel // Assuming Pixel is a custom FontFamily
                    ,color = BlackBoardYellow
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Option to go to Sign Up screen
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Don't have an account?",
                    fontFamily = Pixel,
                    color = BlackBoardYellow
                )
            }

            Spacer(modifier = Modifier.height(16.dp)) // Add space between the text and clickable "Sign Up"

            Text(
                text = "Sign Up", // Text for the Sign Up link
                color = BlackBoardYellow,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // Or use rememberRipple() if a ripple effect is desired
                    ) {
                        onNavigateToSignUp() // Trigger navigation to Sign Up
                    },
                fontFamily = Pixel
            )
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
                text = "Create Your Account",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp),
                color = BlackBoardYellow
            )

            // Email
            var email by remember { mutableStateOf("") }
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", fontFamily = Pixel) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                singleLine = true
            )

            // Username
            var username by remember { mutableStateOf("") }
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", fontFamily = Pixel) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                singleLine = true
            )

            // Password
            var password by remember { mutableStateOf("") }
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", fontFamily = Pixel) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            // Re-enter password
            var reEnteredPassword by remember { mutableStateOf("") }
            OutlinedTextField(
                value = reEnteredPassword,
                onValueChange = { reEnteredPassword = it },
                label = { Text("Re-enter Password", fontFamily = Pixel) },
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
                Text("Create Account",fontFamily = Pixel,color = BlackBoardYellow)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Option to go back to Login screen
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account?",fontFamily = Pixel,color = BlackBoardYellow)
            }
            Spacer(modifier = Modifier.height(16.dp))

             Row(verticalAlignment = Alignment.CenterVertically){
                  Text(
                      text = " Log In",
                      color = BlackBoardYellow,
                      modifier = Modifier
                          .clickable(
                              interactionSource = remember { MutableInteractionSource() },
                              indication = null
                          ) {
                              onNavigateToLogin() // Trigger navigation back to Login
                          }
                          .padding(start = 4.dp)   ,
                      fontFamily = Pixel
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
                modifier = Modifier.padding(bottom = 32.dp),
                fontFamily = Pixel,
                color = BlackBoardYellow
            )

            // Email input
            var email by remember { mutableStateOf("") }
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter your Email", fontFamily = Pixel) },
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
                Text("Reset Password", fontFamily = Pixel,color = BlackBoardYellow)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Option to go back to Login screen
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Remembered your password?", fontFamily = Pixel)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically){
                Text(
                    text = " Log In",
                    color = BlackBoardYellow,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            // indication = rememberRipple() // Uncomment for ripple effect
                            indication = null // Or keep it null for no ripple
                        ) {
                            onNavigateToLogin() // Trigger navigation back to Login
                        }
                        .padding(start = 4.dp),
                    fontFamily = Pixel
                )
                
            }
        }
    }
}