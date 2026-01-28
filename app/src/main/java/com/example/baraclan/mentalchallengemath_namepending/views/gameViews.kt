package com.example.baraclan.mentalchallengemath_namepending.views // Corrected package name


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
// Removed duplicate import for fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
// Removed duplicate import for Text
import androidx.compose.ui.Alignment // <--- ADDED: Missing import for Alignment
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow // Requires ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
// Removed duplicate import for padding
import androidx.compose.foundation.horizontalScroll
// Removed duplicate import for Row, fillMaxWidth, padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember



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
                    card(content = element)
                }
            }
        }
    }
}

@Composable
public fun inputCards(
    currentExpression: List<String>, // The list of cards currently in the input
    modifier: Modifier = Modifier
) {
    // A horizontal scroll state to allow scrolling if the expression gets very long
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth() // Make the input area take full width
            .padding(8.dp) // Padding around the entire input row
            .horizontalScroll(scrollState), // Allow horizontal scrolling
        horizontalArrangement = Arrangement.spacedBy(4.dp), // Space between cards
        verticalAlignment = Alignment.CenterVertically // Center cards vertically if heights vary
    ) {
        if (currentExpression.isEmpty()) {
            // Display a placeholder if no cards have been input yet
            Text(
                text = "Tap cards to build your expression...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 8.dp)
            )
        } else {
            currentExpression.forEach { cardContent ->
                // Display each selected card using the MathElementCard (your 'card' composable)
                card(content = cardContent)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
public fun cardDeck(
    cardContents: List<String>, // A list of strings, expected to have 10 elements
    modifier: Modifier = Modifier,
    onCardClick: ((String) -> Unit)? = null // Optional callback for when a card is clicked
) {
    // Ensure that we only display up to 10 cards if the list is longer,
    // or handle cases where it's shorter. For this scenario, we'll assume
    // the 'cardContents' list will contain exactly 10 items for the deck.
    require(cardContents.size <= 10) { "CardDeck expects a list with up to 10 elements." }


    FlowRow(
        modifier = modifier
            .fillMaxWidth() // Make the deck take full width to allow wrapping
            .padding(horizontal = 8.dp, vertical = 4.dp), // Padding around the entire deck
        horizontalArrangement = Arrangement.Center, // Center the cards horizontally
        verticalArrangement = Arrangement.spacedBy(8.dp) // Space between rows if cards wrap
    ) {
        cardContents.forEach { content ->
            card(
                content = content,
                modifier = Modifier.padding(4.dp), // Padding around each individual card
                onClick = { onCardClick?.invoke(content) }
            )
        }
    }
}

@Composable
public fun card(
    content: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null // Optional click handler for interactivity
) {
    Card(
        modifier = modifier
            .size(64.dp)
            .padding(4.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        indication = LocalIndication.current, // ✅ Material3-compliant ripple
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = CardDefaults.shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
