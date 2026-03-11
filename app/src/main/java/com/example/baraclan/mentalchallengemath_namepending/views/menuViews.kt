package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.Color
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
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val navBot    = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = statusTop, bottom = navBot)
    ) {
        val screenH = maxHeight
        // Scale spacing and logo to available height
        val logoSize   = (screenH * 0.12f).coerceIn(60.dp, 110.dp)
        val btnSpacing = (screenH * 0.012f).coerceIn(4.dp, 14.dp)
        val titleSize  = (screenH.value * 0.045f).coerceIn(18f, 32f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Logo ──────────────────────────────────────────
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Math Card Mania Logo",
                modifier = Modifier.size(logoSize),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(btnSpacing))

            // ── App name ──────────────────────────────────────
            Text(
                text = "Math Card Mania",
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = Pixel,
                color = BlackBoardYellow,
                textAlign = TextAlign.Center,
                fontSize = titleSize.sp
            )

            Spacer(modifier = Modifier.height(btnSpacing * 2))

            Button(onClick = onStartGameClick,   modifier = Modifier.fillMaxWidth(0.6f)) { Text("Start Game",   fontFamily = Pixel) }
            Spacer(modifier = Modifier.height(btnSpacing))
            Button(onClick = onMultiplayerGameClick, modifier = Modifier.fillMaxWidth(0.6f)) { Text("Multiplayer", fontFamily = Pixel) }
            Spacer(modifier = Modifier.height(btnSpacing))
            Button(onClick = onTutorialClick,    modifier = Modifier.fillMaxWidth(0.6f)) { Text("Tutorial",     fontFamily = Pixel) }
            Spacer(modifier = Modifier.height(btnSpacing))
            Button(onClick = onEditDeckClick,    modifier = Modifier.fillMaxWidth(0.6f)) { Text("Edit Deck",    fontFamily = Pixel) }
            Spacer(modifier = Modifier.height(btnSpacing))
            Button(onClick = onProfileClick,     modifier = Modifier.fillMaxWidth(0.6f)) { Text("Profile",      fontFamily = Pixel) }
            Spacer(modifier = Modifier.height(btnSpacing))
            Button(onClick = onSettingsClick,    modifier = Modifier.fillMaxWidth(0.6f)) { Text("Settings",     fontFamily = Pixel) }
            Spacer(modifier = Modifier.height(btnSpacing))
            Button(onClick = onAboutClick,       modifier = Modifier.fillMaxWidth(0.6f)) { Text("About",        fontFamily = Pixel) }
            Spacer(modifier = Modifier.height(btnSpacing))
            Button(onClick = onLogout,           modifier = Modifier.fillMaxWidth(0.6f)) { Text("Logout",       fontFamily = Pixel) }
            Spacer(modifier = Modifier.height(btnSpacing))
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
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Math Card Mania Logo",
            modifier = Modifier.size(80.dp),
            contentScale = ContentScale.Fit
        )

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
    isOfflineMode: Boolean = false
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

        if (!isOfflineMode) {
            Button(onClick = onNavigateToOnline) {
                Text("Online Multiplayer", fontFamily = Pixel, color = BlackBoardYellow)
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Text(
                text = "Online Multiplayer unavailable\n(playing offline)",
                fontFamily = Pixel,
                color = BlackBoardYellow.copy(alpha = 0.4f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(onClick = onNavigateToMenu) {
            Text("Return to Menu", fontFamily = Pixel, color = BlackBoardYellow)
        }
    }
}