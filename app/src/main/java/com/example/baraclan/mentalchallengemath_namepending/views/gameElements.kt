package com.example.baraclan.mentalchallengemath_namepending.views // Corrected package name


import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.heightIn
import com.example.baraclan.mentalchallengemath_namepending.*
import com.example.baraclan.mentalchallengemath_namepending.models.*



@Composable
fun statusBar(score: Int, turn: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Score: $score", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Text("Turn: $turn", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun goal(gameGoals: List<Double>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Current Goal:", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        // For simplicity, just display the first goal for now
        if (gameGoals.isNotEmpty()) {
            Text(gameGoals.first().toString(), style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
        } else {
            Text("No Goal Set", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
public fun EquationDisplay(
    equationElements: List<cardGame>, // Changed to List<cardGame>
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp) // Ensure a minimum height even if empty or short
            .padding(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant, // A slightly different background color
        shape = MaterialTheme.shapes.medium, // Default medium rounded corners
        shadowElevation = 2.dp // A subtle shadow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState) // Allow scrolling for long equations
                .padding(horizontal = 8.dp, vertical = 4.dp), // Padding inside the surface
            horizontalArrangement = Arrangement.spacedBy(4.dp), // Space between cards
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (equationElements.isEmpty()) {
                Text(
                    text = "Your equation will appear here.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp)
                )
            } else {
                equationElements.forEach { element ->
                    // Using the new CardGameDisplay
                    CardGameDisplay(card = element, onClick = {}) // Cards in equation are not interactive here for now
                }
            }
        }
    }
}





