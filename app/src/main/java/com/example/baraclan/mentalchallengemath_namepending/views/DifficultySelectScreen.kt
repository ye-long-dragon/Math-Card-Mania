package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baraclan.mentalchallengemath_namepending.models.Difficulty
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow

@Composable
fun DifficultySelectScreen(
    title: String = "Select Difficulty",
    onDifficultySelected: (Difficulty) -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontFamily = Pixel,
            color = BlackBoardYellow,
            fontSize = 28.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        DifficultyButton(
            label = "Easy",
            description = "Goals from -100 to 100\n2 decimal places • 10 rounds",
            color = Color(0xFF4CAF50),
            onClick = { onDifficultySelected(Difficulty.EASY) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        DifficultyButton(
            label = "Medium",
            description = "Goals from -1,000 to 1,000\n3 decimal places • 10 rounds",
            color = Color(0xFFFF9800),
            onClick = { onDifficultySelected(Difficulty.MEDIUM) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        DifficultyButton(
            label = "Hard",
            description = "Goals from -10,000 to 10,000\n5 decimal places • 10 rounds",
            color = Color(0xFFF44336),
            onClick = { onDifficultySelected(Difficulty.HARD) }
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedButton(onClick = onNavigateBack) {
            Text("Back", fontFamily = Pixel, color = BlackBoardYellow)
        }
    }
}

@Composable
private fun DifficultyButton(
    label: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .border(2.dp, color, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontFamily = Pixel,
                color = color,
                fontSize = 22.sp
            )
            Text(
                text = description,
                fontFamily = Pixel,
                color = BlackBoardYellow.copy(alpha = 0.7f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}