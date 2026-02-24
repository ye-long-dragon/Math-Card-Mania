package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.baraclan.mentalchallengemath_namepending.models.*
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow

@Composable
fun TutorialView(
    initialDeck: deck,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tutorial",
            style = MaterialTheme.typography.headlineLarge,
            fontFamily = Pixel,
            color = BlackBoardYellow
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Learn how to play by completing the first 3 rounds!",
            style = MaterialTheme.typography.titleMedium,
            fontFamily = Pixel,
            color = BlackBoardYellow
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Display tutorial game view (first 3 rounds only)
        GameView(
            initialDeck = initialDeck,
            onGameComplete = onNavigateBack,
            tutorialMode = true,
            maxRounds = 3
        )
    }
}
