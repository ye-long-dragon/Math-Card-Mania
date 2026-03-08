package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import com.example.baraclan.mentalchallengemath_namepending.R
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow

val Pixel = FontFamily(Font(R.font.bit))

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun menu(
    onLogout: () -> Unit,
    onAboutClick: () -> Unit,
    onEditDeckClick: () -> Unit,
    onStartGameClick: () -> Unit,
    onMultiplayerGameClick: () -> Unit,
    onTutorialClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ── Logo ──────────────────────────────────────────────
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Math Card Mania Logo",
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── App name ──────────────────────────────────────────
        Text(
            text = "Math Card Mania",
            style = MaterialTheme.typography.headlineLarge,
            fontFamily = Pixel,
            color = BlackBoardYellow,
            textAlign = TextAlign.Center,
            fontSize = 32.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onStartGameClick) {
            Text("Start Game", fontFamily = Pixel)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onMultiplayerGameClick) {
            Text("Multiplayer", fontFamily = Pixel)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onTutorialClick) {
            Text("Tutorial", fontFamily = Pixel)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onEditDeckClick) {
            Text("Edit Deck", fontFamily = Pixel)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onProfileClick) {
            Text("Profile", fontFamily = Pixel)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onAboutClick) {
            Text("About", fontFamily = Pixel)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onLogout) {
            Text("Logout", fontFamily = Pixel)
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
        // ── Logo ──────────────────────────────────────────────
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Math Card Mania",
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = Pixel,
            color = BlackBoardYellow,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "About the Game",
            style = MaterialTheme.typography.titleMedium,
            fontFamily = Pixel,
            color = BlackBoardYellow
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Math Card Mania is a card game made by Vince Lawrence Baraclan and " +
                    "Zoey Liana Gonzales for their Mobile Development Final Project.\n\n" +
                    "The game includes a tutorial, multiplayer mode, and a single-player mode with 10 rounds.",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = Pixel,
            color = BlackBoardYellow,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Developers",
            style = MaterialTheme.typography.titleMedium,
            fontFamily = Pixel,
            color = BlackBoardYellow
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DeveloperFace(
                icon = R.drawable.vince,
                name = "Vince Lawrence\nBaraclan"
            )
            DeveloperFace(
                icon = R.drawable.zoey,
                name = "Zoey Liana\nGonzales"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onNavigateToMenu) {
            Text("Back to Menu", fontFamily = Pixel, color = BlackBoardYellow)
        }
    }
}

@Composable
fun MultiplayerSelectScreen(
    onNavigateToMenu: () -> Unit,
    onNavigateToLocal: () -> Unit,
    onNavigateToOnline: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Multiplayer Modes",
            style = MaterialTheme.typography.headlineLarge,
            fontFamily = Pixel,
            color = BlackBoardYellow
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onNavigateToLocal) {
            Text("Local Multiplayer", fontFamily = Pixel, color = BlackBoardYellow)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onNavigateToOnline) {
            Text("Online Multiplayer", fontFamily = Pixel, color = BlackBoardYellow)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onNavigateToMenu) {
            Text("Return to Menu", fontFamily = Pixel, color = BlackBoardYellow)
        }
    }
}