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
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.sp // Added for text size adjustment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person

@OptIn(ExperimentalLayoutApi::class)
@Composable
public fun menu(
    onPlayGameClick: () -> Unit = {},
    onEditDeckClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onLogout: () -> Unit // <--- Renamed from onBackToLogin to onLogout
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Play Game Button
            Button(
                onClick = onPlayGameClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(vertical = 8.dp)
            ) {
                Text("Play Game", fontSize = 20.sp)
            }

            // Edit Deck Button
            Button(
                onClick = onEditDeckClick, // <--- Trigger the callback here
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(vertical = 8.dp)
            ) {
                Text("Edit Deck", fontSize = 20.sp)
            }

            // Profile Button
            Button(
                onClick = onProfileClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(vertical = 8.dp)
            ) {
                Text("Profile", fontSize = 20.sp)
            }

            // About Button
            Button(
                onClick = onAboutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(vertical = 8.dp)
            ) {
                Text("About", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(32.dp)) // Add some space before the logout button

            // Logout Button <--- NEW!
            Button(
                onClick = onLogout, // <--- This will trigger the navigation back to Login
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(vertical = 8.dp),

                ) {
                Text("Logout", fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun AboutScreen(
    onNavigateToMenu: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "About the Game",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "(Game Name) is a card game made by Vince Lawrence Baraclan and " +
                    "Zoey Liana Gonzales for their Mobile Development Final Project.\n\n" +
                    "The game includes a tutorial, multiplayer mode, and a single-player mode with 5 levels.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- Developers Section ---
        Text(
            text = "Developers",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DeveloperFace(
                icon = Icons.Default.Person,
                name = "Vince Lawrence\nBaraclan"
            )

            DeveloperFace(
                icon = Icons.Default.Person,
                name = "Zoey Liana\nGonzales"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onNavigateToMenu) {
            Text("Back to Menu")
        }
    }
}