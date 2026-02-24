package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow

@Composable
fun CongratulationsScreen(
    finalScore: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Congratulations!",
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = Pixel,
                color = BlackBoardYellow
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "You've completed all goals!",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Pixel,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Final Score: $finalScore",
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = Pixel,
                    color = BlackBoardYellow
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Return to Menu", fontFamily = Pixel, color = BlackBoardYellow)
            }
        }
    )
}
