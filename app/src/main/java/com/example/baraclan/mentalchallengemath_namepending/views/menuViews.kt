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
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.baraclan.mentalchallengemath_namepending.R
val Pixel= FontFamily(Font(R.font.bit))

@OptIn(ExperimentalLayoutApi::class)

@Composable
fun menu(
    onLogout: () -> Unit,
    onAboutClick: () -> Unit,
    onEditDeckClick: () -> Unit,
    onStartGameClick: () -> Unit // ADDED: New parameter
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Main Menu", style = MaterialTheme.typography.headlineLarge,fontFamily = Pixel)
        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onStartGameClick) { // ADDED: Button to start the game
            Text("Start Game",fontFamily = Pixel)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onEditDeckClick) {
            Text("Edit Deck",fontFamily = Pixel)
        }
        Spacer(modifier =  Modifier.height(16.dp))

        Button(onClick = onAboutClick) {
            Text("About",fontFamily = Pixel)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onLogout) {
            Text("Logout",fontFamily = Pixel)
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
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = Pixel
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "(Game Name) is a card game made by Vince Lawrence Baraclan and " +
                    "Zoey Liana Gonzales for their Mobile Development Final Project.\n\n" +
                    "The game includes a tutorial, multiplayer mode, and a single-player mode with 5 levels.",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = Pixel
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- Developers Section ---
        Text(
            text = "Developers",
            style = MaterialTheme.typography.titleMedium,
            fontFamily = Pixel
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
            Text("Back to Menu",
                fontFamily = Pixel)
        }
    }
}