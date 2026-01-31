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



@Composable
public fun statusBar(
    level: Int,
    coins: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween // Distributes content
    ) {
        // Correct way to pass the text parameter directly
        Text(text = "Level: $level")
        Text(text = "Coins: $coins")
    }
}

@Composable
public fun goal(
    goalNumber: Int, // The target number the player needs to reach
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth() // Take full width
            .padding(8.dp), // Padding around the goal card
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Padding inside the card
            horizontalAlignment = Alignment.CenterHorizontally // Center the content
        ) {
            Text(
                text = "Goal:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = goalNumber.toString(), // Display the goal number
                style = MaterialTheme.typography.displayLarge, // Make the goal number very prominent
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
public fun EquationDisplay(
    equationElements: List<String>, // The list of cards forming the equation
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState() // For horizontal scrolling if the equation is long

    Surface( // Using Surface to give it a distinct background and elevation
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
                    // Display each element using MathElementCard (your 'card' composable)
                    //cardView(content = element)
                }
            }
        }
    }
}





